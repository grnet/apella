package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.User;

import java.util.Locale;
import java.util.ResourceBundle;
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

	private void sendEmail(String aToEmailAddr, String aSubject, String aBody) {
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

	public void sendVerificationEmail(User u) {
		ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-mail", new Locale("el"));
		String title = resources.getString("verification.title");
		String body = resources.getString("verification.body")
			.replace("[username]", u.getUsername())
			.replace("[firstname]", u.getBasicInfo().getFirstname())
			.replace("[lastname]", u.getBasicInfo().getLastname())
			.replace("[verificationLink]", conf.getString("host") + "/depui/registration.html?#email=" + u.getUsername() + "&verification=" + u.getVerificationNumber());
		sendEmail(u.getContactInfo().getEmail(), title, body);
	}

	public void sendPasswordResetEmail(User u, String newPassword) {
		ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-mail", new Locale("el"));
		String title = resources.getString("password.reset.title");
		String body = resources.getString("password.reset.body")
			.replace("[username]", u.getUsername())
			.replace("[firstname]", u.getBasicInfo().getFirstname())
			.replace("[lastname]", u.getBasicInfo().getLastname())
			.replace("[newPassword]", newPassword);
		sendEmail(u.getContactInfo().getEmail(), title, body);
	}

}
