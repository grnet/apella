package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ProfessorService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.PositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
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
public class ProfessorRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	private ProfessorService professorService;

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
		try {
			// get professor
			Professor professor = professorService.getProfessor(professorId);
			// validate
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!professor.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// get committees
			List<PositionCommitteeMember> committees = professorService.getCommittees(professor.getId());

			return committees;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
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
		try {
			// get professor
			Professor professor = professorService.getProfessor(professorId);

			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!professor.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			List<PositionEvaluator> evaluations = professorService.getEvaluations(professor.getId());
			return evaluations;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}
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
		try {
		// get professor
		Professor professor = professorService.getProfessor(professorId);

		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!professor.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<CandidacyEvaluator> evaluations = professorService.getCandidacyEvaluators(professor.getId());

			return evaluations;

		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		}

	}

}
