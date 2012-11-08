package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(columnNames = {"position_id", "professor_id"})
})
public class CommitteeMember implements Serializable {

	private static final long serialVersionUID = -1339853623265264893L;

	public static final int MAX_MEMBERS = 7;

	public static interface ProfessorCommitteesView {
	};

	public static interface DetailedPositionCommitteeMemberView {
	};

	public enum MemberType {
		REGULAR, SUBSTITUTE
	}

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private MemberType type;

	@ManyToOne
	private Committee committee;

	@ManyToOne
	private Professor professor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({ProfessorCommitteesView.class, DetailedPositionCommitteeMemberView.class})
	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public MemberType getType() {
		return type;
	}

	public void setType(MemberType type) {
		this.type = type;
	}

}
