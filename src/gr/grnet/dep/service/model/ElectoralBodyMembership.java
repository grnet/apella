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
public class ElectoralBodyMembership implements Serializable {

	@Embeddable
	public static class ElectoralBodyMembershipId implements Serializable {

		@Column(name = "electoralBody_id")
		private Long electoralBody_id;

		@Column(name = "professor_id")
		private Long professor_id;

		public ElectoralBodyMembershipId() {

		}

		public ElectoralBodyMembershipId(Long electoralBodyId, Long professorId) {
			this.electoralBody_id = electoralBodyId;
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

			ElectoralBodyMembershipId that = (ElectoralBodyMembershipId) o;

			if (professor_id != null ? !professor_id.equals(that.professor_id) : that.professor_id != null) {
				return false;
			}

			if (electoralBody_id != null ? !electoralBody_id.equals(that.electoralBody_id) : that.electoralBody_id != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (professor_id != null ? professor_id.hashCode() : 0);
			result = 31 * result + (electoralBody_id != null ? electoralBody_id.hashCode() : 0);
			return result;
		}
	}

	@EmbeddedId
	private ElectoralBodyMembershipId id = new ElectoralBodyMembershipId();

	@ManyToOne
	@JoinColumn(name = "electoralBody_id", insertable = false, updatable = false)
	private ElectoralBody electoralBody;

	@ManyToOne
	@JoinColumn(name = "professor_id", insertable = false, updatable = false)
	private Professor professor;

	private Boolean confirmedMembership;

	@ManyToOne
	private FileHeader recommendatoryReport;

	public ElectoralBodyMembership() {
	}

	public ElectoralBodyMembership(ElectoralBody eb, Professor p) {
		this.professor = p;
		this.electoralBody = eb;
		this.id.electoralBody_id = eb.getId();
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
		ElectoralBodyMembership that = (ElectoralBodyMembership) o;
		return this.id.equals(that.id);
	}

	@XmlTransient
	public ElectoralBody getElectoralBody() {
		return electoralBody;
	}

	public void setElectoralBody(ElectoralBody electoralBody) {
		this.electoralBody = electoralBody;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public Boolean getConfirmedMembership() {
		return confirmedMembership;
	}

	public void setConfirmedMembership(Boolean confirmedMembership) {
		this.confirmedMembership = confirmedMembership;
	}

	public FileHeader getRecommendatoryReport() {
		return recommendatoryReport;
	}

	public void setRecommendatoryReport(FileHeader recommendatoryReport) {
		this.recommendatoryReport = recommendatoryReport;
	}

}
