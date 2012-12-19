package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.CandidatePositionView;
import gr.grnet.dep.service.model.Position.CommitteeMemberPositionView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCommittee;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;
import gr.grnet.dep.service.model.PositionEvaluation;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.PositionEvaluator.DetailedPositionEvaluatorView;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.PositionPhase;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
import gr.grnet.dep.service.model.file.PositionEvaluationFile;
import gr.grnet.dep.service.model.file.PositionNominationFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
	@Path("/public")
	@JsonView({PublicPositionView.class})
	public List<Position> getPublic() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"where p.phase.candidacies.closingDate >= :today " +
					"and p.permanent = true ")
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
			position.getPhase().setCandidacies(new PositionCandidacies());
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
	@Path("/{id:[0-9]+}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, id);
			existingPosition.copyFrom(position);
			existingPosition.setSubject(supplementSubject(position.getSubject()));
			if (existingPosition.getPhase().getNomination() != null) {
				// Add Nominated
				if (position.getPhase().getNomination().getNominatedCandidacy() != null && position.getPhase().getNomination().getNominatedCandidacy().getId() != null) {
					for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
						if (candidacy.getId().equals(position.getPhase().getNomination().getNominatedCandidacy().getId())) {
							existingPosition.getPhase().getNomination().setNominatedCandidacy(candidacy);
							break;
						}
					}
				} else {
					existingPosition.getPhase().getNomination().setNominatedCandidacy(null);
				}
				// Add Second Nominated
				if (position.getPhase().getNomination().getSecondNominatedCandidacy() != null && position.getPhase().getNomination().getSecondNominatedCandidacy().getId() != null) {
					for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
						if (candidacy.getId().equals(position.getPhase().getNomination().getSecondNominatedCandidacy().getId())) {
							existingPosition.getPhase().getNomination().setSecondNominatedCandidacy(candidacy);
							break;
						}
					}
				} else {
					existingPosition.getPhase().getNomination().setSecondNominatedCandidacy(null);
				}
			}
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
				throw new RestException(Status.CONFLICT, "wrong.position.status.cannot.delete");
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
							// Validate: 
							if (existingPhase.getCandidacies().getClosingDate().compareTo(new Date()) < 0) {
								throw new RestException(Status.CONFLICT, "position.phase.anoixti.wrong.closing.date");
							}
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.ANOIXTI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case EPILOGI:
						case ANAPOMPI:
						case STELEXOMENI:
						case CANCELLED:
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
							// Validate
							if (existingPhase.getCandidacies().getClosingDate().compareTo(new Date()) >= 0) {
								throw new RestException(Status.CONFLICT, "position.phase.epilogi.wrong.closing.date");
							}
							// Update
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.EPILOGI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
							newPhase.setComplementaryDocuments(new PositionComplementaryDocuments());
							newPhase.getComplementaryDocuments().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
						case STELEXOMENI:
						case CANCELLED:
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
							// Validate
							if (FileHeader.filter(existingPhase.getNomination().getFiles(), FileType.APOFASI_ANAPOMPIS).size() == 0) {
								throw new RestException(Status.CONFLICT, "position.phase.anapompi.missing.apofasi");
							}
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
							// Validate
							if (existingPhase.getNomination().getNominatedCandidacy() == null) {
								throw new RestException(Status.CONFLICT, "position.phase.stelexomeni.missing.nominated.candidacy");
							}
							if (FileHeader.filter(existingPhase.getNomination().getFiles(), FileType.PRAKTIKO_EPILOGIS).size() == 0) {
								throw new RestException(Status.CONFLICT, "position.phase.stelexomeni.missing.praktiko.epilogis");
							}
							// Update
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
						case CANCELLED:
							// Validate
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.CANCELLED);
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
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
							newPhase.setComplementaryDocuments(new PositionComplementaryDocuments());
							newPhase.getComplementaryDocuments().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case CANCELLED:
							throw new RestException(Status.CONFLICT, "wrong.position.status");

					}
					break;
				case CANCELLED:
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
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
							newPhase.setComplementaryDocuments(new PositionComplementaryDocuments());
							newPhase.getComplementaryDocuments().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case CANCELLED:
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
					files.addAll(FileHeader.filterDeleted(position.getPhase().getCommittee().getFiles()));
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getEvaluation().getFiles()));
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getComplementaryDocuments().getFiles()));
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getNomination().getFiles()));
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
					files.addAll(FileHeader.filterDeleted(position.getPhase().getCommittee().getFiles()));
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getEvaluation().getFiles()));
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getComplementaryDocuments().getFiles()));
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getNomination().getFiles()));
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
					files.addAll(FileHeader.filterDeleted(position.getPhase().getCommittee().getFiles()));
				}
				break;
			case evaluation:
				if (position.getPhase().getEvaluation() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getEvaluation().getFiles()));
				}
				break;
			case complementaryDocuments:
				if (position.getPhase().getComplementaryDocuments() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getComplementaryDocuments().getFiles()));
				}
				break;
			case nomination:
				if (position.getPhase().getNomination() != null) {
					files.addAll(FileHeader.filterDeleted(position.getPhase().getNomination().getFiles()));
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
					// Check number of file types
					Set<PositionCommitteeFile> pcFiles = FileHeader.filterIncludingDeleted(position.getPhase().getCommittee().getFiles(), type);
					PositionCommitteeFile existingFile = checkNumberOfFileTypes(PositionCommitteeFile.fileTypes, type, pcFiles);
					if (existingFile != null) {
						return _updateFile(loggedOn, fileItems, existingFile);
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
					// Check number of file types
					Set<PositionEvaluationFile> eFiles = FileHeader.filterIncludingDeleted(position.getPhase().getEvaluation().getFiles(), type);
					PositionEvaluationFile existingFile2 = checkNumberOfFileTypes(PositionEvaluationFile.fileTypes, type, eFiles);
					if (existingFile2 != null) {
						return _updateFile(loggedOn, fileItems, existingFile2);
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
					// Check number of file types
					Set<ComplementaryDocumentsFile> cdFiles = FileHeader.filterIncludingDeleted(position.getPhase().getComplementaryDocuments().getFiles(), type);
					ComplementaryDocumentsFile existingFile3 = checkNumberOfFileTypes(ComplementaryDocumentsFile.fileTypes, type, cdFiles);
					if (existingFile3 != null) {
						return _updateFile(loggedOn, fileItems, existingFile3);
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
					return _updateFile(loggedOn, fileItems, committeeFile);
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
					return _updateFile(loggedOn, fileItems, evaluationFile);
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
					return _updateFile(loggedOn, fileItems, complementaryDocumentsFile);
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
					return _updateFile(loggedOn, fileItems, nominationFile);
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
					PositionCommitteeFile cf = deleteAsMuchAsPossible(committeeFile);
					if (cf == null) {
						position.getPhase().getCommittee().getFiles().remove(committeeFile);
					}
					return Response.noContent().build();
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
					PositionEvaluationFile ef = deleteAsMuchAsPossible(evaluationFile);
					if (ef == null) {
						position.getPhase().getEvaluation().getFiles().remove(evaluationFile);
					}
					return Response.noContent().build();
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
					ComplementaryDocumentsFile cdf = deleteAsMuchAsPossible(complementaryDocumentsFile);
					if (cdf == null) {
						position.getPhase().getComplementaryDocuments().getFiles().remove(complementaryDocumentsFile);
					}
					return Response.noContent().build();
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
					PositionNominationFile pnf = deleteAsMuchAsPossible(nominationFile);
					if (pnf == null) {
						position.getPhase().getNomination().getFiles().remove(nominationFile);
					}
					return Response.noContent().build();
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
	@Path("/{id:[0-9][0-9]*}/committee/professor")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<Role> getPositionCommiteeProfessors(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
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
			"select distinct m.professor from Register r " +
				"join r.members m " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and m.professor.status = :status")
			.setParameter("institutionId", position.getDepartment().getInstitution().getId())
			.setParameter("status", RoleStatus.ACTIVE)
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
	public List<PositionCommitteeMember> getPositionCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
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
		for (PositionCommitteeMember member : position.getPhase().getCommittee().getMembers()) {
			member.getProfessor().initializeCollections();
		}
		return position.getPhase().getCommittee().getMembers();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{cmId:[0-9][0-9]*}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId) {
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
		PositionCommitteeMember result = em.find(PositionCommitteeMember.class, cmId);
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
	public PositionCommitteeMember createPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, PositionCommitteeMember newMembership) {
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
			if (!existingProfessor.getStatus().equals(RoleStatus.ACTIVE)) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.status");
			}
			// Check if already exists:
			for (PositionCommitteeMember existingMember : existingPosition.getPhase().getCommittee().getMembers()) {
				if (existingMember.getProfessor().getId().equals(existingProfessor.getId())) {
					throw new RestException(Status.CONFLICT, "member.already.exists");
				}
			}
			int count = 0;
			for (PositionCommitteeMember existingMember : existingPosition.getPhase().getCommittee().getMembers()) {
				if (existingMember.getType().equals(newMembership.getType())) {
					// Count only same type
					count++;
				}
			}
			if (count >= PositionCommitteeMember.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded." + newMembership.getType());
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
	public PositionCommitteeMember updatePositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId, PositionCommitteeMember newMembership) {
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
			PositionCommitteeMember existingMember = null;
			for (PositionCommitteeMember member : existingPosition.getPhase().getCommittee().getMembers()) {
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
			PositionCommitteeMember existingCM = null;
			for (PositionCommitteeMember member : position.getPhase().getCommittee().getMembers()) {
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

	/*************************
	 * Candidacies Functions *
	 *************************/

	@GET
	@Path("/{id:[0-9][0-9]*}/candidacies")
	@JsonView({CandidacyView.class})
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

		Set<Candidacy> result = new HashSet<Candidacy>();
		for (Candidacy candidacy : position.getPhase().getCandidacies().getCandidacies()) {
			if (candidacy.isPermanent()) {
				candidacy.initializeCollections();
				result.add(candidacy);
			}
		}
		return result;
	}

	/*************************
	 * Evaluators Functions *
	 *************************/

	@GET
	@Path("/{id:[0-9][0-9]*}/evaluators/professor")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<Role> getPositionEvaluatorsProfessors(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
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
				"and r.discriminator in (:discriminators) " +
				"and r.status = :status")
			.setParameter("discriminators", discriminatorList)
			.setParameter("status", RoleStatus.ACTIVE)
			.getResultList();

		// Execute
		for (Role r : professors) {
			Professor p = (Professor) r;
			r.initializeCollections();
		}
		return professors;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/evaluators")
	@JsonView({DetailedPositionEvaluatorView.class})
	public Collection<PositionEvaluator> getPositionEvaluators(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
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
		for (PositionEvaluator evaluator : position.getPhase().getEvaluation().getEvaluators()) {
			evaluator.getProfessor().initializeCollections();
		}
		return position.getPhase().getEvaluation().getEvaluators();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/evaluators/{evalId:[0-9][0-9]*}")
	@JsonView({DetailedPositionEvaluatorView.class})
	public PositionEvaluator getPositionEvaluator(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evalId") Long evalId) {
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
		PositionEvaluator result = em.find(PositionEvaluator.class, evalId);
		if (result == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}

		if (!result.getEvaluation().getPosition().getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		result.getProfessor().initializeCollections();

		return result;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/evaluators")
	@JsonView({DetailedPositionEvaluatorView.class})
	public PositionEvaluator createPositionEvaluator(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, PositionEvaluator newEvaluator) {
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
			Professor existingProfessor = em.find(Professor.class, newEvaluator.getProfessor().getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			if (!existingProfessor.getStatus().equals(RoleStatus.ACTIVE)) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.status");
			}
			// Check if already exists:
			for (PositionEvaluator existingEvaluator : existingPosition.getPhase().getEvaluation().getEvaluators()) {
				if (existingEvaluator.getProfessor().getId().equals(existingProfessor.getId())) {
					throw new RestException(Status.CONFLICT, "position.evaluator.already.exists");
				}
			}
			// Update
			newEvaluator.setEvaluation(existingPosition.getPhase().getEvaluation());
			newEvaluator.setProfessor(existingProfessor);
			newEvaluator = em.merge(newEvaluator);

			existingPosition.getPhase().getEvaluation().getEvaluators().add(newEvaluator);
			em.flush();
			// Return result
			newEvaluator.getProfessor().initializeCollections();
			return newEvaluator;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/evaluators/{evalId:[0-9][0-9]*}")
	public void removePositionEvaluator(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evalId") Long evalId) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(loggedOn, positionId);
			if (position.getPhase().getCommittee() == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			PositionEvaluator existingEvaluator = null;
			for (PositionEvaluator evaluator : position.getPhase().getEvaluation().getEvaluators()) {
				if (evaluator.getId().equals(evalId)) {
					existingEvaluator = evaluator;
					break;
				}
			}
			if (existingEvaluator == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
			}
			// Remove
			existingEvaluator.getEvaluation().getEvaluators().remove(existingEvaluator);
			em.remove(existingEvaluator);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
