package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;
import gr.grnet.dep.service.model.PositionComplementaryDocuments.DetailedPositionComplementaryDocumentsView;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/position/{id:[0-9]+}/complementaryDocuments")
@Stateless
public class PositionComplementaryDocumentsRESTService extends RESTService {

	@Inject
	private Logger log;

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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		return existingComDocs;
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingPosition.getPhase().getComplementaryDocuments().getFiles());
		return files;
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingComDocs.getFiles());
		for (ComplementaryDocumentsFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<ComplementaryDocumentsFile> files = FileHeader.filterDeleted(existingComDocs.getFiles());
		for (ComplementaryDocumentsFile file : files) {
			if (file.getId().equals(fileId)) {
				if (file.getId().equals(fileId)) {
					for (FileBody fb : file.getBodies()) {
						if (fb.getId().equals(bodyId)) {
							return sendFileBody(fb);
						}
					}
				}
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.complentaryDocuments.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (existingPosition.getPhase().getStatus().equals(PositionStatus.CANCELLED)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type:
		FileType type = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
				break;
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		try {
			// Check number of file types
			Set<ComplementaryDocumentsFile> cdFiles = FileHeader.filterIncludingDeleted(existingComDocs.getFiles(), type);
			ComplementaryDocumentsFile existingFile3 = checkNumberOfFileTypes(ComplementaryDocumentsFile.fileTypes, type, cdFiles);
			if (existingFile3 != null) {
				return _updateFile(loggedOn, fileItems, existingFile3);
			}

			// Create
			final ComplementaryDocumentsFile cdFile = new ComplementaryDocumentsFile();
			cdFile.setComplementaryDocuments(existingComDocs);
			cdFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, cdFile);
			existingComDocs.addFile(cdFile);
			em.flush();

			// Send E-Mails
			// position.upload@committee
			if (cdFile.getComplementaryDocuments().getPosition().getPhase().getCommittee() != null) {
				for (final PositionCommitteeMember member : cdFile.getComplementaryDocuments().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.upload@committee",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", cdFile.getComplementaryDocuments().getPosition().getName());
								put("institution", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("school", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName());
								put("department", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName());
							}
						}));
				}
			}
			// position.upload@candidates
			for (final Candidacy candidacy : cdFile.getComplementaryDocuments().getPosition().getPhase().getCandidacies().getCandidacies()) {
				mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
					"default.subject",
					"position.upload@candidates",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("firstname", candidacy.getCandidate().getUser().getBasicInfo().getFirstname());
							put("lastname", candidacy.getCandidate().getUser().getBasicInfo().getLastname());
							put("position", cdFile.getComplementaryDocuments().getPosition().getName());
							put("institution", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName());
							put("school", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName());
							put("department", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName());
						}
					}));
			}
			// position.upload@evaluators
			if (cdFile.getComplementaryDocuments().getPosition().getPhase().getEvaluation() != null) {
				for (final PositionEvaluator evaluator : cdFile.getComplementaryDocuments().getPosition().getPhase().getEvaluation().getEvaluators()) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.upload@evaluators",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", cdFile.getComplementaryDocuments().getPosition().getName());
								put("institution", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("school", cdFile.getComplementaryDocuments().getPosition().getDepartment().getSchool().getName());
								put("department", cdFile.getComplementaryDocuments().getPosition().getDepartment().getName());
							}
						}));
				}
			}
			// End: Send E-Mails

			return toJSON(cdFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.complentaryDocuments.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (existingPosition.getPhase().getStatus().equals(PositionStatus.CANCELLED)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type:
		FileType type = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
				break;
			}
		}
		// Extra Validation
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		try {
			if (!ComplementaryDocumentsFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			ComplementaryDocumentsFile complementaryDocumentsFile = null;
			for (ComplementaryDocumentsFile file : existingComDocs.getFiles()) {
				if (file.getId().equals(fileId)) {
					complementaryDocumentsFile = file;
					break;
				}
			}
			if (complementaryDocumentsFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			return _updateFile(loggedOn, fileItems, complementaryDocumentsFile);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
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
		User loggedOn = getLoggedOn(authToken);
		PositionComplementaryDocuments existingComDocs = em.find(PositionComplementaryDocuments.class, cdId);
		if (existingComDocs == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.complentaryDocuments.id");
		}
		Position existingPosition = existingComDocs.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getComplementaryDocuments().getId().equals(cdId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.complentaryDocuments.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (existingPosition.getPhase().getStatus().equals(PositionStatus.CANCELLED)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			ComplementaryDocumentsFile complementaryDocumentsFile = null;
			for (ComplementaryDocumentsFile file : existingComDocs.getFiles()) {
				if (file.getId().equals(fileId)) {
					complementaryDocumentsFile = file;
					break;
				}
			}
			if (complementaryDocumentsFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			ComplementaryDocumentsFile cdf = deleteAsMuchAsPossible(complementaryDocumentsFile);
			if (cdf == null) {
				existingComDocs.getFiles().remove(complementaryDocumentsFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
