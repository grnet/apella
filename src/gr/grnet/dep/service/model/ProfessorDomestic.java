package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("PROFESSOR_DOMESTIC")
public class ProfessorDomestic extends Professor {

/** Default value included to remove warning. Remove or modify at will. **/
   private static final long serialVersionUID = 1L;
   
   @Inject
   @Transient
   private Logger logger;
   

   @ManyToOne(optional=false)
   private Department department;
   
   @ManyToOne(optional=false)
   private Rank rank;
   
   @ManyToOne(optional=false)
   private Subject subject;
   
   private String fek;
   
   @ManyToOne
   private FileHeader fekFile;

   
   
   public ProfessorDomestic() {
	   super();
		setDiscriminator(RoleDiscriminator.PROFESSOR_DOMESTIC);
   }


	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}

	public Rank getRank() {
		return rank;
	}
	public void setRank(Rank rank) {
		this.rank = rank;
	}

	public Subject getSubject() {
		return subject;
	}
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public String getFek() {
		return fek;
	}
	public void setFek(String fek) {
		this.fek = fek;
	}

	public FileHeader getFekFile() {
		return fekFile;
	}
	public void setFekFile(FileHeader fekFile) {
		this.fekFile = fekFile;
	}

   
}