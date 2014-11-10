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
	EvaluationQuestion question;

	private String answer; // One of keys in EvaluationQuestion.possibleAnswers

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

	public EvaluationQuestion getQuestion() {
		return question;
	}

	public void setQuestion(EvaluationQuestion question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
