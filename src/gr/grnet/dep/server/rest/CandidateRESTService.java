package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.PositionCommittee;
import gr.grnet.dep.service.model.PositionNomination;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DateUtil;

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

	/**
	 * Returns Candidate's Candidacies
	 * 
	 * @param authToken The authentication token
	 * @param candidateId The ID of the candidate
	 * @param open If it set returns only candidacies of open positions
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
			"left join fetch c.candidacies.position.assistants pasnt " +
			"left join fetch pasnt.roles pasntrls " +
			"where c.candidate = :candidate " +
			"and c.permanent = true ";
		if (open != null) {
			queryString += " and c.candidacies.closingDate >= :now";
		}
		Query query = em.createQuery(queryString)
			.setParameter("candidate", candidate);

		if (open != null) {
			Date now = new Date();
			query.setParameter("now", now);
		}

		@SuppressWarnings("unchecked")
		List<Candidacy> retv = query.getResultList();

		for (Candidacy c : retv) {
			c.setNominationCommitteeConverged(hasNominationCommitteeConverged(c));
			c.setCanAddEvaluators(canAddEvaluators(c));
		}

		return retv;
	}

	public boolean hasNominationCommitteeConverged(Candidacy c) {
		PositionNomination nomination = c.getCandidacies().getPosition().getPhase().getNomination();
		return nomination != null &&
			nomination.getNominationCommitteeConvergenceDate() != null &&
			DateUtil.compareDates(new Date(), nomination.getNominationCommitteeConvergenceDate()) >= 0;
	}

	public boolean canAddEvaluators(Candidacy c) {
		PositionCommittee committee = c.getCandidacies().getPosition().getPhase().getCommittee();
		return committee != null &&
			committee.getMembers().size() > 0 &&
			DateUtil.compareDates(new Date(), committee.getCandidacyEvalutionsDueDate()) < 0;
	}
}
