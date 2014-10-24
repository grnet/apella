package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.util.IdentificationDeserializer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Entity
@XmlRootElement
@Table(name = "Users")
public class User implements Serializable {

	/**
	 * Default value included to remove warning. Remove or modify at will. *
	 */
	private static final long serialVersionUID = 1L;

	public enum UserStatus {
		UNVERIFIED, ACTIVE, BLOCKED
	}

	public static interface UserView {
	}

	public static interface UserWithLoginDataView extends UserView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Valid
	@Embedded
	private BasicInformation basicInfo = new BasicInformation();

	@Valid
	@Embedded
	private BasicInformation basicInfoLatin = new BasicInformation();

	@Valid
	@Embedded
	private ContactInformation contactInfo = new ContactInformation();

	private String identification;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
	private Set<Role> roles = new HashSet<Role>();

	// Registration Fields

	@Enumerated(EnumType.STRING)
	private AuthenticationType authenticationType;

	private Date creationDate;

	// Authentication Fields

	// A. USERNAME
	@Column(unique = true)
	private String username;

	private String password;

	private String passwordSalt;

	private Long verificationNumber;

	// B. SHIBBOLETH
	@Valid
	@Embedded
	private ShibbolethInformation shibbolethInfo = new ShibbolethInformation();

	// C. EMAIL
	@Column(unique = true)
	private String permanentAuthToken;

	private Boolean loginEmailSent = Boolean.FALSE;

	@Column(unique = true)
	private String authToken;

	// Status Fields

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private Date statusDate;

	@Transient
	private Map<String, String> firstname = new HashMap<String, String>();

	@Transient
	private Map<String, String> lastname = new HashMap<String, String>();

	@Transient
	private Map<String, String> fathername = new HashMap<String, String>();

	///////////////////////////////////////////////////////////////////////////////////////

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIdentification() {
		return identification;
	}

	@JsonDeserialize(using = IdentificationDeserializer.class)
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public ShibbolethInformation getShibbolethInfo() {
		return shibbolethInfo;
	}

	public void setShibbolethInfo(ShibbolethInformation shibbolethInfo) {
		// Hibernate hack for null fields in embeddable objects
		if (shibbolethInfo == null) {
			this.shibbolethInfo = new ShibbolethInformation();
		} else {
			this.shibbolethInfo = shibbolethInfo;
		}
	}

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

	public BasicInformation getBasicInfo() {
		return basicInfo == null ? new BasicInformation() : basicInfo;
	}

	public void setBasicInfo(BasicInformation basicInfo) {
		this.basicInfo = basicInfo;
	}

	public BasicInformation getBasicInfoLatin() {
		return basicInfoLatin == null ? new BasicInformation() : basicInfoLatin;
	}

	public void setBasicInfoLatin(BasicInformation basicInfoLatin) {
		this.basicInfoLatin = basicInfoLatin;
	}

	public ContactInformation getContactInfo() {
		return contactInfo == null ? new ContactInformation() : contactInfo;
	}

	public void setContactInfo(ContactInformation contactInfo) {
		this.contactInfo = contactInfo;
	}

	// This is for circular references when loading Roles
	@JsonView({UserView.class})
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@JsonView({UserWithLoginDataView.class})
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlTransient
	@JsonIgnore
	public String getPasswordSalt() {
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	@XmlTransient
	@JsonIgnore
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@JsonView({UserWithLoginDataView.class})
	public Long getVerificationNumber() {
		return verificationNumber;
	}

	public void setVerificationNumber(Long verificationNumber) {
		this.verificationNumber = verificationNumber;
	}

	@JsonView({UserWithLoginDataView.class})
	public String getPermanentAuthToken() {
		return permanentAuthToken;
	}

	public void setPermanentAuthToken(String permanentAuthToken) {
		this.permanentAuthToken = permanentAuthToken;
	}

	@XmlTransient
	@JsonIgnore
	public Boolean getLoginEmailSent() {
		return loginEmailSent;
	}

	public void setLoginEmailSent(Boolean loginEmailSent) {
		this.loginEmailSent = loginEmailSent;
	}

	@XmlTransient
	@JsonIgnore
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	///////////////////////////////////////////////////////////////////////////////////////

	public Map<String, String> getFirstname() {
		return firstname;
	}

	public Map<String, String> getLastname() {
		return lastname;
	}

	public Map<String, String> getFathername() {
		return fathername;
	}

	@PostPersist
	@PostUpdate
	@PostLoad
	private void refreshLocalizedNames() {
		firstname.put("el", this.basicInfo != null ? this.basicInfo.getFirstname() : null);
		if (this.basicInfoLatin != null &&
				this.basicInfoLatin.getFirstname() != null &&
				!this.basicInfoLatin.getFirstname().isEmpty()) {
			firstname.put("en", this.basicInfoLatin != null ? this.basicInfoLatin.getFirstname() : null);
		} else {
			firstname.put("en", this.basicInfo != null ? this.basicInfo.getFirstname() : null);
		}
		lastname.put("el", this.basicInfo != null ? this.basicInfo.getLastname() : null);
		if (this.basicInfoLatin != null &&
				this.basicInfoLatin.getLastname() != null &&
				!this.basicInfoLatin.getLastname().isEmpty()) {
			lastname.put("en", this.basicInfoLatin != null ? this.basicInfoLatin.getLastname() : null);
		} else {
			lastname.put("en", this.basicInfo != null ? this.basicInfo.getLastname() : null);
		}
		fathername.put("el", this.basicInfo != null ? this.basicInfo.getFathername() : null);
		if (this.basicInfoLatin != null &&
				this.basicInfoLatin.getFathername() != null &&
				!this.basicInfoLatin.getFathername().isEmpty()) {
			fathername.put("en", this.basicInfoLatin != null ? this.basicInfoLatin.getFathername() : null);
		} else {
			fathername.put("en", this.basicInfo != null ? this.basicInfo.getFathername() : null);
		}
	}

	@XmlTransient
	@JsonIgnore
	public String getFirstname(String locale) {
		return firstname.get(locale);
	}

	@XmlTransient
	@JsonIgnore
	public String getLastname(String locale) {
		return lastname.get(locale);
	}

	@XmlTransient
	@JsonIgnore
	public String getFathername(String locale) {
		return fathername.get(locale);
	}

	@XmlTransient
	@JsonIgnore
	public String getFullName(String locale) {
		return this.getFirstname(locale) + " " + this.getLastname(locale);
	}

	@JsonView({UserView.class})
	public boolean isMissingRequiredFields() {
		return (this.basicInfo == null || this.basicInfo.isMissingRequiredFields()) ||
				(this.contactInfo == null || this.contactInfo.isMissingRequiredFields()) ||
				(this.identification == null && !(this.getPrimaryRole().equals(RoleDiscriminator.PROFESSOR_DOMESTIC) || this.getPrimaryRole().equals(RoleDiscriminator.PROFESSOR_FOREIGN)
				));

	}

	public void addRole(Role role) {
		roles.add(role);
		role.setUser(this);
	}

	public void removeRole(Role role) {
		roles.remove(role);
		role.setUser(null);
	}

	private static final List<RoleDiscriminator> roleOrder = Arrays.asList(new RoleDiscriminator[]{
			RoleDiscriminator.PROFESSOR_DOMESTIC,
			RoleDiscriminator.PROFESSOR_FOREIGN,
			RoleDiscriminator.CANDIDATE,
			RoleDiscriminator.INSTITUTION_MANAGER,
			RoleDiscriminator.INSTITUTION_ASSISTANT,
			RoleDiscriminator.MINISTRY_MANAGER,
			RoleDiscriminator.MINISTRY_ASSISTANT,
			RoleDiscriminator.ADMINISTRATOR
	});

	@JsonView({UserView.class, DetailedRoleView.class, DetailedPositionView.class})
	public RoleDiscriminator getPrimaryRole() {
		TreeSet<Role> sortedRoles = new TreeSet<Role>(new Comparator<Role>() {

			@Override
			public int compare(Role o1, Role o2) {
				return roleOrder.indexOf(o1.getDiscriminator()) - roleOrder.indexOf(o2.getDiscriminator());
			}

		});
		sortedRoles.addAll(roles);
		return sortedRoles.iterator().next().getDiscriminator();
	}

	@XmlTransient
	@JsonIgnore
	public Role getRole(RoleDiscriminator discriminator) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator().equals(discriminator)) {
				return r;
			}
		}
		return null;
	}

	@XmlTransient
	@JsonIgnore
	public Role getActiveRole(RoleDiscriminator discriminator) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator().equals(discriminator) && r.getStatus().equals(RoleStatus.ACTIVE)) {
				return r;
			}
		}
		return null;
	}

	@XmlTransient
	@JsonIgnore
	public boolean hasActiveRole(RoleDiscriminator discriminator) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == discriminator && r.getStatus().equals(RoleStatus.ACTIVE)) {
				return true;
			}
		}
		return false;
	}

	@XmlTransient
	@JsonIgnore
	public List<Institution> getAssociatedInstitutions() {
		List<Institution> institutions = new ArrayList<Institution>();
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_MANAGER) {
				InstitutionManager im = (InstitutionManager) r;
				institutions.add(im.getInstitution());
			}
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_ASSISTANT) {
				InstitutionAssistant ia = (InstitutionAssistant) r;
				institutions.add(ia.getInstitution());
			}
		}
		return institutions;
	}

	@XmlTransient
	@JsonIgnore
	public boolean isAssociatedWithInstitution(Institution institution) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_MANAGER) {
				InstitutionManager im = (InstitutionManager) r;
				if (im.getInstitution().getId().equals(institution.getId())) {
					return true;
				}
			}
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_ASSISTANT) {
				InstitutionAssistant ia = (InstitutionAssistant) r;
				if (ia.getInstitution().getId().equals(institution.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	@XmlTransient
	@JsonIgnore
	public boolean isAssociatedWithDepartment(Department department) {
		Institution institution = department.getSchool().getInstitution();
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_MANAGER) {
				InstitutionManager im = (InstitutionManager) r;
				if (im.getInstitution().getId().equals(institution.getId())) {
					return true;
				}
			}
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_ASSISTANT) {
				InstitutionAssistant ia = (InstitutionAssistant) r;
				if (ia.getInstitution().getId().equals(institution.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}
