package gr.grnet.dep.service.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@Embeddable
public class BasicInformation {

	@NotNull
	@NotEmpty
	private String firstname;

	@NotNull
	@NotEmpty
	private String lastname;

	private String fathername;

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

	///////////////////////////////////////////////////////////////

	public String getFullName() {
		return lastname + " " + firstname;
	}

}
