package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Candidacy.SimpleCandidacyView;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.CandidateCommittee;
import gr.grnet.dep.service.model.CandidateCommittee.SimpleCandidateCommitteeView;
import gr.grnet.dep.service.model.CandidateCommitteeMembership;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.InstitutionFile;
import gr.grnet.dep.service.model.file.PositionFile;
import gr.grnet.dep.service.model.file.ProfessorFile;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
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
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate candidate = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", candidacy.getCandidate().getId())
				.getSingleResult();
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && candidate.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			// Initialize collections
			candidacy.initializeSnapshot();
			return candidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
	}

	/**
	 * Check that a (possible) Candidacy passes some basic checks before creation / update.
	 * 
	 * @param candidacy
	 * @param candidate
	 */
	private void validateCandidacy(Candidacy candidacy, Candidate candidate) {
		if (candidate.getFilesOfType(FileType.DIMOSIEYSI).size()==0)
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.dimosieysi");
		if (candidate.getFilesOfType(FileType.PTYXIO).size()==0)
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.ptyxio");
		if (candidate.getFilesOfType(FileType.BIOGRAFIKO).size()==0)
			throw new RestException(Status.CONFLICT, "validation.candidacy.no.cv");
		
	}
	
	/**
	 * Hold on to a snapshot of candidate's details in given candidacy.
	 * 
	 * @param candidacy
	 * @param candidate
	 */
	private void updateSnapshot(Candidacy candidacy, Candidate candidate) {
		candidacy.clearSnapshot();
		candidacy.updateSnapshot(candidate);
		User user = candidate.getUser();
		ProfessorDomestic professorDomestic = (ProfessorDomestic) user.getRole(RoleDiscriminator.PROFESSOR_DOMESTIC);
		ProfessorForeign professorForeign = (ProfessorForeign) user.getRole(RoleDiscriminator.PROFESSOR_FOREIGN);
		if (professorDomestic!=null)
			candidacy.updateSnapshot(professorDomestic);
		else if (professorForeign!=null)
			candidacy.updateSnapshot(professorForeign);
	}
	
	@POST
	@JsonView({SimpleCandidacyView.class})
	public Candidacy create(@HeaderParam(TOKEN_HEADER) String authToken, Candidacy candidacy) {
		try {
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", candidacy.getCandidate().getId())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}
			candidacy.setDate(new Date());
			
			validateCandidacy(candidacy, cy);
			updateSnapshot(candidacy, cy);

			em.persist(candidacy);
			return candidacy;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({SimpleCandidacyView.class})
	public Candidacy update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Candidacy candidacy) {
		try {
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", existingCandidacy.getCandidate().getId())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			validateCandidacy(existingCandidacy, cy);
			updateSnapshot(existingCandidacy, cy);
			return existingCandidacy;
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			Candidacy existingCandidacy = em.find(Candidacy.class, id);
			if (existingCandidacy == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
			}
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", existingCandidacy.getCandidate().getId())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);
			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			//Do Delete:
			//Since relationship isn't explicit, we need to do it manually.
			CandidateCommittee cc = getCandidateCommittee(existingCandidacy);
			em.remove(cc);
			em.remove(existingCandidacy);
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		} catch (PersistenceException e) {
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
	
	private CandidateCommittee getCandidateCommittee(Candidacy c) {
		return (CandidateCommittee) em.createQuery(
				"from CandidateCommittee cc left join fetch cc.members " +
					"where cc.candidacy=:candidacy")
				.setParameter("candidacy", c)
				.getSingleResult();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee getCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		try {
			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate().getId())
				.getSingleResult();

			User loggedOn = getLoggedOn(authToken);

			if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && cy.getUser().getId() != loggedOn.getId()) {
				throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
			}

			CandidateCommittee cc = getCandidateCommittee(c);

			em.flush();
			return cc;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee addToCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, CandidateCommitteeMembership ccm) {
		try {
			CandidateCommittee cc;
			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate().getId())
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
				em.persist(cc);
			}
			if (cc.getMembers().size() >= CandidateCommittee.MAX_MEMBERS) {
				throw new RestException(Status.CONFLICT, "max.members.exceeded");
			}
			for (CandidateCommitteeMembership ccmexisting: cc.getMembers()) {
				if (ccmexisting.getEmail().equalsIgnoreCase(ccm.getEmail()))
					throw new RestException(Status.CONFLICT, "error.candidacy.membership.email.already.exists");
			}

			cc.addMember(ccm);
			em.flush();
			return cc;

		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}/committee")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommittee removeFromCommittee(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, CandidateCommitteeMembership ccm) {
		try {
			CandidateCommittee cc;

			Candidacy c = (Candidacy) em.createQuery(
				"from Candidacy c where c.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			Candidate cy = (Candidate) em.createQuery(
				"from Candidate c where c.id=:id")
				.setParameter("id", c.getCandidate().getId())
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
			
			CandidateCommitteeMembership removed = cc.removeMember(ccm.getId());
			if (removed == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.candidate.committee.membership.id");
			}

			em.flush();
			return cc;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@GET
	@Path("/{id:[0-9][0-9]*}/committee/{committeeid}")
	@JsonView({SimpleCandidateCommitteeView.class})
	public CandidateCommitteeMembership getCommitteeMembership(@HeaderParam(TOKEN_HEADER) String authToken,
		@PathParam("id") long id, @PathParam("committeeid") long committeeId) {
		CandidateCommittee cm = getCommittee(authToken, id);
		for (CandidateCommitteeMembership ccm : cm.getMembers()) {
			if (ccm.getId().equals(committeeId)) {
				return ccm;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.candidate.committee.membership.id");
	}

	
	@GET
	@Path("/{id:[0-9]+}/ekthesi")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getEkthesi(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, id);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		
		return candidacy.getEkthesiAutoaksiologisis();
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/ekthesi/body/{bodyId:[0-9]+}")
	public Response getEkthesiBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, id);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		FileHeader ekthesi = candidacy.getEkthesiAutoaksiologisis();
		for (FileBody fb : ekthesi.getBodies()) {
			if (fb.getId().equals(bodyId)) {
				return sendFileBody(fb);
			}
		}
		// Default Action
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	
	@POST
	@Path("/{id:[0-9][0-9]*}/ekthesi")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createOrUpdateEkthesi(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long candidacyId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		
		try {
			FileHeader file = candidacy.getEkthesiAutoaksiologisis();
			if (file==null) { // Create
				file = new FileHeader();
				file.setType(FileType.EKTHESI_AUTOAKSIOLOGISIS);
				file.setOwner(loggedOn);
			}
			saveFile(loggedOn, fileItems, file);
			candidacy.setEkthesiAutoaksiologisis(file);
			em.flush();

			return toJSON(file, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	
	/**
	 * Deletes the last body of ekthesi file, if possible.
	 * 
	 * @param authToken
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{id:[0-9]+}/ekthesi")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteEkthesi(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long candidacyId) {
		User loggedOn = getLoggedOn(authToken);
		Candidacy candidacy = em.find(Candidacy.class, candidacyId);
		// Validate:
		if (candidacy == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.candidacy.id");
		}
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(candidacy.getCandidate().getUser().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			FileHeader ekthesi = candidacy.getEkthesiAutoaksiologisis();
			if (ekthesi == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			Response retv = deleteFileBody(ekthesi);
			if (retv.getStatus() == Status.NO_CONTENT.getStatusCode()) {
				// Remove from Position
				candidacy.setEkthesiAutoaksiologisis(null);
			}
			return retv;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}


}
