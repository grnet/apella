package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("PROFESSOR_DOMESTIC")
public class ProfessorDomestic extends Professor {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne
	private Institution institution;

	@ManyToOne
	private Department department;

	@ManyToOne
	private Rank rank;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject subject;

	private String fek;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject fekSubject;

	public ProfessorDomestic() {
		super();
		setDiscriminator(RoleDiscriminator.PROFESSOR_DOMESTIC);
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

	public String getFek() {
		return fek;
	}

	public void setFek(String fek) {
		this.fek = fek;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Subject getFekSubject() {
		return fekSubject;
	}

	public void setFekSubject(Subject fekSubject) {
		this.fekSubject = fekSubject;
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public void initializeCollections() {
		super.initializeCollections();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		super.copyFrom(otherRole);
		ProfessorDomestic pd = (ProfessorDomestic) otherRole;
		setInstitution(pd.getInstitution());
		setDepartment(pd.getDepartment());
		setRank(pd.getRank());
		if (getSubject() == null) {
			setSubject(new Subject());
		}
		getSubject().setName(pd.getSubject().getName());
		setFek(pd.getFek());
		if (getFekSubject() == null) {
			setFekSubject(new Subject());
		}
		getFekSubject().setName(pd.getFekSubject().getName());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!super.compareCriticalFields(role)) {
			return false;
		}
		if (!(role instanceof ProfessorDomestic)) {
			return false;
		}
		ProfessorDomestic other = (ProfessorDomestic) role;
		if (!compare(this.institution.getId(), other.getInstitution().getId())) {
			return false;
		}
		if (!compare(this.department.getId(), other.getDepartment().getId())) {
			return false;
		}
		if (!compare(this.fek, other.getFek())) {
			return false;
		}
		if (!compare(this.fekSubject, other.getFekSubject())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (super.isMissingRequiredFields()) {
			return true;
		}
		if (this.institution == null) {
			return true;
		}
		if (this.department == null) {
			return true;
		}
		if (this.fek == null) {
			return true;
		}
		if (this.fekSubject == null) {
			return true;
		}
		if (this.rank == null) {
			return true;
		}
		if (this.subject == null) {
			return true;
		}
		return false;
	}

}
