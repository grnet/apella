package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class DomesticProfessorCreateAccountsService extends CommonService {


    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private ManagementService managementService;

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

    public List<ProfessorDomesticData> createAccounts(HttpServletRequest request, User loggedOn) throws ValidationException, NotEnabledException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Administrator admin = (Administrator) loggedOn.getActiveRole(Role.RoleDiscriminator.ADMINISTRATOR);
        if (!admin.isSuperAdministrator()) {
            throw new NotEnabledException("insufficient.privileges");
        }

        Reader reader = null;
        CSVParser csvParser = null;

        CSVFormat csvFormat = CSVFormat.newFormat(CSV_DELIMITER).withHeader(FILE_HEADER_MAPPING).withQuote(DOUBLE_QUOTE);
        List<FileItem> fileItems = readMultipartFormData(request);

        //In our case the fileΙtems list has always has one upload file
        String name = fileItems.get(0).getName();
        int index = name.lastIndexOf(FULL_STOP);
        if (index == -1 || !name.substring(index + 1).equalsIgnoreCase("csv")) {
            //throw new RestException(Response.Status.CONFLICT, "file.notCSVFile");
            throw new ValidationException("file.notCSVFile");
        }
        try {

            reader = new InputStreamReader(fileItems.get(0).getInputStream(), "UTF-8");
            csvParser = new CSVParser(reader, csvFormat);

            // get domestic professors from csv file
            List<ProfessorDomesticData> professorDomesticDataList = retrieveProfessorDomesticDataFromCSVFile(reader, csvParser);

            // create the domestic professors
            List<ProfessorDomesticData> createdAccountsList = managementService.massCreateProfessorDomesticAccounts(professorDomesticDataList);

            return createdAccountsList;

        } catch (IllegalArgumentException e) {
            log.info("IllegalArgument Exception");
            throw new ValidationException(e.getMessage());
        } catch (Exception e) {
            log.info("Generic Exception");
            throw new ValidationException("csvFile.fileParsing.failed");
        } finally {
            try {
                reader.close();
                csvParser.close();
            } catch (IOException e) {
                log.info("Error while closing fileReader/csvFileParser !!!");
                throw new ValidationException("csvFile.closingResources.failed");
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
            // throw new RestException(Response.Status.NO_CONTENT, "csvFile.empty");
            throw new ValidationException("csvFile.empty");
        }

        List<ProfessorDomesticData> professorDomesticDataList = new ArrayList<>();

        //Read the CSV file records starting from the second record to skip the header
        for (int i = 1; i < csvRecords.size(); i++) {
            CSVRecord record = csvRecords.get(i);

            if (!validateRecord(record)) {
                // throw new RestException(Response.Status.CONFLICT, "csvFile.missing.required.fields");
                throw new ValidationException("csvFile.missing.required.fields");
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
                log.info("Ivalid values are contained for record with department id = " + record.get(DEPARTMENT_ID) + " and institution id = " + record.get(INSTITUTION_ID));
                throw new IllegalArgumentException("csvFile.values.not.exist");
            }
            if (department.getSchool().getInstitution().getId().compareTo(institution.getId()) != 0) {
                log.info("The department does not belong to institution for record with department id = " + record.get(DEPARTMENT_ID) + " and institution id = " + record.get(INSTITUTION_ID));
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

