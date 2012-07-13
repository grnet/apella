package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class CandidateCommitteeMembership implements Serializable {

	private static final long serialVersionUID = -8436764731554690766L;

	@Embeddable
	public static class CandidateCommitteeMembershipId implements Serializable {

		private static final long serialVersionUID = -3146562158610027791L;

		@Column(name = "candidateCommittee_id")
		private Long candidateCommittee_id;

		@Column(name = "professor_id")
		private Long professor_id;

		public CandidateCommitteeMembershipId() {

		}

		public CandidateCommitteeMembershipId(Long candidateCommitteeId, Long professorId) {
			this.candidateCommittee_id = candidateCommitteeId;
			this.professor_id = professorId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			CandidateCommitteeMembershipId that = (CandidateCommitteeMembershipId) o;

			if (professor_id != null ? !professor_id.equals(that.professor_id) : that.professor_id != null) {
				return false;
			}

			if (candidateCommittee_id != null ? !candidateCommittee_id.equals(that.candidateCommittee_id) : that.candidateCommittee_id != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (professor_id != null ? professor_id.hashCode() : 0);
			result = 31 * result + (candidateCommittee_id != null ? candidateCommittee_id.hashCode() : 0);
			return result;
		}
	}

	@EmbeddedId
	private CandidateCommitteeMembershipId id = new CandidateCommitteeMembershipId();

	@ManyToOne
	@JoinColumn(name = "candidateCommittee_id", insertable = false, updatable = false)
	private CandidateCommittee candidateCommittee;

	@ManyToOne
	@JoinColumn(name = "professor_id", insertable = false, updatable = false)
	private Professor professor;

	@ManyToOne
	private FileHeader report;

	public CandidateCommitteeMembership() {
	}

	public CandidateCommitteeMembership(CandidateCommittee cc, Professor p) {
		this.professor = p;
		this.candidateCommittee = cc;
		this.id.candidateCommittee_id = cc.getId();
		this.id.professor_id = p.getId();
	}

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
		CandidateCommitteeMembership that = (CandidateCommitteeMembership) o;
		return this.id.equals(that.id);
	}

	@XmlTransient
	public CandidateCommittee getCandidateCommittee() {
		return candidateCommittee;
	}

	public void setCandidateCommittee(CandidateCommittee candidateCommittee) {
		this.candidateCommittee = candidateCommittee;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public FileHeader getReport() {
		return report;
	}

	public void setReport(FileHeader report) {
		this.report = report;
	}

}
