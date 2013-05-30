package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.CompareUtil;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

@Embeddable
public class ContactInformation {

	@Valid
	@Embedded
	private Address address = new Address();

	@Email
	@Column(unique = true)
	private String email;

	@Size(min = 10)
	private String mobile;

	private String phone;

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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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
		if (!CompareUtil.equalsIgnoreNull(this.getPhone(), that.getPhone())) {
			return false;
		}
		return true;
	}

}
