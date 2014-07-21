package gr.grnet.dep.service.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@Entity
@XmlRootElement
public class Rank {

	public enum Category {
		PROFESSOR, RESEARCHER
	}

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@NotEmpty
	@Enumerated(EnumType.STRING)
	private Category category;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "locale")
	private Map<String, String> name = new HashMap<String, String>();

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

	public Map<String, String> getName() {
		return name;
	}

	public void setName(Map<String, String> name) {
		this.name = name;
	}

}
