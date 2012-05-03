package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/role")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RoleRESTService extends RESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@SuppressWarnings("unused")
	@Inject
	private Logger log;


	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRoleView.class})
	public Role get(@PathParam("id") long id) {
		User loggedOn = getLoggedOn();
		boolean roleBelongsToUser = false;
		for (Role r: loggedOn.getRoles()) {
			if (r.getId()==id) {
				roleBelongsToUser = true;
				break;
			}
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& !roleBelongsToUser)
			throw new WebApplicationException(Status.FORBIDDEN);
		
		Role r = (Role) em.createQuery(
			"from Role r where r.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		r.initializeCollections();
		return r;
	}

	
	@POST
	@JsonView({DetailedRoleView.class})
	public Role create(Role role) {
		User loggedOn = getLoggedOn();
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& role.getUser()!=loggedOn.getId())
			throw new WebApplicationException(Status.FORBIDDEN);
		
		em.persist(role);
		return role;
	}
	
	
	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRoleView.class})
	public Role update(@PathParam("id") long id, Role role) {
		Role existingRole = em.find(Role.class, id);
		if (existingRole == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	
		User loggedOn = getLoggedOn();
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& existingRole.getUser()!=loggedOn.getId())
			throw new WebApplicationException(Status.FORBIDDEN);
		
		Role r = existingRole.copyFrom(role);
		
		return get(r.getId());
	}
	
	
	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@PathParam("id") long id) {
		Role existingRole = em.find(Role.class, id);
		if (existingRole == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		User loggedOn = getLoggedOn();
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& existingRole.getUser()!=loggedOn.getId())
			throw new WebApplicationException(Status.FORBIDDEN);

		//Do Delete:
		em.remove(existingRole);
	}
	
}
