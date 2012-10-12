package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/position/committee")
@Stateless
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
		User loggedOn = getLoggedOn(authToken);
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isDepartmentUser(position.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}

		return position;
	}

	@GET
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public List<PositionCommitteeMember> getPositionCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @QueryParam("position") Long positionId) {
		Position position = getAndCheckPosition(authToken, positionId);
		position.getCommitee().size();
		return position.getCommitee();
	}

	@GET
	@Path("/{cmId:[0-9][0-9]*}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("cmId") Long cmId) {
		PositionCommitteeMember result = em.find(PositionCommitteeMember.class, cmId);
		if (result == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		return result;
	}

	@POST
	@JsonView({DetailedPositionCommitteeMemberView.class})
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

			//TODO: removePositionCommiteeMember: Will obviously not be allowed after some point in the lifecycle

			// Remove
			existingCM.getPosition().getCommitee().remove(existingCM);
			em.remove(existingCM);
			em.flush();
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

}
