package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

@Path("/jira")
@Stateless
public class JiraRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/user/{id:[0-9]+}/issue")
	public List<JiraIssue> getUserIssues(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long userId) {
		final User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.getId().equals(userId)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<JiraIssue> issues = jiraService.getUserIssues(userId);

		return issues;

	}

	@GET
	@Path("/user/{userId:[0-9]+}/issue/{issueId:[0-9]+}")
	public JiraIssue getUserIssue(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("userId") long userId, @PathParam("issueId") long issueId) {
		final User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.getId().equals(userId)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			JiraIssue issue = jiraService.getIssue(issueId);
			if (issue.getUser() == null || !issue.getUser().getId().equals(userId)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			return issue;
		} catch (ServiceException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.issue.id");
		}
	}

	@POST
	@Path("/user/{id:[0-9]+}/issue")
	public JiraIssue createUserIssue(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long userId, JiraIssue issue) {
		final User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.getId().equals(userId)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			issue.setUser(loggedOn);
			issue.setReporter(null);
			JiraIssue createdIssue = jiraService.createIssue(issue);

			return createdIssue;
		} catch (ServiceException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getErrorKey());
		}
	}

	@POST
	@Path("/admin")
	public JiraIssue createAdministratorIssue(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, JiraIssue issue) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			issue.setReporter(loggedOn.getUsername());
			JiraIssue createdIssue = jiraService.createRemoteIssue(issue);
			return createdIssue;
		} catch (ServiceException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getErrorKey());
		}
	}

	@POST
	@Path("/public")
	public JiraIssue createPublicIssue(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, JiraIssue issue) {
		try {
			issue.setReporter(null);
			JiraIssue createdIssue = jiraService.createRemoteIssue(issue);
			return createdIssue;
		} catch (ServiceException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getErrorKey());
		}
	}
}
