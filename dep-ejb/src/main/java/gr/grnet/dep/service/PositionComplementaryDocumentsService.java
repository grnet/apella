package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class PositionComplementaryDocumentsService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private MailService mailService;

    private PositionComplementaryDocuments getById(Long cdId) throws NotFoundException {
        PositionComplementaryDocuments documents = em.find(PositionComplementaryDocuments.class, cdId);

        if (documents == null) {
            throw new NotFoundException("wrong.position.complentaryDocuments.id");
        }

        return documents;
    }


    public PositionComplementaryDocuments get(Long positionId, Long cdId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get
        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        return existingComDocs;
    }

    public Collection<ComplementaryDocumentsFile> getFiles(Long positionId, Long cdId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get
        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingPosition.getPhase().getComplementaryDocuments().getFiles());
        return files;
    }

    public ComplementaryDocumentsFile getFile(Long positionId, Long cdId, Long fileId, User loggedOn) throws NotFoundException, NotEnabledException {
        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingComDocs.getFiles());
        for (ComplementaryDocumentsFile file : files) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        return null;
    }


    public FileBody getFileBody(Long positionId, Long cdId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {

        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
                !(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingComDocs.getFiles());
        for (ComplementaryDocumentsFile file : files) {
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

    public ComplementaryDocumentsFile createFile(Long positionId, Long cdId, HttpServletRequest request, User loggedOn) throws Exception {

        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
            throw new NotEnabledException("wrong.position.complentaryDocuments.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (existingPosition.getPhase().getStatus().equals(Position.PositionStatus.CANCELLED)) {
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

        // Check number of file types
        Set<ComplementaryDocumentsFile> cdFiles = FileHeader.filterIncludingDeleted(existingComDocs.getFiles(), type);
        ComplementaryDocumentsFile existingFile3 = checkNumberOfFileTypes(ComplementaryDocumentsFile.fileTypes, type, cdFiles);
        if (existingFile3 != null) {
            return _updateFile(loggedOn, fileItems, existingFile3);
        }

        // Create
        final ComplementaryDocumentsFile cdFile = new ComplementaryDocumentsFile();
        cdFile.setComplementaryDocuments(existingComDocs);
        cdFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, cdFile);
        existingComDocs.addFile(cdFile);
        em.flush();

        // Send E-Mails
        // position.upload@committee
        if (cdFile.getComplementaryDocuments().getPosition().getPhase().getCommittee() != null) {
            for (final PositionCommitteeMember member : cdFile.getComplementaryDocuments().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(cdFile.getComplementaryDocuments().getPosition().getId()));
                                put("position", cdFile.getComplementaryDocuments().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
        }
        // position.upload@candidates
        for (final Candidacy candidacy : cdFile.getComplementaryDocuments().getPosition().getPhase().getCandidacies().getCandidacies()) {
            if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@candidates",
                        Collections.unmodifiableMap(new HashMap<String, String>() {
                            {
                                put("positionID", StringUtil.formatPositionID(cdFile.getComplementaryDocuments().getPosition().getId()));
                                put("position", cdFile.getComplementaryDocuments().getPosition().getName());

                                put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                put("institution_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                put("institution_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                            }
                        }));
            }
        }
        // position.upload@evaluators
        if (cdFile.getComplementaryDocuments().getPosition().getPhase().getEvaluation() != null) {
            for (final PositionEvaluator evaluator : cdFile.getComplementaryDocuments().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(cdFile.getComplementaryDocuments().getPosition().getId()));
                                put("position", cdFile.getComplementaryDocuments().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        }
        // End: Send E-Mails

        return cdFile;
    }

    public ComplementaryDocumentsFile updateFile(Long positionId, Long cdId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
            throw new NotEnabledException("wrong.position.complentaryDocuments.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (existingPosition.getPhase().getStatus().equals(Position.PositionStatus.CANCELLED)) {
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
        if (!ComplementaryDocumentsFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }
        ComplementaryDocumentsFile complementaryDocumentsFile = null;
        for (ComplementaryDocumentsFile file : existingComDocs.getFiles()) {
            if (file.getId().equals(fileId)) {
                complementaryDocumentsFile = file;
                break;
            }
        }
        if (complementaryDocumentsFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Update
        return _updateFile(loggedOn, fileItems, complementaryDocumentsFile);
    }

    public void deleteFile(Long cdId, Long positionId, Long fileId, User loggedOn) throws Exception {
        PositionComplementaryDocuments existingComDocs = getById(cdId);
        // get position
        Position existingPosition = existingComDocs.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
            throw new NotEnabledException("wrong.position.complentaryDocuments.phase");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (existingPosition.getPhase().getStatus().equals(Position.PositionStatus.CANCELLED)) {
            throw new ValidationException("wrong.position.status");
        }

        ComplementaryDocumentsFile complementaryDocumentsFile = null;
        for (ComplementaryDocumentsFile file : existingComDocs.getFiles()) {
            if (file.getId().equals(fileId)) {
                complementaryDocumentsFile = file;
                break;
            }
        }
        if (complementaryDocumentsFile == null) {
            throw new ValidationException("wrong.file.id");
        }
        ComplementaryDocumentsFile cdf = deleteAsMuchAsPossible(complementaryDocumentsFile);
        if (cdf == null) {
            existingComDocs.getFiles().remove(complementaryDocumentsFile);
        }
    }


}

