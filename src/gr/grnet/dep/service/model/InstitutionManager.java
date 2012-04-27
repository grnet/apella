package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("INSTITUTION_MANAGER")
public class InstitutionManager extends Role {

/** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
   @Inject
   @Transient
   private Logger logger;


   @ManyToOne(optional=false)
   private Institution institution;

   
   public InstitutionManager() {
	   super();
		setDiscriminator(RoleDiscriminator.INSTITUTION_MANAGER);
   }

   
   public Institution getInstitution() {
		return institution;
	}
	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	
	////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void initializeCollections() {
	}   
  
}