package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.Professor;
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

@Path("/position/committee")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PositionCommitteeRESTService extends RESTService {

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
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(department)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		return position;
	}

	@GET
	public List<PositionCommitteeMember> getPositionCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("position") Long positionId) {
		Position position = getAndCheckPosition(authToken, positionId);
		position.getCommitee().size();
		return position.getCommitee();
	}

	@GET
	@Path("/{cmId:[0-9][0-9]*}")
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") Long cmId) {
		PositionCommitteeMember result = em.find(PositionCommitteeMember.class, cmId);
		if (result == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		return result;
	}

	@POST
	public PositionCommitteeMember createPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, PositionCommitteeMember newMembership) {
		try {
			Position existingPosition = getAndCheckPosition(authToken, newMembership.getPosition().getId());
			if (existingPosition.getCommitee().size() >= PositionCommitteeMember.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}
			Professor existingProfessor = em.find(Professor.class, newMembership.getProfessor().getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			newMembership.setPosition(existingPosition);
			newMembership.setProfessor(existingProfessor);
			existingPosition.getCommitee().add(newMembership);

			newMembership = em.merge(newMembership);
			em.flush();
			return newMembership;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{cmId:[0-9][0-9]*}")
	public void removePositionCommiteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") Long cmId) {
		try {
			PositionCommitteeMember existingCM = getPositionCommitteeMember(authToken, cmId);

			//TODO: Will obviously not be allowed after some point in the lifecycle

			// Remove
			existingCM.getPosition().getCommitee().remove(existingCM);
			em.remove(existingCM);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{cmId:[0-9][0-9]*}/report")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") long cmId) {
		PositionCommitteeMember cm = getPositionCommitteeMember(authToken, cmId);
		FileHeader file = cm.getRecommendatoryReport();
		if (file != null) {
			file.getBodies().size();
		}
		return file;
	}

	@POST
	@Path("/{cmId:[0-9][0-9]*}/report")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") Long cmId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		try {
			PositionCommitteeMember cm = getPositionCommitteeMember(authToken, cmId);
			FileHeader file = cm.getRecommendatoryReport();

			//TODO: Security / lifecycle checks

			// Update
			file = uploadFile(loggedOn, request, file);
			cm.setRecommendatoryReport(file);
			em.persist(cm);
			em.flush();
			return file;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}

	}

	@DELETE
	@Path("/{cmId:[0-9][0-9]*}/report")
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") long cmId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			PositionCommitteeMember cm = getPositionCommitteeMember(authToken, cmId);
			FileHeader file = cm.getRecommendatoryReport();

			//TODO: Security / lifecycle checks

			Response retv = deleteFileBody(file);

			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				cm.setRecommendatoryReport(null);
			}
			em.flush();

			return retv;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}
}
