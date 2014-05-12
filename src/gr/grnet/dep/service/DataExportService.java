package gr.grnet.dep.service;

import gr.grnet.dep.service.dto.Statistics;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionAssistant;
import gr.grnet.dep.service.model.InstitutionManager;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Rank;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.file.FileType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	private Map<Long, Map<FileType, Long>> getProfessorFilesData() {
		List<Object[]> files = em.createQuery(
			"select p.id, pf.type, pf.id from ProfessorFile pf " +
				"join pf.professor p " +
				"where pf.deleted = false ", Object[].class).getResultList();
		Map<Long, Map<FileType, Long>> filesMap = new HashMap<Long, Map<FileType, Long>>();
		for (Object[] file : files) {
			Long professorId = (Long) file[0];
			FileType type = (FileType) file[1];
			Long fileId = (Long) file[2];
			if (!filesMap.containsKey(professorId)) {
				filesMap.put(professorId, new HashMap<FileType, Long>());
			}
			filesMap.get(professorId).put(type, fileId);
		}
		return filesMap;
	}

	private List<ProfessorDomestic> getProfessorDomesticData() {
		List<ProfessorDomestic> data = em.createQuery(
			"select distinct pd " +
				"from ProfessorDomestic pd " +
				"join fetch pd.user u " +
				"left join fetch pd.institution inst " +
				"left join fetch pd.department dep " +
				"left join fetch dep.school sch " +
				"left join fetch sch.institution sinst " +
				"left join fetch pd.subject sub " +
				"left join fetch pd.fekSubject fsub ", ProfessorDomestic.class)
			.getResultList();
		return data;
	}

	public InputStream createProfessorDomesticExcel() {
		//1. Get Data
		List<ProfessorDomestic> professors = getProfessorDomesticData();
		Map<Long, Map<FileType, Long>> pFilesMap = getProfessorFilesData();
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
			Map<FileType, Long> pFiles = pFilesMap.containsKey(p.getId()) ? pFilesMap.get(p.getId()) : new HashMap<FileType, Long>();
			Long cvFile = pFiles.get(FileType.PROFILE);
			Long fekFile = pFiles.get(FileType.FEK);

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
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getGivenName() : "");
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getSn() : "");
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getSchacHomeOrganization() : "");
			addCell(row, colNum++, textStyle, p.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, p.getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getStatusDate());
			addCell(row, colNum++, textStyle, p.getRank() != null ? p.getRank().getName().get("el") : "");
			addCell(row, colNum++, textStyle, p.getInstitution().getId());
			addCell(row, colNum++, textStyle, p.getInstitution().getName().get("el"));
			addCell(row, colNum++, textStyle, p.getDepartment() != null ? p.getDepartment().getId() : null);
			addCell(row, colNum++, textStyle, p.getDepartment() != null ? p.getDepartment().getName().get("el") : null);
			addCell(row, colNum++, textStyle, p.getHasOnlineProfile() ? "NAI" : "OXI");
			addCell(row, colNum++, textStyle, p.getProfileURL());
			addCell(row, colNum++, intStyle, cvFile != null ? cvFile : null);
			addCell(row, colNum++, textStyle, p.getFekSubject() != null ? p.getFekSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getSubject() != null ? p.getSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getFek());
			addCell(row, colNum++, intStyle, fekFile != null ? fekFile : null);
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
				"join fetch pf.user u " +
				"left join fetch pf.country c " +
				"left join fetch pf.subject sub ", ProfessorForeign.class)
			.getResultList();
		return data;
	}

	public InputStream createProfessorForeignExcel() {
		//1. Get Data
		List<ProfessorForeign> professors = getProfessorForeignData();
		Map<Long, Map<FileType, Long>> pFilesMap = getProfessorFilesData();
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
		addCell(row, colNum++, titleStyle, "Χώρα Ιδρύματος");
		addCell(row, colNum++, titleStyle, "hasCVURL");
		addCell(row, colNum++, titleStyle, "CVURL");
		addCell(row, colNum++, titleStyle, "CVfile");
		addCell(row, colNum++, titleStyle, "Γνωστικό αντικείμενο");
		addCell(row, colNum++, titleStyle, "speakinggreek");
		addCell(row, colNum++, titleStyle, "hasacceptedterms");

		for (ProfessorForeign p : professors) {
			Map<FileType, Long> pFiles = pFilesMap.containsKey(p.getId()) ? pFilesMap.get(p.getId()) : new HashMap<FileType, Long>();
			Long cvFile = pFiles.get(FileType.PROFILE);

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
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getGivenName() : null);
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getSn() : null);
			addCell(row, colNum++, textStyle, p.getUser().getShibbolethInfo() != null ? p.getUser().getShibbolethInfo().getSchacHomeOrganization() : null);
			addCell(row, colNum++, textStyle, p.getUser().getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getUser().getStatusDate());
			addCell(row, colNum++, textStyle, p.getStatus().toString());
			addCell(row, colNum++, dateStyle, p.getStatusDate());
			addCell(row, colNum++, textStyle, p.getRank() != null ? p.getRank().getName().get("el") : null);
			addCell(row, colNum++, textStyle, p.getInstitution());
			addCell(row, colNum++, textStyle, p.getCountry() != null ? p.getCountry().getName() : null);
			addCell(row, colNum++, textStyle, p.getHasOnlineProfile() ? "NAI" : "OXI");
			addCell(row, colNum++, textStyle, p.getProfileURL());
			addCell(row, colNum++, intStyle, cvFile != null ? cvFile : null);
			addCell(row, colNum++, textStyle, p.getSubject() != null ? p.getSubject().getName() : null);
			addCell(row, colNum++, textStyle, p.getSpeakingGreek() != null ? (p.getSpeakingGreek() ? "NAI" : "OXI") : null);
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

	/****************************/

	private Map<Long, Map<FileType, Set<Long>>> getCandidateFilesData() {
		List<Object[]> files = em.createQuery(
			"select c.id, cf.type, cf.id from CandidateFile cf " +
				"join cf.candidate c " +
				"where cf.deleted = false ", Object[].class).getResultList();
		Map<Long, Map<FileType, Set<Long>>> filesMap = new HashMap<Long, Map<FileType, Set<Long>>>();
		for (Object[] file : files) {
			Long candidateId = (Long) file[0];
			FileType type = (FileType) file[1];
			Long fileId = (Long) file[2];
			Map<FileType, Set<Long>> entry = filesMap.get(candidateId);
			if (entry == null) {
				entry = new HashMap<FileType, Set<Long>>();
				filesMap.put(candidateId, entry);
			}
			Set<Long> entryFiles = entry.get(type);
			if (entryFiles == null) {
				entryFiles = new HashSet<Long>();
				entry.put(type, entryFiles);
			}
			entryFiles.add(fileId);
		}
		return filesMap;
	}

	private Map<Long, Long> getCandidateCandidaciesData() {
		Map<Long, Long> result = new HashMap<Long, Long>();
		List<Object[]> data = em.createQuery(
			"select c.candidate.id, count(c.id) " +
				"from Candidacy c " +
				"group by c.candidate.id ", Object[].class)
			.getResultList();
		for (Object[] row : data) {
			Long cid = (Long) row[0];
			Long candidacies = (Long) row[1];
			result.put(cid, candidacies);
		}
		return result;
	}

	private List<Candidate> getCandidateData() {
		List<Candidate> data = em.createQuery(
			"select c " +
				"from Candidate c " +
				"join fetch c.user u " +
				"where c.user.id not in (select p.user.id from Professor p) ", Candidate.class)
			.getResultList();
		return data;
	}

	public InputStream createCandidateExcel() {
		//1. Get Data
		List<Candidate> candidates = getCandidateData();
		Map<Long, Map<FileType, Set<Long>>> filesMap = getCandidateFilesData();
		Map<Long, Long> candidaciesMap = getCandidateCandidaciesData();
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
			Map<FileType, Set<Long>> cFiles = filesMap.containsKey(c.getId()) ? filesMap.get(c.getId()) : new HashMap<FileType, Set<Long>>();
			Set<Long> idFile = cFiles.get(FileType.TAYTOTHTA);
			Set<Long> militaryFile = cFiles.get(FileType.BEBAIWSH_STRATIOTIKIS_THITIAS);
			Set<Long> formaFile = cFiles.get(FileType.FORMA_SYMMETOXIS);
			Set<Long> cvFile = cFiles.get(FileType.BIOGRAFIKO);
			Set<Long> degreesFiles = cFiles.get(FileType.PTYXIO);
			Set<Long> publicationsFiles = cFiles.get(FileType.DIMOSIEYSI);

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
			addCell(row, colNum++, dateStyle, c.getStatusDate());
			addCell(row, colNum++, textStyle, idFile != null ? idFile.toString() : null);
			addCell(row, colNum++, textStyle, militaryFile != null ? militaryFile.toString() : null);
			addCell(row, colNum++, textStyle, formaFile != null ? formaFile.toString() : null);
			addCell(row, colNum++, textStyle, cvFile != null ? cvFile.toString() : null);
			addCell(row, colNum++, intStyle, degreesFiles != null ? degreesFiles.size() : null);
			addCell(row, colNum++, intStyle, publicationsFiles != null ? publicationsFiles.size() : null);
			addCell(row, colNum++, intStyle, candidaciesMap.containsKey(c.getId()) ? candidaciesMap.get(c.getId()) : null);
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
			"select im from InstitutionManager im " +
				"join fetch im.user u " +
				"left join fetch im.institution i ", InstitutionManager.class)
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
			addCell(row, colNum++, textStyle, im.getVerificationAuthority() != null ? im.getVerificationAuthority().toString() : "");
			addCell(row, colNum++, textStyle, im.getVerificationAuthorityName());
			addCell(row, colNum++, textStyle, im.getAlternateFirstname("el"));
			addCell(row, colNum++, textStyle, im.getAlternateLastname("el"));
			addCell(row, colNum++, textStyle, im.getAlternateFathername("el"));
			addCell(row, colNum++, textStyle, im.getAlternateFirstname("en"));
			addCell(row, colNum++, textStyle, im.getAlternateLastname("en"));
			addCell(row, colNum++, textStyle, im.getAlternateFathername("en"));
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo() != null ? im.getAlternateContactInfo().getEmail() : "");
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo() != null ? im.getAlternateContactInfo().getMobile() : "");
			addCell(row, colNum++, textStyle, im.getAlternateContactInfo() != null ? im.getAlternateContactInfo().getPhone() : "");

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
			"select ia from InstitutionAssistant ia " +
				"join fetch ia.user u " +
				"join fetch ia.manager im " +
				"join fetch im.user um " +
				"left join fetch im.institution imi ", InstitutionAssistant.class)
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
		addCell(row, colNum++, titleStyle, "ProfileStatus");
		addCell(row, colNum++, titleStyle, "ProfileStatusDate");
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

	private List<Register> getRegisterData() {
		List<Register> data = em.createQuery(
			"select r from Register r " +
				"where permanent = true ", Register.class)
			.getResultList();
		return data;
	}

	private Map<Long, Long> getRegisterRegisterMemberData() {
		Map<Long, Long> result = new HashMap<Long, Long>();
		List<Object[]> data = em.createQuery(
			"select rm.register.id, count(rm.id) " +
				"from RegisterMember rm " +
				"group by rm.register.id ", Object[].class)
			.getResultList();
		for (Object[] row : data) {
			Long cid = (Long) row[0];
			Long candidacies = (Long) row[1];
			result.put(cid, candidacies);
		}
		return result;
	}

	public InputStream createRegisterExcel() {
		//1. Get Data
		List<Register> registers = getRegisterData();
		Map<Long, Long> membersMap = getRegisterRegisterMemberData();
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

		for (Register r : registers) {
			Long membersCount = membersMap.containsKey(r.getId()) ? membersMap.get(r.getId()) : 0;

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
			"select rm from RegisterMember rm " +
				"join fetch rm.register r " +
				"join fetch r.subject rsub " +
				"join fetch rm.professor p " +
				"join fetch p.user u " +
				"left join fetch p.department dep " +
				"left join fetch dep.school sch " +
				"left join fetch sch.institution inst " +
				"left join fetch p.subject sub " +
				"left join fetch p.fekSubject fsub ", RegisterMember.class)
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

		Iterator<RegisterMember> iterator = members.iterator();
		while (iterator.hasNext()) {
			RegisterMember rm = iterator.next();
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

			iterator.remove();
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
			"select pcm from PositionCommitteeMember pcm " +
				"join fetch pcm.registerMember rm " +
				"join fetch rm.register r " +
				"join fetch rm.professor p " +
				"join fetch r.subject rsub " +
				"join fetch p.user u " +
				"left join fetch p.department dep " +
				"left join fetch dep.school sch " +
				"left join fetch sch.institution inst " +
				"left join fetch p.subject sub " +
				"left join fetch p.fekSubject fsub ", PositionCommitteeMember.class)
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
			"select e from PositionEvaluator e " +
				"join fetch e.registerMember rm " +
				"join fetch rm.register r " +
				"join fetch r.subject rsub " +
				"join fetch rm.professor p " +
				"join fetch p.user u " +
				"left join fetch p.department dep " +
				"left join fetch dep.school sch " +
				"left join fetch sch.institution inst " +
				"left join fetch p.subject sub " +
				"left join fetch p.fekSubject fsub ", PositionEvaluator.class)
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
			"select cy from Candidacy cy " +
				"join fetch cy.candidate c " +
				"join fetch c.user u " +
				"join fetch cy.candidacies pc " +
				"join fetch pc.position p " +
				"join fetch p.department dep " +
				"join fetch dep.school sch " +
				"join fetch sch.institution inst ", Candidacy.class)
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

	public Statistics getStatistics() {
		Statistics stats = new Statistics();

		// 1. Users
		List<Object[]> userStats = em.createQuery(
			"select role.discriminator, usr.status, role.status, count(usr.id) from Role role " +
				"join role.user usr " +
				"where (role.discriminator != :candidate and role.discriminator != :administrator ) " +
				"or (role.discriminator = :candidate and role.user.id not in (select p.user.id from Professor p) )" +
				"group by role.discriminator, usr.status, role.status " +
				"order by role.discriminator, usr.status, role.status ", Object[].class)
			.setParameter("candidate", RoleDiscriminator.CANDIDATE)
			.setParameter("administrator", RoleDiscriminator.ADMINISTRATOR)
			.getResultList();
		for (RoleDiscriminator discriminator : RoleDiscriminator.values()) {
			if (discriminator.equals(RoleDiscriminator.ADMINISTRATOR)) {
				continue;
			}
			Map<String, Long> user = new HashMap<String, Long>();
			user.put("registered", 0l);
			user.put("verified", 0l);
			stats.getUsers().put(discriminator.toString(), user);
		}
		for (Object[] usr : userStats) {
			String discriminator = ((RoleDiscriminator) usr[0]).toString();
			UserStatus userStatus = (UserStatus) usr[1];
			RoleStatus roleStatus = (RoleStatus) usr[2];
			Long count = (Long) usr[3];
			Map<String, Long> user = stats.getUsers().get(discriminator);
			Long registeredCount = user.get("registered");
			Long verifiedCount = user.get("verified");
			if (userStatus.equals(UserStatus.ACTIVE) &&
				roleStatus.equals(RoleStatus.ACTIVE)) {
				verifiedCount += count;
			}
			registeredCount += count;
			user.put("registered", registeredCount);
			user.put("verified", verifiedCount);
		}

		// 2. Professors
		List<Object[]> pdStats = em.createQuery(
			"select pd.rank.category, usr.status, pd.status, count(usr.id) from ProfessorDomestic pd " +
				"join pd.user usr " +
				"group by pd.rank.category, pd.status, usr.status " +
				"order by pd.rank.category, pd.status, usr.status ", Object[].class)
			.getResultList();

		for (Rank.Category category : Rank.Category.values()) {
			Map<String, Long> prof = new HashMap<String, Long>();
			prof.put("registered", 0l);
			prof.put("verified", 0l);
			stats.getProfessors().put(category.toString(), prof);
		}
		for (Object[] prof : pdStats) {
			String category = ((Rank.Category) prof[0]).toString();
			UserStatus userStatus = (UserStatus) prof[1];
			RoleStatus roleStatus = (RoleStatus) prof[2];
			Long count = (Long) prof[3];
			Map<String, Long> professor = stats.getProfessors().get(category);
			Long registeredCount = professor.get("registered");
			Long verifiedCount = professor.get("verified");
			if (userStatus.equals(UserStatus.ACTIVE) &&
				roleStatus.equals(RoleStatus.ACTIVE)) {
				verifiedCount += count;
			}
			registeredCount += count;
			professor.put("registered", registeredCount);
			professor.put("verified", verifiedCount);
		}

		// 3. IRF
		Long irfStats = em.createQuery(
			"select count(irf) from InstitutionRegulatoryFramework irf " +
				"where irf.permanent = true ", Long.class)
			.getSingleResult();
		stats.setInstitutionRegulatoryFrameworks(irfStats);

		// 4. Registers
		Long registerStats = em.createQuery(
			"select count(r) from Register r " +
				"where r.permanent = true ", Long.class)
			.getSingleResult();
		stats.setRegisters(registerStats);

		// 5. Positions
		List<Object[]> positionStats = em.createQuery(
			"select ph.status, count(pos.id) from Position pos " +
				"join pos.phase ph " +
				"where pos.permanent = true " +
				"group by ph.status " +
				"order by ph.status ", Object[].class)
			.getResultList();
		for (PositionStatus status : PositionStatus.values()) {
			stats.getPositions().put(status.toString(), 0l);
		}
		Long sum = 0L;
		for (Object[] pos : positionStats) {
			String status = ((PositionStatus) pos[0]).toString();
			Long count = (Long) pos[1];
			stats.getPositions().put(status, stats.getPositions().get(status) + count);
			sum += count;
		}
		stats.getPositions().put("TOTAL", sum);

		return stats;
	}
}
