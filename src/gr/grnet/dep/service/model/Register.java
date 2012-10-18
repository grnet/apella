package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.RegisterFile;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Register implements Serializable {

	private static final long serialVersionUID = 6147648073392341863L;

	public static interface DetailedRegisterView {
	};

	@Id
	@GeneratedValue
	private Long id;

	private String title;

	@ManyToOne(optional = false)
	private Institution institution;

	@OneToMany(mappedBy = "register", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RegisterFile> files = new HashSet<RegisterFile>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Set<RegisterFile> getFiles() {
		return files;
	}

	public void setFiles(Set<RegisterFile> files) {
		this.files = files;
	}

	public void addFile(RegisterFile file) {
		this.files.add(file);
		file.setRegister(this);
	}

	public Register copyFrom(Register register) {
		setTitle(register.getTitle());
		setInstitution(register.getInstitution());
		return this;
	}

	public void initializeCollections() {
		this.files.size();
	}
}
