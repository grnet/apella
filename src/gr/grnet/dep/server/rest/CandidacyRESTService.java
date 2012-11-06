package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.PositionStatus;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
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

@Path("/candidacy")
@Stateless
public class CandidacyRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/{id:[0-9]+}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Candidacy candidacy = (Candidacy) em.createQuery(
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate candidate = candidacy.getCandidate();
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
				!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Initialize collections
			candidacy.initializeCollections();
			return candidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
	}

	/**
	 * Check that a (possible) Candidacy passes some basic checks before
	 * creation / update.
	 * 
	 * @param candidacy
	 * @param candidate
	 */
	private void validateCandidacy(Candidacy candidacy, Candidate candidate) {
		if (!candidate.getStatus().equals(RoleStatus.ACTIVE)) {
			throw new RestException(Status.CONFLICT, "validation.candidacy.inactive.role");
		}
		if (FileHeader.filter(candidate.getFiles(), FileType.DIMOSIEYSI).size() == 0) {
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.dimosieysi");
		}
		if (FileHeader.filter(candidate.getFiles(), FileType.PTYXIO).size() == 0) {
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.ptyxio");
		}
		if (FileHeader.filter(candidate.getFiles(), FileType.BIOGRAFIKO).size() == 0) {
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.cv");
		}
	}

	/**
	 * Hold on to a snapshot of candidate's details in given candidacy.
	 * 
	 * @param candidacy
	 * @param candidate
	 */
	private void updateSnapshot(Candidacy candidacy, Candidate candidate) {
		candidacy.clearSnapshot();
		candidacy.updateSnapshot(candidate);
		User user = candidate.getUser();
		ProfessorDomestic professorDomestic = (ProfessorDomestic) user.getRole(RoleDiscriminator.PROFESSOR_DOMESTIC);
		ProfessorForeign professorForeign = (ProfessorForeign) user.getRole(RoleDiscriminator.PROFESSOR_FOREIGN);
		if (professorDomestic != null) {
			candidacy.updateSnapshot(professorDomestic);
		} else if (professorForeign != null) {
			candidacy.updateSnapshot(professorForeign);
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/snapshot")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy updateSnapshot(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", existingCandidacy.getCandidate().getId())
				.getSingleResult();

			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			validateCandidacy(existingCandidacy, candidate);
			updateSnapshot(existingCandidacy, candidate);
			existingCandidacy.initializeCollections();
			return existingCandidacy;
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@JsonView({DetailedCandidacyView.class})
	public Candidacy create(@HeaderParam(TOKEN_HEADER) String authToken, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			Candidate candidate = em.find(Candidate.class, candidacy.getCandidate().getId());
			if (candidate == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
			}
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = em.find(Position.class, candidacy.getPosition().getId());
			if (position == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!position.getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.status");
			}
			try {
				Candidacy existingCandidacy = (Candidacy) em.createQuery(
					"select c from Candidacy c" +
						"where c.candidate.id = :candidateId " +
						"and c.position.id = :positionId")
					.getSingleResult();
				// Return Results
				existingCandidacy.initializeCollections();
				return existingCandidacy;
			} catch (NoResultException e) {
			}
			// Create
			candidacy.setCandidate(candidate);
			candidacy.setPosition(position);
			candidacy.setDate(new Date());
			candidacy.setPermanent(false);
			candidacy.getProposedEvaluators().clear();
			validateCandidacy(candidacy, candidate);
			updateSnapshot(candidacy, candidate);

			em.persist(candidacy);
			em.flush();

			// Return Results
			candidacy.initializeCollections();
			return candidacy;
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = existingCandidacy.getCandidate();
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getPosition();
			if (!position.getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.status");
			}
			if (candidacy.getProposedEvaluators().size() > CandidacyEvaluator.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.evaluators.exceeded");
			}
			validateCandidacy(existingCandidacy, candidate);

			// Update
			existingCandidacy.getProposedEvaluators().clear();
			for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				if (!evaluator.isMissingRequiredField()) {
					existingCandidacy.addProposedEvaluator(evaluator);
				}
			}
			updateSnapshot(existingCandidacy, candidate);
			existingCandidacy.setPermanent(true);

			// Return
			existingCandidacy.initializeCollections();
			return existingCandidacy;
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = existingCandidacy.getCandidate();
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getPosition();
			if (!position.getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}

			// Update
			em.remove(existingCandidacy);
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/*******************************
	 * Snapshot File Functions *****
	 *******************************/
	@GET
	@Path("/{id:[0-9][0-9]*}/snapshot/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidateFile> getSnapshotFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		candidacy.getSnapshotFiles().size();
		return candidacy.getSnapshotFiles();
	}

	@GET
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidateFile getSnapshotFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidateFile file : candidacy.getSnapshotFiles()) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getSnapshotFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidateFile file : candidacy.getSnapshotFiles()) {
			if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
				return sendFileBody(file.getCurrentBody());
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/*******************************
	 * Candidacy File Functions ****
	 *******************************/
	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidacyFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		candidacy.getFiles().size();
		return candidacy.getFiles();
	}

	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidacyFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidacyFile file : candidacy.getFiles()) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidacyFile file : candidacy.getFiles()) {
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
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!candidacy.getPosition().getStatus().equals(PositionStatus.ANOIXTI)) {
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
		// Check number of file types
		Set<CandidacyFile> existingFiles = FileHeader.filter(candidacy.getFiles(), type);
		if (!CandidacyFile.fileTypes.containsKey(type) || existingFiles.size() >= CandidacyFile.fileTypes.get(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		// Create
		try {
			CandidacyFile candidacyFile = new CandidacyFile();
			candidacyFile.setCandidacy(candidacy);
			candidacyFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, candidacyFile);
			candidacy.addFile(candidacyFile);
			em.flush();

			return toJSON(candidacyFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!candidacy.getPosition().getStatus().equals(PositionStatus.ANOIXTI)) {
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
		if (!CandidacyFile.fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		CandidacyFile candidacyFile = null;
		for (CandidacyFile file : candidacy.getFiles()) {
			if (file.getId().equals(fileId)) {
				candidacyFile = file;
				break;
			}
		}
		if (candidacyFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}

		// Update
		try {
			saveFile(loggedOn, fileItems, candidacyFile);
			em.flush();
			candidacyFile.getBodies().size();
			return toJSON(candidacyFile, SimpleFileHeaderView.class);
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
	@Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!candidacy.getPosition().getStatus().equals(PositionStatus.ANOIXTI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}

		try {
			CandidacyFile candidacyFile = null;
			for (CandidacyFile file : candidacy.getFiles()) {
				if (file.getId().equals(fileId)) {
					candidacyFile = file;
					break;
				}
			}
			if (candidacyFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			Response retv = deleteFileBody(candidacyFile);
			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Remove from Position
				candidacy.getFiles().remove(candidacyFile);
			}
			return retv;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
