package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class PositionEvaluator implements Serializable {

	private static final long serialVersionUID = 8362685537946833737L;

	public static interface PositionEvaluatorView {
	};

	public static interface DetailedPositionEvaluatorView extends PositionEvaluatorView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private PositionEvaluation evaluation;

	@ManyToOne
	private Professor professor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({PositionEvaluatorView.class})
	public PositionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(PositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonView({PositionEvaluatorView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

}
