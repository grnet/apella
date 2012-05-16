package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;
import org.jboss.resteasy.spi.NoLogWebApplicationException;

@Path("/role")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RoleRESTService extends RESTService {

	

	@SuppressWarnings("unused")
	@Inject
	private Logger log;
	
	@PersistenceContext(unitName = "depdb")
	private EntityManager em;
	
	
	private static final Set<Pair> forbiddenPairs;
	static {
		Set<Pair> aSet = new HashSet<Pair>();
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.CANDIDATE));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.DEPARTMENT_MANAGER));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_ASSISTANT));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.INSTITUTION_MANAGER));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.MINISTRY_MANAGER));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_DOMESTIC));
        aSet.add(new Pair(RoleDiscriminator.ADMINISTRATOR, RoleDiscriminator.PROFESSOR_FOREIGN));
        forbiddenPairs = Collections.unmodifiableSet(aSet);
    }
	
	private static class Pair {
		RoleDiscriminator first;
		RoleDiscriminator second;
		
		public Pair(RoleDiscriminator first, RoleDiscriminator second) {
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
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (first != other.first)
				return false;
			if (second != other.second)
				return false;
			return true;
		}	
	}


	@GET
	@JsonView({DetailedRoleView.class})
	public Collection<Role> getAll(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("user") Long userID) {
		getLoggedOn(authToken);
		if (userID != null) {
			@SuppressWarnings("unchecked")
			Collection<Role> roles = (Collection<Role>) em.createQuery(
				"from Role r " +
					"where r.user = :userID ")
				.setParameter("userID", userID)
				.getResultList();
			for (Role r : roles) {
				r.initializeCollections();
			}
			return roles;
		} else {
			throw new NoLogWebApplicationException(Status.BAD_REQUEST);
		}
	}

	
	private boolean isForbiddenPair(RoleDiscriminator first, RoleDiscriminator second) {
		return forbiddenPairs.contains(new Pair(first, second))
					|| forbiddenPairs.contains(new Pair(second, first));
	}
	
	private boolean isIncompatibleRole(Role role, Collection<Role> roles) {
		for (Role r: roles) {
			if (isForbiddenPair(role.getDiscriminator(), r.getDiscriminator())) return true;
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
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& !roleBelongsToUser)
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		Role r = (Role) em.createQuery(
			"from Role r where r.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		r.initializeCollections();
		return r;
	}

	@POST
	@JsonView({DetailedRoleView.class})
	public Role create(@HeaderParam(TOKEN_HEADER) String authToken, Role role) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && role.getUser() != loggedOn.getId()) {
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		}
		if (isIncompatibleRole(role, loggedOn.getRoles())) {
			throw new WebApplicationException(Response.status(Status.CONFLICT).
						header("X-Error-Code", "incompatible.role").build());
		}
		
		em.persist(role);
		return role;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRoleView.class})
	public Role update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Role role) {
		Role existingRole = em.find(Role.class, id);
		if (existingRole == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && existingRole.getUser() != loggedOn.getId()) {
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		}
		Role r = existingRole.copyFrom(role);

		return r;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);

		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& role.getUser() != loggedOn.getId())
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		// Delete:
		User user = em.find(User.class, role.getUser());
		user.removeRole(role);
		em.remove(role);
	}

	
	@POST
	@Path("/{id:[0-9][0-9]*}/{var:fekFile|cv|identity|military1599}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({ SimpleFileHeaderView.class })
	public FileHeader cv(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("var") String var, @Context HttpServletRequest request) throws FileUploadException, IOException 
	{
		FileHeader file = null;
		User loggedOn = getLoggedOn(authToken);

		Role role = em.find(Role.class, id);
		// Validate:
		if (role == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && role.getUser() != loggedOn.getId()) {
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		}
		
		if ("fekFile".equals(var)) {
			ProfessorDomestic professorDomestic = (ProfessorDomestic) role;
			file = uploadFile(loggedOn, request, professorDomestic.getFekFile());
			professorDomestic.setFekFile(file);
		}
		else if ("cv".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = uploadFile(loggedOn, request, candidate.getCv());
			candidate.setCv(file);
		}
		else if ("identity".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = uploadFile(loggedOn, request, candidate.getIdentity());
			candidate.setIdentity(file);
		}
		else if ("military1599".equals(var)) {
			Candidate candidate = (Candidate) role;
			file = uploadFile(loggedOn, request, candidate.getMilitary1599());
			candidate.setMilitary1599(file);
		}
		
		return file;
	}
	
}
