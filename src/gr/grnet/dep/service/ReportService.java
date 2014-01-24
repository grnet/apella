package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.RegisterMember;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
public class ReportService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static final Logger logger = Logger.getLogger(ReportService.class.getName());

	private List<Object[]> getRegisterExportData(Long registerId) {
		@SuppressWarnings("unchecked")
		List<Object[]> data = em.createQuery(
			"select rm, " +
				"(select count(pcm.id) from PositionCommitteeMember pcm where pcm.registerMember.professor.id = rm.professor.id " +
				"	and pcm.committee.position.phase.status = :statusEPILOGI), " +
				"(select count(pcm.id) from PositionCommitteeMember pcm where pcm.registerMember.professor.id = rm.professor.id) " +
				"from RegisterMember rm " +
				"where rm.register.id = :registerId ")
			.setParameter("registerId", registerId)
			.setParameter("statusEPILOGI", PositionStatus.EPILOGI)
			.getResultList();
		return data;
	}

	public InputStream createRegisterExportExcel(Long registerId) {
		//1. Get Data
		List<Object[]> registerData = getRegisterExportData(registerId);
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

		Row row = sheet.createRow(rowNum++);

		Cell cell = row.createCell(0);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(new Date()));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ονοματεπώνυμο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Προφίλ Χρήστη");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Βαθμίδα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ίδρυμα/Ερευνητικό Κέντρο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Σχολή/-");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Ινστιτούτο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Εσωτερικό/Εξωτερικό Μέλος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("ΦΕΚ");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Γνωστικό Αντικείμενο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ενεργές Επιτροπές");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Συμμετοχή σε Επιτροπές");
		cell.setCellStyle(titleStyle);

		for (Object[] o : registerData) {
			RegisterMember member = (RegisterMember) o[0];
			Long activeCommittees = (Long) o[1];
			Long allCommittees = (Long) o[2];

			row = sheet.createRow(rowNum++);
			colNum = 0;

			cell = row.createCell(colNum++);
			cell.setCellValue(member.getProfessor().getUser().getFullName());
			cell.setCellStyle(textStyle);

			switch (member.getProfessor().getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) member.getProfessor();

					cell = row.createCell(colNum++);
					cell.setCellValue("Καθηγητής Ημεδαπής");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getRank().getName());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getInstitution().getName());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getName());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getName());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(member.isExternal() ? "ΕΞΩΤΕΡΙΚΟ" : "ΕΣΩΤΕΡΙΚΟ");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getFek());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getFekSubject() != null ? professorDomestic.getFekSubject().getName() : professorDomestic.getSubject().getName());
					cell.setCellStyle(textStyle);

					break;

				case PROFESSOR_FOREIGN:
					ProfessorForeign professorForeign = (ProfessorForeign) member.getProfessor();

					cell = row.createCell(colNum++);
					cell.setCellValue("Καθηγητής Αλλοδαπής");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorForeign.getRank().getName());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorForeign.getInstitution());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(member.isExternal() ? "ΕΞΩΤΕΡΙΚΟ" : "ΕΣΩΤΕΡΙΚΟ");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorForeign.getSubject().getName());
					cell.setCellStyle(textStyle);

				default:
					// Won't happen
					break;
			}

			cell = row.createCell(colNum++);
			cell.setCellValue(activeCommittees);
			cell.setCellStyle(intStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(allCommittees);
			cell.setCellStyle(intStyle);
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

	private List<Position> getPositionsExportData(Long institutionId) {
		String query = "select p from Position p " +
			"left join fetch p.phase ph " +
			"left join fetch ph.candidacies ca " +
			"left join fetch ph.committee co " +
			"left join fetch ph.evaluation ev " +
			"left join fetch ph.nomination no " +
			"left join fetch ph.complementaryDocuments cd ";
		if (institutionId != null) {
			query = query.concat("where p.deparment.school.institution.id = :institutionId");
		}

		Query q = em.createQuery(query);
		if (institutionId != null) {
			q.setParameter("institutionId", institutionId);
		}

		@SuppressWarnings("unchecked")
		List<Position> data = q.getResultList();
		return data;
	}

	public InputStream createPositionsExportExcel(Long institutionId) {
		//1. Get Data
		List<Position> positionsData = getPositionsExportData(institutionId);

		//2. Create XLS
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		DecimalFormat df = new DecimalFormat("00000000000");

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

		Row row = sheet.createRow(rowNum++);
		Cell cell = row.createCell(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(new Date()));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός ΑΠΕΛΛΑ");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τίτλος Θέσης");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ίδρυμα/Ερευνητικό Κέντρο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Σχολή/-");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Ινστιτούτο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Περιγραφή");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Γνωστικό Αντικείμενο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Θεματική Περιοχή");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Φ.Ε.Κ.");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ημερομηνία ΦΕΚ");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κατάσταση Θέσης");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ημερομηνία Έναρξης Υποβολών");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ημερομηνία Λήξης Υποβολών");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Καταληκτική Ημερομηνία Δήλωσης Αξιολογητών από Υποψηφίους");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ημερομηνία Συνεδρίασης Επιτροπής");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Ημερομηνία Σύγκλησης Επιτροπής για Επιλογή");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Εκλεγείς");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Δεύτερος καταλληλότερος υποψήφιος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Φ.Ε.Κ. Πράξης Διορισμού");
		cell.setCellStyle(titleStyle);

		for (Position position : positionsData) {

			row = sheet.createRow(rowNum++);
			colNum = 0;

			cell = row.createCell(colNum++);
			cell.setCellValue(df.format(position.getId()));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getSchool().getInstitution().getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getSchool().getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDescription());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getSubject().getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getSector().getCategory() + " / " + position.getSector().getArea());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getFek());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			if (position.getFekSentDate() != null) {
				cell.setCellValue(position.getFekSentDate());
			}
			cell.setCellStyle(dateStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getPhase().getClientStatusInGreek());
			cell.setCellStyle(textStyle);

			if (position.getPhase().getCandidacies() != null) {
				cell = row.createCell(colNum++);
				if (position.getPhase().getCandidacies().getOpeningDate() != null) {
					cell.setCellValue(position.getPhase().getCandidacies().getOpeningDate());
				}
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getCandidacies().getClosingDate() != null) {
					cell.setCellValue(position.getPhase().getCandidacies().getClosingDate());
				}
				cell.setCellStyle(dateStyle);
			} else {
				cell = row.createCell(colNum++);
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				cell.setCellStyle(dateStyle);
			}

			if (position.getPhase().getCommittee() != null) {
				cell = row.createCell(colNum++);
				if (position.getPhase().getCommittee().getCandidacyEvalutionsDueDate() != null) {
					cell.setCellValue(position.getPhase().getCommittee().getCandidacyEvalutionsDueDate());
				}
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getCommittee().getCommitteeMeetingDate() != null) {
					cell.setCellValue(position.getPhase().getCommittee().getCommitteeMeetingDate());
				}
				cell.setCellStyle(dateStyle);
			} else {
				cell = row.createCell(colNum++);
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				cell.setCellStyle(dateStyle);
			}

			if (position.getPhase().getCommittee() != null) {
				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getNominationCommitteeConvergenceDate() != null) {
					cell.setCellValue(position.getPhase().getNomination().getNominationCommitteeConvergenceDate());
				}
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getNominatedCandidacy() != null) {
					cell.setCellValue(position.getPhase().getNomination().getNominatedCandidacy().getCandidate().getUser().getFullName());
				}
				cell.setCellStyle(textStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getSecondNominatedCandidacy() != null) {
					cell.setCellValue(position.getPhase().getNomination().getSecondNominatedCandidacy().getCandidate().getUser().getFullName());
				}
				cell.setCellStyle(textStyle);

				cell = row.createCell(colNum++);
				cell.setCellValue(position.getPhase().getNomination().getNominationFEK());
				cell.setCellStyle(textStyle);
			} else {
				cell = row.createCell(colNum++);
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				cell.setCellStyle(textStyle);

				cell = row.createCell(colNum++);
				cell.setCellStyle(textStyle);

				cell = row.createCell(colNum++);
				cell.setCellStyle(textStyle);
			}

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
