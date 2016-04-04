package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

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
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/sector")
@Stateless
public class SectorRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all sectors
	 *
	 * @return
	 */
	@GET
	public Collection<Sector> getAll() {
		return em.createQuery("from Sector s " +
				"left join fetch s.name sname " +
				"order by s.areaId, s.subjectId", Sector.class).getResultList();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Sector get(@PathParam("id") long id) {

		Sector sector = em.find(Sector.class, id);

		if (sector == null) {
			throw new RestException(Response.Status.NOT_FOUND, "wrong.sector.id");
		}

		return sector;
	}

	@POST
	public Sector create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Sector sector) {

		User loggedOn = getLoggedOn(authToken);

		if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
		}

		try {
			// validate
			validate(sector);

			// get the max results from the respective fields in order to create the new sectors
			Map<String, Long> map = em.createQuery("Select new MAP ( MAX(s.id) as sector_id, MAX(s.subjectId) as subject_id) " +
					"from Sector s", Map.class)
					.getSingleResult();

			Long maxId = map.get("sector_id");
			Long maxSubjectId = map.get("subject_id");

			sector.setId(maxId + 1);
			sector.setSubjectId(maxSubjectId + 1);

			em.persist(sector);
			em.flush();

			return sector;

		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	public Sector update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Sector sectorToUpdate) {

		User loggedOn = getLoggedOn(authToken);

		if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
		}

		try {
			Sector sector = em.find(Sector.class, id);

			if (sector == null) {
				throw new RestException(Response.Status.NOT_FOUND, "wrong.sector.id");
			}

			boolean isSectorLinkedToPosition = isSectorLinkedToPosition(sector.getId());

			if (isSectorLinkedToPosition) {
				throw new RestException(Response.Status.CONFLICT, "edit.sector.assigned.to.position");
			}

			boolean isSectorLinkedToPositionSearchCriteria = isSectorLinkedToPositionSearchCriteria(sector.getId());

			if (isSectorLinkedToPositionSearchCriteria) {
				throw new RestException(Response.Status.CONFLICT, "edit.sector.assigned.to.position");
			}

			//validate
			validate(sectorToUpdate);

			sector.setName(sectorToUpdate.getName());
			sector.setAreaId(sectorToUpdate.getAreaId());
			sector.setSubjectId(sectorToUpdate.getSubjectId());

			return sector;

		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
		}

	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {

		try {

			User loggedOn = getLoggedOn(authToken);

			if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
				throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
			}

			Sector sector = em.find(Sector.class, id);

			if (sector == null) {
				throw new RestException(Response.Status.NOT_FOUND, "wrong.sector.id");
			}

			boolean isSectorLinkedToPosition = isSectorLinkedToPosition(sector.getId());

			if (isSectorLinkedToPosition) {
				throw new RestException(Response.Status.CONFLICT, "delete.sector.assigned.to.position");
			}

			boolean isSectorLinkedToPositionSearchCriteria = isSectorLinkedToPositionSearchCriteria(sector.getId());

			if (isSectorLinkedToPositionSearchCriteria) {
				throw new RestException(Response.Status.CONFLICT, "delete.sector.assigned.to.position");
			}

			em.remove(sector);
			em.flush();

			return Response.noContent().build();

		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
		}

	}

	private void validate(Sector sector) {

		String[] locales = { "el", "en" };

		boolean validationPassed = sector.getName() != null && sector.getName().get(locales[0]) != null && sector.getName().get(locales[1]) != null;

		if (validationPassed) {
			// check inside properties
			for (String locale : locales) {
				validationPassed = StringUtils.isNotEmpty(sector.getName().get(locale).getArea()) && StringUtils.isNotEmpty(sector.getName().get(locale).getSubject());
				if (!validationPassed) {
					break;
				}
			}
		}

		if (!validationPassed) {
			throw new RestException(Response.Status.CONFLICT, "names.missing");
		}
	}

	private boolean isSectorLinkedToPosition(Long sectorId) {
		try {
			em.createQuery("Select p.id from Position p " +
					"where p.sector.id = :sectorId")
					.setParameter("sectorId", sectorId)
					.setMaxResults(1)
					.getSingleResult();

			return true;

		} catch (NoResultException e) {
			return false;
		}
	}

	private boolean isSectorLinkedToPositionSearchCriteria(Long sectorId) {
		try {
			em.createQuery("Select distinct u.id " +
					"from PositionSearchCriteria u join u.sectors s " +
					"where s.id = :sectorId ", Long.class)
					.setParameter("sectorId", sectorId)
					.setMaxResults(1)
					.getSingleResult();

			return true;

		} catch (NoResultException e) {
			return false;
		}
	}

}
