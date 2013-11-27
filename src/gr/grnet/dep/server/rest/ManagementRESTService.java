package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

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
}
