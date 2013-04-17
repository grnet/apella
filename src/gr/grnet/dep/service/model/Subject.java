package gr.grnet.dep.service.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Subject {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Transient
	private String category;

	@Column(unique = true, nullable = false)
	private String name;

	public Subject() {
		super();
	}

	public Subject(String category, String name) {
		super();
		this.category = category;
		this.name = name;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
