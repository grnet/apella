package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.Position.PositionView;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.PositionCommittee.DetailedPositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluation.DetailedPositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator.DetailedPositionEvaluatorView;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;

import javax.inject.Inject;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

@Entity
@XmlRootElement
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Roles", indexes = {
		@Index(name = "IDX_Roles_discriminator", columnList = "discriminator")
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "discriminator")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProfessorDomestic.class, name = "PROFESSOR_DOMESTIC"),
		@JsonSubTypes.Type(value = ProfessorForeign.class, name = "PROFESSOR_FOREIGN"),
		@JsonSubTypes.Type(value = InstitutionManager.class, name = "INSTITUTION_MANAGER"),
		@JsonSubTypes.Type(value = InstitutionAssistant.class, name = "INSTITUTION_ASSISTANT"),
		@JsonSubTypes.Type(value = MinistryManager.class, name = "MINISTRY_MANAGER"),
		@JsonSubTypes.Type(value = MinistryAssistant.class, name = "MINISTRY_ASSISTANT"),
		@JsonSubTypes.Type(value = Candidate.class, name = "CANDIDATE"),
		@JsonSubTypes.Type(value = Administrator.class, name = "ADMINISTRATOR")
})
public abstract class Role implements Serializable {

	/**
	 * Default value included to remove warning. Remove or modify at will. *
	 */
	private static final long serialVersionUID = 1L;

	public static interface DetailedRoleView {
	}

	public enum RoleDiscriminator {
		PROFESSOR_DOMESTIC,
		PROFESSOR_FOREIGN,
		INSTITUTION_MANAGER,
		INSTITUTION_ASSISTANT,
		MINISTRY_MANAGER,
		MINISTRY_ASSISTANT,
		CANDIDATE,
		ADMINISTRATOR
	}

	public enum RoleStatus {
		UNAPPROVED,
		ACTIVE,
		INACTIVE
	}

	@Inject
	@Transient
	private Logger logger;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;
	@Enumerated(value = EnumType.STRING)
	@NotNull
	private RoleDiscriminator discriminator;

	@Enumerated(EnumType.STRING)
	@NotNull
	private RoleStatus status = RoleStatus.UNAPPROVED;

	private Date statusDate;

	private Date statusEndDate;

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

	public Date getStatusEndDate() {
		return statusEndDate;
	}

	public void setStatusEndDate(Date statusEndDate) {
		this.statusEndDate = statusEndDate;
	}

	@JsonView({
			PositionView.class,
			DetailedRoleView.class,
			DetailedPositionCommitteeMemberView.class,
			DetailedPositionCandidaciesView.class,
			MediumCandidacyView.class,
			DetailedRegisterView.class,
			DetailedRegisterMemberView.class,
			DetailedPositionEvaluatorView.class,
			DetailedPositionEvaluationView.class,
			DetailedPositionCommitteeView.class,
			CandidacyEvaluator.CandidacyEvaluatorView.class})
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	//////////////////////////////////////////////////////////

	public abstract Role copyFrom(Role otherRole);

	public abstract boolean isMissingRequiredFields();

	public abstract boolean compareCriticalFields(Role other);

	public static void updateStatus(Role primaryRole, RoleStatus newStatus) {
		primaryRole.setStatus(newStatus);
		primaryRole.setStatusDate(new Date());
		if (newStatus.equals(RoleStatus.INACTIVE)) {
			primaryRole.setStatusEndDate(new Date());
		}
		// Update all other roles of user (applicable for Professor->Candidate
		for (Role otherRole : primaryRole.getUser().getRoles()) {
			if (otherRole != primaryRole && otherRole.getStatus() != RoleStatus.INACTIVE) {
				otherRole.setStatus(newStatus);
				otherRole.setStatusDate(new Date());
				if (newStatus.equals(RoleStatus.INACTIVE)) {
					otherRole.setStatusEndDate(new Date());
				}
			}
		}
	}

}
