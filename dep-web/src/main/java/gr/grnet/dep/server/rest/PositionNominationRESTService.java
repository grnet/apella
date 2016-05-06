package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionNominationService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.PositionNomination.DetailedPositionNominationView;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.PositionNominationFile;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Collection;

@Path("/position/{id:[0-9][0-9]*}/nomination")
public class PositionNominationRESTService extends RESTService {

    @EJB
    private PositionNominationService positionNominationService;

    /**
     * Returns the nomination description of the specified position
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @GET
    @Path("/{nominationId:[0-9][0-9]*}")
    @JsonView({DetailedPositionNominationView.class})
    public PositionNomination get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get
            PositionNomination existingNomination = positionNominationService.get(positionId, nominationId, loggedOn);

            return existingNomination;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Update the nomination of the specified position
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param newNomination
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.register.member.id
     * @HTTP 409 X-Error-Code: wrong.position.nomination.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: nomination.missing.prosklisi.kosmitora
     * @HTTP 409 X-Error-Code: nomination.missing.praktiko.epilogis
     */
    @PUT
    @Path("/{nominationId:[0-9][0-9]*}")
    @JsonView({DetailedPositionNominationView.class})
    public PositionNomination update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId, @PathParam("nominationId") long nominationId, PositionNomination newNomination) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            PositionNomination existingNomination = positionNominationService.update(positionId, nominationId, newNomination, loggedOn);

            return existingNomination;
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

    /**********************
     * File Functions *****
     ***********************/

    /**
     * Returns the list of file descriptions associated with position
     * nomination
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @GET
    @Path("/{nominationId:[0-9]+}/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<PositionNominationFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            Collection<PositionNominationFile> files = positionNominationService.getFiles(positionId, nominationId, loggedOn);

            return files;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Return the file description of the file with given id and associated with
     * given position nomination
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public PositionNominationFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file
            PositionNominationFile file = positionNominationService.getFile(positionId, nominationId, fileId, loggedOn);

            if (file == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.file.id");
            }

            return file;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Return the file data of the file with given id and associated with
     * given position nomination
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param fileId
     * @param bodyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileBody fileBody = positionNominationService.getFileBody(positionId, nominationId, fileId, bodyId, loggedOn);

            if (fileBody == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.file.id");
            }

            return sendFileBody(fileBody);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Handles upload of a new file associated with given position nomination
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param request
     * @return
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 409 X-Error-Code: wrong.position.nomination.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: nomination.missing.committee.convergence.date
     */
    @POST
    @Path("/{nominationId:[0-9]+}/file")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileHeader file = positionNominationService.createFile(positionId, nominationId, request, loggedOn);

            return toJSON(file, SimpleFileHeaderView.class);
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

    /**
     * Handles upload that updates an existing file associated with position
     * nomination
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param fileId
     * @param request
     * @return
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.nomination.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: wrong.file.type
     */
    @POST
    @Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update file
            FileHeader file = positionNominationService.updateFile(positionId, nominationId, fileId, request, loggedOn);

            return toJSON(file, SimpleFileHeaderView.class);
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

    /**
     * Removes the specified file
     *
     * @param authToken
     * @param positionId
     * @param nominationId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.nomination.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.nomination.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @DELETE
    @Path("/{nominationId:[0-9]+}/file/{fileId:([0-9]+)?}")
    @JsonView({SimpleFileHeaderView.class})
    public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete
            positionNominationService.deleteFile(positionId, nominationId, fileId, loggedOn);

            return Response.noContent().build();
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
