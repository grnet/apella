package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.util.DateUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
				candidacy.initializeCollections();
				return toJSON(candidacy, DetailedCandidacyView.class);
			}
			if (candidacy.getCandidacies().getPosition().getPhase().getCommittee().containsMember(loggedOn) ||
				candidacy.getCandidacies().getPosition().getPhase().getEvaluation().containsEvaluator(loggedOn)) {
				// Medium (without ContactInformation and ProposedEvaluation)
				candidacy.initializeCollections();
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			if (candidacy.isOpenToOtherCandidates() && candidacy.getCandidacies().containsCandidate(loggedOn)) {
				// Medium (without ContactInformation and ProposedEvaluation)
				candidacy.initializeCollections();
				return toJSON(candidacy, MediumCandidacyView.class);
			}
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
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
				throw new RestException(Status.NOT_FOUND, "wrong.position.status");
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
				existingCandidacy.initializeCollections();
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
			validateCandidacy(candidacy, candidate);
			updateSnapshot(candidacy, candidate);

			em.persist(candidacy);
			em.flush();

			// Return Results
			candidacy.initializeCollections();
			return candidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
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
			final Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate candidate = existingCandidacy.getCandidate();
			if (candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Position position = existingCandidacy.getCandidacies().getPosition();
			if (!position.getPhase().getStatus().equals(PositionStatus.ANOIXTI)) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.status");
			}
			if (candidacy.getProposedEvaluators().size() > CandidacyEvaluator.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.evaluators.exceeded");
			}
			validateCandidacy(existingCandidacy, candidate);

			// Update
			existingCandidacy.setOpenToOtherCandidates(candidacy.isOpenToOtherCandidates());
			existingCandidacy.getProposedEvaluators().clear();
			for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				if (!evaluator.isMissingRequiredField()) {
					existingCandidacy.addProposedEvaluator(evaluator);
				}
			}
			updateSnapshot(existingCandidacy, candidate);
			if (!existingCandidacy.isPermanent()) {
				existingCandidacy.setPermanent(true);
			}
			em.flush();

			// Send E-Mails
			// 1. candidacy.create@institutionManager
			sendEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
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
			// 2. candidacy.create.candidacyEvaluator@candidacyEvaluator
			for (final CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				sendEmail(evaluator.getEmail(),
					"default.subject",
					"candidacy.create.candidacyEvaluator@candidacyEvaluator",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("evaluator_name", evaluator.getFullname());
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
			// 3. candidacy.create.candidacyEvaluator@institutionManager
			for (final CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				sendEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
					"default.subject",
					"candidacy.create.candidacyEvaluator@institutionManager",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("evaluator_name", evaluator.getFullname());
							put("position", existingCandidacy.getCandidacies().getPosition().getName());
							put("institution", existingCandidacy.getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingCandidacy.getCandidacies().getPosition().getDepartment().getDepartment());
							put("candidate_firstname", existingCandidacy.getSnapshot().getBasicInfo().getFirstname());
							put("candidate_lastname", existingCandidacy.getSnapshot().getBasicInfo().getLastname());
							Iterator<CandidacyEvaluator> it = existingCandidacy.getProposedEvaluators().iterator();
							if (it.hasNext()) {
								CandidacyEvaluator eval = it.next();
								put("evaluator1_name", eval.getFullname());
								put("evaluator1_email", eval.getEmail());
							} else {
								put("evaluator1_name", "-");
								put("evaluator1_email", "-");
								put("evaluator2_name", "-");
								put("evaluator2_email", "-");
							}
							if (it.hasNext()) {
								CandidacyEvaluator eval = it.next();
								put("evaluator2_name", eval.getFullname());
								put("evaluator2_email", eval.getEmail());
							} else {
								put("evaluator2_name", "-");
								put("evaluator2_email", "-");
							}
						}
					}));
			}
			// END: Send E-Mails

			// Return
			existingCandidacy.initializeCollections();
			return existingCandidacy;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

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

			// Send E-Mails
			// 1. candidacy.remove@institutionManager
			sendEmail(existingCandidacy.getCandidacies().getPosition().getCreatedBy().getContactInfo().getEmail(),
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
			// 2. candidacy.remove@committee
			for (final PositionCommitteeMember member : existingCandidacy.getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
				sendEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
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
			// 3. candidacy.remove@evaluators
			for (final PositionEvaluator evaluator : existingCandidacy.getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
				sendEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
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
			// 4. candidacy.remove@candidates 
			for (final Candidacy candidacy : existingCandidacy.getCandidacies().getCandidacies()) {
				if (candidacy.isPermanent()) {
					sendEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
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

			// Update
			for (FileHeader fh : existingCandidacy.getFiles()) {
				deleteCompletely(fh);
			}
			em.remove(existingCandidacy);
			em.flush();

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
			!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(candidacy.getFiles());
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(candidacy.getCandidacies().getPosition().getDepartment()) &&
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
