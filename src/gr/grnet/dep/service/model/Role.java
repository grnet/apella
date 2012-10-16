package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
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
	@JsonSubTypes.Type(value = InstitutionAssistant.class, name = "INSTITUTION_ASSISTANT"),
	@JsonSubTypes.Type(value = MinistryManager.class, name = "MINISTRY_MANAGER"),
	@JsonSubTypes.Type(value = Candidate.class, name = "CANDIDATE"),
	@JsonSubTypes.Type(value = Administrator.class, name = "ADMINISTRATOR")
})
public abstract class Role implements Serializable {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static interface DetailedRoleView {
	};

	public enum RoleDiscriminator {
		PROFESSOR_DOMESTIC,
		PROFESSOR_FOREIGN,
		INSTITUTION_MANAGER,
		INSTITUTION_ASSISTANT,
		MINISTRY_MANAGER,
		CANDIDATE,
		ADMINISTRATOR
	};

	public enum RoleStatus {
		UNAPPROVED,
		ACTIVE,
		BLOCKED
	};

	@Inject
	@Transient
	private Logger logger;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Enumerated(value = EnumType.STRING)
	@org.hibernate.annotations.Index(name = "IDX_Roles_discriminator")
	@NotNull
	private RoleDiscriminator discriminator;

	@Enumerated(EnumType.STRING)
	@NotNull
	private RoleStatus status = RoleStatus.UNAPPROVED;

	private Date statusDate;

	@ManyToOne
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

	@JsonView({DetailedRoleView.class, DetailedPositionCommitteeMemberView.class})
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	//////////////////////////////////////////////////////////

	public abstract void initializeCollections();

	public abstract Role copyFrom(Role otherRole);

	public abstract boolean isMissingRequiredFields();

	public abstract boolean compareCriticalFields(Role other);

	public boolean compare(Object a, Object b) {
		return a == null ?
			(b == null ? true : false) :
			(b == null ? false : a.equals(b));
	}
}
