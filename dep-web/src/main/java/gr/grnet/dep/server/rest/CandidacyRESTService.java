package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.CandidacyService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.file.*;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import org.apache.commons.fileupload.FileItem;
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
import java.util.List;
import java.util.Map;

@Path("/candidacy")
public class CandidacyRESTService extends RESTService {

    @EJB
    private CandidacyService candidacyService;

    /**
     * Get Candidacy by it's ID
     *
     * @param authToken The Authentication Token
     * @param id        The Id of the Candidacy
     * @returnWrapped gr.grnet.dep.service.model.Candidacy
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     */
    @GET
    @Path("/{id:[0-9]+}")
    public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get map
            Map<Candidacy, Class> candidacyMap = candidacyService.get(id, loggedOn);
            Map.Entry<Candidacy, Class> entry = candidacyMap.entrySet().iterator().next();
            // get json view
            String candidacyJSONView = toJSON(entry.getKey(), entry.getValue());

            return candidacyJSONView;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Creates a new Candidacy, not finalized
     *
     * @param authToken The Authentication Token
     * @param candidacy
     * @return The new candidacy, with ID
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 403 X-Error-Code: wrong.position.candidacies.openingDate
     * @HTTP 403 X-Error-Code: wrong.position.candidacies.closingDate
     * @HTTP 404 X-Error-Code: wrong.candidate.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @POST
    @JsonView({DetailedCandidacyView.class})
    public Candidacy create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create
            candidacy = candidacyService.createCandidacy(candidacy, loggedOn);

            return candidacy;
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
     * Saves and finalizes candidacy
     *
     * @param authToken The Authentication Token
     * @param id
     * @param candidacy
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: max.evaluators.exceeded
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({DetailedCandidacyView.class})
    public Candidacy update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            Candidacy existingCandidacy = candidacyService.updateCandidacy(id, candidacy, loggedOn);

            return existingCandidacy;
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
     * Deletes the submitted candidacy
     *
     * @param authToken The Authentication Token
     * @param id
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: wrong.position.status.committee.converged
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete
            candidacyService.deleteCandidacy(id, loggedOn);
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

    /*********************
     * Register Members **
     *********************/

    /**
     * Returns the list of Register Members selectable
     * as Evaluators for this Candidacy
     *
     * @param authToken   The Authentication Token
     * @param positionId
     * @param candidacyId
     * @returnWrapped gr.grnet.dep.service.model.RegisterMember Array
     */
    @GET
    @Path("/{id:[0-9]+}/register")
    @JsonView({CandidacyEvaluator.CandidacyEvaluatorView.class})
    public Collection<RegisterMember> getCandidacyRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get register members
            Collection<RegisterMember> registerMembers = candidacyService.getCandidacyRegisterMembers(positionId, candidacyId, loggedOn);

            return registerMembers;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


    @POST
    @Path("/{id:[0-9]+}/register/search")
    @JsonView({CandidacyEvaluator.CandidacyEvaluatorView.class})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public SearchData<RegisterMember> search(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get the fetched data
            SearchData<RegisterMember> result = candidacyService.search(candidacyId, request, loggedOn);
            // Return result
            return result;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


    /*******************************
     * Snapshot File Functions *****
     *******************************/

    /**
     * Returns the list of snapshot files of this candidacy
     * (copied from profile)
     *
     * @param authToken   The Authentication Token
     * @param candidacyId
     * @return Array of gr.grnet.dep.service.model.file.CandidateFile Array
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     */
    @GET
    @Path("/{id:[0-9][0-9]*}/snapshot/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<CandidateFile> getSnapshotFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get files
            Collection<CandidateFile> snapshotFiles = candidacyService.getSnapshotFiles(candidacyId, loggedOn);

            return snapshotFiles;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns the file description with the given id
     *
     * @param authToken   The authentication Token
     * @param candidacyId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public CandidateFile getSnapshotFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file
            CandidateFile candidateFile = candidacyService.getSnapshotFile(candidacyId, fileId, loggedOn);

            return candidateFile;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns the actual file body (binary)
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @param bodyId
     * @return file body
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getSnapshotFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileBody fileBody = candidacyService.getSnapshotFileBody(candidacyId, fileId, bodyId, loggedOn);

            return sendFileBody(fileBody);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /*******************************
     * Candidacy File Functions ****
     *******************************/

    /**
     * Returns files specific to this candidacy
     *
     * @param authToken   The authentication Token
     * @param candidacyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     */
    @GET
    @Path("/{id:[0-9][0-9]*}/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<CandidacyFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get files
            Collection<CandidacyFile> candidacyFiles = candidacyService.getFiles(candidacyId, loggedOn);

            return candidacyFiles;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns file description of a file in candidacy
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public CandidacyFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            CandidacyFile candidacyFile = candidacyService.getFile(candidacyId, fileId, loggedOn);

            return candidacyFile;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns the actual file body (binary)
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @param bodyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file body
            FileBody fileBody = candidacyService.getFileBody(candidacyId, fileId, bodyId, loggedOn);

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
     * Uploads a new file
     *
     * @param authToken
     * @param candidacyId
     * @param request
     * @throws FileUploadException
     * @throws IOException
     * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @POST
    @Path("/{id:[0-9][0-9]*}/file")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // Parse Request
            List<FileItem> fileItems = readMultipartFormData(request);
            // get candidacy file
            CandidacyFile candidacyFile = candidacyService.createFile(candidacyId, fileItems, loggedOn);

            return toJSON(candidacyFile, SimpleFileHeaderView.class);
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
     * Uploads and updates an existing file
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @param request
     * @throws FileUploadException
     * @throws IOException
     * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @POST
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // Parse Request
            List<FileItem> fileItems = readMultipartFormData(request);
            // update file
            CandidacyFile candidacyFile = candidacyService.updateFile(candidacyId, fileId, fileItems, loggedOn);

            return toJSON(candidacyFile, FileHeader.SimpleFileHeaderView.class);
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
     * Removes the file body
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @DELETE
    @Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
    @JsonView({SimpleFileHeaderView.class})
    public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long candidacyId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            //delete file
            candidacyService.deleteFile(candidacyId, fileId, loggedOn);

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

    /*******************************
     * Evaluation File Functions ***
     *******************************/

    /**
     * Returns the list of evaluation files of this candidacy
     *
     * @param authToken   The Authentication Token
     * @param candidacyId
     * @return Array of gr.grnet.dep.service.model.file.PositionCandidaciesFile
     * Array
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     */
    @GET
    @Path("/{id:[0-9][0-9]*}/evaluation/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<PositionCandidaciesFile> getEvaluationFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get evaluation files
            Collection<PositionCandidaciesFile> positionCandidaciesFiles = candidacyService.getEvaluationFiles(candidacyId, loggedOn);

            return positionCandidaciesFiles;
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
     * Returns the file description with the given id
     *
     * @param authToken   The authentication Token
     * @param candidacyId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public PositionCandidaciesFile getEvaluationFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            PositionCandidaciesFile positionCandidaciesFile = candidacyService.getEvaluationFile(candidacyId, fileId, loggedOn);

            return positionCandidaciesFile;
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
     * Returns the actual file body (binary)
     *
     * @param authToken
     * @param candidacyId
     * @param fileId
     * @param bodyId
     * @return file body
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.candidacy.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getEvaluationFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file body
            FileBody evaluationFileBody = candidacyService.getEvaluationFileBody(candidacyId, fileId, bodyId, loggedOn);

            return sendFileBody(evaluationFileBody);
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

    @GET
    @Path("/incompletecandidacies")
    @JsonView({MediumCandidacyView.class})
    public Collection<Candidacy> getIncompleteCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get incomplete candidacies
            List<Candidacy> retv = candidacyService.getIncompleteCandidacies(loggedOn);

            return retv;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({Candidacy.DetailedCandidacyView.class})
    @Path("/submitcandidacy")
    public Candidacy createCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // submit candidacy
            candidacy = candidacyService.submitCandidacy(candidacy, loggedOn);

            return candidacy;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id:[0-9]+}/statushistory")
    public Response getCandidacyStatusHistoryList(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            List<CandidacyStatus> statusList = candidacyService.getCandidacyStatus(candidacyId, loggedOn);

            return Response.ok(statusList).build();
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

}
