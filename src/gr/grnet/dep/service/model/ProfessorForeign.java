package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("PROFESSOR_FOREIGN")
public class ProfessorForeign extends Professor {

/** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
   @Inject
   @Transient
   private Logger logger;

   
   private String rank;
   
   private String institution;
   
   private String department;
   
   private String subject;
   
   
   
   public ProfessorForeign() {
	   super();
		setDiscriminator(RoleDiscriminator.PROFESSOR_FOREIGN);
   }



	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}

	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}


	////////////////////////////////////////////////////////////////////////////
	

	@Override
	public void initializeCollections() {	
	}
   
  
}