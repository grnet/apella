package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.ElectoralBody.SimpleElectoralBodyView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class RecommendatoryCommittee {
	
	public static final int MAX_MEMBERS = 3;
	
	
	// define 3 json views
	public static interface IdRecommendatoryCommitteeView {
	}; // shows only id view of a RecommendatoryCommittee

	public static interface SimpleRecommendatoryCommitteeView extends IdRecommendatoryCommitteeView {
	}; // shows a summary view of a RecommendatoryCommittee

	public static interface DetailedRecommendatoryCommitteeView extends SimpleRecommendatoryCommitteeView {
	};
	

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Position position;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "recommendatoryCommittee", orphanRemoval = true)
	private Set<RecommendatoryCommitteeMembership> members = new HashSet<RecommendatoryCommitteeMembership>();

	@ManyToOne
	private FileHeader recommendatoryReport;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView(SimpleRecommendatoryCommitteeView.class)
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@JsonView(SimpleRecommendatoryCommitteeView.class)
	public Set<RecommendatoryCommitteeMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<RecommendatoryCommitteeMembership> members) {
		this.members = members;
	}

	@JsonView(SimpleRecommendatoryCommitteeView.class)
	public FileHeader getRecommendatoryReport() {
		return recommendatoryReport;
	}

	public void setRecommendatoryReport(FileHeader recommendatoryReport) {
		this.recommendatoryReport = recommendatoryReport;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////

	
	public void addMember(Professor professor) {
		RecommendatoryCommitteeMembership rcm = new RecommendatoryCommitteeMembership(this, professor);
		members.add(rcm);
	}
	
	public RecommendatoryCommitteeMembership removeMember(Professor professor) {
		RecommendatoryCommitteeMembership removed = null;
		RecommendatoryCommitteeMembership membership = new RecommendatoryCommitteeMembership(this, professor);
		Iterator<RecommendatoryCommitteeMembership> it = getMembers().iterator();
		while (it.hasNext()) {
			RecommendatoryCommitteeMembership rcm = it.next();
			if (rcm.equals(membership)) {
				it.remove();
				removed = rcm;
			}
		}
		return removed;
	}

}
