package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.IdRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.SimpleUserView;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
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
import org.jboss.resteasy.spi.NoLogWebApplicationException;

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
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

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
		if (authToken == null) {
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
			loggedOn.getId() != id)
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		User user = (User) em.createQuery(
			"from User u left join fetch u.roles " +
				"where u.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		return user;
	}

	@POST
	@JsonView({DetailedUserView.class})
	public User create(User user) {
		Set<Role> roles = user.getRoles();

		user.setActive(false);
		user.setRegistrationDate(new Date());
		user.setVerified(Boolean.FALSE);
		user.setPassword(User.encodePassword(user.getPassword()));
		user.setVerificationNumber(System.currentTimeMillis());
		user.setRoles(new HashSet<Role>());
		em.persist(user);

		for (Role r : roles) {
			user.addRole(r);
		}

		return user;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, User user) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			loggedOn.getId() != id)
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		//TODO: 1. Validate changes:

		// 2. Copy User Fields
		if (user.getPassword() != null) {
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
			loggedOn.getId() != id)
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
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
				return Response.status(Status.UNAUTHORIZED).header("X-Error-Code", "wrong.password").build();
			}
		} catch (NoResultException e) {
			return Response.status(Status.UNAUTHORIZED).header("X-Error-Code", "wrong.username").build();
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
			if (u.getVerified() != null && u.getVerified()) {
				throw new NoLogWebApplicationException(Response.status(Status.FORBIDDEN).header("X-Error-Code", "already.verified").build());
			}
			if (user.getVerificationNumber() == null || !user.getVerificationNumber().equals(u.getVerificationNumber())) {
				throw new NoLogWebApplicationException(Response.status(Status.FORBIDDEN).header("X-Error-Code", "wrong.verification").build());
			}

			// Verify
			u.setVerified(Boolean.TRUE);
			u.setVerificationNumber(null);

			return u;
		} catch (NoResultException e) {
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).header("X-Error-Code", "wrong.username").build());
		}
	}
	
	@PUT
	@Path("/{id:[0-9][0-9]*}/activate")
	@JsonView({DetailedUserView.class})
	public User activate(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @QueryParam("active") Boolean active) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		
		try {
			User u = (User) em.createQuery(
				"from User u where u.id = :id")
				.setParameter("id", id)
				.getSingleResult();

			// Activate
			if (active==null) active=Boolean.TRUE;
			u.setActive(active);

			return u;
		} catch (NoResultException e) {
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).build());
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/roles")
	@JsonView({IdRoleView.class})
	public Set<Role> getRolesForUser(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			loggedOn.getId() != id)
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		User u = (User) em.createQuery(
			"from User u join fetch u.roles where u.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		return u.getRoles();
	}

}
