package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
@DiscriminatorValue("MINISTRY_MANAGER")
public class MinistryManager extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;


	@NotNull
	@NotEmpty
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


}