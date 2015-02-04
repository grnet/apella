package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.Candidacy.CandidacyView;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.PositionCommittee.PositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember.PositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluation.PositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Entity
@XmlRootElement
public class RegisterMember implements Serializable {

	private static final long serialVersionUID = -2335307229946834215L;

	public static interface RegisterMemberView {
	}

	public static interface DetailedRegisterMemberView extends RegisterMemberView {
	}

	private static final Logger log = Logger.getLogger(RegisterMember.class.getName());

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Register register;

	@ManyToOne
	private Professor professor;

	private boolean external;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean deleted = false;

	@Transient
	private boolean canBeDeleted = true;

	@OneToMany(mappedBy = "registerMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PositionCommitteeMember> committees = new HashSet<PositionCommitteeMember>();

	@OneToMany(mappedBy = "registerMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PositionEvaluator> evaluations = new HashSet<PositionEvaluator>();

	@OneToMany(mappedBy = "registerMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<CandidacyEvaluator> candidacyEvaluations = new HashSet<CandidacyEvaluator>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({RegisterMemberView.class, PositionCommitteeView.class, PositionCommitteeMemberView.class, PositionEvaluatorView.class, PositionEvaluationView.class, CandidacyEvaluator.CandidacyEvaluatorView.class})
	public Register getRegister() {
		return register;
	}

	public void setRegister(Register register) {
		this.register = register;
	}

	@JsonView({DetailedRegisterView.class,
			RegisterMemberView.class,
			PositionCommitteeView.class,
			PositionCommitteeMemberView.class,
			PositionEvaluatorView.class,
			PositionEvaluationView.class,
			CandidacyEvaluator.CandidacyEvaluatorView.class,
			CandidacyView.class,
			DetailedPositionCandidaciesView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@XmlTransient
	@JsonIgnore
	public Set<PositionCommitteeMember> getCommittees() {
		return committees;
	}

	public void setCommittees(Set<PositionCommitteeMember> committees) {
		this.committees = committees;
	}

	@XmlTransient
	@JsonIgnore
	public Set<PositionEvaluator> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Set<PositionEvaluator> evaluations) {
		this.evaluations = evaluations;
	}

	@XmlTransient
	@JsonIgnore
	public Set<CandidacyEvaluator> getCandidacyEvaluations() {
		return candidacyEvaluations;
	}

	public void setCandidacyEvaluations(Set<CandidacyEvaluator> candidacyEvaluations) {
		this.candidacyEvaluations = candidacyEvaluations;
	}

	@JsonView({DetailedRegisterView.class})
	public boolean getCanBeDeleted() {
		return this.canBeDeleted;
	}

	public void setCanBeDeleted(boolean canBeDeleted) {
		this.canBeDeleted = canBeDeleted;
	}

}
