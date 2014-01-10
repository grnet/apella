package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Role.RoleDiscriminator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JiraIssue implements Serializable {

	private static final long serialVersionUID = 1885653801140864606L;

	public enum IssueStatus {
		OPEN,
		CLOSED,
		IN_PROGRESS,
		REOPENED,
		RESOLVED
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
		REPORTER
	}

	public enum IssueCall {
		INCOMING,
		OUTGOING
	}

	private String key;

	private User user;

	//	Είδος Χρήστη / User Type
	private RoleDiscriminator role;

	private IssueStatus status = IssueStatus.OPEN;

	//	Είδος Αναφοράς / Issue Type
	private IssueType type = IssueType.GENERAL_INFORMATION;

	private IssueCall call = IssueCall.INCOMING;

	//	Ονοματεπώνυμο / Full name
	private String fullname;

	//	Τηλέφωνο / Telephone number
	private String mobile;

	//	Δ/νση E-mail / E-mail address
	private String email;

	//	Θέμα / Subject
	private String summary;

	//	Περιγραφή / Description
	private String description;

	// Καταγραφή από
	private String reporter;

	private List<JiraComment> comments = new ArrayList<JiraComment>();

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

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

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public List<JiraComment> getComments() {
		return comments;
	}

	public void setComments(List<JiraComment> comments) {
		this.comments = comments;
	}

}
