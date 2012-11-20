package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
public class InstitutionRegulatoryFramework {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private boolean permanent;

	@ManyToOne
	private Institution institution;

	private String organismosURL;

	private String eswterikosKanonismosURL;

	@Temporal(TemporalType.DATE)
	private Date createdAt;

	@Temporal(TemporalType.DATE)
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public String getOrganismosURL() {
		return organismosURL;
	}

	public void setOrganismosURL(String organismosURL) {
		this.organismosURL = organismosURL;
	}

	public String getEswterikosKanonismosURL() {
		return eswterikosKanonismosURL;
	}

	public void setEswterikosKanonismosURL(String eswterikosKanonismosURL) {
		this.eswterikosKanonismosURL = eswterikosKanonismosURL;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCreatedAt() {
		return createdAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
