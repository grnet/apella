package gr.grnet.dep.service.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Digits;
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
	private String email;

	@NotNull
	@Size(min = 10, max = 12)
	@Digits(fraction = 0, integer = 12)
	private String phoneNumber;

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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
