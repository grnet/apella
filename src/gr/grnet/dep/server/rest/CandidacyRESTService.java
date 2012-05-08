package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

import org.codehaus.jackson.map.annotate.JsonView;
import org.jboss.resteasy.spi.NoLogWebApplicationException;

@Path("/candidacy")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CandidacyRESTService extends RESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@SuppressWarnings("unused")
	@Inject
	private Logger log;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Candidacy c = (Candidacy) em.createQuery(
			"from Candidacy c left join fetch c.files " +
				"where c.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		Candidate cy = (Candidate) em.createQuery(
			"from Candidate c where c.id=:id")
			.setParameter("id", c.getCandidate())
			.getSingleResult();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& cy.getUser() != loggedOn.getId())
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		return c;
	}

	@POST
	@JsonView({DetailedCandidacyView.class})
	public Candidacy create(@HeaderParam(TOKEN_HEADER) String authToken, Candidacy candidacy) {
		Candidate cy = (Candidate) em.createQuery(
			"from Candidate c where c.id=:id")
			.setParameter("id", candidacy.getCandidate())
			.getSingleResult();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& cy.getUser() != loggedOn.getId())
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		candidacy.setDate(new Date());

		em.persist(candidacy);
		return candidacy;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		Candidacy existingCandidacy = em.find(Candidacy.class, id);
		if (existingCandidacy == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Candidate cy = (Candidate) em.createQuery(
			"from Candidate c where c.id=:id")
			.setParameter("id", existingCandidacy.getCandidate())
			.getSingleResult();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& cy.getUser() != loggedOn.getId())
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		// So far there are no fields to update!

		return existingCandidacy;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Candidacy existingCandidacy = em.find(Candidacy.class, id);
		if (existingCandidacy == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Candidate cy = (Candidate) em.createQuery(
			"from Candidate c where c.id=:id")
			.setParameter("id", existingCandidacy.getCandidate())
			.getSingleResult();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& cy.getUser() != loggedOn.getId())
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		//Do Delete:
		em.remove(existingCandidacy);
	}

}
