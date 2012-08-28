package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PositionCommitteeMember implements Serializable {

	private static final long serialVersionUID = -1339853623265264893L;

	public static final int MAX_MEMBERS = 7;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Position position;

	@ManyToOne
	private Professor professor;

	private Boolean confirmedMembership;

	@ManyToOne
	private FileHeader recommendatoryReport;

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
