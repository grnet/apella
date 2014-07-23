package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
public class PositionEvaluation {

	public static interface PositionEvaluationView {
	}

	public static interface DetailedPositionEvaluationView extends PositionEvaluationView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PositionEvaluator> evaluators = new HashSet<PositionEvaluator>();

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

	@JsonView({DetailedPositionEvaluationView.class, PositionEvaluatorView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@JsonView({DetailedPositionEvaluationView.class})
	public Set<PositionEvaluator> getEvaluators() {
		return evaluators;
	}

	public void setEvaluators(Set<PositionEvaluator> evaluators) {
		this.evaluators = evaluators;
	}

	@XmlTransient
	@JsonIgnore
	public Set<PositionPhase> getPhases() {
		return phases;
	}

	public void setPhases(Set<PositionPhase> phases) {
		this.phases = phases;
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

	@JsonView({DetailedPositionEvaluationView.class})
	public boolean canUpdateEvaluators() {
		Set<PositionCommitteeFile> committeeFiles = FileHeader.filter(this.getPosition().getPhase().getCommittee().getFiles(), FileType.PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES);
		return !committeeFiles.isEmpty();
	}

	@JsonView({DetailedPositionEvaluationView.class})
	public boolean canUploadEvaluations() {
		Set<PositionCommitteeFile> committeeFiles = FileHeader.filter(this.getPosition().getPhase().getCommittee().getFiles(), FileType.AITIMA_EPITROPIS_PROS_AKSIOLOGITES);
		return !committeeFiles.isEmpty();
	}

	//////////////////////////////////////////////

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void addEvaluator(PositionEvaluator evaluator) {
		evaluator.setEvaluation(this);
		this.evaluators.add(evaluator);
	}

	public void copyFrom(PositionEvaluation other) {
		this.setUpdatedAt(new Date());
	}

	public boolean containsEvaluator(User user) {
		for (PositionEvaluator member : this.evaluators) {
			if (member.getRegisterMember().getProfessor().getUser().getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}
}
