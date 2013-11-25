package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.User;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

@Stateless(name = "jiraService")
public class JiraService {

	private static final Logger logger = Logger.getLogger(MailService.class.getName());

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	private static final String username = "apella";

	private static final String password = "!@#$%^&*()"; // Production Pk%81:$/IES/

	private static final String sesRootURL = "https://staging.tts.grnet.gr/jira/rest/api/2";

	private static final String userRootURL = "http://test.apella.grnet.gr#user/{userId}";

	private static final String jiraProjectKey = "APELLA";

	private static final ObjectMapper mapper = new ObjectMapper();

	private static JsonNode doGet(String path) throws Exception {
		ClientRequest request = new ClientRequest(sesRootURL + path);
		request.accept(MediaType.APPLICATION_JSON);
		request.header("Authorization", "Basic " + authenticationString());
		ClientResponse<String> response = request.get(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Error Code " + response.getStatus());
		}
		String json = response.getEntity();
		JsonNode jsonNode = mapper.readTree(json);
		return jsonNode;
	}

	private static JsonNode doPost(String path, JsonNode data) throws Exception {
		ClientRequest request = new ClientRequest(sesRootURL + path);
		request.accept(MediaType.APPLICATION_JSON);
		request.header("Authorization", "Basic " + authenticationString());
		request.body("application/json", data);
		ClientResponse<String> response = request.post(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Response Error post " + path + " : " + response.getStatus() + " " + response.getEntity());
		}

		String json = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(json);
		return jsonNode;
	}

	private static String authenticationString() {
		byte[] enc = Base64.encodeBase64((username + ":" + password).getBytes());
		return new String(enc);
	}

	public static JsonNode getIssue(String issueKey) throws Exception {
		return doGet("/issue/" + issueKey);
	}

	public void postJira(Long userId, String status, String summaryKey, String descriptionKey, Map<String, String> parameters) {
		ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-jira", new Locale("el"));
		String summary = resources.getString(summaryKey);
		String description = resources.getString(descriptionKey);
		for (String key : parameters.keySet()) {
			String value = parameters.get(key);
			if (value == null || value.trim().isEmpty()) {
				value = "-";
			}
			summary = summary.replaceAll("\\{" + key + "\\}", value);
			description = description.replaceAll("\\{" + key + "\\}", value);
		}

		Connection qConn = null;
		javax.jms.Session session = null;
		MessageProducer sender = null;
		try {

			javax.naming.Context jndiCtx = new InitialContext();

			ConnectionFactory factory = (QueueConnectionFactory) jndiCtx.lookup("/ConnectionFactory");
			Queue queue = (Queue) jndiCtx.lookup("queue/JiraQ");
			qConn = factory.createConnection();
			session = qConn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			sender = session.createProducer(queue);

			MapMessage jiraMessage = session.createMapMessage();
			jiraMessage.setJMSCorrelationID("jira");
			jiraMessage.setLong("userId", userId);
			jiraMessage.setString("status", status);
			jiraMessage.setString("summary", summary);
			jiraMessage.setString("description", description);

			logger.log(Level.INFO, "Posting Jira Message " + userId + " | " + status + " | " + summary + " | " + description);
			sender.send(jiraMessage);
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

	public void updateIssue(Long userId, String status, String summary, String description) {
		User user = em.find(User.class, userId);
		if (user == null) {
			logger.info("Tried to POST Jira Issue to non existin user: " + userId + " " + status + " " + summary + " " + description);
			return;
		}

		// Prepare Data:

		ObjectNode issue = mapper.createObjectNode();
		// Type
		ObjectNode issuetype = mapper.createObjectNode();
		issuetype.put("name", "Συμβάν");
		// Project
		ObjectNode project = mapper.createObjectNode();
		project.put("key", jiraProjectKey);

		// Fields
		ObjectNode fields = mapper.createObjectNode();
		fields.put("project", project);
		fields.put("issuetype", issuetype);
		fields.put("summary", summary);
		fields.put("description", description.concat("\n\n" + createUserInfo(user)));
		issue.put("fields", fields);

		// Send to Jira
		try {
			logger.info("updateIssue POST :\n" + issue.toString());
			JsonNode response = doPost("/issue/", issue);
			logger.info("updateIssue POST Result :\n" + response.toString());
			// TODO: Change Status to Closed
			/*
			if (!status.equals("Open")) {
				// Set Status
				JsonNode statusNode = createUpdateStatusNode(status);
				logger.info("updateIssue POST :\n" + statusNode.toString());
				response = doPost("/issue/" + response.get("key").getTextValue() + "/transitions", statusNode);
				logger.info("updateIssue POST :\n" + response.toString());
			}
			*/

		} catch (Exception e) {
			logger.log(Level.WARNING, "Error posting to Jira\n" + e.getMessage());
		}
	}

	public JsonNode createUpdateStatusNode(String status) {

		ObjectNode issue = mapper.createObjectNode();

		ObjectNode fields = mapper.createObjectNode();

		ObjectNode resolution = mapper.createObjectNode();
		resolution.put("name", status);
		fields.put("resolution", resolution);

		issue.put("fields", fields);

		ObjectNode transition = mapper.createObjectNode();
		transition.put("id", 2);
		issue.put("transition", transition);

		return issue;
	}

	private String createUserInfo(User user) {
		ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-jira", new Locale("el"));
		String comment = resources.getString("user.info")
			.replaceAll("\\{username\\}", user.getUsername())
			.replaceAll("\\{firstname\\}", user.getBasicInfo().getFirstname())
			.replaceAll("\\{lastname\\}", user.getBasicInfo().getLastname())
			.replaceAll("\\{email\\}", user.getContactInfo().getEmail())
			.replaceAll("\\{mobile\\}", user.getContactInfo().getMobile())
			.replaceAll("\\{link\\}", userRootURL.replaceAll("\\{userId\\}", user.getId().toString()));

		return comment;
	}

}
