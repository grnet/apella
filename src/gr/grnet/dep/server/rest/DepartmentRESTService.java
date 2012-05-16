package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Department;

import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/department")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class DepartmentRESTService extends RESTService {

	@GET
	@SuppressWarnings("unchecked")
	public List<Department> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		return (List<Department>) em.createQuery(
			"select distinct d from Department d ")
			.getResultList();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Department get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		return (Department) em.createQuery(
			"select d from Department d " +
				"where d.id = :id")
			.setParameter("id", id)
			.getSingleResult();
	}
}
