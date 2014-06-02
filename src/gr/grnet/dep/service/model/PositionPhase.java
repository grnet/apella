package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Candidacy.DetailedCandidacyView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Position.PublicPositionView;
import gr.grnet.dep.service.model.PositionCommitteeMember.PositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@XmlRootElement
public class PositionPhase {

	public static interface DetailedPositionPhaseView {
	}

	;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@Enumerated(EnumType.STRING)
	private PositionStatus status;

	@OrderColumn
	@Column(name = "orderno")
	private Integer order;

	@ManyToOne(cascade = CascadeType.ALL)
	private PositionCandidacies candidacies;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PositionCommittee committee;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PositionEvaluation evaluation;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PositionNomination nomination;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private PositionComplementaryDocuments complementaryDocuments;

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

	public String getClientStatus() {
		String clientStatus = null;
		if (status.equals(PositionStatus.ANOIXTI)
				&& DateUtil.compareDates(new Date(), this.candidacies.getClosingDate()) > 0) {
			clientStatus = "KLEISTI";
		} else {
			clientStatus = status.toString();
		}
		return clientStatus;
	}

	public String getClientStatusInGreek() {
		String clientStatus = null;
		if (status.equals(PositionStatus.ANOIXTI)
				&& DateUtil.compareDates(new Date(), this.candidacies.getClosingDate()) > 0) {
			clientStatus = "Κλειστή";
		} else {
			clientStatus = status.toGreekString();
		}
		return clientStatus;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@JsonView({PublicPositionView.class, PositionCommitteeMemberView.class, DetailedPositionView.class, PositionEvaluatorView.class})
	public PositionCandidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(PositionCandidacies candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView({DetailedPositionView.class, DetailedCandidacyView.class})
	public PositionCommittee getCommittee() {
		return committee;
	}

	public void setCommittee(PositionCommittee committee) {
		this.committee = committee;
	}

	@JsonView({DetailedPositionView.class})
	public PositionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(PositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonView({DetailedPositionView.class})
	public PositionNomination getNomination() {
		return nomination;
	}

	public void setNomination(PositionNomination nomination) {
		this.nomination = nomination;
	}

	@JsonView({DetailedPositionView.class})
	public PositionComplementaryDocuments getComplementaryDocuments() {
		return complementaryDocuments;
	}

	public void setComplementaryDocuments(PositionComplementaryDocuments complementaryDocuments) {
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
