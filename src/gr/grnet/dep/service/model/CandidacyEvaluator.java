package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
public class CandidacyEvaluator implements Serializable {

	private static final long serialVersionUID = -8436764731554690767L;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@NotNull
	private String fullname;

	@NotNull
	private String email;

	@ManyToOne
	private Candidacy candidacy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Candidacy getCandidacy() {
		return candidacy;
	}

	public void setCandidacy(Candidacy candidacy) {
		this.candidacy = candidacy;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

}
