package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.CandidateCommittee;
import gr.grnet.dep.service.model.CandidateCommitteeMembership;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.ElectoralBody;
import gr.grnet.dep.service.model.CandidateCommittee.SimpleCandidateCommitteeView;
import gr.grnet.dep.service.model.ElectoralBody.SimpleElectoralBodyView;
import gr.grnet.dep.service.model.ElectoralBodyMembership;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.model.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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
import org.jboss.resteasy.spi.NoLogWebApplicationException;

@Path("/position")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PositionRESTService extends RESTService {

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@SuppressWarnings("unused")
	@Inject
	private Logger log;

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
		Department department = em.find(Department.class, position.getDepartment().getId());
		if (department==null) 
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).header(ERROR_CODE_HEADER, "department.not.found").build());
		position.setDepartment(department);

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
						&& !loggedOn.isDepartmentUser(department))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		Subject subject = em.find(Subject.class, position.getSubject().getId());
		if (subject==null) 
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).header(ERROR_CODE_HEADER, "subject.not.found").build());
		position.setSubject(subject);

		em.persist(position);
		return position;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedPositionView.class})
	public Position update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Position position) {
		Position existingPosition = em.find(Position.class, id);
		if (existingPosition == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Department department = existingPosition.getDepartment();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
						&& !loggedOn.isDepartmentUser(department))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		Subject subject = em.find(Subject.class, position.getSubject().getId());
		if (subject==null) 
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).header(ERROR_CODE_HEADER, "subject.not.found").build());
		existingPosition.setSubject(subject);
		existingPosition.setDeanStatus(position.getDeanStatus());
		existingPosition.setDescription(position.getDescription());
		existingPosition.setFekNumber(position.getFekNumber());
		existingPosition.setFekSentDate(position.getFekSentDate());
		existingPosition.setName(position.getName());
		existingPosition.setStatus(position.getStatus());
		
		position = em.merge(existingPosition);
		
		return position;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Position existingPosition = em.find(Position.class, id);
		if (existingPosition == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Department department = existingPosition.getDepartment();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
						&& !loggedOn.isDepartmentUser(department))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);

		//Do Delete:
		em.remove(existingPosition);
	}
	
	@GET
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fek}")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var) 
	{
		FileHeader file = null;
		getLoggedOn(authToken);

		Position position = em.find(Position.class, id);
		// Validate:
		if (position == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}

		if ("prosklisiKosmitora".equals(var)) {
			file = position.getProsklisiKosmitora();
		} else if ("recommendatoryReport".equals(var)) {
			file = position.getRecommendatoryReport();
		} else if ("recommendatoryReportSecond".equals(var)) {
			file = position.getRecommendatoryReport();
		} else if ("fek".equals(var)) {
			file = position.getFek();
		}

		if (file!=null) file.getBodies().size();
		return file;
	}

	
	
	@POST
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fek}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("var") String var, @Context HttpServletRequest request) throws FileUploadException, IOException
	{
		FileHeader file = null;
		Position position = em.find(Position.class, id);
		if (position == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Department department = position.getDepartment();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
						&& !loggedOn.isDepartmentUser(department))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);


		if ("prosklisiKosmitora".equals(var)) {
			file = uploadFile(loggedOn, request, position.getProsklisiKosmitora());
			position.setProsklisiKosmitora(file);
		} else if ("recommendatoryReport".equals(var)) {
			file = uploadFile(loggedOn, request, position.getRecommendatoryReport());
			position.setRecommendatoryReport(file);
		} else if ("recommendatoryReportSecond".equals(var)) {
			file = uploadFile(loggedOn, request, position.getRecommendatoryReportSecond());
			position.setRecommendatoryReportSecond(file);
		} else if ("fek".equals(var)) {
			file = uploadFile(loggedOn, request, position.getFek());
			position.setFek(file);
		}

		return file;
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
	@Path("/{id:[0-9][0-9]*}/{var:prosklisiKosmitora|recommendatoryReport|recommendatoryReportSecond|fek}")
	@JsonView({DetailedFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("var") String var) {
		Position position = em.find(Position.class, id);
		if (position == null) {
			throw new NoLogWebApplicationException(Status.NOT_FOUND);
		}
		Department department = position.getDepartment();

		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
						&& !loggedOn.isDepartmentUser(department))
			throw new NoLogWebApplicationException(Status.FORBIDDEN);
		
		FileHeader file = getFile(authToken, id, var);
		
		Response retv = deleteFileBody(file);
		
		if (retv.getStatus()==Status.NO_CONTENT.getStatusCode()) {
			// Break the relationship as well
			if ("prosklisiKosmitora".equals(var)) {
				position.setProsklisiKosmitora(null);
			} else if ("recommendatoryReport".equals(var)) {
				position.setRecommendatoryReport(null);
			} else if ("recommendatoryReportSecond".equals(var)) {
				position.setRecommendatoryReportSecond(null);
			} else if ("fek".equals(var)) {
				position.setFek(null);
			}
		}
		return retv;
	}
	
	
	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody getElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		Position p = (Position) em.createQuery(
			"from Position p where p.id=:id")
			.setParameter("id", id)
			.getSingleResult();
		Department department = p.getDepartment();
		
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& !loggedOn.isDepartmentUser(department))
				throw new NoLogWebApplicationException(Status.FORBIDDEN);
		
		ElectoralBody eb = (ElectoralBody) em.createQuery(
				"from ElectoralBody eb left join fetch eb.members " +
					"where eb.position=:position")
				.setParameter("position", p)
				.getSingleResult();

		return eb;
	}

	
	@POST
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody addToElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id,
				Professor p) {
		ElectoralBody eb;
		
		Position position = (Position) em.createQuery(
					"from Position p where p.id=:id")
					.setParameter("id", id)
					.getSingleResult();
		Department department = position.getDepartment();
		
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& !loggedOn.isDepartmentUser(department))
				throw new NoLogWebApplicationException(Status.FORBIDDEN);
	
		//TODO: Will obviously not be allowed after some point in the lifecycle
		
		try {
			eb = (ElectoralBody) em.createQuery(
				"from ElectoralBody eb " +
					"where eb.position=:position")
				.setParameter("position", position)
				.getSingleResult();
		} catch (NoResultException e) {
			eb= new ElectoralBody();
			eb.setPosition(position);
		}
		if (eb.getMembers().size()>=ElectoralBody.MAX_MEMBERS) {
			throw new NoLogWebApplicationException(Response.status(Status.CONFLICT).
						header(ERROR_CODE_HEADER, "max.members.exceeded").build());
		}
		
		Professor existingProfessor = em.find(Professor.class, p.getId());
		if (existingProfessor==null) {
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).
						header(ERROR_CODE_HEADER, "professor.not.found").build());
		}
		eb.addMember(existingProfessor);
		em.persist(eb);
		em.flush();
		
		return eb;
	}

	
	@DELETE
	@Path("/{id:[0-9][0-9]*}/electoralbody")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBody removeFromElectoralBody(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id,
				Professor p) {
		ElectoralBody eb;
		
		Position position = (Position) em.createQuery(
					"from Position p where p.id=:id")
					.setParameter("id", id)
					.getSingleResult();
		Department department = position.getDepartment();
		
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& !loggedOn.isDepartmentUser(department))
				throw new NoLogWebApplicationException(Status.FORBIDDEN);
	
		//TODO: Will obviously not be allowed after some point in the lifecycle
		
		//TODO: Will obviously not be allowed after some point in the lifecycle
		
		eb = (ElectoralBody) em.createQuery(
					"from ElectoralBody eb " +
						"where eb.position=:position")
					.setParameter("position", position)
					.getSingleResult();
		
		Professor existingProfessor = em.find(Professor.class, p.getId());
		if (existingProfessor==null) {
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).
						header(ERROR_CODE_HEADER, "professor.not.found").build());
		}
			
		ElectoralBodyMembership removed = eb.removeMember(existingProfessor);
		if (removed==null) {
			throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).
						header(ERROR_CODE_HEADER, "membership.not.found").build());
		}
		
		return eb;
	}
	
	
	
	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBodyMembership getElectoralBodyMembership(@HeaderParam(TOKEN_HEADER) String authToken,
					@PathParam("id") long id, @PathParam("professorId") long professorId) {
		ElectoralBody eb = getElectoralBody(authToken, id);
		for (ElectoralBodyMembership ebm: eb.getMembers()) {
			if (ebm.getProfessor().getId().equals(professorId)) return ebm;
		}
		throw new NoLogWebApplicationException(Response.status(Status.NOT_FOUND).
					header(ERROR_CODE_HEADER, "membership.not.found").build());
	}
	
	
	@PUT
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleElectoralBodyView.class})
	public ElectoralBodyMembership updateElectoralBodyMembership(@HeaderParam(TOKEN_HEADER) String authToken,
					@PathParam("id") long id, @PathParam("professorId") long professorId, ElectoralBodyMembership ebm) {
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR))
				throw new NoLogWebApplicationException(Status.FORBIDDEN);
		
		ElectoralBodyMembership existingEbm = getElectoralBodyMembership(authToken, id, professorId);
		existingEbm.setConfirmedMembership(ebm.getConfirmedMembership());
		
		return existingEbm;
	}
	
	
	@GET
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader getElectoralBodyMembershipReport(@HeaderParam(TOKEN_HEADER) String authToken,
					@PathParam("id") long id, @PathParam("professorId") long professorId) {
		ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
		FileHeader file = ebm.getRecommendatoryReport();
		if (file!=null) file.getBodies().size();
		return file;
	}	
	
	
	@POST
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader postFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("professorId") long professorId,
				@Context HttpServletRequest request) throws FileUploadException, IOException
	{
		User loggedOn = getLoggedOn(authToken);
		ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
		FileHeader file = ebm.getRecommendatoryReport();
		
		//TODO: Security / lifecycle checks

		file = uploadFile(loggedOn, request, file);
		ebm.setRecommendatoryReport(file);
		em.persist(ebm);

		return file;
	}	
	
	
	
	@DELETE
	@Path("/{id:[0-9][0-9]*}/electoralbody/{professorId:[0-9][0-9]*}/report")
	@JsonView({DetailedFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("professorId") long professorId,
				@Context HttpServletRequest request) throws FileUploadException, IOException
	{
		ElectoralBodyMembership ebm = getElectoralBodyMembership(authToken, id, professorId);
		FileHeader file = ebm.getRecommendatoryReport();
		
		//TODO: Security / lifecycle checks

		Response retv = deleteFileBody(file);
		
		if (retv.getStatus()==Status.NO_CONTENT.getStatusCode()) 
			ebm.setRecommendatoryReport(null);
		
		return retv;
	}	
	
}
