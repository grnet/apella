package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Rank;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@Path("/rank")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RankRESTService extends RESTService {

	@GET
	@SuppressWarnings("unchecked")
	public List<Rank> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		return (List<Rank>) em.createQuery(
			"select distinct r from Rank r ")
			.getResultList();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Rank get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
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