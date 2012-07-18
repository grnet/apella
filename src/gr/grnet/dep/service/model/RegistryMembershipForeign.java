package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"registry_id", "professor_id"}))
public class RegistryMembershipForeign implements Serializable {

	private static final long serialVersionUID = 942192432715278689L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private RegistryForeign registry;

	@ManyToOne
	private ProfessorForeign professor;

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RegistryMembershipForeign that = (RegistryMembershipForeign) o;
		return this.id.equals(that.id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RegistryForeign getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryForeign registry) {
		this.registry = registry;
	}

	public ProfessorForeign getProfessor() {
		return professor;
	}

	public void setProfessor(ProfessorForeign professor) {
		this.professor = professor;
	}

}
