package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.hibernate.Session;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/position/{id:[0-9][0-9]*}/candidacies")
@Stateless
public class PositionCandidaciesRESTService extends RESTService {

	@Inject
	private Logger log;

	/*********************
	 * Candidacies *********
	 *********************/

	/**
	 * Returns the list of Candidacies for this position
	 *
	 * @param authToken
	 * @param positionId
	 * @return
	 * @HTTP 403 X-Error-Code insufficient.privileges
	 * @HTTP 404 X-Error-Code wrong.position.id
	 * @HTTP 409 X-Error-Code wrong.position.id
	 */
	@GET
	@JsonView({CandidacyView.class})
	public Set<Candidacy> getPositionCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (position.getPhase().getCandidacies() == null) {
			throw new RestException(Status.CONFLICT, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(position.getDepartment()) &&
				!(position.getPhase().getCommittee() != null && position.getPhase().getCommittee().containsMember(loggedOn)) &&
				!position.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		Set<Candidacy> result = new HashSet<Candidacy>();
		for (Candidacy candidacy : position.getPhase().getCandidacies().getCandidacies()) {
			if (candidacy.isPermanent()) {
				result.add(candidacy);
			}
		}
		return result;
	}

	/**********************************
	 * Position Candidacies ***********
	 **********************************/

	/**
	 * Returns the specific PositionCandidacies info of given Position
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @returnWrapper gr.grnet.dep.service.model.PositionCandidacies
	 * @HTTP 403 X-Error-Code insufficient.privileges
	 * @HTTP 404 X-Error-Code wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code wrong.position.id
	 */
	@GET
	@Path("/{candidaciesId:[0-9]+}")
	@JsonView({DetailedPositionCandidaciesView.class})
	public PositionCandidacies get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
		User loggedOn = getLoggedOn(authToken);

		try {
			Session session = em.unwrap(Session.class);
			session.enableFilter("filterPermanent");

			PositionCandidacies existingCandidacies = em.createQuery(
					"select pc from PositionCandidacies pc " +
							"left join fetch pc.candidacies c " +
							"left join fetch c.proposedEvaluators pe " +
							"where pc.id = :candidaciesId", PositionCandidacies.class)
					.setParameter("candidaciesId", candidaciesId)
					.getSingleResult();

			Position existingPosition = existingCandidacies.getPosition();
			if (!existingPosition.getId().equals(positionId)) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.id");
			}
			if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
					!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
					!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
					!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
					!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
					!existingCandidacies.containsCandidate(loggedOn)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			for (Candidacy candidacy : existingCandidacies.getCandidacies()) {
				if (!candidacy.isOpenToOtherCandidates() &&
						!candidacy.getCandidate().getUser().getId().equals(loggedOn.getId()) &&
						existingCandidacies.containsCandidate(loggedOn)) {
					candidacy.setAllowedToSee(Boolean.FALSE);
				} else {
					// all other cases: owner, admin, mm, ma, im, ia, committee, evaluator
					candidacy.setAllowedToSee(Boolean.TRUE);
				}
			}
			return existingCandidacies;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");

		}

	}

	/**********************
	 * File Functions *****
	 **********************/

	/**
	 * Returns the list of file descriptions associated with position
	 * candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{candidaciesId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCandidaciesFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionCandidaciesFile> result = FileHeader.filterDeleted(existingCandidacies.getFiles());
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
			// Filter Files that do not belong to loggedOnUser
			Iterator<PositionCandidaciesFile> it = result.iterator();
			while (it.hasNext()) {
				PositionCandidaciesFile file = it.next();
				if (!file.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
					it.remove();
				}
			}
		}
		return result;
	}

	/**
	 * Return the file description of the file with given id and associated with
	 * given position candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCandidaciesFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Find File
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		PositionCandidaciesFile existingFile = null;
		for (PositionCandidaciesFile file : files) {
			if (file.getId().equals(fileId)) {
				existingFile = file;
			}
		}
		if (existingFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingFile.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return existingFile;
	}

	/**
	 * Return the file data of the file with given id and associated with
	 * given position candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Find File
		Collection<PositionCandidaciesFile> files = FileHeader.filterDeleted(existingCandidacies.getFiles());
		PositionCandidaciesFile existingFile = null;
		for (PositionCandidaciesFile file : files) {
			if (file.getId().equals(fileId)) {
				existingFile = file;
			}
		}
		if (existingFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingFile.getEvaluator().getCandidacy().getCandidate().getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (FileBody fb : existingFile.getBodies()) {
			if (fb.getId().equals(bodyId)) {
				return sendFileBody(fb);
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Handles upload of a new file associated with given position candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 400 X-Error-Code: missing.candidacy.evaluator
	 * @HTTP 400 X-Error-Code: wrong.candidacy.evaluator.id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{candidaciesId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.CONFLICT, "wrong.position.candidacies.phase");
		}
		// Validate:
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type and candidacy evaluator:
		FileType type = null;
		Long candidacyEvaluatorId = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
			} else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
				candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (candidacyEvaluatorId == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.candidacy.evaluator");
		}
		final CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.BAD_REQUEST, "wrong.candidacy.evaluator.id");
		}
		try {
			// Check number of file types
			Set<PositionCandidaciesFile> pcFiles = FileHeader.filterIncludingDeleted(existingCandidacies.getFiles(), type);
			PositionCandidaciesFile existingFile = checkNumberOfFileTypes(PositionCandidaciesFile.fileTypes, type, pcFiles);
			if (existingFile != null) {
				return _updateFile(loggedOn, fileItems, existingFile);
			}
			// Create
			final PositionCandidaciesFile pcFile = new PositionCandidaciesFile();
			pcFile.setCandidacies(existingCandidacies);
			pcFile.setEvaluator(existingEvaluator);
			pcFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, pcFile);
			existingCandidacies.addFile(pcFile);
			em.flush();

			// Send E-Mails
			if (pcFile.getType().equals(FileType.EISIGISI_DEP_YPOPSIFIOU)) {

				// positionCandidacies.upload.eisigisi@candidate
				mailService.postEmail(existingEvaluator.getCandidacy().getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"positionCandidacies.upload.eisigisi@candidate",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
								put("lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
								put("firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
								put("lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
								put("evaluator_firstname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
								put("evaluator_lastname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
								put("evaluator_firstname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
								put("evaluator_lastname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
							}
						}));
				// positionCandidacies.upload.eisigisi@candidacyEvaluator
				mailService.postEmail(existingEvaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"positionCandidacies.upload.eisigisi@candidacyEvaluator",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
								put("lastname_el", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
								put("firstname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
								put("lastname_en", existingEvaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
								put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
								put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
								put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
								put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
								put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
								put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));
								put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));
							}
						}));

				// positionCandidacies.upload.eisigisi@committee
				for (final PositionCommitteeMember member : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCandidacies.upload.eisigisi@committee",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
									put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
									put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));
									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
									put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
									put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// positionCandidacies.upload.eisigisi@evaluators
				for (final PositionEvaluator evaluator : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCandidacies.upload.eisigisi@evaluators",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("candidate_firstname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("el"));
									put("candidate_lastname_el", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("el"));
									put("institution_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("candidate_firstname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getFirstname("en"));
									put("candidate_lastname_en", existingEvaluator.getCandidacy().getCandidate().getUser().getLastname("en"));
									put("institution_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			} else {
				// position.upload@committee
				for (final PositionCommitteeMember member : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.upload@committee",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("position", pcFile.getCandidacies().getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// position.upload@candidates
				for (final Candidacy candidacy : pcFile.getCandidacies().getCandidacies()) {
					mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.upload@candidates",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("position", pcFile.getCandidacies().getPosition().getName());

									put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
									put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
									put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
									put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
									put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// position.upload@evaluators
				for (final PositionEvaluator evaluator : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getEvaluation().getEvaluators()) {
					mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"position.upload@evaluators",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("position", pcFile.getCandidacies().getPosition().getName());

									put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", pcFile.getCandidacies().getPosition().getDepartment().getName().get("el"));

									put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", pcFile.getCandidacies().getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", pcFile.getCandidacies().getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			}

			// End: Send E-Mails

			return toJSON(pcFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Handles upload that updates an existing file associated with position
	 * candidacies
	 *
	 * @param authToken
	 * @param positionId
	 * @param candidaciesId
	 * @param fileId
	 * @param request
	 * @throws FileUploadException
	 * @throws IOException
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 400 X-Error-Code: missing.candidacy.evaluator
	 * @HTTP 400 X-Error-Code: wrong.candidacy.evaluator.id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
	 * @HTTP 409 X-Error-Code: wrong.file.type
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@POST
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.candidacies.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type and candidacy evaluator:
		FileType type = null;
		Long candidacyEvaluatorId = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
			} else if (fileItem.isFormField() && fileItem.getFieldName().equals("candidacyEvaluatorId")) {
				candidacyEvaluatorId = Long.valueOf(fileItem.getString("UTF-8"));
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (candidacyEvaluatorId == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.candidacy.evaluator");
		}
		CandidacyEvaluator existingEvaluator = findCandidacyEvaluator(existingCandidacies, candidacyEvaluatorId);
		if (existingEvaluator == null) {
			throw new RestException(Status.BAD_REQUEST, "wrong.candidacy.evaluator.id");
		}
		try {
			if (!PositionCandidaciesFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionCandidaciesFile candidaciesFile = null;
			for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
				if (file.getId().equals(fileId)) {
					candidaciesFile = file;
					break;
				}
			}
			if (candidaciesFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			candidaciesFile.setEvaluator(existingEvaluator);
			return _updateFile(loggedOn, fileItems, candidaciesFile);
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
	 * @param candidaciesId
	 * @param positionId
	 * @param fileId
	 * @return
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.candidacies.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.candidacies.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{candidaciesId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("candidaciesId") Long candidaciesId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCandidacies().getId().equals(candidaciesId)) {
			throw new RestException(Status.CONFLICT, "wrong.position.candidacies.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionCandidaciesFile candidaciesFile = null;
			for (PositionCandidaciesFile file : existingCandidacies.getFiles()) {
				if (file.getId().equals(fileId)) {
					candidaciesFile = file;
					break;
				}
			}
			if (candidaciesFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionCandidaciesFile cf = deleteAsMuchAsPossible(candidaciesFile);
			if (cf == null) {
				existingCandidacies.getFiles().remove(candidaciesFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	private static CandidacyEvaluator findCandidacyEvaluator(PositionCandidacies candidacies, Long candidacyEvaluatorId) {
		for (Candidacy candidacy : candidacies.getCandidacies()) {
			for (CandidacyEvaluator evaluator : candidacy.getProposedEvaluators()) {
				if (evaluator.getId().equals(candidacyEvaluatorId)) {
					return evaluator;
				}
			}
		}
		return null;
	}
}
