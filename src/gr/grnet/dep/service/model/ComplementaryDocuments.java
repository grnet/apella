package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ComplementaryDocumentsFile;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class ComplementaryDocuments {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "complementaryDocuments", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@OneToMany(mappedBy = "complementaryDocuments", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ComplementaryDocumentsFile> files = new HashSet<ComplementaryDocumentsFile>();

	@Temporal(TemporalType.DATE)
	private Date createdAt;

	@Temporal(TemporalType.DATE)
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlTransient
	public Set<ComplementaryDocumentsFile> getFiles() {
		return files;
	}

	public void setFiles(Set<ComplementaryDocumentsFile> files) {
		this.files = files;
	}

	public void addFile(ComplementaryDocumentsFile file) {
		file.setComplementaryDocuments(this);
		this.files.add(file);
	}

	public void initializeCollections() {
		this.files.size();
	}
}
