package gr.grnet.dep.service.model;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.logging.Logger;

@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends Role {

	/**
	 * Default value included to remove warning. Remove or modify at will. *
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	private boolean superAdministrator = false;

	public Administrator() {
		super();
		setDiscriminator(RoleDiscriminator.ADMINISTRATOR);
	}

	public boolean isSuperAdministrator() {
		return superAdministrator;
	}

	public void setSuperAdministrator(boolean superAdministrator) {
		this.superAdministrator = superAdministrator;
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public Role copyFrom(Role otherRole) {
		// Do not change superAdministrator. it is fixed
		return this;
	}

	@Override
	public boolean compareCriticalFields(Role role) {
		if (!(role instanceof Administrator)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isMissingRequiredFields() {
		return false;
	}

}
