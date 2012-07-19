package gr.grnet.dep.service.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MINISTRY_MANAGER")
public class MinistryManager extends Role {

	private static final long serialVersionUID = -4122778019338166729L;

	private String ministry;

	public MinistryManager() {
		super();
		setDiscriminator(RoleDiscriminator.MINISTRY_MANAGER);
	}

	public String getMinistry() {
		return ministry;
	}

	public void setMinistry(String ministry) {
		this.ministry = ministry;
	}

	////////////////////////////////////////////////////////////////////////////	

	@Override
	public void initializeCollections() {
	}

	@Override
	public Role copyFrom(Role otherRole) {
		MinistryManager mm = (MinistryManager) otherRole;
		setMinistry(mm.getMinistry());
		return this;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.ministry == null) {
			return true;
		}
		return false;
	}
}
