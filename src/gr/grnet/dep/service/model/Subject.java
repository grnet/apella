package gr.grnet.dep.service.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Subject {

	@Id
	@GeneratedValue
	private Long id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Column(nullable=false)
	private String name;
	
	

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

	
}