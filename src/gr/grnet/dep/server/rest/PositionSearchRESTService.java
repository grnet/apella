package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionSearchCriteria;
import gr.grnet.dep.service.model.PositionSearchCriteria.PositionSearchCriteriaView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/position/criteria")
@Stateless
public class PositionSearchRESTService extends RESTService {

	@Inject
	private Logger log;

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
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			PositionSearchCriteria criteria = fromJSON(PositionSearchCriteria.class, criteriaString);
			if (criteria == null) {
				throw new RestException(Status.BAD_REQUEST, "bad.criteria.format");
			}

			// Prepare Query
			Collection<Long> departmentIds = new ArrayList<Long>();
			departmentIds.add(-1L);
			for (Department department : criteria.getDepartments()) {
				departmentIds.add(department.getId());
			}
			Collection<Long> sectorIds = new ArrayList<Long>();
			sectorIds.add(-1L);
			for (Sector sector : criteria.getSectors()) {
				sectorIds.add(sector.getId());
			}
			String queryString = "from Position p " +
					"left join fetch p.assistants asts " +
					"left join fetch asts.roles astsr " +
					"where p.permanent = true " +
					"and p.phase.candidacies.closingDate >= :today ";
			if (departmentIds.size() > 1 || sectorIds.size() > 1) {
				queryString += "and (" +
						"	p.department.id in (:departmentIds) " +
						"	or p.sector.id in (:sectorIds) " +
						")";
			}
			TypedQuery<Position> query = em.createQuery(queryString, Position.class)
					.setParameter("today", today);
			if (departmentIds.size() > 1 || sectorIds.size() > 1) {
				query = query
						.setParameter("departmentIds", departmentIds)
						.setParameter("sectorIds", sectorIds);
			}

			// Execute Query
			List<Position> positions = query.getResultList();

			// Calculate CanSubmitCandidacy => set false if already submitted
			if (loggedOn.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
				Candidate candidate = (Candidate) loggedOn.getActiveRole(RoleDiscriminator.CANDIDATE);
				for (Candidacy candidacy : candidate.getCandidacies()) {
					if (candidacy.isPermanent()) {
						for (Position position : positions) {
							if (position.getId().equals(candidacy.getCandidacies().getPosition().getId())) {
								position.setCanSubmitCandidacy(Boolean.FALSE);
								break;
							}
						}
					}
				}
			}
			for (Position position : positions) {
				if (!position.getPhase().getClientStatus().equals(PositionStatus.ANOIXTI.toString())) {
					position.setCanSubmitCandidacy(Boolean.FALSE);
				}
			}

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
		try {
			Candidate candidate = (Candidate) loggedOnUser.getActiveRole(RoleDiscriminator.CANDIDATE);
			PositionSearchCriteria criteria = em.createQuery(
					"from PositionSearchCriteria c " +
							"where c.candidate.id = :candidateId ", PositionSearchCriteria.class)
					.setParameter("candidateId", candidate.getId())
					.getSingleResult();
			return criteria;
		} catch (NoResultException e) {
			PositionSearchCriteria criteria = new PositionSearchCriteria();
			return criteria;
		}
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
		User loggedOnUser = getLoggedOn(authToken);
		// Validate
		PositionSearchCriteria criteria = em.find(PositionSearchCriteria.class, id);
		if (criteria == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.search.criteria.id");
		}
		if (!criteria.getCandidate().getUser().getId().equals(loggedOnUser.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return result
		return criteria;
	}

	/**
	 * Creates search critera for logged user
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
			em.createQuery(
					"from PositionSearchCriteria c " +
							"where c.candidate.id = :candidateId ", PositionSearchCriteria.class)
					.setParameter("candidateId", candidate.getId())
					.getSingleResult();
			throw new RestException(Status.CONFLICT, "already.exists");
		} catch (NoResultException e) {
			Collection<Department> departments = utilityService.supplementDepartments(newCriteria.getDepartments());
			Collection<Sector> sectors = utilityService.supplementSectors(newCriteria.getSectors());

			newCriteria.setCandidate(candidate);
			newCriteria.getDepartments().clear();
			newCriteria.getDepartments().addAll(departments);
			newCriteria.getSectors().clear();
			newCriteria.getSectors().addAll(sectors);

			newCriteria = em.merge(newCriteria);
			em.flush();
			return newCriteria;
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
		PositionSearchCriteria criteria = em.find(PositionSearchCriteria.class, id);
		if (criteria == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.search.criteria.id");
		}
		if (!criteria.getCandidate().getUser().getId().equals(loggedOnUser.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Collection<Department> departments = utilityService.supplementDepartments(newCriteria.getDepartments());
		Collection<Sector> sectors = utilityService.supplementSectors(newCriteria.getSectors());

		criteria.getDepartments().clear();
		criteria.getDepartments().addAll(departments);
		criteria.getSectors().clear();
		criteria.getSectors().addAll(sectors);

		em.flush();

		return criteria;
	}
}
