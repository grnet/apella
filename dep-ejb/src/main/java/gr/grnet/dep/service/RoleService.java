package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.file.*;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.PdfUtil;
import org.apache.commons.fileupload.FileItem;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class RoleService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private UtilityService utilityService;

    @EJB
    private JiraService jiraService;

    @EJB
    private MailService mailService;

    private static final Set<RolePair> forbiddenPairs;

    static {
        Set<RolePair> aSet = new HashSet<>();
        // ADMINISTRATOR
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.CANDIDATE));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.PROFESSOR_DOMESTIC));
        aSet.add(new RolePair(Role.RoleDiscriminator.ADMINISTRATOR, Role.RoleDiscriminator.PROFESSOR_FOREIGN));
        // CANDIDATE
        aSet.add(new RolePair(Role.RoleDiscriminator.CANDIDATE, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.CANDIDATE, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.CANDIDATE, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.CANDIDATE, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        // INSTITUTION_MANAGER
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.CANDIDATE));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.PROFESSOR_DOMESTIC));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.PROFESSOR_FOREIGN));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_MANAGER, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        // INSTITUTION_ASSISTANT
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.CANDIDATE));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.PROFESSOR_DOMESTIC));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.PROFESSOR_FOREIGN));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.INSTITUTION_ASSISTANT, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        // MINISTRY_MANAGER
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.CANDIDATE));
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.PROFESSOR_DOMESTIC));
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.PROFESSOR_FOREIGN));
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.MINISTRY_MANAGER, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        // PROFESSOR_DOMESTIC
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_DOMESTIC, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_DOMESTIC, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_DOMESTIC, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_DOMESTIC, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_DOMESTIC, Role.RoleDiscriminator.PROFESSOR_FOREIGN));
        // PROFESSOR_FOREIGN
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_FOREIGN, Role.RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_FOREIGN, Role.RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_FOREIGN, Role.RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_FOREIGN, Role.RoleDiscriminator.MINISTRY_ASSISTANT));
        aSet.add(new RolePair(Role.RoleDiscriminator.PROFESSOR_FOREIGN, Role.RoleDiscriminator.PROFESSOR_DOMESTIC));

        forbiddenPairs = Collections.unmodifiableSet(aSet);
    }

    public enum DocumentDiscriminator {
        InstitutionManagerCertificationDean,
        InstitutionManagerCertificationPresident,
        CandidateRegistrationForm
    }

    public List<Role> getAll(Long userID, String discriminators, String statuses, User loggedOn) throws NotEnabledException, ValidationException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.getId().equals(userID)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Prepare Query
        StringBuilder sb = new StringBuilder();
        sb.append("select r from Role r " +
                "where r.status in (:statuses) ");
        if (userID != null) {
            sb.append("and r.user.id = :userID ");
        }
        if (discriminators != null) {
            sb.append("and r.discriminator in (:discriminators) ");
        }
        // Set Parameters
        TypedQuery<Role> query = em.createQuery(sb.toString(), Role.class);
        Set<Role.RoleStatus> statusesList = new HashSet<>();
        if (statuses != null) {
            try {
                for (String status : statuses.split(",")) {
                    if (status.equalsIgnoreCase("ALL")) {
                        for (Role.RoleStatus st : Role.RoleStatus.values()) {
                            statusesList.add(st);
                        }
                    } else {
                        statusesList.add(Role.RoleStatus.valueOf(status));
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new ValidationException("bad.request");
            }
        } else {
            //Default if no parameter given:
            statusesList.add(Role.RoleStatus.UNAPPROVED);
            statusesList.add(Role.RoleStatus.ACTIVE);
        }
        query.setParameter("statuses", statusesList);
        if (userID != null) {
            query = query.setParameter("userID", userID);
        }
        if (discriminators != null) {
            try {
                List<Role.RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
                for (String discriminator : discriminators.split(",")) {
                    discriminatorList.add(Role.RoleDiscriminator.valueOf(discriminator));
                }
                query = query.setParameter("discriminators", discriminatorList);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("illegal.argument.exception");
            }
        }

        // Execute
        List<Role> roles = query.getResultList();
        for (Role r : roles) {
            r.getUser().getPrimaryRole();
        }
        return roles;
    }

    public Role get(Long id) throws NotFoundException {
        Role role = em.find(Role.class, id);

        if (role == null) {
            throw new NotFoundException("wrong.role.id");
        }

        return role;
    }

    public Role get(Long id, User loggedOn) throws NotEnabledException {
        boolean roleBelongsToUser = false;
        for (Role r : loggedOn.getRoles()) {
            if (r.getId() == id) {
                roleBelongsToUser = true;
                break;
            }
        }
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !roleBelongsToUser) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Role r = em.createQuery(
                "from Role r where r.id=:id", Role.class)
                .setParameter("id", id)
                .getSingleResult();

        r.getUser().getPrimaryRole();

        return r;
    }

    public Role create(Role newRole, User loggedOn) throws NotEnabledException, NotFoundException, ValidationException {
        // Validate
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && !newRole.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        User existingUser = em.find(User.class, newRole.getUser().getId());
        if (existingUser == null) {
            throw new NotFoundException("wrong.id");
        }
        if (isIncompatibleRole(newRole, existingUser.getRoles())) {
            throw new ValidationException("incompatible.role");
        }

        // Update

        // Find if the same role already exists and is active.
        // If it does, it should be made inactive.
        TypedQuery<Role> query = em.createQuery("select r from Role r " +
                "where r.status = :status " +
                "and r.user.id = :userID " +
                "and r.discriminator = :discriminator", Role.class);
        query.setParameter("status", Role.RoleStatus.ACTIVE);
        query.setParameter("userID", newRole.getUser().getId());
        query.setParameter("discriminator", newRole.getDiscriminator());
        List<Role> existingRoles = query.getResultList();
        for (Role existingRole : existingRoles) {
            existingRole.setStatus(Role.RoleStatus.INACTIVE);
            existingRole.setStatusEndDate(new Date());
        }

        newRole.setStatusDate(new Date());
        newRole.setId(null);
        newRole = em.merge(newRole);
        em.flush();

        newRole.getUser().getPrimaryRole();

        return newRole;
    }

    public Role update(Long id, Boolean updateCandidacies, Role role, User loggedOn) throws NotFoundException, NotEnabledException, ValidationException, ServiceException {
        // get role
        Role existingRole = get(id);
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !existingRole.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Validate data if loggedOn is not admin we trust admin(!!!)
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            if (existingRole.getUser().isMissingRequiredFields()) {
                throw new ValidationException("user.missing.required.fields");
            }
            if (!existingRole.getStatus().equals(Role.RoleStatus.UNAPPROVED) &&
                    !existingRole.compareCriticalFields(role)) {
                throw new ValidationException("cannot.change.critical.fields");
            }

            Long managerInstitutionId;
            Long institutionId;
            boolean missingProfile;
            switch (existingRole.getDiscriminator()) {
                case INSTITUTION_ASSISTANT:
                    managerInstitutionId = ((InstitutionAssistant) existingRole).getManager().getInstitution().getId();
                    institutionId = ((InstitutionAssistant) role).getInstitution().getId();
                    if (!managerInstitutionId.equals(institutionId)) {
                        throw new ValidationException("manager.institution.mismatch");
                    }
                    break;
                case PROFESSOR_DOMESTIC:
                    ProfessorDomestic newProfessorDomestic = (ProfessorDomestic) role;
                    ProfessorDomestic existingProfessorDomestic = (ProfessorDomestic) existingRole;
                    missingProfile = newProfessorDomestic.getProfileURL() == null || newProfessorDomestic.getProfileURL().isEmpty();
                    missingProfile = missingProfile && FileHeader.filter(existingProfessorDomestic.getFiles(), FileType.PROFILE).isEmpty();
                    if (missingProfile) {
                        throw new ValidationException("professor.missing.profile");
                    }
                    if (!newProfessorDomestic.getHasAcceptedTerms()) {
                        throw new ValidationException("professor.has.not.accepted.terms");
                    }
                    break;
                case PROFESSOR_FOREIGN:
                    ProfessorForeign newProfessorForeign = (ProfessorForeign) role;
                    ProfessorForeign existingProfessorForeign = (ProfessorForeign) existingRole;
                    missingProfile = newProfessorForeign.getProfileURL() == null || newProfessorForeign.getProfileURL().isEmpty();
                    missingProfile = missingProfile && FileHeader.filter(existingProfessorForeign.getFiles(), FileType.PROFILE).isEmpty();
                    if (missingProfile) {
                        throw new ValidationException("professor.missing.profile");
                    }
                    if (!newProfessorForeign.getHasAcceptedTerms()) {
                        throw new ValidationException("professor.has.not.accepted.terms");
                    }
                    break;
                default:
                    break;
            }
        }
        // Update

        boolean institutionHasChanged = false;
        if (existingRole.getDiscriminator() == Role.RoleDiscriminator.PROFESSOR_DOMESTIC) {
            institutionHasChanged = ((ProfessorDomestic) existingRole).getInstitution().getId().compareTo(((ProfessorDomestic) role).getInstitution().getId()) != 0;
        }
        existingRole = existingRole.copyFrom(role);
        switch (existingRole.getDiscriminator()) {
            case PROFESSOR_DOMESTIC:
                ProfessorDomestic professorDomestic = (ProfessorDomestic) existingRole;
                professorDomestic.setSubject(utilityService.supplementSubject(((ProfessorDomestic) role).getSubject()));
                professorDomestic.setFekSubject(utilityService.supplementSubject(((ProfessorDomestic) role).getFekSubject()));

                if (institutionHasChanged) {
                    for (RegisterMember member : professorDomestic.getRegisterMemberships()) {
                        member.setExternal(professorDomestic.getInstitution().getId().compareTo(member.getRegister().getInstitution().getId()) != 0);
                    }
                }

                // Activate if !misingRequiredFields and is not authenticated by username (needs helpdesk approval)
                if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                        existingRole.getStatus().equals(Role.RoleStatus.UNAPPROVED) &&
                        !existingRole.getUser().getAuthenticationType().equals(AuthenticationType.USERNAME) &&
                        !existingRole.getUser().isMissingRequiredFields() &&
                        !existingRole.isMissingRequiredFields()) {
                    // At this point all fields are filled,
                    // so we activate the Role
                    Role.updateStatus(professorDomestic, Role.RoleStatus.ACTIVE);
                }

                break;
            case PROFESSOR_FOREIGN:
                ProfessorForeign professorForeign = (ProfessorForeign) existingRole;
                professorForeign.setSubject(utilityService.supplementSubject(((ProfessorForeign) role).getSubject()));
                break;
            default:
                break;
        }

        // Check open candidacies
        updateCandidacies = (updateCandidacies == null) ? false : updateCandidacies; // Default to false if null
        if (updateCandidacies && existingRole.getUser().hasActiveRole(Role.RoleDiscriminator.CANDIDATE)) {
            Candidate candidate = (Candidate) existingRole.getUser().getActiveRole(Role.RoleDiscriminator.CANDIDATE);
            updateOpenCandidacies(candidate);
        }

        // Return Result
        em.flush();

        // Post to Jira
        if (!existingRole.isMissingRequiredFields()) {
            String summary = jiraService.getResourceBundleString("user.created.role.summary");
            String description = jiraService.getResourceBundleString("user.created.role.description",
                    "user", existingRole.getUser().getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + existingRole.getUser().getId() + " )");
            JiraIssue issue = JiraIssue.createRegistrationIssue(existingRole.getUser(), summary, description);
            jiraService.queueCreateIssue(issue);
        }

        existingRole.getUser().getPrimaryRole();
        return existingRole;
    }


    public InputStream getDocument(Long id, DocumentDiscriminator fileName) throws NotFoundException, NotEnabledException {
        //find role
        Role role = get(id);

        // Validate:
        switch (fileName) {
            case InstitutionManagerCertificationDean:
            case InstitutionManagerCertificationPresident:
                if (!(role instanceof InstitutionManager)) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                break;
            case CandidateRegistrationForm:
                if (!(role instanceof Candidate)) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                break;
        }
        // Generate Document
        InputStream is = null;
        switch (fileName) {
            case InstitutionManagerCertificationDean:
            case InstitutionManagerCertificationPresident:
                is = PdfUtil.generateFormaAllagisIM((InstitutionManager) role);
                break;
            case CandidateRegistrationForm:
                is = PdfUtil.generateFormaYpopsifiou((Candidate) role);
                break;
        }
        // Return stream
        return is;
    }

    public Set<? extends FileHeader> getFiles(Long id, User loggedOn) throws NotFoundException, NotEnabledException {

        Role role = get(id);
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Set<? extends FileHeader> files;

        // Return Result
        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            files = FileHeader.filterDeleted(candidate.getFiles());
            return files;
        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            files = FileHeader.filterDeleted(professor.getFiles());
            return files;
        }
        // Default Action
        return null;
    }

    public FileHeader getFile(Long id, Long fileId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get the role
        Role role = get(id);

        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            for (CandidateFile cf : candidate.getFiles()) {
                if (cf.getId().equals(fileId) && !cf.isDeleted()) {
                    return cf;
                }
            }
        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            for (ProfessorFile pf : professor.getFiles()) {
                if (pf.getId().equals(fileId) && !pf.isDeleted()) {
                    return pf;
                }
            }
        }

        return null;
    }

    public FileBody getFileBody(Long id, Long fileId, Long bodyId, User loggedOn) throws NotFoundException, NotEnabledException {
        // get role
        Role role = get(id);
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Return Result
        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            for (CandidateFile cf : candidate.getFiles()) {
                if (cf.getId().equals(fileId) && !cf.isDeleted()) {
                    for (FileBody fb : cf.getBodies()) {
                        if (fb.getId().equals(bodyId)) {
                            return fb;
                        }
                    }
                }
            }
        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            for (ProfessorFile pf : professor.getFiles()) {
                if (pf.getId().equals(fileId) && !pf.isDeleted()) {
                    for (FileBody fb : pf.getBodies()) {
                        if (fb.getId().equals(bodyId)) {
                            return fb;
                        }
                    }
                }
            }
        }
        // Default Action
        return null;
    }

    public FileHeader createFile(Long id, HttpServletRequest request, User loggedOn) throws Exception {
        // get role
        Role role = get(id);
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Parse Request
        List<FileItem> fileItems = readMultipartFormData(request);
        // Find required type:
        FileType type = null;
        Boolean updateCandidacies = false;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
            } else if (fileItem.isFormField() && fileItem.getFieldName().equals("updateCandidacies")) {
                updateCandidacies = Boolean.valueOf(fileItem.getString("UTF-8"));
            }
        }

        if (type == null) {
            throw new ValidationException("missing.file.type");
        }
        // Return Result
        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            // Check if file can be changed(active role)
            if (!candidate.getStatus().equals(Role.RoleStatus.UNAPPROVED)) {
                switch (type) {
                    case TAYTOTHTA:
                    case FORMA_SYMMETOXIS:
                    case BEBAIWSH_STRATIOTIKIS_THITIAS:
                        throw new ValidationException("cannot.change.critical.fields");
                    default:
                }
            }
            // Check number of file types
            CandidateFile existingFile = checkNumberOfFileTypes(CandidateFile.fileTypes, type, candidate.getFiles());
            if (existingFile != null) {
                return _updateCandidateFile(loggedOn, fileItems, existingFile, updateCandidacies, candidate);
            }

            //Create File
            CandidateFile candidateFile = new CandidateFile();
            candidateFile.setCandidate(candidate);
            candidateFile.setOwner(candidate.getUser());
            File file = saveFile(loggedOn, fileItems, candidateFile);
            candidate.addFile(candidateFile);

            // Check open candidacies
            if (updateCandidacies) {
                updateOpenCandidacies(candidate);
            }

            em.flush();
            return candidateFile;
        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            // Check number of file types
            ProfessorFile existingFile = checkNumberOfFileTypes(ProfessorFile.fileTypes, type, professor.getFiles());
            if (existingFile != null) {
                return _updateProfessorFile(loggedOn, fileItems, existingFile, updateCandidacies, professor);
            }

            // Create File
            ProfessorFile professorFile = new ProfessorFile();
            professorFile.setProfessor(professor);
            professorFile.setOwner(professor.getUser());
            File file = saveFile(loggedOn, fileItems, professorFile);
            professor.addFile(professorFile);

            // Check open candidacies
            if (updateCandidacies && professor.getUser().hasActiveRole(Role.RoleDiscriminator.CANDIDATE)) {
                Candidate candidate = (Candidate) professor.getUser().getActiveRole(Role.RoleDiscriminator.CANDIDATE);
                updateOpenCandidacies(candidate);
            }
            em.flush();
            return professorFile;
        } else {
            return null;
        }
    }

    public FileHeader updateFile(Long id, Long fileId, HttpServletRequest request, User loggedOn) throws Exception {
        // get role
        Role role = get(id);
        // Validate:
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // Parse Request
        List<FileItem> fileItems = readMultipartFormData(request);
        // Find required type:
        FileType type = null;
        Boolean updateCandidacies = false;
        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
                type = FileType.valueOf(fileItem.getString("UTF-8"));
            } else if (fileItem.isFormField() && fileItem.getFieldName().equals("updateCandidacies")) {
                updateCandidacies = Boolean.valueOf(fileItem.getString("UTF-8"));
            }
        }
        if (type == null) {
            throw new ValidationException("missing.file.type");
        }

        // Return Result
        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            // Check if it exists
            CandidateFile candidateFile = null;
            for (CandidateFile file : candidate.getFiles()) {
                if (file.getId().equals(fileId) && !file.isDeleted()) {
                    candidateFile = file;
                    break;
                }
            }
            if (candidateFile == null) {
                throw new NotFoundException("wrong.file.id");
            }
            // Check if file can be changed(active role)
            if (!candidate.getStatus().equals(Role.RoleStatus.UNAPPROVED)) {
                switch (candidateFile.getType()) {
                    case TAYTOTHTA:
                    case FORMA_SYMMETOXIS:
                    case BEBAIWSH_STRATIOTIKIS_THITIAS:
                        throw new NotEnabledException("cannot.change.critical.fields");
                    default:
                }
            }
            return _updateCandidateFile(loggedOn, fileItems, candidateFile, updateCandidacies, candidate);

        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            ProfessorFile professorFile = null;
            for (ProfessorFile file : professor.getFiles()) {
                if (file.getId().equals(fileId) && !file.isDeleted()) {
                    professorFile = file;
                    break;
                }
            }
            if (professorFile == null) {
                throw new NotFoundException("wrong.file.id");
            }
            return _updateProfessorFile(loggedOn, fileItems, professorFile, updateCandidacies, professor);
        } else {
            throw new ValidationException("wrong.file.type");
        }
    }

    public void deleteFile(Long id, Long fileId, Boolean updateCandidacies, User loggedOn) throws NotFoundException, NotEnabledException, ValidationException {
        // find role
        Role role = get(id);
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (updateCandidacies == null) {
            updateCandidacies = false;
        }

        if (role instanceof Candidate) {
            Candidate candidate = (Candidate) role;
            try {
                CandidateFile candidateFile = em.createQuery(
                        "select cf from CandidateFile cf " +
                                "where cf.candidate.id = :candidateId " +
                                "and cf.id = :fileId " +
                                "and cf.deleted = false", CandidateFile.class)
                        .setParameter("candidateId", id)
                        .setParameter("fileId", fileId)
                        .getSingleResult();
                //Validate delete
                if (!candidate.getStatus().equals(Role.RoleStatus.UNAPPROVED)) {
                    switch (candidateFile.getType()) {
                        case TAYTOTHTA:
                        case FORMA_SYMMETOXIS:
                        case BEBAIWSH_STRATIOTIKIS_THITIAS:
                            throw new NotEnabledException("cannot.change.critical.fields");
                        default:
                    }
                }
                // Do delete
                CandidateFile cf = deleteAsMuchAsPossible(candidateFile);
                if (cf == null) {
                    candidate.getFiles().remove(candidateFile);
                }
                // Check open candidacies
                if (updateCandidacies) {
                    updateOpenCandidacies(candidate);
                }
                return;
            } catch (NoResultException e) {
                throw new NotFoundException("wrong.file.id");
            }

        } else if (role instanceof Professor) {
            Professor professor = (Professor) role;
            try {
                ProfessorFile professorFile = em.createQuery(
                        "select cf from ProfessorFile cf " +
                                "where cf.professor.id = :professorId " +
                                "and cf.id = :fileId " +
                                "and cf.deleted = false", ProfessorFile.class)
                        .setParameter("professorId", id)
                        .setParameter("fileId", fileId)
                        .getSingleResult();
                ProfessorFile pf = deleteAsMuchAsPossible(professorFile);
                if (pf == null) {
                    professor.getFiles().remove(professorFile);
                }
                // Check open candidacies
                if (updateCandidacies && professor.getUser().hasActiveRole(Role.RoleDiscriminator.CANDIDATE)) {
                    Candidate candidate = (Candidate) professor.getUser().getActiveRole(Role.RoleDiscriminator.CANDIDATE);
                    updateOpenCandidacies(candidate);
                }
                return;
            } catch (NoResultException e) {
                throw new NotFoundException("wrong.file.id");
            }
        }
        // Default Action
        throw new NotFoundException("wrong.file.id");
    }

    public Role updateStatus(Long id, Role requestRole, final User loggedOn) throws NotFoundException, ValidationException, NotEnabledException, ServiceException {
        // get role
        final Role primaryRole = get(id);
        // Validate
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (!primaryRole.getDiscriminator().equals(primaryRole.getUser().getPrimaryRole())) {
            throw new ValidationException("not.primary.role");
        }
        //Validate Change
        if (requestRole.getStatus().equals(Role.RoleStatus.ACTIVE) && primaryRole.isMissingRequiredFields()) {
            throw new ValidationException("role.missing.required.fields");
        }
        if (requestRole.getStatus().equals(Role.RoleStatus.ACTIVE) && primaryRole.getUser().isMissingRequiredFields()) {
            throw new ValidationException("user.missing.required.fields");
        }
        if (primaryRole instanceof InstitutionManager) {
            if (requestRole.getStatus().equals(Role.RoleStatus.ACTIVE)) {
                try {
                    InstitutionManager im = (InstitutionManager) primaryRole;
                    // Check if exists active IM for same Institution:
                    em.createQuery("select im.id from InstitutionManager im " +
                            "where im.status = :status " +
                            "and im.institution.id = :institutionId", Long.class)
                            .setParameter("status", Role.RoleStatus.ACTIVE)
                            .setParameter("institutionId", im.getInstitution().getId())
                            .setMaxResults(1)
                            .getSingleResult();
                    throw new ValidationException("exists.active.institution.manager");
                } catch (NoResultException e) {
                }
            }
        }

        if (primaryRole instanceof ProfessorDomestic || primaryRole instanceof ProfessorForeign) {
            if (!requestRole.getStatus().equals(Role.RoleStatus.ACTIVE)) {
                try {
                    em.createQuery("select pcm.id from PositionCommitteeMember pcm " +
                            "where pcm.committee.position.phase.status = :status " +
                            "and pcm.registerMember.professor.id = :professorId", Long.class)
                            .setParameter("status", Position.PositionStatus.EPILOGI)
                            .setParameter("professorId", primaryRole.getId())
                            .setMaxResults(1)
                            .getSingleResult();
                    throw new ValidationException("professor.is.committee.member");
                } catch (NoResultException e) {
                }
                try {
                    em.createQuery("select e.id from PositionEvaluator e " +
                            "where e.evaluation.position.phase.status = :status " +
                            "and e.registerMember.professor.id = :professorId", Long.class)
                            .setParameter("status", Position.PositionStatus.EPILOGI)
                            .setParameter("professorId", primaryRole.getId())
                            .setMaxResults(1)
                            .getSingleResult();
                    throw new ValidationException("professor.is.evaluator");
                } catch (NoResultException e) {
                }
            }
        }

        // Update
        Role.updateStatus(primaryRole, requestRole.getStatus());

        // Post to Jira
        JiraIssue issue = null;
        String summary;
        String description;
        switch (primaryRole.getStatus()) {
            case ACTIVE:
                // Update Issue
                summary = jiraService.getResourceBundleString("helpdesk.activated.role.summary");
                description = jiraService.getResourceBundleString("helpdesk.activated.role.description",
                        "user", primaryRole.getUser().getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + primaryRole.getUser().getId() + " )",
                        "admin", loggedOn.getFullName("el"));
                issue = JiraIssue.createRegistrationIssue(primaryRole.getUser(), summary, description);
                jiraService.queueCreateIssue(issue);

                // Send also an email to user:
                mailService.postEmail(primaryRole.getUser().getContactInfo().getEmail(),
                        "default.subject",
                        "profile.activated@user",
                        Collections.unmodifiableMap(new HashMap<String, String>() {

                            {
                                put("firstname_el", primaryRole.getUser().getFirstname("el"));
                                put("lastname_el", primaryRole.getUser().getLastname("el"));

                                put("firstname_en", primaryRole.getUser().getFirstname("en"));
                                put("lastname_en", primaryRole.getUser().getLastname("en"));
                            }
                        }));

                break;
            case UNAPPROVED:
                summary = jiraService.getResourceBundleString("helpdesk.activated.role.summary");
                description = jiraService.getResourceBundleString("helpdesk.activated.role.description",
                        "user", primaryRole.getUser().getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + primaryRole.getUser().getId() + " )",
                        "admin", loggedOn.getFullName("el"));
                issue = JiraIssue.createRegistrationIssue(primaryRole.getUser(), summary, description);
                jiraService.queueCreateIssue(issue);
                break;
            default:
                break;
        }

        // Return result
        primaryRole.getUser().getPrimaryRole();

        return primaryRole;
    }


    private boolean isForbiddenPair(Role.RoleDiscriminator first, Role.RoleDiscriminator second) {
        return forbiddenPairs.contains(new RolePair(first, second))
                || forbiddenPairs.contains(new RolePair(second, first));
    }

    private boolean isIncompatibleRole(Role role, Collection<Role> roles) {
        for (Role r : roles) {
            if (isForbiddenPair(role.getDiscriminator(), r.getDiscriminator()))
                return true;
        }
        return false;
    }

    private FileHeader _updateProfessorFile(User loggedOn, List<FileItem> fileItems, ProfessorFile professorFile, boolean updateCandidacies, Professor professor) throws Exception {
        File file = saveFile(loggedOn, fileItems, professorFile);
        // Check open candidacies
        if (updateCandidacies && professor.getUser().hasActiveRole(Role.RoleDiscriminator.CANDIDATE)) {
            Candidate candidate = (Candidate) professor.getUser().getActiveRole(Role.RoleDiscriminator.CANDIDATE);
            updateOpenCandidacies(candidate);
        }
        em.flush();
        professorFile.getBodies().size();

        return professorFile;
    }

    private FileHeader _updateCandidateFile(User loggedOn, List<FileItem> fileItems, CandidateFile candidateFile, boolean updateCandidacies, Candidate candidate) throws Exception {
        // Update File
        File file = saveFile(loggedOn, fileItems, candidateFile);
        // Check open candidacies
        if (updateCandidacies) {
            updateOpenCandidacies(candidate);

        }
        em.flush();
        candidateFile.getBodies().size();

        return candidateFile;
    }


}
