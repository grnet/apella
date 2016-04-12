package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.RoleService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.ProfessorFile;
import gr.grnet.dep.service.util.PdfUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/role")
public class RoleRESTService extends RESTService {

    @Inject
    private Logger log;

    @EJB
    private RoleService roleService;

    /**
     * Returns roles of user based on parameters
     *
     * @param authToken      Authentication Token
     * @param userID         ID of user
     * @param discriminators Comma separated list of types of roles
     * @param statuses       Comma separated list of status of roles
     * @return
     */
    @GET
    @JsonView({DetailedRoleView.class})
    public List<Role> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @QueryParam("user") Long userID, @QueryParam("discriminator") String discriminators, @QueryParam("status") String statuses) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get all roles
            List<Role> roles = roleService.getAll(userID, discriminators, statuses, loggedOn);
            return roles;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Returns role with given id
     *
     * @param authToken
     * @param id
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     */
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({DetailedRoleView.class})
    public Role get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get role
            Role r = roleService.get(id, loggedOn);

            return r;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Adds a role to an existing user
     *
     * @param authToken
     * @param newRole
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.id
     * @HTTP 409 X-Error-Code: incompatible.role
     */
    @POST
    @JsonView({DetailedRoleView.class})
    public Role create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Role newRole) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create role
            newRole = roleService.create(newRole, loggedOn);

            return newRole;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Updates role of user
     *
     * @param authToken
     * @param id
     * @param updateCandidacies If true it will also update all open candidacies
     *                          connected to this profile
     * @param role
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     * @HTTP 409 X-Error-Code: cannot.change.critical.fields
     * @HTTP 409 X-Error-Code: manager.institution.mismatch
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @JsonView({DetailedRoleView.class})
    public Role update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @QueryParam("updateCandidacies") Boolean updateCandidacies, Role role) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update
            Role existingRole = roleService.update(id, updateCandidacies, role, loggedOn);

            return existingRole;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (ServiceException e) {
            throw new RestException(Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * ************************
     * Document Functions ******
     * *************************
     */

    /**
     * Creates a PDF File with
     * (1) the form required for Institution Managers or
     * (2) the form for Candidates
     *
     * @param authToken
     * @param id
     * @param fileName  Name of file (Forma_allagis_Diax_Idrymatos or
     *                  Forma_Ypopsifiou)
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id:[0-9]+}/documents/{fileName}")
    public Response getDocument(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileName") RoleService.DocumentDiscriminator fileName) {
        Role role = em.find(Role.class, id);
        // Validate:
        if (role == null) {
            throw new RestException(Status.NOT_FOUND, "wrong.role.id");
        }
        switch (fileName) {
            case InstitutionManagerCertificationDean:
            case InstitutionManagerCertificationPresident:
                if (!(role instanceof InstitutionManager)) {
                    throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                }
                break;
            case CandidateRegistrationForm:
                if (!(role instanceof Candidate)) {
                    throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
                }
                break;
        }
        // Generate Document
        try {
            InputStream is = roleService.getDocument(id, fileName);
            // Return response
            return Response.ok(is)
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("charset", "UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName + ".pdf", "UTF-8") + "\"")
                    .build();
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "getDocument", e);
            throw new EJBException(e);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /***************************
     * File Functions **********
     ***************************/

    /**
     * Retrieves files of role
     *
     * @param authToken
     * @param id
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     */
    @GET
    @Path("/{id:[0-9][0-9]*}/file")
    @JsonView({SimpleFileHeaderView.class})
    public Response getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id) {
        try {
            User loggedOn = getLoggedOn(authToken);

            // get files
            Set<? extends FileHeader> files = roleService.getFiles(id, loggedOn);

            if (files == null) {
                throw new RestException(Status.BAD_REQUEST, "wrong.id");
            }

            // get the role in order to distinguish the files
            Role role = roleService.get(id);

            // Return Result
            if (role instanceof Candidate) {
                Set<CandidateFile> candidateFiles = (Set<CandidateFile>) files;
                GenericEntity<Set<CandidateFile>> entity = new GenericEntity<Set<CandidateFile>>(FileHeader.filterDeleted(candidateFiles)) {
                };
                return Response.ok(entity).build();
            } else if (role instanceof Professor) {
                Set<ProfessorFile> professorFiles = (Set<ProfessorFile>) files;
                GenericEntity<Set<ProfessorFile>> entity = new GenericEntity<Set<ProfessorFile>>(FileHeader.filterDeleted(professorFiles)) {
                };
                return Response.ok(entity).build();
            }
            throw new RestException(Status.BAD_REQUEST, "wrong.id");
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Retrieves specific file description of specific role
     *
     * @param authToken Authentication Token
     * @param id
     * @param fileId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     */
    @GET
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
    @JsonView({SimpleFileHeaderView.class})
    public FileHeader getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file
            FileHeader file = roleService.getFile(id, fileId, loggedOn);

            if (file == null) {
                throw new RestException(Status.BAD_REQUEST, "wrong.id");
            }
            return file;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Retrieves file body (binary) of specified file
     *
     * @param authToken
     * @param id
     * @param fileId
     * @param bodyId
     * @return
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
    public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // get file body
            FileBody fileBody = roleService.getFileBody(id, fileId, bodyId, loggedOn);

            if (fileBody == null) {
                throw new RestException(Status.NOT_FOUND, "wrong.file.id");
            }

            return sendFileBody(fileBody);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Creates a new file associated with specified role
     *
     * @param authToken
     * @param id
     * @param request
     * @return A JSON representation of the file description, needs to be
     * text/plain since it is handled by a multipart-form
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     * @HTTP 409 X-Error-Code: cannot.change.critical.fields
     */
    @POST
    @Path("/{id:[0-9][0-9]*}/file")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // create file
            FileHeader file = roleService.createFile(id, request, loggedOn);

            if (file == null) {
                throw new RestException(Status.CONFLICT, "wrong.file.type");
            }

            return toJSON(file, SimpleFileHeaderView.class);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Updates the specified file
     *
     * @param authToken
     * @param id
     * @param fileId
     * @param request
     *
     * @return A JSON representation of the file description, needs to be
     * text/plain since it is handled by a multipart-form
     * @throws FileUploadException
     * @throws IOException
     * @HTTP 400 X-Error-Code: missing.file.type
     * @HTTP 403 X-Error-Code: insufficient.privileges
     * @HTTP 404 X-Error-Code: wrong.role.id
     * @HTTP 409 X-Error-Code: cannot.change.critical.fields
     */
    @POST
    @Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long id, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
        try {
            User loggedOn = getLoggedOn(authToken);
            // update file
            FileHeader file = roleService.updateFile(id, fileId, request, loggedOn);

            if (file == null) {
                throw new RestException(Status.CONFLICT, "wrong.file.type");
            }
            return toJSON(file, SimpleFileHeaderView.class);
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /**
     * Removes the specified file
     *
     * @param authToken
     * @param id
     * @param fileId
     * @param updateCandidacies If set to true it will update also open
     *                          candidacies connected with this profile
     * @return
     */
    @DELETE
    @Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
    @JsonView({SimpleFileHeaderView.class})
    public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, @PathParam("fileId") Long fileId, @QueryParam("updateCandidacies") Boolean updateCandidacies) {
        try {
            User loggedOn = getLoggedOn(authToken);
            // delete file
            roleService.deleteFile(id, fileId, updateCandidacies, loggedOn);

            return Response.noContent().build();
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }

    /************************************
     *** Administrator Only Functions ***
     ************************************/

    /**
     * Used by admins to update status of users
     *
     * @param authToken
     * @param id
     * @param requestRole
     * @return
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}/status")
    @JsonView({DetailedRoleView.class})
    public Role updateStatus(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Role requestRole) {
        try {
            final User loggedOn = getLoggedOn(authToken);
            // update status
            Role role = roleService.updateStatus(id, requestRole, loggedOn);

            return role;
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            throw new RestException(Status.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }
}
