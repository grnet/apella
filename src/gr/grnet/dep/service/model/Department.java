package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Department {

	@Id
	private Long id;
	
	private String school;

	private String department;

	@ManyToOne(optional=true)
	@JoinColumn(insertable=false, updatable=false)
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

	public String getFullName() {
		StringBuilder sb = new StringBuilder(getDepartment());
		if (getSchool() != null && getSchool().length() > 0) {
			sb.append(", ").append(getSchool());
		}
		if (getInstitution() != null) {
			sb.append(", ").append(getInstitution().getName());
		}

		return sb.toString();
	}

	

}
