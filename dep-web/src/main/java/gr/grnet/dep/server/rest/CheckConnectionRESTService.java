package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.service.CheckConnectionService;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * a rest call for checking connection
 */

@Path("/check")
public class CheckConnectionRESTService extends RESTService {

    @EJB
    private CheckConnectionService checkConnectionService;

    @GET
    public Response testConnection(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {

        // check connection
        boolean ping = checkConnectionService.testConnection();

        if (ping) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }

}
