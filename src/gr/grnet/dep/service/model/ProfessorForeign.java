package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@DiscriminatorValue("PROFESSOR_FOREIGN")
public class ProfessorForeign extends Professor {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	private String institution;

	@ManyToOne
	private Country country;

	@ManyToOne
	private Rank rank;

	@ManyToOne
	private Subject subject;

	private Boolean speakingGreek;

	public ProfessorForeign() {
		super();
		setDiscriminator(RoleDiscriminator.PROFESSOR_FOREIGN);
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Boolean getSpeakingGreek() {
		return speakingGreek;
	}

	public void setSpeakingGreek(Boolean speakingGreek) {
		this.speakingGreek = speakingGreek;
	}

	@Override
	public Role copyFrom(Role otherRole) {
		super.copyFrom(otherRole);
		ProfessorForeign pf = (ProfessorForeign) otherRole;
		setCountry(pf.getCountry());
		setInstitution(pf.getInstitution());
		setRank(pf.getRank());
		setSpeakingGreek(pf.getSpeakingGreek());
		return this;
	}

	@Override
	@XmlTransient
	@JsonIgnore
	public boolean isMissingRequiredFields() {
		if (super.isMissingRequiredFields()) {
			return true;
		}
		if (this.getProfileURL() == null) {
			return true;
		}
		if (this.country == null) {
			return true;
		}
		if (this.institution == null) {
			return true;
		}
		if (this.rank == null) {
			return true;
		}
		if (this.subject == null) {
			return true;
		}
		if (this.speakingGreek == null) {
			return true;
		}
		return false;
	}

}
