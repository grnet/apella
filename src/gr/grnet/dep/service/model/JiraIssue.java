package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JiraIssue implements Serializable {

	private static final long serialVersionUID = 1885653801140864606L;

	public enum IssueType {
		COMPLAINT(11),
		ERROR(14),
		LOGIN(56),
		GENERAL_INFORMATION(57),
		ACCOUNT_MODIFICATION(59),
		REGISTRATION(60);

		private int value;

		private IssueType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private String key;

	private IssueType type;

	private Long userId;

	private String summary;

	private String description;

	public JiraIssue() {
		super();
	}

	public JiraIssue(IssueType type, Long userId, String summary, String description) {
		super();
		this.type = type;
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

	public void setType(IssueType type) {
		this.type = type;
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
