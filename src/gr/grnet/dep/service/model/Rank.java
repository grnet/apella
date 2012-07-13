package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Rank {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@NotEmpty
	private String category;

	@NotNull
	@NotEmpty
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

	public String getCategory() {
		return category;
	}

	public void setType(String category) {
		this.category = category;
	}

}
