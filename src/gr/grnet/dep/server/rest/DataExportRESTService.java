package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.DataExportService;
import gr.grnet.dep.service.model.Administrator;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileType;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/exports")
@Stateless
public class DataExportRESTService extends RESTService {

	@Inject
	private Logger log;

	@EJB
	DataExportService dataExportService;

	@GET
	@Path("/{type}")
	public Response getPositionsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("type") String type) {
		User loggedOn = getLoggedOn(authToken);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Administrator admin = (Administrator) loggedOn.getActiveRole(RoleDiscriminator.ADMINISTRATOR);
		if (!admin.isSuperAdministrator()) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			InputStream is = null;
			if (type.equals("candidacy")) {
				//1. Get Data
				List<Candidacy> members = dataExportService.getCandidacyData();
				//2. Create XLS
				is = dataExportService.createCandidacyExcel(members);
			} else if (type.equals("candidate")) {
				//1. Get Data
				List<Candidate> candidates = dataExportService.getCandidateData();
				Map<Long, Map<FileType, Set<Long>>> filesMap = dataExportService.getCandidateFilesData();
				Map<Long, Long> candidaciesMap = dataExportService.getCandidateCandidaciesData();
				//2. Create XLS
				is = dataExportService.createCandidateExcel(candidates, filesMap, candidaciesMap);
			} else if (type.equals("institution-assistant")) {
				//1. Get Data
				List<InstitutionAssistant> assistants = dataExportService.getInstitutionAssistantData();
				//2. Create XLS
				is = dataExportService.createInstitutionAssistantExcel(assistants);
			} else if (type.equals("institution-manager")) {
				//1. Get Data
				List<InstitutionManager> managers = dataExportService.getInstitutionManagerData();
				//2. Create XLS
				is = dataExportService.createInstitutionManagerExcel(managers);
			} else if (type.equals("institution-regulatory-framework")) {
				//1. Get Data
				List<InstitutionRegulatoryFramework> frameworks = dataExportService.getInstitutionRegulatoryFrameworkData();
				//2. Create XLS
				is = dataExportService.createInstitutionRegulatoryFrameworkExcel(frameworks);
			} else if (type.equals("position-committee-member")) {
				//1. Get Data
				List<PositionCommitteeMember> members = dataExportService.getPositionCommitteeMemberData();
				//2. Create XLS
				is = dataExportService.createPositionCommitteeMemberExcel(members);
			} else if (type.equals("position-evaluator")) {
				//1. Get Data
				List<PositionEvaluator> members = dataExportService.getPositionEvaluatorData();
				//2. Create XLS
				is = dataExportService.createPositionEvaluatorExcel(members);
			} else if (type.equals("professor-domestic")) {
				//1. Get Data
				List<ProfessorDomestic> professors = dataExportService.getProfessorDomesticData();
				Map<Long, Map<FileType, Long>> pFilesMap = dataExportService.getProfessorFilesData();
				//2. Create xls
				is = dataExportService.createProfessorDomesticExcel(professors, pFilesMap);
			} else if (type.equals("professor-foreign")) {
				//1. Get Data
				List<ProfessorForeign> professors = dataExportService.getProfessorForeignData();
				Map<Long, Map<FileType, Long>> pFilesMap = dataExportService.getProfessorFilesData();
				//2. Create XLS
				is = dataExportService.createProfessorForeignExcel(professors, pFilesMap);
			} else if (type.equals("register")) {
				//1. Get Data
				List<Register> registers = dataExportService.getRegisterData();
				Map<Long, Long> membersMap = dataExportService.getRegisterRegisterMemberData();
				//2. Create XLS
				is = dataExportService.createRegisterExcel(registers, membersMap);
			} else if (type.equals("register-member")) {
				is = dataExportService.createRegisterMemberExcel(dataExportService.getRegisterMemberData());
			} else {
				throw new RestException(Status.NOT_FOUND);
			}
			// Return response
			return Response.ok(is)
					.type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(type + ".xlsx", "UTF-8") + "\"")
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "getDocument", e);
			throw new EJBException(e);
		}
	}
}
