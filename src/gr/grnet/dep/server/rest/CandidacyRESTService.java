package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.CandidateCommittee;
import gr.grnet.dep.service.model.CandidateCommittee.SimpleCandidateCommitteeView;
import gr.grnet.dep.service.model.CandidateCommitteeMembership;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.annotate.JsonView;

@Path("/candidacy")
@Stateless
public class CandidacyRESTService extends RESTService {

	@PersistenceContext(unitName = "apelladb")
	private EntityManager em;

	@Inject
	private Logger log;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		try {
			Candidacy candidacy = (Candidacy) em.createQuery(
				"from Candidacy c left join fetch c.files " +
					"where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate candidate = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", candidacy.getCandidate())
				.getSingleResult();
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			return candidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
	}

	@POST
	@JsonView({DetailedCandidacyView.class})
	public Candidacy create(@HeaderParam(TOKEN_HEADER) String authToken, Candidacy candidacy) {
		try {
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", candidacy.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			candidacy.setDate(new Date());

			em.persist(candidacy);
			return candidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedCandidacyView.class})
	public Candidacy update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		try {
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.id");
			}
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", existingCandidacy.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			// So far there are no fields to update!

			return existingCandidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.id");
			}
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", existingCandidacy.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			//Do Delete:
			em.remove(existingCandidacy);
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee getCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c left join fetch c.files " +
					"where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);

			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			CandidateCommittee cc = (CandidateCommittee) em.createQuery(
				"from CandidateCommittee cc left join fetch cc.members " +
					"where cc.candidacy=:candidacy")
				.setParameter("candidacy", c)
				.getSingleResult();

			em.flush();
			return cc;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee addToCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		try {
			CandidateCommittee cc;
			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c left join fetch c.files " +
					"where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			boolean ok = false;
			if (loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) || cy.getUser().getId() == loggedOn.getId()) {
				ok = true;
			}
			if (!ok) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			//TODO: Will obviously not be allowed after some point in the lifecycle

			try {
				cc = (CandidateCommittee) em.createQuery(
					"from CandidateCommittee cc " +
						"where cc.candidacy=:candidacy")
					.setParameter("candidacy", c)
					.getSingleResult();
			} catch (NoResultException e) {
				cc = new CandidateCommittee();
				cc.setCandidacy(c);
			}
			if (cc.getMembers().size() >= CandidateCommittee.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}

			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			em.persist(cc);
			cc.addMember(existingProfessor);

			em.flush();
			return cc;

		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee removeFromCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Professor p) {
		try {
			CandidateCommittee cc;

			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c left join fetch c.files " +
					"where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			boolean ok = false;
			if (loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) || cy.getUser().getId() == loggedOn.getId()) {
				ok = true;
			}
			if (!ok) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			//TODO: Will obviously not be allowed after some point in the lifecycle

			cc = (CandidateCommittee) em.createQuery(
				"from CandidateCommittee cc " +
					"where cc.candidacy=:candidacy")
				.setParameter("candidacy", c)
				.getSingleResult();

			Professor existingProfessor = em.find(Professor.class, p.getId());
			if (existingProfessor == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
			}
			CandidateCommitteeMembership removed = cc.removeMember(existingProfessor);
			if (removed == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.membership.id");
			}

			em.flush();
			return cc;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "cannot.persist");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{professorId:[0-9][0-9]*}")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommitteeMembership getCommitteeMembership(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("professorId") long professorId) {
		CandidateCommittee cm = getCommittee(authToken, id);
		for (CandidateCommitteeMembership ccm : cm.getMembers()) {
			if (ccm.getProfessor().getId().equals(professorId)) {
				return ccm;
			}
		}
		throw new RestException(Status.NOT_FOUND, "membership.not.found");
	}

}
