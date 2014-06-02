package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Rank;

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
@Stateless
public class RankRESTService extends RESTService {

	/**
	 * Returns all existing ranks
	 *
	 * @param authToken The authenticatio token
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Rank> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		return (List<Rank>) em.createQuery(
				"select distinct r from Rank r ")
				.getResultList();
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
			return (Rank) em.createQuery(
					"select r from Rank r " +
							"where r.id = :id")
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.rank.id");
		}
	}
}
