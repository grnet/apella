package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;

import java.io.IOException;
import java.util.Collection;
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

@Path("/position/{id:[0-9][0-9]*}/candidacies")
@Stateless
public class PositionCandidaciesRESTService extends RESTService {

	@Inject
	private Logger log;

	/*********************
	 * Candidacies *********
	 *********************/

	@GET
	@Path("/{candidaciesId:[0-9]+}")
	@JsonView({DetailedPositionCandidaciesView.class})
	public PositionCandidacies get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		existingCandidacies.initializeCollections();
		return existingCandidacies;
	}

	/**********************
	 * File Functions *****
	 **********************/

	@GET
	@Path("/{candidaciesId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCandidaciesFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(existingCandidacies.getFiles());
	}

	@GET
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCandidaciesFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		for (PositionCandidaciesFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		for (FileHeader file : files) {
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

	@POST
	@Path("/{candidaciesId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.phase");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type and candidacy evaluator:
		FileType type = null;
		Long candidacyEvaluatorId = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
			} else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
				candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (candidacyEvaluatorId == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.candidacy.evaluator");
		}
		CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.BAD_REQUEST, "wrong.candidacy.evaluator.id");
		}
		try {
			// Check number of file types
			Set<PositionCandidaciesFile> pcFiles = FileHeader.filterIncludingDeleted(existingCandidacies.getFiles(), type);
			PositionCandidaciesFile existingFile = checkNumberOfFileTypes(PositionCandidaciesFile.fileTypes, type, pcFiles);
			if (existingFile != null) {
				return _updateFile(loggedOn, fileItems, existingFile);
			}
			// Create
			PositionCandidaciesFile pcFile = new PositionCandidaciesFile();
			pcFile.setCandidacies(existingCandidacies);
			pcFile.setEvaluator(existingEvaluator);
			pcFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, pcFile);
			existingCandidacies.addFile(pcFile);
			em.flush();

			if (pcFile.getType().equals(FileType.EISIGISI_DEP_YPOPSIFIOU)) {
				// TODO: Send E-Mails
				// positionCandidacies.upload.eisigisi@candidate
				// positionCandidacies.upload.eisigisi@candidacyEvaluator
				// positionCandidacies.upload.eisigisi@committee
				// positionCandidacies.upload.eisigisi@evaluators
			} else {
				// TODO: Send E-Mails
				// position.upload@committee
				// position.upload@candidates
				// position.upload@evaluators
			}

			return toJSON(pcFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type and candidacy evaluator:
		FileType type = null;
		Long candidacyEvaluatorId = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
			} else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
				candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (candidacyEvaluatorId == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.candidacy.evaluator");
		}
		CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.BAD_REQUEST, "wrong.candidacy.evaluator.id");
		}
		try {
			if (!PositionCandidaciesFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionCandidaciesFile candidaciesFile = null;
			for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
				if (file.getId().equals(fileId)) {
					candidaciesFile = file;
					break;
				}
			}
			if (candidaciesFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			candidaciesFile.setEvaluator(existingEvaluator);
			return _updateFile(loggedOn, fileItems, candidaciesFile);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Deletes the last body of given file, if possible.
	 * 
	 * @param authToken
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("candidaciesId") Long candidaciesId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionCandidaciesFile candidaciesFile = null;
			for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
				if (file.getId().equals(fileId)) {
					candidaciesFile = file;
					break;
				}
			}
			if (candidaciesFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionCandidaciesFile cf = deleteAsMuchAsPossible(candidaciesFile);
			if (cf == null) {
				existingCandidacies.getFiles().remove(candidaciesFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	private static CandidacyEvaluator findCandidacyEvaluator(PositionCandidacies candidacies, Long candidacyEvaluatorId) {
		for (Candidacy candidacy : candidacies.getCandidacies()) {
			for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				if (evaluator.getId().equals(candidacyEvaluatorId)) {
					return evaluator;
				}
			}
		}
		return null;
	}
}
