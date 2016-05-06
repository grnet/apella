package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.SearchData;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserView;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/position")
public class PositionRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private PositionService positionService;


    /**
     * Returns all positions
     *
     * @param authToken
     * @return A list of position
     */
    @GET
    @JsonView({PositionView.class})
    public SearchData<Position> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) {
        try {
            User loggedOnUser = getLoggedOn(authToken);
            // get all
            SearchData<Position> result = positionService.getAll(request, loggedOnUser);

            return result;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns the list of public positions
     *
     * @return A list of position
     */
    @GET
    @Path("/public")
    @JsonView({PublicPositionView.class})
    public Collection<Position> getPublic() {
        try {
            // get public positions
            Collection<Position> positions = positionService.getPublic();

            return positions;
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    /**
     * Return the full position description
     *
     * @param authToken
     * @param positionId
     * @param order      If specified a specific phase will be returned instead of
     *                   the last one
     * @returnWrapper gr.grnet.dep.service.model.Position
     */
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @QueryParam("order") Integer order) {
        try {
            // All logged in users can see the positions
            Position p = positionService.getPosition(positionId);
            String result = toJSON(order == null ? p : p.as(order), DetailedPositionView.class);

            return result;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


    /**
     * Creates a non-finalized position entry
     *
     * @param authToken
     * @param position
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.department.id
     */
    @POST
    @JsonView({DetailedPositionView.class})
    public Position create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Position position) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create
            position = positionService.create(position, loggedOn);

            return position;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Updates and finalizes the position
     *
     * @param authToken
     * @param id
     * @param position
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.sector.id
     */
    @PUT
    @Path("/{id:[0-9]+}")
    @JsonView({DetailedPositionView.class})
    public Position update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            position = positionService.update(id, position, loggedOn);

            return position;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Removes the position entry
     *
     * @param authToken
     * @param id
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 409 X-Error-Code: wrong.position.status.cannot.delete
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete
            positionService.delete(id, loggedOn);
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}/assistants")
    @JsonView({UserView.class})
    public Collection<User> getPositionAssistants(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get position assistants
            Collection<User> managers = positionService.getPositionAssistants(positionId, loggedOn);

            return managers;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Adds a new phase in the position
     *
     * @param authToken
     * @param positionId
     * @param position
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 409 X-Error-Code: position.phase.anoixti.wrong.closing.date
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: position.phase.epilogi.wrong.closing.date
     * @HTTP 409 X-Error-Code: position.phase.anapompi.missing.apofasi
     * @HTTP 409 X-Error-Code:
     * position.phase.stelexomeni.missing.nominated.candidacy
     * @HTTP 409 X-Error-Code:
     * position.phase.stelexomeni.missing.praktiko.epilogis
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}/phase")
    @JsonView({DetailedPositionView.class})
    public Position addPhase(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId, Position position) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // add phase
            position = positionService.addPhase(positionId, position, loggedOn);

            return position;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @GET
    @Path("/export")
    public Response getPositionsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get stream
            InputStream is = positionService.getPositionsExport(loggedOn);
            // Return response
            return Response.ok(is)
                    .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("charset", "UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("positions.xlsx", "UTF-8") + "\"")
                    .build();
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "getDocument", e);
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }
}
