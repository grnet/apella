package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.InstitutionService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.School;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/institution")
public class InstitutionRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	private InstitutionService institutionService;

	/**
	 * Returns all Institutions
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Institution> getAll() {
		// fetch all institution
		Collection<Institution> institutions = institutionService.getAll();
		return institutions;
	}

	/**
	 * Returns Institution with given ID
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Institution get(@PathParam("id") long id) {
		try {
			// fetch institution
			Institution institution = institutionService.getInstitution(id);
			return institution;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

    @PUT
	@Path("/{id:[0-9][0-9]*}")
	public Institution update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Institution institutionToUpdate) {

		User loggedOn = getLoggedOn(authToken);

		if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// update
			Institution institution = institutionService.update(id, institutionToUpdate);
			return institution;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/schools")
	public Collection<School> getSchools(@PathParam("id") long id) {
		// fetch schools
		Collection<School> schools = institutionService.getSchools(id);

		return schools;
	}
}
