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
public class RegisterDomestic {

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Institution institution;

	@ManyToOne(optional = false)
	private Subject subject;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "register")
	private Set<RegisterDomesticMembership> members = new HashSet<RegisterDomesticMembership>();

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

	public Set<RegisterDomesticMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<RegisterDomesticMembership> members) {
		this.members = members;
	}

}
