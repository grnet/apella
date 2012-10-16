package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.ProfessorCommitteesView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/professor")
@Stateless
public class ProfessorRESTService extends RESTService {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@GET
	@JsonView({DetailedRoleView.class})
	public Collection<Role> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		// Prepare Query
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);
		// Set Parameters
		@SuppressWarnings("unchecked")
		List<Role> roles = em.createQuery(
			"select r from Role r " +
				"where r.id is not null " +
				"and r.discriminator in (:discriminators) ")
			.setParameter("discriminators", discriminatorList)
			.getResultList();

		for (Role r : roles) {
			r.initializeCollections();
			Professor p = (Professor) r;
			p.setCommitteesCount(p.getCommittees().size());
		}
		return roles;
	}

	@GET
	@Path("/{id:[0-9]+}")
	@JsonView({DetailedRoleView.class})
	public Role get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long roleId) {
		getLoggedOn(authToken);

		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);
		try {
			Role r = (Role) em.createQuery(
				"select r from Role r " +
					"where r.id = :id " +
					"and r.discriminator in (:discriminators) ")
				.setParameter("id", roleId)
				.setParameter("discriminators", discriminatorList)
				.getSingleResult();

			r.initializeCollections();
			Professor p = (Professor) r;
			p.setCommitteesCount(p.getCommittees().size());

			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
	}

	@GET
	@Path("/{id:[0-9]+}/committees")
	@JsonView({ProfessorCommitteesView.class})
	public Collection<PositionCommitteeMember> getCommittees(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long professorId) {
		User loggedOn = getLoggedOn(authToken);
		Professor p = em.find(Professor.class, professorId);
		if (p == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !p.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		p.initializeCollections();

		return p.getCommittees();
	}

}
