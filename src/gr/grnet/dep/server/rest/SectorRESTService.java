package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Sector;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/sector")
@Stateless
public class SectorRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all sectors
	 *
	 * @return
	 */
	@GET
	public Collection<Sector> getAll() {
		return em.createQuery("from Sector s order by s.areaId, s.subjectId", Sector.class).getResultList();
	}
}
