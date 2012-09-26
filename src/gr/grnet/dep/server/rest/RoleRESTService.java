package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.DepartmentAssistant;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidateFile;
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
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_FOREIGN));
		// CANDIDATE
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.CANDIDATE, RoleDiscriminator.MINISTRY_MANAGER));
		// INSTITUTION_MANAGER
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_MANAGER, RoleDiscriminator.MINISTRY_MANAGER));
		// INSTITUTION_ASSISTANT
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.INSTITUTION_ASSISTANT, RoleDiscriminator.MINISTRY_MANAGER));
		// DEPARTMENT_ASSISTANT
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.DEPARTMENT_ASSISTANT, RoleDiscriminator.MINISTRY_MANAGER));
		// MINISTRY_MANAGER
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.CANDIDATE));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.PROFESSOR_DOMESTIC));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.PROFESSOR_FOREIGN));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.MINISTRY_MANAGER, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		// PROFESSOR_DOMESTIC
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_DOMESTIC, RoleDiscriminator.PROFESSOR_FOREIGN));
		// PROFESSOR_FOREIGN
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.DEPARTMENT_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.INSTITUTION_ASSISTANT));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.INSTITUTION_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.MINISTRY_MANAGER));
		aSet.add(new RolePair(RoleDiscriminator.PROFESSOR_FOREIGN, RoleDiscriminator.PROFESSOR_DOMESTIC));

		forbiddenPairs = Collections.unmodifiableSet(aSet);
	}

	@GET
	@JsonView({DetailedRoleView.class})
	public Collection<Role> getAll(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("user") Long userID, @QueryParam("discriminator") String discriminators) {
		getLoggedOn(authToken);

		// Prepare Query
		StringBuilder sb = new StringBuilder();
		sb.append("select r from Role r " +
			"where r.id is not null ");
		if (userID != null) {
			sb.append("and r.user.id = :userID ");
		}
		if (discriminators != null) {
			sb.append("and r.discriminator in (:discriminators) ");
		}
		// Set Parameters
		Query query = em.createQuery(sb.toString());
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
				throw new RestException(Status.BAD_REQUEST, "bad.request");
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

	private void refreshRoleStatus(Role role) {
		switch (role.getStatus()) {
			case CREATED:
				if (!role.isMissingRequiredFields()) {
					User roleUser = em.find(User.class, role.getUser().getId());
					switch (roleUser.getRegistrationType()) {
						case REGISTRATION_FORM:
							// InstitutionAssistant, DepartmentAssistant do not need Helpdesk Approval, set to ACTIVE
							switch (role.getDiscriminator()) {
								case INSTITUTION_ASSISTANT:
								case DEPARTMENT_ASSISTANT:
									role.setStatus(RoleStatus.ACTIVE);
									role.setStatusDate(new Date());
									break;
								default:
									role.setStatus(RoleStatus.UNAPPROVED);
									role.setStatusDate(new Date());
									break;
							}

							break;
						case SHIBBOLETH:
							// Shibboleth Users do not need Helpdesk Approval, set to ACTIVE
							role.setStatus(RoleStatus.ACTIVE);
							role.setStatusDate(new Date());
							break;
					}
				}
				break;
			case UNAPPROVED:
				if (role.isMissingRequiredFields()) {
					role.setStatus(RoleStatus.CREATED);
					role.setStatusDate(new Date());
				} else {
					User roleUser = em.find(User.class, role.getUser().getId());
					switch (roleUser.getRegistrationType()) {
						case REGISTRATION_FORM:
							// InstitutionAssistant, DepartmentAssistant do not need Helpdesk Approval, set to ACTIVE
							switch (role.getDiscriminator()) {
								case INSTITUTION_ASSISTANT:
								case DEPARTMENT_ASSISTANT:
									role.setStatus(RoleStatus.ACTIVE);
									role.setStatusDate(new Date());
									break;
								default:
									// Do not change in other cases
									break;
							}

							break;
						case SHIBBOLETH:
							// Shibboleth Users do not need Helpdesk Approval, set to ACTIVE
							role.setStatus(RoleStatus.ACTIVE);
							role.setStatusDate(new Date());
							break;
					}
				}
				role.setStatusDate(new Date());
				break;
			case ACTIVE:
				if (role.isMissingRequiredFields()) {
					role.setStatus(RoleStatus.CREATED);
				}
				role.setStatusDate(new Date());
				break;
			case BLOCKED:
			case DELETED:
				// If the role is blocked from the Helpdesk, only the Helpdesk can change it
				break;
		}
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
		if (isIncompatibleRole(newRole, loggedOn.getRoles())) {
			throw new RestException(Status.CONFLICT, "incompatible.role");
		}

		// Update
		try {
			newRole = em.merge(newRole);
			refreshRoleStatus(newRole);
			em.flush();
			return newRole;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !existingRole.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Long managerInstitutionId;
		Long institutionId;
		switch (existingRole.getDiscriminator()) {
			case INSTITUTION_ASSISTANT:
				managerInstitutionId = ((InstitutionAssistant) existingRole).getManager().getInstitution().getId();
				institutionId = ((InstitutionAssistant) role).getInstitution().getId();
				if (!managerInstitutionId.equals(institutionId)) {
					throw new RestException(Status.CONFLICT, "manager.institution.mismatch");
				}
			case DEPARTMENT_ASSISTANT:
				managerInstitutionId = ((InstitutionAssistant) existingRole).getManager().getInstitution().getId();
				institutionId = ((DepartmentAssistant) role).getDepartment().getInstitution().getId();
				if (!managerInstitutionId.equals(institutionId)) {
					throw new RestException(Status.CONFLICT, "manager.institution.mismatch");
				}
			default:
				break;
		}

		try {
			// Update
			existingRole = existingRole.copyFrom(role);
			refreshRoleStatus(existingRole);
			// Return Result
			em.flush();
			return existingRole;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);

		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		User user = em.find(User.class, role.getUser().getId());
		if (user.getRoles().size() < 2) {
			throw new RestException(Status.FORBIDDEN, "one.role.required");
		}
		if (user.getRoles().size() >= 2 && (user.hasRole(RoleDiscriminator.PROFESSOR_FOREIGN) || user.hasRole(RoleDiscriminator.PROFESSOR_DOMESTIC)) && !role.getDiscriminator().equals(RoleDiscriminator.CANDIDATE)) {
			throw new RestException(Status.FORBIDDEN, "one.role.required");
		}
		try {
			// Delete:
			user.removeRole(role);
			em.remove(role);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
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
				if (CandidateFile.fileTypes.containsKey(type)) {
					//TODO: Validate number of types allowed (one, many) against existing
					CandidateFile candidateFile = new CandidateFile();
					candidateFile.setCandidate(candidate);
					candidateFile.setOwner(candidate.getUser());
					saveFile(loggedOn, fileItems, candidateFile);
					candidate.addFile(candidateFile);

					refreshRoleStatus(role);
					em.flush();

					return toJSON(candidateFile, SimpleFileHeaderView.class);
				} else {
					throw new RestException(Status.CONFLICT, "wrong.file.type");
				}
			} else if (role instanceof Professor) {
				Professor professor = (Professor) role;
				if (ProfessorFile.fileTypes.containsKey(type)) {
					//TODO: Validate number of types allowed (one, many) against existing
					ProfessorFile professorFile = new ProfessorFile();
					professorFile.setProfessor(professor);
					professorFile.setOwner(professor.getUser());
					saveFile(loggedOn, fileItems, professorFile);
					professor.addFile(professorFile);

					refreshRoleStatus(role);
					em.flush();

					return toJSON(professorFile, SimpleFileHeaderView.class);
				} else {
					throw new RestException(Status.CONFLICT, "wrong.file.type");
				}
			} else {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
				saveFile(loggedOn, fileItems, candidateFile);
				refreshRoleStatus(role);
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
				refreshRoleStatus(role);
				em.flush();
				professorFile.getBodies().size();
				return toJSON(professorFile, SimpleFileHeaderView.class);
			} else {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
				try {
					CandidateFile candidateFile = (CandidateFile) em.createQuery(
						"select cf from CandidateFile cf " +
							"where cf.candidate.id = :candidateId " +
							"and cf.id = :fileId")
						.setParameter("candidateId", id)
						.setParameter("fileId", fileId)
						.getSingleResult();
					if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(candidateFile.getOwner().getId())) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					Response retv = deleteFileBody(candidateFile);
					if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
						// Remove from Role
						em.remove(candidateFile);
						refreshRoleStatus(role);
					}
					return retv;
				} catch (NoResultException e) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
			} else if (role instanceof Professor) {
				try {
					ProfessorFile professorFile = (ProfessorFile) em.createQuery(
						"select cf from ProfessorFile cf " +
							"where cf.candidate.id = :candidateId " +
							"and cf.id = :fileId")
						.setParameter("candidateId", id)
						.setParameter("fileId", fileId)
						.getSingleResult();
					if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(professorFile.getOwner().getId())) {
						throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
					}
					Response retv = deleteFileBody(professorFile);
					if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
						// Remove from Role
						em.remove(professorFile);
						refreshRoleStatus(role);
					}
					return retv;
				} catch (NoResultException e) {
					throw new RestException(Status.NOT_FOUND, "wrong.file.id");
				}
			}
			// Default Action
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
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
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Update
		try {
			// Update Status
			existingRole.setStatus(requestRole.getStatus());
			existingRole.setStatusDate(new Date());

			return existingRole;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

}
