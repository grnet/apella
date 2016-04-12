package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.InstitutionRegulatoryFrameworkService;
import gr.grnet.dep.service.InstitutionService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
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
			// get institution
			Institution institution = institutionService.getInstitution(newIRF.getInstitution().getId());
			newIRF.setInstitution(institution);
			// validation
			if (!newIRF.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// create
			newIRF = regulatoryFrameworkService.create(newIRF);
			return newIRF;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
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
			// get
			InstitutionRegulatoryFramework existing = regulatoryFrameworkService.get(id);

			if (!existing.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// update
			existing = regulatoryFrameworkService.update(id, existing);

			return existing;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
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
			InstitutionRegulatoryFramework existing = regulatoryFrameworkService.get(id);

			if (!existing.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// delete
			regulatoryFrameworkService.delete(id);
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}



	}
}
