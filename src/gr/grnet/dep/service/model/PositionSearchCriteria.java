package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class PositionSearchCriteria {

	public static interface PositionSearchCriteriaView {
	};

	public static interface DetailedPositionSearchCriteriaView extends PositionSearchCriteriaView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Candidate candidate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedPositionSearchCriteriaView.class})
	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public void copyFrom(PositionSearchCriteria newCriteria) {
		// TODO Auto-generated method stub

	}

	public static PositionSearchCriteria valueOf(String s) {
		return new PositionSearchCriteria();
	}

}
