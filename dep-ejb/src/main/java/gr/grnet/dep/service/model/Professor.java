package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.grnet.dep.service.model.file.ProfessorFile;
import gr.grnet.dep.service.util.CompareUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

@Entity
public abstract class Professor extends Role {

	/**
	 * Default value included to remove warning. Remove or modify at will. *
	 */
	private static final long serialVersionUID = 1L;

	private String profileURL;

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProfessorFile> files = new HashSet<ProfessorFile>();

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<RegisterMember> registerMemberships = new HashSet<RegisterMember>();

	@Transient
	private Long committeesCount;

	@Transient
	private Long evaluationsCount;

	public Boolean hasOnlineProfile = true;

	public Boolean hasAcceptedTerms = false;

	public boolean getHasOnlineProfile() {
		return hasOnlineProfile;
	}

	public void setHasOnlineProfile(boolean hasOnlineProfile) {
		this.hasOnlineProfile = hasOnlineProfile;
	}

	public Boolean getHasAcceptedTerms() {
		return hasAcceptedTerms;
	}

	public void setHasAcceptedTerms(Boolean hasAcceptedTerms) {
		this.hasAcceptedTerms = hasAcceptedTerms;
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

	public Long getCommitteesCount() {
		return committeesCount;
	}

	public void setCommitteesCount(Long committeesCount) {
		this.committeesCount = committeesCount;
	}

	public Long getEvaluationsCount() {
		return evaluationsCount;
	}

	public void setEvaluationsCount(Long evaluationsCount) {
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
		setHasAcceptedTerms(p.getHasAcceptedTerms());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof Professor)) {
			return false;
		}
		Professor other = (Professor) role;
		if (!CompareUtil.equalsIgnoreNull(this.hasOnlineProfile, other.getHasOnlineProfile())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.profileURL, other.getProfileURL())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.hasAcceptedTerms, other.getHasAcceptedTerms())) {
			return false;
		}
		return true;
	}

	@Override
	@XmlTransient
	@JsonIgnore
	public boolean isMissingRequiredFields() {
		if (this.hasOnlineProfile == null) {
			return true;
		}
		if (this.hasOnlineProfile && this.profileURL == null) {
			return true;
		}
		if (!this.hasOnlineProfile) {
			boolean hasProfileFile = false;
			for (ProfessorFile file : this.getFiles()) {
				switch (file.getType()) {
					case PROFILE:
						hasProfileFile = true;
						break;
					default:
				}
			}
			if (!hasProfileFile) {
				return true;
			}
		}
		if (this.hasAcceptedTerms == null) {
			return true;
		}
		return false;
	}
}
