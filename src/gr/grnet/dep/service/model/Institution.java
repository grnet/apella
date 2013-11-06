package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
public class Institution {

	@Id
	private Long id;

	@NotNull
	@NotEmpty
	private String name;

	@NotNull
	@Enumerated(EnumType.STRING)
	private InstitutionCategory category;

	@NotNull
	@Enumerated(EnumType.STRING)
	private UserRegistrationType registrationType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public InstitutionCategory getCategory() {
		return category;
	}

	public void setCategory(InstitutionCategory category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserRegistrationType getRegistrationType() {
		return registrationType;
	}

	public void setRegistrationType(UserRegistrationType registrationType) {
		this.registrationType = registrationType;
	}

}
