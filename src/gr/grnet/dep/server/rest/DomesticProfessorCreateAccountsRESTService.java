package gr.grnet.dep.server.rest;


import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.ManagementService;
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
@Stateless
public class DomesticProfessorCreateAccountsRESTService extends RESTService {

    @EJB
    ManagementService managementService;

    private static final String LAST_NAME = "LASTNAME";
    private static final String FIRST_NAME = "FIRSTNAME";
    private static final String FATHER_NAME = "FATHERNAME";
    private static final String DEPARTMENT_ID = "DEPARTMENTID";
    private static final String INSTITUTION_ID = "INSTITUTIONID";
    private static final String EMAIL = "EMAIL";
    private static final String RANK_ID = "RANKID";
    private static final String FEK = "FEK";
    private static final String FEK_SUBJECT = "FEK_SUBJECT";

    private static final String[] FILE_HEADER_MAPPING = {LAST_NAME, FIRST_NAME, FATHER_NAME, DEPARTMENT_ID, INSTITUTION_ID, EMAIL, RANK_ID, FEK, FEK_SUBJECT};

    private static final char CSV_DELIMITER = ';';
    private static final char DOUBLE_QUOTE = '"';
    private static final char FULL_STOP = '.';

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

        Reader reader = null;
        CSVParser csvParser = null;

        CSVFormat csvFormat = CSVFormat.newFormat(CSV_DELIMITER).withHeader(FILE_HEADER_MAPPING).withQuote(DOUBLE_QUOTE);
        List<FileItem> fileItems = readMultipartFormData(request);

        //In our case the fileΙtems list has always has one upload file
        String name = fileItems.get(0).getName();
        int index = name.lastIndexOf(FULL_STOP);
        if (index == -1 || !name.substring(index + 1).equalsIgnoreCase("csv")) {
            throw new RestException(Response.Status.CONFLICT, "file.notCSVFile");
        }
        try {

            reader = new InputStreamReader(fileItems.get(0).getInputStream(), "UTF-8");
            csvParser = new CSVParser(reader, csvFormat);

            List<ProfessorDomesticData> professorDomesticDataList = retrieveProfessorDomesticDataFromCSVFile(reader, csvParser);

            //create the domestic professors
            List<ProfessorDomesticData> createdAccountsList = managementService.massCreateProfessorDomesticAccounts(professorDomesticDataList);
            //send mass login emails
            managementService.massSendLoginEmails();

            return Response.ok(createdAccountsList).build();

        } catch (IllegalArgumentException e) {
            logger.info("IllegalArgument Exception");
            throw new RestException(Response.Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.info("Generic Exception");
            throw new RestException(Response.Status.CONFLICT, "csvFile.fileParsing.failed");
        } finally {
            try {
                reader.close();
                csvParser.close();
            } catch (IOException e) {
                logger.info("Error while closing fileReader/csvFileParser !!!");
                throw new RestException(Response.Status.CONFLICT, "csvFile.closingResources.failed");
            }
        }
    }

    /**
     * @param reader
     * @param csvParser
     * @return
     * @throws IOException
     * @throws Exception
     */

    private List<ProfessorDomesticData> retrieveProfessorDomesticDataFromCSVFile(Reader reader, CSVParser csvParser) throws IOException, Exception {
        List<CSVRecord> csvRecords = csvParser.getRecords();

        if (csvRecords.isEmpty()) {
            throw new RestException(Response.Status.NO_CONTENT, "csvFile.empty");
        }

        List<ProfessorDomesticData> professorDomesticDataList = new ArrayList<>();

        //Read the CSV file records starting from the second record to skip the header
        for (int i = 1; i < csvRecords.size(); i++) {
            CSVRecord record = csvRecords.get(i);

            if (!validateRecord(record)) {
                throw new RestException(Response.Status.CONFLICT, "csvFile.missing.required.fields");
            }
            ProfessorDomesticData professorDomesticData = new ProfessorDomesticData();
            professorDomesticData.setLastname(record.get(LAST_NAME));
            professorDomesticData.setFirstname(record.get(FIRST_NAME));
            professorDomesticData.setFathername(record.get(FATHER_NAME));
            professorDomesticData.setEmail(record.get(EMAIL));
            professorDomesticData.setFek(record.get(FEK));
            professorDomesticData.setFekSubject(record.get(FEK_SUBJECT));

            Department department = em.find(Department.class, Long.parseLong(record.get(DEPARTMENT_ID)));
            Institution institution = em.find(Institution.class, Long.parseLong(record.get(INSTITUTION_ID)));
            Rank rank = em.find(Rank.class, Long.parseLong(record.get(RANK_ID)));
            if (department == null || institution == null || rank == null) {
                logger.info("Ivalid values are contained for record with department id = " + record.get(DEPARTMENT_ID) + " and institution id = " + record.get(INSTITUTION_ID));
                throw new IllegalArgumentException("csvFile.values.not.exist");
            }
            if (department.getSchool().getInstitution().getId().compareTo(institution.getId()) != 0) {
                logger.info("The department does not belong to institution for record with department id = " + record.get(DEPARTMENT_ID) + " and institution id = " + record.get(INSTITUTION_ID));
                throw new IllegalArgumentException("csvFile.department.notBelong.to.declared.institution");
            }
            professorDomesticData.setDepartment(department);
            professorDomesticData.setInstitution(institution);
            professorDomesticData.setRank(rank);

            professorDomesticDataList.add(professorDomesticData);
        }
        return professorDomesticDataList;
    }

    /**
     * validate if each record has all the required values
     *
     * @param record
     * @return
     */

    private boolean validateRecord(CSVRecord record) {
        return !StringUtils.isEmpty(record.get(LAST_NAME)) && !StringUtils.isEmpty(record.get(FIRST_NAME))
                && !StringUtils.isEmpty(record.get(FATHER_NAME)) && !StringUtils.isEmpty(record.get(DEPARTMENT_ID))
                && !StringUtils.isEmpty(record.get(INSTITUTION_ID)) && !StringUtils.isEmpty(record.get(EMAIL))
                && !StringUtils.isEmpty(record.get(RANK_ID)) && !StringUtils.isEmpty(record.get(FEK))
                && !StringUtils.isEmpty(record.get(FEK_SUBJECT));
    }
}
