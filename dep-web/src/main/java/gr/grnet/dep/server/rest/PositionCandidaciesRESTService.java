package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionCandidaciesService;
import gr.grnet.dep.service.PositionService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
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
import java.util.Set;

@Path("/position/{id:[0-9][0-9]*}/candidacies")
public class PositionCandidaciesRESTService extends RESTService {

    @EJB
    private PositionService positionService;

    @EJB
    private PositionCandidaciesService positionCandidaciesService;

    /*********************
     * Candidacies *********
     *********************/

    /**
     * Returns the list of Candidacies for this position
     *
     * @param authToken
     * @param positionId
     * @return
     * @HTTP 403 X-Error-Code insufficient.privileges
     * @HTTP 404 X-Error-Code wrong.position.id
     * @HTTP 409 X-Error-Code wrong.position.id
     */
    @GET
    @JsonView({CandidacyView.class})
    public Set<Candidacy> getPositionCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            Set<Candidacy> result = positionCandidaciesService.getPositionCandidacies(positionId, loggedOn);

            return result;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**********************************
     * Position Candidacies ***********
     **********************************/

    /**
     * Returns the specific PositionCandidacies info of given Position
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @returnWrapper gr.grnet.dep.service.model.PositionCandidacies
     * @HTTP 403 X-Error-Code insufficient.privileges
     * @HTTP 404 X-Error-Code wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code wrong.position.id
     */
    @GET
    @Path("/{candidaciesId:[0-9]+}")
    @JsonView({DetailedPositionCandidaciesView.class})
    public PositionCandidacies get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get
            PositionCandidacies existingCandidacies = positionCandidaciesService.get(positionId, candidaciesId, loggedOn);

            return existingCandidacies;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**********************
     * File Functions *****
     **********************/

    /**
     * Returns the list of file descriptions associated with position
     * candidacies
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @GET
    @Path("/{candidaciesId:[0-9]+}/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<PositionCandidaciesFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get files
            Collection<PositionCandidaciesFile> positionCandidaciesFiles = positionCandidaciesService.getFiles(positionId, candidaciesId, loggedOn);

            return positionCandidaciesFiles;
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
     * given position candidacies
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public PositionCandidaciesFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get files
            PositionCandidaciesFile file = positionCandidaciesService.getFile(positionId, candidaciesId, fileId, loggedOn);

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
     * given position candidacies
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @param fileId
     * @param bodyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file body
            FileBody fileBody = positionCandidaciesService.getFileBody(positionId, candidaciesId, fileId, bodyId, loggedOn);

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
     * Handles upload of a new file associated with given position candidacies
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @param request
     * @throws FileUploadException
     * @throws IOException
     * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 400 X-Error-Code: missing.candidacy.evaluator
     * @HTTP 400 X-Error-Code: wrong.candidacy.evaluator.id
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @POST
    @Path("/{candidaciesId:[0-9]+}/file")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create file
            FileHeader file = positionCandidaciesService.createFile(positionId, candidaciesId, request, loggedOn);

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
     * candidacies
     *
     * @param authToken
     * @param positionId
     * @param candidaciesId
     * @param fileId
     * @param request
     * @throws FileUploadException
     * @throws IOException
     * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 400 X-Error-Code: missing.candidacy.evaluator
     * @HTTP 400 X-Error-Code: wrong.candidacy.evaluator.id
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
     * @HTTP 409 X-Error-Code: wrong.file.type
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @POST
    @Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update file
            FileHeader file = positionCandidaciesService.updateFile(positionId, candidaciesId, fileId, request, loggedOn);

            return toJSON(file, FileHeader.SimpleFileHeaderView.class);
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
     * @param candidaciesId
     * @param positionId
     * @param fileId
     * @return
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @DELETE
    @Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("candidaciesId") Long candidaciesId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete file
            positionCandidaciesService.deleteFile(candidaciesId, positionId, fileId, loggedOn);

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
