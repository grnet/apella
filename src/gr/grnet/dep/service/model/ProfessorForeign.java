package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ProfessorFile;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

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
	private Rank rank;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject subject;

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

	@Override
	public void initializeCollections() {
		super.initializeCollections();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		super.copyFrom(otherRole);
		ProfessorForeign pf = (ProfessorForeign) otherRole;
		setInstitution(pf.getInstitution());
		setRank(pf.getRank());
		if (getSubject() == null) {
			setSubject(new Subject());
		}
		getSubject().setName(pf.getSubject().getName());
		return this;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (super.isMissingRequiredFields()) {
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
		boolean hasPROFILEFile = false;
		for (ProfessorFile file : this.getFiles()) {
			switch (file.getType()) {
				case PROFILE:
					hasPROFILEFile = true;
					break;
				default:
			}
		}
		if (!hasPROFILEFile) {
			return true;
		}
		return false;
	}

}
