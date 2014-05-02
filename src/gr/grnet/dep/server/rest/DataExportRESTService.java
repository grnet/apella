package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DataExportService;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/exports")
@Stateless
public class DataExportRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	DataExportService dataExportService;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{type}")
	public Response getPositionsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("type") String type) {
		User loggedOn = getLoggedOn(authToken);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			InputStream is = null;
			if (type.equals("candidacy")) {
				is = dataExportService.createCandidacyExcel();
			} else if (type.equals("candidate")) {
				is = dataExportService.createCandidateExcel();
			} else if (type.equals("institution-assistant")) {
				is = dataExportService.createInstitutionAssistantExcel();
			} else if (type.equals("institution-manager")) {
				is = dataExportService.createInstitutionManagerExcel();
			} else if (type.equals("institution-regulatory-framework")) {
				is = dataExportService.createInstitutionRegulatoryFrameworkExcel();
			} else if (type.equals("position-committee-member")) {
				is = dataExportService.createPositionCommitteeMemberExcel();
			} else if (type.equals("position-evaluator")) {
				is = dataExportService.createPositionEvaluatorExcel();
			} else if (type.equals("professor-domestic")) {
				is = dataExportService.createProfessorDomesticExcel();
			} else if (type.equals("professor-foreign")) {
				is = dataExportService.createProfessorForeignExcel();
			} else if (type.equals("register")) {
				is = dataExportService.createRegisterExcel();
			} else if (type.equals("register-member")) {
				is = dataExportService.createRegisterMemberExcel();
			} else {
				throw new RestException(Status.NOT_FOUND);
			}
			// Return response
			String string = ".xls";
			return Response.ok(is)
				.type(MediaType.APPLICATION_OCTET_STREAM)
				.header("charset", "UTF-8")
				.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(type + string, "UTF-8") + "\"")
				.build();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "getDocument", e);
			throw new EJBException(e);
		}
	}
}
