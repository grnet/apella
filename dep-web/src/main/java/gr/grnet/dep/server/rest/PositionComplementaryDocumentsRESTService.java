package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionComplementaryDocumentsService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;
import gr.grnet.dep.service.model.PositionComplementaryDocuments.DetailedPositionComplementaryDocumentsView;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
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

@Path("/position/{id:[0-9]+}/complementaryDocuments")
public class PositionComplementaryDocumentsRESTService extends RESTService {

	@EJB
	private PositionComplementaryDocumentsService positionComplementaryDocumentsService;

	/**
	 * Retrieves the complementary documents parameter of the specified position
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{cdId:[0-9]+}")
	@JsonView({DetailedPositionComplementaryDocumentsView.class})
	public PositionComplementaryDocuments get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get
			PositionComplementaryDocuments existingComDocs = positionComplementaryDocumentsService.get(positionId, cdId, loggedOn);

			return existingComDocs;
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
	 ***********************/

	/**
	 * Returns the list of file descriptions associated with position
	 * candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{cdId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<ComplementaryDocumentsFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId) {
		try {

			User loggedOn = getLoggedOn(authToken);
			Collection<ComplementaryDocumentsFile> files = positionComplementaryDocumentsService.getFiles(positionId, cdId, loggedOn);

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
	 * given position complementary documents
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{cdId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public ComplementaryDocumentsFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			ComplementaryDocumentsFile file = positionComplementaryDocumentsService.getFile(positionId, cdId, fileId, loggedOn);

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
	 * given position complementary documents
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{cdId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			FileBody fileBody = positionComplementaryDocumentsService.getFileBody(positionId, cdId, fileId, bodyId, loggedOn);
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
	 * Handles upload of a new file associated with given position complementary
	 * documents
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.complentaryDocuments.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{cdId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);

			// create file
			FileHeader file = positionComplementaryDocumentsService.createFile(positionId, cdId, request, loggedOn);

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
	 * complementary documents
	 *
	 * @param authToken
	 * @param positionId
	 * @param cdId
	 * @param fileId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.complentaryDocuments.phase
	 * @HTTP 409 X-Error-Code: wrong.file.type
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{cdId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cdId") Long cdId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);
			// update file
			FileHeader file = positionComplementaryDocumentsService.updateFile(positionId, cdId, fileId, request, loggedOn);

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
	 * @param cdId
	 * @param positionId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.complentaryDocuments.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.complentaryDocuments.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{cdId:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("cdId") Long cdId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// delete file
			positionComplementaryDocumentsService.deleteFile(cdId, positionId, fileId, loggedOn);
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
