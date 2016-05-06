package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.system.WebConstants;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Stateless
public class UserService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @EJB
    private AuthenticationService authenticationService;

    @EJB
    private MailService mailService;

    @EJB
    private JiraService jiraService;

    public List<User> getAssistants(Long userId, String username, String firstname, String lastname, String mobile, String email, String status, String role, String roleStatus, Long imId, Long mmId) {
        // Prepare Query
        StringBuilder sb = new StringBuilder();
        sb.append("select usr from User usr " +
                "left join fetch usr.roles rls " +
                "where usr.id in (" +
                "	select distinct u.id from User u " +
                "	join u.roles r " +
                "	where u.basicInfo.firstname like :firstname " +
                "	and u.basicInfo.lastname like :lastname ");
        // Query Params
        if (userId != null) {
            sb.append("	and u.id = :userId ");
        }
        if (username != null && !username.isEmpty()) {
            sb.append("	and u.username like :username ");
        }
        if (mobile != null && !mobile.isEmpty()) {
            sb.append("	and u.contactInfo.mobile = :mobile ");
        }
        if (email != null && !email.isEmpty()) {
            sb.append("	and u.contactInfo.email = :email ");
        }
        if (status != null && !status.isEmpty()) {
            sb.append("	and u.status = :status ");
        }
        if (role != null && !role.isEmpty()) {
            sb.append("	and r.discriminator = :discriminator ");
        }
        if (roleStatus != null && !roleStatus.isEmpty()) {
            sb.append("	and r.status = :roleStatus ");
        }
        if (imId != null) {
            sb.append("	and (u.id in (select ia.user.id from InstitutionAssistant ia where ia.manager.id = :imId )) ");
        }
        if (mmId != null) {
            sb.append("	and (u.id in (select ma.user.id from MinistryAssistant ma where ma.manager.id = :mmId )) ");
        }
        // Query Sorting, grouping
        sb.append(") ");
        sb.append("order by usr.basicInfo.lastname, usr.basicInfo.firstname");

        TypedQuery<User> query = em.createQuery(sb.toString(), User.class)
                .setParameter("firstname", "%" + (firstname != null ? StringUtil.toUppercaseNoTones(firstname, new Locale("el")) : "") + "%")
                .setParameter("lastname", "%" + (lastname != null ? StringUtil.toUppercaseNoTones(lastname, new Locale("el")) : "") + "%");

        if (userId != null) {
            query.setParameter("userId", userId);
        }
        if (username != null && !username.isEmpty()) {
            query.setParameter("username", "%" + username + "%");
        }
        if (mobile != null && !mobile.isEmpty()) {
            query.setParameter("mobile", mobile);
        }
        if (email != null && !email.isEmpty()) {
            query.setParameter("email", email);
        }
        if (status != null && !status.isEmpty()) {
            query = query.setParameter("status", User.UserStatus.valueOf(status));
        }
        if (role != null && !role.isEmpty()) {
            query = query.setParameter("discriminator", Role.RoleDiscriminator.valueOf(role));
        }
        if (roleStatus != null && !roleStatus.isEmpty()) {
            query = query.setParameter("roleStatus", Role.RoleStatus.valueOf(roleStatus));
        }
        if (imId != null) {
            query = query.setParameter("imId", imId);
        }
        if (mmId != null) {
            query = query.setParameter("mmId", mmId);
        }

        // Get Result
        List<User> result = query.getResultList();

        // Filter results where role requested is not primary
        if (role != null && !role.isEmpty()) {
            Iterator<User> it = result.iterator();
            while (it.hasNext()) {
                User u = it.next();
                if (!u.getPrimaryRole().equals(Role.RoleDiscriminator.valueOf(role))) {
                    it.remove();
                }
            }
        }

        // Return result
        return result;
    }

    public User get(Long id) throws NotFoundException {

        try {
            User user = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.id=:id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return user;

        } catch (NoResultException e) {
            throw new NotFoundException("wrong.id");
        }
    }

    public User create(String authToken, User newUser) throws ValidationException, NotEnabledException, ServiceException {
        //1. Validate
        // Has one role
        if (newUser.getRoles().isEmpty() || newUser.getRoles().size() > 1) {
            throw new ValidationException("one.role.required");
        }
        // Username availability
        if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            throw new ValidationException("registration.username.required");
        }
        try {
            em.createQuery(
                    "select u from User u" +
                            " where u.username = :username", User.class)
                    .setParameter("username", newUser.getUsername())
                    .getSingleResult();

            throw new ValidationException("username.not.available");
        } catch (NoResultException e) {
        }

        // Contact mail availability
        if (newUser.getContactInfo().getEmail() == null || newUser.getContactInfo().getEmail().trim().isEmpty()) {
            throw new ValidationException("registration.email.required");
        }
        try {
            em.createQuery(
                    "select u from User u " +
                            "where u.contactInfo.email = :email", User.class)
                    .setParameter("email", newUser.getContactInfo().getEmail())
                    .getSingleResult();

            throw new ValidationException("contact.email.not.available");
        } catch (NoResultException e) {
        }

        //2. Set Fields
        Role firstRole = newUser.getRoles().iterator().next();
        firstRole.setStatus(Role.RoleStatus.UNAPPROVED);
        firstRole.setStatusDate(new Date());

        newUser.setAuthenticationType(AuthenticationType.USERNAME);
        newUser.setPasswordSalt(authenticationService.generatePasswordSalt());
        newUser.setPassword(authenticationService.encodePassword(newUser.getPassword(), newUser.getPasswordSalt()));
        newUser.setVerificationNumber(generateVerificationNumber());

        newUser.setCreationDate(new Date());
        newUser.setStatus(User.UserStatus.UNVERIFIED);
        newUser.setStatusDate(new Date());

        newUser.addRole(firstRole);

        User loggedOn = null;
        switch (firstRole.getDiscriminator()) {
            case PROFESSOR_DOMESTIC:
                // Add Second Role : CANDIDATE
                Candidate secondRole = new Candidate();
                secondRole.setStatus(Role.RoleStatus.UNAPPROVED);
                secondRole.setStatusDate(new Date());
                newUser.addRole(secondRole);
                break;
            case PROFESSOR_FOREIGN:
                break;
            case CANDIDATE:
                break;
            case INSTITUTION_MANAGER:
                // Validate Institution:
                InstitutionManager newIM = (InstitutionManager) firstRole;
                if (newIM.getInstitution() == null || newIM.getInstitution().getId() == null) {
                    throw new ValidationException("wrong.institution.id");
                }
                try {
                    em.createQuery(
                            "select im from InstitutionManager im " +
                                    "where im.institution.id = :institutionId " +
                                    "and im.status = :status ", InstitutionManager.class)
                            .setParameter("institutionId", newIM.getInstitution().getId())
                            .setParameter("status", Role.RoleStatus.ACTIVE)
                            .setMaxResults(1)
                            .getSingleResult();
                    throw new ValidationException("exists.active.institution.manager");
                } catch (NoResultException e) {
                    //Not found, OK
                }
                break;
            case INSTITUTION_ASSISTANT:
                loggedOn = authenticationService.getLoggedOn(authToken);
                if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER)) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                ((InstitutionAssistant) firstRole).setManager(((InstitutionManager) loggedOn.getActiveRole(Role.RoleDiscriminator.INSTITUTION_MANAGER)));
                newUser.setStatus(User.UserStatus.ACTIVE);
                newUser.setVerificationNumber(null);
                firstRole.setStatus(Role.RoleStatus.ACTIVE);
                break;
            case MINISTRY_MANAGER:
                throw new NotEnabledException("insufficient.privileges");
            case MINISTRY_ASSISTANT:
                loggedOn = authenticationService.getLoggedOn(authToken);
                // CHECK LOGGEDON USER, ACTIVATE NEW USER
                if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER)) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                ((MinistryAssistant) firstRole).setManager(((MinistryManager) loggedOn.getActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER)));
                newUser.setStatus(User.UserStatus.ACTIVE);
                newUser.setVerificationNumber(null);
                firstRole.setStatus(Role.RoleStatus.ACTIVE);
                break;
            case ADMINISTRATOR:
                loggedOn = authenticationService.getLoggedOn(authToken);
                // CHECK LOGGEDON USER, ACTIVATE NEW USER
                if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
                if (!admin.isSuperAdministrator()) {
                    throw new NotEnabledException("insufficient.privileges");
                }
                newUser.setStatus(User.UserStatus.ACTIVE);
                newUser.setVerificationNumber(null);
                firstRole.setStatus(Role.RoleStatus.ACTIVE);
        }

        // Identification Required
        if (!firstRole.getDiscriminator().equals(Role.RoleDiscriminator.PROFESSOR_DOMESTIC) &&
                !firstRole.getDiscriminator().equals(Role.RoleDiscriminator.PROFESSOR_FOREIGN) &&
                !firstRole.getDiscriminator().equals(Role.RoleDiscriminator.ADMINISTRATOR) &&
                (newUser.getIdentification() == null || newUser.getIdentification().trim().isEmpty())) {
            throw new ValidationException("registration.identification.required");
        }
        // Identification availability
        if (newUser.getIdentification() != null && newUser.getIdentification().trim().length() > 0) {
            try {
                em.createQuery(
                        "select identification from User u " +
                                "where u.identification = :identification", String.class)
                        .setParameter("identification", newUser.getIdentification())
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("existing.identification.number");
            } catch (NoResultException e) {
            }
        }

        // Capitalize names
        newUser.getBasicInfo().toUppercase(new Locale("el"));
        newUser.getBasicInfoLatin().toUppercase(Locale.ENGLISH);
        //3. Update
        final User savedUser = em.merge(newUser);
        em.flush(); //To catch the exception

        //4. Send Verification E-Mail
        if (newUser.getStatus() == User.UserStatus.UNVERIFIED) {
            mailService.postEmail(newUser.getContactInfo().getEmail(),
                    "verification.title",
                    "verification.body",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("username", savedUser.getUsername());
                            put("verificationLink", WebConstants.conf.getString("home.url") + "/registration.html#username=" + savedUser.getUsername() + "&verification=" + savedUser.getVerificationNumber());
                        }
                    }), true);
        }

        //5. Post Issue:
        if (firstRole.getDiscriminator().equals(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            String summary = jiraService.getResourceBundleString("institution.manager.created.assistant.summary");
            String description = jiraService.getResourceBundleString("institution.manager.created.assistant.description",
                    "user", savedUser.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + savedUser.getId() + " )");
            JiraIssue issue = JiraIssue.createRegistrationIssue(savedUser, summary, description);
            jiraService.queueCreateIssue(issue);
        } else if (!firstRole.getDiscriminator().equals(Role.RoleDiscriminator.ADMINISTRATOR)) {
            String summary = jiraService.getResourceBundleString("user.created.account.summary");
            String description = jiraService.getResourceBundleString("user.created.account.description",
                    "user", savedUser.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + savedUser.getId() + " )");
            JiraIssue issue = JiraIssue.createRegistrationIssue(savedUser, summary, description);
            jiraService.queueCreateIssue(issue);
        }

        //6. Return result
        newUser.getRoles().size();
        return newUser;
    }


    public User update(Long id, User user, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {

        // find user
        User existingUser = get(id);

        boolean canUpdate = loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);

        if (existingUser.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            canUpdate = loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
            if (canUpdate) {
                Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
                canUpdate = admin.isSuperAdministrator();
            }
        }
        if (!canUpdate) {
            canUpdate = loggedOn.getId().equals(id);
        }
        if (!canUpdate && existingUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            InstitutionAssistant ia = (InstitutionAssistant) existingUser.getActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT);
            canUpdate = ia.getManager().getUser().getId().equals(loggedOn.getId());
        }
        if (!canUpdate && existingUser.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT)) {
            MinistryAssistant ma = (MinistryAssistant) existingUser.getActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT);
            canUpdate = ma.getManager().getUser().getId().equals(loggedOn.getId());
        }
        if (!canUpdate) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // Contact mail availability
        try {
            em.createQuery("select u from User u " +
                    "where u.id != :id " +
                    "and u.contactInfo.email = :email", User.class)
                    .setParameter("id", user.getId())
                    .setParameter("email", user.getContactInfo().getEmail())
                    .getSingleResult();

            throw new NotEnabledException("contact.email.not.available");
        } catch (NoResultException e) {
        }
        // Identification Required
        if (!existingUser.getPrimaryRole().equals(Role.RoleDiscriminator.PROFESSOR_DOMESTIC) &&
                !existingUser.getPrimaryRole().equals(Role.RoleDiscriminator.PROFESSOR_FOREIGN) &&
                !existingUser.getPrimaryRole().equals(Role.RoleDiscriminator.ADMINISTRATOR) &&
                (user.getIdentification() == null || user.getIdentification().trim().isEmpty())) {
            throw new ValidationException("registration.identification.required");
        }
        // Identification availability
        if (user.getIdentification() != null && user.getIdentification().trim().length() > 0) {
            try {
                em.createQuery("select identification from User u " +
                        "where u.id != :id " +
                        "and u.identification = :identification")
                        .setParameter("id", user.getId())
                        .setParameter("identification", user.getIdentification())
                        .setMaxResults(1)
                        .getSingleResult();
                throw new ValidationException("existing.identification.number");
            } catch (NoResultException e) {
            }
        }

        // Copy User Fields
        existingUser.setIdentification(user.getIdentification());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPasswordSalt(authenticationService.generatePasswordSalt());
            existingUser.setPassword(authenticationService.encodePassword(user.getPassword(), existingUser.getPasswordSalt()));
        }
        existingUser.setBasicInfo(user.getBasicInfo());
        existingUser.setBasicInfoLatin(user.getBasicInfoLatin());
        existingUser.setContactInfo(user.getContactInfo());
        // Capitalize names
        existingUser.getBasicInfo().toUppercase(new Locale("el"));
        existingUser.getBasicInfoLatin().toUppercase(Locale.ENGLISH);
        em.flush();

        return existingUser;
    }

    public void delete(Long id, User loggedOn) throws NotFoundException, NotEnabledException {
        // get user
        User existingUser = get(id);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                (existingUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT) &&
                        !((InstitutionAssistant) existingUser.getActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)).getManager().getUser().getId().equals(loggedOn.getId())
                )) {
            throw new NotEnabledException("insufficient.privileges");
        }
        if (existingUser.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
                throw new NotEnabledException("insufficient.privileges");
            }
            Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
            if (!admin.isSuperAdministrator()) {
                throw new NotEnabledException("insufficient.privileges");
            }
        }

        //Delete all associations
        em.createQuery(
                "delete from JiraIssue where user.id = :userId")
                .setParameter("userId", id)
                .executeUpdate();

        //Do Delete:
        em.remove(existingUser);
        em.flush();
    }

    public void resetPassword(String email) throws NotFoundException {
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.contactInfo.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();

            // Validate:
            if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
                throw new NotFoundException("login.wrong.registration.type");
            }

            // Reset password
            final String newPassword = authenticationService.generatePassword();
            u.setPasswordSalt(authenticationService.generatePasswordSalt());
            u.setPassword(authenticationService.encodePassword(newPassword, u.getPasswordSalt()));

            // Send email
            mailService.postEmail(u.getContactInfo().getEmail(),
                    "password.reset.title",
                    "password.reset.body",
                    Collections.unmodifiableMap(new HashMap<String, String>() {

                        {
                            put("username", u.getUsername());
                            put("newPassword", newPassword);
                        }
                    }), true);

        } catch (NoResultException e) {
            throw new NotFoundException("login.wrong.email");
        }
    }

    public void sendVerificationEmail(String email) throws NotFoundException, ValidationException {
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.contactInfo.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();

            // Validate
            if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
                throw new NotFoundException("login.wrong.registration.type");
            }
            switch (u.getStatus()) {
                case UNVERIFIED:
                    // Send email
                    mailService.postEmail(u.getContactInfo().getEmail(),
                            "verification.title",
                            "verification.body",
                            Collections.unmodifiableMap(new HashMap<String, String>() {

                                {
                                    put("username", u.getUsername());
                                    put("verificationLink", WebConstants.conf.getString("home.url") + "/registration.html#username=" + u.getUsername() + "&verification=" + u.getVerificationNumber());
                                }
                            }), true);
                default:
                    throw new ValidationException("verify.account.status." + u.getStatus().toString().toLowerCase());
            }
        } catch (NoResultException e) {
            throw new NotFoundException("login.wrong.email");
        }
    }

    public void sendLoginEmail(Long id, Boolean createLoginLink, User loggedOn) throws NotEnabledException, NotFoundException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // Validate
            if (!u.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
                throw new NotFoundException("login.wrong.registration.type");
            }

            if (createLoginLink != null && createLoginLink) {
                // Create new AuthenticationToken
                u.setPermanentAuthToken(authenticationService.generatePermanentAuthenticationToken(u.getId(), u.getContactInfo().getEmail()));
                u.setLoginEmailSent(Boolean.FALSE);
            }
            mailService.sendLoginEmail(u.getId(), true);

        } catch (NoResultException e) {
            throw new NotFoundException("login.wrong.email");
        }
    }

    public void sendReminderLoginEmail(Long id, User loggedOn) throws NotEnabledException, NotFoundException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // Validate
            if (!u.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
                throw new NotFoundException("login.wrong.registration.type");
            }
            // send email
            mailService.sendReminderLoginEmail(u.getId(), true);

        } catch (NoResultException e) {
            throw new NotFoundException("login.wrong.email");
        }
    }

    public void sendEvaluationEmail(Long id, User loggedOn) throws NotEnabledException, NotFoundException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // Validate
            if (u.getStatus().equals(User.UserStatus.ACTIVE) && u.getRole(u.getPrimaryRole()).getStatus().equals(Role.RoleStatus.ACTIVE)) {
                mailService.sendEvaluationEmail(u.getId(), true);
            } else {
                throw new NotEnabledException("user.status.not.active");
            }

        } catch (NoResultException e) {
            throw new NotFoundException("login.wrong.email");
        }
    }

    public User verify(User user) throws NotFoundException, NotEnabledException, ValidationException, ServiceException {
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.username = :username", User.class)
                    .setParameter("username", user.getUsername())
                    .getSingleResult();

            // Validate
            if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
                throw new NotFoundException("login.wrong.registration.type");
            }

            switch (u.getStatus()) {
                case UNVERIFIED:
                    if (user.getVerificationNumber() == null || !user.getVerificationNumber().equals(u.getVerificationNumber())) {
                        throw new NotEnabledException("verify.wrong.verification");
                    }
                    // Verify
                    u.setStatus(User.UserStatus.ACTIVE);
                    u.setStatusDate(new Date());
                    u.setVerificationNumber(null);

                    em.flush();

                    // Post to Jira
                    String summary = jiraService.getResourceBundleString("user.verified.email.summary");
                    String description = jiraService.getResourceBundleString("user.verified.email.description",
                            "user", u.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + u.getId() + " )");
                    JiraIssue issue = JiraIssue.createRegistrationIssue(u, summary, description);
                    jiraService.queueCreateIssue(issue);
                    return u;
                default:
                    throw new NotEnabledException("verify.account.status." + u.getStatus().toString().toLowerCase());
            }

        } catch (NoResultException e) {
            throw new NotFoundException("wrong.username");
        }
    }

    public User updateStatus(Long id, User requestUser, final User loggedOn) throws NotFoundException, NotEnabledException, ValidationException, ServiceException {
        // find existing user
        User existingUser = get(id);

        boolean canUpdate = loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
        if (!canUpdate && existingUser.hasActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT)) {
            InstitutionAssistant ia = (InstitutionAssistant) existingUser.getActiveRole(Role.RoleDiscriminator.INSTITUTION_ASSISTANT);
            canUpdate = ia.getManager().getUser().getId().equals(loggedOn.getId());
        }
        if (!canUpdate && existingUser.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT)) {
            MinistryAssistant ma = (MinistryAssistant) existingUser.getActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT);
            canUpdate = ma.getManager().getUser().getId().equals(loggedOn.getId());
        }
        if (!canUpdate) {
            throw new NotEnabledException("insufficient.privileges");
        }
        try {
            final User u = em.createQuery(
                    "from User u " +
                            "left join fetch u.roles " +
                            "where u.id=:id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
            u.setStatus(requestUser.getStatus());
            u.setStatusDate(new Date());
            em.flush();

            // Post to Jira
            if (u.getStatus().equals(User.UserStatus.BLOCKED)) {
                String summary = jiraService.getResourceBundleString("helpdesk.blocked.user.summary");
                String description = jiraService.getResourceBundleString("helpdesk.blocked.user.description",
                        "user", u.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + u.getId() + " )",
                        "admin", loggedOn.getFullName("el"));
                JiraIssue issue = JiraIssue.createRegistrationIssue(u, summary, description);
                jiraService.queueCreateIssue(issue);
            }
            return u;
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.user.id");
        }
    }

    public SearchData<User> search(HttpServletRequest request, User loggedOn) throws NotEnabledException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(Role.RoleDiscriminator.MINISTRY_ASSISTANT)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // 1. Read parameters:
        Long userId = request.getParameter("user").matches("\\d+") ? Long.valueOf(request.getParameter("user")) : null;
        String username = request.getParameter("username");
        String firstname = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");
        String mobile = request.getParameter("mobile");
        String email = request.getParameter("email");
        String status = request.getParameter("status");
        String role = request.getParameter("role");
        String roleStatus = request.getParameter("roleStatus");
        Long institutionId = request.getParameter("institution").matches("\\d+") ? Long.valueOf(request.getParameter("institution")) : null;
        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");
        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));
        // DataTables:
        String sEcho = request.getParameter("sEcho");

        // 2. Prepare Query
        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append(
                "select u.id from User u " +
                        "join u.roles r " +
                        "where u.id != null ");
        if (userId != null) {
            searchQueryString.append(" and u.id = :userId ");
        }
        if (username != null && !username.isEmpty()) {
            searchQueryString.append(" and u.username like :username ");
        }
        if (firstname != null && !firstname.isEmpty()) {
            searchQueryString.append(" and u.basicInfo.firstname like :firstname ");
        }
        if (lastname != null && !lastname.isEmpty()) {
            searchQueryString.append(" and u.basicInfo.lastname like :lastname ");
        }
        if (mobile != null && !mobile.isEmpty()) {
            searchQueryString.append(" and u.contactInfo.mobile = :mobile ");
        }
        if (email != null && !email.isEmpty()) {
            searchQueryString.append(" and u.contactInfo.email = :email ");
        }
        if (status != null && !status.isEmpty()) {
            searchQueryString.append(" and u.status = :status ");
        }
        if (role != null && !role.isEmpty()) {
            searchQueryString.append(" and r.discriminator = :discriminator ");
            if (role.equals(Role.RoleDiscriminator.CANDIDATE.toString())) {
                // Skip Professors with Candidate role
                searchQueryString.append(" and not exists (select sr.id from Role sr where sr.discriminator = :pdDiscriminator and sr.user.id = u.id) ");
            }
        }
        if (roleStatus != null && !roleStatus.isEmpty()) {
            searchQueryString.append("	and r.status = :roleStatus ");
        }
        if (institutionId != null) {
            searchQueryString.append("	and exists (" +
                    "		select pd.id from ProfessorDomestic pd " +
                    "		where pd.id = r.id " +
                    "		and pd.institution.id = :institutionId " +
                    "	) ");
        }

        // Query Sorting
        String orderString = null;
        if (orderField != null && !orderField.isEmpty()) {
            if (orderField.equals("firstname")) {
                orderString = "order by usr.basicInfo.firstname " + orderDirection + ", usr.id ";
            } else if (orderField.equals("lastname")) {
                orderString = "order by usr.basicInfo.lastname " + orderDirection + ", usr.id ";
            } else if (orderField.equals("username")) {
                orderString = "order by usr.username " + orderDirection + ", usr.id ";
            } else if (orderField.equals("status")) {
                orderString = "order by usr.status " + orderDirection + ", usr.id ";
            } else {
                // id is default
                orderString = "order by usr.id " + orderDirection + " ";
            }
        } else {
            orderString = "order by usr.id " + orderDirection + " ";
        }

        Query countQuery = em.createQuery(
                "select count(id) from User usr " +
                        "where usr.id in ( " +
                        searchQueryString.toString() +
                        " ) ");

        TypedQuery<User> searchQuery = em.createQuery(
                "select usr from User usr " +
                        "where usr.id in ( " +
                        searchQueryString.toString() +
                        " ) " +
                        orderString, User.class);

        if (userId != null) {
            searchQuery.setParameter("userId", userId);
            countQuery.setParameter("userId", userId);
        }
        if (firstname != null && !firstname.isEmpty()) {
            searchQuery.setParameter("firstname", "%" + (firstname != null ? StringUtil.toUppercaseNoTones(firstname, new Locale("el")) : "") + "%");
            countQuery.setParameter("firstname", "%" + (firstname != null ? StringUtil.toUppercaseNoTones(firstname, new Locale("el")) : "") + "%");
        }
        if (lastname != null && !lastname.isEmpty()) {
            searchQuery.setParameter("lastname", "%" + (lastname != null ? StringUtil.toUppercaseNoTones(lastname, new Locale("el")) : "") + "%");
            countQuery.setParameter("lastname", "%" + (lastname != null ? StringUtil.toUppercaseNoTones(lastname, new Locale("el")) : "") + "%");
        }
        if (username != null && !username.isEmpty()) {
            searchQuery.setParameter("username", "%" + username + "%");
            countQuery.setParameter("username", "%" + username + "%");
        }
        if (mobile != null && !mobile.isEmpty()) {
            searchQuery.setParameter("mobile", mobile);
            countQuery.setParameter("mobile", mobile);
        }
        if (email != null && !email.isEmpty()) {
            searchQuery.setParameter("email", email);
            countQuery.setParameter("email", email);
        }
        if (status != null && !status.isEmpty()) {
            searchQuery.setParameter("status", User.UserStatus.valueOf(status));
            countQuery.setParameter("status", User.UserStatus.valueOf(status));
        }
        if (role != null && !role.isEmpty()) {
            searchQuery.setParameter("discriminator", Role.RoleDiscriminator.valueOf(role));
            countQuery.setParameter("discriminator", Role.RoleDiscriminator.valueOf(role));
            if (role.equals(Role.RoleDiscriminator.CANDIDATE.toString())) {
                searchQuery.setParameter("pdDiscriminator", Role.RoleDiscriminator.PROFESSOR_DOMESTIC);
                countQuery.setParameter("pdDiscriminator", Role.RoleDiscriminator.PROFESSOR_DOMESTIC);
            }
        }
        if (roleStatus != null && !roleStatus.isEmpty()) {
            searchQuery.setParameter("roleStatus", Role.RoleStatus.valueOf(roleStatus));
            countQuery.setParameter("roleStatus", Role.RoleStatus.valueOf(roleStatus));
        }
        if (institutionId != null) {
            searchQuery.setParameter("institutionId", institutionId);
            countQuery.setParameter("institutionId", institutionId);
        }

        // Get Result
        Long totalRecords = (Long) countQuery.getSingleResult();
        @SuppressWarnings("unchecked")
        List<User> paginatedUsers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();
        // Lazily load all Roles of Users,
        // with join fetch, hibernate will bring all Users to memory and then apply ordering
        for (User u : paginatedUsers) {
            u.getRoles().size();
        }
        // Fill result
        SearchData<User> result = new SearchData<User>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(paginatedUsers);

        return result;
    }

    public SearchData<User> searchShibbolethAccountUsers(HttpServletRequest request, User loggedOn) throws NotEnabledException {
        // validate
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }
        // 1. Read parameters:
        String filterText = request.getParameter("sSearch");
        String sEcho = request.getParameter("sEcho");
        // Ordering
        String orderNo = request.getParameter("iSortCol_0");
        String orderField = request.getParameter("mDataProp_" + orderNo);
        String orderDirection = request.getParameter("sSortDir_0");
        // Pagination
        int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));

        // 2. Prepare Query
        StringBuilder searchQueryString = new StringBuilder();
        searchQueryString.append(
                " from User u where u.authenticationType = :shibboleth ");

        if (StringUtils.isNotEmpty(filterText)) {
            searchQueryString.append(" and ( UPPER(u.basicInfo.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfoLatin.lastname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfo.firstname) like :filterText ");
            searchQueryString.append(" or UPPER(u.basicInfoLatin.firstname) like :filterText ");
            // treat the user id as text in order to filter it
            searchQueryString.append(" or CAST(u.id AS text) like :filterText ) ");
        }

        // count query
        Query countQuery = em.createQuery(
                "select distinct count(u.id) " +
                        searchQueryString.toString());

        countQuery.setParameter("shibboleth", AuthenticationType.SHIBBOLETH);

        if (StringUtils.isNotEmpty(filterText)) {
            countQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        // Get Result
        Long totalRecords = (Long) countQuery.getSingleResult();

        // Query Sorting
        String orderString = null;

        if (!StringUtils.isNotEmpty(orderField)) {
            if (orderField.equals("firstname")) {
                orderString = "order by usr.basicInfo.firstname " + orderDirection + ", usr.id ";
            } else if (orderField.equals("lastname")) {
                orderString = "order by usr.basicInfo.lastname " + orderDirection + ", usr.id ";
            } else {
                // id is default
                orderString = "order by usr.id " + orderDirection + " ";
            }
        } else {
            orderString = "order by usr.id " + orderDirection + " ";
        }

        // search query
        TypedQuery<User> searchQuery = em.createQuery(
                "select distinct usr from User usr " +
                        "where usr.id in ( select u.id " + searchQueryString.toString() + ") " +
                        orderString, User.class);

        searchQuery.setParameter("shibboleth", AuthenticationType.SHIBBOLETH);

        if (StringUtils.isNotEmpty(filterText)) {
            searchQuery.setParameter("filterText", filterText.toUpperCase() + "%");
        }

        List<User> paginatedShibbolethUsers = searchQuery
                .setFirstResult(iDisplayStart)
                .setMaxResults(iDisplayLength)
                .getResultList();

        for (User shibbolethUser : paginatedShibbolethUsers) {
            shibbolethUser.getRoles().size();
        }

        // Fill result
        SearchData<User> result = new SearchData<User>();
        result.setiTotalRecords(totalRecords);
        result.setiTotalDisplayRecords(totalRecords);
        result.setsEcho(Integer.valueOf(sEcho));
        result.setRecords(paginatedShibbolethUsers);

        return result;
    }


    private Long generateVerificationNumber() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            return Math.abs(sr.nextLong());
        } catch (NoSuchAlgorithmException nsae) {
            throw new EJBException(nsae);
        }
    }


}
