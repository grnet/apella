package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.DepartmentAssistant;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
			refreshRoleStatus(newRole);
			em.persist(newRole);
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
	@Path("/{id:[0-9][0-9]*}/{var:fekFile|cv|identity|military1599|profileFile}{fileId:(/[0-9][0-9]*)?}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var, @PathParam("fileId") String fileId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Get File
		FileHeader file = null;
		if ("fekFile".equals(var)) {
			ProfessorDomestic professorDomestic = (ProfessorDomestic) role;
			file = professorDomestic.getFekFile();
		} else if ("cv".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = candidate.getCv();
		} else if ("identity".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = candidate.getIdentity();
		} else if ("military1599".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = candidate.getMilitary1599();
		} else if ("profileFile".equals(var)) {
			Professor professor = (Professor) role;
			file = professor.getProfileFile();
		}

		if (file != null) {
			file.getBodies().size();
		} else {
			throw new RestException(Status.NOT_FOUND, "missing.file");
		}
		return file;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/publications")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<FileHeader> getPublicationFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!(role instanceof Candidate)) {
			throw new RestException(Status.NOT_FOUND, "wrong.role");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Get File
		Candidate c = (Candidate) role;
		c.getPublications().size();
		return c.getPublications();
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/{var:fekFile|cv|identity|military1599|publications|profileFile}{fileId:(/[0-9][0-9]*)?}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var, @PathParam("fileId") String fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);

		// Validate:
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !role.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		try {
			FileHeader file = null;
			if ("fekFile".equals(var)) {
				ProfessorDomestic professorDomestic = (ProfessorDomestic) role;
				file = uploadFile(loggedOn, request, professorDomestic.getFekFile());
				professorDomestic.setFekFile(file);
			} else if ("cv".equals(var)) {
				Candidate candidate = (Candidate) role;
				file = uploadFile(loggedOn, request, candidate.getCv());
				candidate.setCv(file);
			} else if ("identity".equals(var)) {
				Candidate candidate = (Candidate) role;
				file = uploadFile(loggedOn, request, candidate.getIdentity());
				candidate.setIdentity(file);
			} else if ("military1599".equals(var)) {
				Candidate candidate = (Candidate) role;
				file = uploadFile(loggedOn, request, candidate.getMilitary1599());
				candidate.setMilitary1599(file);
			} else if ("profileFile".equals(var)) {
				Professor professor = (Professor) role;
				file = uploadFile(loggedOn, request, professor.getProfileFile());
				professor.setProfileFile(file);
			} else if ("publications".equals(var)) {
				Candidate candidate = (Candidate) role;
				Set<FileHeader> publications = candidate.getPublications();
				FileHeader existingPublication = null;
				Long fileHeaderId = (fileId.isEmpty()) ? -1L : Long.parseLong(fileId.substring(1));
				if (fileHeaderId > 0) {
					for (FileHeader fh : publications) {
						if (fh.getId().equals(fileHeaderId)) {
							existingPublication = fh;
							break;
						}
					}
				}
				if (existingPublication == null) {
					file = uploadFile(loggedOn, request, null);
					publications.add(file);
				} else {
					file = uploadFile(loggedOn, request, existingPublication);
				}
			}

			refreshRoleStatus(role);
			em.flush();
			return file;
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
	@Path("/{id:[0-9][0-9]*}/{var:fekFile|cv|identity|military1599|profileFile}{fileId:(/[0-9][0-9]*)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("var") String var, @PathParam("fileId") String fileId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		try {
			FileHeader file = getFile(authToken, id, var, fileId);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(file.getOwner().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Response retv = deleteFileBody(file);

			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Break the relationship as well
				if ("fekFile".equals(var)) {
					ProfessorDomestic professorDomestic = (ProfessorDomestic) role;
					professorDomestic.setFekFile(null);
				} else if ("cv".equals(var)) {
					Candidate candidate = (Candidate) role;
					candidate.setCv(null);
				} else if ("identity".equals(var)) {
					Candidate candidate = (Candidate) role;
					candidate.setIdentity(null);
				} else if ("military1599".equals(var)) {
					Candidate candidate = (Candidate) role;
					candidate.setMilitary1599(null);
				} else if ("profileFile".equals(var)) {
					Professor professor = (Professor) role;
					professor.setProfileFile(null);
				}
			}

			refreshRoleStatus(role);
			return retv;
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
	@Path("/{id:[0-9][0-9]*}/publications/{fileId:[0-9][0-9]*}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deletePublication(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Role role = em.find(Role.class, id);
		if (role == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!(role instanceof Candidate)) {
			throw new RestException(Status.NOT_FOUND, "wrong.role");
		}
		try {
			Candidate candidate = (Candidate) role;
			FileHeader publication = null;
			for (FileHeader fh : candidate.getPublications()) {
				if (fh.getId().equals(fileId)) {
					publication = fh;
					break;
				}
			}
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(publication.getOwner().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			Response retv = deleteFileBody(publication);
			candidate.getPublications().remove(publication);

			refreshRoleStatus(role);
			em.flush();
			return retv;
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
