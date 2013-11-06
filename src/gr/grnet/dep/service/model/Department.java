package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
public class Department {

	@Id
	private Long id;

	@NotNull
	@NotEmpty
	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(insertable = false, updatable = false)
	private School school;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}

	///////////////////////////////////////////////////////////////

	@XmlTransient
	@JsonIgnore
	public String getFullName() {
		return school.getInstitution().getName() + ", " + school.getName() + ", " + this.name;
	}

}
