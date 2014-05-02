package gr.grnet.dep.service;

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
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.ProfessorFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

@Stateless
public class DataExportService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static final Logger logger = Logger.getLogger(DataExportService.class.getName());

	/*******************************************************/

	private void addCell(Row row, int col, CellStyle style, String value) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		if (value != null) {
			cell.setCellValue(value);
		}
	}

	private void addCell(Row row, int col, CellStyle style, Integer value) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		if (value != null) {
			cell.setCellValue(value);
		}
	}

	private void addCell(Row row, int col, CellStyle style, Long value) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		if (value != null) {
			cell.setCellValue(value);
		}
	}

	private void addCell(Row row, int col, CellStyle style, Date value) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		if (value != null) {
			cell.setCellValue(value);
		}
	}

	/*******************************************************/

	private List<ProfessorDomestic> getProfessorDomesticData() {
		List<ProfessorDomestic> data = em.createQuery(
			"select distinct pd " +
				"from ProfessorDomestic pd " +
				"left join fetch pd.files f ", ProfessorDomestic.class)
			.getResultList();
		return data;
	}

	public InputStream createProfessorDomesticExcel() {
		//1. Get Data
		List<ProfessorDomestic> professors = getProfessorDomesticData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο");
		addCell(row, colNum++, titleStyle, "Name");
		addCell(row, colNum++, titleStyle, "Surname");
		addCell(row, colNum++, titleStyle, "Father's Name");
		addCell(row, colNum++, titleStyle, "e-mail");
		addCell(row, colNum++, titleStyle, "Κινητό");
		addCell(row, colNum++, titleStyle, "Σταθερό");
		addCell(row, colNum++, titleStyle, "creationdate");
		addCell(row, colNum++, titleStyle, "identification");
		addCell(row, colNum++, titleStyle, "authenticationtype");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_givenname");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_sn");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_schachomeorganization");
		addCell(row, colNum++, titleStyle, "AccountStatus");
		addCell(row, colNum++, titleStyle, "AccountStatusDate");
		addCell(row, colNum++, titleStyle, "ProfileStatus");
		addCell(row, colNum++, titleStyle, "ProfileStatusDate");
		addCell(row, colNum++, titleStyle, "Βαθμίδα");
		addCell(row, colNum++, titleStyle, "institution_id");
		addCell(row, colNum++, titleStyle, "Ίδρυμα/Ερευνητικό κέντρο");
		addCell(row, colNum++, titleStyle, "department_id");
		addCell(row, colNum++, titleStyle, "Τμήμα/Ινστιτούτο");
		addCell(row, colNum++, titleStyle, "hasCVURL");
		addCell(row, colNum++, titleStyle, "CVURL");
		addCell(row, colNum++, titleStyle, "CVfile");
		addCell(row, colNum++, titleStyle, "Γνωστικό αντικείμενο από ΦΕΚ");
		addCell(row, colNum++, titleStyle, "Γνωστικό αντικείμενο");
		addCell(row, colNum++, titleStyle, "ΦΕΚ");
		addCell(row, colNum++, titleStyle, "ΑρχείοΦΕΚ");
		addCell(row, colNum++, titleStyle, "hasacceptedterms");

		for (ProfessorDomestic p : professors) {
			ProfessorFile cvFile = FileHeader.filterOne(p.getFiles(), FileType.BIOGRAFIKO);
			ProfessorFile fekFile = FileHeader.filterOne(p.getFiles(), FileType.FEK);

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, p.getUser().getId());
			addCell(row, colNum++, textStyle, p.getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, p.getUser().getLastname("el"));
			addCell(row, colNum++, textStyle, p.getUser().getFathername("el"));
			addCell(row, colNum++, textStyle, p.getUser().getFirstname("en"));
			addCell(row, colNum++, textStyle, p.getUser().getLastname("en"));
			addCell(row, colNum++, textStyle, p.getUser().getFathername("en"));
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getEmail());
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getMobile());
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getPhone());
			addCell(row, colNum++, dateStyle, p.getUser().getCreationDate());
			addCell(row, colNum++, textStyle, p.getUser().getIdentification());
			addCell(row, colNum++, textStyle, p.getUser().getAuthenticationType().toString());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getGivenName());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getSn());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getSchacHomeOrganization());
			addCell(row, colNum++, textStyle, p.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, p.getStatus().toString());
			addCell(row, colNum++, textStyle, p.getStatusDate());
			addCell(row, colNum++, textStyle, p.getRank().getName().get("el"));
			addCell(row, colNum++, textStyle, p.getInstitution().getId());
			addCell(row, colNum++, textStyle, p.getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, p.getDepartment() != null ? p.getDepartment().getId() : null);
			addCell(row, colNum++, textStyle, p.getDepartment() != null ? p.getDepartment().getName().get("el") : null);
			addCell(row, colNum++, textStyle, p.getHasOnlineProfile() ? "NAI" : "OXI");
			addCell(row, colNum++, textStyle, p.getProfileURL());
			addCell(row, colNum++, intStyle, cvFile != null ? cvFile.getId() : null);
			addCell(row, colNum++, textStyle, p.getFekSubject() != null ? p.getFekSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getSubject() != null ? p.getSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getFek());
			addCell(row, colNum++, intStyle, fekFile != null ? fekFile.getId() : null);
			addCell(row, colNum++, textStyle, p.getHasAcceptedTerms() ? "NAI" : "OXI");
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<ProfessorForeign> getProfessorForeignData() {
		List<ProfessorForeign> data = em.createQuery(
			"select distinct pf " +
				"from ProfessorForeign pf " +
				"left join fetch pd.files f ", ProfessorForeign.class)
			.getResultList();
		return data;
	}

	public InputStream createProfessorForeignExcel() {
		//1. Get Data
		List<ProfessorForeign> professors = getProfessorForeignData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο");
		addCell(row, colNum++, titleStyle, "Name");
		addCell(row, colNum++, titleStyle, "Surname");
		addCell(row, colNum++, titleStyle, "Father's Name");
		addCell(row, colNum++, titleStyle, "e-mail");
		addCell(row, colNum++, titleStyle, "Κινητό");
		addCell(row, colNum++, titleStyle, "Σταθερό");
		addCell(row, colNum++, titleStyle, "creationdate");
		addCell(row, colNum++, titleStyle, "identification");
		addCell(row, colNum++, titleStyle, "authenticationtype");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_givenname");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_sn");
		addCell(row, colNum++, titleStyle, "shibbolethinfo_schachomeorganization");
		addCell(row, colNum++, titleStyle, "AccountStatus");
		addCell(row, colNum++, titleStyle, "AccountStatusDate");
		addCell(row, colNum++, titleStyle, "ProfileStatus");
		addCell(row, colNum++, titleStyle, "ProfileStatusDate");
		addCell(row, colNum++, titleStyle, "Βαθμίδα");
		addCell(row, colNum++, titleStyle, "Ίδρυμα/Ερευνητικό κέντρο");
		addCell(row, colNum++, titleStyle, "hasCVURL");
		addCell(row, colNum++, titleStyle, "CVURL");
		addCell(row, colNum++, titleStyle, "CVfile");
		addCell(row, colNum++, titleStyle, "Γνωστικό αντικείμενο");
		addCell(row, colNum++, titleStyle, "speakinggreek");
		addCell(row, colNum++, titleStyle, "hasacceptedterms");

		for (ProfessorForeign p : professors) {
			ProfessorFile cvFile = FileHeader.filterOne(p.getFiles(), FileType.BIOGRAFIKO);

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, p.getUser().getId());
			addCell(row, colNum++, textStyle, p.getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, p.getUser().getLastname("el"));
			addCell(row, colNum++, textStyle, p.getUser().getFathername("el"));
			addCell(row, colNum++, textStyle, p.getUser().getFirstname("en"));
			addCell(row, colNum++, textStyle, p.getUser().getLastname("en"));
			addCell(row, colNum++, textStyle, p.getUser().getFathername("en"));
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getEmail());
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getMobile());
			addCell(row, colNum++, textStyle, p.getUser().getContactInfo().getPhone());
			addCell(row, colNum++, dateStyle, p.getUser().getCreationDate());
			addCell(row, colNum++, textStyle, p.getUser().getIdentification());
			addCell(row, colNum++, textStyle, p.getUser().getAuthenticationType().toString());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getGivenName());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getSn());
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo().getSchacHomeOrganization());
			addCell(row, colNum++, textStyle, p.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, p.getStatus().toString());
			addCell(row, colNum++, textStyle, p.getStatusDate());
			addCell(row, colNum++, textStyle, p.getRank().getName().get("el"));
			addCell(row, colNum++, textStyle, p.getInstitution());
			addCell(row, colNum++, textStyle, p.getHasOnlineProfile() ? "NAI" : "OXI");
			addCell(row, colNum++, textStyle, p.getProfileURL());
			addCell(row, colNum++, intStyle, cvFile != null ? cvFile.getId() : null);
			addCell(row, colNum++, textStyle, p.getSubject() != null ? p.getSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getSpeakingGreek() ? "NAI" : "OXI");
			addCell(row, colNum++, textStyle, p.getHasAcceptedTerms() ? "NAI" : "OXI");
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	/*
	-Διαχειριστές Ιδρύματος
	id	Όνομα	Επώνυμο	Πατρώνυμο	Name	Surname	Father's Name	e-mail	Κινητό	Σταθερό	creationdate	identification	username	AccountStatus	AccountStatusDate	ProfileStatus	ProfileStatusDate	institution_id	Ίδρυμα	Αρχή πιστοποίησης	Ονοματεπώνυμο αρχής	Όνομα Αναπληρωτή	Επώνυμο Αναπληρωτή	Πατρώνυμο Αναπληρωτή	Name Αναπληρωτή	Surname Αναπληρωτή	Father's Name Αναπληρωτή	e-mail Αναπληρωτή	Κινητό Αναπληρωτή	Σταθερό Αναπληρωτή
	-Βοηθοί Ιδρύματος
	id	Όνομα	Επώνυμο	Πατρώνυμο	Name	Surname	Father's Name	e-mail	Κινητό	Σταθερό	creationdate	identification	username	AccountStatus	AccountStatusDate	institution_id	Ίδρυμα
	*/

	private List<Candidate> getCandidateData() {
		List<Candidate> data = em.createQuery(
			"select distinct c " +
				"from Candidate c " +
				"left join fetch c.files f ", Candidate.class)
			.getResultList();
		return data;
	}

	public InputStream createCandidateExcel() {
		//1. Get Data
		List<Candidate> candidates = getCandidateData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;

		addCell(row, colNum++, titleStyle, "id");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο");
		addCell(row, colNum++, titleStyle, "Name");
		addCell(row, colNum++, titleStyle, "Surname");
		addCell(row, colNum++, titleStyle, "Father's Name");
		addCell(row, colNum++, titleStyle, "e-mail");
		addCell(row, colNum++, titleStyle, "Κινητό");
		addCell(row, colNum++, titleStyle, "Σταθερό");
		addCell(row, colNum++, titleStyle, "creationdate");
		addCell(row, colNum++, titleStyle, "identification");
		addCell(row, colNum++, titleStyle, "username");
		addCell(row, colNum++, titleStyle, "AccountStatus");
		addCell(row, colNum++, titleStyle, "AccountStatusDate");
		addCell(row, colNum++, titleStyle, "ProfileStatus");
		addCell(row, colNum++, titleStyle, "ProfileStatusDate");
		addCell(row, colNum++, titleStyle, "IDfile");
		addCell(row, colNum++, titleStyle, "Βεβαίωση Στρατιωτικής Θητείας");
		addCell(row, colNum++, titleStyle, "Βεβαίωση συμμετοχής");
		addCell(row, colNum++, titleStyle, "CV");
		addCell(row, colNum++, titleStyle, "Πτυχία");
		addCell(row, colNum++, titleStyle, "Δημοσιεύσεις");
		addCell(row, colNum++, titleStyle, "Υποψηφιότητες");

		for (Candidate c : candidates) {
			CandidateFile idFile = FileHeader.filterOne(c.getFiles(), FileType.TAYTOTHTA);
			CandidateFile militaryFile = FileHeader.filterOne(c.getFiles(), FileType.BEBAIWSH_STRATIOTIKIS_THITIAS);
			CandidateFile formaFile = FileHeader.filterOne(c.getFiles(), FileType.FORMA_SYMMETOXIS);
			CandidateFile cvFile = FileHeader.filterOne(c.getFiles(), FileType.BIOGRAFIKO);
			Set<CandidateFile> degreesFiles = FileHeader.filter(c.getFiles(), FileType.PTYXIO);
			Set<CandidateFile> publicationsFiles = FileHeader.filter(c.getFiles(), FileType.DIMOSIEYSI);

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, c.getUser().getId());
			addCell(row, colNum++, textStyle, c.getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, c.getUser().getLastname("el"));
			addCell(row, colNum++, textStyle, c.getUser().getFathername("el"));
			addCell(row, colNum++, textStyle, c.getUser().getFirstname("en"));
			addCell(row, colNum++, textStyle, c.getUser().getLastname("en"));
			addCell(row, colNum++, textStyle, c.getUser().getFathername("en"));
			addCell(row, colNum++, textStyle, c.getUser().getContactInfo().getEmail());
			addCell(row, colNum++, textStyle, c.getUser().getContactInfo().getMobile());
			addCell(row, colNum++, textStyle, c.getUser().getContactInfo().getPhone());
			addCell(row, colNum++, dateStyle, c.getUser().getCreationDate());
			addCell(row, colNum++, textStyle, c.getUser().getIdentification());
			addCell(row, colNum++, textStyle, c.getUser().getUsername());
			addCell(row, colNum++, textStyle, c.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, c.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, c.getStatus().toString());
			addCell(row, colNum++, intStyle, c.getStatusDate());
			addCell(row, colNum++, textStyle, idFile != null ? idFile.getId() : null);
			addCell(row, colNum++, textStyle, militaryFile != null ? militaryFile.getId() : null);
			addCell(row, colNum++, textStyle, formaFile != null ? formaFile.getId() : null);
			addCell(row, colNum++, textStyle, cvFile != null ? cvFile.getId() : null);
			addCell(row, colNum++, textStyle, degreesFiles != null ? FileHeader.ids(degreesFiles).toString() : null);
			addCell(row, colNum++, textStyle, publicationsFiles != null ? FileHeader.ids(publicationsFiles).toString() : null);
			addCell(row, colNum++, intStyle, c.getCandidacies().size());
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<InstitutionManager> getInstitutionManagerData() {
		List<InstitutionManager> data = em.createQuery(
			"select im from InstitutionManager im ", InstitutionManager.class)
			.getResultList();
		return data;
	}

	public InputStream createInstitutionManagerExcel() {
		//1. Get Data
		List<InstitutionManager> managers = getInstitutionManagerData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο");
		addCell(row, colNum++, titleStyle, "Name");
		addCell(row, colNum++, titleStyle, "Surname");
		addCell(row, colNum++, titleStyle, "Father's Name");
		addCell(row, colNum++, titleStyle, "e-mail");
		addCell(row, colNum++, titleStyle, "Κινητό");
		addCell(row, colNum++, titleStyle, "Σταθερό");
		addCell(row, colNum++, titleStyle, "creationdate");
		addCell(row, colNum++, titleStyle, "identification");
		addCell(row, colNum++, titleStyle, "username");
		addCell(row, colNum++, titleStyle, "AccountStatus");
		addCell(row, colNum++, titleStyle, "AccountStatusDate");
		addCell(row, colNum++, titleStyle, "ProfileStatus");
		addCell(row, colNum++, titleStyle, "ProfileStatusDate");
		addCell(row, colNum++, titleStyle, "institution_id");
		addCell(row, colNum++, titleStyle, "Ίδρυμα");
		addCell(row, colNum++, titleStyle, "Αρχή πιστοποίησης");
		addCell(row, colNum++, titleStyle, "Ονοματεπώνυμο αρχής");
		addCell(row, colNum++, titleStyle, "Όνομα Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Επώνυμο Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Name Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Surname Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Father's name Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "e-mail Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Κινητό Αναπληρωτή");
		addCell(row, colNum++, titleStyle, "Σταθερό Αναπληρωτή");

		for (InstitutionManager im : managers) {

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, im.getUser().getId());
			addCell(row, colNum++, textStyle, im.getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, im.getUser().getLastname("el"));
			addCell(row, colNum++, textStyle, im.getUser().getFathername("el"));
			addCell(row, colNum++, textStyle, im.getUser().getFirstname("en"));
			addCell(row, colNum++, textStyle, im.getUser().getLastname("en"));
			addCell(row, colNum++, textStyle, im.getUser().getFathername("en"));
			addCell(row, colNum++, textStyle, im.getUser().getContactInfo().getEmail());
			addCell(row, colNum++, textStyle, im.getUser().getContactInfo().getMobile());
			addCell(row, colNum++, textStyle, im.getUser().getContactInfo().getPhone());
			addCell(row, colNum++, dateStyle, im.getUser().getCreationDate());
			addCell(row, colNum++, textStyle, im.getUser().getIdentification());
			addCell(row, colNum++, textStyle, im.getUser().getUsername());
			addCell(row, colNum++, textStyle, im.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, im.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, im.getStatus().toString());
			addCell(row, colNum++, textStyle, im.getStatusDate());
			addCell(row, colNum++, intStyle, im.getInstitution().getId());
			addCell(row, colNum++, textStyle, im.getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, im.getVerificationAuthority().toString());
			addCell(row, colNum++, textStyle, im.getVerificationAuthorityName());
			addCell(row, colNum++, textStyle, im.getAlternateFirstname("el"));
			addCell(row, colNum++, textStyle, im.getAlternateLastname("el"));
			addCell(row, colNum++, textStyle, im.getAlternateFathername("el"));
			addCell(row, colNum++, textStyle, im.getAlternateFirstname("en"));
			addCell(row, colNum++, textStyle, im.getAlternateLastname("en"));
			addCell(row, colNum++, textStyle, im.getAlternateFathername("en"));
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo().getEmail());
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo().getMobile());
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo().getPhone());

		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<InstitutionAssistant> getInstitutionAssistantData() {
		List<InstitutionAssistant> data = em.createQuery(
			"select ia from InstitutionAssistant ia ", InstitutionAssistant.class)
			.getResultList();
		return data;
	}

	public InputStream createInstitutionAssistantExcel() {
		//1. Get Data
		List<InstitutionAssistant> assistants = getInstitutionAssistantData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Πατρώνυμο");
		addCell(row, colNum++, titleStyle, "Name");
		addCell(row, colNum++, titleStyle, "Surname");
		addCell(row, colNum++, titleStyle, "Father's Name");
		addCell(row, colNum++, titleStyle, "e-mail");
		addCell(row, colNum++, titleStyle, "Κινητό");
		addCell(row, colNum++, titleStyle, "Σταθερό");
		addCell(row, colNum++, titleStyle, "creationdate");
		addCell(row, colNum++, titleStyle, "identification");
		addCell(row, colNum++, titleStyle, "username");
		addCell(row, colNum++, titleStyle, "AccountStatus");
		addCell(row, colNum++, titleStyle, "AccountStatusDate");
		addCell(row, colNum++, titleStyle, "institution_id");
		addCell(row, colNum++, titleStyle, "Ίδρυμα");

		for (InstitutionAssistant ia : assistants) {

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, ia.getUser().getId());
			addCell(row, colNum++, textStyle, ia.getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, ia.getUser().getLastname("el"));
			addCell(row, colNum++, textStyle, ia.getUser().getFathername("el"));
			addCell(row, colNum++, textStyle, ia.getUser().getFirstname("en"));
			addCell(row, colNum++, textStyle, ia.getUser().getLastname("en"));
			addCell(row, colNum++, textStyle, ia.getUser().getFathername("en"));
			addCell(row, colNum++, textStyle, ia.getUser().getContactInfo().getEmail());
			addCell(row, colNum++, textStyle, ia.getUser().getContactInfo().getMobile());
			addCell(row, colNum++, textStyle, ia.getUser().getContactInfo().getPhone());
			addCell(row, colNum++, dateStyle, ia.getUser().getCreationDate());
			addCell(row, colNum++, textStyle, ia.getUser().getIdentification());
			addCell(row, colNum++, textStyle, ia.getUser().getUsername());
			addCell(row, colNum++, textStyle, ia.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, ia.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, ia.getStatus().toString());
			addCell(row, colNum++, dateStyle, ia.getStatusDate());
			addCell(row, colNum++, intStyle, ia.getInstitution().getId());
			addCell(row, colNum++, textStyle, ia.getInstitution().getName().get("el"));
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<InstitutionRegulatoryFramework> getInstitutionRegulatoryFrameworkData() {
		List<InstitutionRegulatoryFramework> data = em.createQuery(
			"select ia from InstitutionRegulatoryFramework ia ", InstitutionRegulatoryFramework.class)
			.getResultList();
		return data;
	}

	public InputStream createInstitutionRegulatoryFrameworkExcel() {
		//1. Get Data
		List<InstitutionRegulatoryFramework> frameworks = getInstitutionRegulatoryFrameworkData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "institution_id");
		addCell(row, colNum++, titleStyle, "Ίδρυμα");
		addCell(row, colNum++, titleStyle, "Οργανισμός");
		addCell(row, colNum++, titleStyle, "Εσωτερικός Κανονισμός");

		for (InstitutionRegulatoryFramework irf : frameworks) {
			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, irf.getInstitution().getId());
			addCell(row, colNum++, textStyle, irf.getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, irf.getOrganismosURL());
			addCell(row, colNum++, textStyle, irf.getEswterikosKanonismosURL());
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<Object[]> getRegisterData() {
		List<Object[]> data = em.createQuery(
			"select r, count(r.members) from Register r ", Object[].class)
			.getResultList();
		return data;
	}

	public InputStream createRegisterExcel() {
		//1. Get Data
		List<Object[]> regsiters = getRegisterData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "institution_id");
		addCell(row, colNum++, titleStyle, "Ίδρυμα");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο");
		addCell(row, colNum++, titleStyle, "Μέλη");

		for (Object[] irf : regsiters) {
			Register r = (Register) irf[0];
			Long membersCount = (Long) irf[1];

			row = sheet.createRow(rowNum++);
			colNum = 0;
			addCell(row, colNum++, intStyle, r.getInstitution().getId());
			addCell(row, colNum++, textStyle, r.getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, r.getSubject().getName());
			addCell(row, colNum++, intStyle, membersCount);
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<RegisterMember> getRegisterMemberData() {
		List<RegisterMember> data = em.createQuery(
			"select r from RegisterMember r " +
				"order by r.member.id ", RegisterMember.class)
			.getResultList();
		return data;
	}

	public InputStream createRegisterMemberExcel() {
		//1. Get Data
		List<RegisterMember> members = getRegisterMemberData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;

		addCell(row, colNum++, titleStyle, "id καθηγητή");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Προφίλ");
		addCell(row, colNum++, titleStyle, "institution_id Καθηγητή");
		addCell(row, colNum++, titleStyle, "Ίδρυμα/Ερευνητικό κέντρο Καθηγητή");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Καθηγητή");
		addCell(row, colNum++, titleStyle, "institution_id Μητρώου");
		addCell(row, colNum++, titleStyle, "Ίδρυμα Μητρώου");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Μητρώου");
		addCell(row, colNum++, titleStyle, "Εσωτερικό/Εξωτερικό μέλος");

		for (RegisterMember rm : members) {
			row = sheet.createRow(rowNum++);
			colNum = 0;

			addCell(row, colNum++, intStyle, rm.getProfessor().getUser().getId());
			addCell(row, colNum++, intStyle, rm.getProfessor().getUser().getFirstname("el"));
			addCell(row, colNum++, intStyle, rm.getProfessor().getUser().getLastname("el"));
			switch (rm.getProfessor().getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) rm.getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Ημεδαπής");
					addCell(row, colNum++, intStyle, professorDomestic.getInstitution().getId());
					addCell(row, colNum++, textStyle, professorDomestic.getInstitution().getName().get("el"));
					addCell(row, colNum++, textStyle, professorDomestic.getFekSubject() != null ? professorDomestic.getFekSubject().getName() : professorDomestic.getSubject().getName());
					break;
				case PROFESSOR_FOREIGN:
					ProfessorForeign professorForeign = (ProfessorForeign) rm.getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Αλλοδαπής");
					addCell(row, colNum++, intStyle, (Integer) null);
					addCell(row, colNum++, textStyle, professorForeign.getInstitution());
					addCell(row, colNum++, textStyle, professorForeign.getSubject().getName());
				default:
					// Won't happen
					break;
			}
			addCell(row, colNum++, intStyle, rm.getRegister().getInstitution().getId());
			addCell(row, colNum++, textStyle, rm.getRegister().getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, rm.getRegister().getSubject().getName());
			addCell(row, colNum++, textStyle, rm.isExternal() ? "ΕΞΩΤΕΡΙΚΟ" : "ΕΣΩΤΕΡΙΚΟ");
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<PositionCommitteeMember> getPositionCommitteeMemberData() {
		List<PositionCommitteeMember> data = em.createQuery(
			"select r from PositionCommitteeMember r ", PositionCommitteeMember.class)
			.getResultList();
		return data;
	}

	public InputStream createPositionCommitteeMemberExcel() {
		//1. Get Data
		List<PositionCommitteeMember> members = getPositionCommitteeMemberData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id καθηγητή");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Προφίλ");
		addCell(row, colNum++, titleStyle, "institution_id Καθηγητή");
		addCell(row, colNum++, titleStyle, "Ίδρυμα/Ερευνητικό κέντρο Καθηγητή");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Καθηγητή");
		addCell(row, colNum++, titleStyle, "id θέσης");
		addCell(row, colNum++, titleStyle, "Τίτλος Θέσης");
		addCell(row, colNum++, titleStyle, "Ίδρυμα Θέσης");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Θέσης");
		addCell(row, colNum++, titleStyle, "Τακτικό/αναπληρωματικό μέλος");
		addCell(row, colNum++, titleStyle, "Κατάσταση θέσης");

		for (PositionCommitteeMember rm : members) {
			row = sheet.createRow(rowNum++);
			colNum = 0;

			addCell(row, colNum++, intStyle, rm.getRegisterMember().getProfessor().getUser().getId());
			addCell(row, colNum++, textStyle, rm.getRegisterMember().getProfessor().getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, rm.getRegisterMember().getProfessor().getUser().getLastname("el"));
			switch (rm.getRegisterMember().getProfessor().getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) rm.getRegisterMember().getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Ημεδαπής");
					addCell(row, colNum++, intStyle, professorDomestic.getInstitution().getId());
					addCell(row, colNum++, textStyle, professorDomestic.getInstitution().getName().get("el"));
					addCell(row, colNum++, textStyle, professorDomestic.getFekSubject() != null ? professorDomestic.getFekSubject().getName() : professorDomestic.getSubject().getName());
					break;
				case PROFESSOR_FOREIGN:
					ProfessorForeign professorForeign = (ProfessorForeign) rm.getRegisterMember().getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Αλλοδαπής");
					addCell(row, colNum++, intStyle, (Integer) null);
					addCell(row, colNum++, textStyle, professorForeign.getInstitution());
					addCell(row, colNum++, textStyle, professorForeign.getSubject().getName());
				default:
					// Won't happen
					break;
			}
			addCell(row, colNum++, intStyle, rm.getCommittee().getPosition().getId());
			addCell(row, colNum++, textStyle, rm.getCommittee().getPosition().getName());
			addCell(row, colNum++, textStyle, rm.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, rm.getCommittee().getPosition().getSubject().getName());

			addCell(row, colNum++, textStyle, rm.getType().toString());
			addCell(row, colNum++, textStyle, rm.getCommittee().getPosition().getPhase().getClientStatusInGreek());

		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<PositionEvaluator> getPositionEvaluatorData() {
		List<PositionEvaluator> data = em.createQuery(
			"select r from PositionEvaluator r ", PositionEvaluator.class)
			.getResultList();
		return data;
	}

	public InputStream createPositionEvaluatorExcel() {
		//1. Get Data
		List<PositionEvaluator> members = getPositionEvaluatorData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		addCell(row, colNum++, titleStyle, "id καθηγητή");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "Προφίλ");
		addCell(row, colNum++, titleStyle, "institution_id Καθηγητή");
		addCell(row, colNum++, titleStyle, "Ίδρυμα/Ερευνητικό κέντρο Καθηγητή");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Καθηγητή");
		addCell(row, colNum++, titleStyle, "id θέσης");
		addCell(row, colNum++, titleStyle, "Τίτλος Θέσης");
		addCell(row, colNum++, titleStyle, "Ίδρυμα Θέσης");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Θέσης");
		addCell(row, colNum++, titleStyle, "Κατάσταση θέσης");

		for (PositionEvaluator rm : members) {
			row = sheet.createRow(rowNum++);
			colNum = 0;

			addCell(row, colNum++, intStyle, rm.getRegisterMember().getProfessor().getUser().getId());
			addCell(row, colNum++, textStyle, rm.getRegisterMember().getProfessor().getUser().getFirstname("el"));
			addCell(row, colNum++, textStyle, rm.getRegisterMember().getProfessor().getUser().getLastname("el"));
			switch (rm.getRegisterMember().getProfessor().getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) rm.getRegisterMember().getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Ημεδαπής");
					addCell(row, colNum++, intStyle, professorDomestic.getInstitution().getId());
					addCell(row, colNum++, textStyle, professorDomestic.getInstitution().getName().get("el"));
					addCell(row, colNum++, textStyle, professorDomestic.getFekSubject() != null ? professorDomestic.getFekSubject().getName() : professorDomestic.getSubject().getName());
					break;
				case PROFESSOR_FOREIGN:
					ProfessorForeign professorForeign = (ProfessorForeign) rm.getRegisterMember().getProfessor();
					addCell(row, colNum++, textStyle, "Καθηγητής Αλλοδαπής");
					addCell(row, colNum++, intStyle, (Integer) null);
					addCell(row, colNum++, textStyle, professorForeign.getInstitution());
					addCell(row, colNum++, textStyle, professorForeign.getSubject().getName());
				default:
					// Won't happen
					break;
			}
			addCell(row, colNum++, intStyle, rm.getEvaluation().getPosition().getId());
			addCell(row, colNum++, textStyle, rm.getEvaluation().getPosition().getName());
			addCell(row, colNum++, textStyle, rm.getEvaluation().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, rm.getEvaluation().getPosition().getSubject().getName());
			addCell(row, colNum++, textStyle, rm.getEvaluation().getPosition().getPhase().getClientStatusInGreek());

		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	private List<Candidacy> getCandidacyData() {
		List<Candidacy> data = em.createQuery(
			"select r from Candidacy r ", Candidacy.class)
			.getResultList();
		return data;
	}

	public InputStream createCandidacyExcel() {
		//1. Get Data
		List<Candidacy> members = getCandidacyData();
		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		row = sheet.createRow(rowNum++);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		addCell(row, 0, titleStyle, "Ημερομηνία: " + sdf.format(new Date()));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		colNum = 0;
		//id υποψηφίου	Όνομα	Επώνυμο	id θέσης	Τίτλος Θέσης	Ίδρυμα Θέσης	Γνωστικό Αντικείμενο Θέσης	Κατάσταση θέσης

		addCell(row, colNum++, titleStyle, "id υποψηφίου");
		addCell(row, colNum++, titleStyle, "Όνομα");
		addCell(row, colNum++, titleStyle, "Επώνυμο");
		addCell(row, colNum++, titleStyle, "id θέσης");
		addCell(row, colNum++, titleStyle, "Τίτλος Θέσης");
		addCell(row, colNum++, titleStyle, "Ίδρυμα Θέσης");
		addCell(row, colNum++, titleStyle, "Γνωστικό Αντικείμενο Θέσης");
		addCell(row, colNum++, titleStyle, "Κατάσταση θέσης");

		for (Candidacy rm : members) {
			row = sheet.createRow(rowNum++);
			colNum = 0;

			addCell(row, colNum++, intStyle, rm.getCandidate().getUser().getId());
			addCell(row, colNum++, textStyle, rm.getSnapshot().getFirstname("el"));
			addCell(row, colNum++, textStyle, rm.getSnapshot().getLastname("el"));
			addCell(row, colNum++, intStyle, rm.getCandidacies().getPosition().getId());
			addCell(row, colNum++, textStyle, rm.getCandidacies().getPosition().getName());
			addCell(row, colNum++, textStyle, rm.getCandidacies().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, rm.getCandidacies().getPosition().getSubject().getName());
			addCell(row, colNum++, textStyle, rm.getCandidacies().getPosition().getPhase().getClientStatusInGreek());
		}

		for (int i = 0; i < colNum; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}
}
