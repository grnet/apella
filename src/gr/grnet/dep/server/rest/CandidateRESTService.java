package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/candidate")
@Stateless
public class CandidateRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@Path("/{id:[0-9]+}/candidacies")
	@JsonView({DetailedCandidacyView.class})
	public Collection<Candidacy> getCandidacies(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidateId, @QueryParam("open") String open) {
		User loggedOn = getLoggedOn(authToken);
		Candidate candidate = em.find(Candidate.class, candidateId);
		if (candidate == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !candidate.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		String queryString = "from Candidacy c where c.candidate = :candidate";
		if (open != null) {
			queryString += " and c.position.closingDate >= :now";
		}
		Query query = em.createQuery(queryString)
			.setParameter("candidate", candidate);

		if (open != null) {
			Date now = new Date();
			query.setParameter("now", now);
		}

		@SuppressWarnings("unchecked")
		List<Candidacy> retv = query.getResultList();
		for (Candidacy cy : retv) {
			cy.initializeCollections();
		}
		return retv;
	}

}
