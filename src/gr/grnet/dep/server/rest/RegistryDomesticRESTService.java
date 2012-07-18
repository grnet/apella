package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.RegistryDomestic;
import gr.grnet.dep.service.model.RegistryMembershipDomestic;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@Path("/registry/domestic")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RegistryDomesticRESTService extends RESTService {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@GET
	public Collection<RegistryDomestic> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		@SuppressWarnings("unchecked")
		Collection<RegistryDomestic> registries = (Collection<RegistryDomestic>) em.createQuery(
			"from RegistryDomestic r ")
			.getResultList();
		for (RegistryDomestic r : registries) {
			r.initializeCollections();
		}
		return registries;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public RegistryDomestic get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		try {
			RegistryDomestic r = (RegistryDomestic) em.createQuery(
				"from RegistryDomestic r where r.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			r.initializeCollections();
			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

	}

	@POST
	public RegistryDomestic create(@HeaderParam(TOKEN_HEADER) String authToken, RegistryDomestic newRegistry) {
		User loggedOn = getLoggedOn(authToken);

		// Validate
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(newRegistry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Update
		try {
			em.persist(newRegistry);
			em.flush();
			return newRegistry;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public RegistryDomestic update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, RegistryDomestic registry) {
		User loggedOn = getLoggedOn(authToken);
		RegistryDomestic existingRegistry = em.find(RegistryDomestic.class, id);
		// Validate:
		if (existingRegistry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(registry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRegistry.getInstitution().getId().equals(registry.getInstitution().getId())) {
			throw new RestException(Status.CONFLICT, "registry.institution.change");
		}

		try {
			// Update
			existingRegistry = existingRegistry.copyFrom(registry);
			// Return Result
			em.flush();
			return existingRegistry;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);

		RegistryDomestic registry = em.find(RegistryDomestic.class, id);
		// Validate:
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(registry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// Delete:
			em.remove(registry);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/member")
	public Collection<RegistryMembershipDomestic> getMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		RegistryDomestic registry = em.find(RegistryDomestic.class, id);
		// Validate:
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(registry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Get File
		registry.getMembers().size();
		return registry.getMembers();
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/member")
	@Produces({MediaType.APPLICATION_JSON})
	public RegistryMembershipDomestic addMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, RegistryMembershipDomestic newMember) {
		User loggedOn = getLoggedOn(authToken);

		RegistryDomestic registry = null;
		ProfessorDomestic professor = null;
		// Validate:
		if (!id.equals(newMember.getRegistry().getId())) {
			throw new RestException(Status.CONFLICT, "wrong.registry.id");
		}
		registry = em.find(RegistryDomestic.class, id);
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.registry.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(registry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		professor = em.find(ProfessorDomestic.class, newMember.getProfessor().getId());
		if (professor == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		//Update
		try {
			newMember.setRegistry(registry);
			newMember.setProfessor(professor);
			newMember = em.merge(newMember);
			registry.getMembers().add(newMember);
			em.flush();
			return newMember;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/member/{memberId:([0-9][0-9]*)}")
	public void removeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("memberId") long memberId) {
		User loggedOn = getLoggedOn(authToken);

		// Validate
		RegistryDomestic registry = em.find(RegistryDomestic.class, id);
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.registry.id");
		}
		RegistryMembershipDomestic member = em.find(RegistryMembershipDomestic.class, memberId);
		if (member == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.member.id");
		}
		if (!member.getRegistry().getId().equals(id)) {
			throw new RestException(Status.CONFLICT, "wrong.member.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(registry.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Update
		try {
			registry.getMembers().remove(member);
			em.remove(member);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}
}
