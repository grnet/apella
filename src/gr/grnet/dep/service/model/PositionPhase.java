package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Position.PositionStatus;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

	private PositionStatus status;

	@OrderColumn
	@Column(name = "orderno")
	private Integer order;

	@ManyToOne(cascade = CascadeType.ALL)
	private Candidacies candidacies;

	@ManyToOne(cascade = CascadeType.ALL)
	private Committee committee;

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

	public Candidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Candidacies candidacies) {
		this.candidacies = candidacies;
	}

	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Nomination getNomination() {
		return nomination;
	}

	public void setNomination(Nomination nomination) {
		this.nomination = nomination;
	}

	public ComplementaryDocuments getComplementaryDocuments() {
		return complementaryDocuments;
	}

	public void setComplementaryDocuments(ComplementaryDocuments complementaryDocuments) {
		this.complementaryDocuments = complementaryDocuments;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
