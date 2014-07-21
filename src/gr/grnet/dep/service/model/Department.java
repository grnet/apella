package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

@Entity
@XmlRootElement
public class Department {

	@Id
	private Long id;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "locale")
	private Map<String, String> name = new HashMap<String, String>();

	@ManyToOne(optional = false)
	@JoinColumn(insertable = false, updatable = false)
	private School school;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, String> getName() {
		return name;
	}

	public void setName(Map<String, String> name) {
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
