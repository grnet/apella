package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;


@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@XmlRootElement
@Table(name="Roles")
public abstract class Role implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;
	
	enum RoleDiscriminator {
		PD,
		PF,
		IM,
		DM,
		IA,
		MM,
		CA
	};
   
	@Inject
	@Transient
	private Logger logger;

   
	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Enumerated(value=EnumType.STRING)
	@org.hibernate.annotations.Index(name = "IDX_Roles_discriminator")
	private RoleDiscriminator discriminator;

	// Inverse to User
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public RoleDiscriminator getDiscriminator() {
		return discriminator;
	}
	void setDiscriminator(RoleDiscriminator discriminator) {
		this.discriminator = discriminator;
	}

	@XmlTransient
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}


   
}