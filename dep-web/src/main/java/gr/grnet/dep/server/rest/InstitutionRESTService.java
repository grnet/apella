package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.School;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.logging.Logger;

@Path("/institution")
@Stateless
public class InstitutionRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all Institutions
	 *
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Institution> getAll() {
		return em.createQuery(
				"select distinct i from Institution i ", Institution.class)
				.getResultList();
	}

	/**
	 * Returns Institution with given ID
	 *
	 * @param id
	 * @return
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Institution get(@PathParam("id") long id) {
		try {
			return em.createQuery(
					"select i from Institution i " +
							"where i.id = :id", Institution.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
	}

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Institution update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Institution institutionToUpdate) {

        User loggedOn = getLoggedOn(authToken);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }

        try {

            Institution institution = em.find(Institution.class, id);
            
            if (institution == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
            }

            if (institutionToUpdate.getName() == null ||
                    StringUtils.isEmpty(institutionToUpdate.getName().get("el")) || StringUtils.isEmpty(institutionToUpdate.getName().get("en"))) {
                throw new RestException(Status.CONFLICT, "names.missing");
            }

            institution.setName(institutionToUpdate.getName());

            return institution;

        } catch (PersistenceException e) {
            sc.setRollbackOnly();
            throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}/schools")
    public Collection<School> getSchools(@PathParam("id") long id) {

        return em.createQuery(
                "select distinct s from School s " +
                        "where s.institution.id = :id", School.class)
                .setParameter("id", id)
                .getResultList();

    }
}
