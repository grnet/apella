package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.*;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class CandidacyService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private RegisterService registerService;

    @EJB
    private MailService mailService;

    @EJB
    private CandidateService candidateService;

    @EJB
    private PositionService positionService;

    @EJB
    private PositionCandidaciesService positionCandidaciesService;


    public Candidacy getCandidacy(Long id, boolean loadSnapshotFiles) throws NotFoundException {
        try {
            Candidacy candidacy = em.createQuery(
                    "from Candidacy c " +
                            "left join fetch c.proposedEvaluators pe " +
                            "where c.id=:id", Candidacy.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // fetch lazy loaded snapshotsFiles Collection
            if (loadSnapshotFiles) {
                candidacy.getSnapshotFiles().size();
            }

            return candidacy;

        } catch (NoResultException e) {
            throw new NotFoundException("wrong.candidacy.id");
        }
    }

    public Candidacy getCandidacyByIdAndPositionId(Long candidateId, Long positionId) {

        try {
            Candidacy candidacy = em.createQuery(
                    "select c from Candidacy c " +
                            "where c.candidate.id = :candidateId " +
                            "and c.candidacies.position.id = :positionId", Candidacy.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("positionId", positionId)
                    .getSingleResult();

            return candidacy;

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Candidacy> getIncompleteCandidacies(User loggedOn) throws NotEnabledException {
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
        if (!admin.isSuperAdministrator()) {
            throw new NotEnabledException("insufficient.privileges");
        }

        List<Candidacy> incompleteCandidacies = em.createQuery("from Candidacy c " +
                "left join fetch c.candidate.user.roles cerls " +
                "left join fetch c.candidacies ca " +
                "left join fetch ca.position po " +
                "where c.permanent = false " +
                "or c.withdrawn = true ", Candidacy.class)
                .getResultList();

        return incompleteCandidacies;
    }

    public List<CandidacyStatus> getCandidacyStatus(Long candidacyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        List<CandidacyStatus> statusList = em.createQuery("Select u " +
                "from CandidacyStatus u " +
                "where u.candidacy.id=:candidacyId " +
                "order by u.date desc", CandidacyStatus.class)
                .setParameter("candidacyId", candidacyId)
                .getResultList();

        return statusList;
    }


    public Map<Candidacy, Class> get(Long id, User loggedOn) throws NotEnabledException, NotFoundException {

        Map<Candidacy, Class> result = new HashMap<>();

        // find candidacy
        Candidacy candidacy = getCandidacy(id, false);
        Candidate candidate = candidacy.getCandidate();

        // Full Access ADMINISTRATOR, MINISTRY, ISNSTITUTION, OWNER
        if (loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) ||
                loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) ||
                loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) ||
                loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) ||
                candidate.getUser().getId().equals(loggedOn.getId())) {
            // Full Access
            candidacy.getCandidacyEvalutionsDueDate(); // Load this to avoid lazy exception
            candidacy.setCanAddEvaluators(canAddEvaluators(candidacy));
            candidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(candidacy));
            result.put(candidacy, Candidacy.DetailedCandidacyView.class);
            return result;
        }
        // Medium Access COMMITTEE MEMBER, EVALUATOR
        if ((candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) ||
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn))) {
            // Medium (without ContactInformation)
            result.put(candidacy, Candidacy.MediumCandidacyView.class);
            return result;
        }
        // Medium Access OTHER CANDIDATE if isOpenToOtherCandidates
        if (candidacy.isOpenToOtherCandidates() && candidacy.getCandidacies().containsCandidate(loggedOn)) {
            // Medium (without ContactInformation)
            result.put(candidacy, Candidacy.MediumCandidacyView.class);
            return result;
        }
        // Medium Access CANDIDACY EVALUATOR
        if (candidacy.containsEvaluator(loggedOn)) {
            // Medium (without ContactInformation)
            result.put(candidacy, Candidacy.EvaluatorCandidacyView.class);
            return result;
        }
        throw new NotEnabledException("insufficient.privileges");
    }


    public Candidacy createCandidacy(Candidacy candidacy, User loggedOn) throws ValidationException, NotFoundException, NotEnabledException {

        Date now = new Date();
        // get candidate
        Candidate candidate = candidateService.getCandidate(candidacy.getCandidate().getId());

        // Validate
        if (!candidate.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // find position
        Position position = positionService.getPositionById(candidacy.getCandidacies().getPosition().getId());

        if (DateUtil.compareDates(position.getPhase().getCandidacies().getOpeningDate(), now) > 0) {
            throw new NotEnabledException("wrong.position.candidacies.openingDate");
        }
        if (DateUtil.compareDates(position.getPhase().getCandidacies().getClosingDate(), now) < 0) {
            throw new NotEnabledException("wrong.position.candidacies.closingDate");
        }
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI)) {
            throw new NotEnabledException("wrong.position.status");
        }
        // create or update candidacy
        // get candidacy if exists
        Candidacy existingCandidacy = getCandidacyByIdAndPositionId(candidacy.getId(), position.getId());

        if (existingCandidacy != null) {
            // update
            existingCandidacy.getCandidacyEvalutionsDueDate();
            existingCandidacy.setDate(new Date());
            CandidacyStatus status = new CandidacyStatus();
            status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
            status.setDate(new Date());
            existingCandidacy.addCandidacyStatus(status);

            em.merge(existingCandidacy);
            em.flush();

            return existingCandidacy;
        }
        // Create
        candidacy.setCandidate(candidate);
        candidacy.setCandidacies(position.getPhase().getCandidacies());
        candidacy.setDate(new Date());
        CandidacyStatus status = new CandidacyStatus();
        status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
        status.setDate(new Date());
        candidacy.addCandidacyStatus(status);
        candidacy.setOpenToOtherCandidates(false);
        candidacy.setPermanent(false);
        candidacy.getProposedEvaluators().clear();
        validateCandidacy(candidacy, candidate, true);
        updateSnapshot(candidacy, candidate);

        em.persist(candidacy);
        em.flush();

        // Return Results
        candidacy.getCandidacyEvalutionsDueDate();
        candidacy.setCanAddEvaluators(canAddEvaluators(candidacy));
        candidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(candidacy));
        return candidacy;
    }

    public Candidacy updateCandidacy(Long candidacyId, Candidacy candidacyToUpdate, User loggedOn) throws ValidationException, NotFoundException, NotEnabledException {
        // get candidacy
        Candidacy existingCandidacy = getCandidacy(candidacyId, false);

        if (!existingCandidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Candidate candidate = existingCandidacy.getCandidate();
        // get position
        Position position = existingCandidacy.getCandidacies().getPosition();
        // validations
        validateCandidacyToUpdate(position, existingCandidacy, candidacyToUpdate);

        boolean isNew = !existingCandidacy.isPermanent();

        //Check changes of Evaluators
        Map<Long, CandidacyEvaluator> existingRegisterMembersAsMap = new HashMap<Long, CandidacyEvaluator>();
        for (CandidacyEvaluator existingEvaluator : existingCandidacy.getProposedEvaluators()) {
            existingRegisterMembersAsMap.put(existingEvaluator.getRegisterMember().getId(), existingEvaluator);
        }

        Set<Long> newRegisterMemberIds = new HashSet<>();

        for (CandidacyEvaluator newEvaluator : candidacyToUpdate.getProposedEvaluators()) {
            if (newEvaluator.getRegisterMember() != null && newEvaluator.getRegisterMember().getId() != null) {
                newRegisterMemberIds.add(newEvaluator.getRegisterMember().getId());
            }
        }

        List<RegisterMember> newRegisterMembers = new ArrayList<>();
        if (!newRegisterMemberIds.isEmpty()) {
            newRegisterMembers = registerService.getRegisterMembers(candidacyToUpdate.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getId(), newRegisterMemberIds, true);
        }

        Set<Long> newRegisterMemberProfessorIds = new HashSet<Long>();

        for (RegisterMember member : newRegisterMembers) {
            // check if there are duplicate professors
            if (newRegisterMemberProfessorIds.contains(member.getProfessor().getId())) {
                // throw new RestException(Response.Status.CONFLICT, "duplicate.evaluators");
                throw new ValidationException("duplicate.evaluators");
            }
            newRegisterMemberProfessorIds.add(member.getProfessor().getId());
        }

        // check if the evaluators are in the position commitee
        if (position.getPhase().getCommittee() != null) {
            for (PositionCommitteeMember committeeMember : position.getPhase().getCommittee().getMembers()) {
                if (newRegisterMemberProfessorIds.contains(committeeMember.getRegisterMember().getProfessor().getId())) {
                    //throw new RestException(Response.Status.CONFLICT, "member.in.committe");
                    throw new ValidationException("member.in.committe");
                }
            }
        }

        Collection<CandidacyEvaluator> addedEvaluators = new ArrayList<CandidacyEvaluator>();

        // Update
        if (isNew) {
            // Fetch from Profile
            updateSnapshot(existingCandidacy, candidate);
        }
        existingCandidacy.setPermanent(true);
        existingCandidacy.setWithdrawn(false);
        existingCandidacy.setOpenToOtherCandidates(candidacyToUpdate.isOpenToOtherCandidates());
        existingCandidacy.getProposedEvaluators().clear();
        for (RegisterMember newRegisterMember : newRegisterMembers) {
            CandidacyEvaluator candidacyEvaluator;
            if (existingRegisterMembersAsMap.containsKey(newRegisterMember.getId())) {
                candidacyEvaluator = existingRegisterMembersAsMap.get(newRegisterMember.getId());
            } else {
                candidacyEvaluator = new CandidacyEvaluator();
                candidacyEvaluator.setCandidacy(existingCandidacy);
                candidacyEvaluator.setRegisterMember(newRegisterMember);
                addedEvaluators.add(candidacyEvaluator);
            }
            existingCandidacy.addProposedEvaluator(candidacyEvaluator);
        }

        em.flush();

        // send mails
        sendEmailsPriorToUpdate(isNew, existingCandidacy, addedEvaluators);

        // Return
        existingCandidacy.getCandidacyEvalutionsDueDate();
        existingCandidacy.setCanAddEvaluators(canAddEvaluators(existingCandidacy));
        existingCandidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(existingCandidacy));

        return existingCandidacy;
    }


    public void deleteCandidacy(Long candidacyId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {

        Candidacy existingCandidacy = getCandidacy(candidacyId, false);
        // get candidate
        Candidate candidate = existingCandidacy.getCandidate();

        if (!candidate.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Position position = existingCandidacy.getCandidacies().getPosition();
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI)
                && (!position.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI))) {
            //throw new RestException(Status.CONFLICT, "wrong.position.status");
            throw new ValidationException("wrong.position.status");
        }
        // NominationCommitteeConvergenceDate
        if (hasNominationCommitteeConverged(existingCandidacy)) {
            // throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
            throw new ValidationException("wrong.position.status.committee.converged");
        }

        if (existingCandidacy.isPermanent()) {
            // Send E-Mails
            sendEmailsPriorToDelete(existingCandidacy);
        }
        // Update
        existingCandidacy.setWithdrawn(true);
        existingCandidacy.setWithdrawnDate(new Date());
        if (DateUtil.compareDates(new Date(), existingCandidacy.getCandidacies().getClosingDate()) <= 0) {
            existingCandidacy.setPermanent(false);
        }
        CandidacyStatus status = new CandidacyStatus();
        status.setAction(CandidacyStatus.ACTIONS.WITHDRAW);
        status.setDate(new Date());
        existingCandidacy.addCandidacyStatus(status);

        em.merge(existingCandidacy);
        em.flush();
    }

    public Collection<RegisterMember> getCandidacyRegisterMembers(Long positionId, Long candidacyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get candidacy
        final Candidacy existingCandidacy = getCandidacy(candidacyId, false);
        // get candidate
        Candidate candidate = existingCandidacy.getCandidate();

        if (!candidate.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return empty response while committee is not defined
        if (!canAddEvaluators(existingCandidacy)) {
            return new ArrayList<>();
        }
        // Prepare Data for Query
        Institution institution = existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution();
        Set<Long> committeeMemberIds = new HashSet<Long>();
        if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
            PositionCommittee committee = existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee();
            for (PositionCommitteeMember member : committee.getMembers()) {
                committeeMemberIds.add(member.getRegisterMember().getId());
            }
        }
        if (committeeMemberIds.isEmpty()) {
            // This should not happen, but just to avoid exceptions in case it does
            return new ArrayList<>();
        }
        // get register members
        List<RegisterMember> registerMembers = registerService.getRegisterMembers(institution.getId(), committeeMemberIds, false);

        // Return result
        return registerMembers;
    }


    public Candidacy submitCandidacy(Candidacy candidacy, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
        // Authenticate
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        User user = em.find(User.class, candidacy.getCandidate().getUser().getId());
        if (user == null) {
            throw new ValidationException("wrong.user.id");
        }
        // Validate
        Candidate candidate = em.find(Candidate.class, user.getRole(Role.RoleDiscriminator.CANDIDATE).getId());
        if (candidate == null) {
            throw new ValidationException("wrong.candidate.id");
        }
        Position position = em.find(Position.class, candidacy.getCandidacies().getPosition().getId());
        if (position == null) {
            throw new ValidationException("wrong.position.id");
        }
        Candidacy existingCandidacy = null;
        try {
            existingCandidacy = em.createQuery(
                    "select c from Candidacy c " +
                            "where c.candidate.id = :candidateId " +
                            "and c.candidacies.position.id = :positionId", Candidacy.class)
                    .setParameter("candidateId", candidate.getId())
                    .setParameter("positionId", position.getId())
                    .getSingleResult();
        } catch (NoResultException e) {
        }

        if (existingCandidacy != null) {
            if (existingCandidacy.isPermanent() && !existingCandidacy.isWithdrawn()) {
                throw new ValidationException("candidacy.already.submitted");
            }
            existingCandidacy.setPermanent(true);
            existingCandidacy.setWithdrawn(false);
            existingCandidacy.setWithdrawnDate(null);
            existingCandidacy.setDate(new Date());
            CandidacyStatus status = new CandidacyStatus();
            status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
            status.setDate(new Date());
            existingCandidacy.addCandidacyStatus(status);
            candidacy = existingCandidacy;
        } else {
            candidacy.setCandidate(candidate);
            candidacy.setCandidacies(position.getPhase().getCandidacies());
            candidacy.setDate(new Date());
            candidacy.setOpenToOtherCandidates(false);
            candidacy.setPermanent(true);
            candidacy.setWithdrawn(false);
            candidacy.setWithdrawnDate(null);
            CandidacyStatus status = new CandidacyStatus();
            status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
            status.setDate(new Date());
            candidacy.addCandidacyStatus(status);

            candidacy.getProposedEvaluators().clear();
            validateCandidacy(candidacy, candidate, true); // isNew
            updateSnapshot(candidacy, candidate);
        }

        candidacy = em.merge(candidacy);
        em.flush();

        // Full Access
        candidacy.getCandidacyEvalutionsDueDate(); // Load this to avoid lazy exception
        candidacy.setCanAddEvaluators(canAddEvaluators(candidacy));
        candidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(candidacy));

        return candidacy;
    }

    public SearchData<RegisterMember> search(Long candidacyId, HttpServletRequest request, User loggedOn) throws NotFoundException, NotEnabledException {

        Candidacy existingCandidacy = getCandidacy(candidacyId, false);

        Candidate candidate = existingCandidacy.getCandidate();
        if (!candidate.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // Return empty response while committee is not defined
        if (!canAddEvaluators(existingCandidacy)) {
            return new SearchData<>();
        }
        // Prepare Data for Query
        Institution institution = existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution();
        Set<Long> committeeMemberProfessorIds = new HashSet<>();
        if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
            PositionCommittee committee = existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee();
            for (PositionCommitteeMember member : committee.getMembers()) {
                committeeMemberProfessorIds.add(member.getRegisterMember().getProfessor().getId());
            }
        }
        if (committeeMemberProfessorIds.isEmpty()) {
            // This should not happen, but just to avoid exceptions in case it does
            return new SearchData<>();
        }

        String filterText = request.getParameter("sSearch");
        String locale = request.getParameter("locale");
        String sEcho = request.getParameter("sEcho");
        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");
        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));

        // Prepare Query
        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append("from RegisterMember m " +
                "join m.professor p " +
                "where m.register.permanent = true " +
                "and m.register.institution.id = :institutionId " +
                "and m.deleted = false " +
                "and p.status = :status " +
                "and p.id not in (:committeeMemberProfessorIds) ");

        if (StringUtils.isNotEmpty(filterText)) {
            searchQueryString.append(" and ( UPPER(m.professor.user.basicInfo.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfo.firstname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.firstname) like :filterText ");
            searchQueryString.append(" or (" +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		join pd.department.name dname " +
                    "		join pd.department.school.name sname " +
                    "		join pd.department.school.institution.name iname " +
                    "		where pd.id = p.id " +
                    "		and ( dname like :filterText " +
                    "			or sname like :filterText " +
                    "			or iname like :filterText " +
                    "		)" +
                    "	) " +
                    "	or exists (" +
                    "		select pf.id from ProfessorForeign pf " +
                    "		where pf.id = p.id " +
                    "		and UPPER(pf.foreignInstitution) like :filterText " +
                    "	) " +
                    ") ) ");
        }

        Query countQuery = em.createQuery(
                "select count(distinct m.id) " +
                        searchQueryString.toString());

        countQuery.setParameter("institutionId", institution.getId());
        countQuery.setParameter("status", Role.RoleStatus.ACTIVE);
        countQuery.setParameter("committeeMemberProfessorIds", committeeMemberProfessorIds);

        if (StringUtils.isNotEmpty(filterText)) {
            countQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        //get Result
        Long totalRecords = (Long) countQuery.getSingleResult();

        StringBuilder orderByClause = new StringBuilder();

        if (StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("register")) {
                orderByClause.append(" order by m.register.subject.name " + orderDirection);
            } else if (orderField.equals("lastname")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by m.professor.user.basicInfo.lastname " + orderDirection);
                } else {
                    orderByClause.append(" order by m.professor.user.basicInfoLatin.lastname " + orderDirection);
                }
            } else if (orderField.equals("firstname")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by m.professor.user.basicInfo.firstname " + orderDirection);
                } else {
                    orderByClause.append(" order by m.professor.user.basicInfoLatin.firstname " + orderDirection);
                }
            } else if (orderField.equals("discriminator")) {
                orderByClause.append(" order by m.professor.discriminator " + orderDirection);
            }
        }

        TypedQuery<RegisterMember> searchQuery = em.createQuery(
                " select m from RegisterMember m " +
                        "where m.id in ( " +
                        "select distinct m.id " +
                        searchQueryString.toString() + ") " + orderByClause.toString(), RegisterMember.class);

        searchQuery.setParameter("institutionId", institution.getId());
        searchQuery.setParameter("status", Role.RoleStatus.ACTIVE);
        searchQuery.setParameter("committeeMemberProfessorIds", committeeMemberProfessorIds);
        if (StringUtils.isNotEmpty(filterText)) {
            searchQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        List<RegisterMember> registerMembers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        SearchData<RegisterMember> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(registerMembers);

        return result;

    }

    public Collection<CandidateFile> getSnapshotFiles(Long candidacyId, User loggedOn) throws NotEnabledException, NotFoundException {
        try {

            // get candidacy
            Candidacy candidacy = getCandidacy(candidacyId, true);

            if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                    !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                    !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                    !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                    (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                    (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                    !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                    !candidacy.containsEvaluator(loggedOn)) {
                throw new NotEnabledException("insufficient.privileges");
            }
            if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                    !candidacy.isOpenToOtherCandidates() &&
                    !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
                throw new NotEnabledException("insufficient.privileges");
            }
            // Return Result
            return candidacy.getSnapshotFiles();
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    public CandidateFile getSnapshotFile(Long candidacyId, Long fileId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get candidacy with the snapshot files
        Candidacy candidacy = getCandidacy(candidacyId, true);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        for (CandidateFile file : candidacy.getSnapshotFiles()) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        throw new NotFoundException("wrong.file.id");
    }

    public FileBody getSnapshotFileBody(Long candidacyId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, true);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        for (CandidateFile file : candidacy.getSnapshotFiles()) {
            if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
                return file.getCurrentBody();
            }
        }
        throw new NotFoundException("wrong.file.id");

    }

    public Collection<CandidacyFile> getFiles(Long candidacyId, User loggedOn) throws NotEnabledException, NotFoundException {

        Candidacy candidacy = getCandidacy(candidacyId, false);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        return FileHeader.filterDeleted(candidacy.getFiles());
    }

    public CandidacyFile getFile(Long candidacyId, Long fileId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        for (CandidacyFile file : candidacy.getFiles()) {
            if (file.getId().equals(fileId) && !file.isDeleted()) {
                return file;
            }
        }
        throw new NotFoundException("wrong.file.id");
    }

    public FileBody getFileBody(Long candidacyId, Long fileId, Long bodyId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.containsEvaluator(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        for (CandidacyFile file : candidacy.getFiles()) {
            if (file.getId().equals(fileId) && !file.isDeleted()) {
                for (FileBody fb : file.getBodies()) {
                    if (fb.getId().equals(bodyId)) {
                        return fb;
                    }
                }
            }
        }
        throw new NotFoundException("wrong.file.id");
    }

    public CandidacyFile createFile(Long candidacyId, HttpServletRequest request, User loggedOn) throws Exception {

        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        // Validate:
        if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Parse Request
        List<FileItem> fileItems = readMultipartFormData(request);
        // Find required type:
        FileType type = null;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
                break;
            }
        }
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }
        // Other files until closing date
        if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
                !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI) &&
                DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
            throw new ValidationException("wrong.position.status");
        }
        //SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
        if (hasNominationCommitteeConverged(candidacy)) {
            throw new ValidationException("wrong.position.status.committee.converged");
        }
        // Check number of file types
        CandidacyFile existingFile = checkNumberOfFileTypes(CandidacyFile.fileTypes, type, candidacy.getFiles());
        if (existingFile != null) {
            return _updateFile(loggedOn, fileItems, existingFile);
        }
        // Create
        CandidacyFile candidacyFile = new CandidacyFile();
        candidacyFile.setCandidacy(candidacy);
        candidacyFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, candidacyFile);
        candidacy.addFile(candidacyFile);
        em.flush();

        return candidacyFile;
    }

    public CandidacyFile updateFile(Long candidacyId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        // Validate:
        if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Parse Request
        List<FileItem> fileItems = readMultipartFormData(request);
        // Find required type:
        FileType type = null;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
                break;
            }
        }
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }

        // Other files until closing date
        if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
                !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI) &&
                DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
            //throw new RestException(Status.CONFLICT, "wrong.position.status");
            throw new ValidationException("wrong.position.status");
        }
        //SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
        if (hasNominationCommitteeConverged(candidacy)) {
            throw new ValidationException("wrong.position.status.committee.converged");
        }
        if (!CandidacyFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }
        CandidacyFile candidacyFile = null;
        for (CandidacyFile file : candidacy.getFiles()) {
            if (file.getId().equals(fileId)) {
                candidacyFile = file;
                break;
            }
        }
        if (candidacyFile == null) {
            throw new NotFoundException("wrong.file.id");
        }

        // Update
        return _updateFile(loggedOn, fileItems, candidacyFile);
    }

    public void deleteFile(Long candidacyId, Long fileId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        CandidacyFile candidacyFile = null;
        for (CandidacyFile file : candidacy.getFiles()) {
            if (file.getId().equals(fileId) && !file.isDeleted()) {
                candidacyFile = file;
                break;
            }
        }
        if (candidacyFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        FileType type = candidacyFile.getType();

        // Other files until closing date
        if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
                !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI) &&
                DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
            throw new ValidationException("wrong.position.status");
        }
        //SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
        if (hasNominationCommitteeConverged(candidacy)) {
            throw new ValidationException("wrong.position.status.committee.converged");
        }
        CandidacyFile cf = deleteAsMuchAsPossible(candidacyFile);
        if (cf == null) {
            candidacy.getFiles().remove(candidacyFile);
        }
    }

    public Collection<PositionCandidaciesFile> getEvaluationFiles(Long candidacyId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);

        PositionCandidacies existingCandidacies = candidacy.getCandidacies();
        if (existingCandidacies == null) {
            throw new NotFoundException("wrong.position.candidacies.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // Search
        List<PositionCandidaciesFile> result = positionCandidaciesService.getPositionCandidaciesFiles(existingCandidacies.getId(), candidacy.getId());

        // Return Result
        return result;
    }

    public PositionCandidaciesFile getEvaluationFile(Long candidacyId, Long fileId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);
        // Validate:
        PositionCandidacies existingCandidacies = candidacy.getCandidacies();
        if (existingCandidacies == null) {
            throw new NotFoundException("wrong.position.candidacies.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Find File
        Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
        for (PositionCandidaciesFile file : files) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        throw new NotFoundException("wrong.file.id");
    }

    public FileBody getEvaluationFileBody(Long candidacyId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
        // get candidacy
        Candidacy candidacy = getCandidacy(candidacyId, false);
        // Validate:
        PositionCandidacies existingCandidacies = candidacy.getCandidacies();
        if (existingCandidacies == null) {
            throw new NotFoundException("wrong.position.candidacies.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
                (candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
                (candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !candidacy.getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
                !candidacy.isOpenToOtherCandidates() &&
                !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
        for (PositionCandidaciesFile file : files) {
            if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
                return file.getCurrentBody();
            }
        }
        throw new NotFoundException("wrong.file.id");
    }

    private boolean canAddEvaluators(Candidacy c) {
        PositionCommittee committee = c.getCandidacies().getPosition().getPhase().getCommittee();
        return committee != null &&
                committee.getMembers().size() > 0 &&
                DateUtil.compareDates(new Date(), committee.getCandidacyEvalutionsDueDate()) <= 0;
    }

    private boolean hasNominationCommitteeConverged(Candidacy c) {
        PositionNomination nomination = c.getCandidacies().getPosition().getPhase().getNomination();
        return nomination != null &&
                nomination.getNominationCommitteeConvergenceDate() != null &&
                DateUtil.compareDates(new Date(), nomination.getNominationCommitteeConvergenceDate()) >= 0;
    }


    private void validateCandidacyToUpdate(Position position, Candidacy existingCandidacy, Candidacy candidacy) throws ValidationException {

        if (!position.getPhase().getStatus().equals(Position.PositionStatus.ANOIXTI) &&
                !position.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            //throw new RestException(Response.Status.CONFLICT, "wrong.position.status");
            throw new ValidationException("wrong.position.status");
        }
        // Only one field is not allowed to change after closing date: isOpenToOtherCandidates
        if ((existingCandidacy.isOpenToOtherCandidates() ^ candidacy.isOpenToOtherCandidates()) &&
                !existingCandidacy.getCandidacies().getPosition().getPhase().getClientStatus().equals(Position.PositionStatus.ANOIXTI.toString())) {
            //throw new RestException(Response.Status.CONFLICT, "wrong.position.candidacies.closingDate");
            throw new ValidationException("wrong.position.candidacies.closingDate");
        }
        Set<Long> newRegisterMemberIds = new HashSet<Long>();
        for (CandidacyEvaluator newEvaluator : candidacy.getProposedEvaluators()) {
            if (newEvaluator.getRegisterMember() != null && newEvaluator.getRegisterMember().getId() != null) {
                if (newRegisterMemberIds.contains(newEvaluator.getRegisterMember().getId())) {
                    //throw new RestException(Response.Status.CONFLICT, "duplicate.evaluators");
                    throw new ValidationException("duplicate.evaluators");
                }
                newRegisterMemberIds.add(newEvaluator.getRegisterMember().getId());
            }
        }
        if (newRegisterMemberIds.size() > 0 && !canAddEvaluators(existingCandidacy)) {
            //throw new RestException(Response.Status.CONFLICT, "committee.not.defined");
            throw new ValidationException("committee.not.defined");
        }
        if (candidacy.getProposedEvaluators().size() > CandidacyEvaluator.MAX_MEMBERS) {
            //throw new RestException(Response.Status.CONFLICT, "max.evaluators.exceeded");
            throw new ValidationException("max.evaluators.exceeded");
        }

        // Check files and candidate status
        validateCandidacy(existingCandidacy, existingCandidacy.getCandidate(), !existingCandidacy.isPermanent());
    }


    private void sendEmailsPriorToUpdate(boolean isNew, final Candidacy existingCandidacy, Collection<CandidacyEvaluator> addedEvaluators) {

        if (isNew) {
            // Send E-Mails
            // 1. candidacy.create@institutionManager
            for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
                mailService.postEmail(recipient.get("email"),
                        "default.subject",
                        "candidacy.create@institutionManager",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                putAll(recipient);

                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

                                put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");
                            }
                        }));
            }
            // 2. candidacy.create@candidate
            mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "candidacy.create@candidate",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                            put("position", existingCandidacy.getCandidacies().getPosition().getName());

                            put("firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                            put("lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));
                            put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                            put("firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                            put("lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));
                            put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                            put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");


                        }
                    }));

        }
        if (!addedEvaluators.isEmpty()) {
            // 3. candidacy.create.candidacyEvaluator@candidacyEvaluator
            for (final CandidacyEvaluator evaluator : addedEvaluators) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "candidacy.create.candidacyEvaluator@candidacyEvaluator",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));
                                put("candidate_firstname_el", existingCandidacy.getSnapshot().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getSnapshot().getLastname("el"));
                                put("assistants_el", extractAssistantsInfo(existingCandidacy.getCandidacies().getPosition(), "el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
                                put("candidate_firstname_en", existingCandidacy.getSnapshot().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getSnapshot().getLastname("en"));
                                put("assistants_en", extractAssistantsInfo(existingCandidacy.getCandidacies().getPosition(), "en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");

                            }

                            private String extractAssistantsInfo(Position position, String locale) {
                                StringBuilder info = new StringBuilder();
                                info.append("<ul>");

                                InstitutionManager manager = position.getManager();
                                String im_firstname = manager.getUser().getFirstname(locale);
                                String im_lastname = manager.getUser().getLastname(locale);
                                String im_email = manager.getUser().getContactInfo().getEmail();
                                String im_phone = manager.getUser().getContactInfo().getPhone();
                                info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");

                                im_firstname = manager.getAlternateFirstname(locale);
                                im_lastname = manager.getAlternateLastname(locale);
                                im_email = manager.getAlternateContactInfo().getEmail();
                                im_phone = manager.getAlternateContactInfo().getPhone();
                                info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");

                                info.append("<li><ul>");
                                Set<User> assistants = new HashSet<User>();
                                assistants.addAll(position.getAssistants());
                                if (position.getCreatedBy().getPrimaryRole().equals(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
                                    assistants.add(position.getCreatedBy());
                                }
                                for (User u : assistants) {
                                    if (u.getId().equals(manager.getUser().getId())) {
                                        continue;
                                    }
                                    im_firstname = u.getFirstname(locale);
                                    im_lastname = u.getLastname(locale);
                                    im_email = u.getContactInfo().getEmail();
                                    im_phone = u.getContactInfo().getPhone();
                                    info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");
                                }
                                info.append("</ul></li>");

                                info.append("</ul>");
                                return info.toString();
                            }

                        }));
            }
            // 4. candidacy.create.candidacyEvaluator@institutionManager
            for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
                mailService.postEmail(recipient.get("email"),
                        "default.subject",
                        "candidacy.create.candidacyEvaluator@institutionManager",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                putAll(recipient); //firstname, lastname (el|en)

                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));
                                put("candidate_firstname_el", existingCandidacy.getSnapshot().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getSnapshot().getLastname("el"));

                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
                                put("candidate_firstname_en", existingCandidacy.getSnapshot().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getSnapshot().getLastname("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");

                                Iterator<CandidacyEvaluator> it = existingCandidacy.getProposedEvaluators().iterator();
                                if (it.hasNext()) {
                                    CandidacyEvaluator eval = it.next();
                                    put("evaluator1_firstname_el", eval.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                    put("evaluator1_lastname_el", eval.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                    put("evaluator1_firstname_en", eval.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                    put("evaluator1_lastname_en", eval.getRegisterMember().getProfessor().getUser().getLastname("en"));

                                    put("evaluator1_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());

                                } else {
                                    put("evaluator1_firstname_el", "-");
                                    put("evaluator1_lastname_el", "-");
                                    put("evaluator1_firstname_en", "-");
                                    put("evaluator1_lastname_en", "-");

                                    put("evaluator1_email", "-");

                                    put("evaluator2_firstname_el", "-");
                                    put("evaluator2_lastname_el", "-");
                                    put("evaluator2_firstname_en", "-");
                                    put("evaluator2_lastname_en", "-");

                                    put("evaluator2_email", "-");

                                }
                                if (it.hasNext()) {
                                    CandidacyEvaluator eval = it.next();
                                    put("evaluator2_firstname_el", eval.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                    put("evaluator2_lastname_el", eval.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                    put("evaluator2_firstname_en", eval.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                    put("evaluator2_lastname_en", eval.getRegisterMember().getProfessor().getUser().getLastname("en"));

                                    put("evaluator2_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());
                                } else {
                                    put("evaluator2_firstname_el", "-");
                                    put("evaluator2_lastname_el", "-");
                                    put("evaluator2_firstname_en", "-");
                                    put("evaluator2_lastname_en", "-");

                                    put("evaluator2_email", "-");
                                }
                            }
                        }));
                // END: Send E-Mails
            }
        }
    }

    private void sendEmailsPriorToDelete(final Candidacy existingCandidacy) {

        // 1. candidacy.remove@institutionManager
        for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
            mailService.postEmail(recipient.get("email"),
                    "default.subject",
                    "candidacy.remove@institutionManager",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            putAll(recipient); //firstname, lastname (el|en)

                            put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                            put("position", existingCandidacy.getCandidacies().getPosition().getName());

                            put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                            put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

                            put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                            put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

                            put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                            put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                            put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");
                        }
                    }));
        }
        // 2. candidacy.remove@candidate
        mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
                "default.subject",
                "candidacy.remove@candidate",
                Collections.unmodifiableMap(new HashMap<String, String>() {

                    {
                        put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                        put("position", existingCandidacy.getCandidacies().getPosition().getName());

                        put("firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                        put("lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));
                        put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                        put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                        put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                        put("firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                        put("lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));
                        put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                        put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                        put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                    }
                }));
        // 3. candidacy.remove@committee
        if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
            for (final PositionCommitteeMember member : existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "candidacy.remove@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

                                put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
        }
        // 4. candidacy.remove@evaluators
        if (existingCandidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null) {
            for (final PositionEvaluator evaluator : existingCandidacy.getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "candidacy.remove@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

                                put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        }
        // 5. candidacy.remove@candidates
        for (final Candidacy candidacy : existingCandidacy.getCandidacies().getCandidacies()) {
            if (candidacy.isPermanent() && !candidacy.getId().equals(existingCandidacy.getId())) {
                mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "candidacy.remove@candidates",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
                                put("position", existingCandidacy.getCandidacies().getPosition().getName());

                                put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

                                put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

                                put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");

                            }
                        }));
            }
        }
        // End: Send E-Mails
    }


}
