package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Rank {

	public enum Category {
		PROFESSOR, RESEARCHER
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@NotEmpty
	@Enumerated(EnumType.STRING)
	private Category category;

	@NotNull
	@NotEmpty
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
