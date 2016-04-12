package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCommittee;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;
import gr.grnet.dep.service.model.PositionEvaluation;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.PositionPhase;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.SearchData;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.User.UserView;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            throw new EJBException(e);
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }
}
