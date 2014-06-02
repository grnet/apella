package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileHeader;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.inject.Inject;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Entity
@XmlRootElement
@DiscriminatorValue("CANDIDATE")
public class Candidate extends Role {

	private static final long serialVersionUID = 7138046612269179606L;

	@Inject
	@Transient
	private Logger logger;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<CandidateFile> files = new HashSet<CandidateFile>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidate", fetch = FetchType.LAZY)
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

	public Candidate() {
		super();
		setDiscriminator(RoleDiscriminator.CANDIDATE);
	}

	@XmlTransient
	@JsonIgnore
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	@XmlTransient
	@JsonIgnore
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
	public Role copyFrom(Role otherRole) {
		Candidate c = (Candidate) otherRole;
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof Candidate)) {
			return false;
		}
		return true;
	}

	@Override
	@XmlTransient
	public boolean isMissingRequiredFields() {
		boolean hasTAYTOTHTAFile = false;
		boolean hasFORMA_SYMMETOXISFile = false;
		for (CandidateFile file : FileHeader.filterDeleted(files)) {
			switch (file.getType()) {
				case TAYTOTHTA:
					hasTAYTOTHTAFile = true;
					break;
				case FORMA_SYMMETOXIS:
					hasFORMA_SYMMETOXISFile = true;
					break;
				default:
			}
		}
		if (!hasTAYTOTHTAFile) {
			return true;
		}
		if (!hasFORMA_SYMMETOXISFile) {
			return true;
		}
		return false;
	}
}
