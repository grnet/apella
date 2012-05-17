package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RegisterDomesticMembership implements Serializable {

	@Embeddable
	public static class RegisterDomesticMembershipId implements Serializable {

		@Column(name = "registerDomestic_id")
		private Long register_id;

		@Column(name = "professorDomestic_id")
		private Long professor_id;

		public RegisterDomesticMembershipId() {

		}

		public RegisterDomesticMembershipId(Long registerDomesticId, Long professorDomesticId) {
			this.register_id = registerDomesticId;
			this.professor_id = professorDomesticId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			RegisterDomesticMembershipId that = (RegisterDomesticMembershipId) o;

			if (professor_id != null ? !professor_id.equals(that.professor_id) : that.professor_id != null) {
				return false;
			}

			if (register_id != null ? !register_id.equals(that.register_id) : that.register_id != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (professor_id != null ? professor_id.hashCode() : 0);
			result = 31 * result + (register_id != null ? register_id.hashCode() : 0);
			return result;
		}
	}

	@EmbeddedId
	private RegisterDomesticMembershipId id = new RegisterDomesticMembershipId();

	@ManyToOne
	@JoinColumn(name = "registerDomestic_id", insertable = false, updatable = false)
	private RegisterDomestic register;

	@ManyToOne
	@JoinColumn(name = "professorDomestic_id", insertable = false, updatable = false)
	private ProfessorDomestic professor;

	public RegisterDomesticMembership() {
	}

	public RegisterDomesticMembership(RegisterDomestic rd, ProfessorDomestic pd) {
		this.professor = pd;
		this.register = rd;
		this.id.register_id = rd.getId();
		this.id.professor_id = pd.getId();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RegisterDomesticMembership that = (RegisterDomesticMembership) o;
		return this.id.equals(that.id);
	}

	public RegisterDomestic getRegister() {
		return register;
	}

	public void setRegister(RegisterDomestic register) {
		this.register = register;
	}

	public ProfessorDomestic getProfessor() {
		return professor;
	}

	public void setProfessor(ProfessorDomestic professor) {
		this.professor = professor;
	}

}
