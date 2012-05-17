package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RecommendatoryCommitteeMembership implements Serializable {

	@Embeddable
	public static class RecommendatoryCommitteeMembershipId implements Serializable {

		@Column(name = "recommendatoryCommittee_id")
		private Long recommendatoryCommittee_id;

		@Column(name = "professor_id")
		private Long professor_id;

		public RecommendatoryCommitteeMembershipId() {

		}

		public RecommendatoryCommitteeMembershipId(Long recommendatoryCommitteeId, Long professorId) {
			this.recommendatoryCommittee_id = recommendatoryCommitteeId;
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

			RecommendatoryCommitteeMembershipId that = (RecommendatoryCommitteeMembershipId) o;

			if (professor_id != null ? !professor_id.equals(that.professor_id) : that.professor_id != null) {
				return false;
			}

			if (recommendatoryCommittee_id != null ? !recommendatoryCommittee_id.equals(that.recommendatoryCommittee_id) : that.recommendatoryCommittee_id != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (professor_id != null ? professor_id.hashCode() : 0);
			result = 31 * result + (recommendatoryCommittee_id != null ? recommendatoryCommittee_id.hashCode() : 0);
			return result;
		}
	}

	@EmbeddedId
	private RecommendatoryCommitteeMembershipId id = new RecommendatoryCommitteeMembershipId();

	@ManyToOne
	@JoinColumn(name = "recommendatoryCommittee_id", insertable = false, updatable = false)
	private RecommendatoryCommittee recommendatoryCommittee;

	@ManyToOne
	@JoinColumn(name = "professor_id", insertable = false, updatable = false)
	private Professor professor;

	public RecommendatoryCommitteeMembership() {
	}

	public RecommendatoryCommitteeMembership(RecommendatoryCommittee rc, Professor p) {
		this.professor = p;
		this.recommendatoryCommittee = rc;
		this.id.recommendatoryCommittee_id = rc.getId();
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
		RecommendatoryCommitteeMembership that = (RecommendatoryCommitteeMembership) o;
		return this.id.equals(that.id);
	}

	public RecommendatoryCommittee getRecommendatoryCommittee() {
		return recommendatoryCommittee;
	}

	public void setRecommendatoryCommittee(RecommendatoryCommittee recommendatoryCommittee) {
		this.recommendatoryCommittee = recommendatoryCommittee;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

}
