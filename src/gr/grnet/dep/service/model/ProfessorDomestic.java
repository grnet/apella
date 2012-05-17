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

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private Institution institution;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private Rank rank;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private Subject subject;

	private String fek;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private Subject fekSubject;

	@ManyToOne
	private FileHeader fekFile;

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

	public FileHeader getFekFile() {
		return fekFile;
	}

	public void setFekFile(FileHeader fekFile) {
		this.fekFile = fekFile;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
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
	}

	@Override
	public Role copyFrom(Role otherRole) {
		super.copyFrom(otherRole);
		ProfessorDomestic pd = (ProfessorDomestic) otherRole;
		setInstitution(pd.getInstitution());
		setPosition(pd.getPosition());
		getRank().setName(pd.getRank().getName());
		getSubject().setName(pd.getSubject().getName());
		setFek(pd.getFek());
		getFekSubject().setName(pd.getFekSubject().getName());
		return this;
	}

}
