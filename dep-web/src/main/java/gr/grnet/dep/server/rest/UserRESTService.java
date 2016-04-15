package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.UserService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.SearchData;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.User.UserView;
import gr.grnet.dep.service.model.User.UserWithLoginDataView;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/user")
public class UserRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private UserService userService;

    @GET
    @JsonView({UserView.class})
    public Collection<User> getAssistants(
            @HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken,
            @QueryParam("user") Long userId,
            @QueryParam("username") String username,
            @QueryParam("firstname") String firstname,
            @QueryParam("lastname") String lastname,
            @QueryParam("mobile") String mobile,
            @QueryParam("email") String email,
            @QueryParam("status") String status,
            @QueryParam("role") String role,
            @QueryParam("roleStatus") String roleStatus,
            @QueryParam("im") Long imId,
            @QueryParam("mm") Long mmId) {

        // Used in showAdministratorsView, showInstitutionAssistantsView, showMinistryAssistantsView
        User loggedOn = getLoggedOn(authToken);
        // Authorize
        if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
                !loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
                !loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }
        switch (loggedOn.getPrimaryRole()) {
            case ADMINISTRATOR:
                break;
            case MINISTRY_MANAGER:
                if (!loggedOn.getActiveRole(RoleDiscriminator.MINISTRY_MANAGER).getId().equals(mmId)) {
                    throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                }
                ;
                break;
            case INSTITUTION_MANAGER:
                if (!loggedOn.getActiveRole(RoleDiscriminator.INSTITUTION_MANAGER).getId().equals(imId)) {
                    throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                }
                ;
                break;
            default:
                // Will Not happen
                throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }

        // get assistants
        List<User> result = userService.getAssistants(userId, username, firstname, lastname, mobile, email, status, role, roleStatus, imId, mmId);

        // Return result
        return result;
    }

    @GET
    @Path("/loggedon")
    @JsonView({UserWithLoginDataView.class})
    public Response getLoggedOn(@Context HttpServletRequest request) {
        String authToken = request.getHeader(WebConstants.AUTHENTICATION_TOKEN_HEADER);
        if (authToken == null && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("_dep_a")) {
                    authToken = c.getValue();
                    break;
                }
            }
        }
        User u = super.getLoggedOn(authToken);
        return Response.status(200)
                .header(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken())
                .cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
                .entity(u)
                .build();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public String get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) throws RestException {
        // This is used mainly in user management, not in candidacies or registers
        User loggedOn = getLoggedOn(authToken);
        try {
            // get user
            User u = userService.get(id);

            if (!loggedOn.getId().equals(id) &&
                    !loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
                    !loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER)) {
                // Check if other user can view this one
                switch (u.getPrimaryRole()) {
                    case ADMINISTRATOR:
                        // Only other admins:
                        throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                    case MINISTRY_MANAGER:
                    case MINISTRY_ASSISTANT:
                        // Other ministry managers:
                        throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                    case INSTITUTION_MANAGER:
                    case INSTITUTION_ASSISTANT:
                        // Other institution managers:
                        if (!loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
                            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                        }
                        break;
                    case CANDIDATE:
                    case PROFESSOR_DOMESTIC:
                    case PROFESSOR_FOREIGN:
                        if (loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT)) {
                            // Do Nothing, allow
                        } else if (loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER) ||
                                loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_ASSISTANT)) {
                            // Check if verified users:
                            if (u.getStatus().equals(UserStatus.UNVERIFIED) ||
                                    u.getRole(u.getPrimaryRole()).getStatus().equals(RoleStatus.UNAPPROVED)) {
                                throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                            }
                        }
                        break;
                }
            }

            if (u.getId().equals(loggedOn.getId())) {
                return toJSON(u, UserWithLoginDataView.class);
            } else {
                return toJSON(u, UserView.class);
            }
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        }
    }

    @POST
    @JsonView({UserWithLoginDataView.class})
    public User create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, User newUser) {
        try {
            // create user
            User user = userService.create(authToken, newUser);

            return user;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.CONFLICT, e.getMessage());
        } catch (ServiceException e) {
            throw new RestException(Status.BAD_REQUEST, "service.exception");
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({UserWithLoginDataView.class})
    public User update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, User user) {
        try {
            // get logged on user
            User loggedOn = getLoggedOn(authToken);
            // update user
            user = userService.update(id, user, loggedOn);

            return user;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete user
            userService.delete(id, loggedOn);

        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @JsonView({UserWithLoginDataView.class})
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            User u = authenticationService.doUsernameLogin(username, password);
            return Response.status(200)
                    .header(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken())
                    .cookie(new NewCookie("_dep_a", u.getAuthToken(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false))
                    .entity(u)
                    .build();
        } catch (ServiceException e) {
            throw new RestException(Status.UNAUTHORIZED, e.getErrorKey());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/logout")
    public Response logout(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
        try {
            authenticationService.logout(authToken);
            return Response.noContent().build();
        } catch (ServiceException e) {
            throw new RestException(Status.NOT_FOUND, e.getErrorKey());
        }

    }

    @PUT
    @Path("/resetPassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response resetPassword(@FormParam("email") String email) {
        try {
            userService.resetPassword(email);
            // Return Result
            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        }
    }

    @PUT
    @Path("/sendVerificationEmail")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendVerificationEmail(@FormParam("email") String email) {
        try {
            // send verification mail
            userService.sendVerificationEmail(email);
            // Return Result
            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}/sendLoginEmail")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendLoginEmail(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @FormParam("createLoginLink") Boolean createLoginLink) {
        User loggedOn = getLoggedOn(authToken);
        try {
            // send login mail
            userService.sendLoginEmail(id, createLoginLink, loggedOn);
            // Return Result
            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }


    @PUT
    @Path("/{id:[0-9][0-9]*}/sendReminderLoginEmail")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendReminderLoginEmail(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @FormParam("createLoginLink") Boolean createLoginLink) {
        User loggedOn = getLoggedOn(authToken);

        try {
            // send reminder login mail
            userService.sendReminderLoginEmail(id, loggedOn);

            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}/sendEvaluationEmail")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendEvaluationEmail(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        User loggedOn = getLoggedOn(authToken);

        try {
            // send evaluation mail
            userService.sendEvaluationEmail(id, loggedOn);

            return Response.noContent().build();
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/verify")
    @JsonView({UserWithLoginDataView.class})
    public User verify(User user) {
        try {
            // verify user
            user = userService.verify(user);

            return user;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (ServiceException e) {
            throw new RestException(Status.BAD_REQUEST, "service.exception");
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }

    }

    @PUT
    @Path("/{id:[0-9][0-9]*}/status")
    @JsonView({UserWithLoginDataView.class})
    public User updateStatus(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, User requestUser) {
        try {
            final User loggedOn = getLoggedOn(authToken);
            // update status
            User u = userService.updateStatus(id, requestUser, loggedOn);
            return u;
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (ServiceException e) {
            throw new RestException(Status.BAD_REQUEST, "service.exception");
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @POST
    @Path("/search")
    @JsonView({UserView.class})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public SearchData<User> search(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // Fill result
            SearchData<User> result = userService.search(request, loggedOn);

            return result;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @POST
    @Path("/search/shibboleth")
    @JsonView({User.UserView.class})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public SearchData<User> searchShibbolethAccountUsers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // Fill result
            SearchData<User> result = userService.searchShibbolethAccountUsers(request, loggedOn);

            return result;
        } catch (NotEnabledException e) {
            throw new RestException(Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    @PUT
    @Path("/{id:[0-9]+}/revertaccount")
    public Response revertShibbolethAccount(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long userId, User user) {
        User loggedOn = getLoggedOn(authToken);

        if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
        }

        try {
            // revert the authentication from shibboleth to email
            authenticationService.revertShibbolethTotEmailAccount(userId);
        } catch (ServiceException e) {
            throw new RestException(Status.CONFLICT, e.getMessage());
        }

        return Response.ok().build();
    }
}
