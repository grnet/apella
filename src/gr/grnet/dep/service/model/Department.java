package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
public class Department {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private String school;

	@NotNull
	@NotEmpty
	private String department;

	@ManyToOne(optional = true)
	@JoinColumn(insertable = false, updatable = false)
	private Institution institution;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String sxoli) {
		this.school = sxoli;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String tmima) {
		this.department = tmima;
	}

	public Institution getInstitution() {
		return institution;
	}

	///////////////////////////////////////////////////////////////

	@XmlTransient
	@JsonIgnore
	public String getFullName() {
		if (getDepartment() != null) {
			StringBuilder sb = new StringBuilder(getDepartment());
			if (getSchool() != null && getSchool().length() > 0) {
				sb.append(", ").append(getSchool());
			}
			if (getInstitution() != null) {
				sb.append(", ").append(getInstitution().getName());
			}
			return sb.toString();
		} else {
			return "";
		}
	}

}
