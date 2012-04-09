package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.codec.binary.Base64;
import org.jboss.logmanager.Level;

@Entity
@XmlRootElement
@Table(name="Users",
uniqueConstraints = {@UniqueConstraint(columnNames = "username"),
			@UniqueConstraint(columnNames = "email")})
public class User implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;


	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@NotNull
	@Size(min = 1, max = 25)
	@Pattern(regexp = "[A-Za-z0-9]*", message = "must contain only letters and digits")
	private String username;

	@Valid
	@Embedded
	@NotNull
	private BasicInformation basicInfo = new BasicInformation();

	@Valid
	@Embedded
	@NotNull
	private ContactInformation contactInfo = new ContactInformation();


	/**
	 * This will only be used for external users. (Non-shibboleth authenticated)
	 */
	@SuppressWarnings("unused")
	private String password;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="user", fetch=FetchType.EAGER)
	private Set<Role> roles = new HashSet<Role>();
	
	private boolean active = true;
	
	@NotNull
	private Date registrationDate;
	
	private Date lastLoginDate;



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

	public BasicInformation getBasicInfo() {
		return basicInfo;
	}
	public void setBasicInfo(BasicInformation basicInfo) {
		this.basicInfo = basicInfo;
	}

	public ContactInformation getContactInfo() {
		return contactInfo;
	}
	public void setContactInfo(ContactInformation contactInfo) {
		this.contactInfo = contactInfo;
	}


	/*
	 * No getter for password
	 */
	public void setPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest(password.getBytes("ISO-8859-1"));
			this.password = new String(Base64.encodeBase64(hash), "ISO-8859-1");
		}
		catch (NoSuchAlgorithmException nsae) {
			logger.log(Level.SEVERE, "", nsae);
			throw new EJBException(nsae);
		}
		catch (UnsupportedEncodingException uee) {
			logger.log(Level.SEVERE, "", uee);
			throw new EJBException(uee);
		}
	}

	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
	
	public Date getLastLoginDate() {
		return lastLoginDate;
	}
	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	

	public void addRole(Role role) {
		roles.add(role);
		role.setUser(this);
	}

	public void removeRole(Role role) {
		roles.remove(role);
		role.setUser(null);
	}

	


}
