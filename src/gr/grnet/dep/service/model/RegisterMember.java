package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

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

	@ManyToMany
	private Set<Subject> subjects;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({RegisterMemberView.class})
	public Register getRegister() {
		return register;
	}

	public void setRegister(Register register) {
		this.register = register;
	}

	@JsonView({RegisterMemberView.class, DetailedRegisterMemberView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public Set<Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(Set<Subject> subjects) {
		this.subjects = subjects;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

}
