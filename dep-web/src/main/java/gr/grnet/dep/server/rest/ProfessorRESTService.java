package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.PositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/professor")
@Stateless
public class ProfessorRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns committees that the professor participates
	 *
	 * @param authToken
	 * @param professorId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.professor.id
	 */
	@GET
	@Path("/{id:[0-9]+}/committees")
	@JsonView({PositionCommitteeMemberView.class})
	public Collection<PositionCommitteeMember> getCommittees(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
		User loggedOn = getLoggedOn(authToken);
		Professor professor = em.find(Professor.class, professorId);
		if (professor == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!professor.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<PositionCommitteeMember> committees = em.createQuery(
				"select pcm from PositionCommitteeMember pcm " +
						"left join fetch pcm.committee co " +
						"left join fetch co.position po " +
						"left join fetch po.phase ph " +
						"left join fetch ph.candidacies pca " +
						"where pcm.registerMember.professor.id = :professorId ", PositionCommitteeMember.class)
				.setParameter("professorId", professorId)
				.getResultList();
		return committees;
	}

	/**
	 * Returns professor's participations as evaluator in positions
	 *
	 * @param authToken
	 * @param professorId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.professor.id
	 */
	@GET
	@Path("/{id:[0-9]+}/evaluations/position")
	@JsonView({PositionEvaluatorView.class})
	public Collection<PositionEvaluator> getPositionEvaluations(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
		User loggedOn = getLoggedOn(authToken);
		Professor professor = em.find(Professor.class, professorId);
		if (professor == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!professor.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<PositionEvaluator> evaluations = em.createQuery(
				"select pe from PositionEvaluator pe " +
						"left join fetch pe.evaluation ev " +
						"left join fetch ev.position po " +
						"left join fetch po.phase ph " +
						"left join fetch ph.candidacies pca " +
						"where pe.registerMember.professor.id = :professorId ", PositionEvaluator.class)
				.setParameter("professorId", professorId)
				.getResultList();
		return evaluations;
	}

	/**
	 * Returns professor's participations as evaluator in candidacies
	 *
	 * @param authToken
	 * @param professorId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.professor.id
	 */
	@GET
	@Path("/{id:[0-9]+}/evaluations/candidacy")
	@JsonView({CandidacyEvaluator.DetailedCandidacyEvaluatorView.class})
	public Collection<CandidacyEvaluator> getCandidacyEvaluations(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
		User loggedOn = getLoggedOn(authToken);
		Professor professor = em.find(Professor.class, professorId);
		if (professor == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!professor.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<CandidacyEvaluator> evaluations = em.createQuery(
				"select ce from CandidacyEvaluator ce " +
						"join ce.candidacy c " +
						"join c.candidacies.position po " +
						"join po.phase ph " +
						"join ph.candidacies pca " +
						"where ce.registerMember.professor.id = :professorId ", CandidacyEvaluator.class)
				.setParameter("professorId", professorId)
				.getResultList();
		return evaluations;
	}

}
