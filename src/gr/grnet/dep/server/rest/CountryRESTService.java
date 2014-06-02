package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Country;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/country")
@Stateless
public class CountryRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all Countries
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Country> getAll() {
		return em.createQuery("from Country c order by c.name, c.code").getResultList();
	}
}
