package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;
import org.jboss.resteasy.spi.NoLogWebApplicationException;

@Path("/file")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class FileRESTService extends RESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@Inject
	private Logger logger;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		FileHeader fh = (FileHeader) em.createQuery(
			"from FileHeader fh left join fetch fh.bodies " +
				"where fh.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(fh.getOwner().getId())) {
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		}

		return fh;
	}

}
