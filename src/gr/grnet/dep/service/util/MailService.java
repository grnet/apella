package gr.grnet.dep.service.util;

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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

@Stateless
public class MailService {

	@Resource(lookup = "java:jboss/mail/Default")
	protected Session mailSession;

	private static final Logger logger = Logger.getLogger(MailService.class.getName());

	private static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
			logger.log(Level.SEVERE, "", e);
		}
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

}
