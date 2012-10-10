package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.PositionFile;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Position {

	public static interface PublicPositionView {
	};

	public static interface DetailedPositionView extends PublicPositionView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@NotEmpty
	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = " character varying(4000)")
	private String description;

	@ManyToOne(optional = false)
	private Department department;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject subject;

	private String fek;

	@Temporal(TemporalType.DATE)
	private Date fekSentDate;

	@Temporal(TemporalType.DATE)
	private Date openingDate; // Έναρξη υποβολών

	@Temporal(TemporalType.DATE)
	private Date closingDate; // Λήξη υποβολών

	@Temporal(TemporalType.DATE)
	private Date committeeMeetingDate; // Ημερομηνία Συνεδρίασης επιτροπής

	@Temporal(TemporalType.DATE)
	private Date nominationCommitteeConvergenceDate; // Ημερομηνία σύγκλισης επιτροπής για επιλογή

	@Temporal(TemporalType.DATE)
	private Date nominationToETDate; // Ημερομηνία αποστολής διορισμού στο Εθνικό Τυπογραφείο

	private String nominationFEK; //ΦΕΚ Διορισμού

	@OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionFile> files = new HashSet<PositionFile>();

	@OneToMany(mappedBy = "position")
	private List<PositionCommitteeMember> commitee;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "position")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getFek() {
		return fek;
	}

	public void setFek(String fek) {
		this.fek = fek;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getFekSentDate() {
		return fekSentDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setFekSentDate(Date fekSentDate) {
		this.fekSentDate = fekSentDate;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getOpeningDate() {
		return openingDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setOpeningDate(Date openingDate) {
		this.openingDate = openingDate;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getClosingDate() {
		return closingDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	@JsonView({DetailedPositionView.class})
	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCommitteeMeetingDate() {
		return committeeMeetingDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCommitteeMeetingDate(Date committeeMeetingDate) {
		this.committeeMeetingDate = committeeMeetingDate;
	}

	@JsonView({DetailedPositionView.class})
	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getNominationCommitteeConvergenceDate() {
		return nominationCommitteeConvergenceDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setNominationCommitteeConvergenceDate(Date nominationCommitteeConvergenceDate) {
		this.nominationCommitteeConvergenceDate = nominationCommitteeConvergenceDate;
	}

	@JsonView({DetailedPositionView.class})
	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getNominationToETDate() {
		return nominationToETDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setNominationToETDate(Date nominationToETDate) {
		this.nominationToETDate = nominationToETDate;
	}

	@JsonView({DetailedPositionView.class})
	public String getNominationFEK() {
		return nominationFEK;
	}

	public void setNominationFEK(String nominationFEK) {
		this.nominationFEK = nominationFEK;
	}

	@XmlTransient
	public Set<PositionFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionFile> files) {
		this.files = files;
	}

	@XmlTransient
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	@XmlTransient
	public List<PositionCommitteeMember> getCommitee() {
		return commitee;
	}

	public void setCommitee(List<PositionCommitteeMember> commitee) {
		this.commitee = commitee;
	}

	public void addFile(PositionFile file) {
		this.files.add(file);
		file.setPosition(this);
	}

	public PositionStatus getStatus() {
		if (openingDate == null || closingDate == null) {
			return PositionStatus.ENTAGMENI;
		}
		Date now = new Date();
		if (now.compareTo(openingDate) < 0) {
			return PositionStatus.ENTAGMENI;
		} else if (now.compareTo(closingDate) < 0) {
			return PositionStatus.ANOIXTI;
		} else if (isCompletedWithNomination()) {
			return PositionStatus.STELEXOMENI;
		} else {
			return PositionStatus.KLEISTI;
		}
	}

	private boolean isCompletedWithNomination() {
		// TODO: Also check PRAKSI_DIORISMOU
		return nominationFEK != null;
	}

	public void copyFrom(Position position) {
		this.name = position.getName();
		this.description = position.getDescription();
		this.subject.setName(position.getSubject().getName());
		this.fek = position.getFek();
		this.fekSentDate = position.getFekSentDate();
		this.openingDate = position.getOpeningDate();
		this.closingDate = position.getClosingDate();
		this.committeeMeetingDate = position.getCommitteeMeetingDate();
		this.nominationCommitteeConvergenceDate = position.getNominationCommitteeConvergenceDate();
		this.nominationToETDate = position.getNominationToETDate();
		this.nominationFEK = position.getNominationFEK();
	}
}
