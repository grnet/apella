package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
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
   
   @NotNull
   @NotEmpty
   private String firstname;
   
   @NotNull
   @NotEmpty
   private String lastname;

   @NotNull
   @NotEmpty
   @Email
   private String email;

   @NotNull
   @Size(min = 10, max = 12)
   @Digits(fraction = 0, integer = 12)
   @Column(name = "phone_number")
   private String phoneNumber;
   
   /**
    * This will only be used for external users. (Non-shibboleth authenticated)
    */
   @SuppressWarnings("unused")
   private String password;
   
   @OneToMany(mappedBy="user", cascade=CascadeType.ALL)
   private Set<Role> roles = new HashSet<Role>();
   
   

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

   public String getEmail() {
	   return email;
   }

   public void setEmail(String email) {
	   this.email = email;
   }

   public String getPhoneNumber() {
	   return phoneNumber;
   }

   public void setPhoneNumber(String phoneNumber) {
	   this.phoneNumber = phoneNumber;
   }

	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
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
	
	public void addRole(Role role) {
		roles.add(role);
		role.setUser(this);
	}
	
	public void removeRole(Role role) {
		roles.remove(role);
		role.setUser(null);
	}
}