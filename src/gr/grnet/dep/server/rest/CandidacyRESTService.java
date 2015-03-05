package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.EvaluatorCandidacyView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/candidacy")
@Stateless
public class CandidacyRESTService extends RESTService {

	@Inject
	private Logger log;

	private boolean canAddEvaluators(Candidacy c) {
		PositionCommittee committee = c.getCandidacies().getPosition().getPhase().getCommittee();
		return committee != null &&
				committee.getMembers().size() > 0 &&
				DateUtil.compareDates(new Date(), committee.getCandidacyEvalutionsDueDate()) <= 0;
	}

	private boolean hasNominationCommitteeConverged(Candidacy c) {
		PositionNomination nomination = c.getCandidacies().getPosition().getPhase().getNomination();
		return nomination != null &&
				nomination.getNominationCommitteeConvergenceDate() != null &&
				DateUtil.compareDates(new Date(), nomination.getNominationCommitteeConvergenceDate()) >= 0;
	}

	/**
	 * Get Candidacy by it's ID
	 *
	 * @param authToken The Authentication Token
	 * @param id        The Id of the Candidacy
	 * @returnWrapped gr.grnet.dep.service.model.Candidacy
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9]+}")
	public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Candidacy candidacy = em.createQuery(
					"from Candidacy c " +
							"left join fetch c.proposedEvaluators pe " +
							"where c.id=:id", Candidacy.class)
					.setParameter("id", id)
					.getSingleResult();
			Candidate candidate = candidacy.getCandidate();

			// Full Access ADMINISTRATOR, MINISTRY, ISNSTITUTION, OWNER
			if (loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
					loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
					loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
					loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) ||
					candidate.getUser().getId().equals(loggedOn.getId())) {
				// Full Access
				candidacy.getCandidacyEvalutionsDueDate(); // Load this to avoid lazy exception
				candidacy.setCanAddEvaluators(canAddEvaluators(candidacy));
				candidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(candidacy));
				return toJSON(candidacy, DetailedCandidacyView.class);
			}
			// Medium Access COMMITTEE MEMBER, EVALUATOR
			if ((candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) ||
					(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn))) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			// Medium Access OTHER CANDIDATE if isOpenToOtherCandidates
			if (candidacy.isOpenToOtherCandidates() && candidacy.getCandidacies().containsCandidate(loggedOn)) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			// Medium Access CANDIDACY EVALUATOR
			if (candidacy.containsEvaluator(loggedOn)) {
				// Medium (without ContactInformation)
				return toJSON(candidacy, EvaluatorCandidacyView.class);
			}
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
	}

	/**
	 * Creates a new Candidacy, not finalized
	 *
	 * @param authToken The Authentication Token
	 * @param candidacy
	 * @return The new candidacy, with ID
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 403 X-Error-Code: wrong.position.candidacies.openingDate
	 * @HTTP 403 X-Error-Code: wrong.position.candidacies.closingDate
	 * @HTTP 404 X-Error-Code: wrong.candidate.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@POST
	@JsonView({DetailedCandidacyView.class})
	public Candidacy create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			Candidate candidate = em.find(Candidate.class, candidacy.getCandidate().getId());
			if (candidate == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
			}
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = em.find(Position.class, candidacy.getCandidacies().getPosition().getId());
			if (position == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			Date now = new Date();
			if (DateUtil.compareDates(position.getPhase().getCandidacies().getOpeningDate(), now) > 0) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.openingDate");
			}
			if (DateUtil.compareDates(position.getPhase().getCandidacies().getClosingDate(), now) < 0) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.closingDate");
			}
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.FORBIDDEN, "wrong.position.status");
			}
			try {
				Candidacy existingCandidacy = em.createQuery(
						"select c from Candidacy c " +
								"where c.candidate.id = :candidateId " +
								"and c.candidacies.position.id = :positionId", Candidacy.class)
						.setParameter("candidateId", candidate.getId())
						.setParameter("positionId", position.getId())
						.getSingleResult();
				// Return Results
				existingCandidacy.getCandidacyEvalutionsDueDate();
				existingCandidacy.setDate(new Date());
				CandidacyStatus status = new CandidacyStatus();
				status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
				status.setDate(new Date());
				existingCandidacy.addCandidacyStatus(status);

				em.merge(existingCandidacy);
				em.flush();

				return existingCandidacy;
			} catch (NoResultException e) {
			}
			// Create
			candidacy.setCandidate(candidate);
			candidacy.setCandidacies(position.getPhase().getCandidacies());
			candidacy.setDate(new Date());
			CandidacyStatus status = new CandidacyStatus();
			status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
			status.setDate(new Date());
			candidacy.addCandidacyStatus(status);
			candidacy.setOpenToOtherCandidates(false);
			candidacy.setPermanent(false);
			candidacy.getProposedEvaluators().clear();
			validateCandidacy(candidacy, candidate, true);
			updateSnapshot(candidacy, candidate);

			em.persist(candidacy);
			em.flush();

			// Return Results
			candidacy.getCandidacyEvalutionsDueDate();
			candidacy.setCanAddEvaluators(canAddEvaluators(candidacy));
			candidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(candidacy));
			return candidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Saves and finalizes candidacy
	 *
	 * @param authToken The Authentication Token
	 * @param id
	 * @param candidacy
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: max.evaluators.exceeded
	 */
	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			boolean isNew = !existingCandidacy.isPermanent();
			Candidate candidate = existingCandidacy.getCandidate();
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getCandidacies().getPosition();
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI) &&
					!position.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			// Only one field is not allowed to change after closing date: isOpenToOtherCandidates
			if ((existingCandidacy.isOpenToOtherCandidates() ^ candidacy.isOpenToOtherCandidates()) &&
					!existingCandidacy.getCandidacies().getPosition().getPhase().getClientStatus().equals(PositionStatus.ANOIXTI.toString())) {
				throw new RestException(Status.CONFLICT, "wrong.position.candidacies.closingDate");
			}
			Set<Long> newRegisterMemberIds = new HashSet<Long>();
			for (CandidacyEvaluator newEvaluator : candidacy.getProposedEvaluators()) {
				if (newEvaluator.getRegisterMember() != null && newEvaluator.getRegisterMember().getId() != null) {
					if(newRegisterMemberIds.contains(newEvaluator.getRegisterMember().getId())){
						throw new RestException(Status.CONFLICT, "duplicate.evaluators");
					}
					newRegisterMemberIds.add(newEvaluator.getRegisterMember().getId());
				}
			}
			if (newRegisterMemberIds.size() > 0 && !canAddEvaluators(existingCandidacy)) {
				throw new RestException(Status.CONFLICT, "committee.not.defined");
			}
			if (candidacy.getProposedEvaluators().size() > CandidacyEvaluator.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.evaluators.exceeded");
			}
			// Check files and candidate status
			validateCandidacy(existingCandidacy, candidate, isNew);

			//Check changes of Evaluators
			Map<Long, CandidacyEvaluator> existingRegisterMembersAsMap = new HashMap<Long, CandidacyEvaluator>();
			for (CandidacyEvaluator existingEvaluator : existingCandidacy.getProposedEvaluators()) {
				existingRegisterMembersAsMap.put(existingEvaluator.getRegisterMember().getId(), existingEvaluator);
			}
			List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();
			if (!newRegisterMemberIds.isEmpty()) {
				TypedQuery<RegisterMember> query = em.createQuery(
						"select distinct m from Register r " +
								"join r.members m " +
								"where r.permanent = true " +
								"and r.institution.id = :institutionId " +
								"and m.deleted = false " +
								"and m.professor.status = :status " +
								"and m.id in (:registerIds)", RegisterMember.class)
						.setParameter("institutionId", candidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getId())
						.setParameter("status", RoleStatus.ACTIVE)
						.setParameter("registerIds", newRegisterMemberIds);
				newRegisterMembers.addAll(query.getResultList());
			}
			Collection<CandidacyEvaluator> addedEvaluators = new ArrayList<CandidacyEvaluator>();

			// Update
			if (isNew) {
				// Fetch from Profile
				updateSnapshot(existingCandidacy, candidate);
			}
			existingCandidacy.setPermanent(true);
			existingCandidacy.setWithdrawn(false);
			existingCandidacy.setOpenToOtherCandidates(candidacy.isOpenToOtherCandidates());
			existingCandidacy.getProposedEvaluators().clear();
			for (RegisterMember newRegisterMember : newRegisterMembers) {
				CandidacyEvaluator candidacyEvaluator = null;
				if (existingRegisterMembersAsMap.containsKey(newRegisterMember.getId())) {
					candidacyEvaluator = existingRegisterMembersAsMap.get(newRegisterMember.getId());
				} else {
					candidacyEvaluator = new CandidacyEvaluator();
					candidacyEvaluator.setCandidacy(existingCandidacy);
					candidacyEvaluator.setRegisterMember(newRegisterMember);
					addedEvaluators.add(candidacyEvaluator);
				}
				existingCandidacy.addProposedEvaluator(candidacyEvaluator);
			}

			em.flush();

			if (isNew) {
				// Send E-Mails
				// 1. candidacy.create@institutionManager
				for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
					mailService.postEmail(recipient.get("email"),
							"default.subject",
							"candidacy.create@institutionManager",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									putAll(recipient);

									put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
									put("position", existingCandidacy.getCandidacies().getPosition().getName());

									put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
									put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

									put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
									put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

									put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// 2. candidacy.create@candidate
				mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"candidacy.create@candidate",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
								put("position", existingCandidacy.getCandidacies().getPosition().getName());

								put("firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
								put("lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));
								put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

								put("firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
								put("lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));
								put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
							}
						}));

			}
			if (!addedEvaluators.isEmpty()) {
				// 3. candidacy.create.candidacyEvaluator@candidacyEvaluator
				for (final CandidacyEvaluator evaluator : addedEvaluators) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"candidacy.create.candidacyEvaluator@candidacyEvaluator",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
									put("position", existingCandidacy.getCandidacies().getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));
									put("candidate_firstname_el", existingCandidacy.getSnapshot().getFirstname("el"));
									put("candidate_lastname_el", existingCandidacy.getSnapshot().getLastname("el"));
									put("assistants_el", extractAssistantsInfo(existingCandidacy.getCandidacies().getPosition(), "el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
									put("candidate_firstname_en", existingCandidacy.getSnapshot().getFirstname("en"));
									put("candidate_lastname_en", existingCandidacy.getSnapshot().getLastname("en"));
									put("assistants_en", extractAssistantsInfo(existingCandidacy.getCandidacies().getPosition(), "en"));

								}

								private String extractAssistantsInfo(Position position, String locale) {
									StringBuilder info = new StringBuilder();
									info.append("<ul>");

									InstitutionManager manager = position.getManager();
									String im_firstname = manager.getUser().getFirstname(locale);
									String im_lastname = manager.getUser().getLastname(locale);
									String im_email = manager.getUser().getContactInfo().getEmail();
									String im_phone = manager.getUser().getContactInfo().getPhone();
									info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");

									im_firstname = manager.getAlternateFirstname(locale);
									im_lastname = manager.getAlternateLastname(locale);
									im_email = manager.getAlternateContactInfo().getEmail();
									im_phone = manager.getAlternateContactInfo().getPhone();
									info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");

									info.append("<li><ul>");
									Set<User> assistants = new HashSet<User>();
									assistants.addAll(position.getAssistants());
									if (position.getCreatedBy().getPrimaryRole().equals(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
										assistants.add(position.getCreatedBy());
									}
									for (User u : assistants) {
										if (u.getId().equals(manager.getUser().getId())) {
											continue;
										}
										im_firstname = u.getFirstname(locale);
										im_lastname = u.getLastname(locale);
										im_email = u.getContactInfo().getEmail();
										im_phone = u.getContactInfo().getPhone();
										info.append("<li>" + im_firstname + " " + im_lastname + ", " + im_email + ", " + im_phone + "</li>");
									}
									info.append("</ul></li>");

									info.append("</ul>");
									return info.toString();
								}

							}));
				}
				// 4. candidacy.create.candidacyEvaluator@institutionManager
				for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
					mailService.postEmail(recipient.get("email"),
							"default.subject",
							"candidacy.create.candidacyEvaluator@institutionManager",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									putAll(recipient); //firstname, lastname (el|en)

									put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
									put("position", existingCandidacy.getCandidacies().getPosition().getName());

									put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));
									put("candidate_firstname_el", existingCandidacy.getSnapshot().getFirstname("el"));
									put("candidate_lastname_el", existingCandidacy.getSnapshot().getLastname("el"));

									put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
									put("candidate_firstname_en", existingCandidacy.getSnapshot().getFirstname("en"));
									put("candidate_lastname_en", existingCandidacy.getSnapshot().getLastname("en"));

									Iterator<CandidacyEvaluator> it = existingCandidacy.getProposedEvaluators().iterator();
									if (it.hasNext()) {
										CandidacyEvaluator eval = it.next();
										put("evaluator1_firstname_el", eval.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("evaluator1_lastname_el", eval.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("evaluator1_firstname_en", eval.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("evaluator1_lastname_en", eval.getRegisterMember().getProfessor().getUser().getLastname("en"));

										put("evaluator1_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());

									} else {
										put("evaluator1_firstname_el", "-");
										put("evaluator1_lastname_el", "-");
										put("evaluator1_firstname_en", "-");
										put("evaluator1_lastname_en", "-");

										put("evaluator1_email", "-");

										put("evaluator2_firstname_el", "-");
										put("evaluator2_lastname_el", "-");
										put("evaluator2_firstname_en", "-");
										put("evaluator2_lastname_en", "-");

										put("evaluator2_email", "-");

									}
									if (it.hasNext()) {
										CandidacyEvaluator eval = it.next();
										put("evaluator2_firstname_el", eval.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("evaluator2_lastname_el", eval.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("evaluator2_firstname_en", eval.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("evaluator2_lastname_en", eval.getRegisterMember().getProfessor().getUser().getLastname("en"));

										put("evaluator2_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());
									} else {
										put("evaluator2_firstname_el", "-");
										put("evaluator2_lastname_el", "-");
										put("evaluator2_firstname_en", "-");
										put("evaluator2_lastname_en", "-");

										put("evaluator2_email", "-");
									}
								}
							}));
					// END: Send E-Mails
				}
			}

			// Return
			existingCandidacy.getCandidacyEvalutionsDueDate();
			existingCandidacy.setCanAddEvaluators(canAddEvaluators(existingCandidacy));
			existingCandidacy.setNominationCommitteeConverged(hasNominationCommitteeConverged(existingCandidacy));
			return existingCandidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Deletes the submitted candidacy
	 *
	 * @param authToken The Authentication Token
	 * @param id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: wrong.position.status.committee.converged
	 */
	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = existingCandidacy.getCandidate();
			if (!candidate.getUser().getId().equals(loggedOn.getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getCandidacies().getPosition();
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)
					&& (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI))) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			// NominationCommitteeConvergenceDate
			if (hasNominationCommitteeConverged(existingCandidacy)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
			}

			if (existingCandidacy.isPermanent()) {
				// Send E-Mails
				// 1. candidacy.remove@institutionManager
				for (final Map<String, String> recipient : existingCandidacy.getCandidacies().getPosition().getEmailRecipients()) {
					mailService.postEmail(recipient.get("email"),
							"default.subject",
							"candidacy.remove@institutionManager",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									putAll(recipient); //firstname, lastname (el|en)

									put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
									put("position", existingCandidacy.getCandidacies().getPosition().getName());

									put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
									put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

									put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
									put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

									put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// 2. candidacy.remove@candidate
				mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"candidacy.remove@candidate",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
								put("position", existingCandidacy.getCandidacies().getPosition().getName());

								put("firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
								put("lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));
								put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

								put("firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
								put("lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));
								put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));

							}
						}));
				// 3. candidacy.remove@committee
				if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
					for (final PositionCommitteeMember member : existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
						mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
								"default.subject",
								"candidacy.remove@committee",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
										put("position", existingCandidacy.getCandidacies().getPosition().getName());

										put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
										put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

										put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
										put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

										put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

										put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
										put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
									}
								}));
					}
				}
				// 4. candidacy.remove@evaluators
				if (existingCandidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null) {
					for (final PositionEvaluator evaluator : existingCandidacy.getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
						mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
								"default.subject",
								"candidacy.remove@evaluators",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
										put("position", existingCandidacy.getCandidacies().getPosition().getName());

										put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
										put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

										put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
										put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

										put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

										put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
										put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
									}
								}));
					}
				}
				// 5. candidacy.remove@candidates 
				for (final Candidacy candidacy : existingCandidacy.getCandidacies().getCandidacies()) {
					if (candidacy.isPermanent() && !candidacy.getId().equals(existingCandidacy.getId())) {
						mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
								"default.subject",
								"candidacy.remove@candidates",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(existingCandidacy.getCandidacies().getPosition().getId()));
										put("position", existingCandidacy.getCandidacies().getPosition().getName());

										put("candidate_firstname_el", existingCandidacy.getCandidate().getUser().getFirstname("el"));
										put("candidate_lastname_el", existingCandidacy.getCandidate().getUser().getLastname("el"));

										put("candidate_firstname_en", existingCandidacy.getCandidate().getUser().getFirstname("en"));
										put("candidate_lastname_en", existingCandidacy.getCandidate().getUser().getLastname("en"));

										put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
										put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
										put("institution_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("el"));

										put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
										put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
										put("institution_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", existingCandidacy.getCandidacies().getPosition().getDepartment().getName().get("en"));
									}
								}));
					}
				}
				// End: Send E-Mails
			}
			// Update
			existingCandidacy.setWithdrawn(true);
			existingCandidacy.setWithdrawnDate(new Date());
			if (DateUtil.compareDates(new Date(), existingCandidacy.getCandidacies().getClosingDate()) <= 0) {
				existingCandidacy.setPermanent(false);
			}
			CandidacyStatus status = new CandidacyStatus();
			status.setAction(CandidacyStatus.ACTIONS.WITHDRAW);
			status.setDate(new Date());
			existingCandidacy.addCandidacyStatus(status);

			em.merge(existingCandidacy);
			em.flush();

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/*********************
	 * Register Members **
	 *********************/

	/**
	 * Returns the list of Register Members selectable
	 * as Evaluators for this Candidacy
	 *
	 * @param authToken   The Authentication Token
	 * @param positionId
	 * @param candidacyId
	 * @returnWrapped gr.grnet.dep.service.model.RegisterMember Array
	 */
	@GET
	@Path("/{id:[0-9]+}/register")
	@JsonView({CandidacyEvaluator.CandidacyEvaluatorView.class})
	public Collection<RegisterMember> getCandidacyRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		final Candidacy existingCandidacy = em.find(Candidacy.class, candidacyId);
		if (existingCandidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		Candidate candidate = existingCandidacy.getCandidate();
		if (!candidate.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return empty response while committee is not defined
		if (!canAddEvaluators(existingCandidacy)) {
			return new ArrayList<RegisterMember>();
		}
		// Prepare Data for Query
		Institution institution = existingCandidacy.getCandidacies().getPosition().getDepartment().getSchool().getInstitution();
		Set<Long> committeeMemberIds = new HashSet<Long>();
		if (existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee() != null) {
			PositionCommittee committee = existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee();
			for (PositionCommitteeMember member : committee.getMembers()) {
				committeeMemberIds.add(member.getRegisterMember().getId());
			}
		}
		if (committeeMemberIds.isEmpty()) {
			// This should not happen, but just to avoid exceptions in case it does
			return new ArrayList<RegisterMember>();
		}
		// Run Query
		List<RegisterMember> registerMembers = em.createQuery(
				"select distinct m from Register r " +
						"join r.members m " +
						"where r.permanent = true " +
						"and r.institution.id = :institutionId " +
						"and m.deleted = false " +
						"and m.professor.status = :status " +
						"and m.id not in (:committeeMemberIds)", RegisterMember.class)
				.setParameter("institutionId", institution.getId())
				.setParameter("status", RoleStatus.ACTIVE)
				.setParameter("committeeMemberIds", committeeMemberIds)
				.getResultList();

		// Return result
		return registerMembers;
	}

	/*******************************
	 * Snapshot File Functions *****
	 *******************************/

	/**
	 * Returns the list of snapshot files of this candidacy
	 * (copied from profile)
	 *
	 * @param authToken   The Authentication Token
	 * @param candidacyId
	 * @return Array of gr.grnet.dep.service.model.file.CandidateFile Array
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/snapshot/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidateFile> getSnapshotFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		candidacy.getSnapshotFiles().size();
		return candidacy.getSnapshotFiles();
	}

	/**
	 * Returns the file description with the given id
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidateFile getSnapshotFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
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

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return file body
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/snapshot/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getSnapshotFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
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

	/**
	 * Returns files specific to this candidacy
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<CandidacyFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(candidacy.getFiles());
	}

	/**
	 * Returns file description of a file in candidacy
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public CandidacyFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidacyFile file : candidacy.getFiles()) {
			if (file.getId().equals(fileId) && !file.isDeleted()) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.containsEvaluator(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (CandidacyFile file : candidacy.getFiles()) {
			if (file.getId().equals(fileId) && !file.isDeleted()) {
				for (FileBody fb : file.getBodies()) {
					if (fb.getId().equals(bodyId)) {
						return sendFileBody(fb);
					}
				}
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Uploads a new file
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
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
		// Other files until closing date
		if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
				!candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) &&
				DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		//SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
		if (hasNominationCommitteeConverged(candidacy)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
		}
		// Check number of file types
		CandidacyFile existingFile = checkNumberOfFileTypes(CandidacyFile.fileTypes, type, candidacy.getFiles());
		if (existingFile != null) {
			return _updateFile(loggedOn, fileItems, existingFile);
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

	/**
	 * Uploads and updates an existing file
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
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
		// Other files until closing date
		if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
				!candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) &&
				DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		//SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
		if (hasNominationCommitteeConverged(candidacy)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
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
		return _updateFile(loggedOn, fileItems, candidacyFile);
	}

	/**
	 * Removes the file body
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		try {
			CandidacyFile candidacyFile = null;
			for (CandidacyFile file : candidacy.getFiles()) {
				if (file.getId().equals(fileId) && !file.isDeleted()) {
					candidacyFile = file;
					break;
				}
			}
			if (candidacyFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			FileType type = candidacyFile.getType();
			// Other files until closing date
			if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) &&
					!candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) &&
					DateUtil.compareDates(new Date(), candidacy.getCandidacies().getClosingDate()) >= 0) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			//SYMPLIROMATIKA EGGRAFA until getNominationCommitteeConvergenceDate
			if (hasNominationCommitteeConverged(candidacy)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
			}
			CandidacyFile cf = deleteAsMuchAsPossible(candidacyFile);
			if (cf == null) {
				candidacy.getFiles().remove(candidacyFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/*******************************
	 * Evaluation File Functions ***
	 *******************************/

	/**
	 * Returns the list of evaluation files of this candidacy
	 *
	 * @param authToken   The Authentication Token
	 * @param candidacyId
	 * @return Array of gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * Array
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}/evaluation/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCandidaciesFile> getEvaluationFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		PositionCandidacies existingCandidacies = candidacy.getCandidacies();
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Search
		List<PositionCandidaciesFile> result = em.createQuery(
				"select pcf from PositionCandidaciesFile pcf " +
						"where pcf.deleted = false " +
						"and pcf.candidacies.id = :pcId " +
						"and pcf.evaluator.candidacy.id = :cid ", PositionCandidaciesFile.class)
				.setParameter("pcId", existingCandidacies.getId())
				.setParameter("cid", candidacy.getId())
				.getResultList();

		// Return Result
		return result;
	}

	/**
	 * Returns the file description with the given id
	 *
	 * @param authToken   The authentication Token
	 * @param candidacyId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCandidaciesFile getEvaluationFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		PositionCandidacies existingCandidacies = candidacy.getCandidacies();
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Find File
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		for (PositionCandidaciesFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Returns the actual file body (binary)
	 *
	 * @param authToken
	 * @param candidacyId
	 * @param fileId
	 * @param bodyId
	 * @return file body
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/evaluation/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getEvaluationFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		PositionCandidacies existingCandidacies = candidacy.getCandidacies();
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(candidacy.getCandidacies().getPosition().getDepartment()) &&
				(candidacy.getCandidacies().getPosition().getPhase().getCommittee() != null && !candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn)) &&
				(candidacy.getCandidacies().getPosition().getPhase().getEvaluation() != null && !candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!candidacy.getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (candidacy.getCandidacies().containsCandidate(loggedOn) &&
				!candidacy.isOpenToOtherCandidates() &&
				!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		for (PositionCandidaciesFile file : files) {
			if (file.getId().equals(fileId) && file.getCurrentBody().getId().equals(bodyId)) {
				return sendFileBody(file.getCurrentBody());
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Path("/incompletecandidacies")
	@JsonView({MediumCandidacyView.class})
	public Collection<Candidacy> getIncompleteCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
        Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
        if (!admin.isSuperAdministrator()) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }

		String queryString = "from Candidacy c " +
				"left join fetch c.candidate.user.roles cerls " +
				"left join fetch c.candidacies ca " +
				"left join fetch ca.position po " +
				"where c.permanent = false " +
				"or c.withdrawn = true ";

		TypedQuery<Candidacy> query = em.createQuery(queryString, Candidacy.class);
		List<Candidacy> retv = query.getResultList();

		return retv;
	}


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({Candidacy.DetailedCandidacyView.class})
    @Path("/submitcandidacy")
    public Candidacy createCandidacy(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Candidacy candidacy) {
        User loggedOn = getLoggedOn(authToken);
        // Authenticate
        if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }
        try {
            User user = em.find(User.class, candidacy.getCandidate().getUser().getId());
            if (user == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.user.id");
            }
            // Validate
            Candidate candidate = em.find(Candidate.class, user.getRole(RoleDiscriminator.CANDIDATE).getId());
            if (candidate == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
            }
            Position position = em.find(Position.class, candidacy.getCandidacies().getPosition().getId());
            if (position == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.position.id");
            }
            Candidacy existingCandidacy = null;
            try {
                existingCandidacy = em.createQuery(
                        "select c from Candidacy c " +
                                "where c.candidate.id = :candidateId " +
                                "and c.candidacies.position.id = :positionId", Candidacy.class)
                        .setParameter("candidateId", candidate.getId())
                        .setParameter("positionId", position.getId())
                        .getSingleResult();
            } catch (NoResultException e) {
            }

            if (existingCandidacy != null) {
                if(existingCandidacy.isPermanent() &&  !existingCandidacy.isWithdrawn()){
                    throw new RestException(Status.CONFLICT, "candidacy.already.submitted");
                }
                existingCandidacy.setPermanent(true);
				existingCandidacy.setWithdrawn(false);
				existingCandidacy.setWithdrawnDate(null);
                existingCandidacy.setDate(new Date());
                CandidacyStatus status = new CandidacyStatus();
                status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
                status.setDate(new Date());
                existingCandidacy.addCandidacyStatus(status);
                candidacy = existingCandidacy;
            } else {
                candidacy.setCandidate(candidate);
                candidacy.setCandidacies(position.getPhase().getCandidacies());
                candidacy.setDate(new Date());
                candidacy.setOpenToOtherCandidates(false);
				candidacy.setPermanent(true);
				candidacy.setWithdrawn(false);
				candidacy.setWithdrawnDate(null);
                CandidacyStatus status = new CandidacyStatus();
                status.setAction(CandidacyStatus.ACTIONS.SUBMIT);
                status.setDate(new Date());
                candidacy.addCandidacyStatus(status);

                candidacy.getProposedEvaluators().clear();
                validateCandidacy(candidacy, candidate, true); // isNew
                updateSnapshot(candidacy, candidate);
            }

            candidacy = em.merge(candidacy);
            em.flush();

            return candidacy;
        } catch (PersistenceException e) {
            sc.setRollbackOnly();
            throw new RestException(Status.BAD_REQUEST, "persistence.exception");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id:[0-9]+}/statushistory")
    public Response getCandidacyStatusHistoryList (@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId) {
        User loggedOn = getLoggedOn(authToken);

        Candidacy candidacy = em.find(Candidacy.class, candidacyId);
        if(candidacy == null){
            throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
        }
        Position position = candidacy.getCandidacies().getPosition();
        Set<Long> userIds = new HashSet<>();

        for(CandidacyEvaluator candidacyEvaluator : candidacy.getProposedEvaluators()){
            userIds.add(candidacyEvaluator.getRegisterMember().getProfessor().getUser().getId());
        }
        if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
                !loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
                !userIds.contains(loggedOn.getId())) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }
        List<CandidacyStatus> statusList = em.createQuery("Select u from CandidacyStatus u where u.candidacy.id=:candidacyId order by u.date desc")
                .setParameter("candidacyId", candidacyId)
                .getResultList();

        return Response.ok(statusList).build();
    }

}
