package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.util.IdentificationDeserializer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJBException;
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

import org.apache.commons.codec.binary.Base64;
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
	};

	public static interface DetailedWithPasswordUserView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Column(unique = true)
	private String username;

	private String identification;

	@Enumerated(EnumType.STRING)
	private UserRegistrationType registrationType;

	@Valid
	@Embedded
	private ShibbolethInformation shibbolethInfo = new ShibbolethInformation();

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private Date statusDate;

	@Valid
	@Embedded
	private BasicInformation basicInfo = new BasicInformation();

	@Valid
	@Embedded
	private BasicInformation basicInfoLatin = new BasicInformation();

	@Valid
	@Embedded
	private ContactInformation contactInfo = new ContactInformation();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
	private Set<Role> roles = new HashSet<Role>();

	private String password;

	private String passwordSalt;

	private Date registrationDate;

	private Long verificationNumber;

	private String jiraIssueKey;

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

	public String getJiraIssueKey() {
		return jiraIssueKey;
	}

	public void setJiraIssueKey(String jiraIssueKey) {
		this.jiraIssueKey = jiraIssueKey;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
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

	public boolean isMissingRequiredFields() {
		return (this.basicInfo == null || this.basicInfo.isMissingRequiredFields()) ||
			(this.contactInfo == null || this.contactInfo.isMissingRequiredFields()) ||
			(this.registrationType.equals(UserRegistrationType.SHIBBOLETH) && (this.shibbolethInfo == null || this.shibbolethInfo.isMissingRequiredFields())) ||
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

	/**
	 * Static Functions
	 */

	private static final String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";

	private static final int PASSWORD_LENGTH = 8;

	public static String generatePassword() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			String pw = "";
			for (int i = 0; i < PASSWORD_LENGTH; i++) {
				int index = (int) (sr.nextDouble() * letters.length());
				pw += letters.substring(index, index + 1);
			}
			return pw;
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		}
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
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			return sr.nextLong();
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		}
	}

	public String generateAuthenticationToken() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest((this.getUsername() + System.currentTimeMillis()).getBytes("ISO-8859-1"));
			return new String(Base64.encodeBase64(hash), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	public static void main(String[] args) {
		String password = "anglen";
		String salt = User.generatePasswordSalt();
		System.out.println(password + ": " + encodePassword(password, salt));
	}

}
