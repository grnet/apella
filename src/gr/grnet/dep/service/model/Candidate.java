package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.CandidateFile;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@DiscriminatorValue("CANDIDATE")
public class Candidate extends Role {

	private static final long serialVersionUID = 7138046612269179606L;

	@Inject
	@Transient
	private Logger logger;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CandidateFile> files = new HashSet<CandidateFile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidate")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

	public Candidate() {
		super();
		setDiscriminator(RoleDiscriminator.CANDIDATE);
	}

	@XmlTransient
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	public Set<CandidateFile> getFiles() {
		return files;
	}

	public void setFiles(Set<CandidateFile> files) {
		this.files = files;
	}

	///////////////////////////////////////////////////////////////////////////////

	public void addFile(CandidateFile file) {
		this.files.add(file);
		file.setCandidate(this);
	}

	public void addCandidacy(Candidacy candidacy) {
		this.candidacies.add(candidacy);
		candidacy.setCandidate(this);
	}

	@Override
	public void initializeCollections() {
		getCandidacies().size();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		Candidate c = (Candidate) otherRole;
		return this;
	}

	@Override
	public boolean isMissingRequiredFields() {
		//TODO:
		/*
		if (this.cv == null) {
			return true;
		}
		if (this.degrees == null) {
			return true;
		}
		if (this.identity == null) {
			return true;
		}
		if (this.military1599 == null) {
			return true;
		}
		if (this.publications == null) {
			return true;
		}
		*/
		return false;
	}
}
