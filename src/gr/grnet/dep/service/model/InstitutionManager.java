package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.CompareUtil;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

	@Enumerated(EnumType.STRING)
	private VerificationAuthority verificationAuthority;

	private String verificationAuthorityName;

	private String phone;

	@Valid
	@Embedded
	@NotNull
	private BasicInformation alternateBasicInfo = new BasicInformation();

	@Valid
	@Embedded
	@NotNull
	private BasicInformation alternateBasicInfoLatin = new BasicInformation();

	@Valid
	@Embedded
	@NotNull
	private ContactInformation alternateContactInfo = new ContactInformation();

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

	public BasicInformation getAlternateBasicInfo() {
		return alternateBasicInfo;
	}

	public void setAlternateBasicInfo(BasicInformation alternateBasicInfo) {
		this.alternateBasicInfo = alternateBasicInfo;
	}

	public BasicInformation getAlternateBasicInfoLatin() {
		return alternateBasicInfoLatin;
	}

	public void setAlternateBasicInfoLatin(BasicInformation alternateBasicInfoLatin) {
		this.alternateBasicInfoLatin = alternateBasicInfoLatin;
	}

	public ContactInformation getAlternateContactInfo() {
		return alternateContactInfo;
	}

	public void setAlternateContactInfo(ContactInformation alternateContactInfo) {
		this.alternateContactInfo = alternateContactInfo;
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
		this.setAlternateBasicInfo(im.getAlternateBasicInfo());
		this.setAlternateBasicInfoLatin(im.getAlternateBasicInfoLatin());
		this.setAlternateContactInfo(im.getAlternateContactInfo());
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof InstitutionManager)) {
			return false;
		}
		InstitutionManager other = (InstitutionManager) role;
		if (!CompareUtil.equalsIgnoreNull(this.institution.getId(), other.getInstitution().getId())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.verificationAuthority, other.getVerificationAuthority())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.verificationAuthorityName, other.getVerificationAuthorityName())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.getAlternateBasicInfo(), other.getAlternateBasicInfo())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.getAlternateBasicInfoLatin(), other.getAlternateBasicInfoLatin())) {
			return false;
		}
		if (!CompareUtil.equalsIgnoreNull(this.getAlternateContactInfo(), other.getAlternateContactInfo())) {
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
		if (this.phone == null) {
			return true;
		}
		if (this.alternateBasicInfo.isMissingRequiredFields()) {
			return true;
		}
		if (this.alternateBasicInfoLatin.isMissingRequiredFields()) {
			return true;
		}
		if (this.alternateContactInfo.isMissingRequiredFields()) {
			return true;
		}
		return false;
	}

}
