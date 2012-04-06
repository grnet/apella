package gr.grnet.dep.service.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class RecommendatoryCommittee {

	@Id
	@GeneratedValue
	private Long id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional=false)
	private Position position;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="recommendatoryCommittee")
	private Set<RecommendatoryCommitteeMembership> members = new HashSet<RecommendatoryCommitteeMembership>();

	@ManyToOne
	private FileHeader recommendatoryReport;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	
	public Set<RecommendatoryCommitteeMembership> getMembers() {
		return members;
	}
	public void setMembers(Set<RecommendatoryCommitteeMembership> members) {
		this.members = members;
	}
	
	public FileHeader getRecommendatoryReport() {
		return recommendatoryReport;
	}
	public void setRecommendatoryReport(FileHeader recommendatoryReport) {
		this.recommendatoryReport = recommendatoryReport;
	}


}