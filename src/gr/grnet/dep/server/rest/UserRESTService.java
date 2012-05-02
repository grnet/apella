package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.IdRoleView;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.SimpleUserView;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/user")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserRESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@SuppressWarnings("unused")
	@Inject
	private Logger log;

	@Context
	UriInfo uriInfo;

	@GET
	@JsonView({SimpleUserView.class})
	@SuppressWarnings("unchecked")
	public List<User> getAll() {
		return (List<User>) em.createQuery(
			"select distinct u from User u " +
				"join fetch u.roles " +
				"where u.active=true " +
				"order by u.basicInfo.lastname, u.basicInfo.firstname")
			.getResultList();
	}

	@GET
	@Path("/loggedon")
	@JsonView({DetailedUserView.class})
	public Response getLoggedOn(@HeaderParam("X-Auth-token") String authToken, @CookieParam("_dep_a") String cookieAuthToken) {
		if (authToken != null || cookieAuthToken != null) {
			if (authToken == null) {
				authToken = cookieAuthToken;
			}
			User u = (User) em.createQuery(
				"from User u " +
					"join fetch u.roles " +
					"where u.authToken = :authToken")
				.setParameter("authToken", authToken)
				.getSingleResult();
			return Response.status(200)
				.header("X-Auth-Token", u.getAuthToken())
				.entity(u)
				.build();
		}
		return Response.status(Status.UNAUTHORIZED).build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User get(@PathParam("id") long id) {
		User user = (User) em.createQuery(
			"from User u join fetch u.roles " +
				"where u.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		return user;
	}

	@POST
	@JsonView({DetailedUserView.class})
	public User create(User user) {
		user.setActive(Boolean.FALSE);
		user.setRegistrationDate(new Date());
		user.setVerified(Boolean.FALSE);
		// TODO: Create a verification generation algorithm
		user.setVerificationNumber(System.currentTimeMillis());
		em.persist(user);
		return user;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User update(@PathParam("id") long id, User user) {
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		//TODO: 1. Validate changes:

		// 2. Copy User Fields
		if (user.getPassword() != null) {
			existingUser.setPassword(User.encodePassword(user.getPassword()));
		}
		existingUser.setBasicInfo(user.getBasicInfo());
		existingUser.setContactInfo(user.getContactInfo());

		return get(existingUser.getId());
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@PathParam("id") long id) {
		User existingUser = em.find(User.class, id);
		if (existingUser == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
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
					"where u.username = :username")
				.setParameter("username", username)
				.getSingleResult();

			if (u.getPassword().equals(User.encodePassword(password))) {
				//TODO: Create Token: 
				u.setAuthToken("" + System.currentTimeMillis());
				u = em.merge(u);

				return Response.status(200)
					.header("X-Auth-Token", u.getAuthToken())
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
					"join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", user.getUsername())
				.getSingleResult();
			// Validate
			if (u.getVerified() != null && u.getVerified()) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).header("X-Error-Code", "already.verified").build());
			}
			if (user.getVerificationNumber() == null || !user.getVerificationNumber().equals(u.getVerificationNumber())) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).header("X-Error-Code", "wrong.verification").build());
			}

			// Verify
			u.setVerified(Boolean.TRUE);
			u.setVerificationNumber(null);

			return u;
		} catch (NoResultException e) {
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).header("X-Error-Code", "wrong.username").build());
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/roles")
	@JsonView({IdRoleView.class})
	public Set<Role> getRolesForUser(@PathParam("id") long id) {
		User u = (User) em.createQuery(
			"from User u join fetch u.roles where u.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		return u.getRoles();
	}

}
