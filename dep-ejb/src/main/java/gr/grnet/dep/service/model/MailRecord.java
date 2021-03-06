package gr.grnet.dep.service.model;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = {"toEmailAddr", "subject", "body"})
})
public class MailRecord {

	@Id
	@GeneratedValue
	private Long id;

	private String toEmailAddr;

	private String subject;

	@Column(columnDefinition = "TEXT")
	private String body;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToEmailAddr() {
		return toEmailAddr;
	}

	public void setToEmailAddr(String toEmailAddr) {
		this.toEmailAddr = toEmailAddr;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "MailRecord{" +
				"toEmailAddr='" + toEmailAddr + '\'' +
				", subject='" + subject + '\'' +
				", body='" + body + '\'' +
				'}';
	}
}
