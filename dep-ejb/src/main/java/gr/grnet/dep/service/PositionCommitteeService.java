package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
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
public class PositionCommitteeService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private MailService mailService;

    private PositionCommittee getById(Long id) throws NotFoundException {
        PositionCommittee positionCommittee = em.find(PositionCommittee.class, id);

        if (positionCommittee == null) {
            throw new NotFoundException("wrong.position.committee.id");
        }

        return positionCommittee;
    }

    public PositionCommittee get(Long positionId, Long committeeId, User loggedOn) throws NotFoundException, NotEnabledException {
        PositionCommittee existingCommittee;
        try {
            existingCommittee = em.createQuery(
                    "select pcom from PositionCommittee pcom " +
                            "left join fetch pcom.members pmem " +
                            "where pcom.id = :committeeId", PositionCommittee.class)
                    .setParameter("committeeId", committeeId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.position.committee.id");
        }
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingCommittee.containsMember(loggedOn) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        return existingCommittee;
    }

    public Collection<Register> getPositionCommiteeRegisters(Long positionId, Long committeeId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)
                && !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Prepare Query
        List<Register> registers = em.createQuery(
                "select r from Register r " +
                        "where r.permanent = true " +
                        "and r.institution.id = :institutionId ", Register.class)
                .setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
                .getResultList();

        return registers;
    }

    public SearchData<RegisterMember> getPositionCommiteeRegisterMembers(Long positionId, Long committeeId, Long registerId, HttpServletRequest request, User loggedOn) throws Exception {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)
                && !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
            throw new NotEnabledException("insufficient.privileges");
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


        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append("from RegisterMember m " +
                "join m.professor prof " +
                "join prof.user u " +
                "join u.roles r " +
                "where m.register.permanent = true " +
                "and m.deleted = false " +
                "and m.register.institution.id = :institutionId " +
                "and m.register.id = :registerId " +
                "and m.professor.status = :status ");

        if (StringUtils.isNotEmpty(filterText)) {
            searchQueryString.append(" and ( UPPER(m.professor.user.basicInfo.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfo.firstname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.firstname) like :filterText ");
            // treat the professor user is as text in order to filter it
            searchQueryString.append(" or CAST(m.professor.user.id AS text) like :filterText ");
            searchQueryString.append(" or (" +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		join pd.department.name dname " +
                    "		join pd.department.school.name sname " +
                    "		join pd.department.school.institution.name iname " +
                    "		where pd.id = prof.id " +
                    "		and ( dname like  :filterText " +
                    "			or sname like :filterText " +
                    "			or iname like :filterText " +
                    "		)" +
                    "	) " +
                    "	or exists (" +
                    "		select pf.id from ProfessorForeign pf " +
                    "		where pf.id = prof.id " +
                    "		and UPPER(pf.foreignInstitution) like :filterText " +
                    "	) " +
                    ") ) ");
        }

        Query countQuery = em.createQuery(" select count(distinct m.id) " +
                searchQueryString.toString())
                .setParameter("registerId", registerId)
                .setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
                .setParameter("status", Role.RoleStatus.ACTIVE);

        if (StringUtils.isNotEmpty(filterText)) {
            countQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        // find the total records
        Long totalRecords = (Long) countQuery.getSingleResult();

        StringBuilder orderByClause = new StringBuilder();

        if (StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("id")) {
                orderByClause.append(" order by m.professor.user.id " + orderDirection);
            } else if (orderField.equals("firstname")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by m.professor.user.basicInfo.firstname " + orderDirection);
                } else {
                    orderByClause.append(" order by m.professor.user.basicInfoLatin.firstname " + orderDirection);
                }
            } else if (orderField.equals("lastname")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by m.professor.user.basicInfo.lastname " + orderDirection);
                } else {
                    orderByClause.append(" order by m.professor.user.basicInfoLatin.lastname " + orderDirection);
                }
            }
        }

        TypedQuery<RegisterMember> searchQuery = em.createQuery(
                " select m from RegisterMember m " +
                        "where m.id in ( " +
                        "select distinct m.id " +
                        searchQueryString.toString() + ") " + orderByClause.toString(), RegisterMember.class);

        searchQuery.setParameter("registerId", registerId);
        searchQuery.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId());
        searchQuery.setParameter("status", Role.RoleStatus.ACTIVE);

        if (StringUtils.isNotEmpty(filterText)) {
            searchQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        // Execute
        List<RegisterMember> registerMembers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        // Additional actions
        addCommitteesCount(registerMembers);
        addEvaluationsCount(registerMembers);

        SearchData<RegisterMember> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(registerMembers);

        return result;
    }

    private void addCommitteesCount(List<RegisterMember> registerMembers) {
        Map<Long, Long> countMap = new HashMap<Long, Long>();
        for (RegisterMember rm : registerMembers) {
            countMap.put(rm.getProfessor().getId(), 0L);
        }
        if (countMap.isEmpty()) {
            return;
        }

        List<Object[]> result = em.createQuery(
                "select prof.id, count(pcm.id) " +
                        "from PositionCommitteeMember pcm " +
                        "join pcm.registerMember.professor prof " +
                        "join pcm.committee pcom " +
                        "join pcom.position pos " +
                        "join pos.phase ph " +
                        "where ph.status =:epilogi and prof.id in (:professorIds)" +
                        "group by prof.id ",
                Object[].class)
                .setParameter("epilogi", Position.PositionStatus.EPILOGI)
                .setParameter("professorIds", countMap.keySet())
                .getResultList();

        for (Object[] count : result) {
            Long professorId = (Long) count[0];
            Long counter = (Long) count[1];
            countMap.put(professorId, counter);
        }
        for (RegisterMember rm : registerMembers) {
            rm.getProfessor().setCommitteesCount(countMap.get(rm.getProfessor().getId()));
        }
    }

    private void addEvaluationsCount(List<RegisterMember> registerMembers) {
        Map<Long, Long> countMap = new HashMap<Long, Long>();
        for (RegisterMember rm : registerMembers) {
            countMap.put(rm.getProfessor().getId(), 0L);
        }
        if (countMap.isEmpty()) {
            return;
        }

        List<Object[]> result = em.createQuery(
                "select prof.id, count(pe.id) " +
                        "from PositionEvaluator pe " +
                        "join pe.registerMember.professor prof " +
                        "join pe.evaluation peval " +
                        "join peval.position pos " +
                        "join pos.phase ph " +
                        "where ph.status =:epilogi and prof.id in (:professorIds)" +
                        "group by prof.id ",
                Object[].class)
                .setParameter("epilogi", Position.PositionStatus.EPILOGI)
                .setParameter("professorIds", countMap.keySet())
                .getResultList();

        for (Object[] count : result) {
            Long professorId = (Long) count[0];
            Long counter = (Long) count[1];
            countMap.put(professorId, counter);
        }
        for (RegisterMember rm : registerMembers) {
            rm.getProfessor().setEvaluationsCount(countMap.get(rm.getProfessor().getId()));
        }
    }

    public PositionCommitteeMember getPositionCommitteeMember(Long positionId, Long committeeId, Long cmId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        for (PositionCommitteeMember existingMember : existingCommittee.getMembers()) {
            if (existingMember.getId().equals(cmId)) {
                existingMember.getRegisterMember().getProfessor().getUser().getRoles();
                return existingMember;
            }
        }
        throw new NotFoundException("wrong.position.commitee.member.id");
    }

    public PositionCommittee update(Long positionId, Long committeeId, PositionCommittee newCommittee, User loggedOn) throws NotFoundException, NotEnabledException, ValidationException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI) || existingPosition.getPhase().getCandidacies() == null) {
            throw new ValidationException("wrong.position.status");
        }
        // Retrieve new Members from Institution's Register
        Map<Long, PositionCommitteeMember> existingCommitteeMemberAsMap = new HashMap<Long, PositionCommitteeMember>();
        for (PositionCommitteeMember existingCommitteeMember : existingCommittee.getMembers()) {
            existingCommitteeMemberAsMap.put(existingCommitteeMember.getRegisterMember().getId(), existingCommitteeMember);
        }

        // Retrieve new Members from Institution's Register
        Map<Long, PositionCommitteeMember.MemberType> newCommitteeMemberAsMap = new HashMap<Long, PositionCommitteeMember.MemberType>();
        for (PositionCommitteeMember newCommitteeMember : newCommittee.getMembers()) {
            newCommitteeMemberAsMap.put(newCommitteeMember.getRegisterMember().getId(), newCommitteeMember.getType());
        }
        List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();

        if (!newCommitteeMemberAsMap.isEmpty()) {
            TypedQuery<RegisterMember> query = em.createQuery(
                    "select distinct m from Register r " +
                            "join r.members m " +
                            "where r.permanent = true " +
                            "and r.institution.id = :institutionId " +
                            "and m.deleted = false " +
                            "and m.professor.status = :status " +
                            "and m.id in (:registerIds)", RegisterMember.class)
                    .setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
                    .setParameter("status", Role.RoleStatus.ACTIVE)
                    .setParameter("registerIds", newCommitteeMemberAsMap.keySet());
            newRegisterMembers.addAll(query.getResultList());
        }
        if (newCommittee.getMembers().size() != newRegisterMembers.size()) {
            throw new NotFoundException("wrong.register.member.id");
        }
        // Validate New Committee
        if (newCommittee.getMembers().size() > 0 &&
                newCommittee.getCandidacyEvalutionsDueDate() == null) {
            throw new ValidationException("committee.missing.candidacyEvalutions.dueDate");
        }

        // Να μην είναι δυνατή η συμπλήρωση της Επιτροπής εάν δεν έχει συμπληρωθεί το πεδίο "Απόφαση Σύστασης Επιτροπής".
        if (newCommittee.getMembers().size() > 0 &&
                FileHeader.filter(existingCommittee.getFiles(), FileType.APOFASI_SYSTASIS_EPITROPIS).isEmpty()) {
            throw new ValidationException("committee.missing.apofasi.systasis.epitropis");
        }

        // Check committee structure
        int countRegular = 0;
        int countSubstitute = 0;
        int countInternalRegular = 0;
        int countInternalSubstitute = 0;
        int countExternalRegular = 0;
        int countExternalRegularForeign = 0;
        int countExternalSubstitute = 0;
        int countExternalSubstituteForeign = 0;
        for (RegisterMember newRegisterMember : newRegisterMembers) {
            PositionCommitteeMember.MemberType type = newCommitteeMemberAsMap.get(newRegisterMember.getId());
            switch (type) {
                case REGULAR:
                    countRegular++;
                    if (newRegisterMember.isExternal()) {
                        countExternalRegular++;
                        if (newRegisterMember.getProfessor().getUser().hasActiveRole(Role.RoleDiscriminator.PROFESSOR_FOREIGN)) {
                            countExternalRegularForeign++;
                        }
                    } else {
                        countInternalRegular++;
                    }
                    break;
                case SUBSTITUTE:
                    countSubstitute++;
                    if (newRegisterMember.isExternal()) {
                        countExternalSubstitute++;
                        if (newRegisterMember.getProfessor().getUser().hasActiveRole(Role.RoleDiscriminator.PROFESSOR_FOREIGN)) {
                            countExternalSubstituteForeign++;
                        }
                    } else {
                        countInternalSubstitute++;
                    }
                    break;
            }
        }
        if (countRegular != PositionCommitteeMember.MAX_MEMBERS) {
            throw new ValidationException("max.regular.members.failed");
        }
        if (countSubstitute != PositionCommitteeMember.MAX_MEMBERS) {
            throw new ValidationException("max.substitute.members.failed");
        }
        if (countExternalRegular < PositionCommitteeMember.MIN_EXTERNAL) {
            throw new ValidationException("min.external.regular.members.failed");
        }
        if (countExternalRegularForeign == 0) {
            throw new ValidationException("min.external.regular.foreign.members.failed");
        }
        if (countExternalSubstitute < PositionCommitteeMember.MIN_EXTERNAL) {
            throw new ValidationException("min.external.substitute.members.failed");
        }
        if (countExternalSubstituteForeign == 0) {
            throw new ValidationException("min.external.substitute.foreign.members.failed");
        }

        // Keep these to send mails
        Set<Long> addedMemberIds = new HashSet<Long>();
        Set<Long> removedMemberIds = new HashSet<Long>();
        removedMemberIds.addAll(existingCommitteeMemberAsMap.keySet());
        boolean committeeMeetingDateUpdated = (newCommittee.getCommitteeMeetingDate() != null) &&
                (DateUtil.compareDates(existingCommittee.getCommitteeMeetingDate(), newCommittee.getCommitteeMeetingDate()) != 0);

        // Update

        existingCommittee.copyFrom(newCommittee);

        existingCommittee.getMembers().clear();
        for (RegisterMember newRegisterMember : newRegisterMembers) {
            PositionCommitteeMember newCommitteeMember = null;
            if (existingCommitteeMemberAsMap.containsKey(newRegisterMember.getId())) {
                newCommitteeMember = existingCommitteeMemberAsMap.get(newRegisterMember.getId());
            } else {
                newCommitteeMember = new PositionCommitteeMember();
                newCommitteeMember.setRegisterMember(newRegisterMember);
                addedMemberIds.add(newRegisterMember.getId());
            }
            newCommitteeMember.setType(newCommitteeMemberAsMap.get(newRegisterMember.getId()));
            existingCommittee.addMember(newCommitteeMember);

            removedMemberIds.remove(newRegisterMember.getId());
        }

        final PositionCommittee savedCommittee = em.merge(existingCommittee);
        em.flush();

        // Send E-Mails
        for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
            if (addedMemberIds.contains(member.getRegisterMember().getId())) {
                if (member.getType().equals(PositionCommitteeMember.MemberType.REGULAR)) {
                    // positionCommittee.create.regular.member@member
                    mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.create.regular.member@member",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                    put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                    put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                                }
                            }));
                } else {
                    // positionCommittee.create.substitute.member@member
                    mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.create.substitute.member@member",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                    put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                    put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                                }
                            }));
                }
            }
        }
        for (Long registerMemberID : removedMemberIds) {
            final PositionCommitteeMember removedMember = existingCommitteeMemberAsMap.get(registerMemberID);
            if (removedMember.getType().equals(PositionCommitteeMember.MemberType.REGULAR)) {
                // positionCommittee.remove.regular.member@member
                mailService.postEmail(removedMember.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCommittee.remove.regular.member@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                put("position", savedCommittee.getPosition().getName());

                                put("firstname_el", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", removedMember.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", removedMember.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
                            }
                        }));
            } else {
                // positionCommittee.remove.substitute.member@member
                mailService.postEmail(removedMember.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCommittee.remove.substitute.member@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                put("position", savedCommittee.getPosition().getName());

                                put("firstname_el", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", removedMember.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", removedMember.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
                            }
                        }));
            }
        }
        if (!addedMemberIds.isEmpty() && removedMemberIds.isEmpty()) {
            // positionCommittee.create.members@candidates
            for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.create.members@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
        }
        if (!removedMemberIds.isEmpty()) {
            //positionCommittee.remove.members@candidates
            for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.remove.members@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
            //positionCommittee.remove.members@evaluators
            if (savedCommittee.getPosition().getPhase().getEvaluation() != null) {
                for (final PositionEvaluator member : savedCommittee.getPosition().getPhase().getEvaluation().getEvaluators()) {
                    //positionCommittee.remove.members@members
                    mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.remove.members@evaluators",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                    put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                    put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                                }
                            }));
                }
            }
            //positionCommittee.remove.members@members
            for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCommittee.remove.members@members",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                put("position", savedCommittee.getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }

        }
        if (committeeMeetingDateUpdated) {
            // positionCommittee.update.committeeMeetingDate@members
            for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCommittee.update.committeeMeetingDate@members",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                put("position", savedCommittee.getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionCommittee.update.committeeMeetingDate@candidates
            for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionCommittee.update.committeeMeetingDate@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
                                    put("position", savedCommittee.getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
        }

        // End: Send E-Mails

        // Return result
        return savedCommittee;
    }

    public Collection<PositionCommitteeFile> getFiles(Long positionId, Long committeeId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingCommittee.containsMember(loggedOn) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // Return Result
        return FileHeader.filterDeleted(existingCommittee.getFiles());
    }

    public PositionCommitteeFile getFile(Long positionId, Long committeeId, Long fileId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingCommittee.containsMember(loggedOn) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // Return Result
        Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
        for (PositionCommitteeFile file : files) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }

        return null;
    }

    public FileBody getFileBody(Long positionId, Long committeeId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingCommittee.containsMember(loggedOn) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
        for (FileHeader file : files) {
            if (file.getId().equals(fileId)) {
                if (file.getId().equals(fileId)) {
                    for (FileBody fb : file.getBodies()) {
                        if (fb.getId().equals(bodyId)) {
                            return fb;
                        }
                    }
                }
            }
        }
        return null;
    }

    public FileHeader createFile(Long positionId, Long committeeId, HttpServletRequest request, User loggedOn) throws Exception {
        // get committee
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
            throw new ValidationException("wrong.position.committee.phase");
        }
        // Validate:
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
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
        // 2) Να μην είναι δυνατή η συμπλήρωση των πεδίων
        // "Πρακτικό Συνεδρίασης Επιτροπής για Αξιολογητές" και "Αίτημα Επιτροπής για Αξιολογητές" εάν δεν έχει συμπληρωθεί το πεδίο "Ημερομηνία Συνεδρίασης Επιτροπής".
        if ((type.equals(FileType.PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES) || type.equals(FileType.AITIMA_EPITROPIS_PROS_AKSIOLOGITES))
                && existingCommittee.getCommitteeMeetingDate() == null) {
            throw new ValidationException("committee.missing.committee.meeting.day");
        }

        // Check number of file types
        Set<PositionCommitteeFile> pcFiles = FileHeader.filterIncludingDeleted(existingCommittee.getFiles(), type);
        PositionCommitteeFile existingFile = checkNumberOfFileTypes(PositionCommitteeFile.fileTypes, type, pcFiles);
        if (existingFile != null) {
            return _updateFile(loggedOn, fileItems, existingFile);
        }
        // Create
        final PositionCommitteeFile pcFile = new PositionCommitteeFile();
        pcFile.setCommittee(existingCommittee);
        pcFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, pcFile);
        existingCommittee.addFile(pcFile);
        em.flush();

        // Send E-Mails
        // position.upload@committee
        for (final PositionCommitteeMember member : pcFile.getCommittee().getPosition().getPhase().getCommittee().getMembers()) {
            mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "position.upload@committee",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
                            put("position", pcFile.getCommittee().getPosition().getName());

                            put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                            put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                            put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

                            put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                            put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                            put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));

                            put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                        }
                    }));
        }
        // position.upload@candidates
        for (final Candidacy candidacy : pcFile.getCommittee().getPosition().getPhase().getCandidacies().getCandidacies()) {
            if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@candidates",
                        Collections.unmodifiableMap(new HashMap<String, String>() {
                            {
                                put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
                                put("position", pcFile.getCommittee().getPosition().getName());

                                put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                            }
                        }));
            }
        }
        // position.upload@evaluators
        for (final PositionEvaluator evaluator : pcFile.getCommittee().getPosition().getPhase().getEvaluation().getEvaluators()) {
            mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "position.upload@evaluators",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
                            put("position", pcFile.getCommittee().getPosition().getName());

                            put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                            put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                            put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

                            put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                            put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                            put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));

                            put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                        }
                    }));
        }

        return pcFile;
    }

    public FileHeader updateFile(Long positionId, Long committeeId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
            throw new ValidationException("wrong.position.committee.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
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
        // Extra Validation
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }

        if (!PositionCommitteeFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }
        PositionCommitteeFile committeeFile = null;
        for (PositionCommitteeFile file : existingCommittee.getFiles()) {
            if (file.getId().equals(fileId)) {
                committeeFile = file;
                break;
            }
        }
        if (committeeFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Update
        return _updateFile(loggedOn, fileItems, committeeFile);
    }

    public void deleteFile(Long committeeId, Long positionId, Long fileId, User loggedOn) throws Exception {
        PositionCommittee existingCommittee = getById(committeeId);
        // get position
        Position existingPosition = existingCommittee.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
            throw new ValidationException("wrong.position.committee.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }

        PositionCommitteeFile committeeFile = null;
        for (PositionCommitteeFile file : existingCommittee.getFiles()) {
            if (file.getId().equals(fileId)) {
                committeeFile = file;
                break;
            }
        }
        if (committeeFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        PositionCommitteeFile cf = deleteAsMuchAsPossible(committeeFile);
        if (cf == null) {
            existingCommittee.getFiles().remove(committeeFile);
        }
    }
}
