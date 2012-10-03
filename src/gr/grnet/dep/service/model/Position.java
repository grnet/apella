package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.PositionFile;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.map.annotate.JsonView;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Position {

	public static interface DetailedPositionView {
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
	private Institution institution;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject subject;

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private PositionStatus status = PositionStatus.ENTAGMENI;

	private String fek;

	@Temporal(TemporalType.DATE)
	private Date fekSentDate;

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

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public PositionStatus getStatus() {
		return status;
	}

	public void setStatus(PositionStatus status) {
		this.status = status;
	}

	@JsonView({DetailedPositionView.class})
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	public Date getFekSentDate() {
		return fekSentDate;
	}

	public void setFekSentDate(Date fekSentDate) {
		this.fekSentDate = fekSentDate;
	}

	public String getFek() {
		return fek;
	}

	public void setFek(String fek) {
		this.fek = fek;
	}

	public Set<PositionFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionFile> files) {
		this.files = files;
	}

	@JsonView({DetailedPositionView.class})
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

}
