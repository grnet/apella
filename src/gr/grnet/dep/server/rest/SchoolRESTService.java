package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/school")
@Stateless
public class SchoolRESTService extends RESTService {

    @Inject
    private Logger log;

    /**
     * Returns all Schools
     *
     * @return
     */

    @GET
    @SuppressWarnings("unchecked")
    public Collection<School> getAll() {
        return em.createQuery(
                "select distinct i from School i ", School.class)
                .getResultList();
    }

    /**
     * Returns School with given ID
     *
     * @param id
     * @return
     */

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public School get(@PathParam("id") long id) {
        try {
            return em.createQuery(
                    "select i from School i " +
                            "where i.id = :id", School.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new RestException(Response.Status.NOT_FOUND, "wrong.school.id");
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public School update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, School schoolToUpdate) {

        User loggedOn = getLoggedOn(authToken);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
        }

        try {
            School school = em.find(School.class, id);
            if (school == null) {
                throw new RestException(Response.Status.NOT_FOUND, "wrong.school.id");
            }

            if (schoolToUpdate.getName() == null ||
                    StringUtils.isEmpty(schoolToUpdate.getName().get("el")) || StringUtils.isEmpty(schoolToUpdate.getName().get("en"))) {
                throw new RestException(Response.Status.CONFLICT, "names.missing");
            }

            school.setName(schoolToUpdate.getName());

            return school;

        } catch (PersistenceException e) {
            log.log(Level.WARNING, e.getMessage(), e);
            sc.setRollbackOnly();
            throw new RestException(Response.Status.BAD_REQUEST, "persistence.exception");
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}/departments")
    public Collection<Department> getDepartments(@PathParam("id") long id) {

        return em.createQuery(
                "Select d from Department d " +
                        "where d.school.id = :id", Department.class)
                .setParameter("id", id)
                .getResultList();
    }
}
