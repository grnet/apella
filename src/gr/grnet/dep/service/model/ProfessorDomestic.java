package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.ProfessorFile;
import gr.grnet.dep.service.util.CompareUtil;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

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
	public Role copyFrom(Role otherRole) {
		super.copyFrom(otherRole);
		ProfessorDomestic pd = (ProfessorDomestic) otherRole;
		setInstitution(pd.getInstitution());
		setDepartment(pd.getDepartment());
		setRank(pd.getRank());
		setFek(pd.getFek());
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
		if (!CompareUtil.equalsIgnoreNull(this.institution.getId(), other.getInstitution().getId())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.department.getId(), other.getDepartment().getId())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.fek, other.getFek())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.fekSubject, other.getFekSubject())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.subject, other.getSubject())) {
			return false;
		}
		return true;
	}

	@Override
	@XmlTransient
	@JsonIgnore
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
		if (this.rank == null) {
			return true;
		}
		if (this.fekSubject == null && this.subject == null) {
			return true;
		}
		boolean hasFEKFile = false;
		for (ProfessorFile file : this.getFiles()) {
			switch (file.getType()) {
				case FEK:
					hasFEKFile = true;
					break;
				default:
			}
		}
		if (!hasFEKFile) {
			return true;
		}
		return false;
	}

}
