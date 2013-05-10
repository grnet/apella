package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.MailRecord;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

@Stateless
public class MailService {

	@Resource(name = "java:jboss/mail/Default")
	protected Session mailSession;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static final Logger logger = Logger.getLogger(MailService.class.getName());

	public void pushEmail(String aToEmailAddr, String aSubject, String aBody) {
		MailRecord mail = new MailRecord();
		mail.setToEmailAddr(aToEmailAddr);
		mail.setSubject(aSubject);
		mail.setBody(aBody);
		try {
			em.persist(mail);
			em.flush();
		} catch (PersistenceException e) {
			// Same email, already added to list
		}

	}

	public MailRecord popEmail() {
		try {
			MailRecord mail = (MailRecord) em.createQuery(
				"from MailRecord")
				.setMaxResults(1)
				.getSingleResult();
			em.remove(mail);
			em.flush();

			return mail;
		} catch (NoResultException e) {
			return null;
		}
	}

	public int sendPendingEmails() {
		int i = 0;

		MailRecord mail = popEmail();
		while (mail != null) {
			sendEmail(mail);
			mail = popEmail();
			i += 1;
		}

		return i;
	}

	public void sendEmail(String aToEmailAddr, String aSubject, String aBody) {
		logger.log(Level.INFO, "Sending email to " + aToEmailAddr + " " + aSubject + "\n" + aBody);
		try {
			MimeMessage message = new MimeMessage(mailSession);
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
			message.setSubject(aSubject, "UTF-8");
			message.setText(aBody, "UTF-8");
			Transport.send(message);
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Failed to send email to " + aToEmailAddr + " " + aSubject + "\n" + aBody, e);
		}
	}

	public void sendEmail(MailRecord mail) {
		sendEmail(mail.getToEmailAddr(), mail.getSubject(), mail.getBody());
	}

}
