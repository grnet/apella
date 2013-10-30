package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	public Administrator() {
		super();
		setDiscriminator(RoleDiscriminator.ADMINISTRATOR);
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public Role copyFrom(Role otherRole) {
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
