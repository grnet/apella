package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;

@Path("/department")
@Stateless
public class DepartmentRESTService extends RESTService {

	/**
	 * Returns all Departments
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Department> getAll() {
		return em.createQuery(
				"select distinct d from Department d ", Department.class)
				.getResultList();
	}

	/**
	 * Returns Department with given id
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Department get(@PathParam("id") long id) {
		try {
			return em.createQuery(
					"select d from Department d " +
							"where d.id = :id", Department.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.department.id");
		}
	}
}
