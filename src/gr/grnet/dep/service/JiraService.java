package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

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

	/**
	 * username: apella
	 * password: Test "!@#$%^&*()" || Production: "Pk%81:$/IES/"
	 */

	private static String USERNAME;

	private static String PASSWORD;

	private static String REST_URL;

	private static String PROJECT_KEY;

	private static Configuration conf;

	public enum IssueType {
		REGISTRATION,
		GENERAL_INFORMATION,
		LOGIN,
		ACCOUNT_MODIFICATION,
		ERROR,
		COMPLAINT;

	}

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();

			PROJECT_KEY = conf.getString("jira.project.key");
			REST_URL = conf.getString("jira.url");
			USERNAME = conf.getString("jira.username");
			PASSWORD = conf.getString("jira.password");

		} catch (ConfigurationException e) {
		}
	}

	public String getIssueTypeString(IssueType issueType) {
		String retv;
		switch (issueType) {
			case REGISTRATION:
				retv = "Εγγραφή/Πιστοποίηση";
				break;
			case GENERAL_INFORMATION:
				retv = "γενικές πληροφορίες";
				break;
			case LOGIN:
				retv = "θέματα πρόσβασης";
				break;
			case ACCOUNT_MODIFICATION:
				retv = "μεταβολή στοιχείων";
				break;
			case ERROR:
				retv = "Πρόβλημα";
				break;
			case COMPLAINT:
				retv = "Παράπονα";
				break;
			default:
				retv = "ΑΛΛΟ";
				break;
		}
		return retv;
	}

	public String getRoleString(RoleDiscriminator role) {
		String retv;
		switch (role) {
			case ADMINISTRATOR:
				retv = "";
				break;
			case CANDIDATE:
				retv = "υποψήφιος";
				break;
			case INSTITUTION_ASSISTANT:
				retv = "βοηθός ιδρύματος";
				break;
			case INSTITUTION_MANAGER:
				retv = "διαχειριστής ιδρύματος";
				break;
			case MINISTRY_ASSISTANT:
				retv = "βοηθός υπουργείου";
				break;
			case MINISTRY_MANAGER:
				retv = "διαχειριστής υπουργείου";
				break;
			case PROFESSOR_DOMESTIC:
				retv = "καθηγητής/ερευνητής ημεδαπής";
				break;
			case PROFESSOR_FOREIGN:
				retv = "καθηγητής/ερευνητής αλλοδαπής";
				break;
			default:
				retv = "ΑΛΛΟ";
				break;
		}
		return retv;
	}

	private final ObjectMapper mapper = new ObjectMapper();

	private JsonNode doGet(String path) throws Exception {
		ClientRequest request = new ClientRequest(REST_URL + path);
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

	private JsonNode doPost(String path, JsonNode data) throws Exception {
		ClientRequest request = new ClientRequest(REST_URL + path);
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

	private String authenticationString() {
		byte[] enc = Base64.encodeBase64((USERNAME + ":" + PASSWORD).getBytes());
		return new String(enc);
	}

	public JsonNode getIssue(String issueKey) throws Exception {
		return doGet("/issue/" + issueKey);
	}

	public void queueJiraIssue(Long userId, IssueType issueType, String summary, String description) {
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
			jiraMessage.setObject("issueType", issueType);
			jiraMessage.setString("summary", summary);
			jiraMessage.setString("description", description);

			logger.log(Level.INFO, "Posting Jira Message " + userId + " | " + issueType + " | " + summary + " | " + description);
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

	public JsonNode openIssue(Long userId, IssueType issueType, String summary, String description) {
		User user = em.find(User.class, userId);
		if (user == null) {
			logger.info("Tried to POST Jira Issue to non existin user: " + userId + " " + issueType + " " + summary + " " + description);
			return null;
		}
		// Prepare Data:
		JsonNode issueData = prepareIssueData(user, issueType, summary, description);
		// Send to Jira
		try {
			logger.info("updateIssue POST :\n" + issueData.toString());
			JsonNode response = doPost("/issue/", issueData);
			logger.info("updateIssue POST Result :\n" + response.toString());
			return response;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error posting to Jira\n" + e.getMessage());
			return null;
		}
	}

	private JsonNode prepareIssueData(User user, IssueType issueType, String summary, String description) {
		/* ISSUE TEMPLATE:
		{
			"id": "23580",
			"key": "APELLA-52",
			"fields": {
				"project" : {
					key: "APELLA"
					name: "apella"
				},
		    	"summary": "Εγγραφή",
				"issuetype": {
					"name": "Εγγραφή/Πιστοποίηση",
				},
				"description": "Περιγραφή Εγγραφής",
				"customfield_12650": {
					"value": "καθηγητής/ερευνητής ημεδαπής",
					"id": "10221"
				},
				"customfield_12651": {
					"value": "εξερχόμενη",
					"id": "10228"
				},
				"customfield_12652": "anglen"
				"customfield_12653": "Angelos Lenis",
				"customfield_12654": "69000001010",
				"customfield_12655": "lenis.angelos@gmail.com",
			}
		}
		*/

		ObjectNode issue = mapper.createObjectNode();
		// Project

		// Fields
		ObjectNode fields = mapper.createObjectNode();
		issue.put("fields", fields);

		ObjectNode project = mapper.createObjectNode();
		fields.put("project", project);
		project.put("key", PROJECT_KEY);

		fields.put("summary", summary);

		ObjectNode issueTypeNode = mapper.createObjectNode();
		issueTypeNode.put("name", getIssueTypeString(issueType));
		fields.put("issuetype", issueTypeNode);

		fields.put("description", description);

		ObjectNode roleNode = mapper.createObjectNode();
		roleNode.put("value", getRoleString(user.getPrimaryRole()));
		fields.put("customfield_12650", roleNode);

		ObjectNode callTypeNode = mapper.createObjectNode();
		roleNode.put("value", "εισερχόμενη");
		fields.put("customfield_12651", callTypeNode);

		fields.put("customfield_12652", user.getUsername() == null ? "-" : user.getUsername());
		fields.put("customfield_12653", user.getBasicInfo().getFirstname() + " " + user.getBasicInfo().getLastname());
		fields.put("customfield_12654", user.getContactInfo().getMobile());
		fields.put("customfield_12655", user.getContactInfo().getEmail());

		return issue;

	}
}
