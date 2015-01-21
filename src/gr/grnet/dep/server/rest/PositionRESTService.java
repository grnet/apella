package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
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
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.User.UserView;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.util.StringUtil;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/position")
@Stateless
public class PositionRESTService extends RESTService {

	@Inject
	private Logger log;

	private Position getPosition(Long positionId) {
		Position position = null;
		try {
			position = em.createQuery(
					"from Position p " +
							"left join fetch p.phases ph " +
							"left join fetch ph.candidacies ca " +
							"left join fetch ph.committee co " +
							"left join fetch ph.evaluation ev " +
							"left join fetch ph.nomination no " +
							"left join fetch ph.complementaryDocuments cd " +
							"where p.id = :positionId ", Position.class)
					.setParameter("positionId", positionId)
					.getSingleResult();
			for (User assistant : position.getAssistants()) {
				assistant.getRoles().size();
			}
			position.getCreatedBy().getRoles().size();
			position.getManager().getUser().getRoles().size();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		return position;
	}

	/**
	 * Returns all positions
	 *
	 * @param authToken
	 * @return A list of position
	 */
	@GET
	@JsonView({PositionView.class})
	public Collection<Position> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOnUser = getLoggedOn(authToken);
		if (loggedOnUser.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER) ||
				loggedOnUser.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {

			List<Institution> institutions = new ArrayList<Institution>();
			institutions.addAll(loggedOnUser.getAssociatedInstitutions());
			List<Position> positions = em.createQuery(
					"select distinct p from Position p " +
							"join fetch p.phase ph " +
							"join fetch ph.candidacies cs " +
							"left join fetch p.assistants " +
							"where p.permanent = true " +
							"and p.department.school.institution in (:institutions)", Position.class)
					.setParameter("institutions", institutions)
					.getResultList();

			return positions;
		} else if (loggedOnUser.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
				loggedOnUser.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
				loggedOnUser.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {

			List<Position> positions = em.createQuery(
					"select p from Position p " +
							"join fetch p.phase ph " +
							"join fetch ph.candidacies cs " +
							"left join fetch p.assistants " +
							"where p.permanent = true ", Position.class)
					.getResultList();
			return positions;
		}
		throw new RestException(Status.UNAUTHORIZED, "insufficient.privileges");
	}

	/**
	 * Returns the list of public positions
	 *
	 * @return A list of position
	 */
	@GET
	@Path("/public")
	@JsonView({PublicPositionView.class})
	public Collection<Position> getPublic() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			List<Position> positions = em.createQuery(
					"from Position p " +
							"where p.phase.candidacies.closingDate >= :today " +
							"and p.permanent = true ", Position.class)
					.setParameter("today", today)
					.getResultList();

			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}

	}

	/**
	 * Return the full position description
	 *
	 * @param authToken
	 * @param positionId
	 * @param order      If specified a specific phase will be returned instead of
	 *                   the last one
	 * @returnWrapper gr.grnet.dep.service.model.Position
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @QueryParam("order") Integer order) {
		User loggedOn = getLoggedOn(authToken);
		// All logged in users can see the positions
		Position p = getPosition(positionId);
		String result = toJSON(order == null ? p : p.as(order), DetailedPositionView.class);
		return result;
	}

	/**
	 * Creates a non-finalized position entry
	 *
	 * @param authToken
	 * @param position
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.department.id
	 */
	@POST
	@JsonView({DetailedPositionView.class})
	public Position create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Department department = em.find(Department.class, position.getDepartment().getId());
			if (department == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.department.id");
			}
			position.setDepartment(department);
			if (!position.isUserAllowedToEdit(loggedOn)) {
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
			position.getPhase().setComplementaryDocuments(new PositionComplementaryDocuments());
			position.getPhase().getComplementaryDocuments().setPosition(position);
			position.getPhase().getComplementaryDocuments().setCreatedAt(now);
			position.getPhase().getComplementaryDocuments().setUpdatedAt(now);

			position.getPhase().setNomination(null);

			position = em.merge(position);
			em.flush();

			position = getPosition(position.getId());
			return position;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Updates and finalizes the position
	 *
	 * @param authToken
	 * @param id
	 * @param position
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.sector.id
	 */
	@PUT
	@Path("/{id:[0-9]+}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			final Position existingPosition = getPosition(id);
			if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			boolean isNew = !existingPosition.isPermanent();
			// Validate
			Sector sector = em.find(Sector.class, position.getSector().getId());
			if (sector == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.sector.id");
			}
			// Only admin can change positionCandidacies dates if permanent, just ignore changes otherwise
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && existingPosition.isPermanent()) {
				position.getPhase().getCandidacies().setOpeningDate(existingPosition.getPhase().getCandidacies().getOpeningDate());
				position.getPhase().getCandidacies().setClosingDate(existingPosition.getPhase().getCandidacies().getClosingDate());
			}
			// Update

			existingPosition.copyFrom(position);
			existingPosition.setSector(sector);
			existingPosition.setSubject(utilityService.supplementSubject(position.getSubject()));
			if (isNew) {
				// Managers
				Set<Long> managerIds = new HashSet<Long>();
				for (User manager : position.getAssistants()) {
					managerIds.add(manager.getId());
				}
				if (managerIds.isEmpty()) {
					existingPosition.getAssistants().clear();
				} else {
					@SuppressWarnings("unchecked")
					List<User> assistants = em.createQuery(
							"select usr from User usr " +
									"left join fetch usr.roles rls " +
									"where usr.id in ( " +
									"	select u.id from User u " +
									"	join u.roles r " +
									"	where u.id in (:managerIds) " +
									"	and r.discriminator = :discriminator " +
									"	and u.status = :userStatus " +
									"	and r.status = :roleStatus " +
									")", User.class)
							.setParameter("managerIds", managerIds)
							.setParameter("discriminator", RoleDiscriminator.INSTITUTION_ASSISTANT)
							.setParameter("userStatus", UserStatus.ACTIVE)
							.setParameter("roleStatus", RoleStatus.ACTIVE)
							.getResultList();
					existingPosition.getAssistants().clear();
					existingPosition.getAssistants().addAll(assistants);
				}
			}
			existingPosition.setPermanent(true);
			em.flush();

			// Send E-Mails
			if (isNew) {
				sendNotificationsToInterestedCandidates(existingPosition);
				// Send to Assistants
				for (final User assistant : existingPosition.getAssistants()) {
					mailService.postEmail(assistant.getContactInfo().getEmail(),
							"default.subject",
							"position.create@institutionAssistant",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
									put("position", existingPosition.getName());

									put("firstname_el", assistant.getFirstname("el"));
									put("lastname_el", assistant.getLastname("el"));
									put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
									put("department_el", existingPosition.getDepartment().getName().get("el"));

									put("firstname_en", assistant.getFirstname("en"));
									put("lastname_en", assistant.getLastname("en"));
									put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
									put("department_en", existingPosition.getDepartment().getName().get("en"));
								}
							}));
				}

				if (loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
					// Send also to manager and alternateManager
					final InstitutionManager im = existingPosition.getManager();
					mailService.postEmail(im.getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.create@institutionManager",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
									put("position", existingPosition.getName());

									put("firstname_el", im.getUser().getFirstname("el"));
									put("lastname_el", im.getUser().getLastname("el"));
									put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
									put("department_el", existingPosition.getDepartment().getName().get("el"));

									put("firstname_en", im.getUser().getFirstname("en"));
									put("lastname_en", im.getUser().getLastname("en"));
									put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
									put("department_en", existingPosition.getDepartment().getName().get("en"));
								}
							}));
					mailService.postEmail(im.getAlternateContactInfo().getEmail(),
							"default.subject",
							"position.create@institutionManager",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingPosition.getId()));
									put("position", existingPosition.getName());

									put("firstname_el", im.getAlternateFirstname("el"));
									put("lastname_el", im.getAlternateLastname("el"));
									put("institution_el", existingPosition.getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingPosition.getDepartment().getSchool().getName().get("el"));
									put("department_el", existingPosition.getDepartment().getName().get("el"));

									put("firstname_en", im.getAlternateFirstname("en"));
									put("lastname_en", im.getAlternateLastname("en"));
									put("institution_en", existingPosition.getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingPosition.getDepartment().getSchool().getName().get("en"));
									put("department_en", existingPosition.getDepartment().getName().get("en"));
								}
							}));
				}
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
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			// Execute Query
			@SuppressWarnings("unchecked")
			List<Candidate> candidates = em.createQuery(
					"select distinct(c.candidate) from PositionSearchCriteria c " +
							"left join c.departments d " +
							"left join c.sectors s " +
							"where ((s is null) and (d is not null) and (d.id = :departmentId)) " +
							"or ((d is null) and (s is not null) and (s.id = :sectorId)) " +
							"or ((s is not null) and (s.id = :sectorId) and (d is not null) and (d.id = :departmentId))", Candidate.class)
					.setParameter("departmentId", position.getDepartment().getId())
					.setParameter("sectorId", position.getSector().getId())
					.getResultList();

			// Send E-Mails
			for (final Candidate c : candidates) {
				mailService.postEmail(c.getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.create@interested.candidates",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(position.getId()));
								put("position", position.getName());
								put("discipline", position.getSubject() != null ? position.getSubject().getName() : "");
								put("openingDate", sdf.format(position.getPhase().getCandidacies().getOpeningDate()));
								put("closingDate", sdf.format(position.getPhase().getCandidacies().getClosingDate()));

								put("firstname_el", c.getUser().getFirstname("el"));
								put("lastname_el", c.getUser().getLastname("el"));
								put("institution_el", position.getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", position.getDepartment().getSchool().getName().get("el"));
								put("department_el", position.getDepartment().getName().get("el"));

								put("firstname_en", c.getUser().getFirstname("en"));
								put("lastname_en", c.getUser().getLastname("en"));
								put("institution_en", position.getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", position.getDepartment().getSchool().getName().get("en"));
								put("department_en", position.getDepartment().getName().get("en"));
							}
						}));
			}
		} catch (ParseException e) {
			logger.log(Level.WARNING, "", e);
		}

	}

	/**
	 * Removes the position entry
	 *
	 * @param authToken
	 * @param id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.status.cannot.delete
	 */
	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getPosition(id);
			if (!position.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
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

	@GET
	@Path("/{id:[0-9][0-9]*}/assistants")
	@JsonView({UserView.class})
	public Collection<User> getPositionAssistants(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position existingPosition = getPosition(positionId);
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		List<User> managers = em.createQuery(
				"select usr from User usr " +
						"left join fetch usr.roles rls " +
						"where usr.id in ( " +
						"	select u.id from InstitutionAssistant ia " +
						"	join ia.user u " +
						"	where u.status = :userStatus " +
						"	and ia.status = :roleStatus " +
						"	and ia.institution.id = :institutionId  " +
						")", User.class)
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.getResultList();
		return managers;
	}

	/**
	 * Adds a new phase in the position
	 *
	 * @param authToken
	 * @param positionId
	 * @param position
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: position.phase.anoixti.wrong.closing.date
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: position.phase.epilogi.wrong.closing.date
	 * @HTTP 409 X-Error-Code: position.phase.anapompi.missing.apofasi
	 * @HTTP 409 X-Error-Code:
	 * position.phase.stelexomeni.missing.nominated.candidacy
	 * @HTTP 409 X-Error-Code:
	 * position.phase.stelexomeni.missing.praktiko.epilogis
	 */
	@PUT
	@Path("/{id:[0-9][0-9]*}/phase")
	@JsonView({DetailedPositionView.class})
	public Position addPhase(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long positionId, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			PositionStatus newStatus = position.getPhase().getStatus();
			Position existingPosition = getPosition(positionId);
			if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			if (!existingPosition.isPermanent()) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
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
							if (existingPhase.getCandidacies().getOpeningDate().compareTo(new Date()) > 0) {
								throw new RestException(Status.CONFLICT, "position.phase.anoixti.wrong.opening.date");
							}
							if (existingPhase.getCandidacies().getClosingDate().compareTo(new Date()) < 0) {
								throw new RestException(Status.CONFLICT, "position.phase.anoixti.wrong.closing.date");
							}
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.ANOIXTI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
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
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case ANAPOMPI:
						case STELEXOMENI:
							throw new RestException(Status.CONFLICT, "wrong.position.status");
						case CANCELLED:
							// Validate:
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.CANCELLED);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
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
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
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
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
							newPhase.setNomination(existingPhase.getNomination());
							// Add to Position
							existingPosition.addPhase(newPhase);
							break;
						case CANCELLED:
							// Validate
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.CANCELLED);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
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
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(existingPhase.getCommittee());
							newPhase.setEvaluation(existingPhase.getEvaluation());
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
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
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
							// Validate: allow only if previous Status was not ANOIXTI
							PositionPhase previousPhase = existingPosition.getPhases()
									.get(Math.max(existingPosition.getPhases().size() - 2, 0));
							if (previousPhase.getStatus().equals(PositionStatus.ANOIXTI)) {
								throw new RestException(Status.CONFLICT, "wrong.position.status");
							}
							newPhase = new PositionPhase();
							newPhase.setStatus(PositionStatus.EPILOGI);
							newPhase.setCandidacies(existingPhase.getCandidacies());
							newPhase.setComplementaryDocuments(existingPhase.getComplementaryDocuments());
							newPhase.setCommittee(new PositionCommittee());
							newPhase.getCommittee().setPosition(existingPosition);
							newPhase.setEvaluation(new PositionEvaluation());
							newPhase.getEvaluation().setPosition(existingPosition);
							newPhase.setNomination(new PositionNomination());
							newPhase.getNomination().setPosition(existingPosition);
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
			return existingPosition;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@GET
	@Path("/export")
	public Response getPositionsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Generate Document
		Long institutionId = null;
		if (loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
			institutionId = ((InstitutionManager) loggedOn.getActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)).getInstitution().getId();
		}
		if (institutionId == null &&
				loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
			institutionId = ((InstitutionAssistant) loggedOn.getActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)).getInstitution().getId();
		}
		try {
			//1. Get Data
			List<Position> positionsData = reportService.getPositionsExportData(institutionId);
			//2. Create XLS
			InputStream is = reportService.createPositionsExportExcel(positionsData);
			// Return response
			return Response.ok(is)
					.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode("positions.xlsx", "UTF-8") + "\"")
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "getDocument", e);
			throw new EJBException(e);
		}
	}
}
