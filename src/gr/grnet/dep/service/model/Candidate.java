package gr.grnet.dep.service.model;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@DiscriminatorValue("CANDIDATE")
public class Candidate extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne
	private FileHeader cv;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name = "Candidate_Degrees",
		uniqueConstraints = {@UniqueConstraint(columnNames = "degrees_id")})
	private Set<FileHeader> degrees = new HashSet<FileHeader>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name = "Candidate_Publications",
		uniqueConstraints = {@UniqueConstraint(columnNames = "publications_id")})
	private Set<FileHeader> publications = new HashSet<FileHeader>();

	@ManyToOne
	private FileHeader identity;

	@ManyToOne
	private FileHeader military1599;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "candidate")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

	public Candidate() {
		super();
		setDiscriminator(RoleDiscriminator.CANDIDATE);
	}

	public FileHeader getCv() {
		return cv;
	}

	public void setCv(FileHeader cv) {
		this.cv = cv;
	}

	@XmlTransient
	public Set<FileHeader> getDegrees() {
		return degrees;
	}

	public void setDegrees(Set<FileHeader> degrees) {
		this.degrees = degrees;
	}

	@XmlTransient
	public Set<FileHeader> getPublications() {
		return publications;
	}

	public void setPublications(Set<FileHeader> publications) {
		this.publications = publications;
	}

	public FileHeader getMilitary1599() {
		return military1599;
	}

	public void setMilitary1599(FileHeader military1599) {
		this.military1599 = military1599;
	}

	public FileHeader getIdentity() {
		return identity;
	}

	public void setIdentity(FileHeader identity) {
		this.identity = identity;
	}

	@XmlTransient
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	///////////////////////////////////////////////////////////////////////////////

	public void addCandidacy(Candidacy candidacy) {
		this.candidacies.add(candidacy);
		candidacy.setCandidate(this);
	}

	@Override
	public void initializeCollections() {
		getCandidacies().size();
		getDegrees().size();
		getPublications().size();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		Candidate c = (Candidate) otherRole;
		setCv(c.getCv());
		setIdentity(c.getIdentity());
		setMilitary1599(c.getMilitary1599());
		return this;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.cv == null) {
			return true;
		}
		if (this.degrees == null) {
			return true;
		}
		if (this.identity == null) {
			return true;
		}
		if (this.military1599 == null) {
			return true;
		}
		if (this.publications == null) {
			return true;
		}
		return false;
	}
}
