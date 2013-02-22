package gr.grnet.dep.service.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageListener;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@MessageDriven(activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/EMailQ")
})
public class MailService implements MessageListener {

	@Resource(lookup = "java:jboss/mail/Default")
	protected Session mailSession;

	private static final Logger logger = Logger.getLogger(MailService.class.getName());

	@Override
	public void onMessage(javax.jms.Message message) {
		MapMessage mailMessage = (MapMessage) message;
		try {
			String aToEmailAddr = mailMessage.getString("aToEmailAddr");
			String aSubject = mailMessage.getString("aSubject");
			String aBody = mailMessage.getString("aBody");
			sendEmail(aToEmailAddr, aSubject, aBody);
		} catch (JMSException e) {
			logger.log(Level.WARNING, "", e);
			throw new EJBException(e);
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

}
