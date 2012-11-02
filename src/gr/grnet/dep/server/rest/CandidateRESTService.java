package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/candidate")
@Stateless
public class CandidateRESTService extends RESTService {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@GET
	@Path("/{id:[0-9]+}/candidacies")
	@JsonView({DetailedCandidacyView.class})
	public Collection<Candidacy> getCandidacies(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidateId) {
		User loggedOn = getLoggedOn(authToken);
		Candidate c = em.find(Candidate.class, candidateId);
		if (c == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !c.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		c.initializeCollections();

		for (Candidacy cy : c.getCandidacies()) {
			cy.initializeCollections();
		}

		return c.getCandidacies();
	}

}
