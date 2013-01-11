package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
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
import gr.grnet.dep.service.model.file.PositionEvaluationFile;

import java.io.IOException;
import java.util.Collection;
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

@Path("/position/{id:[0-9][0-9]*}/evaluation")
@Stateless
public class PositionEvaluationRESTService extends RESTService {

	@Inject
	private Logger log;

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
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (PositionEvaluator evaluator : existingEvaluation.getEvaluators()) {
			evaluator.getRegisterMember().getProfessor().initializeCollections();
		}
		return existingEvaluation;
	}

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
		existingEvaluator.getRegisterMember().getProfessor().initializeCollections();
		return existingEvaluator;
	}

	@PUT
	@Path("/{evaluationId:[0-9]+}")
	@JsonView({DetailedPositionEvaluationView.class})
	public PositionEvaluation updatePositionEvaluation(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, PositionEvaluation newEvaluation) {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
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
		if (newEvaluation.getEvaluators().size() > PositionEvaluator.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "max.evaluators.exceeded");
		}

		// Retrieve new Members from Institution's Register
		Set<Long> newEvaluatorsRegisterIds = new HashSet<Long>();
		for (PositionEvaluator newEvaluator : newEvaluation.getEvaluators()) {
			newEvaluatorsRegisterIds.add(newEvaluator.getRegisterMember().getId());
		}
		List<RegisterMember> newRegisterMembers = em.createQuery(
			"select distinct rm from Register r " +
				"join r.members rm " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and rm.deleted = false " +
				"and rm.external = true " +
				"and rm.professor.status = :status " +
				"and rm.id in (:registerIds)")
			.setParameter("institutionId", existingPosition.getDepartment().getInstitution().getId())
			.setParameter("status", RoleStatus.ACTIVE)
			.setParameter("registerIds", newEvaluatorsRegisterIds)
			.getResultList();
		if (newEvaluatorsRegisterIds.size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}
		// Extra Validation
		// Check if a member is in Commitee
		for (PositionCommitteeMember committeeMember : existingPosition.getPhase().getCommittee().getMembers()) {
			if (newEvaluatorsRegisterIds.contains(committeeMember.getRegisterMember().getId())) {
				throw new RestException(Status.CONFLICT, "member.in.committe");
			}
		}
		try {
			// Update
			existingEvaluation.copyFrom(newEvaluation);
			existingEvaluation.getEvaluators().clear();
			for (RegisterMember newRegisterMember : newRegisterMembers) {
				PositionEvaluator newEvaluator = new PositionEvaluator();
				newEvaluator.setRegisterMember(newRegisterMember);
				existingPosition.getPhase().getEvaluation().addEvaluator(newEvaluator);
			}
			em.flush();
			// Return result
			return existingEvaluation;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	@GET
	@Path("/{evaluationId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionEvaluationFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
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
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluationFile> files = FileHeader.filterDeleted(existingEvaluation.getFiles());
		return files;
	}

	@GET
	@Path("/{evaluationId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("fileId") Long fileId) {
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
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluationFile> files = FileHeader.filterDeleted(existingEvaluation.getFiles());
		for (PositionEvaluationFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{evaluationId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
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
			!(existingPosition.getPhase().getCommittee() != null && existingPosition.getPhase().getCommittee().containsMember(loggedOn)) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Set<PositionEvaluationFile> files = FileHeader.filterDeleted(existingEvaluation.getFiles());
		for (PositionEvaluationFile file : files) {
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
	@Path("/{evaluationId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
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
			// Check number of file types
			Set<PositionEvaluationFile> eFiles = FileHeader.filterIncludingDeleted(existingEvaluation.getFiles(), type);
			PositionEvaluationFile existingFile2 = checkNumberOfFileTypes(PositionEvaluationFile.fileTypes, type, eFiles);
			if (existingFile2 != null) {
				return _updateFile(loggedOn, fileItems, existingFile2);
			}

			// Create
			PositionEvaluationFile eFile = new PositionEvaluationFile();
			eFile.setEvaluation(existingEvaluation);
			eFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, eFile);
			existingEvaluation.addFile(eFile);
			em.flush();

			return toJSON(eFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{evaluationId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
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
			if (!PositionEvaluationFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionEvaluationFile evaluationFile = null;
			for (PositionEvaluationFile file : existingEvaluation.getFiles()) {
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
	 * Deletes the last body of given file, if possible.
	 * 
	 * @param authToken
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{evaluationId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId, @PathParam("fileId") Long fileId) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionEvaluation existingEvaluation = em.find(PositionEvaluation.class, evaluationId);
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
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionEvaluationFile evaluationFile = null;
			for (PositionEvaluationFile file : existingEvaluation.getFiles()) {
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
				existingEvaluation.getFiles().remove(evaluationFile);
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
	@GET
	@Path("/{evaluationId:[0-9]+}/register")
	@JsonView({DetailedRegisterMemberView.class})
	public List<RegisterMember> getPositionEvaluationRegisterMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("evaluationId") Long evaluationId) {
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
		List<RegisterMember> registerMembers = em.createQuery(
			"select distinct m from Register r " +
				"join r.members m " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and m.professor.status = :status " +
				"and m.external = true " +
				"and m.deleted = false")
			.setParameter("institutionId", existingPosition.getDepartment().getInstitution().getId())
			.setParameter("status", RoleStatus.ACTIVE)
			.getResultList();

		// Execute
		for (RegisterMember r : registerMembers) {
			Professor p = (Professor) r.getProfessor();
			p.setCommitteesCount(Professor.countCommittees(p));
			r.getProfessor().initializeCollections();
		}
		return registerMembers;
	}
}
