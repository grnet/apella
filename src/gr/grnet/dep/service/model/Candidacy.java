package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.User.DetailedUserView;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class Candidacy {
	
	// define 2 json views
	public static interface SimpleCandidacyView {
	}; // shows a summary view of a Candidacy

	public static interface DetailedCandidacyView extends SimpleCandidacyView {
	};
		

	@Id
	@GeneratedValue
	private Long id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	@ManyToOne(optional=false)
	private Candidate candidate;

	@ManyToOne(optional=false)
	private Position position;

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

	@XmlTransient
	public Candidate getCandidate() {
		return candidate;
	}
	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}

	@JsonView({DetailedUserView.class})
	public Set<FileBody> getFiles() {
		return files;
	}
	public void setFiles(Set<FileBody> files) {
		this.files = files;
	}

}