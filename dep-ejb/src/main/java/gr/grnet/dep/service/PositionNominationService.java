package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionNominationFile;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.DateUtil;
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
public class PositionNominationService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private MailService mailService;

    private PositionNomination getById(Long positionNominationId) throws NotFoundException {
        PositionNomination positionNomination = em.find(PositionNomination.class, positionNominationId);

        if (positionNomination == null) {
            throw new NotFoundException("wrong.position.nomination.id");
        }
        return positionNomination;
    }

    public PositionNomination get(Long positionId, Long nominationId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get existing nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position existingPosition = existingNomination.getPosition();
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
        return existingNomination;
    }

    public PositionNomination update(Long positionId, Long nominationId, PositionNomination newNomination, User loggedOn) throws NotFoundException, NotEnabledException, ValidationException {

        final PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
        if (existingNomination == null) {
            throw new NotFoundException("wrong.position.nomination.id");
        }
        Position existingPosition = existingNomination.getPosition();
        if (!existingPosition.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!existingPosition.getPhase().getNomination().getId().equals(existingNomination.getId())) {
            throw new NotEnabledException("wrong.position.nomination.phase");
        }
        if (!existingPosition.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI) &&
                !existingPosition.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
            throw new ValidationException("wrong.position.status");
        }

        boolean updatedNominationCommitteeConvergenceDate = newNomination.getNominationCommitteeConvergenceDate() != null &&
                DateUtil.compareDates(existingNomination.getNominationCommitteeConvergenceDate(), newNomination.getNominationCommitteeConvergenceDate()) != 0;
        boolean updatedNominatedCandidacy = (newNomination.getNominatedCandidacy() != null && newNomination.getNominatedCandidacy().getId() != null) && (existingNomination.getNominatedCandidacy() == null || !existingNomination.getNominatedCandidacy().getId().equals(newNomination.getNominatedCandidacy().getId()));
        boolean updatedSecondNominatedCandidacy = (newNomination.getSecondNominatedCandidacy() != null && newNomination.getSecondNominatedCandidacy().getId() != null) && (existingNomination.getSecondNominatedCandidacy() == null || !existingNomination.getSecondNominatedCandidacy().getId().equals(newNomination.getSecondNominatedCandidacy().getId()));

        // Validation Rules
        // 5) Να μην είναι δυνατή η συμπλήρωση του πεδίου "Ημερομηνία Σύγκλησης Επιτροπής για Επιλογή" εάν δεν είναι συμπληρωμένο
        // το πεδίο "Πρόσκληση Κοσμήτορα για Σύγκληση της Επιτροπής για επιλογή"
        if (newNomination.getNominationCommitteeConvergenceDate() != null &&
                FileHeader.filter(existingNomination.getFiles(), FileType.PROSKLISI_KOSMITORA).isEmpty()) {
            throw new ValidationException("nomination.missing.prosklisi.kosmitora");
        }
        // 7) Να μην είναι δυνατή η συμπλήρωση των πεδίων "Διαβιβαστικό Πρακτικού, "Εκλεγείς", "Δεύτερος Καταλληλότερος Υποψήφιος", εάν δεν έχει αναρτηθεί το Πρακτικό Επιλογής.
        if ((newNomination.getNominatedCandidacy().getId() != null || newNomination.getSecondNominatedCandidacy().getId() != null)
                && FileHeader.filter(existingNomination.getFiles(), FileType.PRAKTIKO_EPILOGIS).isEmpty()) {
            throw new ValidationException("nomination.missing.praktiko.epilogis");
        }
        // 8) Τα πεδία "Πράξη Διορισμού" και "ΦΕΚ Πράξης Διορισμού" να ενεργοποιούνται μόνο μετά τη μετάβαση της θέσης σε status "Στελεχωμένη".
        if (newNomination.getNominationFEK() != null && !newNomination.getNominationFEK().isEmpty()
                && !existingPosition.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
            throw new ValidationException("wrong.position.status");
        }

        //Update
        existingNomination.copyFrom(newNomination);
        // Add Nominated
        if (newNomination.getNominatedCandidacy() != null && newNomination.getNominatedCandidacy().getId() != null) {
            for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
                if (candidacy.getId().equals(newNomination.getNominatedCandidacy().getId())) {
                    existingPosition.getPhase().getNomination().setNominatedCandidacy(candidacy);
                    break;
                }
            }
        } else {
            existingNomination.setNominatedCandidacy(null);
        }
        // Add Second Nominated
        if (newNomination.getSecondNominatedCandidacy() != null && newNomination.getSecondNominatedCandidacy().getId() != null) {
            for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
                if (candidacy.getId().equals(newNomination.getSecondNominatedCandidacy().getId())) {
                    existingPosition.getPhase().getNomination().setSecondNominatedCandidacy(candidacy);
                    break;
                }
            }
        } else {
            existingNomination.setSecondNominatedCandidacy(null);
        }
        em.flush();

        // Send E-Mails
        if (updatedNominationCommitteeConvergenceDate) {
            // positionNomination.update.nominationCommitteeConvergenceDate@committee
            for (final PositionCommitteeMember member : existingNomination.getPosition().getPhase().getEvaluation().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionNomination.update.nominationCommitteeConvergenceDate@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingNomination.getPosition().getPhase().getPosition().getId()));
                                put("position", existingNomination.getPosition().getPhase().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionNomination.update.nominationCommitteeConvergenceDate@candidates
            for (final Candidacy candidacy : existingNomination.getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionNomination.update.nominationCommitteeConvergenceDate@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(existingNomination.getPosition().getId()));
                                    put("position", existingNomination.getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
            // positionNomination.update.nominationCommitteeConvergenceDate@evaluators
            for (final PositionEvaluator evaluator : existingNomination.getPosition().getPhase().getEvaluation().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionNomination.update.nominationCommitteeConvergenceDate@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(existingNomination.getPosition().getId()));
                                put("position", existingNomination.getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }

        }
        if (updatedNominatedCandidacy) {
            // positionNomination.update.nominated@candidate
            mailService.postEmail(existingNomination.getNominatedCandidacy().getCandidate().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "positionNomination.update.nominated@candidate",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("positionID", StringUtil.formatPositionID(existingNomination.getPosition().getId()));
                            put("position", existingNomination.getPosition().getName());

                            put("firstname_el", existingNomination.getNominatedCandidacy().getCandidate().getUser().getFirstname("el"));
                            put("lastname_el", existingNomination.getNominatedCandidacy().getCandidate().getUser().getLastname("el"));
                            put("institution_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("el"));

                            put("firstname_en", existingNomination.getNominatedCandidacy().getCandidate().getUser().getFirstname("en"));
                            put("lastname_en", existingNomination.getNominatedCandidacy().getCandidate().getUser().getLastname("en"));
                            put("institution_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("en"));
                        }
                    }));
        }
        if (updatedSecondNominatedCandidacy) {
            // positionNomination.update.secondNominated@candidate
            mailService.postEmail(existingNomination.getSecondNominatedCandidacy().getCandidate().getUser().getContactInfo().getEmail(),
                    "default.subject",
                    "positionNomination.update.secondNominated@candidate",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("positionID", StringUtil.formatPositionID(existingNomination.getPosition().getId()));
                            put("position", existingNomination.getPosition().getName());

                            put("firstname_el", existingNomination.getSecondNominatedCandidacy().getCandidate().getUser().getFirstname("el"));
                            put("lastname_el", existingNomination.getSecondNominatedCandidacy().getCandidate().getUser().getLastname("el"));
                            put("institution_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                            put("school_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("el"));
                            put("department_el", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("el"));

                            put("firstname_en", existingNomination.getSecondNominatedCandidacy().getCandidate().getUser().getFirstname("en"));
                            put("lastname_en", existingNomination.getSecondNominatedCandidacy().getCandidate().getUser().getLastname("en"));
                            put("institution_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                            put("school_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getSchool().getName().get("en"));
                            put("department_en", existingNomination.getPosition().getPhase().getPosition().getDepartment().getName().get("en"));
                        }
                    }));
        }

        // End: Send E-Mails

        return existingNomination;
    }

    public Collection<PositionNominationFile> getFiles(Long positionId, Long nominationId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        if (existingNomination == null) {
            throw new NotFoundException("wrong.position.nomination.id");
        }
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
                !(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(position.getPhase().getEvaluation() != null && position.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        return FileHeader.filterDeleted(existingNomination.getFiles());
    }

    public PositionNominationFile getFile(Long positionId, Long nominationId, Long fileId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
                !(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(position.getPhase().getEvaluation() != null && position.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<PositionNominationFile> files = FileHeader.filterDeleted(position.getPhase().getNomination().getFiles());
        for (PositionNominationFile file : files) {
            if (file.getId().equals(fileId)) {
                return file;
            }
        }
        return null;
    }

    public FileBody getFileBody(Long positionId, Long nominationId, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
                !(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
                !(position.getPhase().getEvaluation() != null && position.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
                !position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        Collection<PositionNominationFile> files = FileHeader.filterDeleted(existingNomination.getFiles());
        for (PositionNominationFile file : files) {
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

    public FileHeader createFile(Long positionId, Long nominationId, HttpServletRequest request, User loggedOn) throws Exception {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
            throw new NotEnabledException("not.current.position.phase");
        }
        // Validate:
        if (!position.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI) &&
                !position.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
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

        // 6) Να μην είναι δυνατή η ανάρτηση του Πρακτικού Επιλογής αν δεν είναι συμπληρωμένο το πεδίο ""Ημερομηνία Σύγκλησης Επιτροπής για Επιλογή"
        if ((type.equals(FileType.PRAKTIKO_EPILOGIS) || type.equals(FileType.DIAVIVASTIKO_PRAKTIKOU))
                && existingNomination.getNominationCommitteeConvergenceDate() == null) {
            throw new ValidationException("nomination.missing.committee.convergence.date");
        }
        // 8) Τα πεδία "Πράξη Διορισμού" και "ΦΕΚ Πράξης Διορισμού" να ενεργοποιούνται μόνο μετά τη μετάβαση της θέσης σε status "Στελεχωμένη".
        if (type.equals(FileType.PRAKSI_DIORISMOU) && !position.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
            throw new ValidationException("wrong.position.status");
        }


        Set<PositionNominationFile> nominationFiles = FileHeader.filterIncludingDeleted(position.getPhase().getNomination().getFiles(), type);
        PositionNominationFile existingFile4 = checkNumberOfFileTypes(PositionNominationFile.fileTypes, type, nominationFiles);
        if (existingFile4 != null) {
            return _updateFile(loggedOn, fileItems, existingFile4);
        }

        // Create
        final PositionNominationFile nominationFile = new PositionNominationFile();
        nominationFile.setNomination(position.getPhase().getNomination());
        nominationFile.setOwner(loggedOn);
        saveFile(loggedOn, fileItems, nominationFile);
        position.getPhase().getNomination().addFile(nominationFile);
        em.flush();

        // Send E-Mails
        if (nominationFile.getType().equals(FileType.PRAKTIKO_EPILOGIS)) {
            // positionNomination.upload.praktikoEpilogis@committee
            for (final PositionCommitteeMember member : nominationFile.getNomination().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionNomination.upload.praktikoEpilogis@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                put("position", nominationFile.getNomination().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // positionNomination.upload.praktikoEpilogis@candidates
            for (final Candidacy candidacy : nominationFile.getNomination().getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "positionNomination.upload.praktikoEpilogis@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                    put("position", nominationFile.getNomination().getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
            // positionNomination.upload.praktikoEpilogis@evaluators
            for (final PositionEvaluator evaluator : nominationFile.getNomination().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "positionNomination.upload.praktikoEpilogis@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                put("position", nominationFile.getNomination().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        } else {
            // position.upload@committee
            for (final PositionCommitteeMember member : nominationFile.getNomination().getPosition().getPhase().getCommittee().getMembers()) {
                mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@committee",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                put("position", nominationFile.getNomination().getPosition().getName());

                                put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
                            }
                        }));
            }
            // position.upload@candidates
            for (final Candidacy candidacy : nominationFile.getNomination().getPosition().getPhase().getCandidacies().getCandidacies()) {
                if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
                    mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
                            "default.subject",
                            "position.upload@candidates",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                    put("position", nominationFile.getNomination().getPosition().getName());

                                    put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
                                    put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
                                    put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                    put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                    put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                    put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
                                    put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
                                    put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                    put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                    put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                    put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
                                }
                            }));
                }
            }
            // position.upload@evaluators
            for (final PositionEvaluator evaluator : nominationFile.getNomination().getPosition().getPhase().getEvaluation().getEvaluators()) {
                mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "position.upload@evaluators",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("positionID", StringUtil.formatPositionID(nominationFile.getNomination().getPosition().getId()));
                                put("position", nominationFile.getNomination().getPosition().getName());

                                put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
                                put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
                                put("institution_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
                                put("school_el", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("el"));
                                put("department_el", nominationFile.getNomination().getPosition().getDepartment().getName().get("el"));

                                put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
                                put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
                                put("institution_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
                                put("school_en", nominationFile.getNomination().getPosition().getDepartment().getSchool().getName().get("en"));
                                put("department_en", nominationFile.getNomination().getPosition().getDepartment().getName().get("en"));

                                put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
                            }
                        }));
            }
        }

        return nominationFile;
    }

    public FileHeader updateFile(Long positionId, Long nominationId, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
            throw new NotEnabledException("not.current.position.phase");
        }
        // Validate:
        if (!position.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI) &&
                !position.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
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
        if (!PositionNominationFile.fileTypes.containsKey(type)) {
            throw new ValidationException("wrong.file.type");
        }

        PositionNominationFile nominationFile = null;
        for (PositionNominationFile file : position.getPhase().getNomination().getFiles()) {
            if (file.getId().equals(fileId)) {
                nominationFile = file;
                break;
            }
        }
        if (nominationFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        // Update
        return _updateFile(loggedOn, fileItems, nominationFile);
    }

    public void deleteFile(Long positionId, Long nominationId, Long fileId, User loggedOn) throws NotFoundException, NotEnabledException, ValidationException {
        // get nomination
        PositionNomination existingNomination = getById(nominationId);
        // get position
        Position position = existingNomination.getPosition();
        if (!position.getId().equals(positionId)) {
            throw new NotFoundException("wrong.position.id");
        }
        if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
            throw new NotEnabledException("not.current.position.phase");
        }
        // Validate:
        if (!position.isUserAllowedToEdit(loggedOn)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!position.getPhase().getStatus().equals(Position.PositionStatus.EPILOGI) &&
                !position.getPhase().getStatus().equals(Position.PositionStatus.STELEXOMENI)) {
            throw new ValidationException("wrong.position.status");
        }

        PositionNominationFile nominationFile = null;
        for (PositionNominationFile file : position.getPhase().getNomination().getFiles()) {
            if (file.getId().equals(fileId)) {
                nominationFile = file;
                break;
            }
        }
        if (nominationFile == null) {
            throw new NotFoundException("wrong.file.id");
        }
        PositionNominationFile pnf = deleteAsMuchAsPossible(nominationFile);
        if (pnf == null) {
            position.getPhase().getNomination().getFiles().remove(nominationFile);
        }
    }

}
