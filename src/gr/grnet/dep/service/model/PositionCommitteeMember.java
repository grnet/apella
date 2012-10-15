package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
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
public class PositionCommitteeMember implements Serializable {

	private static final long serialVersionUID = -1339853623265264893L;

	public static final int MAX_MEMBERS = 7;

	public static interface DetailedPositionCommitteeMemberView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Position position;

	@ManyToOne
	private Professor professor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

}
