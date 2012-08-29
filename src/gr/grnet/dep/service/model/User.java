package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@XmlRootElement
@Table(name = "Users")
public class User implements Serializable {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public enum UserStatus {
		CREATED, UNVERIFIED, ACTIVE, BLOCKED, DELETED
	}

	public static interface DetailedUserView {
	};

	public static interface DetailedWithPasswordUserView {
	};

	@Inject
	@Transient
	private Logger logger;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	@Column(unique = true)
	private String username;

	@Enumerated(EnumType.STRING)
	private UserRegistrationType registrationType;

	@Column(unique = true)
	private String shibPersonalUniqueCode;

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private Date statusDate;

	@Valid
	@Embedded
	@NotNull
	private BasicInformation basicInfo = new BasicInformation();

	@Valid
	@Embedded
	@NotNull
	private BasicInformation basicInfoLatin = new BasicInformation();

	@Valid
	@Embedded
	@NotNull
	private ContactInformation contactInfo = new ContactInformation();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private Set<Role> roles = new HashSet<Role>();

	private String password;

	private String passwordSalt;

	@NotNull
	private Date registrationDate;

	private Long verificationNumber;

	private Date lastLoginDate;

	@Column(unique = true)
	private String authToken;

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

	public String getShibPersonalUniqueCode() {
		return shibPersonalUniqueCode;
	}

	public void setShibPersonalUniqueCode(String shibPersonalUniqueCode) {
		this.shibPersonalUniqueCode = shibPersonalUniqueCode;
	}

	public UserRegistrationType getRegistrationType() {
		return registrationType;
	}

	public void setRegistrationType(UserRegistrationType registrationType) {
		this.registrationType = registrationType;
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
		this.basicInfo = basicInfo;
	}

	public BasicInformation getBasicInfoLatin() {
		return basicInfoLatin;
	}

	public void setBasicInfoLatin(BasicInformation basicInfoLatin) {
		this.basicInfoLatin = basicInfoLatin;
	}

	public ContactInformation getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(ContactInformation contactInfo) {
		this.contactInfo = contactInfo;
	}

	@JsonView({DetailedWithPasswordUserView.class})
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlTransient
	public String getPasswordSalt() {
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	@JsonView({DetailedUserView.class})
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Long getVerificationNumber() {
		return verificationNumber;
	}

	public void setVerificationNumber(Long verificationNumber) {
		this.verificationNumber = verificationNumber;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	@XmlTransient
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	///////////////////////////////////////////////////////////////////////////////////////

	public void addRole(Role role) {
		roles.add(role);
		role.setUser(this);
	}

	public void removeRole(Role role) {
		roles.remove(role);
		role.setUser(null);
	}

	public Role getRole(RoleDiscriminator discriminator) {
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == discriminator && r.getStatus().equals(RoleStatus.ACTIVE)) {
				return r;
			}
		}
		return null;
	}

	public boolean hasRole(RoleDiscriminator discriminator) {
		return getRole(discriminator) != null;
	}

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

	public boolean isDepartmentUser(Department department) {
		Institution institution = department.getInstitution();
		for (Role r : getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.DEPARTMENT_ASSISTANT) {
				DepartmentAssistant dm = (DepartmentAssistant) r;
				if (dm.getDepartment().getId().equals(department.getId())) {
					return true;
				}
			}
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

	public static String generatePasswordSalt() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[10];
			sr.nextBytes(salt);
			return new String(Base64.encodeBase64(salt), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	public static String encodePassword(String password, String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest(password.concat(salt).getBytes("ISO-8859-1"));
			return new String(Base64.encodeBase64(hash), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	public Long generateVerificationNumber() {
		return System.currentTimeMillis();
	}

	public String generateAuthenticationToken() {
		return this.getUsername() + "" + System.currentTimeMillis();
	}

	public static void main(String[] args) {
		String password = "anglen";
		String salt = User.generatePasswordSalt();
		System.out.println(password + ": " + encodePassword(password, salt));
	}

}
