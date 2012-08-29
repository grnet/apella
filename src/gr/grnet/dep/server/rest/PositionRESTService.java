package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/position")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PositionRESTService extends RESTService {

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@Inject
	private Logger log;

	private Position getAndCheckPosition(String authToken, long positionId) {
		Position position = (Position) em.createQuery(
			"from Position p where p.id=:id")
			.setParameter("id", positionId)
			.getSingleResult();
		Department department = position.getDepartment();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
			&& !loggedOn.isDepartmentUser(department)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		return position;
	}

	@GET
	@JsonView({DetailedPositionView.class})
	public List<Position> getAll(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("user") long userId) {
		getLoggedOn(authToken);

		User requestUser = em.find(User.class, userId);
		if (requestUser == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.user.id");
		}
		//TODO : Return positions based on User Roles
		@SuppressWarnings("unchecked")
		List<Position> positions = (List<Position>) em.createQuery(
			"from Position p ")
			.getResultList();

		return positions;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedPositionView.class})
	public Position get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);

		Position p = (Position) em.createQuery(
			"from Position p where p.id=:id")
			.setParameter("id", id)
			.getSingleResult();

		return p;
	}

	@POST
	@JsonView({DetailedPositionView.class})
	public Position create(@HeaderParam(TOKEN_HEADER) String authToken, Position position) {
		try {
			Department department = em.find(Department.class, position.getDepartment().getId());
			if (department == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.department");
			}
			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
				&& !loggedOn.isDepartmentUser(department)) {
				throw new RestException(Status.FORBIDDEN, "insufficinet.privileges");
			}
			position = em.merge(position);
			em.flush();
			return position;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		try {
			Position existingPosition = getAndCheckPosition(authToken, id);

			existingPosition.setSubject(position.getSubject());
			existingPosition.setDeanStatus(position.getDeanStatus());
			existingPosition.setDescription(position.getDescription());
			existingPosition.setFek(position.getFek());
			existingPosition.setFekSentDate(position.getFekSentDate());
			existingPosition.setName(position.getName());
			existingPosition.setStatus(position.getStatus());

			position = em.merge(existingPosition);
			em.flush();
			return position;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			Position position = getAndCheckPosition(authToken, id);
			//Do Delete:
			em.remove(position);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fekFile}{fileId:(/[0-9][0-9]*)?}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var) {
		FileHeader file = null;
		getLoggedOn(authToken);

		Position position = em.find(Position.class, id);
		// Validate:
		if (position == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}

		if ("prosklisiKosmitora".equals(var)) {
			file = position.getProsklisiKosmitora();
		} else if ("recommendatoryReport".equals(var)) {
			file = position.getRecommendatoryReport();
		} else if ("recommendatoryReportSecond".equals(var)) {
			file = position.getRecommendatoryReportSecond();
		} else if ("fekFile".equals(var)) {
			file = position.getFekFile();
		}

		if (file != null) {
			file.getBodies().size();
		}
		return file;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fekFile}{fileId:(/[0-9][0-9]*)?}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		try {
			Position position = getAndCheckPosition(authToken, id);
			FileHeader file = null;
			if ("prosklisiKosmitora".equals(var)) {
				file = uploadFile(loggedOn, request, position.getProsklisiKosmitora());
				position.setProsklisiKosmitora(file);
			} else if ("recommendatoryReport".equals(var)) {
				file = uploadFile(loggedOn, request, position.getRecommendatoryReport());
				position.setRecommendatoryReport(file);
			} else if ("recommendatoryReportSecond".equals(var)) {
				file = uploadFile(loggedOn, request, position.getRecommendatoryReportSecond());
				position.setRecommendatoryReportSecond(file);
			} else if ("fekFile".equals(var)) {
				file = uploadFile(loggedOn, request, position.getFekFile());
				position.setFekFile(file);
			}
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
	 * @param id
	 * @param var
	 * @return
	 */
	@DELETE
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fekFile}{fileId:(/[0-9][0-9]*)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("var") String var) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			FileHeader file = getFile(authToken, id, var);
			if (file == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.id");
			}
			Response retv = deleteFileBody(file);
			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Break the relationship as well
				if ("prosklisiKosmitora".equals(var)) {
					position.setProsklisiKosmitora(null);
				} else if ("recommendatoryReport".equals(var)) {
					position.setRecommendatoryReport(null);
				} else if ("recommendatoryReportSecond".equals(var)) {
					position.setRecommendatoryReportSecond(null);
				} else if ("fekFile".equals(var)) {
					position.setFekFile(null);
				}
			}
			em.flush();
			return retv;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}
}
