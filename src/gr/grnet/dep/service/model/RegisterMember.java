package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionCommittee.PositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember.PositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionEvaluation.PositionEvaluationView;
import gr.grnet.dep.service.model.PositionEvaluator.PositionEvaluatorView;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@XmlRootElement
public class RegisterMember implements Serializable {

	private static final long serialVersionUID = -2335307229946834215L;

	public static interface RegisterMemberView {
	};

	public static interface DetailedRegisterMemberView extends RegisterMemberView {
	};

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

	private boolean deleted = false;

	@OneToMany(mappedBy = "registerMember", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionCommitteeMember> committees = new HashSet<PositionCommitteeMember>();

	@OneToMany(mappedBy = "registerMember", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionEvaluator> evaluations = new HashSet<PositionEvaluator>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({RegisterMemberView.class, PositionCommitteeView.class, PositionCommitteeMemberView.class, PositionEvaluatorView.class, PositionEvaluationView.class})
	public Register getRegister() {
		return register;
	}

	public void setRegister(Register register) {
		this.register = register;
	}

	@JsonView({DetailedRegisterView.class, RegisterMemberView.class, PositionCommitteeView.class, PositionCommitteeMemberView.class, PositionEvaluatorView.class, PositionEvaluationView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	@XmlTransient
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	@XmlTransient
	public Set<PositionCommitteeMember> getCommittees() {
		return committees;
	}

	public void setCommittees(Set<PositionCommitteeMember> committees) {
		this.committees = committees;
	}

	@XmlTransient
	public Set<PositionEvaluator> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Set<PositionEvaluator> evaluations) {
		this.evaluations = evaluations;
	}

	///////////////////////////////

	public void initializeCollections() {
		this.evaluations.size();
		this.professor.initializeCollections();
	}

}
