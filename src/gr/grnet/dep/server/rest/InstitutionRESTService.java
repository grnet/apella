package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/institution")
@Stateless
public class InstitutionRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all Institutions
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Institution> getAll() {
		return (List<Institution>) em.createQuery(
				"select distinct i from Institution i ")
				.getResultList();
	}

	/**
	 * Returns Institution with given ID
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Institution get(@PathParam("id") long id) {
		try {
			return (Institution) em.createQuery(
					"select i from Institution i " +
							"where i.id = :id")
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
	}
}
