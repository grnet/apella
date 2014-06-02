package gr.grnet.dep.service.model;

import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class ProfessorDomesticData implements Serializable {

	private static final long serialVersionUID = -4886809324903547462L;

	@Id
	@Email
	private String email;

	private String firstname;

	private String lastname;

	private String fathername;

	@ManyToOne
	private Institution institution;

	@ManyToOne
	private Department department;

	@ManyToOne
	private Rank rank;

	private String fek;

	@Column(length = 2048)
	private String fekSubject;

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFathername() {
		return fathername;
	}

	public void setFathername(String fathername) {
		this.fathername = fathername;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public String getFek() {
		return fek;
	}

	public void setFek(String fek) {
		this.fek = fek;
	}

	public String getFekSubject() {
		return fekSubject;
	}

	public void setFekSubject(String fekSubject) {
		this.fekSubject = fekSubject;
	}

}
