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

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
@Table(name = "Roles")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "discriminator")
@JsonSubTypes({
	@JsonSubTypes.Type(value = ProfessorDomestic.class, name = "PROFESSOR_DOMESTIC"),
	@JsonSubTypes.Type(value = ProfessorForeign.class, name = "PROFESSOR_FOREIGN"),
	@JsonSubTypes.Type(value = InstitutionManager.class, name = "INSTITUTION_MANAGER"),
	@JsonSubTypes.Type(value = DepartmentManager.class, name = "DEPARTMENT_MANAGER"),
	@JsonSubTypes.Type(value = InstitutionAssistant.class, name = "INSTITUTION_ASSISTANT"),
	@JsonSubTypes.Type(value = MinistryManager.class, name = "MINISTRY_MANAGER"),
	@JsonSubTypes.Type(value = Candidate.class, name = "CANDIDATE")
})
public abstract class Role implements Serializable {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;
	
	
	// define 3 json views
	public static interface IdRoleView {
	}; // shows only id view of a Role
	
	public static interface SimpleRoleView extends IdRoleView {
	}; // shows a summary view of a Role

	public static interface DetailedRoleView extends SimpleRoleView {
	};
	

	enum RoleDiscriminator {
		PROFESSOR_DOMESTIC,
		PROFESSOR_FOREIGN,
		INSTITUTION_MANAGER,
		DEPARTMENT_MANAGER,
		INSTITUTION_ASSISTANT,
		MINISTRY_MANAGER,
		CANDIDATE
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

	@Enumerated(value = EnumType.STRING)
	@org.hibernate.annotations.Index(name = "IDX_Roles_discriminator")
	private RoleDiscriminator discriminator;

	// Inverse to User
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({ SimpleRoleView.class })
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
	
	
	//////////////////////////////////////////////////////////
	
	public abstract void initializeCollections() ;

}
