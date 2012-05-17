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
public class RegisterForeign {

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Subject subject;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "register")
	private Set<RegisterForeignMembership> members = new HashSet<RegisterForeignMembership>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Set<RegisterForeignMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<RegisterForeignMembership> members) {
		this.members = members;
	}

}
