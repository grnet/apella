package gr.grnet.dep.server.rest;


import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DomesticProfessorCreateAccountsService;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Path("/domesticprofessor")
public class DomesticProfessorCreateAccountsRESTService extends RESTService {

    @EJB
    ManagementService managementService;

    @EJB
    DomesticProfessorCreateAccountsService domesticProfessorCreateAccountsService;

    @POST
    @Path("/createaccount")
    @Consumes("multipart/form-data")
    public Response createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @Context HttpServletRequest request) throws FileUploadException, IOException {

        User loggedOn = getLoggedOn(authToken);
        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
        }

        Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
        if (!admin.isSuperAdministrator()) {
            throw new RestException(Response.Status.FORBIDDEN, "insufficient.privileges");
        }

        try {
            // create accounts
            List<ProfessorDomesticData> createdAccountsList = domesticProfessorCreateAccountsService.createAccounts(request);

            //send mass login emails
            managementService.massSendLoginEmails();

            return Response.ok(createdAccountsList).build();

        } catch (ValidationException e) {
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
        }
    }
}
