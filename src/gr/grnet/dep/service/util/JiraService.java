package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.User;

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

@Stateless
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

	private static final String username = "anglen";

	private static final String password = "angelosjira";

	private static final String sesRootURL = "https://ebsdev.atlassian.net/rest/api/2";

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
		request.body(MediaType.APPLICATION_JSON, data.toString());

		ClientResponse<String> response = request.post(String.class);
		if (response.getStatus() < 200 || response.getStatus() > 299) {
			throw new Exception("Error Code " + response.getStatus());
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

	public void postJira(String action, Long userId) {
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
			jiraMessage.setString("action", action);
			jiraMessage.setLong("user", userId);

			logger.log(Level.INFO, "Posting Jira Message " + action + " : " + userId);
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

	public void openIssue(Long userId) throws Exception {
		User user = em.find(User.class, userId);
		if (user == null) {
			logger.warning("Tried to open issue for not existin user " + userId);
			return;
		}

		ObjectNode data = mapper.createObjectNode();
		ObjectNode fields = mapper.createObjectNode();
		ObjectNode project = mapper.createObjectNode();
		ObjectNode issuetype = mapper.createObjectNode();

		data.put("fields", fields);
		fields.put("project", project);
		fields.put("summary", "A Summary " + System.currentTimeMillis());
		fields.put("description", "A description " + System.currentTimeMillis());
		project.put("key", "TESTREST");
		issuetype.put("name", "Task");
		fields.put("issuetype", issuetype);

		logger.info("openIssue POST (not implemented): " + data.toString());
		//JsonNode result = doPost("/issue/", data);
	}

	public void updateIssue(Long userId) throws Exception {
		User user = em.find(User.class, userId);
		if (user == null) {
			logger.warning("Tried to open issue for not existin user " + userId);
			return;
		}
		String jiraIssueKey = user.getJiraIssueKey();

		ObjectNode data = mapper.createObjectNode();
		ObjectNode fields = mapper.createObjectNode();
		ObjectNode project = mapper.createObjectNode();
		ObjectNode issuetype = mapper.createObjectNode();

		data.put("fields", fields);
		fields.put("project", project);
		fields.put("summary", "A Summary " + System.currentTimeMillis());
		fields.put("description", "A description " + System.currentTimeMillis());
		project.put("key", "TESTREST");
		issuetype.put("name", "Task");
		fields.put("issuetype", issuetype);

		logger.info("closeIssue POST (not implemented): " + jiraIssueKey + " " + data.toString());
		//JsonNode result = doPost("/issue/", data);
	}

	public void closeIssue(Long userId) throws Exception {
		User user = em.find(User.class, userId);
		if (user == null) {
			logger.warning("Tried to open issue for not existin user " + userId);
			return;
		}
		String jiraIssueKey = user.getJiraIssueKey();

		ObjectNode data = mapper.createObjectNode();
		ObjectNode fields = mapper.createObjectNode();
		ObjectNode project = mapper.createObjectNode();
		ObjectNode issuetype = mapper.createObjectNode();

		data.put("fields", fields);
		fields.put("project", project);
		fields.put("summary", "A Summary " + System.currentTimeMillis());
		fields.put("description", "A description " + System.currentTimeMillis());
		project.put("key", "TESTREST");
		issuetype.put("name", "Task");
		fields.put("issuetype", issuetype);

		logger.info("closeIssue POST (not implemented): " + jiraIssueKey + " " + data.toString());
		//JsonNode result = doPost("/issue/", data);

	}
}
