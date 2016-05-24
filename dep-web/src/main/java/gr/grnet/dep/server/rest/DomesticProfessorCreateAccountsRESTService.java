package gr.grnet.dep.server.rest;


import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DomesticProfessorCreateAccountsService;
import gr.grnet.dep.service.ManagementService;
import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.User;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
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
        try {
            User loggedOn = getLoggedOn(authToken);
            // Parse Request
            List<FileItem> fileItems = readMultipartFormData(request);
            // create accounts
            List<ProfessorDomesticData> createdAccountsList = domesticProfessorCreateAccountsService.createAccounts(fileItems, loggedOn);

            //send mass login emails
            managementService.massSendLoginEmails();

            return Response.ok(createdAccountsList).build();
        } catch (NotEnabledException e) {
            throw new RestException(Response.Status.FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            throw new RestException(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
        }
    }
}
