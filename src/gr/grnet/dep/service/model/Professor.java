package gr.grnet.dep.service.model;

import javax.persistence.Entity;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	private String position;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setPosition(p.getPosition());
		return this;
	}

}
