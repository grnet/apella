package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.DetailedUserView;
import gr.grnet.dep.service.model.User.SimpleUserView;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.annotate.JsonView;

/**
 * JAX-RS Example This class produces a RESTful service to read the contents of
 * the users table.
 */
@Path("/users")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserRESTService {

	// @PersistenceContext(unitName = "depdb", type = PersistenceContextType.EXTENDED)
	@PersistenceContext(unitName="depdb")
	private EntityManager em;

	@Inject
	private Logger log;

	@SuppressWarnings("unchecked")
	@GET
	@JsonView({SimpleUserView.class})
	public List<User> listAllUsers() {
		Query qry =	em.createQuery("select distinct u from User u join fetch u.roles where u.active=true " +
					"order by u.basicInfo.lastname, u.basicInfo.firstname");
		return qry.getResultList();
		// return service.getUsers();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedUserView.class})
	public User getUserById(@PathParam("id") long id) throws JsonGenerationException, JsonMappingException, IOException {
		Query qry =	em.createQuery("from User u join fetch u.roles where u.id=:id")
					.setParameter("id", id);
		return (User) qry.getSingleResult();
	}
}
