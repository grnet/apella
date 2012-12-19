package gr.grnet.dep.service.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

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

	@XmlTransient
	public String getFullName() {
		return lastname + " " + firstname;
	}

	public boolean isMissingRequiredFields() {
		if (this.firstname == null) {
			return true;
		}
		if (this.lastname == null) {
			return true;
		}
		if (this.fathername == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BasicInformation that = (BasicInformation) obj;
		if (!this.getFirstname().equals(that.getFirstname())) {
			return false;
		}
		if (!this.getLastname().equals(that.getLastname())) {
			return false;
		}
		if (!this.getFathername().equals(that.getFathername())) {
			return false;
		}
		return true;
	}

}
