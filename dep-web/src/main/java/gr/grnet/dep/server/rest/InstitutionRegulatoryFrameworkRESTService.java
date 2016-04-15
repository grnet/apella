package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.InstitutionRegulatoryFrameworkService;
import gr.grnet.dep.service.InstitutionService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/institutionrf")
public class InstitutionRegulatoryFrameworkRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	private InstitutionRegulatoryFrameworkService regulatoryFrameworkService;

	@EJB
	private InstitutionService institutionService;

	/**
	 * Returns Regulatory Frameworks of all Institutions
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<InstitutionRegulatoryFramework> getAll() {
		// fetch
		Collection<InstitutionRegulatoryFramework> institutionRegulatoryFrameworks = regulatoryFrameworkService.getAll();
		return institutionRegulatoryFrameworks;
	}

	/**
	 * Returns Regulatory Framework with given ID
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9]+}")
	public InstitutionRegulatoryFramework get(@PathParam("id") long id) {
		try {
			// fetch
			InstitutionRegulatoryFramework institutionRegulatoryFramework = regulatoryFrameworkService.get(id);
			return institutionRegulatoryFramework;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Creates a Regulatory Framework, not finalized
	 *
	 * @param authToken
	 * @param newIRF
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.institution.id
	 * @HTTP 409 X-Error-Code: institutionrf.already.exists
	 */
	@POST
	public InstitutionRegulatoryFramework create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, InstitutionRegulatoryFramework newIRF) {
		try {
			// get logged on user
			User loggedOn = getLoggedOn(authToken);
			newIRF = regulatoryFrameworkService.create(newIRF, loggedOn);
			return newIRF;
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

	/**
	 * Updates and finalizes Regulatory Frameowrk with given ID
	 *
	 * @param authToken
	 * @param id
	 * @param newIRF
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.institutionregulatoryframework.id
	 */
	@PUT
	@Path("/{id:[0-9]+}")
	public InstitutionRegulatoryFramework update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, InstitutionRegulatoryFramework newIRF) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// update
			InstitutionRegulatoryFramework existing = regulatoryFrameworkService.update(id, newIRF, loggedOn);

			return existing;
		} catch (NotEnabledException e) {
			throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}

	/**
	 * Removes Regulatory Framework with given ID
	 *
	 * @param authToken
	 * @param id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.institutionregulatoryframework.id
	 */
	@DELETE
	@Path("/{id:[0-9]+}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		try {
			User loggedOn = getLoggedOn(authToken);

			// delete
			regulatoryFrameworkService.delete(id, loggedOn);
		} catch (NotEnabledException e) {
			throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}
}
