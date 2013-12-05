package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JiraIssue implements Serializable {

	private static final long serialVersionUID = 1885653801140864606L;

	public enum IssueCall {
		INCOMING(10227),
		OUTGOING(10228);

		private int id;

		private IssueCall(int id) {
			this.id = id;
		}

		public int intValue() {
			return this.id;
		}

		public static IssueCall valueOf(int id) {
			IssueCall[] values = IssueCall.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].intValue() == (id))
					return values[i];
			}
			throw new IllegalArgumentException();
		}
	}

	public enum IssueStatus {
		OPEN(1),
		CLOSED(2),
		IN_PROGRESS(3),
		REOPENED(4),
		RESOLVED(5);

		private int id;

		private IssueStatus(int id) {
			this.id = id;
		}

		public int intValue() {
			return this.id;
		}

		public static IssueStatus valueOf(int id) {
			IssueStatus[] values = IssueStatus.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].intValue() == (id))
					return values[i];
			}
			throw new IllegalArgumentException();
		}
	}

	public enum IssueType {
		COMPLAINT(11),
		ERROR(14),
		LOGIN(56),
		GENERAL_INFORMATION(57),
		ACCOUNT_MODIFICATION(59),
		REGISTRATION(60);

		private int id;

		private IssueType(int id) {
			this.id = id;
		}

		public int intValue() {
			return this.id;
		}

		public static IssueType valueOf(int id) {
			IssueType[] values = IssueType.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].intValue() == (id))
					return values[i];
			}
			throw new IllegalArgumentException();
		}
	}

	public enum IssueResolution {
		FIXED(1),
		WONT_FIX(2),
		DUPLICATE(3),
		INCOMPLETE(4),
		CANNOT_REPRODUCE(5),
		FIXED_WORKAROUND(6);

		private int id;

		private IssueResolution(int id) {
			this.id = id;
		}

		public int intValue() {
			return this.id;
		}

		public static IssueResolution valueOf(int id) {
			IssueResolution[] values = IssueResolution.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].intValue() == (id))
					return values[i];
			}
			throw new IllegalArgumentException();
		}
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
