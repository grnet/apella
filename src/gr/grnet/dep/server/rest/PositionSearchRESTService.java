package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionSearchCriteria;
import gr.grnet.dep.service.model.PositionSearchCriteria.PositionSearchCriteriaView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/position/criteria")
@Stateless
public class PositionSearchRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/search")
	@JsonView({PublicPositionView.class})
	public List<Position> search(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("criteria") String criteriaString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			PositionSearchCriteria criteria = fromJSON(PositionSearchCriteria.class, criteriaString);
			if (criteria == null) {
				throw new RestException(Status.BAD_REQUEST, "bad.criteria.format");
			}

			// Prepare Query
			String queryString = "from Position p " +
				"where p.permanent = true " +
				"and p.phase.candidacies.closingDate >= :today ";
			if (!criteria.getDepartments().isEmpty()) {
				queryString += "and p.department.id in (:departmentIds) ";
			}
			if (!criteria.getSubjects().isEmpty()) {
				queryString += "and p.subject.name in (:subjects) ";
			}

			Query query = em.createQuery(queryString)
				.setParameter("today", today);

			if (!criteria.getDepartments().isEmpty()) {
				Collection<Long> departmentIds = new ArrayList<Long>();
				for (Department department : criteria.getDepartments()) {
					departmentIds.add(department.getId());
				}
				query = query.setParameter("departmentIds", departmentIds);
			}
			if (!criteria.getSubjects().isEmpty()) {
				Collection<String> subjects = new ArrayList<String>();
				for (Subject subject : criteria.getSubjects()) {
					subjects.add(subject.getName());
				}
				query = query.setParameter("subjects", subjects);
			}

			// Execute Query
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) query.getResultList();

			// Return Result
			for (Position position : positions) {
				position.initializeCollections();
			}
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}
	}

	@GET
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria get(@HeaderParam(TOKEN_HEADER) String authToken) {
		User loggedOnUser = getLoggedOn(authToken);
		if (!loggedOnUser.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			Candidate candidate = (Candidate) loggedOnUser.getActiveRole(RoleDiscriminator.CANDIDATE);
			PositionSearchCriteria criteria = (PositionSearchCriteria) em.createQuery(
				"from PositionSearchCriteria c " +
					"where c.candidate.id = :candidateId ")
				.setParameter("candidateId", candidate.getId())
				.getSingleResult();
			criteria.initializeCollections();
			return criteria;
		} catch (NoResultException e) {
			PositionSearchCriteria criteria = new PositionSearchCriteria();
			return criteria;
		}
	}

	@GET
	@Path("/{id:[0-9]+}")
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
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

	@POST
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria create(@HeaderParam(TOKEN_HEADER) String authToken, PositionSearchCriteria newCriteria) {
		User loggedOnUser = getLoggedOn(authToken);
		// Validate
		if (!loggedOnUser.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Candidate candidate = (Candidate) loggedOnUser.getActiveRole(RoleDiscriminator.CANDIDATE);
		try {
			em.createQuery(
				"from PositionSearchCriteria c " +
					"where c.candidate.id = :candidateId ")
				.setParameter("candidateId", candidate.getId())
				.getSingleResult();
			throw new RestException(Status.CONFLICT, "already.exists");
		} catch (NoResultException e) {
			Collection<Department> departments = supplementDepartments(newCriteria.getDepartments());
			Collection<Subject> subjects = supplementSubjects(newCriteria.getSubjects());

			newCriteria.setCandidate(candidate);
			newCriteria.getDepartments().clear();
			newCriteria.getDepartments().addAll(departments);
			newCriteria.getSubjects().clear();
			newCriteria.getSubjects().addAll(subjects);

			newCriteria = em.merge(newCriteria);
			em.flush();
			return newCriteria;
		}
	}

	@PUT
	@Path("/{id:[0-9]+}")
	@JsonView({PositionSearchCriteriaView.class})
	public PositionSearchCriteria update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, PositionSearchCriteria newCriteria) {
		User loggedOnUser = getLoggedOn(authToken);
		// Validate
		PositionSearchCriteria criteria = em.find(PositionSearchCriteria.class, id);
		if (criteria == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.search.criteria.id");
		}
		if (!criteria.getCandidate().getUser().getId().equals(loggedOnUser.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Collection<Department> departments = supplementDepartments(newCriteria.getDepartments());
		Collection<Subject> subjects = supplementSubjects(newCriteria.getSubjects());

		criteria.getDepartments().clear();
		criteria.getDepartments().addAll(departments);
		criteria.getSubjects().clear();
		criteria.getSubjects().addAll(subjects);

		em.flush();

		return criteria;
	}
}