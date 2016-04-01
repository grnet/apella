package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
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
				"select distinct d from Department d " +
						"left join fetch d.name dname " +
						"left join fetch d.school s " +
						"left join fetch s.name sname " +
						"left join fetch s.institution i " +
						"left join fetch i.name iname ", Department.class)
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
					"select distinct d from Department d " +
							"left join fetch d.name dname " +
							"left join fetch d.school s " +
							"left join fetch s.name sname " +
							"left join fetch s.institution i " +
							"left join fetch i.name iname " +
							"where d.id = :id", Department.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.department.id");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Department update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Department departmentToUpdate) {

		User loggedOn = getLoggedOn(authToken);

		if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		try {
			Department department = em.find(Department.class, id);

			if (department == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.department.id");
			}

			if (departmentToUpdate.getName() == null ||
					StringUtils.isEmpty(departmentToUpdate.getName().get("el")) || StringUtils.isEmpty(departmentToUpdate.getName().get("en"))) {
				throw new RestException(Status.CONFLICT, "names.missing");
			}

			department.setName(departmentToUpdate.getName());

			return department;

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
