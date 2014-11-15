package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileHeader;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/management")
@Stateless
public class ManagementRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	ManagementService mgmtService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massCreateProfessorDomesticAccounts")
	public String massCreateProfessorDomesticAccounts(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		mgmtService.massCreateProfessorDomesticAccounts();

		return "OK";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massSendLoginEmails")
	public String massSendLoginEmails(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		mgmtService.massSendLoginEmails();
		return "OK";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massSendReminderLoginEmails")
	public String massSendReminderLoginEmails(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		mgmtService.massSendReminderLoginEmails();
		return "OK";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massSendReminderFinalizeRegistrationEmails")
	public String massSendReminderFinalizeRegistrationEmails(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		mgmtService.massSendReminderFinalizeRegistrationEmails();
		return "OK";
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massSendShibbolethConnectEmails")
	public String massSendShibbolethConnectEmails(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Collection<Long> institutionIds) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		mgmtService.massSendShibbolethConnectEmails(institutionIds);
		return "OK";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/uppercaseSubjects")
	public String uppercaseSubjects(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<Long> allSubjectIds = em.createQuery("select s.id from Subject s", Long.class).getResultList();
		for (Long subjectId : allSubjectIds) {
			mgmtService.upperCaseSubject(subjectId);
		}
		return "OK";
	}


	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@JsonView({Candidacy.DetailedCandidacyView.class})
	@Path("/candidacy")
	public Candidacy createCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken,
									 @FormParam("position") Long positionId,
									 @FormParam("candidate") Long candidateId) {
		User loggedOn = getLoggedOn(authToken);
		// Authenticate
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// Validate
			Candidate candidate = em.find(Candidate.class, candidateId);
			if (candidate == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
			}
			Position position = em.find(Position.class, positionId);
			if (position == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			try {
				Candidacy existingCandidacy = em.createQuery(
						"select c from Candidacy c " +
								"where c.candidate.id = :candidateId " +
								"and c.candidacies.position.id = :positionId", Candidacy.class)
						.setParameter("candidateId", candidate.getId())
						.setParameter("positionId", position.getId())
						.getSingleResult();
				// Return Results
				existingCandidacy.getCandidacyEvalutionsDueDate();
				return existingCandidacy;
			} catch (NoResultException e) {
			}
			// Create
			Candidacy candidacy = new Candidacy();
			candidacy.setCandidate(candidate);
			candidacy.setCandidacies(position.getPhase().getCandidacies());
			candidacy.setDate(new Date());
			candidacy.setOpenToOtherCandidates(false);
			candidacy.setPermanent(false);
			candidacy.getProposedEvaluators().clear();
			validateCandidacy(candidacy, candidate, true); // isNew
			updateSnapshot(candidacy, candidate);

			em.persist(candidacy);
			em.flush();

			// Return Results
			candidacy = em.find(Candidacy.class, candidacy.getId());
			candidacy.getCandidacyEvalutionsDueDate();
			return candidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/candidacy/{id:[0-9][0-9]*}")
	public void deleteCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		// Authenticate
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// Validate
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Response.Status.NOT_FOUND, "wrong.candidacy.id");
			}
			// Update
			for (FileHeader fh : existingCandidacy.getFiles()) {
				deleteCompletely(fh);
			}
			for (CandidacyEvaluator eval : existingCandidacy.getProposedEvaluators()) {
				for (FileHeader fh : eval.getFiles()) {
					deleteCompletely(fh);
				}
			}
			existingCandidacy.getCandidacies().getCandidacies().remove(existingCandidacy);
			em.remove(existingCandidacy);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
