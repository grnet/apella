package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Entity
@XmlRootElement
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

	////////////////////////////////////////////

	public boolean isUserAllowedToEdit(User user) {
		if (this.getInstitution() == null) {
			return false;
		}
		for (Role r : user.getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.ADMINISTRATOR) {
				return true;
			}
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_MANAGER) {
				InstitutionManager im = (InstitutionManager) r;
				if (im.getInstitution().getId().equals(this.getInstitution().getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
