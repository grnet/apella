package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommittee;
import gr.grnet.dep.service.model.PositionCommittee.DetailedPositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionCommitteeMember.MemberType;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

@Path("/position/{id:[0-9][0-9]*}/committee")
@Stateless
public class PositionCommitteeRESTService extends RESTService {

	@Inject
	private Logger log;

	/*********************
	 * Register Members **
	 *********************/

	@GET
	@Path("/{committeeId:[0-9]+}/register")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Collection<RegisterMember> getPositionCommiteeRegisterMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)
			&& !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RegisterMember> registerMembers = em.createQuery(
			"select distinct m from Register r " +
				"join r.members m " +
				"where r.permanent = true " +
				"and r.institution.id = :institutionId " +
				"and m.professor.status = :status " +
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

	/*********************
	 * Committee *********
	 *********************/

	@GET
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!existingCommittee.containsMember(loggedOn) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		existingCommittee.initializeCollections();
		return existingCommittee;
	}

	@GET
	@Path("/{committeeId:[0-9]+}/member/{cmId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (PositionCommitteeMember existingMember : existingCommittee.getMembers()) {
			if (existingMember.getId().equals(cmId)) {
				return existingMember;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.position.commitee.member.id");
	}

	@PUT
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, PositionCommittee newCommittee) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.committee.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI) || existingPosition.getPhase().getCandidacies() == null) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Retrieve new Members from Institution's Register
		Map<Long, PositionCommitteeMember> existingCommitteeMemberAsMap = new HashMap<Long, PositionCommitteeMember>();
		for (PositionCommitteeMember existingCommitteeMember : newCommittee.getMembers()) {
			existingCommitteeMemberAsMap.put(existingCommitteeMember.getRegisterMember().getId(), existingCommitteeMember);
		}

		// Retrieve new Members from Institution's Register
		Map<Long, MemberType> newCommitteeMemberAsMap = new HashMap<Long, MemberType>();
		for (PositionCommitteeMember newCommitteeMember : newCommittee.getMembers()) {
			newCommitteeMemberAsMap.put(newCommitteeMember.getRegisterMember().getId(), newCommitteeMember.getType());
		}
		List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();
		if (!newCommitteeMemberAsMap.isEmpty()) {
			Query query = em.createQuery(
				"select distinct m from Register r " +
					"join r.members m " +
					"where r.permanent = true " +
					"and r.institution.id = :institutionId " +
					"and m.professor.status = :status " +
					"and m.deleted = false " +
					"and m.id in (:registerIds)")
				.setParameter("institutionId", existingPosition.getDepartment().getInstitution().getId())
				.setParameter("status", RoleStatus.ACTIVE)
				.setParameter("registerIds", newCommitteeMemberAsMap.keySet());
			newRegisterMembers.addAll(query.getResultList());
		}
		if (newCommittee.getMembers().size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}
		// Validate New Committee
		// Check if a member is Evaluator
		for (PositionEvaluator evaluator : existingPosition.getPhase().getEvaluation().getEvaluators()) {
			if (newCommitteeMemberAsMap.containsKey(evaluator.getRegisterMember().getId())) {
				throw new RestException(Status.CONFLICT, "member.is.evaluator");
			}
		}
		// Check committee structure
		int countRegular = 0;
		int countSubstitute = 0;
		int countInternalRegular = 0;
		int countInternalSubstitute = 0;
		int countExternalRegular = 0;
		int countExternalSubstitute = 0;
		for (RegisterMember newRegisterMember : newRegisterMembers) {
			MemberType type = newCommitteeMemberAsMap.get(newRegisterMember.getId());
			switch (type) {
			case REGULAR:
				countRegular++;
				if (newRegisterMember.isExternal()) {
					countExternalRegular++;
				} else {
					countInternalRegular++;
				}
				break;
			case SUBSTITUTE:
				countSubstitute++;
				if (newRegisterMember.isExternal()) {
					countExternalSubstitute++;
				} else {
					countInternalSubstitute++;
				}
				break;
			}
		}
		if (countRegular != PositionCommitteeMember.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "max.regular.members.failed");
		}
		if (countSubstitute != PositionCommitteeMember.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "max.substitute.members.failed");
		}
		if (countInternalRegular < PositionCommitteeMember.MIN_INTERNAL) {
			throw new RestException(Status.CONFLICT, "min.internal.regular.members.failed");
		}
		if (countInternalSubstitute < PositionCommitteeMember.MIN_INTERNAL) {
			throw new RestException(Status.CONFLICT, "min.internal.substitute.members.failed");
		}
		if (countExternalRegular < PositionCommitteeMember.MIN_EXTERNAL) {
			throw new RestException(Status.CONFLICT, "min.external.regular.members.failed");
		}
		if (countExternalSubstitute < PositionCommitteeMember.MIN_EXTERNAL) {
			throw new RestException(Status.CONFLICT, "min.external.substitute.members.failed");
		}

		// Update
		try {
			existingCommittee.copyFrom(newCommittee);
			existingCommittee.getMembers().clear();
			for (RegisterMember newRegisterMember : newRegisterMembers) {
				PositionCommitteeMember newCommitteeMember = null;
				if (existingCommitteeMemberAsMap.containsKey(newRegisterMember.getId())) {
					newCommitteeMember = existingCommitteeMemberAsMap.get(newRegisterMember.getId());
				} else {
					newCommitteeMember = new PositionCommitteeMember();
				}
				newCommitteeMember.setCommittee(existingCommittee);
				newCommitteeMember.setRegisterMember(newRegisterMember);
				newCommitteeMember.setType(newCommitteeMemberAsMap.get(newRegisterMember.getId()));

				existingCommittee.addMember(newCommitteeMember);
			}
			existingCommittee = em.merge(existingCommittee);
			em.flush();

			// TODO: Send E-Mails
			// positionCommittee.create.regular.member@member
			// positionCommittee.create.substitute.member@member
			// positionCommittee.update.members@candidates
			// positionCommittee.update.committeeMeetingDate@members
			// positionCommittee.update.committeeMeetingDate@candidates

			// Return result
			existingCommittee.initializeCollections();
			return existingCommittee;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 **********************/

	@GET
	@Path("/{committeeId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCommitteeFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!existingCommittee.containsMember(loggedOn) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(existingCommittee.getFiles());
	}

	@GET
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCommitteeFile getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!existingCommittee.containsMember(loggedOn) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
		for (PositionCommitteeFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment()) &&
			!existingCommittee.containsMember(loggedOn) &&
			!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
		for (FileHeader file : files) {
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
	@Path("/{committeeId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.committee.phase");
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
			Set<PositionCommitteeFile> pcFiles = FileHeader.filterIncludingDeleted(existingCommittee.getFiles(), type);
			PositionCommitteeFile existingFile = checkNumberOfFileTypes(PositionCommitteeFile.fileTypes, type, pcFiles);
			if (existingFile != null) {
				return _updateFile(loggedOn, fileItems, existingFile);
			}
			// Create
			PositionCommitteeFile pcFile = new PositionCommitteeFile();
			pcFile.setCommittee(existingCommittee);
			pcFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, pcFile);
			existingCommittee.addFile(pcFile);
			em.flush();

			// TODO: Send E-Mails
			// position.upload@committee
			// position.upload@candidates
			// position.upload@evaluators

			return toJSON(pcFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.committee.phase");
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
		// Find required type:
		FileType type = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
				break;
			}
		}
		// Extra Validation
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		try {
			if (!PositionCommitteeFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionCommitteeFile committeeFile = null;
			for (PositionCommitteeFile file : existingCommittee.getFiles()) {
				if (file.getId().equals(fileId)) {
					committeeFile = file;
					break;
				}
			}
			if (committeeFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			return _updateFile(loggedOn, fileItems, committeeFile);
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
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("committeeId") Long committeeId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.committee.phase");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionCommitteeFile committeeFile = null;
			for (PositionCommitteeFile file : existingCommittee.getFiles()) {
				if (file.getId().equals(fileId)) {
					committeeFile = file;
					break;
				}
			}
			if (committeeFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionCommitteeFile cf = deleteAsMuchAsPossible(committeeFile);
			if (cf == null) {
				existingCommittee.getFiles().remove(committeeFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
