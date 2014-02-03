package gr.grnet.dep.service.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

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
