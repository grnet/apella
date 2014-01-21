package gr.grnet.dep.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

@Stateless
public class ReportService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static final Logger logger = Logger.getLogger(ReportService.class.getName());

	private Object[] getRegisterExportData(Long registerId) {
		// TODO:
		return new Object[0];
	}

	public InputStream createRegisterExportExcel(Long registerId) {
		//1. Get Data
		Object[] registerData = getRegisterExportData(registerId);

		//2. Create XLS
		// TODO:
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
		/*
		Row row = sheet.createRow(rowNum++);
		Cell cell = row.createCell(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(fromDate) + " έως " + sdf.format(toDate));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		
		cell = row.createCell(colNum++);
		cell.setCellValue("Ίδρυμα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Σχολή Φοιτητή");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Σχολή Μαθήματος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός Μαθήματος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Μάθημα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός Βιβλίου");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Βιβλίο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Πλήθος Παραδόσεων");
		cell.setCellStyle(titleStyle);

		for (Object[] o : data) {
			Course c = (Course) o[0];
			Book b = (Book) o[1];
			Secretariat sec = (Secretariat) o[2];
			Long deliveryCount = (Long) o[3];

			row = sheet.createRow(rowNum++);
			colNum = 0;

			cell = row.createCell(colNum++);
			cell.setCellValue(sec.getSecretaryAcademic().getInstitution());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(sec.getSecretaryAcademic().getDepartment());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getSecretariat().getSecretaryAcademic().getDepartment());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getCode());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getTitle());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(b.getId().toString());
			cell.setCellStyle(intStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(createBookSummary(b));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(deliveryCount.toString());
			cell.setCellStyle(intStyle);
		}
		*/
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

	private Object[] getPositionsExportData(Long institutionId) {
		// TODO:
		return new Object[0];
	}

	public InputStream createPositionsExportExcel(Long institutionId) {
		//1. Get Data
		Object[] positionsData = getPositionsExportData(institutionId);

		//2. Create XLS
		// TODO:
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
		/*
		Row row = sheet.createRow(rowNum++);
		Cell cell = row.createCell(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		cell.setCellValue("Ημερομηνία: " + sdf.format(fromDate) + " έως " + sdf.format(toDate));
		cell.setCellStyle(titleStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

		row = sheet.createRow(rowNum++);
		
		cell = row.createCell(colNum++);
		cell.setCellValue("Ίδρυμα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Σχολή Φοιτητή");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Τμήμα/Σχολή Μαθήματος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός Μαθήματος");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Μάθημα");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Κωδικός Βιβλίου");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Βιβλίο");
		cell.setCellStyle(titleStyle);

		cell = row.createCell(colNum++);
		cell.setCellValue("Πλήθος Παραδόσεων");
		cell.setCellStyle(titleStyle);

		for (Object[] o : data) {
			Course c = (Course) o[0];
			Book b = (Book) o[1];
			Secretariat sec = (Secretariat) o[2];
			Long deliveryCount = (Long) o[3];

			row = sheet.createRow(rowNum++);
			colNum = 0;

			cell = row.createCell(colNum++);
			cell.setCellValue(sec.getSecretaryAcademic().getInstitution());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(sec.getSecretaryAcademic().getDepartment());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getSecretariat().getSecretaryAcademic().getDepartment());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getCode());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(c.getTitle());
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(b.getId().toString());
			cell.setCellStyle(intStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(createBookSummary(b));
			cell.setCellStyle(textStyle);

			cell = row.createCell(colNum++);
			cell.setCellValue(deliveryCount.toString());
			cell.setCellStyle(intStyle);
		}
		*/
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
