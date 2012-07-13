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

	// define 3 json views
	public static interface IdCandidacyView {
	}; // shows only id view of a Candidacy

	public static interface SimpleCandidacyView extends IdCandidacyView {
	}; // shows a summary view of a Candidacy

	public static interface DetailedCandidacyView extends SimpleCandidacyView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	// Inverse to User
	@Basic(optional = false)
	@Column(name = "candidate_id")
	private Long candidate;

	// Inverse to Position
	@Basic(optional = false)
	@Column(name = "position_id")
	private Long position;

	@ManyToMany
	private Set<FileBody> files = new HashSet<FileBody>();

	public Long getId() {
		return id;
	}

	@JsonView(SimpleCandidacyView.class)
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonView(SimpleCandidacyView.class)
	public Long getCandidate() {
		return candidate;
	}

	public void setCandidate(Long candidate) {
		this.candidate = candidate;
	}

	@JsonView(SimpleCandidacyView.class)
	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	@JsonView({DetailedCandidacyView.class})
	public Set<FileBody> getFiles() {
		return files;
	}

	public void setFiles(Set<FileBody> files) {
		this.files = files;
	}

}
