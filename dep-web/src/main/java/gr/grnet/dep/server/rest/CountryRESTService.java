package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.CountryService;
import gr.grnet.dep.service.model.Country;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/country")
@Stateless
public class CountryRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	private CountryService countryService;

	/**
	 * Returns all Countries
	 *
	 * @return
	 */
	@GET
	public Collection<Country> getAll() {

		List<Country> countryList = countryService.getAll();

		return countryList;
	}
}
