package gr.grnet.dep.service.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class Candidacy {

	public static interface DetailedCandidacyView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	@JsonView({DetailedCandidacyView.class})
	private Candidate candidate;

	// Inverse to Position
	@Basic(optional = false)
	@Column(name = "position_id")
	private Long position;

	@ManyToMany
	private Set<FileBody> files = new HashSet<FileBody>();

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	@JsonView(DetailedCandidacyView.class)
	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	@JsonView(DetailedCandidacyView.class)
	public Set<FileBody> getFiles() {
		return files;
	}

	public void setFiles(Set<FileBody> files) {
		this.files = files;
	}

}
