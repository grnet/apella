package gr.grnet.dep.service;

import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.JiraIssue.IssueResolution;
import gr.grnet.dep.service.model.JiraIssue.IssueStatus;
import gr.grnet.dep.service.model.JiraIssue.IssueType;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

@Stateless(name = "jiraService")
public class JiraService {

	private static final Logger logger = Logger.getLogger(MailService.class.getName());

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private ResourceBundle jiraResourceBundle = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-jira", new Locale("el"));

	/**
	 * username: apella
	 * password: Test "!@#$%^&*()" || Production: "Pk%81:$/IES/"
	 */

	private static String USERNAME;

	private static String PASSWORD;

	private static String REST_URL;

	private static String PROJECT_KEY;

	private static Configuration conf;

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

	public String getResourceBundleString(String key, String... args) {
		String result = jiraResourceBundle.getString(key);
		if (args != null && (args.length % 2 == 0)) {
			for (int i = 0; i < args.length - 1; i += 2) {
				String name = args[i];
				String value = args[i + 1];
				result = result.replaceAll("\\{" + name + "\\}", value);
			}
		}
		return result;
	}

	private final ObjectMapper mapper = new ObjectMapper();

	private JsonNode doGet(String path) throws Exception {
		ClientRequest request = new ClientRequest(REST_URL + path);
		request.accept(MediaType.APPLICATION_JSON);
		request.header("Authorization", "Basic " + authenticationString());
		logger.info("GET REQUEST: " + path);
		ClientResponse<String> response = request.get(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Error Code " + response.getStatus());
		}
		String json = response.getEntity();
		JsonNode jsonNode = mapper.readTree(json);
		logger.info("GET RESPONSE: " + path + " " + jsonNode.toString());
		return jsonNode;
	}

	private JsonNode doPost(String path, JsonNode data) throws Exception {
		ClientRequest request = new ClientRequest(REST_URL + path);
		request.accept(MediaType.APPLICATION_JSON);
		request.header("Authorization", "Basic " + authenticationString());
		request.body("application/json", data);
		logger.info("POST REQUEST: " + path + " " + data.toString());
		ClientResponse<String> response = request.post(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Response Error post " + path + " : " + response.getStatus() + " " + response.getEntity());
		}
		String json = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(json);
		logger.info("POST RESPONSE: " + path + " " + jsonNode.toString());
		return jsonNode;
	}

	private JsonNode doPut(String path, JsonNode data) throws Exception {
		ClientRequest request = new ClientRequest(REST_URL + path);
		request.accept(MediaType.APPLICATION_JSON);
		request.header("Authorization", "Basic " + authenticationString());
		request.body("application/json", data);
		logger.info("PUT REQUEST: " + path + " " + data.toString());
		ClientResponse<String> response = request.put(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Response Error post " + path + " : " + response.getStatus() + " " + response.getEntity());
		}

		String json = response.getEntity();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(json);
		logger.info("PUT RESPONSE: " + path + " " + jsonNode.toString());
		return jsonNode;
	}

	private String authenticationString() {
		byte[] enc = Base64.encodeBase64((USERNAME + ":" + PASSWORD).getBytes());
		return new String(enc);
	}

	public void queueCreateIssue(JiraIssue issue) {
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

			ObjectMessage jiraMessage = session.createObjectMessage();
			jiraMessage.setJMSCorrelationID("jira");
			jiraMessage.setObject(issue);

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

	public JiraIssue getIssue(String issueKey) throws Exception {
		JsonNode node = doGet("/issue/" + issueKey);
		return fromJSON(node);
	}

	public JiraIssue createIssue(JiraIssue issue) {
		// Prepare Data:
		JsonNode issueData = toJSON(issue);
		if (issueData == null) {
			return null;
		}
		// Send to Jira
		try {
			JsonNode response = doPost("/issue/", issueData);
			issue.setKey(response.get("key").asText());
			if (!issue.getStatus().equals(IssueStatus.OPEN)) {
				JsonNode updateStatusData = toTransitionJSON(issue);
				response = doPost("/issue/" + issue.getKey() + "/transitions", updateStatusData);
			}
			return issue;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error posting to Jira:\n" + e.getMessage());
			return null;
		}
	}

	private JiraIssue fromJSON(JsonNode jiraIssue) {
		try {
			String key = jiraIssue.get("key").asText();

			JsonNode fields = jiraIssue.get("fields");
			IssueType type = IssueType.valueOf(fields.get("issuetype").get("id").asInt());
			IssueStatus status = IssueStatus.valueOf(fields.get("status").get("id").asInt());
			String summary = fields.get("summary").asText();
			String description = fields.get("description").asText();
			String email = fields.get("customfield_12655").asText();

			Long userId = (Long) em.createQuery(
				"select u.id from User u where u.contactInfo.email = :email ")
				.setParameter("email", email)
				.getSingleResult();

			JiraIssue issue = new JiraIssue(status, type, userId, summary, description);
			issue.setKey(key);

			return issue;

		} catch (NoResultException e) {
			return null;
		}
	}

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
				id: "60"
			},
			status: {
				name: "Open"
				id: "1"
			}
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

	private JsonNode toJSON(JiraIssue jiraIssue) {
		User user = em.find(User.class, jiraIssue.getUserId());
		if (user == null) {
			logger.info("Tried to POST Jira Issue to non existin user: " + jiraIssue.getUserId() + " " + jiraIssue.getType() + " " + jiraIssue.getSummary() + " " + jiraIssue.getDescription());
			return null;
		}

		ObjectNode issue = mapper.createObjectNode();
		// Project

		// Fields
		ObjectNode fields = mapper.createObjectNode();
		issue.put("fields", fields);

		ObjectNode project = mapper.createObjectNode();
		fields.put("project", project);
		project.put("key", PROJECT_KEY);

		fields.put("summary", jiraIssue.getSummary());

		ObjectNode issueTypeNode = mapper.createObjectNode();
		issueTypeNode.put("id", jiraIssue.getType().intValue());
		fields.put("issuetype", issueTypeNode);

		fields.put("description", jiraIssue.getDescription());

		ObjectNode roleNode = mapper.createObjectNode();
		roleNode.put("value", getRoleString(user.getPrimaryRole()));
		fields.put("customfield_12650", roleNode);

		ObjectNode callTypeNode = mapper.createObjectNode();
		callTypeNode.put("value", "εισερχόμενη");
		fields.put("customfield_12651", callTypeNode);

		fields.put("customfield_12652", user.getUsername() == null ? "-" : user.getUsername());
		fields.put("customfield_12653", user.getBasicInfo().getFirstname() + " " + user.getBasicInfo().getLastname());
		fields.put("customfield_12654", user.getContactInfo().getMobile());
		fields.put("customfield_12655", user.getContactInfo().getEmail());

		return issue;

	}

	/*
	 {
	 	"update": {
	    	"comment": [
	        	{
	            	"add": {
	                	"body": "Bug has been fixed."
	            	}
	        	}
	    	]
		},
		"fields": {
	    	"assignee": {
	        	"name": "bob"
	    	},
	    	"resolution": {
	        	"name": "Fixed"
	    	}
		},
		"transition": {
	    	"id": "5"
		}
	}
	*/

	private JsonNode toTransitionJSON(JiraIssue jiraIssue) {
		ObjectNode issue = mapper.createObjectNode();

		// Transition
		ObjectNode update = mapper.createObjectNode();
		issue.put("update", update);
		ArrayNode comments = mapper.createArrayNode();
		update.put("comment", comments);

		ObjectNode comment = mapper.createObjectNode();
		comments.add(comment);
		ObjectNode add = mapper.createObjectNode();
		comment.put("add", add);
		add.put("body", "Automatically Closed");

		ObjectNode fields = mapper.createObjectNode();
		issue.put("fields", fields);

		ObjectNode resolution = mapper.createObjectNode();
		fields.put("resolution", resolution);
		resolution.put("name", IssueResolution.FIXED_WORKAROUND.intValue());

		ObjectNode transistionNode = mapper.createObjectNode();
		transistionNode.put("id", jiraIssue.getStatus().intValue());
		issue.put("transition", transistionNode);

		return issue;

	}
}
