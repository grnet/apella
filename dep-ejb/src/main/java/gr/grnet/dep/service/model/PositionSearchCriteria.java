package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
public class PositionSearchCriteria {

	public static interface PositionSearchCriteriaView {
	}

	public static interface DetailedPositionSearchCriteriaView extends PositionSearchCriteriaView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Candidate candidate;

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Department> departments = new HashSet<Department>();

	@ManyToMany(fetch = FetchType.EAGER)
	private Set<Sector> sectors = new HashSet<Sector>();

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

	public Set<Sector> getSectors() {
		return sectors;
	}

	public void setSectors(Set<Sector> sectors) {
		this.sectors = sectors;
	}

	////////////////////////////////////////

}
