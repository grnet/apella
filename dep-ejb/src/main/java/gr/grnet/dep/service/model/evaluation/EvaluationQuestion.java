package gr.grnet.dep.service.model.evaluation;

import gr.grnet.dep.service.model.Role;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class EvaluationQuestion {

	@Id
	private Long id;

	@Enumerated(EnumType.STRING)
	private Role.RoleDiscriminator role;

	private Integer orderno;

	private String question;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "evaluationquestion_answers")
	@MapKeyColumn(name = "code")
	@Column(name = "answer")
	private Map<Long, String> possibleAnswers = new HashMap<Long, String>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Role.RoleDiscriminator getRole() {
		return role;
	}

	public void setRole(Role.RoleDiscriminator role) {
		this.role = role;
	}

	public Integer getOrderno() {
		return orderno;
	}

	public void setOrderno(Integer orderno) {
		this.orderno = orderno;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public Map<Long, String> getPossibleAnswers() {
		return possibleAnswers;
	}

	public void setPossibleAnswers(Map<Long, String> possibleAnswers) {
		this.possibleAnswers = possibleAnswers;
	}
}
