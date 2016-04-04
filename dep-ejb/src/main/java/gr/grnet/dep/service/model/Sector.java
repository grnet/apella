package gr.grnet.dep.service.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@Entity
@XmlRootElement
public class Sector {

	@Id
	private Long id;

	private Long areaId;

	private Long subjectId;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "locale")
	private Map<String, SectorName> name = new HashMap<String, SectorName>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Map<String, SectorName> getName() {
		return name;
	}

	public void setName(Map<String, SectorName> name) {
		this.name = name;
	}
}
