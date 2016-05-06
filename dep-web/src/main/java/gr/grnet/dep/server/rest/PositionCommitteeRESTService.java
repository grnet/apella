package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.PositionCommitteeService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.PositionCommittee.DetailedPositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
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

@Path("/position/{id:[0-9][0-9]*}/committee")
public class PositionCommitteeRESTService extends RESTService {

	@EJB
	private PositionCommitteeService positionCommitteeService;

	/*********************
	 * Register Members **
	 *********************/

	/**
	 * Returns the list of registries associated with specified position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return A list of registerMember
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/register")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Collection<Register> getPositionCommiteeRegisters(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get members
			Collection<Register> registers = positionCommitteeService.getPositionCommiteeRegisters(positionId, committeeId, loggedOn);

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
	 * Returns the list of register members associates with specified position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return A list of registerMember
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/register/{registerId:[0-9]+}/member")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public SearchData<RegisterMember> getPositionCommiteeRegisterMembers(
			@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken,
			@PathParam("id") Long positionId,
			@PathParam("committeeId") Long committeeId,
			@PathParam("registerId") Long registerId,
			@Context HttpServletRequest request) {

		try {
			User loggedOn = getLoggedOn(authToken);
			// search
			SearchData<RegisterMember> members = positionCommitteeService.getPositionCommiteeRegisterMembers(positionId, committeeId, registerId, request, loggedOn);

			return members;
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
	 * Committee *********
	 *********************/

	/**
	 * Returns the committee description of the specified position
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get
			PositionCommittee existingCommittee = positionCommitteeService.get(positionId, committeeId, loggedOn);

			return existingCommittee;
		} catch (NotEnabledException e) {
			throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}

	/**
	 * Returns information of the specified member of the committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param cmId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.position.commitee.member.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/member/{cmId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("cmId") Long cmId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			PositionCommitteeMember existingMember = positionCommitteeService.getPositionCommitteeMember(positionId, committeeId, cmId, loggedOn);

			return existingMember;
		} catch (NotEnabledException e) {
			throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
		}
	}

	/**
	 * Updates the specified committee of the position
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param newCommittee
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.register.member.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: committee.missing.apofasi.systasis.epitropis
	 * @HTTP 409 X-Error-Code: member.is.evaluator
	 * @HTTP 409 X-Error-Code: max.regular.members.failed
	 * @HTTP 409 X-Error-Code: max.substitute.members.failed
	 * @HTTP 409 X-Error-Code: min.internal.regular.members.failed
	 * @HTTP 409 X-Error-Code: min.internal.substitute.members.failed
	 * @HTTP 409 X-Error-Code: min.external.regular.members.failed
	 * @HTTP 409 X-Error-Code: min.external.substitute.members.failed
	 */
	@PUT
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, PositionCommittee newCommittee) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// update
			PositionCommittee existingCommittee = positionCommitteeService.update(positionId, committeeId, newCommittee, loggedOn);

			return existingCommittee;
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
	 **********************/

	/**
	 * Returns the list of file descriptions associated with position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCommitteeFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get files
			Collection<PositionCommitteeFile> positionCommitteeFiles = positionCommitteeService.getFiles(positionId, committeeId, loggedOn);

			return positionCommitteeFiles;
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
	 * given position committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCommitteeFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// get file
			PositionCommitteeFile positionCommitteeFile = positionCommitteeService.getFile(positionId, committeeId, fileId, loggedOn);

			if (positionCommitteeFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}

			return positionCommitteeFile;
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
	 * given position committtee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			FileBody fileBody = positionCommitteeService.getFileBody(positionId, committeeId, fileId, bodyId, loggedOn);

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
	 * Handles upload of a new file associated with given position committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: committee.missing.committee.meeting.day
	 */
	@POST
	@Path("/{committeeId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);
			// create file
			FileHeader file = positionCommitteeService.createFile(positionId, committeeId, request, loggedOn);

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
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: wrong.file.type
	 */
	@POST
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			User loggedOn = getLoggedOn(authToken);
			// update file
			FileHeader file = positionCommitteeService.updateFile(positionId, committeeId, fileId, request, loggedOn);

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
	 * @param committeeId
	 * @param positionId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("committeeId") Long committeeId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		try {
			User loggedOn = getLoggedOn(authToken);
			// delete file
			positionCommitteeService.deleteFile(committeeId, positionId, fileId, loggedOn);

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
