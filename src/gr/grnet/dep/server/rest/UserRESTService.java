package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Address;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.DepartmentAssistant;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.SimpleUserView;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.UserRegistrationType;
import gr.grnet.dep.service.util.MailClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import javax.ejb.Stateless;
import javax.mail.MessagingException;
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
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserRESTService extends RESTService {

	@GET
	@JsonView({SimpleUserView.class})
	@SuppressWarnings("unchecked")
	public List<User> getAll(
		@HeaderParam(TOKEN_HEADER) String authToken,
		@QueryParam("username") String username,
		@QueryParam("firstname") String firstname,
		@QueryParam("lastname") String lastname,
		@QueryParam("status") String status,
		@QueryParam("role") String role,
		@QueryParam("roleStatus") String roleStatus,
		@QueryParam("manager") Long managerId) {

		getLoggedOn(authToken);

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct u from User u " +
			"left join fetch u.roles r " +
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
		if (managerId != null) {
			sb.append("and (u.id in (select ia.user from InstitutionAssistant ia where ia.manager.id = :managerId ) or u.id in (select da.user from DepartmentAssistant da where da.manager.id = :managerId )) ");
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
		if (managerId != null) {
			query = query.setParameter("managerId", managerId);
		}

		List<User> result = query.getResultList();
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
		return Response.status(200)
			.header(TOKEN_HEADER, u.getAuthToken())
			.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, Integer.MAX_VALUE, false))
			.entity(u)
			.build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(id)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			return (User) em.createQuery(
				"from User u left join fetch u.roles " +
					"where u.id=:id")
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

	}

	@POST
	@JsonView({DetailedUserView.class})
	public User create(@HeaderParam(TOKEN_HEADER) String authToken, User user) {
		try {
			User loggedOn = null;
			//1. Validate
			if (user.getRoles().isEmpty() || user.getRoles().size() > 1) {
				throw new RestException(Status.CONFLICT, "one.role.required");
			}
			Role firstRole = user.getRoles().iterator().next();
			switch (firstRole.getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					break;
				case PROFESSOR_FOREIGN:
					break;
				case INSTITUTION_MANAGER:
					break;
				case CANDIDATE:
					break;
				case INSTITUTION_ASSISTANT:
					loggedOn = getLoggedOn(authToken);
					if (!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					((InstitutionAssistant) firstRole).setManager(((InstitutionManager) loggedOn.getRole(RoleDiscriminator.INSTITUTION_MANAGER)));
					break;
				case DEPARTMENT_ASSISTANT:
					loggedOn = getLoggedOn(authToken);
					if (!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					((DepartmentAssistant) firstRole).setManager(((InstitutionManager) loggedOn.getRole(RoleDiscriminator.INSTITUTION_MANAGER)));
					break;
				case MINISTRY_MANAGER:
					break;
				case ADMINISTRATOR:
					throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			//2. Update
			user.setRegistrationType(UserRegistrationType.REGISTRATION_FORM);
			user.setRegistrationDate(new Date());
			user.setStatus(UserStatus.UNVERIFIED);
			user.setStatusDate(new Date());
			user.setPassword(User.encodePassword(user.getPassword()));
			user.setVerificationNumber(user.generateVerificationNumber());
			user.setRoles(new HashSet<Role>());
			em.persist(user);

			firstRole.setStatus(RoleStatus.CREATED);
			firstRole.setStatusDate(new Date());
			user.addRole(firstRole);

			em.flush(); //To catch the exception

			// Send Verification E-Mail
			try {
				MailClient.sendVerificationEmail(user);
			} catch (MessagingException e) {
				logger.log(Level.SEVERE, "Error sending verification email to user " + user.getUsername(), e);
			}

			return user;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.FORBIDDEN, "username.not.available");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, User user) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(id)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		try {

			// Copy User Fields
			if (user.getPassword() != null && !user.getPassword().isEmpty()) {
				existingUser.setPassword(User.encodePassword(user.getPassword()));
			}
			existingUser.setBasicInfo(user.getBasicInfo());
			existingUser.setBasicInfoLatin(user.getBasicInfoLatin());
			existingUser.setContactInfo(user.getContactInfo());

			em.flush();
			return existingUser;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		try {
			//Do Delete:
			em.remove(existingUser);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
					if (u.getPassword().equals(User.encodePassword(password))) {
						u.setAuthToken(u.generateAuthenticationToken());
						u = em.merge(u);
						return Response.status(200)
							.header(TOKEN_HEADER, u.getAuthToken())
							.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, Integer.MAX_VALUE, false))
							.entity(u)
							.build();
					} else {
						throw new RestException(Status.UNAUTHORIZED, "wrong.password");
					}
				default:
					throw new RestException(Status.UNAUTHORIZED, "account.status." + u.getStatus().toString().toLowerCase());
			}
		} catch (NoResultException e) {
			throw new RestException(Status.UNAUTHORIZED, "wrong.username");
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
			u.setStatus(UserStatus.CREATED);
			u.setStatusDate(new Date());

			ProfessorDomestic pd = new ProfessorDomestic();
			pd.setDepartment(department);
			pd.setInstitution(department.getInstitution());
			pd.setStatus(RoleStatus.CREATED);
			pd.setStatusDate(new Date());
			//TODO:  Get Rank from affiliation
			u.addRole(pd);
		}
		// 4. Persist User
		try {
			u = em.merge(u);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}

		return Response.status(200)
			.header(TOKEN_HEADER, u.getAuthToken())
			.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, Integer.MAX_VALUE, false))
			.location(nextURL)
			.entity(u)
			.build();
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
						throw new RestException(Status.FORBIDDEN, "wrong.verification");
					}
					// Verify
					u.setStatus(UserStatus.ACTIVE);
					u.setStatusDate(new Date());
					u.setVerificationNumber(null);

					em.flush();
					return u;
				default:
					throw new RestException(Status.FORBIDDEN, "account.status." + u.getStatus().toString().toLowerCase());
			}

		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.username");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/status")
	@JsonView({DetailedUserView.class})
	public User updateStatus(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, User requestUser) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
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
			return u;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

}
