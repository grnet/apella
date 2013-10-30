package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.CandidacyEvaluator.DetailedCandidacyEvaluatorView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.util.DateUtil;

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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
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

@Path("/candidacy")
@Stateless
public class CandidacyRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Get Candidacy by it's ID
	 * 
	 * @param authToken The Authentication Token
	 * @param id The Id of the Candidacy
	 * @returnWrapped gr.grnet.dep.service.model.Candidacy
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
	@GET
	@Path("/{id:[0-9]+}")
	public String get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Candidacy candidacy = (Candidacy) em.createQuery(
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate candidate = candidacy.getCandidate();

			if (loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
				loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
				loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
				loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) ||
				candidate.getUser().getId().equals(loggedOn.getId())) {
				// Full Access
				return toJSON(candidacy, DetailedCandidacyView.class);
			}
			if (candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn) ||
				candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) {
				// Medium (without ContactInformation and ProposedEvaluation)
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			if (candidacy.isOpenToOtherCandidates() && candidacy.getCandidacies().containsCandidate(loggedOn)) {
				// Medium (without ContactInformation and ProposedEvaluation)
				return toJSON(candidacy, MediumCandidacyView.class);
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
				Candidacy existingCandidacy = (Candidacy) em.createQuery(
					"select c from Candidacy c " +
						"where c.candidate.id = :candidateId " +
						"and c.candidacies.position.id = :positionId")
					.setParameter("candidateId", candidate.getId())
					.setParameter("positionId", position.getId())
					.getSingleResult();
				// Return Results
				return existingCandidacy;
			} catch (NoResultException e) {
			}
			// Create
			candidacy.setCandidate(candidate);
			candidacy.setCandidacies(position.getPhase().getCandidacies());
			candidacy.setDate(new Date());
			candidacy.setOpenToOtherCandidates(false);
			candidacy.setPermanent(false);
			candidacy.getProposedEvaluators().clear();
			validateCandidacy(candidacy, candidate, true);
			updateSnapshot(candidacy, candidate);

			em.persist(candidacy);
			em.flush();

			// Return Results
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
	public Candidacy update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			boolean isNew = !existingCandidacy.isPermanent();
			Candidate candidate = existingCandidacy.getCandidate();
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getCandidacies().getPosition();
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			if (candidacy.getProposedEvaluators().size() > CandidacyEvaluator.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.evaluators.exceeded");
			}
			// Check files and candidate status
			validateCandidacy(existingCandidacy, candidate, isNew);
			//Check changes of Evaluators
			Set<Long> newRegisterMemberIds = new HashSet<Long>();
			Map<Long, CandidacyEvaluator> existingRegisterMembersAsMap = new HashMap<Long, CandidacyEvaluator>();
			for (CandidacyEvaluator existingEvaluator : existingCandidacy.getProposedEvaluators()) {
				existingRegisterMembersAsMap.put(existingEvaluator.getRegisterMember().getId(), existingEvaluator);
			}
			for (CandidacyEvaluator newEvaluator : candidacy.getProposedEvaluators()) {
				newRegisterMemberIds.add(newEvaluator.getRegisterMember().getId());
			}
			List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();
			if (!newRegisterMemberIds.isEmpty()) {
				Query query = em.createQuery(
					"select distinct m from Register r " +
						"join r.members m " +
						"where r.permanent = true " +
						"and r.institution.id = :institutionId " +
						"and m.professor.status = :status " +
						"and m.id in (:registerIds)")
					.setParameter("institutionId", candidacy.getCandidacies().getPosition().getDepartment().getInstitution().getId())
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
				mailService.postEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.create@institutionManager",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getUsername());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
						}
					}));
				// 2. candidacy.create@candidate
				mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.create@candidate",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getUsername());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
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
								put("evaluator_firstname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("evaluator_lastname", evaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("position", existingCandidacy.getCandidacies().getPosition().getName());
								put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
								put("candidate_firstname", existingCandidacy.getSnapshot().getBasicInfo().getFirstname());
								put("candidate_lastname", existingCandidacy.getSnapshot().getBasicInfo().getLastname());
								put("im_firstname", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getBasicInfo().getFirstname());
								put("im_lastname", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getBasicInfo().getLastname());
								put("im_email", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail());
								put("im_phone", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getPhone());
							}
						}));
				}
				// 4. candidacy.create.candidacyEvaluator@institutionManager
				mailService.postEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.create.candidacyEvaluator@institutionManager",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getUsername());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
							put("candidate_firstname", existingCandidacy.getSnapshot().getBasicInfo().getFirstname());
							put("candidate_lastname", existingCandidacy.getSnapshot().getBasicInfo().getLastname());
							Iterator<CandidacyEvaluator> it = existingCandidacy.getProposedEvaluators().iterator();
							if (it.hasNext()) {
								CandidacyEvaluator eval = it.next();
								put("evaluator1_firstname", eval.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("evaluator1_lastname", eval.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("evaluator1_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());
							} else {
								put("evaluator1_firstname", "-");
								put("evaluator1_lastname", "-");
								put("evaluator1_email", "-");
								put("evaluator2_firstname", "-");
								put("evaluator2_lastname", "-");
								put("evaluator2_email", "-");
							}
							if (it.hasNext()) {
								CandidacyEvaluator eval = it.next();
								put("evaluator2_firstname", eval.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
								put("evaluator2_lastname", eval.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
								put("evaluator2_email", eval.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail());
							} else {
								put("evaluator2_firstname", "-");
								put("evaluator2_lastname", "-");
								put("evaluator2_email", "-");
							}
						}
					}));
				// END: Send E-Mails
			}

			// Return
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
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			// Validate
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = existingCandidacy.getCandidate();
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getCandidacies().getPosition();
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)
				&& (!position.getPhase().getStatus().equals(PositionStatus.EPILOGI))) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			}
			if (position.getPhase().getStatus().equals(PositionStatus.EPILOGI) &&
				position.getPhase().getNomination().getNominationCommitteeConvergenceDate() != null
				&& DateUtil.compareDates(position.getPhase().getNomination().getNominationCommitteeConvergenceDate(), new Date()) <= 0) {
				throw new RestException(Status.CONFLICT, "wrong.position.status.committee.converged");
			}

			if (existingCandidacy.isPermanent()) {
				// Send E-Mails
				// 1. candidacy.remove@institutionManager
				mailService.postEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.remove@institutionManager",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getUsername());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
						}
					}));
				// 2. candidacy.remove@candidate
				mailService.postEmail(existingCandidacy.getCandidate().getUser().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.remove@candidate",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", existingCandidacy.getCandidacies().getPosition().getCreatedBy().getUsername());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
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
									put("username", member.getRegisterMember().getProfessor().getUser().getUsername());
									put("position", existingCandidacy.getCandidacies().getPosition().getName());
									put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
									put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
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
									put("username", evaluator.getRegisterMember().getProfessor().getUser().getUsername());
									put("position", existingCandidacy.getCandidacies().getPosition().getName());
									put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
									put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
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
									put("username", candidacy.getCandidate().getUser().getUsername());
									put("position", existingCandidacy.getCandidacies().getPosition().getName());
									put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
									put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
								}
							}));
					}
				}
				// End: Send E-Mails
			}

			// Update
			for (FileHeader fh : existingCandidacy.getFiles()) {
				deleteCompletely(fh);
			}
			for (CandidacyEvaluator eval : existingCandidacy.getProposedEvaluators()) {
				for (FileHeader fh : eval.getFiles()) {
					deleteCompletely(fh);
				}
			}
			existingCandidacy.getCandidacies().getCandidacies().remove(existingCandidacy);
			em.remove(existingCandidacy);
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
	 * @param authToken The Authentication Token
	 * @param positionId
	 * @param candidacyId
	 * @returnWrapped gr.grnet.dep.service.model.RegisterMember Array
	 */
	@GET
	@Path("/{id:[0-9]+}/register")
	@JsonView({DetailedCandidacyEvaluatorView.class})
	public Collection<RegisterMember> getCandidacyRegisterMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("id") Long candidacyId) {
		getLoggedOn(authToken);
		final Candidacy existingCandidacy = em.find(Candidacy.class, candidacyId);
		if (existingCandidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		// Prepare Query
		List<RegisterMember> registerMembers = em.createQuery(
			"select distinct m from Register r " +
				"join r.members m " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and m.professor.status = :status ")
			.setParameter("institutionId", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getId())
			.setParameter("status", RoleStatus.ACTIVE)
			.getResultList();

		return registerMembers;
	}

	/*******************************
	 * Snapshot File Functions *****
	 *******************************/

	/**
	 * Returns the list of snapshot files of this candidacy
	 * (copied from profile)
	 * 
	 * @param authToken The Authentication Token
	 * @param candidacyId
	 * @return Array of gr.grnet.dep.service.model.file.CandidateFile Array
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	 * @param authToken The authentication Token
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
	public CandidateFile getSnapshotFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	public Response getSnapshotFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	 * @param authToken The authentication Token
	 * @param candidacyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 */
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	public CandidacyFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidacies().containsCandidate(loggedOn)) {
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
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
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
		if (type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !(candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) || candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.EPILOGI))) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		} else if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
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
	 * @returnWrapped gr.grnet.dep.service.model.file.CandidacyFile
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.candidacy.id
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
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
		if (type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !(candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) || candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.EPILOGI))) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		} else if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
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
			if (type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !(candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI) || candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.EPILOGI))) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
			} else if (!type.equals(FileType.SYMPLIROMATIKA_EGGRAFA) && !candidacy.getCandidacies().getPosition().getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.CONFLICT, "wrong.position.status");
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
}
