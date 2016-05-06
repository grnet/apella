package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.CountryService;
import gr.grnet.dep.service.model.Country;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.List;

@Path("/country")
public class CountryRESTService extends RESTService {

	@EJB
	private CountryService countryService;

	/**
	 * Returns all Countries
	 *
	 * @return
	 */
	@GET
	public Collection<Country> getAll() {
		// get all
		List<Country> countryList = countryService.getAll();

		return countryList;
	}
}
