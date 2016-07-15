package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionSearchService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionSearchCriteria;
import gr.grnet.dep.service.model.PositionSearchCriteria.PositionSearchCriteriaView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

@Path("/position/criteria")
public class PositionSearchRESTService extends RESTService {

	@EJB
	private PositionSearchService positionSearchService;

	/**
	 * Searches for Positions given specific criteria
	 *
	 * @param authToken      The authentication token
	 * @param criteriaString A JSON representation of query parameters
	 * @return
	 * @HTTP 400 X-Error-Code: bad.criteria.format
	 */
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@JsonView({PublicPositionView.class})
	public Collection<Position> search(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @FormParam("criteria") String criteriaString) {
		User loggedOn = getLoggedOn(authToken);
		try {
			PositionSearchCriteria criteria = fromJSON(PositionSearchCriteria.class, criteriaString);

			if (criteria == null) {
				throw new RestException(Status.BAD_REQUEST, "bad.criteria.format");
			}
			// get positions
			List<Position> positions = positionSearchService.search(criteria, loggedOn);
			// Return Result
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}
	}

	/**
	 * Returns saved search criteria of logged on User
	 *
	 * @param authToken
	 * @return
	 */
	@GET
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOnUser = getLoggedOn(authToken);
		if (!loggedOnUser.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// cast to candidate
		Candidate candidate = (Candidate) loggedOnUser.getActiveRole(RoleDiscriminator.CANDIDATE);
		// get the criteria
		PositionSearchCriteria criteria = positionSearchService.getByCandidate(candidate.getId());
		return criteria;
	}

	/**
	 * Return saved search criteria of User with given id
	 *
	 * @param authToken
	 * @param id
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.search.criteria.id
	 */
	@GET
	@Path("/{id:[0-9]+}")
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		try {
			User loggedOnUser = getLoggedOn(authToken);
			// Validate
			PositionSearchCriteria criteria = positionSearchService.get(id);

			if (!criteria.getCandidate().getUser().getId().equals(loggedOnUser.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Return result
			return criteria;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Creates search criteria for logged user
	 *
	 * @param authToken
	 * @param newCriteria
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 409 X-Error-Code: already.exists
	 */
	@POST
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, PositionSearchCriteria newCriteria) {
		User loggedOnUser = getLoggedOn(authToken);
		// Validate
		if (!loggedOnUser.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		Candidate candidate = (Candidate) loggedOnUser.getActiveRole(RoleDiscriminator.CANDIDATE);
		try {
			// create
			newCriteria = positionSearchService.create(newCriteria, candidate.getId());

			return newCriteria;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (ValidationException e) {
			throw new RestException(Status.CONFLICT, e.getMessage());
		}
	}

	/**
	 * Update search criteria of logged user with given criteria id
	 *
	 * @param authToken   The authentication token
	 * @param id
	 * @param newCriteria
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.search.criteria.id
	 */
	@PUT
	@Path("/{id:[0-9]+}")
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, PositionSearchCriteria newCriteria) {
		User loggedOnUser = getLoggedOn(authToken);
		// Validate
		try {
			PositionSearchCriteria criteria = positionSearchService.get(id);

			if (!criteria.getCandidate().getUser().getId().equals(loggedOnUser.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// update
			criteria = positionSearchService.update(id, newCriteria);

			return criteria;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
	}
}
