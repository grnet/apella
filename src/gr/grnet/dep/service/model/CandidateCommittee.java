package gr.grnet.dep.service.model;

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
public class CandidateCommittee {
	
	public static final int MAX_MEMBERS = 2;
	
	
	// define 3 json views
	public static interface IdCandidateCommitteeView {
	}; // shows only id view of a CandidateCommittee

	public static interface SimpleCandidateCommitteeView extends IdCandidateCommitteeView {
	}; // shows a summary view of a CandidateCommittee

	public static interface DetailedCandidateCommitteeView extends SimpleCandidateCommitteeView {
	};
	

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@ManyToOne(optional = false)
	private Candidacy candidacy;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidateCommittee", orphanRemoval = true)
	private Set<CandidateCommitteeMembership> members = new HashSet<CandidateCommitteeMembership>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedCandidateCommitteeView.class})
	public Candidacy getCandidacy() {
		return candidacy;
	}

	public void setCandidacy(Candidacy candidacy) {
		this.candidacy = candidacy;
	}

	@JsonView({SimpleCandidateCommitteeView.class})
	public Set<CandidateCommitteeMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<CandidateCommitteeMembership> members) {
		this.members = members;
	}

	
	///////////////////////////////////////////////////////////////////////////////////////

	
	public void addMember(CandidateCommitteeMembership ccm) {
		members.add(ccm);
		ccm.setCandidateCommittee(this);
	}
	
	public CandidateCommitteeMembership removeMember(Long id) {
		CandidateCommitteeMembership removed = null;
		Iterator<CandidateCommitteeMembership> it = getMembers().iterator();
		while (it.hasNext()) {
			CandidateCommitteeMembership ccm = it.next();
			if (ccm.getId().equals(id)) {
				it.remove();
				removed = ccm;
			}
		}
		return removed;
	}
	
}
