package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacies;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Committee;
import gr.grnet.dep.service.model.CommitteeMember;
import gr.grnet.dep.service.model.CommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.ComplementaryDocuments;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Evaluation;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Nomination;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.CandidatePositionView;
import gr.grnet.dep.service.model.Position.CommitteeMemberPositionView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionPhase;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
import gr.grnet.dep.service.model.file.PositionEvaluationFile;
import gr.grnet.dep.service.model.file.PositionNominationFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

@Path("/position")
@Stateless
public class PositionRESTService extends RESTService {

	@Inject
	private Logger log;

	private Position getAndCheckPosition(User loggedOn, long positionId) {
		Position position = em.find(Position.class, positionId);
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		position.initializeCollections();
		return position;
	}

	@GET
	@JsonView({PublicPositionView.class})
	public List<Position> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		User loggedOnUser = getLoggedOn(authToken);
		if (loggedOnUser.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER) ||
			loggedOnUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {

			List<Institution> institutions = new ArrayList<Institution>();
			institutions.addAll(loggedOnUser.getAssociatedInstitutions());
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"where p.permanent = true " +
					"and p.department.institution in (:institutions)")
				.setParameter("institutions", institutions)
				.getResultList();

			for (Position position : positions) {
				position.initializeCollections();
			}
			return positions;
		} else if (loggedOnUser.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
			loggedOnUser.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {

			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"where p.permanent = true")
				.getResultList();
			for (Position position : positions) {
				position.initializeCollections();
			}
			return positions;
		}
		throw new RestException(Status.UNAUTHORIZED, "insufficient.privileges");
	}

	@GET
	@Path("/search")
	@JsonView({PublicPositionView.class})
	public List<Position> search(@HeaderParam(TOKEN_HEADER) String authToken) {
		// TODO: Search based on QueryParameter (or a Search object)
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"where p.permanent = true " +
					"and p.phase.candidacies.closingDate >= :today ")
				.setParameter("today", today)
				.getResultList();

			for (Position position : positions) {
				position.initializeCollections();
			}
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}
	}

	@GET
	@Path("/public")
	@JsonView({PublicPositionView.class})
	public List<Position> getPublic() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"where p.phase.candidacies.closingDate >= :today ")
				.setParameter("today", today)
				.getResultList();

			for (Position position : positions) {
				position.initializeCollections();
			}
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}

	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public String get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @QueryParam("phase") Integer order) {
		User loggedOn = getLoggedOn(authToken);
		Position p = getAndCheckPosition(loggedOn, positionId);

		String result = null;
		if (loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
			loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
			loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
			loggedOn.isDepartmentUser(p.getDepartment())) {
			result = toJSON(order == null ? p : p.as(order), DetailedPositionView.class);
		} else if (loggedOn.hasActiveRole(RoleDiscriminator.PROFESSOR_DOMESTIC) ||
			loggedOn.hasActiveRole(RoleDiscriminator.PROFESSOR_FOREIGN)) {
			PositionPhase phase = order == null ? p.getPhase() : p.getPhases().get(order);
			if (phase.getCommittee() != null && phase.getCommittee().containsMember(loggedOn)) {
				result = toJSON(order == null ? p : p.as(order), CommitteeMemberPositionView.class);
			} else {
				result = toJSON(order == null ? p : p.as(order), PublicPositionView.class);
			}
		} else if (loggedOn.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			PositionPhase phase = order == null ? p.getPhase() : p.getPhases().get(order);
			if (phase.getCandidacies().containsCandidate(loggedOn)) {
				result = toJSON(order == null ? p : p.as(order), CandidatePositionView.class);
			} else {
				result = toJSON(order == null ? p : p.as(order), PublicPositionView.class);
			}
		} else {
			result = toJSON(order == null ? p : p.as(order), PublicPositionView.class);
		}
		return result;
	}

	@POST
	@JsonView({DetailedPositionView.class})
	public Position create(@HeaderParam(TOKEN_HEADER) String authToken, Position position) {
		try {
			Department department = em.find(Department.class, position.getDepartment().getId());
			if (department == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.department.id");
			}
			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(department)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Date now = new Date();
			position.setPermanent(false);

			position.setPhase(new PositionPhase());
			position.getPhase().setPosition(position);
			position.getPhase().setOrder(0);
			position.getPhase().setStatus(PositionStatus.ENTAGMENI);
			position.getPhase().setCreatedAt(now);
			position.getPhase().setUpdatedAt(now);
			position.getPhase().setCandidacies(new Candidacies());
			position.getPhase().getCandidacies().setPosition(position);
			position.getPhase().getCandidacies().setCreatedAt(now);
			position.getPhase().getCandidacies().setUpdatedAt(now);
			position.getPhase().setCommittee(null);
			position.getPhase().setComplementaryDocuments(null);
			position.getPhase().setNomination(null);

			position = em.merge(position);
			em.flush();

			position.initializeCollections();
			return position;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, id);
			existingPosition.copyFrom(position);
			existingPosition.setPermanent(true);
			em.flush();
			existingPosition.initializeCollections();
			return existingPosition;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(loggedOn, id);
			if (!position.getPhase().getStatus().equals(PositionStatus.ENTAGMENI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			em.remove(position);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/phase")
	@JsonView({DetailedPositionView.class})
	public Position addPhase(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long positionId, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			PositionStatus newStatus = position.getPhase().getStatus();
			Position existingPosition = getAndCheckPosition(loggedOn, positionId);
			PositionPhase existingPhase = existingPosition.getPhase();
			PositionPhase newPhase = null;
			// Go through all transition scenarios, 
			// update when needed or throw exception if transition not allowed
			switch (existingPhase.getStatus()) {
				case ENTAGMENI:
					switch (newStatus) {
						case ENTAGMENI:
							break;
						case ANOIXTI:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.ANOIXTI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case EPILOGI:
						case ANAPOMPI:
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
					}
					break;
				case ANOIXTI:
					switch (newStatus) {
						case ENTAGMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case ANOIXTI:
							break;
						case EPILOGI:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.EPILOGI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setCommittee(new Committee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new Evaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new Nomination());
							newPhase.getNomination().setPosition(existingPosition);
							newPhase.setComplementaryDocuments(new ComplementaryDocuments());
							newPhase.getComplementaryDocuments().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
					}
					break;
				case EPILOGI:
					switch (newStatus) {
						case ENTAGMENI:
						case ANOIXTI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case EPILOGI:
							break;
						case ANAPOMPI:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.ANAPOMPI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setNomination(existingPhase.getNomination());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case STELEXOMENI:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.STELEXOMENI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setNomination(existingPhase.getNomination());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
					}
					break;
				case STELEXOMENI:
					throw new RestException(Status.CONFLICT, "wrong.position.status");
				case ANAPOMPI:
					switch (newStatus) {
						case ENTAGMENI:
						case ANOIXTI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case EPILOGI:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.EPILOGI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							// TODO: Use the same Committe until a request to create a new one occurs.
							// newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setCommittee(new Committee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new Evaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new Nomination());
							newPhase.getNomination().setPosition(existingPosition);
							newPhase.setComplementaryDocuments(new ComplementaryDocuments());
							newPhase.getComplementaryDocuments().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");

					}
					break;
			}

			em.flush();
			existingPosition.initializeCollections();
			return existingPosition;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	public enum FileDiscriminator {
		committee,
		evaluation,
		complementaryDocuments,
		nomination
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/{discriminator}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<? extends FileHeader> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("discriminator") FileDiscriminator discriminator) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
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
		List<FileHeader> files = new ArrayList<FileHeader>();
		switch (discriminator) {
			case committee:
				if (position.getPhase().getCommittee() != null) {
					files.addAll(position.getPhase().getCommittee().getFiles());
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(position.getPhase().getEvaluation().getFiles());
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(position.getPhase().getComplementaryDocuments().getFiles());
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(position.getPhase().getNomination().getFiles());
				}
				break;
		}
		return files;
	}

	@GET
	@Path("/{id:[0-9]+}/{discriminator}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("discriminator") FileDiscriminator discriminator, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
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
		List<FileHeader> files = new ArrayList<FileHeader>();
		switch (discriminator) {
			case committee:
				if (position.getPhase().getCommittee() != null) {
					files.addAll(position.getPhase().getCommittee().getFiles());
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(position.getPhase().getEvaluation().getFiles());
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(position.getPhase().getComplementaryDocuments().getFiles());
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(position.getPhase().getNomination().getFiles());
				}
				break;
		}
		for (FileHeader file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/{discriminator}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("discriminator") FileDiscriminator discriminator, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
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
		List<FileHeader> files = new ArrayList<FileHeader>();
		switch (discriminator) {
			case committee:
				if (position.getPhase().getCommittee() != null) {
					files.addAll(position.getPhase().getCommittee().getFiles());
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(position.getPhase().getEvaluation().getFiles());
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(position.getPhase().getComplementaryDocuments().getFiles());
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(position.getPhase().getNomination().getFiles());
				}
				break;
		}
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
	@Path("/{id:[0-9][0-9]*}/{discriminator}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("discriminator") FileDiscriminator discriminator, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
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
			switch (discriminator) {
				case committee:
					Set<PositionCommitteeFile> pcFiles = FileHeader.filter(position.getPhase().getCommittee().getFiles(), type);
					if (!PositionCommitteeFile.fileTypes.containsKey(type) || pcFiles.size() >= PositionCommitteeFile.fileTypes.get(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					// Create
					PositionCommitteeFile pcFile = new PositionCommitteeFile();
					pcFile.setCommittee(position.getPhase().getCommittee());
					pcFile.setOwner(loggedOn);
					saveFile(loggedOn, fileItems, pcFile);
					position.getPhase().getCommittee().addFile(pcFile);
					em.flush();

					return toJSON(pcFile, SimpleFileHeaderView.class);
				case evaluation:
					Set<PositionEvaluationFile> eFiles = FileHeader.filter(position.getPhase().getEvaluation().getFiles(), type);
					if (!PositionEvaluationFile.fileTypes.containsKey(type) || eFiles.size() >= PositionEvaluationFile.fileTypes.get(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					// Create
					PositionEvaluationFile eFile = new PositionEvaluationFile();
					eFile.setEvaluation(position.getPhase().getEvaluation());
					eFile.setOwner(loggedOn);
					saveFile(loggedOn, fileItems, eFile);
					position.getPhase().getEvaluation().addFile(eFile);
					em.flush();

					return toJSON(eFile, SimpleFileHeaderView.class);
				case complementaryDocuments:
					Set<ComplementaryDocumentsFile> cdFiles = FileHeader.filter(position.getPhase().getComplementaryDocuments().getFiles(), type);
					if (!ComplementaryDocumentsFile.fileTypes.containsKey(type) || cdFiles.size() >= ComplementaryDocumentsFile.fileTypes.get(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					// Create
					ComplementaryDocumentsFile cdFile = new ComplementaryDocumentsFile();
					cdFile.setComplementaryDocuments(position.getPhase().getComplementaryDocuments());
					cdFile.setOwner(loggedOn);
					saveFile(loggedOn, fileItems, cdFile);
					position.getPhase().getComplementaryDocuments().addFile(cdFile);
					em.flush();

					return toJSON(cdFile, SimpleFileHeaderView.class);
				case nomination:
					Set<PositionNominationFile> nominationFiles = FileHeader.filter(position.getPhase().getNomination().getFiles(), type);
					if (!PositionNominationFile.fileTypes.containsKey(type) || nominationFiles.size() >= PositionNominationFile.fileTypes.get(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					// Create
					PositionNominationFile nominationFile = new PositionNominationFile();
					nominationFile.setNomination(position.getPhase().getNomination());
					nominationFile.setOwner(loggedOn);
					saveFile(loggedOn, fileItems, nominationFile);
					position.getPhase().getNomination().addFile(nominationFile);
					em.flush();

					return toJSON(nominationFile, SimpleFileHeaderView.class);
			}
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
		throw new RestException(Status.BAD_REQUEST, "wrong.file.type");
	}

	@POST
	@Path("/{id:[0-9]+}/{discriminator}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("discriminator") FileDiscriminator discriminator, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
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
		// Extra Validation
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		try {
			switch (discriminator) {
				case committee:
					if (!PositionCommitteeFile.fileTypes.containsKey(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					PositionCommitteeFile committeeFile = null;
					for (PositionCommitteeFile file : position.getPhase().getCommittee().getFiles()) {
						if (file.getId().equals(fileId)) {
							committeeFile = file;
							break;
						}
					}
					if (committeeFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					// Update
					saveFile(loggedOn, fileItems, committeeFile);
					em.flush();
					committeeFile.getBodies().size();
					return toJSON(committeeFile, SimpleFileHeaderView.class);
				case evaluation:
					if (!PositionEvaluationFile.fileTypes.containsKey(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					PositionEvaluationFile evaluationFile = null;
					for (PositionEvaluationFile file : position.getPhase().getEvaluation().getFiles()) {
						if (file.getId().equals(fileId)) {
							evaluationFile = file;
							break;
						}
					}
					if (evaluationFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					// Update
					saveFile(loggedOn, fileItems, evaluationFile);
					em.flush();
					evaluationFile.getBodies().size();
					return toJSON(evaluationFile, SimpleFileHeaderView.class);
				case complementaryDocuments:
					if (!ComplementaryDocumentsFile.fileTypes.containsKey(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
					ComplementaryDocumentsFile complementaryDocumentsFile = null;
					for (ComplementaryDocumentsFile file : position.getPhase().getComplementaryDocuments().getFiles()) {
						if (file.getId().equals(fileId)) {
							complementaryDocumentsFile = file;
							break;
						}
					}
					if (complementaryDocumentsFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					// Update
					saveFile(loggedOn, fileItems, complementaryDocumentsFile);
					em.flush();
					complementaryDocumentsFile.getBodies().size();
					return toJSON(complementaryDocumentsFile, SimpleFileHeaderView.class);
				case nomination:
					if (!PositionNominationFile.fileTypes.containsKey(type)) {
						throw new RestException(Status.CONFLICT, "wrong.file.type");
					}
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
					saveFile(loggedOn, fileItems, nominationFile);
					em.flush();
					nominationFile.getBodies().size();
					return toJSON(nominationFile, SimpleFileHeaderView.class);
				default:
					throw new RestException(Status.BAD_REQUEST, "wrong.file.discriminator");

			}
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
	@Path("/{id:[0-9]+}/{discriminator}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("discriminator") FileDiscriminator discriminator, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			switch (discriminator) {
				case committee:
					PositionCommitteeFile committeeFile = null;
					for (PositionCommitteeFile file : position.getPhase().getCommittee().getFiles()) {
						if (file.getId().equals(fileId)) {
							committeeFile = file;
							break;
						}
					}
					if (committeeFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					File cPhysicalFile = deleteFileBody(committeeFile);
					cPhysicalFile.delete();
					if (committeeFile.getCurrentBody() == null) {
						// Remove from Position
						position.getPhase().getCommittee().getFiles().remove(committeeFile);
						return Response.noContent().build();
					} else {
						return Response.ok(committeeFile).build();
					}
				case evaluation:
					PositionEvaluationFile evaluationFile = null;
					for (PositionEvaluationFile file : position.getPhase().getEvaluation().getFiles()) {
						if (file.getId().equals(fileId)) {
							evaluationFile = file;
							break;
						}
					}
					if (evaluationFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					File ePhysicalFile = deleteFileBody(evaluationFile);
					ePhysicalFile.delete();
					if (evaluationFile.getCurrentBody() == null) {
						// Remove from Position
						position.getPhase().getComplementaryDocuments().getFiles().remove(evaluationFile);
						return Response.noContent().build();
					} else {
						return Response.ok(evaluationFile).build();
					}
				case complementaryDocuments:
					ComplementaryDocumentsFile complementaryDocumentsFile = null;
					for (ComplementaryDocumentsFile file : position.getPhase().getComplementaryDocuments().getFiles()) {
						if (file.getId().equals(fileId)) {
							complementaryDocumentsFile = file;
							break;
						}
					}
					if (complementaryDocumentsFile == null) {
						throw new RestException(Status.NOT_FOUND, "wrong.file.id");
					}
					File cdPhysicalFile = deleteFileBody(complementaryDocumentsFile);
					cdPhysicalFile.delete();
					if (complementaryDocumentsFile.getCurrentBody() == null) {
						// Remove from Position
						position.getPhase().getComplementaryDocuments().getFiles().remove(complementaryDocumentsFile);
						return Response.noContent().build();
					} else {
						return Response.ok(complementaryDocumentsFile).build();
					}
				case nomination:
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
					File nPhysicalFile = deleteFileBody(nominationFile);
					nPhysicalFile.delete();
					if (nominationFile.getCurrentBody() == null) {
						// Remove from Position
						position.getPhase().getComplementaryDocuments().getFiles().remove(nominationFile);
						return Response.noContent().build();
					} else {
						return Response.ok(nominationFile).build();
					}
				default:
					throw new RestException(Status.BAD_REQUEST, "wrong.file.discriminator");

			}
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/************************
	 * Committee Functions **
	 ************************/

	@GET
	@Path("/{id:[0-9][0-9]*}/professor")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<Role> getPositionProfessors(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);

		List<Role> professors = em.createQuery(
			"select r from Role r " +
				"where r.id is not null " +
				"and r.discriminator in (:discriminators) ")
			.setParameter("discriminators", discriminatorList)
			.getResultList();

		// Execute
		for (Role r : professors) {
			Professor p = (Professor) r;
			p.setCommitteesCount(p.getCommittees().size());
			r.initializeCollections();
		}
		return professors;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<CommitteeMember> getPositionCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (position.getPhase().getCommittee() == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment()) &&
			!position.getPhase().getCommittee().containsMember(loggedOn) &&
			!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (CommitteeMember member : position.getPhase().getCommittee().getMembers()) {
			member.getProfessor().initializeCollections();
		}
		return position.getPhase().getCommittee().getMembers();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{cmId:[0-9][0-9]*}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public CommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (position.getPhase().getCommittee() == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		CommitteeMember result = em.find(CommitteeMember.class, cmId);
		if (result == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.commitee.member.id");
		}

		if (!result.getCommittee().getPosition().getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		result.getProfessor().initializeCollections();

		return result;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public CommitteeMember createPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, CommitteeMember newMembership) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, positionId);
			if (existingPosition.getPhase().getCommittee() == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			Professor existingProfessor = em.find(Professor.class, newMembership.getProfessor().getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			// Check if already exists:
			for (CommitteeMember existingMember : existingPosition.getPhase().getCommittee().getMembers()) {
				if (existingMember.getProfessor().getId().equals(existingProfessor.getId())) {
					throw new RestException(Status.CONFLICT, "member.already.exists");
				}
			}
			int count = 0;
			for (CommitteeMember existingMember : existingPosition.getPhase().getCommittee().getMembers()) {
				if (existingMember.getType() == newMembership.getType()) {
					count++;
				}
			}
			if (count >= CommitteeMember.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}

			// Update
			newMembership.setCommittee(existingPosition.getPhase().getCommittee());
			newMembership.setProfessor(existingProfessor);
			existingPosition.getPhase().getCommittee().getMembers().add(newMembership);
			newMembership = em.merge(newMembership);
			em.flush();

			// Return result
			newMembership.getProfessor().initializeCollections();
			return newMembership;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9]+}/committee/{cmId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public CommitteeMember updatePositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId, CommitteeMember newMembership) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, positionId);
			if (existingPosition.getPhase().getCommittee() == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			// Check if already exists:
			CommitteeMember existingMember = null;
			for (CommitteeMember member : existingPosition.getPhase().getCommittee().getMembers()) {
				if (member.getId().equals(cmId)) {
					existingMember = member;
				}
			}
			if (existingMember == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.committee.member.id");
			}
			// Update (only type allowed)
			existingMember.setType(newMembership.getType());
			em.flush();

			// Return result
			existingMember.getProfessor().initializeCollections();
			return existingMember;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee/{cmId:[0-9][0-9]*}")
	public void removePositionCommiteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(loggedOn, positionId);
			if (position.getPhase().getCommittee() == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			CommitteeMember existingCM = null;
			for (CommitteeMember member : position.getPhase().getCommittee().getMembers()) {
				if (member.getId().equals(cmId)) {
					existingCM = member;
					break;
				}
			}
			if (existingCM == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.committee.member.id");
			}
			// Remove
			existingCM.getCommittee().getMembers().remove(existingCM);
			em.remove(existingCM);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/candidacies")
	@JsonView({DetailedCandidacyView.class})
	public Set<Candidacy> getPositionCandidacies(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (position.getPhase().getCandidacies() == null) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment()) &&
			!(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
			!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (Candidacy candidacy : position.getPhase().getCandidacies().getCandidacies()) {
			candidacy.initializeCollections();
		}
		return position.getPhase().getCandidacies().getCandidacies();
	}
}
