package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.CommitteeMember.ProfessorCommitteesView;
import gr.grnet.dep.service.model.Position.CandidatePositionView;
import gr.grnet.dep.service.model.Position.CommitteeMemberPositionView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class PositionPhase {

	public static interface DetailedPositionPhaseView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Position position;

	@Enumerated(EnumType.STRING)
	private PositionStatus status;

	@OrderColumn
	@Column(name = "orderno")
	private Integer order;

	@ManyToOne(cascade = CascadeType.ALL)
	private Candidacies candidacies;

	@ManyToOne(cascade = CascadeType.ALL)
	private Committee committee;

	@ManyToOne(cascade = CascadeType.ALL)
	private Evaluation evaluation;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nomination nomination;

	@ManyToOne(cascade = CascadeType.ALL)
	private ComplementaryDocuments complementaryDocuments;

	@Temporal(TemporalType.DATE)
	private Date createdAt;

	@Temporal(TemporalType.DATE)
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedPositionPhaseView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public PositionStatus getStatus() {
		return status;
	}

	public void setStatus(PositionStatus status) {
		this.status = status;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@JsonView({PublicPositionView.class, ProfessorCommitteesView.class, DetailedPositionView.class, CommitteeMemberPositionView.class, CandidatePositionView.class})
	public Candidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Candidacies candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView({DetailedPositionView.class, CommitteeMemberPositionView.class, CandidatePositionView.class})
	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	@JsonView({DetailedPositionView.class, CommitteeMemberPositionView.class})
	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonView({DetailedPositionView.class, CommitteeMemberPositionView.class, CandidatePositionView.class})
	public Nomination getNomination() {
		return nomination;
	}

	public void setNomination(Nomination nomination) {
		this.nomination = nomination;
	}

	@JsonView({DetailedPositionView.class, CommitteeMemberPositionView.class})
	public ComplementaryDocuments getComplementaryDocuments() {
		return complementaryDocuments;
	}

	public void setComplementaryDocuments(ComplementaryDocuments complementaryDocuments) {
		this.complementaryDocuments = complementaryDocuments;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCreatedAt() {
		return createdAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
