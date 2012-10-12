package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ProfessorFile;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	private String profileURL;

	@OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProfessorFile> files = new HashSet<ProfessorFile>();

	public String getProfileURL() {
		return profileURL;
	}

	public void setProfileURL(String profileURL) {
		this.profileURL = profileURL;
	}

	/////////////////////////////////////////////////////////////

	@XmlTransient
	public Set<ProfessorFile> getFiles() {
		return files;
	}

	public void setFiles(Set<ProfessorFile> files) {
		this.files = files;
	}

	public void addFile(ProfessorFile professorFile) {
		this.files.add(professorFile);
		professorFile.setProfessor(this);
	}

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setProfileURL(p.getProfileURL());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof ProfessorDomestic)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.files.isEmpty() || profileURL == null) {
			return true;
		}
		return false;
	}
}
