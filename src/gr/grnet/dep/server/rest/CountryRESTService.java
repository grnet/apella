package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Country;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/country")
@Stateless
public class CountryRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@SuppressWarnings("unchecked")
	public Collection<Country> getAll() {
		return em.createQuery("from Country c order by c.name, c.code").getResultList();
	}
}
