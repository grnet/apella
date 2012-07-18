package gr.grnet.dep.service.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class RegistryDomestic {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne(optional = false)
	private Institution institution;

	@ManyToOne(optional = false)
	private Subject subject;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "registry")
	private Set<RegistryMembershipDomestic> members = new HashSet<RegistryMembershipDomestic>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Set<RegistryMembershipDomestic> getMembers() {
		return members;
	}

	public void setMembers(Set<RegistryMembershipDomestic> members) {
		this.members = members;
	}

	public RegistryDomestic copyFrom(RegistryDomestic registry) {
		setInstitution(registry.getInstitution());
		getSubject().setName(registry.getSubject().getName());

		return this;
	}

	public void initializeCollections() {
		this.members.size();
	}

}
