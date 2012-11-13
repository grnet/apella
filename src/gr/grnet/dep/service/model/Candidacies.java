package gr.grnet.dep.service.model;

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

@Entity
public class Candidacies {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "candidacies", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@Temporal(TemporalType.DATE)
	private Date openingDate; // Έναρξη υποβολών

	@Temporal(TemporalType.DATE)
	private Date closingDate; // Λήξη υποβολών

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidacies")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

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

	@XmlTransient
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

	//////////////////////////////////////////////

	public void copyFrom(Candidacies other) {
		this.setClosingDate(other.getOpeningDate());
		this.setOpeningDate(other.getOpeningDate());
		this.setUpdatedAt(new Date());
	}

	public void initializeCollections() {
		this.candidacies.size();
	}
}
