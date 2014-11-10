package gr.grnet.dep.service.model.evaluation;

import gr.grnet.dep.service.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Evaluation {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private User user;

	private Integer year;

	@OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	List<EvaluationAnswer> answers = new ArrayList<EvaluationAnswer>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public List<EvaluationAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<EvaluationAnswer> answers) {
		this.answers = answers;
	}
}
