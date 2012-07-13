package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.User;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public class MailClient {

	private static final Logger logger = Logger.getLogger(MailClient.class.getName());

	private static Configuration conf;

	private static Properties props;

	private static Authenticator authenticator;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
			props = new Properties();
			props.put("mail.host", conf.getString("mail.host", "localhost"));
			props.put("mail.port", conf.getString("mail.port", "25"));
			props.put("mail.from", conf.getString("mail.from", "noreply@apella.grnet.gr"));
			if (conf.getBoolean("mail.smtp.auth", false)) {
				authenticator = new Authenticator(conf.getString("mail.smtp.auth.user"), conf.getString("mail.smtp.auth.password"));
				props.put("mail.smtp.auth", "true");
				props.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
			}
		} catch (ConfigurationException e) {
			logger.log(Level.SEVERE, "", e);
		}
	}

	private static class Authenticator extends javax.mail.Authenticator {

		private PasswordAuthentication authentication;

		public Authenticator(String username, String password) {
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}

	public static void sendVerificationEmail(User u) throws MessagingException {
		ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-mail", new Locale("el"));
		String title = resources.getString("verification.title");
		String body = resources.getString("verification.body")
			.replace("[username]", u.getUsername())
			.replace("[firstname]", u.getBasicInfo().getFirstname())
			.replace("[lastname]", u.getBasicInfo().getLastname())
			.replace("[verificationLink]", conf.getString("host") + "/depui/registration.html?#email=" + u.getUsername() + "&verification=" + u.getVerificationNumber());
		sendEmail(u.getContactInfo().getEmail(), title, body);
	}

	private static void sendEmail(String aToEmailAddr, String aSubject, String aBody) throws MessagingException {
		logger.log(Level.INFO, "Sending email to " + aToEmailAddr + " " + aSubject + "\n" + aBody);
		Session session = Session.getInstance(props, authenticator);
		MimeMessage message = new MimeMessage(session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
		message.setSubject(aSubject, "UTF-8");
		message.setText(aBody, "UTF-8");
		Transport.send(message);
	}

}
