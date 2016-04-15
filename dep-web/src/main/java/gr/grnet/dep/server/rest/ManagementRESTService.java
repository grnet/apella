package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.SubjectService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/management")
public class ManagementRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	ManagementService mgmtService;

	@EJB
	SubjectService subjectService;

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
		List<Long> allSubjectIds = subjectService.getAllSubjectIds();
		for (Long subjectId : allSubjectIds) {
			mgmtService.upperCaseSubject(subjectId);
		}
		return "OK";
	}


	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/massSendEvaluationEmails")
	public String massSendEvaluationEmails(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		mgmtService.massSendEvaluationEmails();
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
		try {
			User loggedOn = getLoggedOn(authToken);
			// create candidacy
			Candidacy candidacy = mgmtService.createCandidacy(positionId, candidateId, loggedOn);

			return candidacy;
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

	@DELETE
	@Path("/candidacy/{id:[0-9][0-9]*}")
	public void deleteCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// delete candidacy
			mgmtService.deleteCandidacy(id, loggedOn);
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
}
