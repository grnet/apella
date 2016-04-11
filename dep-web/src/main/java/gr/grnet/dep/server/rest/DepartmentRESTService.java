package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DepartmentService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
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
import java.util.List;

@Path("/department")
@Stateless
public class DepartmentRESTService extends RESTService {

	@EJB
	private DepartmentService departmentService;

	/**
	 * Returns all Departments
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Department> getAll() {

		// fetch departments
		Collection<Department> departments = departmentService.getAll();

		return departments;
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
			// fetch department
			Department department = departmentService.get(id);

			return department;

		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Department update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Department departmentToUpdate) {
		try {
			User loggedOn = getLoggedOn(authToken);

			if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// update department
			Department department = departmentService.update(id, departmentToUpdate);

			return department;
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}
}
