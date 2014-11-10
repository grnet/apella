package gr.grnet.dep.service.model.evaluation;

import gr.grnet.dep.service.model.Role;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
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
	@MapKeyColumn(name = "code")
	private Map<String, String> possibleAnswers = new HashMap<String, String>();

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

	public Map<String, String> getPossibleAnswers() {
		return possibleAnswers;
	}

	public void setPossibleAnswers(Map<String, String> possibleAnswers) {
		this.possibleAnswers = possibleAnswers;
	}
}
