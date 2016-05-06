package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.SectorService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.User;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sector")
public class SectorRESTService extends RESTService {

    @EJB
    private SectorService sectorService;

    /**
     * Returns all sectors
     *
     * @return
     */
    @GET
    public List<Sector> getAll() {
        // retrieve all sectors
        List<Sector> sectors = sectorService.getAll();

        return sectors;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Sector get(@PathParam("id") long id) {
        try {
            Sector sector = sectorService.get(id);

            return sector;
        } catch (NotFoundException e) {
            throw new RestException(Response.Status.NOT_FOUND, e.getMessage());
        }
    }

    @POST
    public Sector create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Sector sector) {

        User loggedOn = getLoggedOn(authToken);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
        }

        try {
            // create sector
            sector = sectorService.create(sector);

            return sector;
        } catch (ValidationException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
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
            // update
            Sector sector = sectorService.update(id, sectorToUpdate);

            return sector;
        } catch (NotFoundException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
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

            // delete
            sectorService.delete(id);

            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
        }
    }


}
