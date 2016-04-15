package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionEvaluationService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.PositionEvaluation.DetailedPositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator.DetailedPositionEvaluatorView;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.PositionEvaluatorFile;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/position/{id:[0-9][0-9]*}/evaluation")
public class PositionEvaluationRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private PositionEvaluationService positionEvaluationService;

    /**
     * Returns the specific Position Evaluation info of given Position
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @return
     * @HTTP 403 X-Error-Code insufficient.privileges
     * @HTTP 404 X-Error-Code wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code wrong.position.id
     */
    @GET
    @Path("/{evaluationId:[0-9]+}")
    @JsonView({DetailedPositionEvaluationView.class})
    public PositionEvaluation getPositionEvaluation(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get evaluations
            PositionEvaluation existingEvaluation = positionEvaluationService.getPositionEvaluation(positionId, evaluationId, loggedOn);

            return existingEvaluation;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns information on spsecific evaluator of the position
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @return
     * @HTTP 403 X-Error-Code insufficient.privileges
     * @HTTP 404 X-Error-Code wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code wrong.position.id
     * @HTTP 404 X-Error-Code wrong.position.evaluator.id
     */
    @GET
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}")
    @JsonView({DetailedPositionEvaluatorView.class})
    public PositionEvaluator getPositionEvaluator(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            PositionEvaluator existingEvaluator = positionEvaluationService.getPositionEvaluator(positionId, evaluationId, evaluatorId, loggedOn);

            return existingEvaluator;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Updates the position evaluation with evaluators
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param newEvaluation
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.register.member.id
     * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: member.in.committe
     * @HTTP 409 X-Error-Code: wrong.evaluators.size
     * @HTTP 409 X-Error-Code:
     * committee.missing.praktiko.synedriasis.epitropis.gia.aksiologites
     */
    @PUT
    @Path("/{evaluationId:[0-9]+}")
    @JsonView({DetailedPositionEvaluationView.class})
    public PositionEvaluation updatePositionEvaluation(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, PositionEvaluation newEvaluation) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            PositionEvaluation evaluation = positionEvaluationService.updatePositionEvaluation(positionId, evaluationId, newEvaluation, loggedOn);

            return evaluation;
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
     * Returns the list of file descriptions associated with position evaluator
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @GET
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file")
    @JsonView({SimpleFileHeaderView.class})
    public Collection<PositionEvaluatorFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            Collection<PositionEvaluatorFile> files = positionEvaluationService.getFiles(positionId, evaluationId, evaluatorId, loggedOn);

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
     * given position evaluator
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public FileHeader getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileHeader file = positionEvaluationService.getFile(positionId, evaluationId, evaluatorId, fileId, loggedOn);
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
     * given position evaluator
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @param fileId
     * @param bodyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileBody fileBody = positionEvaluationService.getFileBody(positionId, evaluationId, evaluatorId, fileId, bodyId, loggedOn);
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
     * Handles upload of a new file associated with given position evaluator
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @param request
     * @return
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code:
     * committee.missing.aitima.epitropis.pros.aksiologites
     */
    @POST
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            FileHeader file = positionEvaluationService.createFile(positionId, evaluationId, evaluatorId, request, loggedOn);
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
     * evaluator
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @param evaluatorId
     * @param fileId
     * @param request
     * @throws FileUploadException
     * @throws IOException
     * @returnWrapper gr.grnet.dep.service.model.file.PositionEvaluatorFile
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     * @HTTP 409 X-Error-Code: wrong.file.type
     */
    @POST
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update file
            FileHeader file = positionEvaluationService.updateFile(positionId, evaluationId, evaluatorId, fileId, request, loggedOn);

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
     * @param evaluationId
     * @param evaluatorId
     * @param fileId
     * @return
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
     * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     * @HTTP 404 X-Error-Code: wrong.file.id
     * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
     * @HTTP 409 X-Error-Code: wrong.position.status
     */
    @DELETE
    @Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete file
            positionEvaluationService.deleteFile(positionId, evaluationId, evaluatorId, fileId, loggedOn);

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

    /****************************
     * RegisterMember Functions *
     ****************************/

    /**
     * Returns the list of registries associated with specified position
     * committee
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @return A list of registerMember
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.position.committee.id
     * @HTTP 404 X-Error-Code: wrong.position.id
     */
    @GET
    @Path("/{evaluationId:[0-9]+}/register")
    @JsonView({DetailedPositionCommitteeMemberView.class})
    public Collection<Register> getPositionEvaluationRegisters(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // fetch
            Collection<Register> registers = positionEvaluationService.getPositionEvaluationRegisters(positionId, evaluationId, loggedOn);

            return registers;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns a list of the register members participating in the evaluation of
     * the specififed position
     *
     * @param authToken
     * @param positionId
     * @param evaluationId
     * @return
     */
    @GET
    @Path("/{evaluationId:[0-9]+}/register/{registerId:[0-9]+}/member")
    @JsonView({DetailedRegisterMemberView.class})
    public SearchData<RegisterMember> getPositionEvaluationRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("registerId") Long registerId, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // fetch the register members
            SearchData<RegisterMember> result = positionEvaluationService.getPositionEvaluationRegisterMembers(positionId, evaluationId, registerId, request, loggedOn);
            return result;
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
