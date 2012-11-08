package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.PositionNominationFile;

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
public class Nomination {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@Temporal(TemporalType.DATE)
	private Date nominationCommitteeConvergenceDate; // Ημερομηνία σύγκλισης επιτροπής για επιλογή

	@Temporal(TemporalType.DATE)
	private Date nominationToETDate; // Ημερομηνία αποστολής διορισμού στο Εθνικό Τυπογραφείο

	private String nominationFEK; //ΦΕΚ Διορισμού

	@OneToMany(mappedBy = "nomination", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@OneToMany(mappedBy = "nomination", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionNominationFile> files = new HashSet<PositionNominationFile>();

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

	public Date getNominationCommitteeConvergenceDate() {
		return nominationCommitteeConvergenceDate;
	}

	public void setNominationCommitteeConvergenceDate(Date nominationCommitteeConvergenceDate) {
		this.nominationCommitteeConvergenceDate = nominationCommitteeConvergenceDate;
	}

	public Date getNominationToETDate() {
		return nominationToETDate;
	}

	public void setNominationToETDate(Date nominationToETDate) {
		this.nominationToETDate = nominationToETDate;
	}

	public String getNominationFEK() {
		return nominationFEK;
	}

	public void setNominationFEK(String nominationFEK) {
		this.nominationFEK = nominationFEK;
	}

	@XmlTransient
	public Set<PositionNominationFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionNominationFile> files) {
		this.files = files;
	}

	public void addFile(PositionNominationFile file) {
		file.setNomination(this);
		this.files.add(file);
	}

	public void initializeCollections() {
		this.files.size();
	}
}
