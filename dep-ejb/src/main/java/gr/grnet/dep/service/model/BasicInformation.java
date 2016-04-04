package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.StringUtil;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import java.util.Locale;

@Embeddable
public class BasicInformation {

	@XmlElement(nillable = true)
	private String firstname;

	@XmlElement(nillable = true)
	private String lastname;

	@XmlElement(nillable = true)
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

	public void toUppercase(Locale locale) {
		if (this.firstname != null && !this.firstname.isEmpty()) {
			this.firstname = StringUtil.toUppercaseNoTones(firstname, locale);
		}
		if (this.lastname != null && !this.lastname.isEmpty()) {
			this.lastname = StringUtil.toUppercaseNoTones(lastname, locale);
		}
		if (this.fathername != null && !this.fathername.isEmpty()) {
			this.fathername = StringUtil.toUppercaseNoTones(fathername, locale);
		}
	}
}
