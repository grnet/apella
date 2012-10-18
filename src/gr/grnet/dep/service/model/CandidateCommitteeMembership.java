package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class CandidateCommitteeMembership implements Serializable {

	private static final long serialVersionUID = -8436764731554690767L;


	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne
	@JoinColumn(name = "candidateCommittee_id")
	private CandidateCommittee candidateCommittee;

	@NotNull
	private String fullname;
	
	@NotNull
	private String email;
	
	
	public CandidateCommitteeMembership() {
	}

	public CandidateCommitteeMembership(CandidateCommittee cc, String email) {
		this.candidateCommittee = cc;
		this.email = email;
	}

	@Override
	public int hashCode() {	
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CandidateCommitteeMembership that = (CandidateCommitteeMembership) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}

		if (email != null ? !email.equals(that.email) : that.email != null) {
			return false;
		}

		return true;
	}

	@XmlTransient
	public CandidateCommittee getCandidateCommittee() {
		return candidateCommittee;
	}

	public void setCandidateCommittee(CandidateCommittee candidateCommittee) {
		this.candidateCommittee = candidateCommittee;
	}

	
	public Long getId() {
		return id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	
	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}


}
