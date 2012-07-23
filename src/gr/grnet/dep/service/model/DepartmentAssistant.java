package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("DEPARTMENT_ASSISTANT")
public class DepartmentAssistant extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne
	private Department department;

	@ManyToOne
	private InstitutionManager manager;

	public DepartmentAssistant() {
		super();
		setDiscriminator(RoleDiscriminator.DEPARTMENT_ASSISTANT);
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	////////////////////////////////////////////////////////////////////////////

	public InstitutionManager getManager() {
		return manager;
	}

	public void setManager(InstitutionManager manager) {
		this.manager = manager;
	}

	@Override
	public void initializeCollections() {
	}

	@Override
	public Role copyFrom(Role otherRole) {
		DepartmentAssistant dp = (DepartmentAssistant) otherRole;
		this.setDepartment(dp.getDepartment());
		return this;
	}

	@Override
	public boolean isMissingRequiredFields() {
		if (this.department == null) {
			return true;
		}
		return false;
	}

}
