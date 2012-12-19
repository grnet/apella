package gr.grnet.dep.service.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@Embeddable
public class Address {

	@NotNull
	@NotEmpty
	private String street;

	private String number;

	@NotNull
	@NotEmpty
	private String zip;

	private String city;

	private String country;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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
		Address that = (Address) obj;
		if (!this.getCity().equals(that.getCity())) {
			return false;
		}
		if (!this.getCountry().equals(that.getCountry())) {
			return false;
		}
		if (!this.getNumber().equals(that.getNumber())) {
			return false;
		}
		if (!this.getStreet().equals(that.getStreet())) {
			return false;
		}
		if (!this.getZip().equals(that.getZip())) {
			return false;
		}
		return true;
	}

}
