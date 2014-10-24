package gr.grnet.dep.service.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.InstitutionManager;
import org.apache.commons.io.IOUtils;

import javax.ejb.EJBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfUtil {

	private static final Logger logger = Logger.getLogger(PdfUtil.class.getName());

	private static final float BASE_FONT_SIZE = 10.0f;

	public static InputStream generateFormaAllagisIM(InstitutionManager im) {
		try {
			FontFactory.register("gr/grnet/dep/service/util/resources/arial.ttf");
			FontFactory.register("gr/grnet/dep/service/util/resources/arialbd.ttf");
			Font normalFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BASE_FONT_SIZE);
			Font boldFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, normalFont.getSize(), Font.BOLD);
			Font largerBoldFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, (float) 1.2 * normalFont.getSize(), Font.BOLD);

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date now = new Date();

			Document doc = new Document();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PdfWriter.getInstance(doc, output);

			doc.open();

			Image logoApella = Image.getInstance(loadImage("logo_apella.png"));
			logoApella.scaleToFit(1200, 120);
			logoApella.setAlignment(Element.ALIGN_RIGHT);
			doc.add(logoApella);

			Paragraph p = new Paragraph("Αριθμός Βεβαίωσης:  " + im.getUser().getId() + " / " + sdf.format(now), boldFont);
			p.setAlignment(Element.ALIGN_RIGHT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Προς το Εθνικό Δίκτυο", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Έρευνας και Τεχνολογίας", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("FAX: 210-7724396, 210-7724397", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Βεβαίωση Συμμετοχής Διαχειριστή Ιδρύματος στην Απέλλα", largerBoldFont);
			p.setAlignment(Element.ALIGN_CENTER);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			PdfPTable institutionTable = new PdfPTable(2);
			institutionTable.setWidthPercentage(100);
			institutionTable.setWidths(new float[]{30, 70});
			institutionTable.addCell(createCell(new Phrase("ΙΔΡΥΜΑ", normalFont), Element.ALIGN_LEFT, 1));
			institutionTable.addCell(createCell(new Phrase(im.getInstitution().getName().get("el"), normalFont), Element.ALIGN_LEFT, 1));
			switch (im.getVerificationAuthority()) {
				case DEAN:
					institutionTable.addCell(createCell(new Phrase("ΟΝ/ΜΟ ΠΡΥΤΑΝΗ", normalFont), Element.ALIGN_LEFT, 1));
					break;
				case PRESIDENT:
					institutionTable.addCell(createCell(new Phrase("ΟΝ/ΜΟ ΠΡΟΕΔΡΟΥ", normalFont), Element.ALIGN_LEFT, 1));
					break;
			}
			institutionTable.addCell(createCell(new Phrase(im.getVerificationAuthorityName(), normalFont), Element.ALIGN_LEFT, 1));
			doc.add(institutionTable);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Βεβαιώνεται ότι το ως άνω Ίδρυμα ορίζει ως Διαχειριστή Ιδρύματος στο πρόγραμμα ΑΠΕΛΛΑ τον/την " +
					im.getUser().getBasicInfo().getFirstname() +
					" " +
					im.getUser().getBasicInfo().getLastname() +
					" με e-mail " +
					im.getUser().getContactInfo().getEmail() +
					", τηλέφωνο " +
					im.getUser().getContactInfo().getPhone() +
					" και όνομα χρήστη " +
					im.getUser().getUsername()
					+ "."
					, normalFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Αναπληρωτής Διαχειριστής Ιδρύματος ορίζεται ο/η " +
					im.getAlternateBasicInfo().getFirstname() +
					" " +
					im.getAlternateBasicInfo().getLastname() +
					" με e-mail " +
					im.getAlternateContactInfo().getEmail() +
					", τηλέφωνο " +
					im.getAlternateContactInfo().getPhone()
					+ ".",
					normalFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			PdfPTable signTable = new PdfPTable(3);
			signTable.setWidthPercentage(100);
			signTable.setWidths(new float[]{33, 33, 33});
			signTable.addCell(createCell(new Phrase("__/__/____", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase(" ", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase(" ", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase("ΗΜΕΡΟΜΗΝΙΑ", normalFont), Element.ALIGN_CENTER, 0));
			switch (im.getVerificationAuthority()) {
				case DEAN:
					signTable.addCell(createCell(new Phrase("Υπογραφή Πρύτανη", normalFont), Element.ALIGN_CENTER, 0));
					break;
				case PRESIDENT:
					signTable.addCell(createCell(new Phrase("Υπογραφή Προέδρου", normalFont), Element.ALIGN_CENTER, 0));
					break;
			}
			signTable.addCell(createCell(new Phrase("Σφραγίδα Ιδρύματος", normalFont), Element.ALIGN_CENTER, 0));
			doc.add(signTable);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			doc.add(createLogoTable());

			doc.close();
			output.close();
			return new ByteArrayInputStream(output.toByteArray());
		} catch (DocumentException e) {
			logger.log(Level.SEVERE, "", e);
			throw new EJBException(e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
			throw new EJBException(e);
		}
	}

	public static InputStream generateFormaYpopsifiou(Candidate candidate) {
		try {
			FontFactory.register("gr/grnet/dep/service/util/resources/arial.ttf");
			FontFactory.register("gr/grnet/dep/service/util/resources/arialbd.ttf");
			Font normalFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BASE_FONT_SIZE);
			Font boldFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, normalFont.getSize(), Font.BOLD);
			Font largerBoldFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, (float) 1.2 * normalFont.getSize(), Font.BOLD);

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date now = new Date();

			Document doc = new Document();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PdfWriter.getInstance(doc, output);

			doc.open();

			Image logoApella = Image.getInstance(loadImage("logo_apella.png"));
			logoApella.scaleToFit(1200, 120);
			logoApella.setAlignment(Element.ALIGN_RIGHT);
			doc.add(logoApella);

			Paragraph p = new Paragraph("Αριθμός Βεβαίωσης:  " + candidate.getUser().getId() + " / " + sdf.format(now), boldFont);
			p.setAlignment(Element.ALIGN_RIGHT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Προς το Εθνικό Δίκτυο", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Έρευνας και Τεχνολογίας", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("FAX: 210-7724396, 210-7724397", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Βεβαίωση Συμμετοχής Υποψηφίου στην Απέλλα", largerBoldFont);
			p.setAlignment(Element.ALIGN_CENTER);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("Βεβαιώνεται ότι ο " +
					candidate.getUser().getBasicInfo().getFirstname() +
					" (" +
					candidate.getUser().getBasicInfoLatin().getFirstname() +
					") " +
					candidate.getUser().getBasicInfo().getLastname() +
					" (" +
					candidate.getUser().getBasicInfoLatin().getLastname() +
					") " +
					" με Α.Δ.Τ. "
					+ candidate.getUser().getIdentification() +
					", e-mail "
					+ candidate.getUser().getContactInfo().getEmail() +
					" και τηλέφωνο " +
					candidate.getUser().getContactInfo().getMobile() +
					" συμμετέχει στο πρόγραμμα \"Απέλλα - Σύστημα Εκλογής και Εξέλιξης Μελών ΔΕΠ\" με το Όνομα Χρήστη "
					+ candidate.getUser().getUsername()
					+ ".",
					normalFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph("O συμμετέχων δηλώνει υπεύθυνα ότι αποδέχεται τους όρους και τις προϋποθέσεις του προγράμματος " +
					"\"Απέλλα - Σύστημα Εκλογής και Εξέλιξης Μελών ΔΕΠ\", όπως κάθε φορά ισχύουν.", normalFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			PdfPTable signTable = new PdfPTable(2);
			signTable.setWidthPercentage(100);
			signTable.setWidths(new float[]{50, 50});
			signTable.addCell(createCell(new Phrase(" ", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase("Ο/Η Συμμετέχων", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase("__/__/____", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase(" ", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase("ΗΜΕΡΟΜΗΝΙΑ", normalFont), Element.ALIGN_CENTER, 0));
			signTable.addCell(createCell(new Phrase("Υπογραφή", normalFont), Element.ALIGN_CENTER, 0));
			doc.add(signTable);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			p = new Paragraph(" ", boldFont);
			p.setAlignment(Element.ALIGN_LEFT);
			doc.add(p);

			doc.add(createLogoTable());

			doc.close();
			output.close();
			return new ByteArrayInputStream(output.toByteArray());
		} catch (DocumentException e) {
			logger.log(Level.SEVERE, "", e);
			throw new EJBException(e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
			throw new EJBException(e);
		}
	}

	private static PdfPCell createCell(Phrase p, int align, int borderWidth) {
		PdfPCell cell = new PdfPCell(p);
		cell.setBorderWidth(borderWidth);
		cell.setHorizontalAlignment(align);
		cell.setPadding(10);
		return cell;
	}

	private static PdfPCell createImageCell(Image img, int align, int borderWidth) {
		PdfPCell cell = new PdfPCell(img);
		cell.setBorderWidth(borderWidth);
		cell.setHorizontalAlignment(align);
		cell.setPadding(10);
		return cell;
	}

	private static PdfPTable createLogoTable() throws MalformedURLException, IOException, DocumentException {
		// Load resources
		Font normalFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BASE_FONT_SIZE);
		Image logoMinedu = Image.getInstance(loadImage("logo_minedu.png"));
		logoMinedu.scaleToFit(1200, 36);
		Image logoGRnet = Image.getInstance(loadImage("logo_grnet.png"));
		logoGRnet.scaleToFit(1200, 36);
		Image logoEU = Image.getInstance(loadImage("logo_eu.jpg"));
		logoEU.scaleToFit(1200, 36);
		Image logoEspa = Image.getInstance(loadImage("logo_espa.jpg"));
		logoEspa.scaleToFit(1200, 36);
		Image logoDM = Image.getInstance(loadImage("logo_ep.jpg"));
		logoDM.scaleToFit(1200, 36);
		// Create table
		PdfPTable logoTable = new PdfPTable(5);
		logoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		logoTable.setWidths(new float[]{27, 28, 12, 15, 18});

		logoTable.addCell(createImageCell(logoMinedu, Element.ALIGN_CENTER, 0));
		logoTable.addCell(createImageCell(logoGRnet, Element.ALIGN_CENTER, 0));
		logoTable.addCell(createImageCell(logoEU, Element.ALIGN_CENTER, 0));
		logoTable.addCell(createImageCell(logoDM, Element.ALIGN_CENTER, 0));
		logoTable.addCell(createImageCell(logoEspa, Element.ALIGN_CENTER, 0));
		PdfPCell cell = createCell(new Phrase("Με τη συγχρηματοδότηση της Ελλάδας και της Ευρωπαϊκής Ένωσης", normalFont), Element.ALIGN_CENTER, 0);
		cell.setColspan(5);
		logoTable.addCell(cell);

		return logoTable;
	}

	private static byte[] loadImage(String img) {
		InputStream is = Image.class.getClassLoader().getResourceAsStream("gr/grnet/dep/service/util/resources/" + img);
		try {
			return IOUtils.toByteArray(is);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot Read image for pdf generation", e);
		}
		return new byte[0];
	}

}
