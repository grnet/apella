package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.CompareUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

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

	@Transient
	private Map<String, String> alternateFirstname = new HashMap<String, String>();

	@Transient
	private Map<String, String> alternateLastname = new HashMap<String, String>();

	@Transient
	private Map<String, String> alternateFathername = new HashMap<String, String>();

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

	public Map<String, String> getAlternateFirstname() {
		return alternateFirstname;
	}

	public Map<String, String> getAlternateLastname() {
		return alternateLastname;
	}

	public Map<String, String> getAlternateFathername() {
		return alternateFathername;
	}

	@XmlTransient
	@JsonIgnore
	public String getAlternateFirstname(String locale) {
		return alternateFirstname.get(locale);
	}

	@XmlTransient
	@JsonIgnore
	public String getAlternateLastname(String locale) {
		return alternateLastname.get(locale);

	}

	@XmlTransient
	@JsonIgnore
	public String getAlternateFathername(String locale) {
		return alternateFathername.get(locale);
	}

	@PostPersist
	@PostUpdate
	@PostLoad
	private void refreshLocalizedNames() {
		alternateFirstname.put("el", this.alternateBasicInfo != null ? this.alternateBasicInfo.getFirstname() : null);
		if (this.alternateBasicInfoLatin != null &&
			this.alternateBasicInfoLatin.getFirstname() != null &&
			!this.alternateBasicInfoLatin.getFirstname().isEmpty()) {
			alternateFirstname.put("en", this.alternateBasicInfoLatin != null ? this.alternateBasicInfoLatin.getFirstname() : null);
		} else {
			alternateFirstname.put("en", this.alternateBasicInfo != null ? this.alternateBasicInfo.getFirstname() : null);
		}
		alternateLastname.put("el", this.alternateBasicInfo != null ? this.alternateBasicInfo.getLastname() : null);
		if (this.alternateBasicInfoLatin != null &&
			this.alternateBasicInfoLatin.getLastname() != null &&
			!this.alternateBasicInfoLatin.getLastname().isEmpty()) {
			alternateLastname.put("en", this.alternateBasicInfoLatin != null ? this.alternateBasicInfoLatin.getLastname() : null);
		} else {
			alternateLastname.put("en", this.alternateBasicInfo != null ? this.alternateBasicInfo.getLastname() : null);
		}
		alternateFathername.put("el", this.alternateBasicInfo != null ? this.alternateBasicInfo.getFathername() : null);
		if (this.alternateBasicInfoLatin != null &&
			this.alternateBasicInfoLatin.getFathername() != null &&
			!this.alternateBasicInfoLatin.getFathername().isEmpty()) {
			alternateFathername.put("en", this.alternateBasicInfoLatin != null ? this.alternateBasicInfoLatin.getFathername() : null);
		} else {
			alternateFathername.put("en", this.alternateBasicInfo != null ? this.alternateBasicInfo.getFathername() : null);
		}
	}

	@Override
	public Role copyFrom(Role otherRole) {
		InstitutionManager im = (InstitutionManager) otherRole;
		this.setInstitution(im.getInstitution());
		this.setVerificationAuthority(im.getVerificationAuthority());
		this.setVerificationAuthorityName(im.getVerificationAuthorityName());
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
