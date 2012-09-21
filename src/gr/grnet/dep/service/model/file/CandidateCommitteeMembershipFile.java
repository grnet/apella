package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.CandidateCommitteeMembership;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("CandidateCommitteeMembershipFile")
public class CandidateCommitteeMembershipFile extends FileHeader {

	private static final long serialVersionUID = -6549768008032278790L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			//TODO:
			put(FileType.YPOPSIFIOTITA, -1);
		}
	});

	@ManyToOne
	private CandidateCommitteeMembership candidateCommitteeMembership;

	public CandidateCommitteeMembership getCandidateCommitteeMembership() {
		return candidateCommitteeMembership;
	}

	public void setCandidateCommitteeMembership(CandidateCommitteeMembership candidateCommitteeMembership) {
		this.candidateCommitteeMembership = candidateCommitteeMembership;
	}

}