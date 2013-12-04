package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;

@Path("/jira")
@Stateless
public class JiraRESTService extends RESTService {

	@Inject
	private Logger log;

	@POST
	public JiraIssue create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, JiraIssue issue) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		JiraIssue createdIssue = jiraService.createIssue(issue);
		if (createdIssue == null) {
			throw new RestException(Status.BAD_REQUEST, "bad.request");
		}
		return createdIssue;
	}
}
