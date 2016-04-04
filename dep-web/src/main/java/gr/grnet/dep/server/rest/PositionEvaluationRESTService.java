package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluation.DetailedPositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator.DetailedPositionEvaluatorView;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionEvaluatorFile;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/position/{id:[0-9][0-9]*}/evaluation")
@Stateless
public class PositionEvaluationRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns the specific Position Evaluation info of given Position
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @return
	 * @HTTP 403 X-Error-Code insufficient.privileges
	 * @HTTP 404 X-Error-Code wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code wrong.position.id
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}")
	@JsonView({DetailedPositionEvaluationView.class})
	public PositionEvaluation getPositionEvaluation(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = null;
		try {
			existingEvaluation = em.createQuery(
					"from PositionEvaluation pe " +
							"left join fetch pe.evaluators pee " +
							"where pe.id = :evaluationId", PositionEvaluation.class)
					.setParameter("evaluationId", evaluationId)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingPosition.getPhase().getCommittee().containsMember(loggedOn) &&
				!existingEvaluation.containsEvaluator(loggedOn) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		existingEvaluation.canUpdateEvaluators();
		existingEvaluation.canUploadEvaluations();
		return existingEvaluation;
	}

	/**
	 * Returns information on spsecific evaluator of the position
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @return
	 * @HTTP 403 X-Error-Code insufficient.privileges
	 * @HTTP 404 X-Error-Code wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code wrong.position.id
	 * @HTTP 404 X-Error-Code wrong.position.evaluator.id
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}")
	@JsonView({DetailedPositionEvaluatorView.class})
	public PositionEvaluator getPositionEvaluator(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
		if (existingEvaluation == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingPosition.getPhase().getCommittee().containsMember(loggedOn) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Search:
		PositionEvaluator existingEvaluator = null;
		for (PositionEvaluator eval : existingEvaluation.getEvaluators()) {
			if (eval.getId().equals(evaluatorId)) {
				existingEvaluator = eval;
				break;
			}
		}
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		//Return
		return existingEvaluator;
	}

	/**
	 * Updates the position evaluation with evaluators
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param newEvaluation
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.register.member.id
	 * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: member.in.committe
	 * @HTTP 409 X-Error-Code: wrong.evaluators.size
	 * @HTTP 409 X-Error-Code:
	 * committee.missing.praktiko.synedriasis.epitropis.gia.aksiologites
	 */
	@PUT
	@Path("/{evaluationId:[0-9]+}")
	@JsonView({DetailedPositionEvaluationView.class})
	public PositionEvaluation updatePositionEvaluation(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, PositionEvaluation newEvaluation) {
		User loggedOn = getLoggedOn(authToken);
		final PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
		if (existingEvaluation == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.evaluation.phase");
		}
		// Validate:
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		if (newEvaluation.getEvaluators().size() != PositionEvaluator.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "wrong.evaluators.size");
		}
		if (!existingEvaluation.canUpdateEvaluators()) {
			throw new RestException(Status.CONFLICT, "committee.missing.praktiko.synedriasis.epitropis.gia.aksiologites");
		}

		// Retrieve new Members from Institution's Register
		Map<Long, PositionEvaluator> existingEvaluatorsRegisterMap = new HashMap<Long, PositionEvaluator>();
		for (PositionEvaluator existingEvaluator : existingEvaluation.getEvaluators()) {
			existingEvaluatorsRegisterMap.put(existingEvaluator.getRegisterMember().getId(), existingEvaluator);
		}
		// Retrieve new Members from Institution's Register
		Map<Long, Long> newEvaluatorsRegisterMap = new HashMap<Long, Long>();
		for (PositionEvaluator newEvaluator : newEvaluation.getEvaluators()) {
			newEvaluatorsRegisterMap.put(newEvaluator.getRegisterMember().getId(), newEvaluator.getPosition());
		}
		List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();
		if (!newEvaluatorsRegisterMap.isEmpty()) {
			TypedQuery<RegisterMember> query = em.createQuery(
					"select distinct rm from Register r " +
							"join r.members rm " +
							"where r.permanent = true " +
							"and r.institution.id = :institutionId " +
							"and rm.deleted = false " +
							"and rm.external = true " +
							"and rm.professor.status = :status " +
							"and rm.id in (:registerIds)", RegisterMember.class)
					.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
					.setParameter("status", RoleStatus.ACTIVE)
					.setParameter("registerIds", newEvaluatorsRegisterMap.keySet());
			newRegisterMembers.addAll(query.getResultList());
		}
		if (newEvaluatorsRegisterMap.keySet().size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}

		// Check if a member is Candidacy Evaluator
		for (Candidacy candidacy : existingPosition.getPhase().getCandidacies().getCandidacies()) {
			for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				if (newEvaluatorsRegisterMap.containsKey(evaluator.getRegisterMember().getId())) {
					throw new RestException(Status.CONFLICT, "member.is.candidacy.evaluator");
				}
			}
		}

		Set<Long> addedEvaluatorIds = new HashSet<Long>();
		try {
			// Update
			existingEvaluation.copyFrom(newEvaluation);
			existingEvaluation.getEvaluators().clear();
			for (RegisterMember newRegisterMember : newRegisterMembers) {
				PositionEvaluator newEvaluator = null;
				if (existingEvaluatorsRegisterMap.containsKey(newRegisterMember.getId())) {
					newEvaluator = existingEvaluatorsRegisterMap.get(newRegisterMember.getId());
				} else {
					newEvaluator = new PositionEvaluator();
					newEvaluator.setRegisterMember(newRegisterMember);
					newEvaluator.setPosition(newEvaluatorsRegisterMap.get(newRegisterMember.getId()));

					addedEvaluatorIds.add(newRegisterMember.getId());
				}
				existingEvaluation.addEvaluator(newEvaluator);
			}

			em.flush();

			// Send E-Mails
			for (final PositionEvaluator evaluator : existingEvaluation.getEvaluators()) {
				if (addedEvaluatorIds.contains(evaluator.getRegisterMember().getId())) {
					// positionEvaluation.create.evaluator@evaluator
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionEvaluation.create.evaluator@evaluator",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
									put("position", existingEvaluation.getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

									put("assistants_el", extractAssistantsInfo(existingEvaluation.getPosition(), "el"));
									put("assistants_en", extractAssistantsInfo(existingEvaluation.getPosition(), "en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
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
			}
			boolean removed = false;
			for (Long id : existingEvaluatorsRegisterMap.keySet()) {
				if (!newEvaluatorsRegisterMap.keySet().contains(id)) {
					removed = true;
					final PositionEvaluator removedEvaluator = existingEvaluatorsRegisterMap.get(id);
					// positionEvaluation.remove.evaluator@evaluator
					mailService.postEmail(removedEvaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionEvaluation.remove.evaluator@evaluator",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
									put("position", existingEvaluation.getPosition().getName());

									put("firstname_el", removedEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", removedEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", removedEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", removedEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			}

			if (!addedEvaluatorIds.isEmpty() || removed) {
				// positionEvaluation.update@committee
				for (final PositionCommitteeMember member : existingEvaluation.getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionEvaluation.update@committee",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
									put("position", existingEvaluation.getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
								}
							}));
				}
				// positionEvaluation.update@candidates
				for (final Candidacy candidacy : existingEvaluation.getPosition().getPhase().getCandidacies().getCandidacies()) {
					if (!candidacy.isWithdrawn() && candidacy.isPermanent()) {
						mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
								"default.subject",
								"positionEvaluation.update@candidates",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(existingEvaluation.getPosition().getId()));
										put("position", existingEvaluation.getPosition().getName());

										put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
										put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
										put("institution_el", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", existingEvaluation.getPosition().getDepartment().getName().get("el"));

										put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
										put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
										put("institution_en", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", existingEvaluation.getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", existingEvaluation.getPosition().getDepartment().getName().get("en"));

										put("ref", WebConstants.conf.getString("home.url") + "/apella.html#candidateCandidacies");
									}
								}));
					}
				}

			}
			// End: Send E-Mails

			// Return result
			existingEvaluation.canUpdateEvaluators();
			existingEvaluation.canUploadEvaluations();
			return existingEvaluation;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	/**
	 * Returns the list of file descriptions associated with position evaluator
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionEvaluatorFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
		return files;
	}

	/**
	 * Return the file description of the file with given id and associated with
	 * given position evaluator
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
		for (PositionEvaluatorFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Return the file data of the file with given id and associated with
	 * given position evaluator
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluatorFile> files = FileHeader.filterDeleted(existingEvaluator.getFiles());
		for (PositionEvaluatorFile file : files) {
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
	 * Handles upload of a new file associated with given position evaluator
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code:
	 * committee.missing.aitima.epitropis.pros.aksiologites
	 */
	@POST
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.evaluation.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		if (!existingEvaluation.canUploadEvaluations()) {
			throw new RestException(Status.CONFLICT, "committee.missing.aitima.epitropis.pros.aksiologites");
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
			Set<PositionEvaluatorFile> eFiles = FileHeader.filterIncludingDeleted(existingEvaluator.getFiles(), type);
			PositionEvaluatorFile existingFile2 = checkNumberOfFileTypes(PositionEvaluatorFile.fileTypes, type, eFiles);
			if (existingFile2 != null) {
				return _updateFile(loggedOn, fileItems, existingFile2);
			}

			// Create
			final PositionEvaluatorFile eFile = new PositionEvaluatorFile();
			eFile.setEvaluator(existingEvaluator);
			eFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, eFile);
			existingEvaluator.addFile(eFile);
			em.flush();

			// Send E-Mails
			if (eFile.getType().equals(FileType.AKSIOLOGISI)) {
				// positionEvaluation.upload.aksiologisi@committee
				for (final PositionCommitteeMember member : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionEvaluation.upload.aksiologisi@committee",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
									put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
								}
							}));
				}
				// positionEvaluation.upload.aksiologisi@evaluators
				for (final PositionEvaluator evaluator : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getEvaluation().getEvaluators()) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionEvaluation.upload.aksiologisi@evaluators",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
									put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
								}
							}));
				}
			} else {
				// position.upload@committee
				for (final PositionCommitteeMember member : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.upload@committee",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
									put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorCommittees");
								}
							}));
				}
				// position.upload@evaluators
				for (final PositionEvaluator evaluator : eFile.getEvaluator().getEvaluation().getPosition().getPhase().getEvaluation().getEvaluators()) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.upload@evaluators",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(eFile.getEvaluator().getEvaluation().getPosition().getId()));
									put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName().get("en"));

									put("ref", WebConstants.conf.getString("home.url") + "/apella.html#professorEvaluations");
								}
							}));
				}
			}

			return toJSON(eFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Handles upload that updates an existing file associated with position
	 * evaluator
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @param fileId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionEvaluatorFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: wrong.file.type
	 */
	@POST
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.evaluation.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
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
			if (!PositionEvaluatorFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionEvaluatorFile evaluationFile = null;
			for (PositionEvaluatorFile file : existingEvaluator.getFiles()) {
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
	 * @param positionId
	 * @param evaluationId
	 * @param evaluatorId
	 * @param fileId
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.evaluator.id
	 * @HTTP 404 X-Error-Code: wrong.position.evaluation.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.evaluation.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluator existingEvaluator = em.find(PositionEvaluator.class, evaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluator.id");
		}
		PositionEvaluation existingEvaluation = existingEvaluator.getEvaluation();
		if (!existingEvaluation.getId().equals(evaluationId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getEvaluation().getId().equals(existingEvaluation.getId())) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.evaluation.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionEvaluatorFile evaluationFile = null;
			for (PositionEvaluatorFile file : existingEvaluator.getFiles()) {
				if (file.getId().equals(fileId)) {
					evaluationFile = file;
					break;
				}
			}
			if (evaluationFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionEvaluatorFile ef = deleteAsMuchAsPossible(evaluationFile);
			if (ef == null) {
				existingEvaluator.getFiles().remove(evaluationFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/****************************
	 * RegisterMember Functions *
	 ****************************/

	/**
	 * Returns the list of registries associated with specified position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @return A list of registerMember
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/register")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Collection<Register> getPositionEvaluationRegisters(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
		if (existingEvaluation == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<Register> registers = em.createQuery(
				"select r from Register r " +
						"where r.permanent = true " +
						"and r.institution.id = :institutionId ", Register.class)
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.getResultList();

		return registers;
	}

	/**
	 * Returns a list of the register members participating in the evaluation of
	 * the specififed position
	 *
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @return
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/register/{registerId:[0-9]+}/member")
	@JsonView({DetailedRegisterMemberView.class})
	public SearchData<RegisterMember> getPositionEvaluationRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("registerId") Long registerId, @Context HttpServletRequest request) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);

		if (existingEvaluation == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.evaluation.id");
		}
		Position existingPosition = existingEvaluation.getPosition();

		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}

		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		String filterText = request.getParameter("sSearch");
		String locale = request.getParameter("locale");
		String sEcho = request.getParameter("sEcho");
		// Ordering
		String orderNo = request.getParameter("iSortCol_0");
		String orderField = request.getParameter("mDataProp_" + orderNo);
		String orderDirection = request.getParameter("sSortDir_0");
		// Pagination
		int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
		int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));

		String excludeInternal = request.getParameter("external");

		StringBuilder searchQueryString = new StringBuilder();

		searchQueryString.append("from RegisterMember m " +
				"join m.professor prof " +
				"where m.register.permanent = true " +
				"and m.deleted = false " +
				"and m.register.institution.id = :institutionId " +
				"and m.register.id = :registerId " +
				"and m.professor.status = :status ");

		// if not null, retrieve only external members
		if (excludeInternal != null) {
			searchQueryString.append("and m.external = true ");
		}

		if (StringUtils.isNotEmpty(filterText)) {
			searchQueryString.append(" and ( UPPER(m.professor.user.basicInfo.lastname) like :filterText ");
			searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.lastname) like :filterText ");
			searchQueryString.append(" or UPPER(m.professor.user.basicInfo.firstname) like :filterText ");
			searchQueryString.append(" or UPPER(m.professor.user.basicInfoLatin.firstname) like :filterText ");
			searchQueryString.append(" or CAST(m.professor.user.id AS text) like :filterText ");
			searchQueryString.append(" or (" +
					"	exists (" +
					"		select pd.id from ProfessorDomestic pd " +
					"		join pd.department.name dname " +
					"		join pd.department.school.name sname " +
					"		join pd.department.school.institution.name iname " +
					"		where pd.id = prof.id " +
					"		and ( dname like :filterText " +
					"			or sname like :filterText " +
					"			or iname like :filterText " +
					"		)" +
					"	) " +
					"	or exists (" +
					"		select pf.id from ProfessorForeign pf " +
					"		where pf.id = prof.id " +
					"		and UPPER(pf.foreignInstitution) like :filterText " +
					"	) " +
					") ) ");
		}

		Query countQuery = em.createQuery(" select count(distinct m.id) " +
				searchQueryString.toString())
				.setParameter("registerId", registerId)
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.setParameter("status", RoleStatus.ACTIVE);

		if (StringUtils.isNotEmpty(filterText)) {
			countQuery.setParameter("filterText", filterText.toUpperCase() + "%");
		}

		Long totalRecords = (Long) countQuery.getSingleResult();

		StringBuilder orderByClause = new StringBuilder();

		if (StringUtils.isNotEmpty(orderField)) {
			if (orderField.equals("id")) {
				orderByClause.append(" order by m.professor.user.id " + orderDirection);
			} else if (orderField.equals("firstname")) {
				if (locale.equals("el")) {
					orderByClause.append(" order by m.professor.user.basicInfo.firstname " + orderDirection);
				} else {
					orderByClause.append(" order by m.professor.user.basicInfoLatin.firstname " + orderDirection);
				}
			} else if (orderField.equals("lastname")) {
				if (locale.equals("el")) {
					orderByClause.append(" order by m.professor.user.basicInfo.lastname " + orderDirection);
				} else {
					orderByClause.append(" order by m.professor.user.basicInfoLatin.lastname " + orderDirection);
				}
			}
		}

		TypedQuery<RegisterMember> searchQuery = em.createQuery(
				" select m from RegisterMember m " +
						"where m.id in ( " +
						"select distinct m.id " +
						searchQueryString.toString() + ") " + orderByClause.toString(), RegisterMember.class);

		searchQuery.setParameter("registerId", registerId);
		searchQuery.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId());
		searchQuery.setParameter("status", RoleStatus.ACTIVE);

		if (StringUtils.isNotEmpty(filterText)) {
			searchQuery.setParameter("filterText", filterText.toUpperCase() + "%");
		}

		// Prepare Query
		List<RegisterMember> registerMembers = searchQuery
				.setFirstResult(iDisplayStart)
				.setMaxResults(iDisplayLength)
				.getResultList();

		// Execute
		addCommitteesCount(registerMembers);
		addEvaluationsCount(registerMembers);


		SearchData<RegisterMember> result = new SearchData<>();
		result.setiTotalRecords(totalRecords);
		result.setiTotalDisplayRecords(totalRecords);
		result.setsEcho(Integer.valueOf(sEcho));
		result.setRecords(registerMembers);

		return result;
	}

	private void addCommitteesCount(List<RegisterMember> registerMembers) {
		Map<Long, Long> countMap = new HashMap<Long, Long>();
		for (RegisterMember rm : registerMembers) {
			countMap.put(rm.getProfessor().getId(), 0L);
		}
		if (countMap.isEmpty()) {
			return;
		}

		List<Object[]> result = em.createQuery(
				"select prof.id, count(pcm.id) " +
						"from PositionCommitteeMember pcm " +
						"join pcm.registerMember.professor prof " +
						"join pcm.committee pcom " +
						"join pcom.position pos " +
						"join pos.phase ph " +
						"where ph.status =:epilogi and prof.id in (:professorIds)" +
						"group by prof.id ",
				Object[].class)
				.setParameter("epilogi", PositionStatus.EPILOGI)
				.setParameter("professorIds", countMap.keySet())
				.getResultList();

		for (Object[] count : result) {
			Long professorId = (Long) count[0];
			Long counter = (Long) count[1];
			countMap.put(professorId, counter);
		}
		for (RegisterMember rm : registerMembers) {
			rm.getProfessor().setCommitteesCount(countMap.get(rm.getProfessor().getId()));
		}
	}

	private void addEvaluationsCount(List<RegisterMember> registerMembers) {
		Map<Long, Long> countMap = new HashMap<Long, Long>();
		for (RegisterMember rm : registerMembers) {
			countMap.put(rm.getProfessor().getId(), 0L);
		}
		if (countMap.isEmpty()) {
			return;
		}

		List<Object[]> result = em.createQuery(
				"select prof.id, count(pe.id) " +
						"from PositionEvaluator pe " +
						"join pe.registerMember.professor prof " +
						"join pe.evaluation peval " +
						"join peval.position pos " +
						"join pos.phase ph " +
						"where ph.status =:epilogi and prof.id in (:professorIds)" +
						"group by prof.id ",
				Object[].class)
				.setParameter("epilogi", PositionStatus.EPILOGI)
				.setParameter("professorIds", countMap.keySet())
				.getResultList();

		for (Object[] count : result) {
			Long professorId = (Long) count[0];
			Long counter = (Long) count[1];
			countMap.put(professorId, counter);
		}
		for (RegisterMember rm : registerMembers) {
			rm.getProfessor().setEvaluationsCount(countMap.get(rm.getProfessor().getId()));
		}
	}
}