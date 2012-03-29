package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("IA")
public class InstitutionAssistant extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;


	@ManyToOne(optional=false)
	private Institution institution;

	@ManyToOne(optional=false)
	private InstitutionManager manager;


	public InstitutionAssistant() {
		super();
		setDiscriminator(RoleDiscriminator.IA);
	}


	public Institution getInstitution() {
		return institution;
	}
	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public InstitutionManager getManager() {
		return manager;
	}
	public void setManager(InstitutionManager manager) {
		this.manager = manager;
	}   

}