package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

@Path("/position")
@Stateless
public class PositionRESTService extends RESTService {

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@Inject
	private Logger log;

	private Position getAndCheckPosition(User loggedOn, long positionId) {
		Position position = em.find(Position.class, positionId);
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		position.initializeCollections();
		return position;
	}

	@GET
	@JsonView({PublicPositionView.class})
	public List<Position> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		User loggedOnUser = getLoggedOn(authToken);

		if (loggedOnUser.hasRole(RoleDiscriminator.INSTITUTION_MANAGER) ||
			loggedOnUser.hasRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {

			List<Institution> institutions = new ArrayList<Institution>();
			institutions.addAll(loggedOnUser.getAssociatedInstitutions());
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"left join fetch p.files f " +
					"left join fetch p.commitee co " +
					"left join fetch p.candidacies ca " +
					"where p.department.institution in (:institutions)")
				.setParameter("institutions", institutions)
				.getResultList();
			return positions;
		} else if (loggedOnUser.hasRole(RoleDiscriminator.MINISTRY_MANAGER) ||
			loggedOnUser.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {

			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"left join fetch p.files f " +
					"left join fetch p.commitee co " +
					"left join fetch p.candidacies ca ")
				.getResultList();
			return positions;
		}
		throw new RestException(Status.UNAUTHORIZED, "insufficient.privileges");
	}

	@GET
	@Path("/search")
	@JsonView({PublicPositionView.class})
	public List<Position> search(@HeaderParam(TOKEN_HEADER) String authToken) {
		// TODO: Search based on QueryParameter (or a Search object)
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"left join fetch p.files f " +
					"left join fetch p.commitee co " +
					"left join fetch p.candidacies ca " +
					"where p.openingDate <= :today and p.closingDate >= :today ")
				.setParameter("today", today)
				.getResultList();
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}
	}

	@GET
	@Path("/public")
	@JsonView({PublicPositionView.class})
	public List<Position> getPublic() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date today = sdf.parse(sdf.format(new Date()));
			@SuppressWarnings("unchecked")
			List<Position> positions = (List<Position>) em.createQuery(
				"from Position p " +
					"left join fetch p.files f " +
					"left join fetch p.commitee co " +
					"left join fetch p.candidacies ca " +
					"where p.openingDate <= :today and p.closingDate >= :today ")
				.setParameter("today", today)
				.getResultList();
			return positions;
		} catch (ParseException e) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "parse.exception");
		}

	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public String get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		Position p = getAndCheckPosition(loggedOn, id);
		String result = null;
		if (loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) ||
			loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) ||
			loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) ||
			loggedOn.isDepartmentUser(p.getDepartment())) {
			result = toJSON(p, DetailedPositionView.class);
		} else {
			result = toJSON(p, PublicPositionView.class);
		}
		return result;
	}

	@POST
	@JsonView({DetailedPositionView.class})
	public Position create(@HeaderParam(TOKEN_HEADER) String authToken, Position position) {
		try {
			Department department = em.find(Department.class, position.getDepartment().getId());
			if (department == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.department.id");
			}
			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(department)) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			position.setPermanent(false);
			position.setLastUpdate(new Date());
			position = em.merge(position);
			em.flush();

			position.initializeCollections();
			return position;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, id);
			existingPosition.copyFrom(position);
			existingPosition.setPermanent(true);
			existingPosition.setLastUpdate(new Date());
			em.flush();
			existingPosition.initializeCollections();
			return existingPosition;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(loggedOn, id);
			em.remove(position);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		position.getFiles().size();
		return position.getFiles();
	}

	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (PositionFile file : position.getFiles()) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (PositionFile file : position.getFiles()) {
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
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
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
		// Check number of file types
		Set<PositionFile> existingFiles = FileHeader.filter(position.getFiles(), type);
		if (!PositionFile.fileTypes.containsKey(type) || existingFiles.size() >= PositionFile.fileTypes.get(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}

		// Create
		try {
			PositionFile positionFile = new PositionFile();
			positionFile.setPosition(position);
			positionFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, positionFile);
			position.addFile(positionFile);
			position.setLastUpdate(new Date());
			em.flush();

			if (type == FileType.APOFASI_ANAPOMPIS) {
				// TODO: CREATE CLONE OF POSITION WITH SAME FIELDS BUT WITHOUT THE APOFASI ANAPOMPIS
			}

			return toJSON(positionFile, SimpleFileHeaderView.class);
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
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
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
		// Extra Validation
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (!PositionFile.fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		PositionFile positionFile = null;
		for (PositionFile file : position.getFiles()) {
			if (file.getId().equals(fileId)) {
				positionFile = file;
				break;
			}
		}
		if (positionFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}

		// Update
		try {
			saveFile(loggedOn, fileItems, positionFile);
			position.setLastUpdate(new Date());
			em.flush();
			positionFile.getBodies().size();
			return toJSON(positionFile, SimpleFileHeaderView.class);
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
	@Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = em.find(Position.class, positionId);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			PositionFile positionFile = null;
			for (PositionFile file : position.getFiles()) {
				if (file.getId().equals(fileId)) {
					positionFile = file;
					break;
				}
			}
			if (positionFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			Response retv = deleteFileBody(positionFile);
			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Remove from Position
				position.getFiles().remove(positionFile);
			}
			position.setLastUpdate(new Date());
			return retv;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/************************
	 * Committee Functions **
	 ************************/

	@GET
	@Path("/{id:[0-9][0-9]*}/professor")
	@JsonView({DetailedRoleView.class})
	public List<Role> getPositionProfessors(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);

		List<Role> professors = em.createQuery(
			"select r from Role r " +
				"where r.id is not null " +
				"and r.discriminator in (:discriminators) ")
			.setParameter("discriminators", discriminatorList)
			.getResultList();

		// Execute
		for (Role r : professors) {
			r.initializeCollections();
		}
		return professors;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<PositionCommitteeMember> getPositionCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (PositionCommitteeMember member : position.getCommitee()) {
			member.getProfessor().initializeCollections();
		}
		return position.getCommitee();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{cmId:[0-9][0-9]*}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		Position position = getAndCheckPosition(loggedOn, positionId);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
			!loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		PositionCommitteeMember result = em.find(PositionCommitteeMember.class, cmId);
		if (result == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.commitee.member.id");
		}

		if (!result.getPosition().getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		result.getProfessor().initializeCollections();

		return result;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember createPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, PositionCommitteeMember newMembership) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, positionId);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(existingPosition.getDepartment())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Professor existingProfessor = em.find(Professor.class, newMembership.getProfessor().getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			// Check if already exists:
			for (PositionCommitteeMember existingMember : existingPosition.getCommitee()) {
				if (existingMember.getProfessor().getId().equals(existingProfessor.getId())) {
					throw new RestException(Status.CONFLICT, "member.already.exists");
				}
			}
			int count = 0;
			for (PositionCommitteeMember existingMember : existingPosition.getCommitee()) {
				if (existingMember.getType() == newMembership.getType()) {
					count++;
				}
			}
			if (count >= PositionCommitteeMember.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}

			// Update
			newMembership.setPosition(existingPosition);
			newMembership.setProfessor(existingProfessor);
			existingPosition.getCommitee().add(newMembership);
			existingPosition.setLastUpdate(new Date());
			newMembership = em.merge(newMembership);
			em.flush();

			// Return result
			newMembership.getProfessor().initializeCollections();
			return newMembership;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9]+}/committee/{cmId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember updatePositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId, PositionCommitteeMember newMembership) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position existingPosition = getAndCheckPosition(loggedOn, positionId);
			// Check if already exists:
			PositionCommitteeMember existingMember = null;
			for (PositionCommitteeMember member : existingPosition.getCommitee()) {
				if (member.getId().equals(cmId)) {
					existingMember = member;
				}
			}
			if (existingMember == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.committee.member.id");
			}
			// Update (only type allowed)
			existingMember.setType(newMembership.getType());
			existingPosition.setLastUpdate(new Date());
			em.flush();

			// Return result
			return existingMember;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee/{cmId:[0-9][0-9]*}")
	public void removePositionCommiteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(loggedOn, positionId);
			PositionCommitteeMember existingCM = null;
			for (PositionCommitteeMember member : position.getCommitee()) {
				if (member.getId().equals(cmId)) {
					existingCM = member;
					break;
				}
			}
			if (existingCM == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.position.committee.member.id");
			}
			// Remove
			existingCM.getPosition().getCommitee().remove(existingCM);
			em.remove(existingCM);
			position.setLastUpdate(new Date());
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

}
