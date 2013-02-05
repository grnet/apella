package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

@Path("/institutionrf")
@Stateless
public class InstitutionRegulatoryFrameworkRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@SuppressWarnings("unchecked")
	public Collection<InstitutionRegulatoryFramework> getAll() {
		return (List<InstitutionRegulatoryFramework>) em.createQuery(
			"select i from InstitutionRegulatoryFramework i " +
				"where i.permanent = true")
			.getResultList();
	}

	@GET
	@Path("/{id:[0-9]+}")
	public InstitutionRegulatoryFramework get(@PathParam("id") long id) {
		try {
			return (InstitutionRegulatoryFramework) em.createQuery(
				"select i from InstitutionRegulatoryFramework i " +
					"where i.id = :id")
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.institutionregulatoryframework.id");
		}
	}

	@POST
	public InstitutionRegulatoryFramework create(@HeaderParam(TOKEN_HEADER) String authToken, InstitutionRegulatoryFramework newIRF) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, newIRF.getInstitution().getId());
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Validate
		try {
			em.createQuery("select irf from InstitutionRegulatoryFramework irf " +
				"where irf.institution.id = :institutionId " +
				"and irf.permanent = true ")
				.setParameter("institutionId", institution.getId())
				.getSingleResult();
			throw new RestException(Status.CONFLICT, "institutionrf.already.exists");
		} catch (NoResultException ne) {
		}
		// Create
		try {
			newIRF.setInstitution(institution);
			newIRF.setCreatedAt(new Date());
			newIRF.setUpdatedAt(new Date());
			newIRF.setPermanent(false);
			newIRF = em.merge(newIRF);
			em.flush();
			return newIRF;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9]+}")
	public InstitutionRegulatoryFramework update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id, InstitutionRegulatoryFramework newIRF) {
		User loggedOn = getLoggedOn(authToken);
		InstitutionRegulatoryFramework existing = em.find(InstitutionRegulatoryFramework.class, id);
		if (existing == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institutionregulatoryframework.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isInstitutionUser(existing.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			existing.setEswterikosKanonismosURL(newIRF.getEswterikosKanonismosURL());
			existing.setOrganismosURL(newIRF.getOrganismosURL());
			existing.setUpdatedAt(new Date());
			existing.setPermanent(true);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
		return existing;
	}

	@DELETE
	@Path("/{id:[0-9]+}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
		User loggedOn = getLoggedOn(authToken);
		InstitutionRegulatoryFramework existing = em.find(InstitutionRegulatoryFramework.class, id);
		if (existing == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institutionregulatoryframework.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
			!loggedOn.isInstitutionUser(existing.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			em.remove(existing);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
