package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.InstitutionService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.School;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
		try {
			User loggedOn = getLoggedOn(authToken);
			// update
			Institution institution = institutionService.update(id, institutionToUpdate, loggedOn);
			return institution;
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

	@GET
	@Path("/{id:[0-9][0-9]*}/schools")
	public Collection<School> getSchools(@PathParam("id") long id) {
		// fetch schools
		Collection<School> schools = institutionService.getSchools(id);

		return schools;
	}
}
