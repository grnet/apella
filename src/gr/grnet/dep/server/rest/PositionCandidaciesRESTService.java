package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCandidacies;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.PositionCandidacies.PositionCandidaciesView;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.hibernate.Session;

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
	public Set<Candidacy> getPositionCandidacies(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
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
	public String get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
		User loggedOn = getLoggedOn(authToken);

		Session session = em.unwrap(Session.class);
		session.enableFilter("filterPermanent");

		PositionCandidacies existingCandidacies = em.find(PositionCandidacies.class, candidaciesId);
		if (existingCandidacies == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.candidacies.id");
		}
		Position existingPosition = existingCandidacies.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
			!existingCandidacies.containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		existingCandidacies.initializeCollections();
		for (Candidacy candidacy : existingCandidacies.getCandidacies()) {
			if (!candidacy.isOpenToOtherCandidates() && !candidacy.getCandidate().getUser().getId().equals(loggedOn.getId()) && existingCandidacies.containsCandidate(loggedOn)) {
				candidacy.setAllowedToSee(Boolean.FALSE);
			} else {
				// all other cases: owner, admin, mm, ma, im, ia, committee, evaluator
				candidacy.setAllowedToSee(Boolean.TRUE);
			}
		}
		String result = null;
		if (loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) ||
			loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) ||
			loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
			loggedOn.isDepartmentUser(existingPosition.getDepartment()) ||
			(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) ||
			(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn))) {
			result = toJSON(existingCandidacies, DetailedPositionCandidaciesView.class);
		} else {
			//Is a Candidate, Evaluators are filtered
			result = toJSON(existingCandidacies, PositionCandidaciesView.class);
		}

		return result;
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
	public Collection<PositionCandidaciesFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	public PositionCandidaciesFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
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
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
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
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * @throws FileUploadException
	 * @throws IOException
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
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @Context HttpServletRequest request) throws FileUploadException, IOException {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
							put("username", existingEvaluator.getCandidacy().getCandidate().getUser().getUsername());
							put("evaluator_firstname", existingEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
							put("evaluator_lastname", existingEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
						}
					}));
				// positionCandidacies.upload.eisigisi@candidacyEvaluator
				mailService.postEmail(existingEvaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
					"default.subject",
					"positionCandidacies.upload.eisigisi@candidacyEvaluator",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("evaluator_firstname", existingEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getFirstname());
							put("evaluator_lastname", existingEvaluator.getRegisterMember().getProfessor().getUser().getBasicInfo().getLastname());
							put("candidate_firstname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getFirstname());
							put("candidate_lastname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getLastname());
							put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
							put("institution", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getInstitution().getName());
							put("department", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getDepartment());
						}
					}));

				// positionCandidacies.upload.eisigisi@committee
				for (final PositionCommitteeMember member : existingEvaluator.getCandidacy().getCandidacies().getPosition().getPhase().getCommittee().getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"positionCandidacies.upload.eisigisi@committee",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("username", member.getRegisterMember().getProfessor().getUser().getUsername());
								put("candidate_firstname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getFirstname());
								put("candidate_lastname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
								put("institution", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getDepartment());
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
								put("username", evaluator.getRegisterMember().getProfessor().getUser().getUsername());
								put("candidate_firstname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getFirstname());
								put("candidate_lastname", existingEvaluator.getCandidacy().getCandidate().getUser().getBasicInfo().getLastname());
								put("position", existingEvaluator.getCandidacy().getCandidacies().getPosition().getName());
								put("institution", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", existingEvaluator.getCandidacy().getCandidacies().getPosition().getDepartment().getDepartment());
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
								put("username", member.getRegisterMember().getProfessor().getUser().getUsername());
								put("position", pcFile.getCandidacies().getPosition().getName());
								put("institution", pcFile.getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", pcFile.getCandidacies().getPosition().getDepartment().getDepartment());
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
								put("username", candidacy.getCandidate().getUser().getUsername());
								put("position", pcFile.getCandidacies().getPosition().getName());
								put("institution", pcFile.getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", pcFile.getCandidacies().getPosition().getDepartment().getDepartment());
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
								put("username", evaluator.getRegisterMember().getProfessor().getUser().getUsername());
								put("position", pcFile.getCandidacies().getPosition().getName());
								put("institution", pcFile.getCandidacies().getPosition().getDepartment().getInstitution().getName());
								put("department", pcFile.getCandidacies().getPosition().getDepartment().getDepartment());
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
	 * @returnWrapper gr.grnet.dep.service.model.file.PositionCandidaciesFile
	 * @throws FileUploadException
	 * @throws IOException
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
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("candidaciesId") Long candidaciesId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("candidaciesId") Long candidaciesId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
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
