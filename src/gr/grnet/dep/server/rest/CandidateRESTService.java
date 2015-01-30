package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Path("/candidate")
@Stateless
public class CandidateRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns Candidate's Candidacies
	 *
	 * @param authToken   The authentication token
	 * @param candidateId The ID of the candidate
	 * @param open        If it set returns only candidacies of open positions
	 * @return
	 * @HTTP 403 X-Error-Code: wrong.candidate.id
	 * @HTTP 404 X-Error-Code: insufficient.privileges
	 */
	@GET
	@Path("/{id:[0-9]+}/candidacies")
	@JsonView({MediumCandidacyView.class})
	public Collection<Candidacy> getCandidacies(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long candidateId, @QueryParam("open") String open) {
		User loggedOn = getLoggedOn(authToken);
		Candidate candidate = em.find(Candidate.class, candidateId);
		if (candidate == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidate.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!candidate.getUser().getId().equals(loggedOn.getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		String queryString = "from Candidacy c " +
				"left join fetch c.candidate.user.roles cerls " +
				"left join fetch c.candidacies ca " +
				"left join fetch ca.position po " +
				"where c.candidate = :candidate " +
				"and c.permanent = true ";
		if (open != null) {
			queryString += " and c.candidacies.closingDate >= :now";
		}
        if (candidate.getUser().getId().equals(loggedOn.getId())) {
            queryString += " and c.withdrawn = false";
        }
		TypedQuery<Candidacy> query = em.createQuery(queryString, Candidacy.class)
				.setParameter("candidate", candidate);

		if (open != null) {
			Date now = new Date();
			query.setParameter("now", now);
		}

		List<Candidacy> retv = query.getResultList();
		List<Long> candidaciesThatCanAddEvaluators = canAddEvaluators(retv);
		List<Long> candidaciesThatNominationCommitteeConverged = hasNominationCommitteeConverged(retv);

		for (Candidacy c : retv) {
			c.setNominationCommitteeConverged(candidaciesThatNominationCommitteeConverged.contains(c.getId()));
			c.setCanAddEvaluators(candidaciesThatCanAddEvaluators.contains(c.getId()));
		}
		return retv;
	}

	private List<Long> hasNominationCommitteeConverged(List<Candidacy> candidacies) {
		Set<Long> ids = new HashSet<Long>();
		for (Candidacy c : candidacies) {
			ids.add(c.getId());
		}
		if (ids.isEmpty()) {
			return new ArrayList<Long>();
		}
		List<Long> data = em.createQuery(
				"select c.id from Candidacy c " +
						"join c.candidacies.position.phase.nomination no " +
						"where no.nominationCommitteeConvergenceDate IS NOT NULL " +
                        "and no.nominationCommitteeConvergenceDate < :now " +
                        "and c.id in (:ids) ", Long.class)
				.setParameter("ids", ids)
				.setParameter("now", new Date())
				.getResultList();
		return data;
	}

	private List<Long> canAddEvaluators(List<Candidacy> candidacies) {
		Set<Long> ids = new HashSet<Long>();
		for (Candidacy c : candidacies) {
			ids.add(c.getId());
		}
		if (ids.isEmpty()) {
			return new ArrayList<Long>();
		}
		List<Long> data = em.createQuery(
				"select c.id from Candidacy c " +
						"join c.candidacies.position.phase.committee co " +
						"where co.members IS NOT EMPTY " +
						"and co.candidacyEvalutionsDueDate >= :now " +
                        "and c.id in (:ids) ", Long.class)
				.setParameter("ids", ids)
				.setParameter("now", new Date())
				.getResultList();
		return data;
	}
}
