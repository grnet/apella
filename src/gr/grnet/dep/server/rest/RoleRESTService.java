package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.ProfessorFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/role")
@Stateless
public class RoleRESTService extends RESTService {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	private static class RolePair {

		RoleDiscriminator first;

		RoleDiscriminator second;

		public RolePair(RoleDiscriminator first, RoleDiscriminator second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((first == null) ? 0 : first.hashCode());
			result = prime * result + ((second == null) ? 0 : second.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			RolePair other = (RolePair) obj;
			if (!first.equals(other.first)) {
				return false;
			}
			if (!second.equals(other.second)) {
				return false;
			}
			return true;
		}
	}

	private static final Set<RolePair> forbiddenPairs;
	static {
		Set<RolePair> aSet = new HashSet<RolePair>();
		// ADMINISTRATOR
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_FOREIGN));
		// CANDIDATE
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.MINISTRY_MANAGER));
		// INSTITUTION_MANAGER
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.MINISTRY_MANAGER));
		// INSTITUTION_ASSISTANT
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.MINISTRY_MANAGER));
		// MINISTRY_MANAGER
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.INSTITUTION_ASSISTANT));
		// PROFESSOR_DOMESTIC
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.PROFESSOR_FOREIGN));
		// PROFESSOR_FOREIGN
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.PROFESSOR_DOMESTIC));

		forbiddenPairs = Collections.unmodifiableSet(aSet);
	}

	@GET
	@JsonView({DetailedRoleView.class})
	public Collection<Role> getAll(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("user") Long userID, @QueryParam("discriminator") String discriminators,
				@QueryParam("status") String statuses) {
		getLoggedOn(authToken);

		// Prepare Query
		StringBuilder sb = new StringBuilder();
		sb.append("select r from Role r " +
			"where r.status in (:statuses) ");
		if (userID != null) {
			sb.append("and r.user.id = :userID ");
		}
		if (discriminators != null) {
			sb.append("and r.discriminator in (:discriminators) ");
		}
		// Set Parameters
		Query query = em.createQuery(sb.toString());
		Set<RoleStatus> statusesList = new HashSet<RoleStatus>();
		if (statuses != null) {
			try {
				for (String status : statuses.split(",")) {
					if (status.equalsIgnoreCase("ALL")) {
						for (RoleStatus st : RoleStatus.values()) {
							statusesList.add(st);
						}
					}
					else
						statusesList.add(RoleStatus.valueOf(status));
				}
			} catch (IllegalArgumentException e) {
				throw new RestException(Status.BAD_REQUEST, "bad.request");
			}
		}
		else { //Default if no parameter given: ACTIVE only, not all
			statusesList.add(RoleStatus.ACTIVE);
		}
		query.setParameter("statuses", statusesList);
		if (userID != null) {
			query = query.setParameter("userID", userID);
		}
		if (discriminators != null) {
			try {
				List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
				for (String discriminator : discriminators.split(",")) {
					discriminatorList.add(RoleDiscriminator.valueOf(discriminator));
				}
				query = query.setParameter("discriminators", discriminatorList);
			} catch (IllegalArgumentException e) {
				throw new RestException(Status.BAD_REQUEST, "illegal.argument.exception");
			}
		}

		// Execute
		List<Role> roles = query.getResultList();
		for (Role r : roles) {
			r.initializeCollections();
		}
		return roles;
	}

	private boolean isForbiddenPair(RoleDiscriminator first, RoleDiscriminator second) {
		return forbiddenPairs.contains(new RolePair(first, second))
			|| forbiddenPairs.contains(new RolePair(second, first));
	}

	private boolean isIncompatibleRole(Role role, Collection<Role> roles) {
		for (Role r : roles) {
			if (isForbiddenPair(role.getDiscriminator(), r.getDiscriminator()))
				return true;
		}
		return false;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRoleView.class})
	public Role get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		boolean roleBelongsToUser = false;
		for (Role r : loggedOn.getRoles()) {
			if (r.getId() == id) {
				roleBelongsToUser = true;
				break;
			}
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !roleBelongsToUser) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		Role r = (Role) em.createQuery(
			"from Role r where r.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		r.initializeCollections();
		return r;
	}

	@POST
	@JsonView({DetailedRoleView.class})
	public Role create(@HeaderParam(TOKEN_HEADER) String authToken, Role newRole) {
		User loggedOn = getLoggedOn(authToken);

		// Validate
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !newRole.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		User existingUser = em.find(User.class, newRole.getUser().getId());
		if (existingUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (isIncompatibleRole(newRole, existingUser.getRoles())) {
			throw new RestException(Status.CONFLICT, "incompatible.role");
		}

		// Update
		try {
			// Find if the same role already exists and is active.
			// If it does, it should be made inactive.
			Query query = em.createQuery("select r from Role r " +
						"where r.status = :status " +
						"and r.user.id = :userID " +
						"and r.discriminator = :discriminator");
			query.setParameter("status", RoleStatus.ACTIVE);
			query.setParameter("userID", newRole.getUser().getId());
			query.setParameter("discriminator", newRole.getDiscriminator());
			List<Role> existingRoles = (List<Role>) query.getResultList();
			for (Role existingRole : existingRoles) {
				existingRole.setStatus(RoleStatus.INACTIVE);
				existingRole.setStatusEndDate(new Date());
			}
			
			newRole.setStatusDate(new Date());
			newRole.setId(null);
			newRole = em.merge(newRole);
			em.flush();
			newRole.initializeCollections();
			return newRole;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRoleView.class})
	public Role update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Role role) {
		User loggedOn = getLoggedOn(authToken);
		Role existingRole = em.find(Role.class, id);
		// Validate:
		if (existingRole == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.role.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !existingRole.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRole.getStatus().equals(RoleStatus.UNAPPROVED) && !existingRole.compareCriticalFields(role)) {
			throw new RestException(Status.FORBIDDEN, "cannot.change.critical.fields");
		}
		Long managerInstitutionId;
		Long institutionId;
		String identification;
		switch (existingRole.getDiscriminator()) {
			case INSTITUTION_ASSISTANT:
				managerInstitutionId = ((InstitutionAssistant) existingRole).getManager().getInstitution().getId();
				institutionId = ((InstitutionAssistant) role).getInstitution().getId();
				if (!managerInstitutionId.equals(institutionId)) {
					throw new RestException(Status.CONFLICT, "manager.institution.mismatch");
				}
			case CANDIDATE:
				identification = ((Candidate) role).getIdentification();
				try {
					Long found = (Long) em.createQuery("select c.id from Candidate c " +
						"where c.identification = :identification " +
						"and c.id != :id")
						.setParameter("identification", identification)
						.setParameter("id", id)
						.getSingleResult();
					log.log(Level.INFO, found.toString());
					throw new RestException(Status.CONFLICT, "existing.identification.number");
				} catch (NoResultException e) {
				}
				try {
					Long found = (Long) em.createQuery("select p.id from Professor p " +
						"where p.identification = :identification " +
						"and p.id != :id")
						.setParameter("identification", identification)
						.setParameter("id", id)
						.getSingleResult();
					log.log(Level.INFO, found.toString());
					throw new RestException(Status.CONFLICT, "existing.identification.number");
				} catch (NoResultException e) {
				}
				break;
			case PROFESSOR_DOMESTIC:
			case PROFESSOR_FOREIGN:
				identification = ((Professor) role).getIdentification();
				try {
					Long found = (Long) em.createQuery("select c.id from Candidate c " +
						"where c.identification = :identification " +
						"and c.id != :id")
						.setParameter("identification", identification)
						.setParameter("id", id)
						.getSingleResult();
					log.log(Level.INFO, found.toString());
					throw new RestException(Status.CONFLICT, "existing.identification.number");
				} catch (NoResultException e) {
				}
				try {
					Long found = (Long) em.createQuery("select p.id from Professor p " +
						"where p.identification = :identification " +
						"and p.id != :id")
						.setParameter("identification", identification)
						.setParameter("id", id)
						.getSingleResult();
					log.log(Level.INFO, found.toString());
					throw new RestException(Status.CONFLICT, "existing.identification.number");
				} catch (NoResultException e) {
				}
				break;
			default:
				break;
		}

		try {
			// Update
			existingRole = existingRole.copyFrom(role);
			// Return Result
			em.flush();
			existingRole.initializeCollections();
			return existingRole;
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

		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.role.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		User user = em.find(User.class, role.getUser().getId());
		if (user.getRoles().size() < 2) {
			throw new RestException(Status.FORBIDDEN, "one.role.required");
		}
		try {
			// Delete:
			user.removeRole(role);
			em.remove(role);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Response getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_ASSISTANT) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		if (role instanceof Candidate) {
			Candidate candidate = (Candidate) role;
			candidate.initializeCollections();
			GenericEntity<Set<CandidateFile>> entity = new GenericEntity<Set<CandidateFile>>(candidate.getFiles()) {};
			return Response.ok(entity).build();
		} else if (role instanceof Professor) {
			Professor professor = (Professor) role;
			professor.initializeCollections();
			GenericEntity<Set<ProfessorFile>> entity = new GenericEntity<Set<ProfessorFile>>(professor.getFiles()) {};
			return Response.ok(entity).build();
		}
		// Default Action
		throw new RestException(Status.BAD_REQUEST, "wrong.id");
	}

	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_ASSISTANT) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		if (role instanceof Candidate) {
			Candidate candidate = (Candidate) role;
			for (CandidateFile cf : candidate.getFiles()) {
				if (cf.getId().equals(fileId)) {
					return cf;
				}
			}
		} else if (role instanceof Professor) {
			Professor professor = (Professor) role;
			for (ProfessorFile pf : professor.getFiles()) {
				if (pf.getId().equals(fileId)) {
					return pf;
				}
			}
		}
		// Default Action
		throw new RestException(Status.BAD_REQUEST, "wrong.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.role.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_MANAGER) &&
			!loggedOn.hasRole(RoleDiscriminator.INSTITUTION_ASSISTANT) &&
			!loggedOn.hasRole(RoleDiscriminator.MINISTRY_MANAGER) &&
			!role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		if (role instanceof Candidate) {
			Candidate candidate = (Candidate) role;
			for (CandidateFile cf : candidate.getFiles()) {
				if (cf.getId().equals(fileId)) {
					for (FileBody fb : cf.getBodies()) {
						if (fb.getId().equals(bodyId)) {
							return sendFileBody(fb);
						}
					}
				}
			}
		} else if (role instanceof Professor) {
			Professor professor = (Professor) role;
			for (ProfessorFile pf : professor.getFiles()) {
				if (pf.getId().equals(fileId)) {
					for (FileBody fb : pf.getBodies()) {
						if (fb.getId().equals(bodyId)) {
							return sendFileBody(fb);
						}
					}
				}
			}
		}
		// Default Action
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
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
		try {
			// Return Result
			if (role instanceof Candidate) {
				Candidate candidate = (Candidate) role;
				// Check if file can be changed(active role)
				if (!candidate.getStatus().equals(RoleStatus.UNAPPROVED)) {
					switch (type) {
						case TAYTOTHTA:
						case FORMA_SYMMETOXIS:
						case BEBAIWSH_STRATIOTIKIS_THITIAS:
							throw new RestException(Status.FORBIDDEN, "cannot.change.critical.fields");
						default:
					}
				}
				// Check number of file types
				Set<CandidateFile> existingFiles = FileHeader.filter(candidate.getFiles(), type);
				if (!CandidateFile.fileTypes.containsKey(type) || existingFiles.size() >= CandidateFile.fileTypes.get(type)) {
					throw new RestException(Status.CONFLICT, "wrong.file.type");
				}
				//Create File
				CandidateFile candidateFile = new CandidateFile();
				candidateFile.setCandidate(candidate);
				candidateFile.setOwner(candidate.getUser());
				saveFile(loggedOn, fileItems, candidateFile);
				candidate.addFile(candidateFile);
				em.flush();
				return toJSON(candidateFile, SimpleFileHeaderView.class);
			} else if (role instanceof Professor) {
				Professor professor = (Professor) role;
				// Check number of file types
				Set<ProfessorFile> existingFiles = FileHeader.filter(professor.getFiles(), type);
				if (!ProfessorFile.fileTypes.containsKey(type) || existingFiles.size() >= ProfessorFile.fileTypes.get(type)) {
					throw new RestException(Status.CONFLICT, "wrong.file.type");
				}
				// Create File
				ProfessorFile professorFile = new ProfessorFile();
				professorFile.setProfessor(professor);
				professorFile.setOwner(professor.getUser());
				saveFile(loggedOn, fileItems, professorFile);
				professor.addFile(professorFile);
				em.flush();
				return toJSON(professorFile, SimpleFileHeaderView.class);
			} else {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
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
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
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
		try {
			// Return Result
			if (role instanceof Candidate) {
				Candidate candidate = (Candidate) role;
				// Check if it exists
				CandidateFile candidateFile = null;
				for (CandidateFile file : candidate.getFiles()) {
					if (file.getId().equals(fileId)) {
						candidateFile = file;
						break;
					}
				}
				if (candidateFile == null) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
				// Check if file can be changed(active role)
				if (!candidate.getStatus().equals(RoleStatus.UNAPPROVED)) {
					switch (candidateFile.getType()) {
						case TAYTOTHTA:
						case FORMA_SYMMETOXIS:
						case BEBAIWSH_STRATIOTIKIS_THITIAS:
							throw new RestException(Status.FORBIDDEN, "cannot.change.critical.fields");
						default:
					}
				}
				// Update File
				saveFile(loggedOn, fileItems, candidateFile);
				em.flush();
				candidateFile.getBodies().size();
				return toJSON(candidateFile, SimpleFileHeaderView.class);
			} else if (role instanceof Professor) {
				Professor professor = (Professor) role;
				ProfessorFile professorFile = null;
				for (ProfessorFile file : professor.getFiles()) {
					if (file.getId().equals(fileId)) {
						professorFile = file;
						break;
					}
				}
				if (professorFile == null) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
				saveFile(loggedOn, fileItems, professorFile);
				em.flush();
				professorFile.getBodies().size();
				return toJSON(professorFile, SimpleFileHeaderView.class);
			} else {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
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
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			if (role instanceof Candidate) {
				Candidate candidate = (Candidate) role;
				try {
					CandidateFile candidateFile = (CandidateFile) em.createQuery(
						"select cf from CandidateFile cf " +
							"where cf.candidate.id = :candidateId " +
							"and cf.id = :fileId")
						.setParameter("candidateId", id)
						.setParameter("fileId", fileId)
						.getSingleResult();
					//Validate delete
					if (!candidate.getStatus().equals(RoleStatus.UNAPPROVED)) {
						switch (candidateFile.getType()) {
							case TAYTOTHTA:
							case FORMA_SYMMETOXIS:
							case BEBAIWSH_STRATIOTIKIS_THITIAS:
								throw new RestException(Status.FORBIDDEN, "cannot.change.critical.fields");
							default:
						}
					}
					// Do delete
					Response retv = deleteFileBody(candidateFile);
					if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
						// Remove from Role
						em.remove(candidateFile);
					}
					return retv;
				} catch (NoResultException e) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
			} else if (role instanceof Professor) {
				try {
					ProfessorFile professorFile = (ProfessorFile) em.createQuery(
						"select cf from ProfessorFile cf " +
							"where cf.professor.id = :professorId " +
							"and cf.id = :fileId")
						.setParameter("professorId", id)
						.setParameter("fileId", fileId)
						.getSingleResult();
					Response retv = deleteFileBody(professorFile);
					if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
						// Remove from Role
						em.remove(professorFile);
					}
					return retv;
				} catch (NoResultException e) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
			}
			// Default Action
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/************************************
	 *** Administrator Only Functions ***
	 ************************************/

	@PUT
	@Path("/{id:[0-9][0-9]*}/status")
	@JsonView({DetailedRoleView.class})
	public Role updateStatus(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Role requestRole) {
		User loggedOn = getLoggedOn(authToken);
		Role existingRole = em.find(Role.class, id);
		// Validate
		if (existingRole == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.role.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		//Validate Change
		if (existingRole instanceof InstitutionManager) {
			if (requestRole.getStatus().equals(RoleStatus.ACTIVE)) {
				try {
					InstitutionManager im = (InstitutionManager) existingRole;
					// Check if exists active IM for same Institution:
					em.createQuery("select im from InstitutionManager im " +
						"where im.status = :status " +
						"and im.institution.id = :instituionId")
						.setParameter("status", RoleStatus.ACTIVE)
						.setParameter("institutionId", im.getInstitution().getId())
						.setMaxResults(1)
						.getSingleResult();
					throw new RestException(Status.CONFLICT, "exists.active.institution.manager");
				} catch (NoResultException e) {
				}
			}
		}

		// Update
		try {
			// Update Status
			existingRole.setStatus(requestRole.getStatus());
			if (requestRole.equals(RoleStatus.INACTIVE))
				existingRole.setStatusEndDate(new Date());
			existingRole.initializeCollections();
			return existingRole;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
