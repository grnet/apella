package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RegisterForeignMembership implements Serializable {

	private static final long serialVersionUID = 533452297460873945L;

	@Embeddable
	public static class RegisterForeignMembershipId implements Serializable {

		private static final long serialVersionUID = -5646724108206196710L;

		@Column(name = "registerForeign_id")
		private Long register_id;

		@Column(name = "professorForeign_id")
		private Long professor_id;

		public RegisterForeignMembershipId() {

		}

		public RegisterForeignMembershipId(Long registerForeignId, Long professorForeignId) {
			this.register_id = registerForeignId;
			this.professor_id = professorForeignId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			RegisterForeignMembershipId that = (RegisterForeignMembershipId) o;

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
	private RegisterForeignMembershipId id = new RegisterForeignMembershipId();

	@ManyToOne
	@JoinColumn(name = "registerForeign_id", insertable = false, updatable = false)
	private RegisterForeign register;

	@ManyToOne
	@JoinColumn(name = "professorForeign_id", insertable = false, updatable = false)
	private ProfessorForeign professor;

	public RegisterForeignMembership() {
	}

	public RegisterForeignMembership(RegisterForeign rd, ProfessorForeign pd) {
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
		RegisterForeignMembership that = (RegisterForeignMembership) o;
		return this.id.equals(that.id);
	}

	public RegisterForeign getRegister() {
		return register;
	}

	public void setRegister(RegisterForeign register) {
		this.register = register;
	}

	public ProfessorForeign getProfessor() {
		return professor;
	}

	public void setProfessor(ProfessorForeign professor) {
		this.professor = professor;
	}

}
