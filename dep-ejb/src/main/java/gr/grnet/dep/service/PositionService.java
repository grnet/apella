package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PositionService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private UtilityService utilityService;

    @EJB
    private MailService mailService;

    @EJB
    private ReportService reportService;

    public Position getPositionById(Long id) throws NotFoundException {

        Position position = em.find(Position.class, id);

        if (position == null) {
            throw new NotFoundException("wrong.position.id");
        }

        return position;
    }

    public Position getPosition(Long positionId) throws NotFoundException {
        Position position;
        try {
            position = em.createQuery(
                    "from Position p " +
                            "left join fetch p.phases ph " +
                            "left join fetch ph.candidacies ca " +
                            "left join fetch ph.committee co " +
                            "left join fetch ph.evaluation ev " +
                            "left join fetch ph.nomination no " +
                            "left join fetch ph.complementaryDocuments cd " +
                            "where p.id = :positionId ", Position.class)
                    .setParameter("positionId", positionId)
                    .getSingleResult();

            for (User assistant : position.getAssistants()) {
                assistant.getRoles().size();
            }
            position.getCreatedBy().getRoles().size();
            position.getManager().getUser().getRoles().size();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.position.id");
        }
        return position;
    }

    public SearchData<Position> getAll(HttpServletRequest request, User loggedOnUser) throws NotEnabledException {
        // validations
        if (!loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) && !loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOnUser.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && !loggedOnUser.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOnUser.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT)) {

            throw new NotEnabledException("insufficient.privileges");
        }

        String positionIdFilterText = request.getParameter("sSearch_0");
        String positionTitleFilterText = request.getParameter("sSearch_1");
        String institutionFilterText = request.getParameter("sSearch_2");
        String statusFilterText = request.getParameter("sSearch_3");
        String locale = request.getParameter("locale");
        String sEcho = request.getParameter("sEcho");

        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");

        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));

        List<Institution> institutions = new ArrayList<Institution>();
        institutions.addAll(loggedOnUser.getAssociatedInstitutions());

        Position.PositionStatus status = null;

        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append(" from Position p " +
                "join  p.phase ph " +
                "join  ph.candidacies cs " +
                "left join  p.assistants " +
                "where p.permanent = true ");

        if (loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) ||
                loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            searchQueryString.append(" and p.department.school.institution in (:institutions) ");
        }

        if (StringUtils.isNotEmpty(positionIdFilterText)) {
            searchQueryString.append(" and CAST(p.id AS text) like  :positionIdFilterText ");
        }

        if (StringUtils.isNotEmpty(positionTitleFilterText)) {
            searchQueryString.append(" and UPPER(p.name) like :positionTitleFilterText ");
        }
        if (StringUtils.isNotEmpty(institutionFilterText)) {
            searchQueryString.append(" and (" +
                    "	exists ( " +
                    "       select d from Department d" +
                    " join d.name dname " +
                    " join d.school.name sname " +
                    " join d.school.institution.name iname " +
                    " where p.department.id = d.id " +
                    " and ( dname like :institutionFilterText " +
                    " or sname like :institutionFilterText " +
                    " or iname like :institutionFilterText ) " +
                    "	) " +
                    " ) ");
        }

        // for back end there is one status for open and closed positions. The difference is that for closed positions
        // the closing date has passed
        if (StringUtils.isNotEmpty(statusFilterText)) {
            // convert closed status to open
            if (statusFilterText.equals("KLEISTI")) {
                status = Position.PositionStatus.ANOIXTI;
                searchQueryString.append(" and ph.status = :status and now() > ph.candidacies.closingDate ");
            } else {
                // find if the input value is one of the position statuses
                for (Position.PositionStatus positionStatus : Position.PositionStatus.values()) {
                    if (positionStatus.toString().equals(statusFilterText)) {
                        status = positionStatus;
                        searchQueryString.append(" and ph.status  = :status ");
                        if (status == Position.PositionStatus.ANOIXTI) {
                            searchQueryString.append(" and now() <= ph.candidacies.closingDate ");
                        }
                        break;
                    }
                }
            }
        }

        Query countQuery = em.createQuery(" select count(distinct p.id) " +
                searchQueryString.toString());

        if (loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) ||
                loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            countQuery.setParameter("institutions", institutions);
        }

        if (StringUtils.isNotEmpty(positionIdFilterText)) {
            countQuery.setParameter("positionIdFilterText", positionIdFilterText.toUpperCase() + "%");
        }

        if (StringUtils.isNotEmpty(positionTitleFilterText)) {
            countQuery.setParameter("positionTitleFilterText", "%" + positionTitleFilterText.toUpperCase() + "%");
        }

        if (StringUtils.isNotEmpty(institutionFilterText)) {
            countQuery.setParameter("institutionFilterText", "%" + institutionFilterText.toUpperCase() + "%");
        }

        if (status != null) {
            countQuery.setParameter("status", status);
        }

        Long totalRecords = (Long) countQuery.getSingleResult();

        StringBuilder orderByClause = new StringBuilder();

        if (StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("positionId")) {
                orderByClause.append(" order by p.id " + orderDirection);
            } else if (orderField.equals("positionName")) {
                orderByClause.append(" order by p.name " + orderDirection);
            }
        }

        TypedQuery<Position> searchQuery = em.createQuery(
                " select p from Position p " +
                        "where p.id in ( " +
                        "select distinct p.id " +
                        searchQueryString.toString() + ") " + orderByClause.toString(), Position.class);

        if (loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) ||
                loggedOnUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            searchQuery.setParameter("institutions", institutions);
        }

        if (StringUtils.isNotEmpty(positionIdFilterText)) {
            searchQuery.setParameter("positionIdFilterText", positionIdFilterText.toUpperCase() + "%");
        }

        if (StringUtils.isNotEmpty(positionTitleFilterText)) {
            searchQuery.setParameter("positionTitleFilterText", "%" + positionTitleFilterText.toUpperCase() + "%");
        }

        if (StringUtils.isNotEmpty(institutionFilterText)) {
            searchQuery.setParameter("institutionFilterText", "%" + institutionFilterText.toUpperCase() + "%");
        }

        if (status != null) {
            searchQuery.setParameter("status", status);
        }

        List<Position> positions = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        SearchData<Position> result = new SearchData<>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(positions);

        return result;
    }

    public Collection<Position> getPublic() throws Exception {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date today = sdf.parse(sdf.format(new Date()));
            List<Position> positions = em.createQuery(
                    "from Position p " +
                            "where p.phase.candidacies.closingDate >= :today " +
                            "and p.permanent = true ", Position.class)
                    .setParameter("today", today)
                    .getResultList();

            return positions;
        } catch (ParseException e) {
            throw new Exception("parse.exception");
        }
    }

    public Position create(Position position, User loggedOn) throws NotFoundException, NotEnabledException {
        Department department = em.find(Department.class, position.getDepartment().getId());
        if (department == null) {
            throw new NotFoundException("wrong.department.id");
        }
        position.setDepartment(department);
        if (!position.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        Date now = new Date();
        position.setPermanent(false);
        position.setCreatedBy(loggedOn);

        position.setPhase(new PositionPhase());
        position.getPhase().setPosition(position);
        position.getPhase().setOrder(0);
        position.getPhase().setStatus(Position.PositionStatus.ENTAGMENI);
        position.getPhase().setCreatedAt(now);
        position.getPhase().setUpdatedAt(now);
        position.getPhase().setCandidacies(new PositionCandidacies());
        position.getPhase().getCandidacies().setPosition(position);
        position.getPhase().getCandidacies().setCreatedAt(now);
        position.getPhase().getCandidacies().setUpdatedAt(now);
        position.getPhase().setCommittee(null);
        position.getPhase().setComplementaryDocuments(new PositionComplementaryDocuments());
        position.getPhase().getComplementaryDocuments().setPosition(position);
        position.getPhase().getComplementaryDocuments().setCreatedAt(now);
        position.getPhase().getComplementaryDocuments().setUpdatedAt(now);

        position.getPhase().setNomination(null);

        position = em.merge(position);
        em.flush();

        position = getPosition(position.getId());

        return position;
    }

    public Position update(Long id, Position position, User loggedOn) throws NotFoundException {
        // get position
        final Position existingPosition = getPosition(id);

        boolean isNew = !existingPosition.isPermanent();
        // Validate
        Sector sector = em.find(Sector.class, position.getSector().getId());
        if (sector == null) {
            throw new NotFoundException("wrong.sector.id");
        }
        // Validate that openingDate is greater than today for new position
        if (isNew && DateUtil.compareDates(position.getPhase().getCandidacies().getOpeningDate(), new Date()) < 0) {
            throw new NotFoundException("position.opening.date.greater.today");
        }
        // Only admin can change positionCandidacies dates if permanent, just ignore changes otherwise
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && existingPosition.isPermanent()) {
            position.getPhase().getCandidacies().setOpeningDate(existingPosition.getPhase().getCandidacies().getOpeningDate());
            position.getPhase().getCandidacies().setClosingDate(existingPosition.getPhase().getCandidacies().getClosingDate());
        }
        // Update

        existingPosition.copyFrom(position);
        existingPosition.setSector(sector);
        existingPosition.setSubject(utilityService.supplementSubject(position.getSubject()));
        if (isNew) {
            // Managers
            Set<Long> managerIds = new HashSet<Long>();
            for (User manager : position.getAssistants()) {
                managerIds.add(manager.getId());
            }
            if (managerIds.isEmpty()) {
                existingPosition.getAssistants().clear();
            } else {
                @SuppressWarnings("unchecked")
                List<User> assistants = em.createQuery(
                        "select usr from User usr " +
                                "left join fetch usr.roles rls " +
                                "where usr.id in ( " +
                                "	select u.id from User u " +
                                "	join u.roles r " +
                                "	where u.id in (:managerIds) " +
                                "	and r.discriminator = :discriminator " +
                                "	and u.status = :userStatus " +
                                "	and r.status = :roleStatus " +
                                ")", User.class)
                        .setParameter("managerIds", managerIds)
                        .setParameter("discriminator", Role.RoleDiscriminator.INSTITUTION_ASSISTANT)
                        .setParameter("userStatus", User.UserStatus.ACTIVE)
                        .setParameter("roleStatus", Role.RoleStatus.ACTIVE)
                        .getResultList();
                existingPosition.getAssistants().clear();
                existingPosition.getAssistants().addAll(assistants);
            }
        }
        existingPosition.setPermanent(true);
        em.flush();

        // Send E-Mails
        if (isNew) {
            sendNotificationsToInterestedCandidates(existingPosition);
            // Send to Assistants
            for (final User assistant : existingPosition.getAssistants()) {
                mailService.postEmail(assistant.getContactInfo().getEmail(),
                        "default.subject",
                        "position.create@institutionAssistant",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
                                put("position", existingPosition.getName());

                                put("firstname_el", assistant.getFirstname("el"));
                                put("lastname_el", assistant.getLastname("el"));
                                put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingPosition.getDepartment().getName().get("el"));

                                put("firstname_en", assistant.getFirstname("en"));
                                put("lastname_en", assistant.getLastname("en"));
                                put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingPosition.getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");
                            }
                        }));
            }

            if (loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
                // Send also to manager and alternateManager
                final InstitutionManager im = existingPosition.getManager();
                mailService.postEmail(im.getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.create@institutionManager",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
                                put("position", existingPosition.getName());

                                put("firstname_el", im.getUser().getFirstname("el"));
                                put("lastname_el", im.getUser().getLastname("el"));
                                put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingPosition.getDepartment().getName().get("el"));

                                put("firstname_en", im.getUser().getFirstname("en"));
                                put("lastname_en", im.getUser().getLastname("en"));
                                put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingPosition.getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");
                            }
                        }));
                mailService.postEmail(im.getAlternateContactInfo().getEmail(),
                        "default.subject",
                        "position.create@institutionManager",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
                                put("position", existingPosition.getName());

                                put("firstname_el", im.getAlternateFirstname("el"));
                                put("lastname_el", im.getAlternateLastname("el"));
                                put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingPosition.getDepartment().getName().get("el"));

                                put("firstname_en", im.getAlternateFirstname("en"));
                                put("lastname_en", im.getAlternateLastname("en"));
                                put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingPosition.getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#positions");
                            }
                        }));
            }
        }

        // Return result
        return existingPosition;
    }

    public void delete(Long id, User loggedOn) throws NotEnabledException, ValidationException, NotFoundException {
        Position position = getPosition(id);
        if (!position.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.ENTAGMENI)) {
            throw new ValidationException("wrong.position.status.cannot.delete");
        }

        em.remove(position);
        em.flush();
    }

    public Collection<User> getPositionAssistants(Long positionId, User loggedOn) throws NotEnabledException, NotFoundException {
        Position existingPosition = getPosition(positionId);
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        List<User> managers = em.createQuery(
                "select usr from User usr " +
                        "left join fetch usr.roles rls " +
                        "where usr.id in ( " +
                        "	select u.id from InstitutionAssistant ia " +
                        "	join ia.user u " +
                        "	where u.status = :userStatus " +
                        "	and ia.status = :roleStatus " +
                        "	and ia.institution.id = :institutionId  " +
                        ")", User.class)
                .setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
                .setParameter("userStatus", User.UserStatus.ACTIVE)
                .setParameter("roleStatus", Role.RoleStatus.ACTIVE)
                .getResultList();

        return managers;
    }

    public Position addPhase(Long positionId, Position position, User loggedOn) throws NotEnabledException, NotFoundException, ValidationException {

        Position.PositionStatus newStatus = position.getPhase().getStatus();
        Position existingPosition = getPosition(positionId);
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.isPermanent()) {
            throw new ValidationException("wrong.position.status");
        }
        PositionPhase existingPhase = existingPosition.getPhase();
        PositionPhase newPhase = null;
        // Go through all transition scenarios,
        // update when needed or throw exception if transition not allowed
        switch (existingPhase.getStatus()) {
            case ENTAGMENI:
                switch (newStatus) {
                    case ENTAGMENI:
                        break;
                    case ANOIXTI:
                        // Validate:
                        if (existingPhase.getCandidacies().getOpeningDate().compareTo(new Date()) > 0) {
                            throw new ValidationException("position.phase.anoixti.wrong.opening.date");
                        }
                        if (existingPhase.getCandidacies().getClosingDate().compareTo(new Date()) < 0) {
                            throw new ValidationException("position.phase.anoixti.wrong.closing.date");
                        }
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.ANOIXTI);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                    case EPILOGI:
                    case ANAPOMPI:
                    case STELEXOMENI:
                    case CANCELLED:
                        throw new ValidationException("wrong.position.status");
                }
                break;
            case ANOIXTI:
                switch (newStatus) {
                    case ENTAGMENI:
                        throw new ValidationException("wrong.position.status");
                    case ANOIXTI:
                        break;
                    case EPILOGI:
                        // Validate
                        if (existingPhase.getCandidacies().getClosingDate().compareTo(new Date()) >= 0) {
                            throw new ValidationException("position.phase.epilogi.wrong.closing.date");
                        }
                        // Update
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.EPILOGI);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(new PositionCommittee());
                        newPhase.getCommittee().setPosition(existingPosition);
                        newPhase.setEvaluation(new PositionEvaluation());
                        newPhase.getEvaluation().setPosition(existingPosition);
                        newPhase.setNomination(new PositionNomination());
                        newPhase.getNomination().setPosition(existingPosition);
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                    case ANAPOMPI:
                    case STELEXOMENI:
                        throw new ValidationException("wrong.position.status");
                    case CANCELLED:
                        // Validate:
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.CANCELLED);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                }
                break;
            case EPILOGI:
                switch (newStatus) {
                    case ENTAGMENI:
                    case ANOIXTI:
                        throw new ValidationException("wrong.position.status");
                    case EPILOGI:
                        break;
                    case ANAPOMPI:
                        // Validate
                        if (FileHeader.filter(existingPhase.getNomination().getFiles(), FileType.APOFASI_ANAPOMPIS).size() == 0) {
                            throw new ValidationException("position.phase.anapompi.missing.apofasi");
                        }
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.ANAPOMPI);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(existingPhase.getCommittee());
                        newPhase.setEvaluation(existingPhase.getEvaluation());
                        newPhase.setNomination(existingPhase.getNomination());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                    case STELEXOMENI:
                        // Validate
                        if (existingPhase.getNomination().getNominatedCandidacy() == null) {
                            throw new ValidationException("position.phase.stelexomeni.missing.nominated.candidacy");
                        }
                        if (FileHeader.filter(existingPhase.getNomination().getFiles(), FileType.PRAKTIKO_EPILOGIS).size() == 0) {
                            throw new ValidationException("position.phase.stelexomeni.missing.praktiko.epilogis");
                        }
                        // Update
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.STELEXOMENI);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(existingPhase.getCommittee());
                        newPhase.setEvaluation(existingPhase.getEvaluation());
                        newPhase.setNomination(existingPhase.getNomination());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                    case CANCELLED:
                        // Validate
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.CANCELLED);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(existingPhase.getCommittee());
                        newPhase.setEvaluation(existingPhase.getEvaluation());
                        newPhase.setNomination(existingPhase.getNomination());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                }
                break;
            case STELEXOMENI:
                switch (newStatus) {
                    case ENTAGMENI:
                    case ANOIXTI:
                    case EPILOGI:
                    case ANAPOMPI:
                    case STELEXOMENI:
                        throw new ValidationException("wrong.position.status");
                    case CANCELLED:
                        // Validate
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.CANCELLED);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(existingPhase.getCommittee());
                        newPhase.setEvaluation(existingPhase.getEvaluation());
                        newPhase.setNomination(existingPhase.getNomination());
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                }
                break;
            case ANAPOMPI:
                switch (newStatus) {
                    case ENTAGMENI:
                    case ANOIXTI:
                        throw new ValidationException("wrong.position.status");
                    case EPILOGI:
                        newPhase = new PositionPhase();
                        newPhase.setStatus(Position.PositionStatus.EPILOGI);
                        newPhase.setCandidacies(existingPhase.getCandidacies());
                        newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
                        newPhase.setCommittee(new PositionCommittee());
                        newPhase.getCommittee().setPosition(existingPosition);
                        newPhase.setEvaluation(new PositionEvaluation());
                        newPhase.getEvaluation().setPosition(existingPosition);
                        newPhase.setNomination(new PositionNomination());
                        newPhase.getNomination().setPosition(existingPosition);
                        // Add to Position
                        existingPosition.addPhase(newPhase);
                        break;
                    case ANAPOMPI:
                        throw new ValidationException("wrong.position.status");
                    case STELEXOMENI:
                        throw new ValidationException("wrong.position.status");
                    case CANCELLED:
                        throw new ValidationException("wrong.position.status");

                }
                break;
            case CANCELLED:
                throw new ValidationException("wrong.position.status");
        }
        em.flush();

        return existingPosition;
    }

    public InputStream getPositionsExport(User loggedOn) throws NotEnabledException {
        // Authorize
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Generate Document
        Long institutionId = null;
        if (loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER)) {
            institutionId = ((InstitutionManager) loggedOn.getActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER)).getInstitution().getId();
        }
        if (institutionId == null &&
                loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            institutionId = ((InstitutionAssistant) loggedOn.getActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)).getInstitution().getId();
        }
        //1. Get Data
        List<Position> positionsData = reportService.getPositionsExportData(institutionId);
        //2. Create XLS
        InputStream is = reportService.createPositionsExportExcel(positionsData);
        // Return stream
        return is;
    }


    private void sendNotificationsToInterestedCandidates(final Position position) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date today = sdf.parse(sdf.format(new Date()));
            // Execute Query
            @SuppressWarnings("unchecked")
            List<Candidate> candidates = em.createQuery(
                    "select distinct(c.candidate) from PositionSearchCriteria c " +
                            "left join c.departments d " +
                            "left join c.sectors s " +
                            "where ((s is null) and (d is not null) and (d.id = :departmentId)) " +
                            "or ((d is null) and (s is not null) and (s.id = :sectorId)) " +
                            "or ((s is not null) and (s.id = :sectorId) and (d is not null) and (d.id = :departmentId))", Candidate.class)
                    .setParameter("departmentId", position.getDepartment().getId())
                    .setParameter("sectorId", position.getSector().getId())
                    .getResultList();

            // Send E-Mails
            for (final Candidate c : candidates) {
                mailService.postEmail(c.getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.create@interested.candidates",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(position.getId()));
                                put("position", position.getName());
                                put("discipline", position.getSubject() != null ? position.getSubject().getName() : "");
                                put("openingDate", sdf.format(position.getPhase().getCandidacies().getOpeningDate()));
                                put("closingDate", sdf.format(position.getPhase().getCandidacies().getClosingDate()));

                                put("firstname_el", c.getUser().getFirstname("el"));
                                put("lastname_el", c.getUser().getLastname("el"));
                                put("institution_el", position.getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", position.getDepartment().getSchool().getName().get("el"));
                                put("department_el", position.getDepartment().getName().get("el"));

                                put("firstname_en", c.getUser().getFirstname("en"));
                                put("lastname_en", c.getUser().getLastname("en"));
                                put("institution_en", position.getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", position.getDepartment().getSchool().getName().get("en"));
                                put("department_en", position.getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#sposition");
                            }
                        }));
            }
        } catch (ParseException e) {
            log.log(Level.WARNING, "parse exception");
        }

    }


}
