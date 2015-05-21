package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * a rest call for checking connection
 */

@Path("/check")
@Stateless
public class CheckConnectionRESTService extends RESTService {

    @GET
    public Response testConnection(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
        try {
            em.createQuery(
                    "Select a.id from Administrator a ", Long.class)
                    .setMaxResults(1)
                    .getSingleResult();

            return Response.ok().build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
