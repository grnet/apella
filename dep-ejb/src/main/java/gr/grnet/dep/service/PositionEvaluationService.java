package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionEvaluatorFile;
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
public class PositionEvaluationService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private MailService mailService;

    private PositionEvaluation getById(Long id) throws NotFoundException {

        PositionEvaluation positionEvaluation = em.find(PositionEvaluation.class, id);

        if (positionEvaluation == null) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        return positionEvaluation;
    }

    public PositionEvaluation getPositionEvaluation(Long positionId, Long evaluationId, User loggedOn) throws NotEnabledException, NotFoundException {
        PositionEvaluation existingEvaluation;
        try {
            existingEvaluation = em.createQuery(
                    "from PositionEvaluation pe " +
                            "left join fetch pe.evaluators pee " +
                            "where pe.id = :evaluationId", PositionEvaluation.class)
                    .setParameter("evaluationId", evaluationId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingPosition.getPhase().getCommittee().containsMember(loggedOn) &&
                !existingEvaluation.containsEvaluator(loggedOn) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        existingEvaluation.canUpdateEvaluators();
        existingEvaluation.canUploadEvaluations();

        return existingEvaluation;
    }

    public PositionEvaluator getPositionEvaluator(Long positionId, Long evaluationId, Long evaluatorId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get evaluation
        PositionEvaluation existingEvaluation = getById(evaluationId);
        // get position
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !existingPosition.getPhase().getCommittee().containsMember(loggedOn) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Search:
        PositionEvaluator existingEvaluator = null;
        for (PositionEvaluator eval : existingEvaluation.getEvaluators()) {
            if (eval.getId().equals(evaluatorId)) {
                existingEvaluator = eval;
                break;
            }
        }
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        //Return
        return existingEvaluator;
    }

    public PositionEvaluation updatePositionEvaluation(Long positionId, Long evaluationId, PositionEvaluation newEvaluation, User loggedOn) throws ValidationException, NotEnabledException, NotFoundException {
        // get evaluation
        final PositionEvaluation existingEvaluation = getById(evaluationId);
        // get position
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
            throw new NotEnabledException("wrong.position.evaluation.phase");
        }
        // Validate:
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }
        if (newEvaluation.getEvaluators().size() != PositionEvaluator.MAX_MEMBERS) {
            throw new ValidationException("wrong.evaluators.size");
        }
        if (!existingEvaluation.canUpdateEvaluators()) {
            throw new ValidationException("committee.missing.praktiko.synedriasis.epitropis.gia.aksiologites");
        }

        // Retrieve new Members from Institution's Register
        Map<Long, PositionEvaluator> existingEvaluatorsRegisterMap = new HashMap<>();
        for (PositionEvaluator existingEvaluator : existingEvaluation.getEvaluators()) {
            existingEvaluatorsRegisterMap.put(existingEvaluator.getRegisterMember().getId(), existingEvaluator);
        }
        // Retrieve new Members from Institution's Register
        Map<Long, Long> newEvaluatorsRegisterMap = new HashMap<>();
        for (PositionEvaluator newEvaluator : newEvaluation.getEvaluators()) {
            newEvaluatorsRegisterMap.put(newEvaluator.getRegisterMember().getId(), newEvaluator.getPosition());
        }
        List<RegisterMember> newRegisterMembers = new ArrayList<>();
        if (!newEvaluatorsRegisterMap.isEmpty()) {
            TypedQuery<RegisterMember> query = em.createQuery(
                    "select distinct rm from Register r " +
                            "join r.members rm " +
                            "where r.permanent = true " +
                            "and r.institution.id = :institutionId " +
                            "and rm.deleted = false " +
                            "and rm.external = true " +
                            "and rm.professor.status = :status " +
                            "and rm.id in (:registerIds)", RegisterMember.class)
                    .setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
                    .setParameter("status", Role.RoleStatus.ACTIVE)
                    .setParameter("registerIds", newEvaluatorsRegisterMap.keySet());
            newRegisterMembers.addAll(query.getResultList());
        }
        if (newEvaluatorsRegisterMap.keySet().size() != newRegisterMembers.size()) {
            throw new NotFoundException("wrong.register.member.id");
        }

        // Check if a member is Candidacy Evaluator
        for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
            for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
                if (newEvaluatorsRegisterMap.containsKey(evaluator.getRegisterMember().getId())) {
                    throw new ValidationException("member.is.candidacy.evaluator");
                }
            }
        }

        Set<Long> addedEvaluatorIds = new HashSet<Long>();

        // Update
        existingEvaluation.copyFrom(newEvaluation);
        existingEvaluation.getEvaluators().clear();
        for (RegisterMember newRegisterMember : newRegisterMembers) {
            PositionEvaluator newEvaluator = null;
            if (existingEvaluatorsRegisterMap.containsKey(newRegisterMember.getId())) {
                newEvaluator = existingEvaluatorsRegisterMap.get(newRegisterMember.getId());
            } else {
                newEvaluator = new PositionEvaluator();
                newEvaluator.setRegisterMember(newRegisterMember);
                newEvaluator.setPosition(newEvaluatorsRegisterMap.get(newRegisterMember.getId()));

                addedEvaluatorIds.add(newRegisterMember.getId());
            }
            existingEvaluation.addEvaluator(newEvaluator);
        }

        em.flush();

        // Send E-Mails
        for (final PositionEvaluator evaluator : existingEvaluation.getEvaluators()) {
            if (addedEvaluatorIds.contains(evaluator.getRegisterMember().getId())) {
                // positionEvaluation.create.evaluator@evaluator
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionEvaluation.create.evaluator@evaluator",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
                                put("position", existingEvaluation.getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

                                put("assistants_el", extractAssistantsInfo(existingEvaluation.getPosition(), "el"));
                                put("assistants_en", extractAssistantsInfo(existingEvaluation.getPosition(), "en"));

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
        }
        boolean removed = false;
        for (Long id : existingEvaluatorsRegisterMap.keySet()) {
            if (!newEvaluatorsRegisterMap.keySet().contains(id)) {
                removed = true;
                final PositionEvaluator removedEvaluator = existingEvaluatorsRegisterMap.get(id);
                // positionEvaluation.remove.evaluator@evaluator
                mailService.postEmail(removedEvaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionEvaluation.remove.evaluator@evaluator",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
                                put("position", existingEvaluation.getPosition().getName());

                                put("firstname_el", removedEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", removedEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", removedEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", removedEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));
                            }
                        }));
            }
        }

        if (!addedEvaluatorIds.isEmpty() || removed) {
            // positionEvaluation.update@committee
            for (final PositionCommitteeMember member : existingEvaluation.getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionEvaluation.update@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
                                put("position", existingEvaluation.getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionEvaluation.update@candidates
            for (final Candidacy candidacy : existingEvaluation.getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionEvaluation.update@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
                                    put("position", existingEvaluation.getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
        }
        // End: Send E-Mails

        // Return result
        existingEvaluation.canUpdateEvaluators();
        existingEvaluation.canUploadEvaluations();

        return existingEvaluation;
    }

    public Collection<PositionEvaluatorFile> getFiles(Long positionId, Long evaluationId, Long evaluatorId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
        return files;
    }

    public FileHeader getFile(Long positionId, Long evaluationId, Long evaluatorId, Long fileId, User loggedOn) throws NotEnabledException, NotFoundException {
        // find evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
        for (PositionEvaluatorFile file : files) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        return null;
    }

    public FileBody getFileBody(Long positionId, Long evaluationId, Long evaluatorId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
        for (PositionEvaluatorFile file : files) {
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

    public FileHeader createFile(Long positionId, Long evaluationId, Long evaluatorId, HttpServletRequest request, User loggedOn) throws Exception {
        // get evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
            throw new NotEnabledException("wrong.position.evaluation.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }
        if (!existingEvaluation.canUploadEvaluations()) {
            throw new ValidationException("committee.missing.aitima.epitropis.pros.aksiologites");
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

        // Check number of file types
        Set<PositionEvaluatorFile> eFiles = FileHeader.filterIncludingDeleted(existingEvaluator.getFiles(), type);
        PositionEvaluatorFile existingFile2 = checkNumberOfFileTypes(PositionEvaluatorFile.fileTypes, type, eFiles);
        if (existingFile2 != null) {
            return _updateFile(loggedOn, fileItems, existingFile2);
        }

        // Create
        final PositionEvaluatorFile eFile = new PositionEvaluatorFile();
        eFile.setEvaluator(existingEvaluator);
        eFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, eFile);
        existingEvaluator.addFile(eFile);
        em.flush();

        // Send E-Mails
        if (eFile.getType().equals(FileType.AKSIOLOGISI)) {
            // positionEvaluation.upload.aksiologisi@committee
            for (final PositionCommitteeMember member : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionEvaluation.upload.aksiologisi@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
                                put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionEvaluation.upload.aksiologisi@evaluators
            for (final PositionEvaluator evaluator : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionEvaluation.upload.aksiologisi@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
                                put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        } else {
            // position.upload@committee
            for (final PositionCommitteeMember member : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
                                put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // position.upload@evaluators
            for (final PositionEvaluator evaluator : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
                                put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        }

        return eFile;
    }

    public FileHeader updateFile(Long positionId, Long evaluationId, Long evaluatorId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        // get existing evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
            throw new NotEnabledException("wrong.position.evaluation.phase");
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
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }

        if (!PositionEvaluatorFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }
        PositionEvaluatorFile evaluationFile = null;
        for (PositionEvaluatorFile file : existingEvaluator.getFiles()) {
            if (file.getId().equals(fileId)) {
                evaluationFile = file;
                break;
            }
        }
        if (evaluationFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Update
        return _updateFile(loggedOn, fileItems, evaluationFile);
    }

    public void deleteFile(Long positionId, Long evaluationId, Long evaluatorId, Long fileId, User loggedOn) throws Exception {
        // find existing evaluator
        PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
        if (existingEvaluator == null) {
            throw new NotFoundException("wrong.position.evaluator.id");
        }
        PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
        if (!existingEvaluation.getId().equals(evaluationId)) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
            throw new NotEnabledException("wrong.position.evaluation.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI)) {
            throw new ValidationException("wrong.position.status");
        }

        PositionEvaluatorFile evaluationFile = null;
        for (PositionEvaluatorFile file : existingEvaluator.getFiles()) {
            if (file.getId().equals(fileId)) {
                evaluationFile = file;
                break;
            }
        }
        if (evaluationFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        PositionEvaluatorFile ef = deleteAsMuchAsPossible(evaluationFile);
        if (ef == null) {
            existingEvaluator.getFiles().remove(evaluationFile);
        }
    }

    public Collection<Register> getPositionEvaluationRegisters(Long positionId, Long evaluationId, User loggedOn) throws NotFoundException, NotEnabledException {

        PositionEvaluation existingEvaluation = getById(evaluationId);
        // get position
        Position existingPosition = existingEvaluation.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
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

    public SearchData<RegisterMember> getPositionEvaluationRegisterMembers(Long positionId, Long evaluationId, Long registerId, HttpServletRequest request, User loggedOn) throws Exception {

        PositionEvaluation existingEvaluation = getById(evaluationId);

        if (existingEvaluation == null) {
            throw new NotFoundException("wrong.position.evaluation.id");
        }
        Position existingPosition = existingEvaluation.getPosition();

        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }

        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
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

        String excludeInternal = request.getParameter("external");

        StringBuilder searchQueryString = new StringBuilder();

        searchQueryString.append("from RegisterMember m " +
                "join m.professor prof " +
                "where m.register.permanent = true " +
                "and m.deleted = false " +
                "and m.register.institution.id = :institutionId " +
                "and m.register.id = :registerId " +
                "and m.professor.status = :status ");

        // if not null, retrieve only external members
        if (excludeInternal != null) {
            searchQueryString.append("and m.external = true ");
        }

        if (StringUtils.isNotEmpty(filterText)) {
            searchQueryString.append(" and ( UPPER(m.professor.user.basicInfo.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfo.firstname) like :filterText ");
            searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.firstname) like :filterText ");
            searchQueryString.append(" or CAST(m.professor.user.id AS text) like :filterText ");
            searchQueryString.append(" or (" +
                    "	exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		join pd.department.name dname " +
                    "		join pd.department.school.name sname " +
                    "		join pd.department.school.institution.name iname " +
                    "		where pd.id = prof.id " +
                    "		and ( dname like :filterText " +
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

        // Prepare Query
        List<RegisterMember> registerMembers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        // Execute
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

}
