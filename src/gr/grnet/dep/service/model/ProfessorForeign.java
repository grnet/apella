package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("PF")
public class ProfessorForeign extends Professor {

/** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
   @Inject
   @Transient
   private Logger logger;

  
   public ProfessorForeign() {
	   super();
	   setDiscriminator(RoleDiscriminator.PF);
   }


   
  
}