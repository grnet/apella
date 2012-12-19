package gr.grnet.dep.service.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Embeddable
public class ContactInformation {

	@Valid
	@Embedded
	@NotNull
	private Address address = new Address();

	@NotNull
	@NotEmpty
	@Email
	@Column(unique = true)
	private String email;

	@NotNull
	@Size(min = 10)
	private String mobile;

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public boolean isMissingRequiredFields() {
		if (this.address == null) {
			return true;
		}
		if (this.email == null) {
			return true;
		}
		if (this.mobile == null) {
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
		ContactInformation that = (ContactInformation) obj;
		if (!this.getAddress().equals(that.getAddress())) {
			return false;
		}
		if (!this.getEmail().equals(that.getEmail())) {
			return false;
		}
		if (!this.getMobile().equals(that.getMobile())) {
			return false;
		}
		return true;
	}
}
