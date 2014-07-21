package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
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
}
