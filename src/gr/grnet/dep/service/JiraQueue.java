package gr.grnet.dep.service;

import gr.grnet.dep.service.model.JiraIssue;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

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
		ObjectMessage jiraMessage = (ObjectMessage) message;
		try {
			JiraIssue jiraIssue = (JiraIssue) jiraMessage.getObject();
			service.createRemoteIssue(jiraIssue);
		} catch (JMSException e) {
			logger.log(Level.WARNING, "", e);
			throw new EJBException(e);
		} catch (Exception e) {
			logger.log(Level.WARNING, "", e); // Do not re-send if an error happens
		}
	}
}
