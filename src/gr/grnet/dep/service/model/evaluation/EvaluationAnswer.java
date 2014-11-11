package gr.grnet.dep.service.model.evaluation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class EvaluationAnswer {

	@Id
	@GeneratedValue
	Long id;

	@ManyToOne
	Evaluation evaluation;

	@ManyToOne
	EvaluationQuestion evaluationQuestion;

	private String question;

	private Long code;

	private String answer;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public EvaluationQuestion getEvaluationQuestion() {
		return evaluationQuestion;
	}

	public void setEvaluationQuestion(EvaluationQuestion evaluationQuestion) {
		this.evaluationQuestion = evaluationQuestion;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
