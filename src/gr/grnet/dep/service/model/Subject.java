package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.CompareUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Subject {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Column(unique = true, nullable = false, length = 2048)
	private String name;

	public Subject() {
		super();
	}

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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Subject)) {
			return false;
		}
		Subject that = (Subject) obj;
		return CompareUtil.equalsIgnoreNull(this.getName(), that.getName());
	}
}
