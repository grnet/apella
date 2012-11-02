package gr.grnet.dep.service.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("MINISTRY_ASSISTANT")
public class MinistryAssistant extends Role {

	private static final long serialVersionUID = -4122778019338166729L;

	@ManyToOne
	private MinistryManager manager;

	public MinistryAssistant() {
		super();
		setDiscriminator(RoleDiscriminator.MINISTRY_ASSISTANT);
	}

	public MinistryManager getManager() {
		return manager;
	}

	public void setManager(MinistryManager manager) {
		this.manager = manager;
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public void initializeCollections() {
		this.manager.initializeCollections();
	}

	@Override
	public Role copyFrom(Role otherRole) {
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof MinistryAssistant)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.manager == null) {
			return true;
		}
		return false;
	}
}
