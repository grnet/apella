package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("DEPARTMENT_MANAGER")
public class DepartmentManager extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Inject
	@Transient
	private Logger logger;

	@ManyToOne
	private Department department;

	public DepartmentManager() {
		super();
		setDiscriminator(RoleDiscriminator.DEPARTMENT_MANAGER);
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	////////////////////////////////////////////////////////////////////////////

	@Override
	public void initializeCollections() {
	}

	@Override
	public Role copyFrom(Role otherRole) {
		DepartmentManager dp = (DepartmentManager) otherRole;
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
