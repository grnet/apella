package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.SimpleUserView;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
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
	public List<User> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient-priviledges");
		}
		return (List<User>) em.createQuery(
			"select distinct u from User u " +
				"left join fetch u.roles " +
				"order by u.basicInfo.lastname, u.basicInfo.firstname")
			.getResultList();
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
	public User get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			loggedOn.getId() != id) {
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
	public User create(User user) {
		try {

			Set<Role> roles = user.getRoles();

			user.setRegistrationDate(new Date());
			user.setStatus(UserStatus.UNVERIFIED);
			user.setStatusDate(new Date());
			user.setPassword(User.encodePassword(user.getPassword()));
			user.setVerificationNumber(System.currentTimeMillis());
			user.setRoles(new HashSet<Role>());
			em.persist(user);
			for (Role r : roles) {
				user.addRole(r);
			}
			em.flush(); //To catch the exception
			return user;
		} catch (PersistenceException e) {
			throw new RestException(Status.FORBIDDEN, "username.not.available");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, User user) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			loggedOn.getId() != id) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

		// Copy User Fields
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			existingUser.setPassword(User.encodePassword(user.getPassword()));
		}
		existingUser.setBasicInfo(user.getBasicInfo());
		existingUser.setContactInfo(user.getContactInfo());

		return existingUser;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			loggedOn.getId() != id) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		//TODO: Validate:

		//Do Delete:
		em.remove(existingUser);
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
					"where u.active=true and u.verified=true and u.username = :username")
				.setParameter("username", username)
				.getSingleResult();

			if (u.getPassword().equals(User.encodePassword(password))) {
				//TODO: Create Token: 
				u.setAuthToken("" + System.currentTimeMillis());
				u = em.merge(u);

				return Response.status(200)
					.header(TOKEN_HEADER, u.getAuthToken())
					.cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, Integer.MAX_VALUE, false))
					.entity(u)
					.build();
			} else {
				throw new RestException(Status.UNAUTHORIZED, "wrong.password");
			}
		} catch (NoResultException e) {
			throw new RestException(Status.UNAUTHORIZED, "wrong.username");
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
					"where u.active=true and u.username = :username")
				.setParameter("username", user.getUsername())
				.getSingleResult();
			// Validate
			switch (u.getStatus()) {
				case CREATED:
					throw new RestException(Status.FORBIDDEN, "account.status.created");
				case UNVERIFIED:
					if (user.getVerificationNumber() == null || !user.getVerificationNumber().equals(u.getVerificationNumber())) {
						throw new RestException(Status.FORBIDDEN, "wrong.verification");
					}
					// Verify
					u.setStatus(UserStatus.ACTIVE);
					u.setStatusDate(new Date());
				case ACTIVE:
					throw new RestException(Status.FORBIDDEN, "account.status.active");
				case BLOCKED:
					throw new RestException(Status.FORBIDDEN, "account.status.blocked");
				case DELETED:
					throw new RestException(Status.FORBIDDEN, "account.status.deleted");
			}
			return u;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.username");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/status")
	@JsonView({DetailedUserView.class})
	public User updateStatus(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @QueryParam("status") String status) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			User u = (User) em.createQuery(
				"from User u where u.id = :id")
				.setParameter("id", id)
				.getSingleResult();
			u.setStatus(UserStatus.valueOf(status));
			return u;
		} catch (IllegalArgumentException e) {
			throw new RestException(Status.BAD_REQUEST);
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
	}

}
