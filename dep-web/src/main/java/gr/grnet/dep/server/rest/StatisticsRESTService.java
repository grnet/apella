package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DataExportService;
import gr.grnet.dep.service.dto.Statistics;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/statistics")
@Stateless
public class StatisticsRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	DataExportService dataExportService;

	@GET
	public Response getStatistics(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Statistics stats = dataExportService.getStatistics();
		return Response.ok(stats).build();
	}
}
