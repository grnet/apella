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

	public static final int MAX_MEMBERS = 2;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private PositionEvaluation evaluation;

	@ManyToOne
	private RegisterMember registerMember;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedPositionEvaluatorView.class})
	public PositionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(PositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonView({PositionEvaluatorView.class})
	public RegisterMember getRegisterMember() {
		return registerMember;
	}

	public void setRegisterMember(RegisterMember registerMember) {
		this.registerMember = registerMember;
	}

}
