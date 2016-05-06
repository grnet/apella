package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.CandidateService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;

@Path("/candidate")
public class CandidateRESTService extends RESTService {

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
            // find candidacies
            List<Candidacy> retv = candidateService.getCandidaciesForSpecificCandidate(candidateId, open, loggedOn);

            return retv;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


}
