package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.RegisterService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.Register.RegisterView;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/register")
public class RegisterRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private RegisterService registerService;

    /**
     * Returns all registers
     *
     * @param authToken
     * @return
     */
    @GET
    @JsonView({RegisterView.class})
    public SearchData<Register> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get all
            SearchData<Register> result = registerService.getAll(request, loggedOn);

            return result;
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns register with given ID
     * without the lazy collections!
     *
     * @param authToken
     * @param id
     * @return
     * @HTTP 404 X-Error-Code: wrong.register.id
     */
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({DetailedRegisterView.class})
    public Register get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            // get register
            Register register = registerService.get(id);

            return register;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }

    }

    /**
     * Creates a new Register, non-finalized
     *
     * @param authToken
     * @param newRegister
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.department.id
     * @HTTP 409 X-Error-Code: register.already.exists
     */
    @POST
    @JsonView({DetailedRegisterView.class})
    public Register create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Register newRegister) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create
            Register register = registerService.create(newRegister, loggedOn);

            return register;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Updates and finalizes the Register with the given ID
     *
     * @param authToken
     * @param id
     * @param register
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.register.id
     * @HTTP 404 X-Error-Code: wrong.professor.id
     * @HTTP 409 X-Error-Code: register.institution.change
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({DetailedRegisterView.class})
    public Register update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Register register) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            register = registerService.update(id, register, loggedOn);

            return register;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Removes the Register with the given ID
     *
     * @param authToken
     * @param id
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.register.id
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete
            registerService.delete(id, loggedOn);

        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /***********************
     * Members Functions ***
     ***********************/

    /**
     * Returns the list of Register Members of this Register
     *
     * @param authToken
     * @param registerId
     * @return
     * @HTTP 404 X-Error-Code: wrong.register.id
     */
    @GET
    @Path("/{id:[0-9]+}/members")
    @JsonView({DetailedRegisterView.class})
    public SearchData<RegisterMember> getRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @Context HttpServletRequest request) {
        try {
            // get the register members
            SearchData<RegisterMember> result = registerService.searchRegisterMembers(registerId, request);
            return result;
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns the specific member of this register with the given ID
     *
     * @param authToken
     * @param registerId
     * @param memberId
     * @return
     * @HTTP 404 X-Error-Code: wrong.register.id
     * @HTTP 404 X-Error-Code: wrong.register.member.id
     */
    @GET
    @Path("/{id:[0-9]+}/members/{memberId:[0-9]+}")
    @JsonView({DetailedRegisterMemberView.class})
    public RegisterMember getRegisterMember(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("memberId") Long memberId) {
        try {
            RegisterMember registerMember = registerService.getRegisterMember(registerId, memberId);
            return registerMember;
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @GET
    @Path("/{id:[0-9]+}/professor")
    @JsonView({DetailedRegisterMemberView.class})
    public Collection<Role> getRegisterProfessors(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @QueryParam("prof[]") List<Long> roleIds) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get professors
            Collection<Role> professors = registerService.getRegisterProfessors(registerId, roleIds, loggedOn);

            return professors;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns a paginated list of professors that can be added in this register
     *
     * @param authToken
     * @param registerId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.register.id
     */
    @POST
    @Path("/{id:[0-9]+}/professor")
    @JsonView({DetailedRegisterMemberView.class})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public SearchData<Role> searchRegisterProfessors(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // search
            SearchData<Role> result = registerService.searchRegisterProfessors(registerId, request, loggedOn);
            return result;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @GET
    @Path("/professorsexport")
    public Response getProfessorsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
        try {
            User loggedOn = getLoggedOn(authToken);
            InputStream is = registerService.getProfessorsExport(loggedOn);
            String filename = "generic_register.xlsx";

            // Return response
            return Response.ok(is)
                    .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("charset", "UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"")
                    .build();
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "getDocument", e);
            throw new EJBException(e);
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @GET
    @Path("/{id:[0-9]+}/export")
    public Response getRegisterExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // generate document
            InputStream is = registerService.getRegisterExport(registerId, loggedOn);

            // get Register for filling the file name
            Register register = registerService.getById(registerId);

            String filename = "register_" +
                    (register.getInstitution().getSchacHomeOrganization() == null ? register.getInstitution().getId() : register.getInstitution().getSchacHomeOrganization()) +
                    ".xlsx";

            // Return response
            return Response.ok(is)
                    .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("charset", "UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"")
                    .build();
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "getDocument", e);
            throw new EJBException(e);
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }
}
