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

}
