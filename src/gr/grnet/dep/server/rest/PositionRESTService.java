package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.ElectoralBody;
import gr.grnet.dep.service.model.ElectoralBody.SimpleElectoralBodyView;
import gr.grnet.dep.service.model.ElectoralBodyMembership;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.RecommendatoryCommittee;
import gr.grnet.dep.service.model.RecommendatoryCommittee.SimpleRecommendatoryCommitteeView;
import gr.grnet.dep.service.model.RecommendatoryCommitteeMembership;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.List;
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

	@PersistenceContext(unitName = "depdb")
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
			em.persist(position);
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
	@JsonView({DetailedFileHeaderView.class})
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
	@JsonView({DetailedFileHeaderView.class})
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

	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody getElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			ElectoralBody eb = (ElectoralBody) em.createQuery(
				"from ElectoralBody eb left join fetch eb.members " +
					"where eb.position=:position")
				.setParameter("position", position)
				.getSingleResult();

			return eb;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody addToElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		ElectoralBody eb;
		try {
			Position position = getAndCheckPosition(authToken, id);
			//TODO: Will obviously not be allowed after some point in the lifecycle
			try {
				eb = (ElectoralBody) em.createQuery(
					"from ElectoralBody eb " +
						"where eb.position=:position")
					.setParameter("position", position)
					.getSingleResult();
			} catch (NoResultException e) {
				eb = new ElectoralBody();
				eb.setPosition(position);
			}
			if (eb.getMembers().size() >= ElectoralBody.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}

			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			em.persist(eb);
			eb.addMember(existingProfessor);
			em.flush();
			return eb;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody removeFromElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			//TODO: Will obviously not be allowed after some point in the lifecycle
			ElectoralBody eb = (ElectoralBody) em.createQuery(
				"from ElectoralBody eb " +
					"where eb.position=:position")
				.setParameter("position", position)
				.getSingleResult();

			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}

			ElectoralBodyMembership removed = eb.removeMember(existingProfessor);
			if (removed == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.membership.id");
			}

			return eb;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBodyMembership getElectoralBodyMembership(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("professorId") long professorId) {
		ElectoralBody eb = getElectoralBody(authToken, id);
		for (ElectoralBodyMembership ebm : eb.getMembers()) {
			if (ebm.getProfessor().getId().equals(professorId)) {
				return ebm;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.membership.id");
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBodyMembership updateElectoralBodyMembership(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("professorId") long professorId, ElectoralBodyMembership ebm) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			ElectoralBodyMembership existingEbm = getElectoralBodyMembership(authToken, id, professorId);
			existingEbm.setConfirmedMembership(ebm.getConfirmedMembership());
			em.flush();
			return existingEbm;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader getElectoralBodyMembershipReport(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("professorId") long professorId) {
		ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
		FileHeader file = ebm.getRecommendatoryReport();
		if (file != null) {
			file.getBodies().size();
		}
		return file;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("professorId") long professorId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		try {
			ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
			FileHeader file = ebm.getRecommendatoryReport();
			//TODO: Security / lifecycle checks
			file = uploadFile(loggedOn, request, file);
			ebm.setRecommendatoryReport(file);
			em.persist(ebm);
			em.flush();
			return file;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}

	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@JsonView({DetailedFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("professorId") long professorId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		try {
			ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
			FileHeader file = ebm.getRecommendatoryReport();

			//TODO: Security / lifecycle checks

			Response retv = deleteFileBody(file);

			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				ebm.setRecommendatoryReport(null);
			}
			em.flush();

			return retv;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleRecommendatoryCommitteeView.class})
	public RecommendatoryCommittee getRecommendatoryCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			RecommendatoryCommittee rc = (RecommendatoryCommittee) em.createQuery(
				"from RecommendatoryCommittee rc left join fetch rc.members " +
					"where rc.position=:position")
				.setParameter("position", position)
				.getSingleResult();

			return rc;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleRecommendatoryCommitteeView.class})
	public RecommendatoryCommittee addToRecommendatoryCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			RecommendatoryCommittee rc;
			try {
				rc = (RecommendatoryCommittee) em.createQuery(
					"from RecommendatoryCommittee rc " +
						"where rc.position=:position")
					.setParameter("position", position)
					.getSingleResult();
			} catch (NoResultException e) {
				rc = new RecommendatoryCommittee();
				rc.setPosition(position);
			}
			if (rc.getMembers().size() >= RecommendatoryCommittee.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}
			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			em.persist(rc);
			rc.addMember(existingProfessor);
			em.flush();
			return rc;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleRecommendatoryCommitteeView.class})
	public RecommendatoryCommittee removeFromRecommendatoryCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		Position position = getAndCheckPosition(authToken, id);
		try {
			RecommendatoryCommittee rc;
			//TODO: Will obviously not be allowed after some point in the lifecycle
			rc = (RecommendatoryCommittee) em.createQuery(
				"from RecommendatoryCommittee rc " +
					"where rc.position=:position")
				.setParameter("position", position)
				.getSingleResult();

			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}

			RecommendatoryCommitteeMembership removed = rc.removeMember(existingProfessor);
			if (removed == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.membership.id");
			}

			return rc;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleRecommendatoryCommitteeView.class})
	public RecommendatoryCommitteeMembership getRecommendatoryCommitteeMembership(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("professorId") long professorId) {
		RecommendatoryCommittee rc = getRecommendatoryCommittee(authToken, id);
		for (RecommendatoryCommitteeMembership rcm : rc.getMembers()) {
			if (rcm.getProfessor().getId().equals(professorId))
				return rcm;
		}
		throw new RestException(Status.NOT_FOUND, "wrong.membership.id");
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/report")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader getRecommendatoryCommitteeReport(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		RecommendatoryCommittee rc = getRecommendatoryCommittee(authToken, id);
		FileHeader file = rc.getRecommendatoryReport();
		if (file != null) {
			file.getBodies().size();
		}
		return file;
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee/report")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		try {
			RecommendatoryCommittee rc = getRecommendatoryCommittee(authToken, id);
			FileHeader file = rc.getRecommendatoryReport();
			//TODO: Security / lifecycle checks
			file = uploadFile(loggedOn, request, file);
			rc.setRecommendatoryReport(file);
			em.persist(rc);
			em.flush();
			return file;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee/report")
	@JsonView({DetailedFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @Context HttpServletRequest request) throws FileUploadException, IOException {
		RecommendatoryCommittee rc = getRecommendatoryCommittee(authToken, id);
		try {
			FileHeader file = rc.getRecommendatoryReport();

			//TODO: Security / lifecycle checks

			Response retv = deleteFileBody(file);

			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				rc.setRecommendatoryReport(null);
			}
			em.flush();
			return retv;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

}
