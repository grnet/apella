package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.InstitutionFile;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Institution {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@NotEmpty
	private String name;

	@NotNull
	@NotEmpty
	@Enumerated(EnumType.STRING)
	private UserRegistrationType registrationType = UserRegistrationType.SHIBBOLETH;

	@OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<InstitutionFile> files = new HashSet<InstitutionFile>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserRegistrationType getRegistrationType() {
		return registrationType;
	}

	public void setRegistrationType(UserRegistrationType registrationType) {
		this.registrationType = registrationType;
	}

	@XmlTransient
	public Set<InstitutionFile> getFiles() {
		return files;
	}

	public void setFiles(Set<InstitutionFile> files) {
		this.files = files;
	}

	public void addFile(InstitutionFile institutionFile) {
		institutionFile.setInstitution(this);
		this.files.add(institutionFile);
	}
}
