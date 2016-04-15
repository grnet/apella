package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DepartmentService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;

@Path("/department")
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
			// update department
			Department department = departmentService.update(id, departmentToUpdate, loggedOn);

			return department;
		} catch (NotEnabledException e) {
			throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}
}
