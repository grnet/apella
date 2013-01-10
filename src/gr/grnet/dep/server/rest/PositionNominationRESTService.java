package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.PositionNomination.DetailedPositionNominationView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionNominationFile;

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
import javax.ws.rs.PUT;
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

@Path("/position/{id:[0-9][0-9]*}/nomination")
@Stateless
public class PositionNominationRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/{nominationId:[0-9][0-9]*}")
	@JsonView({DetailedPositionNominationView.class})
	public PositionNomination get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId) {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position existingPosition = existingNomination.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}

		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		return existingNomination;
	}

	@PUT
	@Path("/{nominationId:[0-9][0-9]*}")
	@JsonView({DetailedPositionNominationView.class})
	public PositionNomination update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long positionId, @PathParam("nominationId") long nominationId, PositionNomination newNomination) {
		User loggedOn = getLoggedOn(authToken);
		try {
			PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
			if (existingNomination == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
			}
			Position existingPosition = existingNomination.getPosition();
			if (!existingPosition.getId().equals(positionId)) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (!existingPosition.getPhase().getNomination().getId().equals(existingNomination.getId())) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.nomination.phase");
			}
			//Update
			existingNomination.copyFrom(newNomination);
			// Add Nominated
			if (newNomination.getNominatedCandidacy() != null && newNomination.getNominatedCandidacy().getId() != null) {
				for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
					if (candidacy.getId().equals(newNomination.getNominatedCandidacy().getId())) {
						existingPosition.getPhase().getNomination().setNominatedCandidacy(candidacy);
						break;
					}
				}
			} else {
				existingNomination.setNominatedCandidacy(null);
			}
			// Add Second Nominated
			if (newNomination.getSecondNominatedCandidacy() != null && newNomination.getSecondNominatedCandidacy().getId() != null) {
				for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
					if (candidacy.getId().equals(newNomination.getSecondNominatedCandidacy().getId())) {
						existingPosition.getPhase().getNomination().setSecondNominatedCandidacy(candidacy);
						break;
					}
				}
			} else {
				existingNomination.setSecondNominatedCandidacy(null);
			}
			em.flush();
			return existingNomination;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	@GET
	@Path("/{nominationId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionNominationFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId) {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment()) &&
			!(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
			!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(position.getPhase().getNomination().getFiles());
	}

	@GET
	@Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionNominationFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment()) &&
			!(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
			!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionNominationFile> files = FileHeader.filterDeleted(position.getPhase().getNomination().getFiles());
		for (PositionNominationFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment()) &&
			!(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
			!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionNominationFile> files = FileHeader.filterDeleted(existingNomination.getFiles());
		for (PositionNominationFile file : files) {
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
	@Path("/{nominationId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
			throw new RestException(Status.FORBIDDEN, "not.current.position.phase");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
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
			Set<PositionNominationFile> nominationFiles = FileHeader.filterIncludingDeleted(position.getPhase().getNomination().getFiles(), type);
			PositionNominationFile existingFile4 = checkNumberOfFileTypes(PositionNominationFile.fileTypes, type, nominationFiles);
			if (existingFile4 != null) {
				return _updateFile(loggedOn, fileItems, existingFile4);
			}

			// Create
			PositionNominationFile nominationFile = new PositionNominationFile();
			nominationFile.setNomination(position.getPhase().getNomination());
			nominationFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, nominationFile);
			position.getPhase().getNomination().addFile(nominationFile);
			em.flush();

			return toJSON(nominationFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{nominationId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
			throw new RestException(Status.FORBIDDEN, "not.current.position.phase");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
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
		if (!PositionNominationFile.fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		try {
			PositionNominationFile nominationFile = null;
			for (PositionNominationFile file : position.getPhase().getNomination().getFiles()) {
				if (file.getId().equals(fileId)) {
					nominationFile = file;
					break;
				}
			}
			if (nominationFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			return _updateFile(loggedOn, fileItems, nominationFile);
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
	@Path("/{nominationId:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long positionId, @PathParam("nominationId") Long nominationId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionNomination existingNomination = em.find(PositionNomination.class, nominationId);
		if (existingNomination == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.nomination.id");
		}
		Position position = existingNomination.getPosition();
		if (!position.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!position.getPhase().getNomination().getId().equals(existingNomination.getId())) {
			throw new RestException(Status.FORBIDDEN, "not.current.position.phase");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionNominationFile nominationFile = null;
			for (PositionNominationFile file : position.getPhase().getNomination().getFiles()) {
				if (file.getId().equals(fileId)) {
					nominationFile = file;
					break;
				}
			}
			if (nominationFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionNominationFile pnf = deleteAsMuchAsPossible(nominationFile);
			if (pnf == null) {
				position.getPhase().getNomination().getFiles().remove(nominationFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

}
