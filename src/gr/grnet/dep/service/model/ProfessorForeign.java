package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
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

	@ManyToOne(optional = false)
	private Rank rank;

	@ManyToOne(optional = false)
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
	}

	@Override
	public Role copyFrom(Role otherRole) {
		ProfessorForeign pf = (ProfessorForeign) otherRole;
		setInstitution(pf.getInstitution());
		setRank(pf.getRank());
		setSubject(pf.getSubject());
		return this;
	}

}
