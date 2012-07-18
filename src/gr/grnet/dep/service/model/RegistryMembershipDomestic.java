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
public class RegistryMembershipDomestic implements Serializable {

	private static final long serialVersionUID = -705538054348397004L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private RegistryDomestic registry;

	@ManyToOne
	private ProfessorDomestic professor;

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
		RegistryMembershipDomestic that = (RegistryMembershipDomestic) o;
		return this.id.equals(that.id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RegistryDomestic getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryDomestic registry) {
		this.registry = registry;
	}

	public ProfessorDomestic getProfessor() {
		return professor;
	}

	public void setProfessor(ProfessorDomestic professor) {
		this.professor = professor;
	}

}
