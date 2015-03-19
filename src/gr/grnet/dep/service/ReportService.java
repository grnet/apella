package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.SectorName;
import gr.grnet.dep.service.model.User.UserStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class ReportService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static final Logger logger = Logger.getLogger(ReportService.class.getName());

	public List<Professor> getProfessorData() {
		List<Professor> data = em.createQuery(
				"select p " +
						"from Professor p " +
						"where p.status = :roleStatus and p.user.status = :userStatus ", Professor.class)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.getResultList();
		return data;
	}

	public static InputStream createProfessorDataExcel(List<Professor> professors) {

		Workbook wb = new SXSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		DataFormat df = wb.createDataFormat();

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("Arial");
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(df.getFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(CellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		Cell cell = null;

		row = sheet.createRow(rowNum++);

		cell = row.createCell(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(new Date()));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);

		cell = row.createCell(colNum++);
		cell.setCellValue("Όνομα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Επώνυμο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός");
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
		cell.setCellValue("Γνωστικό Αντικείμενο");
		cell.setCellStyle(titleStyle);

		for (Professor p : professors) {
			row = sheet.createRow(rowNum++);

			colNum = 0;
			cell = row.createCell(colNum++);
			cell.setCellValue(p.getUser().getFirstname("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(p.getUser().getLastname("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(p.getUser().getId());
			cell.setCellStyle(intStyle);

			switch (p.getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) p;

					cell = row.createCell(colNum++);
					cell.setCellValue("Καθηγητής Ημεδαπής");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getRank().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getInstitution().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getFekSubject() != null ? professorDomestic.getFekSubject().getName() : professorDomestic.getSubject().getName());
					cell.setCellStyle(textStyle);

					break;

				case PROFESSOR_FOREIGN:
					ProfessorForeign professorForeign = (ProfessorForeign) p;

					cell = row.createCell(colNum++);
					cell.setCellValue("Καθηγητής Αλλοδαπής");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorForeign.getRank().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorForeign.getInstitution());
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
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

	public List<Object[]> getRegisterExportData(Long registerId) {
		List<Object[]> data = em.createQuery(
				"select rm, ( " +
						"	select count(pcm.id) from PositionCommitteeMember pcm" +
						"	where pcm.registerMember.professor.id = rm.professor.id " +
						"	and pcm.committee.position.phase.status = :statusEPILOGI " +
						"), ( " +
						"	select count(pcm.id) from PositionCommitteeMember pcm " +
						"	where pcm.registerMember.professor.id = rm.professor.id " +
						") " +
						"from RegisterMember rm " +
						"join fetch rm.professor p " +
						"where rm.register.id = :registerId " +
						"and rm.deleted = false ", Object[].class)
				.setParameter("registerId", registerId)
				.setParameter("statusEPILOGI", PositionStatus.EPILOGI)
				.getResultList();
		return data;
	}

	public static InputStream createRegisterExportExcel(Register register, List<Object[]> registerData) {

		Workbook wb = new SXSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		DataFormat df = wb.createDataFormat();

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("Arial");
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(df.getFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(CellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		int rowNum = 0;
		int colNum = 0;

		Row row = null;
		Cell cell = null;

		row = sheet.createRow(rowNum++);

		cell = row.createCell(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(new Date()));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);

		cell = row.createCell(0);
		cell.setCellValue("Γνωστικό Αντικείμενο: " + (register.getSubject() != null ? register.getSubject().getName() : ""));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

		row = sheet.createRow(rowNum++);

		cell = row.createCell(colNum++);
		cell.setCellValue("Όνομα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Επώνυμο");
		cell.setCellStyle(titleStyle);

        cell = row.createCell(colNum++);
        cell.setCellValue("Κωδικός");
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
			cell.setCellValue(member.getProfessor().getUser().getFirstname("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(member.getProfessor().getUser().getLastname("el"));
			cell.setCellStyle(textStyle);

            cell = row.createCell(colNum++);
            cell.setCellValue(member.getProfessor().getUser().getId());
            cell.setCellStyle(textStyle);

			switch (member.getProfessor().getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					ProfessorDomestic professorDomestic = (ProfessorDomestic) member.getProfessor();

					cell = row.createCell(colNum++);
					cell.setCellValue("Καθηγητής Ημεδαπής");
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getRank().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getInstitution().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getSchool().getName().get("el"));
					cell.setCellStyle(textStyle);

					cell = row.createCell(colNum++);
					cell.setCellValue(professorDomestic.getDepartment().getName().get("el"));
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
					cell.setCellValue(professorForeign.getRank().getName().get("el"));
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

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			wb.write(output);
			output.close();

			return new ByteArrayInputStream(output.toByteArray());
		} catch (IOException e) {
			throw new EJBException(e);
		}
	}

	public List<Position> getPositionsExportData(Long institutionId) {
		String query = "select p from Position p " +
				"left join fetch p.phase ph " +
				"left join fetch ph.candidacies ca " +
				"left join fetch ph.committee co " +
				"left join fetch ph.evaluation ev " +
				"left join fetch ph.nomination no " +
				"left join fetch ph.complementaryDocuments cd " +
				"where p.permanent = true ";
		if (institutionId != null) {
			query = query.concat("and p.department.school.institution.id = :institutionId");
		}

		TypedQuery<Position> q = em.createQuery(query, Position.class);
		if (institutionId != null) {
			q.setParameter("institutionId", institutionId);
		}

		List<Position> data = q.getResultList();
		return data;
	}

	public static InputStream createPositionsExportExcel(List<Position> positionsData) {

		Workbook wb = new SXSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		DataFormat df = wb.createDataFormat();

		CellStyle titleStyle = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("Arial");
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleStyle.setFont(font);
		titleStyle.setWrapText(false);

		DecimalFormat decimalFormat = new DecimalFormat("00000000000");

		CellStyle textStyle = wb.createCellStyle();
		textStyle.setWrapText(false);

		CellStyle floatStyle = wb.createCellStyle();
		floatStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		floatStyle.setDataFormat(df.getFormat("0.00"));

		CellStyle intStyle = wb.createCellStyle();
		intStyle.setAlignment(CellStyle.ALIGN_RIGHT);

		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

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
			cell.setCellValue(decimalFormat.format(position.getId()));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getName());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getSchool().getInstitution().getName().get("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getSchool().getName().get("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDepartment().getName().get("el"));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getDescription());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(position.getSubject().getName());
			cell.setCellStyle(textStyle);

			SectorName sectorName = position.getSector().getName().get("el");
			cell = row.createCell(colNum++);
			cell.setCellValue(sectorName.getArea() + " / " + sectorName.getSubject());
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

			if (position.getPhase().getNomination() != null) {
				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getNominationCommitteeConvergenceDate() != null) {
					cell.setCellValue(position.getPhase().getNomination().getNominationCommitteeConvergenceDate());
				}
				cell.setCellStyle(dateStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getNominatedCandidacy() != null) {
					cell.setCellValue(position.getPhase().getNomination().getNominatedCandidacy().getCandidate().getUser().getFullName("el"));
				}
				cell.setCellStyle(textStyle);

				cell = row.createCell(colNum++);
				if (position.getPhase().getNomination().getSecondNominatedCandidacy() != null) {
					cell.setCellValue(position.getPhase().getNomination().getSecondNominatedCandidacy().getCandidate().getUser().getFullName("el"));
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
