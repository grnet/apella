package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@DiscriminatorValue("INSTITUTION_ASSISTANT")
public class InstitutionAssistant extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne(optional = false)
	private Institution institution;

	@ManyToOne
	private InstitutionManager manager;

	private String phone;

	public InstitutionAssistant() {
		super();
		setDiscriminator(RoleDiscriminator.INSTITUTION_ASSISTANT);
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	@XmlTransient
	public InstitutionManager getManager() {
		return manager;
	}

	public void setManager(InstitutionManager manager) {
		this.manager = manager;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public void initializeCollections() {
		manager.initializeCollections();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		InstitutionAssistant ia = (InstitutionAssistant) otherRole;
		this.setInstitution(ia.getInstitution());
		this.setPhone(ia.getPhone());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof InstitutionAssistant)) {
			return false;
		}
		InstitutionAssistant other = (InstitutionAssistant) role;
		if (!compare(this.institution.getId(), other.getInstitution().getId())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.institution == null) {
			return true;
		}
		if (this.manager == null) {
			return true;
		}
		if (this.phone == null) {
			return true;
		}
		return false;
	}
}
