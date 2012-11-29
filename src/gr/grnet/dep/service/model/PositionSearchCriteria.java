package gr.grnet.dep.service.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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

	@ManyToMany
	private Set<Department> departments = new HashSet<Department>();

	@ManyToMany
	private Set<Subject> subjects = new HashSet<Subject>();

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

	public Set<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	public Set<Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(Set<Subject> subjects) {
		this.subjects = subjects;
	}

	////////////////////////////////////////

	public static PositionSearchCriteria valueOf(String s) {
		return new PositionSearchCriteria();
	}

	public void initializeCollections() {
		this.departments.size();
		this.subjects.size();
	}

}
