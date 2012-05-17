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
public class CandidateCommittee {

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Candidacy candidacy;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidateCommittee")
	private Set<CandidateCommitteeMembership> members = new HashSet<CandidateCommitteeMembership>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Candidacy getCandidacy() {
		return candidacy;
	}

	public void setCandidacy(Candidacy candidacy) {
		this.candidacy = candidacy;
	}

	public Set<CandidateCommitteeMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<CandidateCommitteeMembership> members) {
		this.members = members;
	}

}
