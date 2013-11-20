package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluation;
import gr.grnet.dep.service.model.PositionEvaluation.DetailedPositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.PositionEvaluator.DetailedPositionEvaluatorView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionEvaluatorFile;

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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
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
	public PositionEvaluation getPositionEvaluation(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	public PositionEvaluator getPositionEvaluator(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	 *       committee.missing.praktiko.synedriasis.epitropis.gia.aksiologites
	 */
	@PUT
	@Path("/{evaluationId:[0-9]+}")
	@JsonView({DetailedPositionEvaluationView.class})
	public PositionEvaluation updatePositionEvaluation(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, PositionEvaluation newEvaluation) {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
			Query query = em.createQuery(
				"select distinct rm from Register r " +
					"join r.members rm " +
					"where r.permanent = true " +
					"and r.institution.id = :institutionId " +
					"and rm.external = true " +
					"and rm.professor.status = :status " +
					"and rm.id in (:registerIds)")
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.setParameter("status", RoleStatus.ACTIVE)
				.setParameter("registerIds", newEvaluatorsRegisterMap.keySet());
			newRegisterMembers.addAll(query.getResultList());
		}
		if (newEvaluatorsRegisterMap.keySet().size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}

		// Check if a member is in Commitee
		for (PositionCommitteeMember committeeMember : existingPosition.getPhase().getCommittee().getMembers()) {
			if (newEvaluatorsRegisterMap.containsKey(committeeMember.getRegisterMember().getId())) {
				throw new RestException(Status.CONFLICT, "member.in.committe");
			}
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
								put("firstname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluation.getPosition().getName());
								put("institution", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", existingEvaluation.getPosition().getDepartment().getName());
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
								put("firstname", removedEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", removedEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluation.getPosition().getName());
								put("institution", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", existingEvaluation.getPosition().getDepartment().getName());
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
								put("firstname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluation.getPosition().getName());
								put("institution", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", existingEvaluation.getPosition().getDepartment().getName());
							}
						}));
				}
				// positionEvaluation.update@candidates
				for (final Candidacy candidacy : existingEvaluation.getPosition().getPhase().getCandidacies().getCandidacies()) {
					mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"positionEvaluation.update@candidates",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", candidacy.getCandidate().getUser().getBasicInfo().getFirstname());
								put("lastname", candidacy.getCandidate().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluation.getPosition().getName());
								put("institution", existingEvaluation.getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", existingEvaluation.getPosition().getDepartment().getName());
							}
						}));
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
	public Collection<PositionEvaluatorFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	 *       committee.missing.aitima.epitropis.pros.aksiologites
	 */
	@POST
	@Path("/{evaluationId:[0-9]+}/evaluator/{evaluatorId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @Context HttpServletRequest request) throws FileUploadException, IOException {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
								put("firstname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());
								put("institution", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName());
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
								put("firstname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());
								put("institution", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName());
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
								put("firstname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", member.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());
								put("institution", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName());
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
								put("firstname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", eFile.getEvaluator().getEvaluation().getPosition().getName());
								put("institution", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName());
								put("department", eFile.getEvaluator().getEvaluation().getPosition().getDepartment().getName());
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
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionEvaluatorFile
	 * @throws FileUploadException
	 * @throws IOException
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
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("evaluatorId") Long evaluatorId, @PathParam("fileId") Long fileId) throws FileUploadException, IOException {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
	 * Returns a list of the register members participating in the evaluation of
	 * the specififed position
	 * 
	 * @param authToken
	 * @param positionId
	 * @param evaluationId
	 * @return
	 */
	@GET
	@Path("/{evaluationId:[0-9]+}/register")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<RegisterMember> getPositionEvaluationRegisterMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Prepare Query
		@SuppressWarnings("unchecked")
		List<RegisterMember> registerMembers = em.createQuery(
			"select distinct m from Register r " +
				"join r.members m " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and m.professor.status = :status " +
				"and m.external = true " +
				"and m.id not in (" +
				"	select rm.id from Position p " +
				"	join p.phase.committee.members cm " +
				"	join cm.registerMember rm " +
				"	where p.id = :positionId " +
				")")
			.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
			.setParameter("positionId", positionId)
			.setParameter("status", RoleStatus.ACTIVE)
			.getResultList();

		// Execute
		for (RegisterMember r : registerMembers) {
			Professor p = (Professor) r.getProfessor();
			p.setCommitteesCount(Professor.countCommittees(p));
		}
		return registerMembers;
	}
}
