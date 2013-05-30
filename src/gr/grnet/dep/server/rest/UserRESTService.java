package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Address;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.MinistryAssistant;
import gr.grnet.dep.service.model.MinistryManager;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.UserRegistrationType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.ws.rs.Produces;
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

	@GET
	@JsonView({DetailedUserView.class})
	@SuppressWarnings("unchecked")
	public Collection<User> getAll(
		@HeaderParam(TOKEN_HEADER) String authToken,
		@QueryParam("username") String username,
		@QueryParam("firstname") String firstname,
		@QueryParam("lastname") String lastname,
		@QueryParam("status") String status,
		@QueryParam("role") String role,
		@QueryParam("roleStatus") String roleStatus,
		@QueryParam("im") Long imId,
		@QueryParam("mm") Long mmId) {

		getLoggedOn(authToken);

		// Prepare Query
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct u from User u " +
			"left join u.roles r " +
			"where u.username like :username " +
			"and u.basicInfo.firstname like :firstname " +
			"and u.basicInfo.lastname like :lastname ");
		if (status != null && !status.isEmpty()) {
			sb.append("and u.status = :status ");
		}
		if (role != null && !role.isEmpty()) {
			sb.append("and r.discriminator = :discriminator ");
		}
		if (roleStatus != null && !roleStatus.isEmpty()) {
			sb.append("and r.status = :roleStatus ");
		}
		if (imId != null) {
			sb.append("and (u.id in (select ia.user.id from InstitutionAssistant ia where ia.manager.id = :imId )) ");
		}
		if (mmId != null) {
			sb.append("and (u.id in (select ma.user.id from MinistryAssistant ma where ma.manager.id = :mmId )) ");
		}
		sb.append("order by u.basicInfo.lastname, u.basicInfo.firstname");

		Query query = em.createQuery(sb.toString())
			.setParameter("username", "%" + (username != null ? username : "") + "%")
			.setParameter("firstname", "%" + (firstname != null ? firstname : "") + "%")
			.setParameter("lastname", "%" + (lastname != null ? lastname : "") + "%");
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
				u.initializeCollections();
				if (!u.getPrimaryRole().equals(RoleDiscriminator.valueOf(role))) {
					it.remove();
				}
			}
		}

		// Return result
		for (User u : result) {
			u.initializeCollections();
		}
		return result;
	}

	@GET
	@Path("/loggedon")
	@JsonView({DetailedUserView.class})
	public Response getLoggedOn(@Context HttpServletRequest request) {
		String authToken = request.getHeader(TOKEN_HEADER);
		if (authToken == null && request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (c.getName().equals("_dep_a")) {
					authToken = c.getValue();
					break;
				}
			}
		}
		User u = super.getLoggedOn(authToken);
		u.initializeCollections();
		return Response.status(200)
			.header(TOKEN_HEADER, u.getAuthToken())
			.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
			.entity(u)
			.build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) throws RestException {
		getLoggedOn(authToken);
		try {
			User u = (User) em.createQuery(
				"from User u left join fetch u.roles " +
					"where u.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			u.initializeCollections();
			return u;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

	}

	@POST
	@JsonView({DetailedUserView.class})
	public User create(@HeaderParam(TOKEN_HEADER) String authToken, final User newUser) {
		try {
			//1. Validate
			// Has one role
			if (newUser.getRoles().isEmpty() || newUser.getRoles().size() > 1) {
				throw new RestException(Status.CONFLICT, "one.role.required");
			}
			// Identification availability
			try {
				em.createQuery("select identification from User u where u.identification = :identification")
					.setParameter("identification", newUser.getIdentification())
					.setMaxResults(1)
					.getSingleResult();
				throw new RestException(Status.CONFLICT, "existing.identification.number");
			} catch (NoResultException e) {
			}
			// Username availability
			try {
				em.createQuery("select u from User u where u.username = :username")
					.setParameter("username", newUser.getUsername())
					.getSingleResult();
				throw new RestException(Status.FORBIDDEN, "username.not.available");
			} catch (NoResultException e) {
			}
			// Contact mail availability
			try {
				em.createQuery("select u from User u where u.contactInfo.email = :email")
					.setParameter("email", newUser.getContactInfo().getEmail())
					.getSingleResult();
				throw new RestException(Status.FORBIDDEN, "contact.email.not.available");
			} catch (NoResultException e) {
			}

			//2. Set Fields
			Role firstRole = newUser.getRoles().iterator().next();
			firstRole.setStatus(RoleStatus.UNAPPROVED);
			firstRole.setStatusDate(new Date());

			newUser.setRegistrationType(UserRegistrationType.REGISTRATION_FORM);
			newUser.setRegistrationDate(new Date());
			newUser.setPasswordSalt(User.generatePasswordSalt());
			newUser.setPassword(User.encodePassword(newUser.getPassword(), newUser.getPasswordSalt()));
			newUser.setStatus(UserStatus.UNVERIFIED);
			newUser.setStatusDate(new Date());
			newUser.setVerificationNumber(newUser.generateVerificationNumber());
			newUser.setRoles(new ArrayList<Role>());
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
					throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			//3. Update
			em.persist(newUser);
			em.flush(); //To catch the exception

			//4. Send Verification E-Mail
			if (newUser.getStatus() == UserStatus.UNVERIFIED) {
				postEmail(newUser.getContactInfo().getEmail(),
					"verification.title",
					"verification.body",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", newUser.getUsername());
							put("firstname", newUser.getBasicInfo().getFirstname());
							put("lastname", newUser.getBasicInfo().getLastname());
							put("verificationLink", conf.getString("host") + "/depui/registration.html?#email=" + newUser.getUsername() + "&verification=" + newUser.getVerificationNumber());
						}
					}));
			}

			//5. Return result
			newUser.initializeCollections();
			return newUser;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, User user) {
		User loggedOn = getLoggedOn(authToken);
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		boolean canUpdate = loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR);
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
		// Identification availability
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
		try {

			// Copy User Fields
			existingUser.setIdentification(user.getIdentification());
			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				existingUser.setPasswordSalt(User.generatePasswordSalt());
				existingUser.setPassword(User.encodePassword(user.getPassword(), existingUser.getPasswordSalt()));
			}
			existingUser.setBasicInfo(user.getBasicInfo());
			existingUser.setBasicInfoLatin(user.getBasicInfoLatin());
			existingUser.setContactInfo(user.getContactInfo());

			em.flush();
			existingUser.initializeCollections();
			return existingUser;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			(existingUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT) && !((InstitutionAssistant) existingUser.getActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)).getManager().getUser().getId().equals(loggedOn.getId()))) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
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
	@JsonView({DetailedUserView.class})
	public Response login(@FormParam("username") String username, @FormParam("password") String password) {
		try {
			User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", username)
				.getSingleResult();

			switch (u.getStatus()) {
				case ACTIVE:
					if (u.getPassword().equals(User.encodePassword(password, u.getPasswordSalt()))) {
						u.setAuthToken(u.generateAuthenticationToken());
						u = em.merge(u);
						u.initializeCollections();
						return Response.status(200)
							.header(TOKEN_HEADER, u.getAuthToken())
							.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
							.entity(u)
							.build();
					} else {
						throw new RestException(Status.UNAUTHORIZED, "login.wrong.password");
					}
				default:
					throw new RestException(Status.UNAUTHORIZED, "login.account.status." + u.getStatus().toString().toLowerCase());
			}
		} catch (NoResultException e) {
			throw new RestException(Status.UNAUTHORIZED, "login.wrong.username");
		}
	}

	@GET
	@Path("/shibbolethLogin")
	@Produces("text/html")
	public Response shibbolethLogin(@Context HttpServletRequest request) {
		// 1. Read Attributes from request:
		String personalUniqueCode = readShibbolethField(request, "HTTP_SHIB_PERSONALUNIQUECODE", "Shib-PersonalUniqueCode");
		String name = readShibbolethField(request, "HTTP_SHIB_GIVENNAME", "Shib-givenName");
		String lastName = readShibbolethField(request, "HTTP_SHIB_SN", "Shib-sn");
		String affiliation = readShibbolethField(request, "HTTP_SHIB_AFFILIATION", "Shib-Affiliation");
		String email = readShibbolethField(request, "HTTP_SHIB_MAIL", "Shib-mail");
		String eduBranch = readShibbolethField(request, "HTTP_SHIB_UNDERGRADUATEBRANCH", "Shib-UndergraduateBranch");
		Long departmentId = (eduBranch != null && eduBranch.matches("(\\d+)")) ? Long.valueOf(eduBranch) : null;
		URI nextURL;
		try {
			nextURL = new URI(request.getParameter("nextURL") == null ? "" : request.getParameter("nextURL"));
		} catch (URISyntaxException e1) {
			throw new RestException(Status.BAD_REQUEST, "malformed.next.url");
		}

		// 2. Validate
		if (personalUniqueCode == null || name == null || lastName == null || affiliation == null || email == null || eduBranch == null || departmentId == null) {
			throw new RestException(Status.BAD_REQUEST, "shibboleth.fields.error");
		}
		if (!affiliation.equals("Professor") || !affiliation.equals("Researcher")) {
			throw new RestException(Status.UNAUTHORIZED, "wrong.affiliation");
		}
		Department department = em.find(Department.class, departmentId);
		if (department == null) {
			throw new RestException(Status.BAD_REQUEST, "wrong.department.id");
		}

		// 3. Find User
		User u;
		try {
			u = (User) em.createQuery("")
				.setParameter("shibPersonalUniqueCode", personalUniqueCode)
				.getSingleResult();
			u.setUsername(email);
			u.setAuthToken(u.generateAuthenticationToken());
		} catch (NoResultException e) {
			// Create User from Shibboleth Fields 
			u = new User();
			u.setRegistrationType(UserRegistrationType.SHIBBOLETH);
			u.setUsername(email);
			u.setShibPersonalUniqueCode(personalUniqueCode);
			u.getBasicInfo().setFirstname(name);
			u.getBasicInfo().setLastname(lastName);
			u.getContactInfo().setAddress(new Address());
			u.setRegistrationDate(new Date());
			u.setStatus(UserStatus.ACTIVE); // We trust shibboleth data, and lock changes from beginning
			u.setStatusDate(new Date());

			ProfessorDomestic pd = new ProfessorDomestic();
			pd.setDepartment(department);
			pd.setInstitution(department.getInstitution());
			pd.setStatus(RoleStatus.ACTIVE); // We trust shibboleth data, and lock changes from beginning
			pd.setStatusDate(new Date());
			//TODO: Shibboleth login: Get Rank from affiliation
			u.addRole(pd);
		}
		// 4. Persist User
		try {
			u = em.merge(u);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}

		return Response.status(200)
			.header(TOKEN_HEADER, u.getAuthToken())
			.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
			.location(nextURL)
			.entity(u)
			.build();
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
			// Reset password
			final String newPassword = User.generatePassword();
			u.setPasswordSalt(User.generatePasswordSalt());
			u.setPassword(User.encodePassword(newPassword, u.getPasswordSalt()));
			// Send email
			postEmail(u.getContactInfo().getEmail(),
				"password.reset.title",
				"password.reset.body",
				Collections.unmodifiableMap(new HashMap<String, String>() {

					{
						put("username", u.getUsername());
						put("firstname", u.getBasicInfo().getFirstname());
						put("lastname", u.getBasicInfo().getLastname());
						put("newPassword", newPassword);
					}
				}), true);
			// Return Result
			return Response.noContent().build();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.username");
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
				.setParameter("username", email)
				.getSingleResult();
			// Validate
			switch (u.getStatus()) {
				case UNVERIFIED:
					// Send email
					postEmail(u.getContactInfo().getEmail(),
						"verification.title",
						"verification.body",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("username", u.getUsername());
								put("firstname", u.getBasicInfo().getFirstname());
								put("lastname", u.getBasicInfo().getLastname());
								put("verificationLink", conf.getString("host") + "/depui/registration.html?#email=" + u.getUsername() + "&verification=" + u.getVerificationNumber());
							}
						}), true);

					// Return Result
					return Response.noContent().build();
				default:
					throw new RestException(Status.CONFLICT, "verify.account.status." + u.getStatus().toString().toLowerCase());
			}
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.username");
		}
	}

	@PUT
	@Path("/verify")
	@JsonView({DetailedUserView.class})
	public User verify(User user) {
		try {
			User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", user.getUsername())
				.getSingleResult();
			// Validate
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
					u.initializeCollections();
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
	@JsonView({DetailedUserView.class})
	public User updateStatus(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, User requestUser) {
		User loggedOn = getLoggedOn(authToken);
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
			User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			u.setStatus(requestUser.getStatus());
			u.setStatusDate(new Date());
			em.flush();

			u.initializeCollections();
			return u;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

}
