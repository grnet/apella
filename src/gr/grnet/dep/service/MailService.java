package gr.grnet.dep.service;

import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.MailRecord;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

@Stateless
public class MailService {

	@Resource(name = "java:jboss/mail/Default")
	protected Session mailSession;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	private ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-mail", new Locale("el"));

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

			if (i % 10 == 0) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "Mail sent error: ", e.getMessage());
				}
			}
		}

		return i;
	}

	public void postEmail(String aToEmailAddr, String aSubjectKey, String aBodyKey, Map<String, String> parameters) {
		postEmail(aToEmailAddr, aSubjectKey, aBodyKey, parameters, false);
	}

	public void postEmail(String aToEmailAddr, String aSubjectKey, String aBodyKey, Map<String, String> parameters, boolean sendNow) {

		String aSubject = resources.getString(aSubjectKey);
		String aBody = resources.getString(aBodyKey);
		for (String key : parameters.keySet()) {
			String value = parameters.get(key);
			if (value == null || value.trim().isEmpty()) {
				value = "-";
			}
			aBody = aBody.replaceAll("\\[" + key + "\\]", value);
		}
		// Replace login with link
		aBody = aBody.replaceAll("\\[login\\]", "Είσοδο");

		// Add footer
		String aFooter = resources.getString("default.footer");
		aBody = aBody.concat("<br/><br/>").concat(aFooter);

		// Validate Email
		Connection qConn = null;
		javax.jms.Session session = null;
		MessageProducer sender = null;
		try {

			javax.naming.Context jndiCtx = new InitialContext();

			ConnectionFactory factory = (QueueConnectionFactory) jndiCtx.lookup("/ConnectionFactory");
			Queue queue = (Queue) jndiCtx.lookup("queue/EMailQ");
			qConn = factory.createConnection();
			session = qConn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			sender = session.createProducer(queue);

			MapMessage mailMessage = session.createMapMessage();
			mailMessage.setJMSCorrelationID("mail");
			mailMessage.setString("aToEmailAddr", aToEmailAddr);
			mailMessage.setString("aSubject", aSubject);
			mailMessage.setString("aBody", aBody);
			mailMessage.setBoolean("sendNow", sendNow);

			logger.log(Level.INFO, "Posting emailMessage to " + aToEmailAddr + " " + aSubject + "\n" + aBody);
			sender.send(mailMessage);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Message not published: ", e);
		} catch (JMSException e) {
			logger.log(Level.SEVERE, "Message not published: ", e);
		} finally {
			try {
				if (sender != null)
					sender.close();
				if (session != null)
					session.close();
				if (qConn != null)
					qConn.close();
			} catch (JMSException e) {
				logger.log(Level.WARNING, "Message not published: ", e);
			}
		}
	}

	public void sendEmail(String aToEmailAddr, String aSubject, String aBody) {
		logger.log(Level.INFO, "Sending email to " + aToEmailAddr + " " + aSubject + "\n" + aBody);
		try {
			MimeMessage message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress("apella@grnet.gr"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
			message.setSubject(aSubject, "UTF-8");

			MimeMultipart multipart = new MimeMultipart("alternative");
			MimeBodyPart htmlBodyPart = new MimeBodyPart();
			htmlBodyPart.setContent(aBody, "text/html; charset=utf-8");
			multipart.addBodyPart(htmlBodyPart);
			message.setContent(multipart);

			Transport.send(message);
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Failed to send email to " + aToEmailAddr + " " + aSubject + "\n" + aBody + ": " + e.getMessage());
		}
	}

	public void sendEmail(MailRecord mail) {
		sendEmail(mail.getToEmailAddr(), mail.getSubject(), mail.getBody());
	}

	public void sendLoginEmail(User u, boolean sendNow) {
		try {
			// Double Check here:
			if (u.getAuthenticationType().equals(AuthenticationType.EMAIL) &&
				u.getPermanentAuthToken() != null &&
				!u.getLoginEmailSent()) {

				String aToEmailAddr = u.getContactInfo().getEmail();
				String aSubject = resources.getString("login.email.subject");
				String aBody = resources.getString("login.email.body")
					.replaceAll("\\[firstname\\]", u.getBasicInfo().getFirstname())
					.replaceAll("\\[lastname\\]", u.getBasicInfo().getLastname())
					.replaceAll("\\[loginLink\\]", "<a href=\"" + getloginLink(u.getPermanentAuthToken()) + "\">Είσοδος</a>");
				if (sendNow) {
					sendEmail(aToEmailAddr, aSubject, aBody);
				} else {
					pushEmail(aToEmailAddr, aSubject, aBody);
				}
				logger.log(Level.INFO, "Sent login email to user with id " + u.getId() + " " + getloginLink(u.getPermanentAuthToken()));
			} else {
				logger.log(Level.INFO, "Skipped login email for user with id " + u.getId());
			}
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "Failed to send login email", e.getMessage());
		}
	}

	private String getloginLink(String token) throws UnsupportedEncodingException {
		return conf.getString("rest.url") +
			"/email/login?nextURL=" + conf.getString("home.url") + "/apella.html" +
			"&user=" + URLEncoder.encode(token, "UTF-8");
	}
}
