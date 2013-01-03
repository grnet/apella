package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.file.PositionEvaluationFile;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class PositionEvaluation {

	public static interface PositionEvaluationView {
	};

	public static interface DetailedPositionEvaluationView extends PositionEvaluationView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionEvaluator> evaluators = new HashSet<PositionEvaluator>();

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionEvaluationFile> files = new HashSet<PositionEvaluationFile>();

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
	public Set<PositionPhase> getPhases() {
		return phases;
	}

	public void setPhases(Set<PositionPhase> phases) {
		this.phases = phases;
	}

	@XmlTransient
	public Set<PositionEvaluationFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionEvaluationFile> files) {
		this.files = files;
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

	//////////////////////////////////////////////

	public void addEvaluator(PositionEvaluator evaluator) {
		evaluator.setEvaluation(this);
		this.evaluators.add(evaluator);
	}

	public void addFile(PositionEvaluationFile file) {
		file.setEvaluation(this);
		this.files.add(file);
	}

	public void copyFrom(PositionEvaluation other) {
		this.setUpdatedAt(new Date());
	}

	public void initializeCollections() {
		this.files.size();
	}
}
