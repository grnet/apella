package gr.grnet.dep.service.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/JiraQ")
})
public class JiraQueue implements MessageListener {

	private static final Logger logger = Logger.getLogger(JiraQueue.class.getName());

	@EJB
	JiraService service;

	@Override
	public void onMessage(javax.jms.Message message) {
		MapMessage jiraMessage = (MapMessage) message;
		try {
			String action = jiraMessage.getStringProperty("action");
			Long userId = jiraMessage.getLongProperty("user");

			if (action.equals("OPEN")) {
				service.openIssue(userId);
			} else if (action.equals("UPDATE")) {
				service.updateIssue(userId);
			} else if (action.equals("CLOSE")) {
				service.closeIssue(userId);
			}

		} catch (JMSException e) {
			logger.log(Level.WARNING, "", e);
			throw new EJBException(e);
		} catch (Exception e) {
			logger.log(Level.WARNING, "", e); // Do not re-send if an error happens
		}
	}
}
