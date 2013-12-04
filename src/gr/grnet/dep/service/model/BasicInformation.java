package gr.grnet.dep.service.model;

import java.util.Locale;

import javax.persistence.Embeddable;

@Embeddable
public class BasicInformation {

	private String firstname;

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
			this.firstname = toUppercaseNoTones(firstname, locale);
		}
		if (this.lastname != null && !this.lastname.isEmpty()) {
			this.lastname = toUppercaseNoTones(lastname, locale);
		}
		if (this.fathername != null && !this.fathername.isEmpty()) {
			this.fathername = toUppercaseNoTones(lastname, locale);
		}
	}

	private String toUppercaseNoTones(String s, Locale locale) {
		//FIRST CHARACTER KEEPS TONE
		return s.substring(0, 1).toUpperCase(locale) +
			s.substring(1).toUpperCase(locale)
				.replaceAll("Ά", "Α")
				.replaceAll("Έ", "Ε")
				.replaceAll("Ή", "Η")
				.replaceAll("Ί", "Ι")
				.replaceAll("Ύ", "Υ")
				.replaceAll("Ό", "Ο")
				.replaceAll("Ώ", "Ω");
	}
}
