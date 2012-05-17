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
public class ElectoralBody {

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Position position;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "electoralBody")
	private Set<ElectoralBodyMembership> members = new HashSet<ElectoralBodyMembership>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Set<ElectoralBodyMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<ElectoralBodyMembership> members) {
		this.members = members;
	}

}
