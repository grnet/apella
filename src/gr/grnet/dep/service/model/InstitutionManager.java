package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("INSTITUTION_MANAGER")
public class InstitutionManager extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public enum VerificationAuthority {
		DEAN, PRESIDENT
	}

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne
	private Institution institution;

	private VerificationAuthority verificationAuthority;

	private String verificationAuthorityName;

	private String phone;

	public InstitutionManager() {
		super();
		setDiscriminator(RoleDiscriminator.INSTITUTION_MANAGER);
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public VerificationAuthority getVerificationAuthority() {
		return verificationAuthority;
	}

	public void setVerificationAuthority(VerificationAuthority verificationAuthority) {
		this.verificationAuthority = verificationAuthority;
	}

	public String getVerificationAuthorityName() {
		return verificationAuthorityName;
	}

	public void setVerificationAuthorityName(String verificationAuthorityName) {
		this.verificationAuthorityName = verificationAuthorityName;
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
	}

	@Override
	public Role copyFrom(Role otherRole) {
		InstitutionManager im = (InstitutionManager) otherRole;
		this.setInstitution(im.getInstitution());
		this.setVerificationAuthority(im.getVerificationAuthority());
		this.setVerificationAuthorityName(im.getVerificationAuthorityName());
		this.setPhone(im.getPhone());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof InstitutionManager)) {
			return false;
		}
		InstitutionManager other = (InstitutionManager) role;
		if (!compare(this.institution.getId(), other.getInstitution().getId())) {
			return false;
		}
		if (!compare(this.verificationAuthority, other.getVerificationAuthority())) {
			return false;
		}
		if (!compare(this.verificationAuthorityName, other.getVerificationAuthorityName())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.institution == null) {
			return true;
		}
		if (this.verificationAuthority == null) {
			return true;
		}
		if (this.verificationAuthorityName == null) {
			return true;
		}
		return false;
	}

}
