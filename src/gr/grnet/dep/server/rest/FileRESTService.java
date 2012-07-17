package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

@Path("/file")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class FileRESTService extends RESTService {

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			FileHeader fh = (FileHeader) em.createQuery(
				"from FileHeader fh left join fetch fh.bodies " +
					"where fh.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(fh.getOwner().getId())) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			return fh;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
	}

}
