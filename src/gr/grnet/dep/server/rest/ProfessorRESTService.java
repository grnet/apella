package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.ProfessorCommitteesView;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/professor")
@Stateless
public class ProfessorRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/{id:[0-9]+}/committees")
	@JsonView({ProfessorCommitteesView.class})
	public Collection<PositionCommitteeMember> getCommittees(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
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
				"where pcm.registerMember.professor.id = :professorId " +
				"and pcm.registerMember.deleted = false")
			.setParameter("professorId", professorId)
			.getResultList();
		return committees;
	}

	@GET
	@Path("/{id:[0-9]+}/evaluations")
	@JsonView({PositionEvaluatorView.class})
	public Collection<PositionEvaluator> getEvaluations(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
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
				"where pe.registerMember.professor.id = :professorId ")
			.setParameter("professorId", professorId)
			.getResultList();
		return evaluations;
	}

}
