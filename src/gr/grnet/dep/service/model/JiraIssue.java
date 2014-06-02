package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

@Entity
@XmlRootElement
public class JiraIssue implements Serializable {

	private static final long serialVersionUID = 1885653801140864606L;

	public enum IssueStatus {
		OPEN,
		CLOSE,
		IN_PROGRESS,
		REOPENED,
		RESOLVED,
		CLOSED,
	}

	public enum IssueType {
		COMPLAINT,
		ERROR,
		LOGIN,
		GENERAL_INFORMATION,
		ACCOUNT_MODIFICATION,
		REGISTRATION
	}

	public enum IssueResolution {
		FIXED,
		WONT_FIX,
		DUPLICATE,
		INCOMPLETE,
		CANNOT_REPRODUCE,
		FIXED_WORKAROUND
	}

	public enum IssueCustomField {
		ROLE,
		CALL_TYPE,
		USERNAME,
		FULLNAME,
		MOBILE,
		EMAIL,
		REPORTER,
		COMMENT,
		CREATED,
		UPDATED
	}

	public enum IssueCall {
		INCOMING,
		OUTGOING
	}

	@Id
	private Long id;

	private String key;

	@Enumerated(EnumType.STRING)
	private IssueStatus status;

	//	Είδος Αναφοράς / Issue Type
	@Enumerated(EnumType.STRING)
	private IssueType type;

	@Enumerated(EnumType.STRING)
	private IssueCall call;

	private IssueResolution resolution;

	//	Είδος Χρήστη / User Type
	@Enumerated(EnumType.STRING)
	private RoleDiscriminator role;

	//	Όνομα Χρήστη / Username
	private String username;

	//	Ονοματεπώνυμο / Full name
	private String fullname;

	//	Τηλέφωνο / Telephone number
	private String mobile;

	//	Δ/νση E-mail / E-mail address
	private String email;

	//	Θέμα / Subject
	private String summary;

	//	Περιγραφή / Description
	@Column(columnDefinition = "text")
	private String description;

	// Σχόλιο που βλέπει ο χρήστης
	private String comment;

	// Καταγραφή από
	private String reporter;

	private Date created;

	private Date updated;

	@ManyToOne
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
	}

	public IssueType getType() {
		return type;
	}

	public void setType(IssueType type) {
		this.type = type;
	}

	public IssueCall getCall() {
		return call;
	}

	public void setCall(IssueCall call) {
		this.call = call;
	}

	public IssueResolution getResolution() {
		return resolution;
	}

	public void setResolution(IssueResolution resolution) {
		this.resolution = resolution;
	}

	@XmlTransient
	@JsonIgnore
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public RoleDiscriminator getRole() {
		return role;
	}

	public void setRole(RoleDiscriminator role) {
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	////////////////////////////////////////////

	public static JiraIssue createRegistrationIssue(User user, String summary, String description) {
		JiraIssue issue = new JiraIssue();

		issue.setStatus(IssueStatus.CLOSE);
		issue.setType(IssueType.REGISTRATION);
		issue.setCall(IssueCall.INCOMING);

		issue.setSummary(summary);
		issue.setDescription(description);
		issue.setReporter("apella");

		issue.setFullname(user.getFullName("el"));
		issue.setRole(user.getPrimaryRole());
		issue.setEmail(user.getContactInfo().getEmail());
		issue.setMobile(user.getContactInfo().getMobile());
		issue.setResolution(IssueResolution.FIXED);

		return issue;
	}
}
