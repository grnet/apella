package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.Session;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class PositionCandidaciesService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private PositionService positionService;

    @EJB
    private MailService mailService;

    private PositionCandidacies getById(Long id) throws NotFoundException {
        PositionCandidacies positionCommittee = em.find(PositionCandidacies.class, id);

        if (positionCommittee == null) {
            throw new NotFoundException("wrong.position.candidacies.id");
        }

        return positionCommittee;
    }


    public List<PositionCandidaciesFile> getPositionCandidaciesFiles(Long existingCandidaciesId, Long candidacyId) {

        List<PositionCandidaciesFile> result = em.createQuery(
                "select pcf from PositionCandidaciesFile pcf " +
                        "where pcf.deleted = false " +
                        "and pcf.candidacies.id = :pcId " +
                        "and pcf.evaluator.candidacy.id = :cid ", PositionCandidaciesFile.class)
                .setParameter("pcId", existingCandidaciesId)
                .setParameter("cid", candidacyId)
                .getResultList();

        return result;
    }

    public Set<Candidacy> getPositionCandidacies(Long positionId, User loggedOn) throws NotEnabledException, NotFoundException {
        // get position
        Position position = positionService.getPositionById(positionId);

        if (position.getPhase().getCandidacies() == null) {
            throw new NotFoundException("wrong.position.id");
        }

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
                !(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
                !position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Set<Candidacy> result = new HashSet<Candidacy>();
        for (Candidacy candidacy : position.getPhase().getCandidacies().getCandidacies()) {
            if (candidacy.isPermanent()) {
                result.add(candidacy);
            }
        }
        return result;
    }

    public PositionCandidacies get(Long positionId, Long candidaciesId, User loggedOn) throws NotFoundException, NotEnabledException {
        try {
            Session session = em.unwrap(Session.class);
            session.enableFilter("filterPermanent");

            PositionCandidacies existingCandidacies = em.createQuery(
                    "select pc from PositionCandidacies pc " +
                            "left join fetch pc.candidacies c " +
                            "left join fetch c.proposedEvaluators pe " +
                            "where pc.id = :candidaciesId", PositionCandidacies.class)
                    .setParameter("candidaciesId", candidaciesId)
                    .getSingleResult();

            Position existingPosition = existingCandidacies.getPosition();
            if (!existingPosition.getId().equals(positionId)) {
                throw new NotFoundException("wrong.position.id");
            }
            if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                    !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                    !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                    !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                    !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                    !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                    !existingCandidacies.containsCandidate(loggedOn)) {
                throw new NotEnabledException("insufficient.privileges");
            }
            for (Candidacy candidacy : existingCandidacies.getCandidacies()) {
                if (!candidacy.isOpenToOtherCandidates() &&
                        !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId()) &&
                        existingCandidacies.containsCandidate(loggedOn)) {
                    candidacy.setAllowedToSee(Boolean.FALSE);
                } else {
                    // all other cases: owner, admin, mm, ma, im, ia, committee, evaluator
                    candidacy.setAllowedToSee(Boolean.TRUE);
                }
            }
            return existingCandidacies;
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.position.candidacies.id");
        }

    }

    public Collection<PositionCandidaciesFile> getFiles(Long positionId, Long candidaciesId, User loggedOn) throws NotFoundException, NotEnabledException {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingCandidacies.containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Set<PositionCandidaciesFile> result = FileHeader.filterDeleted(existingCandidacies.getFiles());
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
            // Filter Files that do not belong to loggedOnUser
            Iterator<PositionCandidaciesFile> it = result.iterator();
            while (it.hasNext()) {
                PositionCandidaciesFile file = it.next();
                if (!file.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
                    it.remove();
                }
            }
        }
        return result;
    }

    public PositionCandidaciesFile getFile(Long positionId, Long candidaciesId, Long fileId, User loggedOn) throws NotEnabledException, NotFoundException {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Find File
        Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
        PositionCandidaciesFile existingFile = null;
        for (PositionCandidaciesFile file : files) {
            if (file.getId().equals(fileId)) {
                existingFile = file;
            }
        }
        if (existingFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingFile.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        return existingFile;
    }

    public FileBody getFileBody(Long positionId, Long candidaciesId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Find File
        Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
        PositionCandidaciesFile existingFile = null;
        for (PositionCandidaciesFile file : files) {
            if (file.getId().equals(fileId)) {
                existingFile = file;
            }
        }
        if (existingFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingFile.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        for (FileBody fb : existingFile.getBodies()) {
            if (fb.getId().equals(bodyId)) {
                return fb;
            }
        }
        return null;
    }

    public FileHeader createFile(Long positionId, Long candidaciesId, HttpServletRequest request, User loggedOn) throws Exception {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
            throw new ValidationException("wrong.position.candidacies.phase");
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
        // Find required type and candidacy evaluator:
        FileType type = null;
        Long candidacyEvaluatorId = null;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
            } else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
                candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
            }
        }
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }
        if (candidacyEvaluatorId == null) {
            throw new ValidationException("missing.candidacy.evaluator");
        }
        final CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
        if (existingEvaluator == null) {
            throw new ValidationException("wrong.candidacy.evaluator.id");
        }

        // Check number of file types
        Set<PositionCandidaciesFile> pcFiles = FileHeader.filterIncludingDeleted(existingCandidacies.getFiles(), type);
        PositionCandidaciesFile existingFile = checkNumberOfFileTypes(PositionCandidaciesFile.fileTypes, type, pcFiles);
        if (existingFile != null) {
            return _updateFile(loggedOn, fileItems, existingFile);
        }
        // Create
        final PositionCandidaciesFile pcFile = new PositionCandidaciesFile();
        pcFile.setCandidacies(existingCandidacies);
        pcFile.setEvaluator(existingEvaluator);
        pcFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, pcFile);
        existingCandidacies.addFile(pcFile);
        em.flush();

        // Send E-Mails
        if (pcFile.getType().equals(FileType.EISIGISI_DEP_YPOPSIFIOU)) {
            if (!existingEvaluator.getCandidacy().isWithdrawn() && existingEvaluator.getCandidacy().isPermanent()) {
                // positionCandidacies.upload.eisigisi@candidate
                mailService.postEmail(existingEvaluator.getCandidacy().getCandidate().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCandidacies.upload.eisigisi@candidate",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
                                put("lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
                                put("firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
                                put("lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
                                put("evaluator_firstname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("evaluator_lastname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("evaluator_firstname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("evaluator_lastname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                            }
                        }));
            }
            // positionCandidacies.upload.eisigisi@candidacyEvaluator
            mailService.postEmail(existingEvaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "positionCandidacies.upload.eisigisi@candidacyEvaluator",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("firstname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                            put("lastname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                            put("firstname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                            put("lastname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                            put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
                            put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
                            put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
                            put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));

                            put("positionID", StringUtil.formatPositionID(existingEvaluator.getCandidacy().getCandidacies().getPosition().getId()));
                            put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
                            put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));
                            put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));
                        }
                    }));

            // positionCandidacies.upload.eisigisi@committee
            for (final PositionCommitteeMember member : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCandidacies.upload.eisigisi@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {

                                put("positionID", StringUtil.formatPositionID(existingEvaluator.getCandidacy().getCandidacies().getPosition().getId()));
                                put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
                                put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));
                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
                                put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionCandidacies.upload.eisigisi@evaluators
            for (final PositionEvaluator evaluator : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionCandidacies.upload.eisigisi@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingEvaluator.getCandidacy().getCandidacies().getPosition().getId()));
                                put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
                                put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
                                put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
                                put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
                                put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        } else {
            // position.upload@committee
            for (final PositionCommitteeMember member : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(pcFile.getCandidacies().getPosition().getId()));
                                put("position", pcFile.getCandidacies().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // position.upload@candidates
            for (final Candidacy candidacy : pcFile.getCandidacies().getCandidacies()) {
                if (!existingEvaluator.getCandidacy().isWithdrawn() && existingEvaluator.getCandidacy().isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "position.upload@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(pcFile.getCandidacies().getPosition().getId()));
                                    put("position", pcFile.getCandidacies().getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
            // position.upload@evaluators
            for (final PositionEvaluator evaluator : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(pcFile.getCandidacies().getPosition().getId()));
                                put("position", pcFile.getCandidacies().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        }

        // End: Send E-Mails

        return pcFile;
    }


    public FileHeader updateFile(Long positionId, Long candidaciesId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
            throw new NotEnabledException("wrong.position.candidacies.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }
        // Parse Request
        List<FileItem> fileItems = readMultipartFormData(request);
        // Find required type and candidacy evaluator:
        FileType type = null;
        Long candidacyEvaluatorId = null;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
            } else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
                candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
            }
        }
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }
        if (candidacyEvaluatorId == null) {
            throw new ValidationException("missing.candidacy.evaluator");
        }
        CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
        if (existingEvaluator == null) {
            throw new ValidationException("wrong.candidacy.evaluator.id");
        }

        if (!PositionCandidaciesFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }
        PositionCandidaciesFile candidaciesFile = null;
        for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
            if (file.getId().equals(fileId)) {
                candidaciesFile = file;
                break;
            }
        }
        if (candidaciesFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Update
        candidaciesFile.setEvaluator(existingEvaluator);
        return _updateFile(loggedOn, fileItems, candidaciesFile);
    }

    public void deleteFile(Long candidaciesId, Long positionId, Long fileId, User loggedOn) throws Exception {
        PositionCandidacies existingCandidacies = getById(candidaciesId);
        // get position
        Position existingPosition = existingCandidacies.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
            throw new NotEnabledException("wrong.position.candidacies.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }

        PositionCandidaciesFile candidaciesFile = null;
        for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
            if (file.getId().equals(fileId)) {
                candidaciesFile = file;
                break;
            }
        }
        if (candidaciesFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        PositionCandidaciesFile cf = deleteAsMuchAsPossible(candidaciesFile);
        if (cf == null) {
            existingCandidacies.getFiles().remove(candidaciesFile);
        }
    }

    private CandidacyEvaluator findCandidacyEvaluator(PositionCandidacies candidacies, Long candidacyEvaluatorId) {
        for (Candidacy candidacy : candidacies.getCandidacies()) {
            for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
                if (evaluator.getId().equals(candidacyEvaluatorId)) {
                    return evaluator;
                }
            }
        }
        return null;
    }


}
