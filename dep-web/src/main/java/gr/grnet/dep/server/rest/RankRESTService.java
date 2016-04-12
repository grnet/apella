package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.RankService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Rank;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;

@Path("/rank")
public class RankRESTService extends RESTService {

	@EJB
	private RankService rankService;

	/**
	 * Returns all existing ranks
	 *
	 * @param authToken The authenticatio token
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public List<Rank> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		// get all
		List<Rank> ranks = rankService.getAll();
		return ranks;
	}

	/**
	 * Returns Rank with given id
	 *
	 * @param authToken
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Rank get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		try {
			Rank rank = rankService.get(id);
			return rank;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.rank.id");
		}
	}
}
