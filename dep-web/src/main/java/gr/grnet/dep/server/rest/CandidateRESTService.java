package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.CandidateService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
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
public class CandidateRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private CandidateService candidateService;

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
        try {
            User loggedOn = getLoggedOn(authToken);
            // get candidate
            Candidate candidate = candidateService.getCandidate(candidateId);

            if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
                    !candidate.getUser().getId().equals(loggedOn.getId())) {
                throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
            }
            // find candidacies
            List<Candidacy> retv = candidateService.getCandidaciesForSpecificCandidate(candidateId, open, loggedOn);

            return retv;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        }
    }


}
