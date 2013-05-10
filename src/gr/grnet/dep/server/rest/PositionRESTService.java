package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.CandidatePositionView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.MemberPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCommittee;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;
import gr.grnet.dep.service.model.PositionEvaluation;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.PositionPhase;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

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
	public Collection<Position> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
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
	public Collection<Position> getPublic() {
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
	public String get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @QueryParam("order") Integer order) {
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
				result = toJSON(order == null ? p : p.as(order), MemberPositionView.class);
			} else if (phase.getEvaluation() != null && phase.getEvaluation().containsEvaluator(loggedOn)) {
				result = toJSON(order == null ? p : p.as(order), MemberPositionView.class);
			} else {
				result = toJSON(order == null ? p : p.as(order), PositionView.class);
			}
		} else if (loggedOn.hasActiveRole(RoleDiscriminator.CANDIDATE)) {
			PositionPhase phase = order == null ? p.getPhase() : p.getPhases().get(order);
			if (phase.getCandidacies().containsCandidate(loggedOn)) {
				result = toJSON(order == null ? p : p.as(order), CandidatePositionView.class);
			} else {
				result = toJSON(order == null ? p : p.as(order), PositionView.class);
			}
		} else {
			result = toJSON(order == null ? p : p.as(order), PositionView.class);
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
			position.setCreatedBy(loggedOn);

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
			boolean isNew = !existingPosition.isPermanent();

			// Validate
			Sector sector = em.find(Sector.class, position.getSector().getId());
			if (sector == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.sector.id");
			}

			// Update
			existingPosition.copyFrom(position);
			existingPosition.setSector(sector);
			existingPosition.setSubject(supplementSubject(position.getSubject()));
			existingPosition.setPermanent(true);
			em.flush();

			// Send E-Mails
			existingPosition.initializeCollections();
			if (isNew) {
				sendNotificationsToInterestedCandidates(existingPosition);
			}

			// Return result
			return existingPosition;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	private void sendNotificationsToInterestedCandidates(final Position position) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			// Execute Query
			@SuppressWarnings("unchecked")
			List<Candidate> candidates = (List<Candidate>) em.createQuery(
				"select distinct(c.candidate) from PositionSearchCriteria c " +
					"left join c.departments d " +
					"left join c.sectors s " +
					"where ((s is null) and (d is not null) and (d.id = :departmentId)) " +
					"or ((d is null) and (s is not null) and (s.id = :sectorId)) " +
					"or ((s is not null) and (s.id = :sectorId) and (d is not null) and (d.id = :departmentId))")
				.setParameter("departmentId", position.getDepartment().getId())
				.setParameter("sectorId", position.getSector().getId())
				.getResultList();

			// Send E-Mails
			for (final Candidate c : candidates) {
				sendEmail(c.getUser().getContactInfo().getEmail(),
					"default.subject",
					"position.create@interested.candidates",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", c.getUser().getUsername());
							put("position", position.getName());
							put("institution", position.getDepartment().getInstitution().getName());
							put("department", position.getDepartment().getDepartment());
						}
					}));
			}
		} catch (ParseException e) {
			logger.log(Level.WARNING, "", e);
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
					switch (newStatus) {
						case ENTAGMENI:
						case ANOIXTI:
						case EPILOGI:
						case ANAPOMPI:
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
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
				case ANAPOMPI:
					switch (newStatus) {
						case ENTAGMENI:
						case ANOIXTI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case EPILOGI:
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
}
