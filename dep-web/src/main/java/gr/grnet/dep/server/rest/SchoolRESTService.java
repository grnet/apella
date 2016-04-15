package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.SchoolService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.School;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/school")
public class SchoolRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private SchoolService schoolService;

    /**
     * Returns all Schools
     *
     * @return
     */

    @GET
    public Collection<School> getAll() {
        // get all schools
        List<School> schools = schoolService.getAll();

        return schools;
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
            // find school
            School school = schoolService.get(id);
            return school;
        } catch (NotFoundException e) {
            throw new RestException(Response.Status.NOT_FOUND, "wrong.school.id");
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public School update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, School schoolToUpdate) {
        try {
            User loggedOn = getLoggedOn(authToken);

            // update school
            School school = schoolService.update(id, schoolToUpdate, loggedOn);
            return school;

        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}/departments")
    public Collection<Department> getDepartments(@PathParam("id") long id) {
        // get departments
        List<Department> departments = schoolService.getDepartments(id);

        return departments;
    }
}
