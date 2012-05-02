package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.SimpleCandidacyView;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/candidacy")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CandidacyRESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@SuppressWarnings("unused")
	@Inject
	private Logger log;


	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({SimpleCandidacyView.class})
	public Candidacy get(@PathParam("id") long id) {
		Candidacy c = (Candidacy) em.createQuery(
			"from Candidacy c where c.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		return c;
	}

	
}
