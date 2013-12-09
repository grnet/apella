package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JiraIssue implements Serializable {

	private static final long serialVersionUID = 1885653801140864606L;

	public enum IssueCall {
		INCOMING,
		OUTGOING
	}

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
		EMAIL
	}

	private String key;

	private IssueStatus status = IssueStatus.OPEN;

	private IssueType type = IssueType.GENERAL_INFORMATION;

	private IssueCall call = IssueCall.INCOMING;

	private Long userId;

	private String summary;

	private String description;

	public JiraIssue() {
		super();
	}

	public JiraIssue(IssueStatus status, IssueType type, IssueCall call, Long userId, String summary, String description) {
		super();
		this.status = status;
		this.type = type;
		this.call = call;
		this.userId = userId;
		this.summary = summary;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public IssueType getType() {
		return type;
	}

	public IssueStatus getStatus() {
		return status;
	}

	public void setStatus(IssueStatus status) {
		this.status = status;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

}
