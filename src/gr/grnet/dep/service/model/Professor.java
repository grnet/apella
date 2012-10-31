package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ProfessorFile;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	private String identification;

	private String profileURL;

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProfessorFile> files = new HashSet<ProfessorFile>();

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionCommitteeMember> committees = new HashSet<PositionCommitteeMember>();

	@Transient
	private Integer committeesCount;

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getProfileURL() {
		return profileURL;
	}

	public void setProfileURL(String profileURL) {
		this.profileURL = profileURL;
	}

	@XmlTransient
	public Set<ProfessorFile> getFiles() {
		return files;
	}

	public void setFiles(Set<ProfessorFile> files) {
		this.files = files;
	}

	@XmlTransient
	public Set<PositionCommitteeMember> getCommittees() {
		return committees;
	}

	public void setCommittees(Set<PositionCommitteeMember> committees) {
		this.committees = committees;
	}

	public Integer getCommitteesCount() {
		return committeesCount;
	}

	public void setCommitteesCount(Integer committeesCount) {
		this.committeesCount = committeesCount;
	}

	/////////////////////////////////////////////////////

	public void addFile(ProfessorFile professorFile) {
		this.files.add(professorFile);
		professorFile.setProfessor(this);
	}

	@Override
	public void initializeCollections() {
		this.getFiles().size();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setIdentification(p.getIdentification());
		setProfileURL(p.getProfileURL());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof ProfessorDomestic)) {
			return false;
		}
		Professor other = (Professor) role;
		if (!compare(this.identification, other.getIdentification())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.identification == null) {
			return true;
		}
		return false;
	}
}
