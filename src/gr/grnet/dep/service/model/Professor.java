package gr.grnet.dep.service.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public abstract class Professor extends Role {

	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	@Column(columnDefinition = " boolean DEFAULT true")
	private boolean active = true;

	private String position;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/////////////////////////////////////////////////////////////

	@Override
	public Role copyFrom(Role otherRole) {
		Professor p = (Professor) otherRole;
		setPosition(p.getPosition());
		return this;
	}

}
