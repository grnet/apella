package gr.grnet.dep.service;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.SearchData;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.CompareUtil;
import gr.grnet.dep.service.util.StringUtil;

@Stateless
public class RegisterService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    private EntityManager em;

    @EJB
    private UtilityService utilityService;

    @EJB
    private MailService mailService;

    @EJB
    private ReportService reportService;

    public Register getById(Long id) throws NotFoundException {
        Register register = em.find(Register.class, id);

        if (register == null) {
            throw new NotFoundException("wrong.register.id");
        }

        return register;
    }

    public Register get(Long id) throws NotFoundException {
        try {
            // A Hibernate specific solution, a JPA filtering mechanism does not exist
            Session session = em.unwrap(Session.class);
            session.enableFilter("filterDeleted");
            // do not fetch the lazy collections - not same to getRegisterById
            Register r = em.createQuery(
                    "select r from Register r " +
                            "where r.id=:id", Register.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return r;
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.register.id");
        }

    }

    public SearchData<Register> getAll(HttpServletRequest request, User loggedOn) {

        String institutionFilterText = request.getParameter("sSearch_0");
        String subjectFilterText = request.getParameter("sSearch_1");
        String amMemberFilterText = request.getParameter("sSearch_2");
        String sEcho = request.getParameter("sEcho");
        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");
        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));

        Collection<Long> loggedOnRegisterIds = em.createQuery(
                "select distinct(rm.register.id) " +
                        "from RegisterMember rm " +
                        "where rm.deleted = false " +
                        "and rm.professor.user.id = :userId", Long.class)
                .setParameter("userId", loggedOn.getId())
                .getResultList();


        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append("from Register r " +
                " join r.institution.name iname " +
                " where r.permanent = true ");

        if (StringUtils.isNotEmpty(institutionFilterText)) {
            searchQueryString.append(" and iname like  :institutionFilterText ");
        }

        if (StringUtils.isNotEmpty(subjectFilterText)) {
            searchQueryString.append(" and UPPER(r.subject.name) like :subjectFilterText ");
        }

        if (StringUtils.isNotEmpty(amMemberFilterText)) {
            if (amMemberFilterText.equalsIgnoreCase("true")) {
                searchQueryString.append(" and r.id in (:loggedOnRegisterIds) ");
            } else {
                searchQueryString.append(" and r.id not in (:loggedOnRegisterIds) ");
            }
        }

        Query countQuery = em.createQuery(" select count(distinct r.id) " +
                searchQueryString.toString());

        if (StringUtils.isNotEmpty(institutionFilterText)) {
            countQuery.setParameter("institutionFilterText", "%" + institutionFilterText.toUpperCase() + "%");
        }
        if (StringUtils.isNotEmpty(subjectFilterText)) {
            countQuery.setParameter("subjectFilterText", "%" + subjectFilterText.toUpperCase() + "%");
        }
        if (StringUtils.isNotEmpty(amMemberFilterText)) {
            countQuery.setParameter("loggedOnRegisterIds", loggedOnRegisterIds);
        }

        Long totalRecords = (Long) countQuery.getSingleResult();

        StringBuilder orderByClause = new StringBuilder();

        if (StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("subject")) {
                orderByClause.append(" order by r.subject.name " + orderDirection);
            }
        }

        TypedQuery<Register> searchQuery = em.createQuery(
                " select r from Register r " +
                        " where r.id in ( " +
                        " select distinct r.id " +
                        searchQueryString.toString() + ") " + orderByClause.toString(), Register.class);

        if (StringUtils.isNotEmpty(institutionFilterText)) {
            searchQuery.setParameter("institutionFilterText", "%" + institutionFilterText.toUpperCase() + "%");
        }
        if (StringUtils.isNotEmpty(subjectFilterText)) {
            searchQuery.setParameter("subjectFilterText", "%" + subjectFilterText.toUpperCase() + "%");
        }
        if (StringUtils.isNotEmpty(amMemberFilterText)) {
            searchQuery.setParameter("loggedOnRegisterIds", loggedOnRegisterIds);
        }

        // Prepare Query
        List<Register> registers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        for (Register r : registers) {
            r.setAmMember(loggedOnRegisterIds.contains(r.getId()));
        }

        SearchData<Register> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(registers);

        return result;
    }


    public List<RegisterMember> getRegisterMembers(Long institutionId, Set<Long> newRegisterMemberIds, boolean registerMembersIncluded) {

        String registerMemberIdsClause;
        if (registerMembersIncluded) {
            registerMemberIdsClause = "and m.id in (:registerMembersIds)";
        } else {
            registerMemberIdsClause = "and m.id not in (:registerMembersIds)";
        }

        List<RegisterMember> registerMembers = em.createQuery(
                "select distinct m from Register r " +
                        "join r.members m " +
                        "where r.permanent = true " +
                        "and r.institution.id = :institutionId " +
                        "and m.deleted = false " +
                        "and m.professor.status = :status " +
                        registerMemberIdsClause, RegisterMember.class)
                .setParameter("institutionId", institutionId)
                .setParameter("status", Role.RoleStatus.ACTIVE)
                .setParameter("registerMembersIds", newRegisterMemberIds)
                .getResultList();

        return registerMembers;
    }

    public Register create(Register newRegister, User loggedOn) throws NotFoundException, NotEnabledException {
        // find institution
        Institution institution = em.find(Institution.class, newRegister.getInstitution().getId());
        // Validate
        if (institution == null) {
            throw new NotFoundException("wrong.department.id");
        }
        if (!newRegister.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Update
        newRegister.setInstitution(institution);
        newRegister.setSubject(null);
        newRegister.setPermanent(false);
        em.persist(newRegister);
        em.flush();

        return getRegisterById(newRegister.getId());
    }

    public Register update(Long id, Register register, User loggedOn) throws NotEnabledException, NotFoundException, ValidationException {

        Register existingRegister;
        try {
            existingRegister = em.createQuery(
                    "select r from Register r " +
                            "left join fetch r.members rm " +
                            "left join fetch rm.professor p " +
                            "left join fetch p.user u " +
                            "left join fetch u.roles rls " +
                            "where r.id=:id", Register.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            throw new NotFoundException("wrong.register.id");
        }
        // Validate:
        if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingRegister.getInstitution().getId().equals(register.getInstitution().getId())) {
            throw new ValidationException("register.institution.change");
        }
        if (register.getSubject() == null || register.getSubject().getName() == null) {
            throw new ValidationException("register.missing.subject");
        }
        if (!CompareUtil.equalsIgnoreNull(register.getSubject(), existingRegister.getSubject())) {
            // Validate subject change
            try {
                em.createQuery(
                        "select r.id from Register r " +
                                "where r.subject.name = :name " +
                                "and r.institution.id = :institutionId " +
                                "and r.id != :registerId ", Long.class)
                        .setParameter("institutionId", existingRegister.getInstitution().getId())
                        .setParameter("name", register.getSubject().getName())
                        .setParameter("registerId", existingRegister.getId())
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("register.subject.unavailable");
            } catch (NoResultException e) {
            }
        }

        // Retrieve existing Members (ProfessorID -> RegisterMember)
        Map<Long, RegisterMember> existingMembersAsMap = new HashMap<Long, RegisterMember>();
        for (RegisterMember existingMember : existingRegister.getMembers()) {
            existingMembersAsMap.put(existingMember.getProfessor().getId(), existingMember);
        }
        // Retrieve new Member Professor Ids
        Set<Long> allProfessorIDs = new HashSet<Long>();
        List<Professor> allProfessors = new ArrayList<Professor>();

        // Calculate Added and Removed Professor IDs
        Set<Long> additionProfessorIds = new HashSet<Long>();
        Set<Long> removalProfessorIds = new HashSet<Long>();

        for (RegisterMember newCommitteeMember : register.getMembers()) {
            // retrieve the members going to be deleted
            if (newCommitteeMember.getToBeDeleted()) {
                removalProfessorIds.add(newCommitteeMember.getProfessor().getId());
            } else {
                additionProfessorIds.add(newCommitteeMember.getProfessor().getId());
            }
            allProfessorIDs.add(newCommitteeMember.getProfessor().getId());
        }

        if (!allProfessorIDs.isEmpty()) {
            TypedQuery<Professor> query = em.createQuery(
                    "select distinct p from Professor p " +
                            "where p.id in (:ids)", Professor.class)
                    .setParameter("ids", allProfessorIDs);
            allProfessors.addAll(query.getResultList());
        }
        if (allProfessorIDs.size() != allProfessors.size()) {
            throw new NotFoundException("wrong.professor.id");
        }

        // Validate removal
        if (removalProfessorIds.size() > 0) {
            try {
                em.createQuery("select pcm.id from PositionCommitteeMember pcm " +
                        "where pcm.committee.position.phase.status = :activeStatus " +
                        "and pcm.registerMember.register.id = :registerId " +
                        "and pcm.registerMember.professor.id in (:professorIds)", Long.class)
                        .setParameter("activeStatus", Position.PositionStatus.EPILOGI)
                        .setParameter("registerId", existingRegister.getId())
                        .setParameter("professorIds", removalProfessorIds)
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("professor.is.committee.member");
            } catch (NoResultException e) {
            }
            try {
                em.createQuery("select e.id from PositionEvaluator e " +
                        "where e.evaluation.position.phase.status = :activeStatus " +
                        "and e.registerMember.register.id = :registerId " +
                        "and e.registerMember.professor.id in (:professorIds)", Long.class)
                        .setParameter("activeStatus", Position.PositionStatus.EPILOGI)
                        .setParameter("registerId", existingRegister.getId())
                        .setParameter("professorIds", removalProfessorIds)
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("professor.is.evaluator");
            } catch (NoResultException e) {
            }
            try {
                em.createQuery("select e.id from CandidacyEvaluator e " +
                        "where e.candidacy.candidacies.position.phase.status = :activeStatus " +
                        "and e.registerMember.register.id = :registerId " +
                        "and e.registerMember.professor.id in (:professorIds)", Long.class)
                        .setParameter("activeStatus", Position.PositionStatus.EPILOGI)
                        .setParameter("registerId", existingRegister.getId())
                        .setParameter("professorIds", removalProfessorIds)
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("professor.is.candidacy.evaluator");
            } catch (NoResultException e) {
            }
        }
        // Validate addition
        for (Professor p : allProfessors) {
            if (additionProfessorIds.contains(p.getId()) && !p.getStatus().equals(Role.RoleStatus.ACTIVE)) {
                throw new ValidationException("professor.is.inactive");
            }
        }

        // Update

        // Add new members to existingMembersMap
        for (Professor professor : allProfessors) {
            if (!existingMembersAsMap.containsKey(professor.getId())) {
                // Create new member
                RegisterMember newMember = new RegisterMember();
                newMember.setRegister(existingRegister);
                newMember.setProfessor(professor);
                newMember.setDeleted(false);
                switch (professor.getDiscriminator()) {
                    case PROFESSOR_DOMESTIC:
                        newMember.setExternal(!existingRegister.getInstitution().getId().equals(((ProfessorDomestic) professor).getDepartment().getSchool().getInstitution().getId()));
                        break;
                    case PROFESSOR_FOREIGN:
                        newMember.setExternal(true);
                        break;
                    default:
                        break;
                }
                existingMembersAsMap.put(professor.getId(), newMember);
            }
        }
        // Set deleted flags based on removalProfessorIds
        for (RegisterMember rm : existingMembersAsMap.values()) {
            if (removalProfessorIds.contains(rm.getProfessor().getId()) || (rm.isDeleted() && !additionProfessorIds.contains(rm.getProfessor().getId()))) {
                rm.setDeleted(true);
            } else {
                rm.setDeleted(false);
            }
        }
        // Save
        existingRegister = existingRegister.copyFrom(register);
        existingRegister.setSubject(utilityService.supplementSubject(register.getSubject()));
        existingRegister.getMembers().clear();
        for (RegisterMember member : existingMembersAsMap.values()) {
            existingRegister.addMember(member);
        }
        existingRegister.setPermanent(true);
        existingRegister = em.merge(existingRegister);

        em.flush();

        // Send E-Mails:
        //1. To Added Members:
        for (Long professorId : additionProfessorIds) {
            // No need to check deleted flag here, additionProfessorIds are all deleted=false
            final RegisterMember savedMember = existingMembersAsMap.get(professorId);
            if (savedMember.isExternal()) {
                // register.create.register.member.external@member
                mailService.postEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "register.create.register.member.external@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", savedMember.getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", savedMember.getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedMember.getRegister().getInstitution().getName().get("el"));

                                put("firstname_en", savedMember.getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", savedMember.getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedMember.getRegister().getInstitution().getName().get("en"));

                                put("discipline", savedMember.getRegister().getSubject() != null ? savedMember.getRegister().getSubject().getName() : "");
                            }
                        }));
            } else {
                // register.create.register.member.internal@member
                mailService.postEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "register.create.register.member.internal@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", savedMember.getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", savedMember.getProfessor().getUser().getLastname("el"));
                                put("institution_el", savedMember.getRegister().getInstitution().getName().get("el"));

                                put("firstname_en", savedMember.getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", savedMember.getProfessor().getUser().getLastname("en"));
                                put("institution_en", savedMember.getRegister().getInstitution().getName().get("en"));

                                put("discipline", savedMember.getRegister().getSubject() != null ? savedMember.getRegister().getSubject().getName() : "");
                            }
                        }));
            }
        }
        //2. To Removed Members:
        for (Long professorID : removalProfessorIds) {
            // No need to check deleted flag here, removalProfessorIds are all deleted=true
            final RegisterMember removedMember = existingMembersAsMap.get(professorID);
            if (removedMember.isExternal()) {
                // register.remove.register.member.external@member
                mailService.postEmail(removedMember.getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "register.remove.register.member.external@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", removedMember.getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", removedMember.getProfessor().getUser().getLastname("el"));
                                put("institution_el", removedMember.getRegister().getInstitution().getName().get("el"));

                                put("firstname_en", removedMember.getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", removedMember.getProfessor().getUser().getLastname("en"));
                                put("institution_en", removedMember.getRegister().getInstitution().getName().get("en"));

                                put("discipline", removedMember.getRegister().getSubject() != null ? removedMember.getRegister().getSubject().getName() : "");
                            }
                        }));
            } else {
                // register.remove.register.member.internal@member
                mailService.postEmail(removedMember.getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "register.remove.register.member.internal@member",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", removedMember.getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", removedMember.getProfessor().getUser().getLastname("el"));
                                put("institution_el", removedMember.getRegister().getInstitution().getName().get("el"));

                                put("firstname_en", removedMember.getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", removedMember.getProfessor().getUser().getLastname("en"));
                                put("institution_en", removedMember.getRegister().getInstitution().getName().get("en"));

                                put("discipline", removedMember.getRegister().getSubject() != null ? removedMember.getRegister().getSubject().getName() : "");
                            }
                        }));
            }
        }
        // End: Send E-Mails

        // Return Result
        em.clear(); // Necessary, the attached object will be returned otherwise
        return getRegisterById(existingRegister.getId());
    }

    public void delete(Long id, User loggedOn) throws ValidationException, NotFoundException, NotEnabledException {
        // find register
        Register register = getById(id);

        // 1. Authorize
        if (!register.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // 2. Validate if can be deleted
        addCanBeDeletedInfo(register);
        for (RegisterMember rm : register.getMembers()) {
            if (!rm.getCanBeDeleted()) {
                throw new ValidationException("register.cannot.remove");
            }
        }

        // 3. Remove by setting permanent to false and deleted to true for members
        register.setPermanent(false);
        register = em.merge(register);
        for (RegisterMember rm : register.getMembers()) {
            rm.setDeleted(true);
            em.merge(rm);
        }

        em.flush();
    }


    private Register getRegisterById(Long id) throws NotFoundException {
        try {
            // A Hibernate specific solution, a JPA filtering mechanism does not exist
            Session session = em.unwrap(Session.class);
            session.enableFilter("filterDeleted");
            Register r = em.createQuery(
                    "select r from Register r " +
                            "left join fetch r.members rm " +
                            "left join fetch rm.professor p " +
                            "left join fetch p.user u " +
                            "left join fetch u.roles rls " +
                            "where r.id=:id", Register.class)
                    .setParameter("id", id)
                    .getSingleResult();

            addCanBeDeletedInfo(r);
            return r;
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.register.id");
        }
    }

    private void addCanBeDeletedInfo(Register register) {
        List<Long> nonRemovableMemberIds = em.createQuery(
                "select rm.id from RegisterMember rm " +
                        "where rm.register.id = :registerId " +
                        "and (" +
                        "	exists (" +
                        "		select pcm.id from PositionCommitteeMember pcm " +
                        "		where pcm.committee.position.phase.status = :activeStatus " +
                        "		and pcm.registerMember.id = rm.id " +
                        "	) " +
                        "	or exists (" +
                        "		select pe.id from PositionEvaluator pe " +
                        "		where pe.evaluation.position.phase.status = :activeStatus " +
                        "		and pe.registerMember.id = rm.id " +
                        "	) " +
                        "	or exists (" +
                        "		select ce.id from CandidacyEvaluator ce " +
                        "		where ce.candidacy.candidacies.position.phase.status = :activeStatus " +
                        "		and ce.registerMember.id = rm.id " +
                        "	) " +
                        ")", Long.class)
                .setParameter("activeStatus", Position.PositionStatus.EPILOGI)
                .setParameter("registerId", register.getId())
                .getResultList();

        for (RegisterMember member : register.getMembers()) {
            boolean cannotBeDeleted = nonRemovableMemberIds.contains(member.getId());
            member.setCanBeDeleted(!cannotBeDeleted);
        }
    }

    public SearchData<RegisterMember> searchRegisterMembers(Long registerId, HttpServletRequest request) {
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

        searchQueryString.append(" from RegisterMember rm " +
                "left join rm.professor p " +
                "left join p.user u " +
                "where rm.register.id=:id " +
                "and rm.deleted is false");

        if (StringUtils.isNotEmpty(filterText)) {
            searchQueryString.append(" and ( UPPER(u.basicInfo.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfoLatin.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfo.firstname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfoLatin.firstname) like :filterText ");
            searchQueryString.append(" or CAST(u.id AS text) like :filterText ");
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

        Query countQuery = em.createQuery(" select count(distinct rm.id) " +
                searchQueryString.toString())
                .setParameter("id", registerId);

        if (StringUtils.isNotEmpty(filterText)) {
            countQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        // A Hibernate specific solution, a JPA filtering mechanism does not exist
        Session session = em.unwrap(Session.class);
        session.enableFilter("filterDeleted");

        Long totalRecords = (Long) countQuery.getSingleResult();

        StringBuilder orderByClause = new StringBuilder();

        if (StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("id")) {
                orderByClause.append(" order by u.id " + orderDirection);
            } else if (orderField.equals("firstName")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by u.basicInfo.firstname " + orderDirection);
                } else {
                    orderByClause.append(" order by u.basicInfoLatin.firstname " + orderDirection);
                }
            } else if (orderField.equals("lastName")) {
                if (locale.equals("el")) {
                    orderByClause.append(" order by u.basicInfo.lastname " + orderDirection);
                } else {
                    orderByClause.append(" order by u.basicInfoLatin.lastname " + orderDirection);
                }
            } else if (orderField.equals("rank")) {
                orderByClause.append(" order by p.rank.name " + orderDirection);
            }
        }

        TypedQuery<RegisterMember> searchQuery = em.createQuery(
                " select rm from RegisterMember  rm " +
                        "left join rm.professor p " +
                        "left join p.user u " +
                        "where rm.id in ( " +
                        "select distinct rm.id " +
                        searchQueryString.toString() + ") " + orderByClause.toString(), RegisterMember.class);

        searchQuery.setParameter("id", registerId);

        if (StringUtils.isNotEmpty(filterText)) {
            searchQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        // Prepare Query
        List<RegisterMember> registerMemberList = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();


        List<Long> nonRemovableMemberIds = em.createQuery(
                "select rm.id from RegisterMember rm " +
                        "where rm.register.id = :registerId " +
                        "and (" +
                        "	exists (" +
                        "		select pcm.id from PositionCommitteeMember pcm " +
                        "		where pcm.committee.position.phase.status = :activeStatus " +
                        "		and pcm.registerMember.id = rm.id " +
                        "	) " +
                        "	or exists (" +
                        "		select pe.id from PositionEvaluator pe " +
                        "		where pe.evaluation.position.phase.status = :activeStatus " +
                        "		and pe.registerMember.id = rm.id " +
                        "	) " +
                        "	or exists (" +
                        "		select ce.id from CandidacyEvaluator ce " +
                        "		where ce.candidacy.candidacies.position.phase.status = :activeStatus " +
                        "		and ce.registerMember.id = rm.id " +
                        "	) " +
                        ")", Long.class)
                .setParameter("activeStatus", Position.PositionStatus.EPILOGI)
                .setParameter("registerId", registerId)
                .getResultList();

        for (RegisterMember member : registerMemberList) {
            boolean cannotBeDeleted = nonRemovableMemberIds.contains(member.getId());
            member.setCanBeDeleted(!cannotBeDeleted);
        }

        SearchData<RegisterMember> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(registerMemberList);

        return result;
    }

    public RegisterMember getRegisterMember(Long registerId, Long memberId) throws NotFoundException {
        // find register
        Register register = getById(registerId);

        for (RegisterMember member : register.getMembers()) {
            if (member.getId().equals(memberId)) {
                return member;
            }
        }
        throw new NotFoundException("wrong.register.member.id");
    }

    public Collection<Role> getRegisterProfessors(Long registerId, List<Long> roleIds, User loggedOn) throws NotFoundException, NotEnabledException {

        Register existingRegister = getById(registerId);
        if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Prepare Query
        List<Role.RoleDiscriminator> discriminatorList = new ArrayList<>();
        discriminatorList.add(Role.RoleDiscriminator.PROFESSOR_DOMESTIC);
        discriminatorList.add(Role.RoleDiscriminator.PROFESSOR_FOREIGN);

        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> professors = em.createQuery(
                    "select r from Role r " +
                            "where r.id in (:ids) " +
                            "and r.discriminator in (:discriminators) " +
                            "and r.status = :status", Role.class)
                    .setParameter("discriminators", discriminatorList)
                    .setParameter("status", Role.RoleStatus.ACTIVE)
                    .setParameter("ids", roleIds)
                    .getResultList();

            return professors;
        } else {
            return new ArrayList<>();
        }
    }

    public SearchData<Role> searchRegisterProfessors(Long registerId, HttpServletRequest request, User loggedOn) throws NotFoundException, NotEnabledException {

        Register existingRegister = getRegisterById(registerId);
        // Validate:
        if (existingRegister == null) {
            throw new NotFoundException("wrong.register.id");
        }
        if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // 1. Read parameters:
        Long userId = request.getParameter("user").matches("\\d+") ? Long.valueOf(request.getParameter("user")) : null;
        String firstname = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");
        String role = request.getParameter("role");
        Long rankId = request.getParameter("rank").matches("\\d+") ? Long.valueOf(request.getParameter("rank")) : null;
        String institution = request.getParameter("institution");
        String subject = request.getParameter("subject");

        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");
        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));
        // DataTables:
        String sEcho = request.getParameter("sEcho");

        // Prepare Query
        StringBuilder searchQueryString = new StringBuilder(
                "select rl.id from Role rl " +
                        "where rl.id is not null " +
                        "and rl.discriminator in (:discriminators) " +
                        "and rl.status = :status ");

        if (userId != null) {
            searchQueryString.append(" and rl.user.id = :userId ");
        }
        if (firstname != null && !firstname.isEmpty()) {
            searchQueryString.append(" and rl.user.basicInfo.firstname like :firstname");
        }
        if (lastname != null && !lastname.isEmpty()) {
            searchQueryString.append(" and rl.user.basicInfo.lastname like :lastname");
        }
        if (role != null && !role.isEmpty()) {
            searchQueryString.append(" and rl.discriminator = :role ");
        }
        if (rankId != null) {
            searchQueryString.append(" and ( " +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		where pd.id = rl.id " +
                    "		and pd.rank.id = :rankId " +
                    "	) " +
                    "	or exists (" +
                    "		select pf.id from ProfessorForeign pf " +
                    "		where pf.id = rl.id " +
                    "		and pf.rank.id = :rankId " +
                    "	) " +
                    ") ");
        }
        if (institution != null && !institution.isEmpty()) {
            searchQueryString.append(" and (" +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		join pd.department.name dname " +
                    "		join pd.department.school.name sname " +
                    "		join pd.department.school.institution.name iname " +
                    "		where pd.id = rl.id " +
                    "		and ( dname like :institution " +
                    "			or sname like :institution " +
                    "			or iname like :institution " +
                    "		)" +
                    "	) " +
                    "	or exists (" +
                    "		select pf.id from ProfessorForeign pf " +
                    "		where pf.id = rl.id " +
                    "		and UPPER(pf.foreignInstitution) like :institution " +
                    "	) " +
                    ") ");
        }
        if (subject != null && !subject.isEmpty()) {
            searchQueryString.append(" and (" +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		left join pd.fekSubject fsub " +
                    "		left join pd.subject sub " +
                    "		where pd.id = rl.id " +
                    "		and ( fsub.name like :subject or sub.name like :subject) " +
                    "	) " +
                    "	or exists (" +
                    "		select pf.id from ProfessorForeign pf " +
                    "		left join pf.subject sub " +
                    "		where pf.id = rl.id " +
                    "		and sub.name like :subject " +
                    "	) " +
                    ") ");
        }

        // Query Sorting
        String orderString;
        if (orderField != null && !orderField.isEmpty()) {
            if (orderField.equals("id")) {
                orderString = "order by r.user.id " + orderDirection;
            } else if (orderField.equals("profile")) {
                orderString = "order by r.discriminator " + orderDirection + ", r.user.id " + orderDirection;
            } else if (orderField.equals("firstname")) {
                orderString = "order by r.user.basicInfo.firstname " + orderDirection + " , r.user.basicInfo.lastname, r.user.id ";
            } else {
                // lastname is default
                orderString = "order by r.user.basicInfo.lastname " + orderDirection + " , r.user.basicInfo.firstname, r.user.id ";
            }
        } else {
            orderString = "order by r.user.basicInfo.lastname " + orderDirection + " , r.user.basicInfo.firstname, r.user.id ";
        }

        Query countQuery = em.createQuery(
                "select count(id) from Role r " +
                        "where r.id in ( " +
                        searchQueryString.toString() +
                        " ) ");
        TypedQuery<Role> searchQuery = em.createQuery(
                "select r from Role r " +
                        "left join fetch r.user u " +
                        "where r.id in ( " +
                        searchQueryString.toString() +
                        " ) " +
                        orderString, Role.class);

        // Parameters:
        List<Role.RoleDiscriminator> discriminatorList = new ArrayList<>();
        discriminatorList.add(Role.RoleDiscriminator.PROFESSOR_DOMESTIC);
        discriminatorList.add(Role.RoleDiscriminator.PROFESSOR_FOREIGN);
        searchQuery.setParameter("discriminators", discriminatorList);
        countQuery.setParameter("discriminators", discriminatorList);

        searchQuery.setParameter("status", Role.RoleStatus.ACTIVE);
        countQuery.setParameter("status", Role.RoleStatus.ACTIVE);

        if (userId != null) {
            searchQuery.setParameter("userId", userId);
            countQuery.setParameter("userId", userId);
        }
        if (firstname != null && !firstname.isEmpty()) {
            searchQuery.setParameter("firstname", "%" + StringUtil.toUppercaseNoTones(firstname, new Locale("el")) + "%");
            countQuery.setParameter("firstname", "%" + StringUtil.toUppercaseNoTones(firstname, new Locale("el")) + "%");
        }
        if (lastname != null && !lastname.isEmpty()) {
            searchQuery.setParameter("lastname", "%" + StringUtil.toUppercaseNoTones(lastname, new Locale("el")) + "%");
            countQuery.setParameter("lastname", "%" + StringUtil.toUppercaseNoTones(lastname, new Locale("el")) + "%");
        }
        if (role != null && !role.isEmpty()) {
            searchQuery.setParameter("role", Role.RoleDiscriminator.valueOf(role));
            countQuery.setParameter("role", Role.RoleDiscriminator.valueOf(role));
        }
        if (rankId != null) {
            searchQuery.setParameter("rankId", rankId);
            countQuery.setParameter("rankId", rankId);
        }
        if (institution != null && !institution.isEmpty()) {
            searchQuery.setParameter("institution", "%" + StringUtil.toUppercaseNoTones(institution, new Locale("el")) + "%");
            countQuery.setParameter("institution", "%" + StringUtil.toUppercaseNoTones(institution, new Locale("el")) + "%");
        }
        if (subject != null && !subject.isEmpty()) {
            searchQuery.setParameter("subject", "%" + StringUtil.toUppercaseNoTones(subject, new Locale("el")) + "%");
            countQuery.setParameter("subject", "%" + StringUtil.toUppercaseNoTones(subject, new Locale("el")) + "%");
        }
        // Execute
        Long totalRecords = (Long) countQuery.getSingleResult();
        List<Role> paginatedProfessors = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        // Return Result
        SearchData<Role> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(paginatedProfessors);

        return result;
    }

    public InputStream getProfessorsExport(User loggedOn) throws NotEnabledException {
        // Authorize
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Generate Document
        //1. Get Data
        List<Professor> professors = reportService.getProfessorData();
        //2. Create XLS
        InputStream is = reportService.createProfessorDataExcel(professors);

        // return stream
        return is;
    }

    public InputStream getRegisterExport(Long registerId, User loggedOn) throws NotFoundException, NotEnabledException {

        Register register = getById(registerId);
        // Authorize
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithInstitution(register.getInstitution())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Generate Document
        //1. Get Data
        List<Object[]> registerData = reportService.getRegisterExportData(registerId);
        //2. Create XLS
        InputStream is = reportService.createRegisterExportExcel(register, registerData);
        // return stream
        return is;
    }


}
