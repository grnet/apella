package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.util.IdentificationDeserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@XmlRootElement
@Table(name = "Users")
public class User implements Serializable {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public enum UserStatus {
		UNVERIFIED, ACTIVE, BLOCKED
	}

	public static interface DetailedUserView {
	}

	public static interface DetailedWithPasswordUserView {
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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
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
		return basicInfo;
	}

	public void setBasicInfo(BasicInformation basicInfo) {
		// Hibernate hack for null fields in embeddable objects
		if (basicInfo == null) {
			this.basicInfo = new BasicInformation();
		} else {
			this.basicInfo = basicInfo;
		}
	}

	public BasicInformation getBasicInfoLatin() {
		return basicInfoLatin;
	}

	public void setBasicInfoLatin(BasicInformation basicInfoLatin) {
		// Hibernate hack for null fields in embeddable objects
		if (basicInfoLatin == null) {
			this.basicInfoLatin = new BasicInformation();
		} else {
			this.basicInfoLatin = basicInfoLatin;
		}
	}

	public ContactInformation getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(ContactInformation contactInfo) {
		// Hibernate hack for null fields in embeddable objects
		if (contactInfo == null) {
			this.contactInfo = new ContactInformation();
		} else {
			this.contactInfo = contactInfo;
		}
	}

	@JsonView({DetailedUserView.class})
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@JsonView({DetailedWithPasswordUserView.class})
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
	public Date creationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@JsonView({DetailedUserView.class})
	public Long getVerificationNumber() {
		return verificationNumber;
	}

	public void setVerificationNumber(Long verificationNumber) {
		this.verificationNumber = verificationNumber;
	}

	@XmlTransient
	@JsonIgnore
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

	@XmlTransient
	@JsonIgnore
	public String getFullName() {
		if (this.basicInfo == null) {
			return "- -";
		}
		return this.basicInfo.getFirstname() + " " + this.basicInfo.getLastname();
	}

	public boolean isMissingRequiredFields() {
		return (this.basicInfo == null || this.basicInfo.isMissingRequiredFields()) ||
			(this.contactInfo == null || this.contactInfo.isMissingRequiredFields()) ||
			this.identification == null;
	}

	public void addRole(Role role) {
		roles.add(role);
		role.setUser(this);
	}

	public void removeRole(Role role) {
		roles.remove(role);
		role.setUser(null);
	}

	private static final List<RoleDiscriminator> roleOrder = Arrays.asList(new RoleDiscriminator[] {
		RoleDiscriminator.PROFESSOR_DOMESTIC,
		RoleDiscriminator.PROFESSOR_FOREIGN,
		RoleDiscriminator.CANDIDATE,
		RoleDiscriminator.INSTITUTION_MANAGER,
		RoleDiscriminator.INSTITUTION_ASSISTANT,
		RoleDiscriminator.MINISTRY_MANAGER,
		RoleDiscriminator.MINISTRY_ASSISTANT,
		RoleDiscriminator.ADMINISTRATOR
	});

	@JsonView({DetailedUserView.class, DetailedRoleView.class, PositionView.class})
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
	public Role getActiveRole(RoleDiscriminator discriminator) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == discriminator && r.getStatus().equals(RoleStatus.ACTIVE)) {
				return r;
			}
		}
		return null;
	}

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
	public boolean isInstitutionUser(Institution institution) {
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
	public boolean isDepartmentUser(Department department) {
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
