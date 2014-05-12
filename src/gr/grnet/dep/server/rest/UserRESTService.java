package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.AuthenticationService;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.Administrator;
import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.MinistryAssistant;
import gr.grnet.dep.service.model.MinistryManager;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.SearchData;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.User.UserView;
import gr.grnet.dep.service.model.User.UserWithLoginDataView;
import gr.grnet.dep.service.util.StringUtil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/user")
@Stateless
public class UserRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	AuthenticationService authenticationService;

	@GET
	@SuppressWarnings("unchecked")
	@JsonView({UserView.class})
	public Collection<User> getAll(
		@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken,
		@QueryParam("user") Long userId,
		@QueryParam("username") String username,
		@QueryParam("firstname") String firstname,
		@QueryParam("lastname") String lastname,
		@QueryParam("mobile") String mobile,
		@QueryParam("email") String email,
		@QueryParam("status") String status,
		@QueryParam("role") String role,
		@QueryParam("roleStatus") String roleStatus,
		@QueryParam("im") Long imId,
		@QueryParam("mm") Long mmId) {

		getLoggedOn(authToken);

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

		Query query = em.createQuery(sb.toString())
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
			query = query.setParameter("status", UserStatus.valueOf(status));
		}
		if (role != null && !role.isEmpty()) {
			query = query.setParameter("discriminator", RoleDiscriminator.valueOf(role));
		}
		if (roleStatus != null && !roleStatus.isEmpty()) {
			query = query.setParameter("roleStatus", RoleStatus.valueOf(roleStatus));
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
				if (!u.getPrimaryRole().equals(RoleDiscriminator.valueOf(role))) {
					it.remove();
				}
			}
		}

		// Return result
		return result;
	}

	@GET
	@Path("/loggedon")
	@JsonView({UserWithLoginDataView.class})
	public Response getLoggedOn(@Context HttpServletRequest request) {
		String authToken = request.getHeader(WebConstants.AUTHENTICATION_TOKEN_HEADER);
		if (authToken == null && request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (c.getName().equals("_dep_a")) {
					authToken = c.getValue();
					break;
				}
			}
		}
		User u = super.getLoggedOn(authToken);
		return Response.status(200)
			.header(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken())
			.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
			.entity(u)
			.build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) throws RestException {
		User loggedOn = getLoggedOn(authToken);
		try {
			User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			if (u.getId().equals(loggedOn.getId())) {
				return toJSON(u, UserWithLoginDataView.class);
			} else {
				return toJSON(u, UserView.class);
			}
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

	}

	@POST
	@JsonView({UserWithLoginDataView.class})
	public User create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, User newUser) {
		try {
			//1. Validate
			// Has one role
			if (newUser.getRoles().isEmpty() || newUser.getRoles().size() > 1) {
				throw new RestException(Status.CONFLICT, "one.role.required");
			}
			// Username availability
			if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
				throw new RestException(Status.BAD_REQUEST, "registration.username.required");
			}
			try {
				em.createQuery(
					"select u from User u" +
						" where u.username = :username")
					.setParameter("username", newUser.getUsername())
					.getSingleResult();
				throw new RestException(Status.FORBIDDEN, "username.not.available");
			} catch (NoResultException e) {
			}
			// Contact mail availability
			if (newUser.getContactInfo().getEmail() == null || newUser.getContactInfo().getEmail().trim().isEmpty()) {
				throw new RestException(Status.BAD_REQUEST, "registration.email.required");
			}
			try {
				em.createQuery(
					"select u from User u " +
						"where u.contactInfo.email = :email")
					.setParameter("email", newUser.getContactInfo().getEmail())
					.getSingleResult();
				throw new RestException(Status.FORBIDDEN, "contact.email.not.available");
			} catch (NoResultException e) {
			}

			//2. Set Fields
			Role firstRole = newUser.getRoles().iterator().next();
			firstRole.setStatus(RoleStatus.UNAPPROVED);
			firstRole.setStatusDate(new Date());

			newUser.setAuthenticationType(AuthenticationType.USERNAME);
			newUser.setPasswordSalt(authenticationService.generatePasswordSalt());
			newUser.setPassword(authenticationService.encodePassword(newUser.getPassword(), newUser.getPasswordSalt()));
			newUser.setVerificationNumber(generateVerificationNumber());

			newUser.setCreationDate(new Date());
			newUser.setStatus(UserStatus.UNVERIFIED);
			newUser.setStatusDate(new Date());

			newUser.addRole(firstRole);

			User loggedOn = null;
			switch (firstRole.getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					// Add Second Role : CANDIDATE
					Candidate secondRole = new Candidate();
					secondRole.setStatus(RoleStatus.UNAPPROVED);
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
						throw new RestException(Status.BAD_REQUEST, "wrong.institution.id");
					}
					try {
						em.createQuery(
							"select im from InstitutionManager im " +
								"where im.institution.id = :institutionId " +
								"and im.status = :status ")
							.setParameter("institutionId", newIM.getInstitution().getId())
							.setParameter("status", RoleStatus.ACTIVE)
							.setMaxResults(1)
							.getSingleResult();
						throw new RestException(Status.FORBIDDEN, "exists.active.institution.manager");
					} catch (NoResultException e) {
						//Not found, OK
					}
					break;
				case INSTITUTION_ASSISTANT:
					// CHECK LOGGEDON USER, ACTIVATE NEW USER
					loggedOn = getLoggedOn(authToken);
					if (!loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					((InstitutionAssistant) firstRole).setManager(((InstitutionManager) loggedOn.getActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)));
					newUser.setStatus(UserStatus.ACTIVE);
					newUser.setVerificationNumber(null);
					firstRole.setStatus(RoleStatus.ACTIVE);
					break;
				case MINISTRY_MANAGER:
					throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
				case MINISTRY_ASSISTANT:
					// CHECK LOGGEDON USER, ACTIVATE NEW USER
					loggedOn = getLoggedOn(authToken);
					if (!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER)) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					((MinistryAssistant) firstRole).setManager(((MinistryManager) loggedOn.getActiveRole(RoleDiscriminator.MINISTRY_MANAGER)));
					newUser.setStatus(UserStatus.ACTIVE);
					newUser.setVerificationNumber(null);
					firstRole.setStatus(RoleStatus.ACTIVE);
					break;
				case ADMINISTRATOR:
					// CHECK LOGGEDON USER, ACTIVATE NEW USER
					loggedOn = getLoggedOn(authToken);
					if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
					if (!admin.isSuperAdministrator()) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					newUser.setStatus(UserStatus.ACTIVE);
					newUser.setVerificationNumber(null);
					firstRole.setStatus(RoleStatus.ACTIVE);
			}

			// Identification Required
			if (!firstRole.getDiscriminator().equals(RoleDiscriminator.PROFESSOR_DOMESTIC) &&
				!firstRole.getDiscriminator().equals(RoleDiscriminator.PROFESSOR_FOREIGN) &&
				!firstRole.getDiscriminator().equals(RoleDiscriminator.ADMINISTRATOR) &&
				(newUser.getIdentification() == null || newUser.getIdentification().trim().isEmpty())) {
				throw new RestException(Status.BAD_REQUEST, "registration.identification.required");
			}
			// Identification availability
			if (newUser.getIdentification() != null && newUser.getIdentification().trim().length() > 0) {
				try {
					em.createQuery(
						"select identification from User u " +
							"where u.identification = :identification")
						.setParameter("identification", newUser.getIdentification())
						.setMaxResults(1)
						.getSingleResult();
					throw new RestException(Status.CONFLICT, "existing.identification.number");
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
			if (newUser.getStatus() == UserStatus.UNVERIFIED) {
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
			if (firstRole.getDiscriminator().equals(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
				String summary = jiraService.getResourceBundleString("institution.manager.created.assistant.summary");
				String description = jiraService.getResourceBundleString("institution.manager.created.assistant.description",
					"user", savedUser.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + savedUser.getId() + " )");
				JiraIssue issue = JiraIssue.createRegistrationIssue(savedUser, summary, description);
				jiraService.queueCreateIssue(issue);
			} else if (!firstRole.getDiscriminator().equals(RoleDiscriminator.ADMINISTRATOR)) {
				String summary = jiraService.getResourceBundleString("user.created.account.summary");
				String description = jiraService.getResourceBundleString("user.created.account.description",
					"user", savedUser.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + savedUser.getId() + " )");
				JiraIssue issue = JiraIssue.createRegistrationIssue(savedUser, summary, description);
				jiraService.queueCreateIssue(issue);
			}

			//6. Return result
			newUser.getRoles().size();
			return newUser;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}

	private Long generateVerificationNumber() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			return Math.abs(sr.nextLong());
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({UserWithLoginDataView.class})
	public User update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, User user) {
		User loggedOn = getLoggedOn(authToken);
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		boolean canUpdate = loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR);
		if (existingUser.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			canUpdate = loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR);
			if (canUpdate) {
				Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
				canUpdate = admin.isSuperAdministrator();
			}
		}
		if (!canUpdate) {
			canUpdate = loggedOn.getId().equals(id);
		}
		if (!canUpdate && existingUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
			InstitutionAssistant ia = (InstitutionAssistant) existingUser.getActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT);
			canUpdate = ia.getManager().getUser().getId().equals(loggedOn.getId());
		}
		if (!canUpdate && existingUser.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {
			MinistryAssistant ma = (MinistryAssistant) existingUser.getActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT);
			canUpdate = ma.getManager().getUser().getId().equals(loggedOn.getId());
		}
		if (!canUpdate) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Contact mail availability
		try {
			em.createQuery("select u from User u " +
				"where u.id != :id " +
				"and u.contactInfo.email = :email")
				.setParameter("id", user.getId())
				.setParameter("email", user.getContactInfo().getEmail())
				.getSingleResult();
			throw new RestException(Status.FORBIDDEN, "contact.email.not.available");
		} catch (NoResultException e) {
		}
		// Identification Required
		if (!existingUser.getPrimaryRole().equals(RoleDiscriminator.PROFESSOR_DOMESTIC) &&
			!existingUser.getPrimaryRole().equals(RoleDiscriminator.PROFESSOR_FOREIGN) &&
			!existingUser.getPrimaryRole().equals(RoleDiscriminator.ADMINISTRATOR) &&
			(user.getIdentification() == null || user.getIdentification().trim().isEmpty())) {
			throw new RestException(Status.BAD_REQUEST, "registration.identification.required");
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
				throw new RestException(Status.CONFLICT, "existing.identification.number");
			} catch (NoResultException e) {
			}
		}

		try {

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
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			(existingUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT) &&
			!((InstitutionAssistant) existingUser.getActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)).getManager().getUser().getId().equals(loggedOn.getId())
			)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (existingUser.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
			if (!admin.isSuperAdministrator()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
		}

		try {
			//Delete all associations
			em.createQuery(
				"delete from JiraIssue where user.id = :userId")
				.setParameter("userId", id)
				.executeUpdate();
			//Do Delete:
			em.remove(existingUser);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@JsonView({UserWithLoginDataView.class})
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {

		try {
			User u = authenticationService.doUsernameLogin(username, password);
			return Response.status(200)
				.header(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken())
				.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
				.entity(u)
				.build();
		} catch (ServiceException e) {
			throw new RestException(Status.UNAUTHORIZED, e.getErrorKey());
		}

	}

	@PUT
	@Path("/resetPassword")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response resetPassword(@FormParam("email") String email) {
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.contactInfo.email = :email")
				.setParameter("email", email)
				.getSingleResult();
			// Validate:
			if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
				throw new RestException(Status.NOT_FOUND, "login.wrong.registration.type");
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
			// Return Result
			return Response.noContent().build();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "login.wrong.email");
		}
	}

	@PUT
	@Path("/sendVerificationEmail")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendVerificationEmail(@FormParam("email") String email) {
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.contactInfo.email = :email")
				.setParameter("email", email)
				.getSingleResult();

			// Validate
			if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
				throw new RestException(Status.NOT_FOUND, "login.wrong.registration.type");
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

					// Return Result
					return Response.noContent().build();
				default:
					throw new RestException(Status.CONFLICT, "verify.account.status." + u.getStatus().toString().toLowerCase());
			}
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "login.wrong.email");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/sendLoginEmail")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendLoginEmail(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @FormParam("createLoginLink") Boolean createLoginLink) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.id = :id")
				.setParameter("id", id)
				.getSingleResult();

			// Validate
			if (!u.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
				throw new RestException(Status.NOT_FOUND, "login.wrong.registration.type");
			}

			if (createLoginLink != null && createLoginLink.booleanValue()) {
				// Create new AuthenticationToken
				u.setPermanentAuthToken(authenticationService.generatePermanentAuthenticationToken(u.getId(), u.getContactInfo().getEmail()));
				u.setLoginEmailSent(Boolean.FALSE);
			}
			mailService.sendLoginEmail(u.getId(), true);

			return Response.noContent().build();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "login.wrong.email");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/sendReminderLoginEmail")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendReminderLoginEmail(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @FormParam("createLoginLink") Boolean createLoginLink) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.id = :id")
				.setParameter("id", id)
				.getSingleResult();

			// Validate
			if (!u.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
				throw new RestException(Status.NOT_FOUND, "login.wrong.registration.type");
			}
			mailService.sendReminderLoginEmail(u.getId(), true);

			return Response.noContent().build();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "login.wrong.email");
		}
	}

	@PUT
	@Path("/verify")
	@JsonView({UserWithLoginDataView.class})
	public User verify(User user) {
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", user.getUsername())
				.getSingleResult();
			// Validate
			if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
				throw new RestException(Status.NOT_FOUND, "login.wrong.registration.type");
			}
			switch (u.getStatus()) {
				case UNVERIFIED:
					if (user.getVerificationNumber() == null || !user.getVerificationNumber().equals(u.getVerificationNumber())) {
						throw new RestException(Status.FORBIDDEN, "verify.wrong.verification");
					}
					// Verify
					u.setStatus(UserStatus.ACTIVE);
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
					throw new RestException(Status.FORBIDDEN, "verify.account.status." + u.getStatus().toString().toLowerCase());
			}

		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.username");
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/status")
	@JsonView({UserWithLoginDataView.class})
	public User updateStatus(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, User requestUser) {
		final User loggedOn = getLoggedOn(authToken);
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		boolean canUpdate = loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR);
		if (!canUpdate && existingUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
			InstitutionAssistant ia = (InstitutionAssistant) existingUser.getActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT);
			canUpdate = ia.getManager().getUser().getId().equals(loggedOn.getId());
		}
		if (!canUpdate && existingUser.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {
			MinistryAssistant ma = (MinistryAssistant) existingUser.getActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT);
			canUpdate = ma.getManager().getUser().getId().equals(loggedOn.getId());
		}
		if (!canUpdate) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			final User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			u.setStatus(requestUser.getStatus());
			u.setStatusDate(new Date());
			em.flush();

			// Post to Jira
			if (u.getStatus().equals(UserStatus.BLOCKED)) {
				String summary = jiraService.getResourceBundleString("helpdesk.blocked.user.summary");
				String description = jiraService.getResourceBundleString("helpdesk.blocked.user.description",
					"user", u.getFullName("el") + " ( " + WebConstants.conf.getString("home.url") + "/apella.html#user/" + u.getId() + " )",
					"admin", loggedOn.getFullName("el"));
				JiraIssue issue = JiraIssue.createRegistrationIssue(u, summary, description);
				jiraService.queueCreateIssue(issue);
			}
			return u;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/search")
	@JsonView({UserView.class})
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public SearchData<User> search(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) {
		User loggedOn = getLoggedOn(authToken);

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
			if (role.equals(RoleDiscriminator.CANDIDATE.toString())) {
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

		Query searchQuery = em.createQuery(
			"select usr from User usr " +
				"join fetch usr.roles rls " +
				"where usr.id in ( " +
				searchQueryString.toString() +
				" ) " +
				orderString);

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
			searchQuery.setParameter("status", UserStatus.valueOf(status));
			countQuery.setParameter("status", UserStatus.valueOf(status));
		}
		if (role != null && !role.isEmpty()) {
			searchQuery.setParameter("discriminator", RoleDiscriminator.valueOf(role));
			countQuery.setParameter("discriminator", RoleDiscriminator.valueOf(role));
			if (role.equals(RoleDiscriminator.CANDIDATE.toString())) {
				searchQuery.setParameter("pdDiscriminator", RoleDiscriminator.PROFESSOR_DOMESTIC);
				countQuery.setParameter("pdDiscriminator", RoleDiscriminator.PROFESSOR_DOMESTIC);
			}
		}
		if (roleStatus != null && !roleStatus.isEmpty()) {
			searchQuery.setParameter("roleStatus", RoleStatus.valueOf(roleStatus));
			countQuery.setParameter("roleStatus", RoleStatus.valueOf(roleStatus));
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
		// Fill result
		SearchData<User> result = new SearchData<User>();
		result.setiTotalRecords(totalRecords);
		result.setiTotalDisplayRecords(totalRecords);
		result.setsEcho(Integer.valueOf(sEcho));
		result.setRecords(paginatedUsers);

		return result;
	}
}
