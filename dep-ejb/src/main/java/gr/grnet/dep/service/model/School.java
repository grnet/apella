package gr.grnet.dep.service.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@Entity
@XmlRootElement
public class School {

	@Id
	private Long id;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "locale")
	private Map<String, String> name = new HashMap<String, String>();

	@ManyToOne(optional = false)
	@JoinColumn(insertable = false, updatable = false)
	private Institution institution;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Map<String, String> getName() {
		return name;
	}

	public void setName(Map<String, String> name) {
		this.name = name;
	}

}
