package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	private String profileURL;

	@ManyToOne
	private FileHeader profileFile;

	public String getProfileURL() {
		return profileURL;
	}

	public void setProfileURL(String profileURL) {
		this.profileURL = profileURL;
	}

	public FileHeader getProfileFile() {
		return profileFile;
	}

	public void setProfileFile(FileHeader profileFile) {
		this.profileFile = profileFile;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setProfileURL(p.getProfileURL());
		return this;
	}

}
