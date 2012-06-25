package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

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
	@JsonSubTypes.Type(value = Candidate.class, name = "CANDIDATE"),
	@JsonSubTypes.Type(value = Administrator.class, name = "ADMINISTRATOR")
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

	public enum RoleDiscriminator {
		PROFESSOR_DOMESTIC,
		PROFESSOR_FOREIGN,
		INSTITUTION_MANAGER,
		DEPARTMENT_MANAGER,
		INSTITUTION_ASSISTANT,
		MINISTRY_MANAGER,
		CANDIDATE,
		ADMINISTRATOR
	};

	public enum RoleStatus {
		CREATED,
		UNAPPROVED,
		ACTIVE,
		BLOCKED,
		DELETED
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

	@Enumerated(EnumType.STRING)
	private RoleStatus status;

	private Date statusDate;

	// Inverse to User
	@Basic(optional = false)
	@Column(name = "user_id")
	private Long user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({SimpleRoleView.class})
	public RoleDiscriminator getDiscriminator() {
		return discriminator;
	}

	void setDiscriminator(RoleDiscriminator discriminator) {
		this.discriminator = discriminator;
	}

	public RoleStatus getStatus() {
		return status;
	}

	public void setStatus(RoleStatus status) {
		this.status = status;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

	@JsonView({SimpleRoleView.class})
	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	//////////////////////////////////////////////////////////

	public abstract void initializeCollections();

	public abstract Role copyFrom(Role otherRole);

	public abstract boolean isMissingRequiredFields();

}
