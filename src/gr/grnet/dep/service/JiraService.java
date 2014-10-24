package gr.grnet.dep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.JiraIssue;
import gr.grnet.dep.service.model.JiraIssue.IssueCall;
import gr.grnet.dep.service.model.JiraIssue.IssueCustomField;
import gr.grnet.dep.service.model.JiraIssue.IssueResolution;
import gr.grnet.dep.service.model.JiraIssue.IssueStatus;
import gr.grnet.dep.service.model.JiraIssue.IssueType;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless(name = "jiraService")
public class JiraService {

	private static final Logger logger = Logger.getLogger(JiraService.class.getName());
	/**
	 * username: apella
	 * password: Test "!@#$%^&*()" || Production: "Pk%81:$/IES/"
	 */

	private static String PROJECT_KEY;
	private static String REST_URL;
	private static String USERNAME;
	private static String PASSWORD;
	private static String CONFIGURATION;
	private static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();

			PROJECT_KEY = conf.getString("jira.project.key");
			REST_URL = conf.getString("jira.url");
			USERNAME = conf.getString("jira.username");
			PASSWORD = conf.getString("jira.password");
			CONFIGURATION = conf.getString("jira.configuration", "PRODUCTION");

		} catch (ConfigurationException e) {
		}
	}

	private final ObjectMapper mapper = new ObjectMapper();
	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;
	@EJB
	JiraService jiraService;
	private ResourceBundle jiraResourceBundle = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-jira", new Locale("el"));

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

	private JsonNode doGet(String path) throws Exception {
		Client client = ClientBuilder.newClient();
		logger.info("GET REQUEST: " + path);
		JsonNode jsonNode = client.target(REST_URL + path)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", "Basic " + authenticationString())
				.get(JsonNode.class);
		logger.info("GET RESPONSE: " + path + " SUCCESS");
		client.close();
		return jsonNode;
	}

	private JsonNode doPost(String path, JsonNode data) {
		Client client = ClientBuilder.newClient();
		logger.info("POST REQUEST: " + path + " " + data.toString());
		JsonNode jsonNode = client.target(REST_URL + path)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", "Basic " + authenticationString())
				.post(Entity.json(data), JsonNode.class);
		logger.info("POST RESPONSE: " + path + " SUCCESS");
		client.close();
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

	public JiraIssue getRemoteIssue(String issueKey) throws Exception {
		JsonNode node = doGet("/issue/" + issueKey);
		return fromJSON(node);
	}

	public List<JiraIssue> getRemoteIssues(List<Long> issueIds) throws Exception {
		if (issueIds.isEmpty()) {
			return new ArrayList<JiraIssue>();
		}
		// Comma separated values
		String ids = "";
		for (Long id : issueIds) {
			ids = ids.concat("," + id);
		}
		ids = ids.substring(1);
		// Search query
		String jql = "id in (" + ids + ")";

		/*
		{
		    "expand": "names,schema",
		    "startAt": 0,
		    "maxResults": 50,
		    "total": 1,
		    "issues": [
		        {
		            "expand": "",
		            "id": "10001",
		            "self": "http://www.example.com/jira/rest/api/2/issue/10001",
		            "key": "HSP-1"
		        }
		    ]
		}
		*/
		JsonNode searchNode = doGet("/search?jql=" + URLEncoder.encode(jql, "UTF-8") + "&maxResults=" + issueIds.size());
		List<JiraIssue> issues = new ArrayList<JiraIssue>();
		Iterator<JsonNode> issuesIterator = searchNode.get("issues").iterator();
		while (issuesIterator.hasNext()) {
			JsonNode issueNode = issuesIterator.next();
			issues.add(fromJSON(issueNode));
		}
		return issues;
	}

	public JiraIssue createRemoteIssue(JiraIssue issue) throws ServiceException {
		// Read and validate data
		IssueStatus status = issue.getStatus();
		IssueResolution resolution = issue.getResolution();
		if (!status.equals(IssueStatus.OPEN) && resolution == null) {
			throw new ServiceException("jira.missing.resolution");
		}
		try {
			// 1. Create Issue
			issue.setStatus(null);
			issue.setResolution(null);
			JsonNode issueData = toJSON(issue);
			JsonNode response = doPost("/issue/", issueData);
			// Response sends only id and key
			issue.setId(response.get("id").asLong());
			issue.setKey(response.get("key").asText());
			// 2. Update with a transition to status if not OPEN
			if (!status.equals(IssueStatus.OPEN)) {
				JsonNode updateStatusData = toTransitionJSON(status, resolution, "Automatic transition");
				response = doPost("/issue/" + issue.getKey() + "/transitions", updateStatusData);
				// Response is empty if transition succeeds
				issue.setStatus(status);
				issue.setResolution(resolution);
			}
			// 3. Set Date (will be updated when quartz job refreshes all issues)
			Date now = new Date();
			issue.setCreated(now);
			issue.setUpdated(now);
			return issue;
		} catch (Exception e) {
			logger.log(Level.WARNING, "", e);
			throw new ServiceException("jira.communication.problem", e);
		}

	}

	public JiraIssue getIssue(Long issueId) throws ServiceException {
		JiraIssue issue = em.find(JiraIssue.class, issueId);
		if (issue == null) {
			throw new ServiceException("issue.not.found");
		}
		return issue;
	}

	public List<JiraIssue> getUserIssues(Long userId) {
		@SuppressWarnings("unchecked")
		List<JiraIssue> issues = em.createQuery(
				"from JiraIssue issue " +
						"where issue.user.id = :userId")
				.setParameter("userId", userId)
				.getResultList();

		return issues;
	}

	public JiraIssue createIssue(JiraIssue issue) throws ServiceException {
		// Validate data
		if (issue.getUser() == null || issue.getUser().getId() == null) {
			throw new ServiceException("missing.user");
		}
		User user = em.find(User.class, issue.getUser().getId());
		if (user == null) {
			throw new ServiceException("user.not.found");
		}
		try {
			// Send to Jira
			JiraIssue createdIssue = createRemoteIssue(issue);
			// Save to Database
			createdIssue.setUser(user);
			createdIssue = em.merge(createdIssue);

			return createdIssue;

		} catch (Exception e) {
			throw new ServiceException("jira.communication.problem", e);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public JiraIssue updateIssue(JiraIssue issue) throws ServiceException {
		JiraIssue existingIssue = getIssue(issue.getId());
		// Validate data
		issue.setUser(existingIssue.getUser());
		// Update (no need to copy to existingIssue, since ids are not auto-generated and we want to override all fields)
		issue = em.merge(issue);
		// Return result
		return issue;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public int synchronizeIssues() throws Exception {
		List<Long> localIssueIds = em.createQuery(
				"select i.id from JiraIssue i", Long.class)
				.getResultList();
		List<JiraIssue> remoteIssues = getRemoteIssues(localIssueIds);
		for (JiraIssue issue : remoteIssues) {
			jiraService.updateIssue(issue);
		}
		return remoteIssues.size();
	}

	//////////////////////////////////////////
	// Parsers, formatters

	/* ISSUE TEMPLATE:
	{
		"id": "25655",
		"key": "APELLA-1251",
		"fields": {
			"project": {
		  		"id": "11241",
		  		"key": "APELLA",
		  		"name": "Apella"
			},
			"issuetype": {
		  		"id": "14",
		  		"description": "Αφορά σε πρόβλημα της υπηρεσίας",
		    	"name": "Πρόβλημα",
			},
			"resolution": {
		  		"id": "1",
		  		"description": "A fix for this issue is checked into the tree and tested.",
		  		"name": "Fixed"
			},
			"created": "2014-01-13T10:28:18.000+0200",
			"updated": "2014-01-13T11:35:33.000+0200",
			
			summary": "Αλλαγή ονόματος πατρός",
			"description": "Το όνομα του πατέρα μου είναι ΑΧΙΛΛΕΑΣ.\r\nΠαρακαλώ να αντικατασταθεί το Ιωάννης που αναγράφεται στα στοιχεία μου με το Αχιλλέας.\r\n\r\nΕυχαριστώ\n\n*Reporter*: Φαρμάκη Βασιλική Καθηγήτρια Τμήμα Μαθηματικών ΕΚΠΑ\n*E-mail*: [mailto:vfarmaki@math.uoa.gr]",
			
			"status": {
		  		"description": "The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.",
		  		"name": "Closed",
		  		"id": "6"
			},
			"customfield_12352": null,
			"customfield_12351": null,
			"customfield_12350": null,
			"customfield_10350": null,
			"customfield_12751": null,
			"customfield_11850": null,
			"customfield_12550": null,
			"customfield_12551": null,
			"customfield_12552": null,
			"customfield_12553": "6974676161",
			"customfield_12653": null,
			"customfield_12652": null,
			"customfield_11551": null,
			
			"comment": {
		  		"startAt": 0,
		  		"maxResults": 1,
		  		"total": 1,
		  		"comments": [
		  			{
		      			"id": "37246",
		      			"body": "The issue has just been emailed to:  *vfarmaki@math.uoa.gr*,  \\\\\r\nwith subject: *(APELLA-1251) Αλλαγή ονόματος πατρός * \\\\\r\n \\\\ \r\nand content:\\\\\r\n----\r\nΗ αλλαγή στα στοιχεία του λογαριασμού σας έχει πραγματοποιηθεί.\r\n\r\nΣτη διάθεσή σας, \r\nΓραφείο Αρωγής Χρηστών ΑΠΕΛΛΑ\r\n\r\nΠΡΟΣΟΧΗ:\r\nΠαρακαλούμε MHN απαντήσετε σε αυτό το e-mail. \r\nΓια οποιαδήποτε απορία ή διευκρίνιση μπορείτε να επικοινωνήσετε με το [Γραφείο Αρωγής Χρηστών|https://apella.minedu.gov.gr/contact].",
		      			"created": "2014-01-13T11:35:27.000+0200",
		      			"updated": "2014-01-13T11:35:27.000+0200"
		      		}
		  		]
			}
		}
	}
	*/

	private JsonNode toJSON(JiraIssue jiraIssue) {
		JiraConfiguration jiraConfiguration = getJiraConfiguration(CONFIGURATION);

		ObjectNode issue = mapper.createObjectNode();

		if (jiraIssue.getId() != null) {
			issue.put("id", jiraIssue.getId());
		}

		if (jiraIssue.getKey() != null) {
			issue.put("key", jiraIssue.getKey());
		}

		// Fields
		ObjectNode fields = mapper.createObjectNode();
		issue.put("fields", fields);

		ObjectNode project = mapper.createObjectNode();
		project.put("key", PROJECT_KEY);
		fields.put("project", project);

		if (jiraIssue.getResolution() != null) {
			ObjectNode resolutionNode = mapper.createObjectNode();
			resolutionNode.put("id", jiraConfiguration.getIssueResolutionId(jiraIssue.getResolution()));
			fields.put("resolution", resolutionNode);
		}

		if (jiraIssue.getStatus() != null) {
			ObjectNode statusNode = mapper.createObjectNode();
			statusNode.put("id", jiraConfiguration.getIssueStatusId(jiraIssue.getStatus()));
			fields.put("status", statusNode);
		}

		if (jiraIssue.getSummary() != null) {
			fields.put("summary", jiraIssue.getSummary());
		}

		if (jiraIssue.getDescription() != null) {
			fields.put("description", jiraIssue.getDescription());
		}

		if (jiraIssue.getType() != null) {
			ObjectNode issueTypeNode = mapper.createObjectNode();
			issueTypeNode.put("id", jiraConfiguration.getIssueTypeId(jiraIssue.getType()));
			fields.put("issuetype", issueTypeNode);
		}

		if (jiraIssue.getCall() != null) {
			ObjectNode callTypeNode = mapper.createObjectNode();
			callTypeNode.put("id", jiraConfiguration.getIssueCallId(jiraIssue.getCall()));
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.CALL_TYPE), callTypeNode);
		}

		if (jiraIssue.getRole() != null) {
			ObjectNode roleNode = mapper.createObjectNode();
			roleNode.put("value", jiraConfiguration.getRoleValue(jiraIssue.getRole()));
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.ROLE), roleNode);
		}

		if (jiraIssue.getUsername() != null) {
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.USERNAME), jiraIssue.getUsername());
		}

		if (jiraIssue.getFullname() != null) {
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.FULLNAME), jiraIssue.getFullname());
		}

		if (jiraIssue.getMobile() != null) {
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.MOBILE), jiraIssue.getMobile());
		}

		if (jiraIssue.getEmail() != null) {
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.EMAIL), jiraIssue.getEmail());
		}

		if (jiraConfiguration.getIssueCustomFieldId(IssueCustomField.REPORTER).length() > 0 &&
				jiraIssue.getReporter() != null) {
			fields.put(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.REPORTER), jiraIssue.getReporter());
		}

		return issue;

	}

	private JiraIssue fromJSON(JsonNode issueNode) {
		JiraConfiguration jiraConfiguration = getJiraConfiguration(CONFIGURATION);

		JiraIssue issue = new JiraIssue();

		// Read parameters
		if (issueNode.has("id")) {
			Long id = issueNode.get("id").asLong();
			issue.setId(id);
		}

		if (issueNode.has("id")) {
			String key = issueNode.get("key").asText();
			issue.setKey(key);
		}
		// Fields
		JsonNode fieldsNode = issueNode.get("fields");

		if (fieldsNode.has("resolution") && !fieldsNode.get("resolution").isNull()) {
			IssueResolution resolution = jiraConfiguration.getIssueResolution(fieldsNode.get("resolution").get("id").asText());
			issue.setResolution(resolution);
		}
		if (fieldsNode.has("status")) {
			IssueStatus status = jiraConfiguration.getIssueStatus(fieldsNode.get("status").get("id").asText());
			issue.setStatus(status);
		}
		if (fieldsNode.has("summary")) {
			String summary = fieldsNode.get("summary").asText();
			issue.setSummary(summary);
		}
		if (fieldsNode.has("description")) {
			String description = fieldsNode.get("description").asText();
			issue.setDescription(description);
		}
		if (fieldsNode.has("issuetype")) {
			IssueType type = jiraConfiguration.getIssueType(fieldsNode.get("issuetype").get("id").asInt());
			issue.setType(type);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.CALL_TYPE))) {
			IssueCall call = jiraConfiguration.getIssueCall(fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.CALL_TYPE)).get("id").asText());
			issue.setCall(call);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.ROLE))) {
			RoleDiscriminator role = jiraConfiguration.getIssueRole(fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.ROLE)).get("value").asText());
			issue.setRole(role);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.USERNAME))) {
			String username = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.USERNAME)).asText();
			issue.setUsername(username);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.FULLNAME))) {
			String fullname = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.FULLNAME)).asText();
			issue.setFullname(fullname);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.MOBILE))) {
			String mobile = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.MOBILE)).asText();
			issue.setMobile(mobile);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.EMAIL))) {
			String email = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.EMAIL)).asText();
			issue.setEmail(email);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.REPORTER))) {
			String reporter = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.REPORTER)).asText();
			issue.setReporter(reporter);
		}
		if (fieldsNode.has(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.COMMENT))) {
			String comment = fieldsNode.get(jiraConfiguration.getIssueCustomFieldId(IssueCustomField.COMMENT)).asText();
			issue.setComment(comment);
		}
		if (fieldsNode.has("updated")) {
			try {
				Date updated = jiraConfiguration.getDateFormat().parse(fieldsNode.get("updated").asText());
				issue.setUpdated(updated);
			} catch (ParseException e) {
			}

		}
		if (fieldsNode.has("created")) {
			try {
				Date created = jiraConfiguration.getDateFormat().parse(fieldsNode.get("created").asText());
				issue.setCreated(created);
			} catch (ParseException e) {
			}
		}
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
	    	"resolution": {
	        	"name": "Fixed"
	    	}
		},
		"transition": {
	    	"id": "5"
		}
	}
	*/

	private JsonNode toTransitionJSON(IssueStatus status, IssueResolution resolution, String comment) {
		JiraConfiguration jiraConfiguration = getJiraConfiguration(CONFIGURATION);

		ObjectNode issue = mapper.createObjectNode();

		// Transition
		ObjectNode updateNode = mapper.createObjectNode();
		issue.put("update", updateNode);

		ArrayNode commentsArrayNode = mapper.createArrayNode();
		updateNode.put("comment", commentsArrayNode);

		ObjectNode commentNode = mapper.createObjectNode();
		commentsArrayNode.add(commentNode);
		ObjectNode addCommentNode = mapper.createObjectNode();
		commentNode.put("add", addCommentNode);
		addCommentNode.put("body", comment);

		ObjectNode fieldsNode = mapper.createObjectNode();
		issue.put("fields", fieldsNode);

		ObjectNode resolutionNode = mapper.createObjectNode();
		resolutionNode.put("id", jiraConfiguration.getIssueResolutionId(resolution));
		fieldsNode.put("resolution", resolutionNode);

		ObjectNode transistionNode = mapper.createObjectNode();
		transistionNode.put("id", jiraConfiguration.getIssueStatusId(status));
		issue.put("transition", transistionNode);

		return issue;

	}

	//////////////////////////////////////////////////
	// Configurations

	private JiraConfiguration getJiraConfiguration(String jiraConfiguration) {
		if (jiraConfiguration.equals("STAGING")) {
			return new StagingJiraConfigurationImpl();
		} else {
			return new ProductionJiraConfigurationImpl();
		}
	}

	private interface JiraConfiguration {

		public SimpleDateFormat getDateFormat();

		public String getIssueCallId(IssueCall call);

		public IssueCall getIssueCall(String id);

		public String getIssueStatusId(IssueStatus status);

		public IssueStatus getIssueStatus(String id);

		public int getIssueTypeId(IssueType type);

		public IssueType getIssueType(int id);

		public String getIssueResolutionId(IssueResolution type);

		public IssueResolution getIssueResolution(String id);

		public String getIssueCustomFieldId(IssueCustomField type);

		public IssueCustomField getIssueCustomField(String id);

		public String getRoleValue(RoleDiscriminator role);

		public RoleDiscriminator getIssueRole(String value);
	}

	private class ProductionJiraConfigurationImpl implements JiraConfiguration {

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		public SimpleDateFormat getDateFormat() {
			return sdf;
		}

		@Override
		public String getIssueCallId(IssueCall call) {
			switch (call) {
				case INCOMING:
					return "10220";
				case OUTGOING:
					return "10221";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueStatusId(IssueStatus status) {
			switch (status) {
				case OPEN:
					return "1";
				case CLOSE:
					return "2";
				case IN_PROGRESS:
					return "3";
				case REOPENED:
					return "4";
				case RESOLVED:
					return "5";
				case CLOSED:
					return "6";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public int getIssueTypeId(IssueType type) {
			switch (type) {
				case COMPLAINT:
					return 11;
				case ERROR:
					return 14;
				case LOGIN:
					return 79;
				case GENERAL_INFORMATION:
					return 78;
				case ACCOUNT_MODIFICATION:
					return 80;
				case REGISTRATION:
					return 77;
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueResolutionId(IssueResolution type) {
			switch (type) {
				case FIXED:
					return "1";
				case WONT_FIX:
					return "2";
				case DUPLICATE:
					return "3";
				case INCOMPLETE:
					return "4";
				case CANNOT_REPRODUCE:
					return "5";
				case FIXED_WORKAROUND:
					return "7";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueCustomFieldId(IssueCustomField type) {
			switch (type) {
				case ROLE:
					return "customfield_12652";
				case CALL_TYPE:
					return "customfield_12551";
				case USERNAME:
					return "customfield_12552";
				case FULLNAME:
					return "customfield_12350";
				case MOBILE:
					return "customfield_12553";
				case EMAIL:
					return "customfield_12550";
				case REPORTER:
					return "customfield_12751";
				case COMMENT:
					return "customfield_12754";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getRoleValue(RoleDiscriminator role) {
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

		@Override
		public IssueCall getIssueCall(String id) {
			for (IssueCall value : IssueCall.values()) {
				if (getIssueCallId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueStatus getIssueStatus(String id) {
			for (IssueStatus value : IssueStatus.values()) {
				if (getIssueStatusId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueType getIssueType(int id) {
			for (IssueType value : IssueType.values()) {
				if (getIssueTypeId(value) == id) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueResolution getIssueResolution(String id) {
			for (IssueResolution value : IssueResolution.values()) {
				if (getIssueResolutionId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueCustomField getIssueCustomField(String id) {
			for (IssueCustomField value : IssueCustomField.values()) {
				if (getIssueCustomFieldId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public RoleDiscriminator getIssueRole(String value) {
			for (RoleDiscriminator role : RoleDiscriminator.values()) {
				if (getRoleValue(role).equals(value)) {
					return role;
				}
			}
			throw new IllegalArgumentException();
		}

	}

	private class StagingJiraConfigurationImpl implements JiraConfiguration {

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		public SimpleDateFormat getDateFormat() {
			return sdf;
		}

		@Override
		public String getIssueCallId(IssueCall call) {
			switch (call) {
				case INCOMING:
					return "10227";
				case OUTGOING:
					return "10228";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueStatusId(IssueStatus status) {
			switch (status) {
				case OPEN:
					return "1";
				case CLOSE:
					return "2";
				case IN_PROGRESS:
					return "3";
				case REOPENED:
					return "4";
				case RESOLVED:
					return "5";
				case CLOSED:
					return "6";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public int getIssueTypeId(IssueType type) {
			switch (type) {
				case COMPLAINT:
					return 11;
				case ERROR:
					return 14;
				case LOGIN:
					return 56;
				case GENERAL_INFORMATION:
					return 57;
				case ACCOUNT_MODIFICATION:
					return 59;
				case REGISTRATION:
					return 60;
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueResolutionId(IssueResolution type) {
			switch (type) {
				case FIXED:
					return "1";
				case WONT_FIX:
					return "2";
				case DUPLICATE:
					return "3";
				case INCOMPLETE:
					return "4";
				case CANNOT_REPRODUCE:
					return "5";
				case FIXED_WORKAROUND:
					return "6";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getIssueCustomFieldId(IssueCustomField type) {
			switch (type) {
				case ROLE:
					return "customfield_12650";
				case CALL_TYPE:
					return "customfield_12651";
				case USERNAME:
					return "customfield_12652";
				case FULLNAME:
					return "customfield_12653";
				case MOBILE:
					return "customfield_12654";
				case EMAIL:
					return "customfield_12655";
				case REPORTER:
					return "";
				case COMMENT:
					return "";
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public String getRoleValue(RoleDiscriminator role) {
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

		@Override
		public IssueCall getIssueCall(String id) {
			for (IssueCall value : IssueCall.values()) {
				if (getIssueCallId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueStatus getIssueStatus(String id) {
			for (IssueStatus value : IssueStatus.values()) {
				if (getIssueStatusId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueType getIssueType(int id) {
			for (IssueType value : IssueType.values()) {
				if (getIssueTypeId(value) == id) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueResolution getIssueResolution(String id) {
			for (IssueResolution value : IssueResolution.values()) {
				if (getIssueResolutionId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public IssueCustomField getIssueCustomField(String id) {
			for (IssueCustomField value : IssueCustomField.values()) {
				if (getIssueCustomFieldId(value).equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

		@Override
		public RoleDiscriminator getIssueRole(String value) {
			for (RoleDiscriminator role : RoleDiscriminator.values()) {
				if (getRoleValue(role).equals(value)) {
					return role;
				}
			}
			throw new IllegalArgumentException();
		}

	}

}
