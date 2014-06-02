package gr.grnet.dep.service;

import gr.grnet.dep.service.model.MailRecord;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/EMailQ")
})
public class MailQueue implements MessageListener {

	private static final Logger logger = Logger.getLogger(MailQueue.class.getName());

	@EJB
	MailService service;

	@Override
	public void onMessage(javax.jms.Message message) {
		MapMessage mailMessage = (MapMessage) message;
		try {
			String aToEmailAddr = mailMessage.getString("aToEmailAddr");
			String aSubject = mailMessage.getString("aSubject");
			String aBody = mailMessage.getString("aBody");
			boolean sendNow = mailMessage.getBoolean("sendNow");

			MailRecord mail = new MailRecord();
			mail.setToEmailAddr(aToEmailAddr);
			mail.setSubject(aSubject);
			mail.setBody(aBody);

			if (sendNow) {
				service.sendEmail(mail);
			} else {
				service.pushEmail(aToEmailAddr, aSubject, aBody);
			}
		} catch (JMSException e) {
			logger.log(Level.WARNING, "", e);
			throw new EJBException(e);
		}
	}
}
