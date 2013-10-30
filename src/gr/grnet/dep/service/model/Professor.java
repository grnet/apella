package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ProfessorFile;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	private String profileURL;

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProfessorFile> files = new HashSet<ProfessorFile>();

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<RegisterMember> registerMemberships = new HashSet<RegisterMember>();

	@Transient
	private Integer committeesCount;

	@Transient
	private Integer evaluationsCount;

	public Boolean hasOnlineProfile = true;

	public boolean getHasOnlineProfile() {
		return hasOnlineProfile;
	}

	public void setHasOnlineProfile(boolean hasOnlineProfile) {
		this.hasOnlineProfile = hasOnlineProfile;
	}

	public String getProfileURL() {
		return profileURL;
	}

	public void setProfileURL(String profileURL) {
		this.profileURL = profileURL;
	}

	@XmlTransient
	@JsonIgnore
	public Set<ProfessorFile> getFiles() {
		return files;
	}

	public void setFiles(Set<ProfessorFile> files) {
		this.files = files;
	}

	@XmlTransient
	@JsonIgnore
	public Set<RegisterMember> getRegisterMemberships() {
		return registerMemberships;
	}

	public void setRegisterMemberships(Set<RegisterMember> registerMemberships) {
		this.registerMemberships = registerMemberships;
	}

	public Integer getCommitteesCount() {
		return committeesCount;
	}

	public void setCommitteesCount(Integer committeesCount) {
		this.committeesCount = committeesCount;
	}

	public Integer getEvaluationsCount() {
		return evaluationsCount;
	}

	public void setEvaluationsCount(Integer evaluationsCount) {
		this.evaluationsCount = evaluationsCount;
	}

	/////////////////////////////////////////////////////

	public void addFile(ProfessorFile professorFile) {
		this.files.add(professorFile);
		professorFile.setProfessor(this);
	}

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setHasOnlineProfile(p.getHasOnlineProfile());
		setProfileURL(p.getProfileURL());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof Professor)) {
			return false;
		}
		Professor other = (Professor) role;
		return true;
	}

	@Override
	@XmlTransient
	@JsonIgnore
	public boolean isMissingRequiredFields() {
		return false;
	}

	public static int countCommittees(Professor p) {
		int count = 0;
		for (RegisterMember professorMembership : p.getRegisterMemberships()) {
			count += professorMembership.getCommittees().size();
		}
		return count;
	}

	public static int countEvaluations(Professor p) {
		int count = 0;
		for (RegisterMember professorMembership : p.getRegisterMemberships()) {
			count += professorMembership.getEvaluations().size();
		}
		return count;
	}
}
