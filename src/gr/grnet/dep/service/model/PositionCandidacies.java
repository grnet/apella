package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
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

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;
import org.hibernate.annotations.Filter;

@Entity
public class PositionCandidacies {

	public static interface PositionCandidaciesView {
	};

	public static interface DetailedPositionCandidaciesView extends PositionCandidaciesView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "candidacies", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidacies")
	@Filter(name = "permanent", condition = ":permanent = true")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

	@OneToMany(mappedBy = "candidacies", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionCandidaciesFile> files = new HashSet<PositionCandidaciesFile>();

	@Temporal(TemporalType.DATE)
	private Date openingDate; // Έναρξη υποβολών

	@Temporal(TemporalType.DATE)
	private Date closingDate; // Λήξη υποβολών

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

	@JsonView({DetailedPositionCandidaciesView.class, CandidacyView.class})
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

	@JsonView({DetailedPositionCandidaciesView.class})
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCreatedAt() {
		return createdAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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

	public Set<PositionCandidaciesFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionCandidaciesFile> files) {
		this.files = files;
	}

	//////////////////////////////////////////////

	public void addFile(PositionCandidaciesFile file) {
		file.setCandidacies(this);
		this.files.add(file);
	}

	public void copyFrom(PositionCandidacies other) {
		this.setClosingDate(other.getClosingDate());
		this.setOpeningDate(other.getOpeningDate());
		this.setUpdatedAt(new Date());
	}

	public void initializeCollections() {
		this.candidacies.size();
	}

	public boolean containsCandidate(User user) {
		for (Candidacy candidacy : this.candidacies) {
			if (candidacy.getCandidate().getUser().getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}
}
