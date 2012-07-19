package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.Registry;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/registry")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RegistryRESTService extends RESTService {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@GET
	public Collection<Registry> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		@SuppressWarnings("unchecked")
		Collection<Registry> registries = (Collection<Registry>) em.createQuery(
			"from RegistryDomestic r ")
			.getResultList();
		return registries;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Registry get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		try {
			Registry r = (Registry) em.createQuery(
				"from RegistryDomestic r where r.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

	}

	@POST
	public Registry create(@HeaderParam(TOKEN_HEADER) String authToken, Registry newRegistry) {
		User loggedOn = getLoggedOn(authToken);

		// Validate
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(newRegistry.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		// Update
		try {
			em.persist(newRegistry);
			em.flush();
			return newRegistry;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Registry update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Registry registry) {
		User loggedOn = getLoggedOn(authToken);
		Registry existingRegistry = em.find(Registry.class, id);
		// Validate:
		if (existingRegistry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(registry.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRegistry.getDepartment().getId().equals(registry.getDepartment().getId())) {
			throw new RestException(Status.CONFLICT, "registry.department.change");
		}

		try {
			// Update
			existingRegistry = existingRegistry.copyFrom(registry);
			// Return Result
			em.flush();
			return existingRegistry;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);

		Registry registry = em.find(Registry.class, id);
		// Validate:
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(registry.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// Delete:
			em.remove(registry);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/file/{fileId:(/[0-9][0-9]*)?}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") String fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Registry registry = em.find(Registry.class, id);
		// Validate:
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(registry.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Do Upload
		try {
			FileHeader file = uploadFile(loggedOn, request, registry.getRegistryFile());
			em.flush();
			return file;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	/**
	 * Deletes the last body of given file, if possible.
	 * 
	 * @param authToken
	 * @param registryId
	 * @return
	 */
	@DELETE
	@Path("/{registryId:[0-9][0-9]*}/file/{fileId:([0-9][0-9]*)}")
	@JsonView({DetailedFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("registryId") long registryId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Registry registry = em.find(Registry.class, registryId);
		// Validate:
		if (registry == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.registry.id");
		}
		if (registry.getRegistryFile() == null || !registry.getRegistryFile().getId().equals(fileId)) {
			throw new RestException(Status.CONFLICT, "wrong.file.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(registry.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			FileHeader file = registry.getRegistryFile();
			Response retv = deleteFileBody(file);
			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Break the relationship as well
				registry.setRegistryFile(null);
			}
			return retv;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}
}
