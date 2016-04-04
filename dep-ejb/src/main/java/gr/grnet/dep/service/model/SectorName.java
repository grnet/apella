package gr.grnet.dep.service.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SectorName {

	@Column(name = "area")
	private String area;

	@Column(name = "subject")
	private String subject;

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
