package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.PositionNominationFile;
import gr.grnet.dep.service.util.SimpleDateSerializer;

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

import org.codehaus.jackson.map.annotate.JsonSerialize;

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

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getNominationCommitteeConvergenceDate() {
		return nominationCommitteeConvergenceDate;
	}

	public void setNominationCommitteeConvergenceDate(Date nominationCommitteeConvergenceDate) {
		this.nominationCommitteeConvergenceDate = nominationCommitteeConvergenceDate;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
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

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@XmlTransient
	public Set<PositionPhase> getPhases() {
		return phases;
	}

	public void setPhases(Set<PositionPhase> phases) {
		this.phases = phases;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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
