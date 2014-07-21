package gr.grnet.dep.service.model.system;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@XmlRootElement
public class Notification {

	@Id
	@GeneratedValue
	private Long id;

	private String type;

	private Date date;

	private String status;

	private Long referredEntityId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getReferredEntityId() {
		return referredEntityId;
	}

	public void setReferredEntityId(Long referredEntityId) {
		this.referredEntityId = referredEntityId;
	}

}
