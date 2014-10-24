package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
public class Register implements Serializable {

	private static final long serialVersionUID = 6147648073392341863L;

	public static interface RegisterView {
	}

	public static interface DetailedRegisterView extends RegisterView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private boolean permanent;

	@ManyToOne(optional = false)
	private Institution institution;

	@ManyToOne
	private Subject subject;

	@OneToMany(mappedBy = "register", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<RegisterMember> members = new HashSet<RegisterMember>();

	@Transient
	private Boolean amMember = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	@JsonView({DetailedRegisterView.class})
	public Set<RegisterMember> getMembers() {
		return members;
	}

	public void setMembers(Set<RegisterMember> members) {
		this.members = members;
	}

	public void addMember(RegisterMember member) {
		member.setRegister(this);
		this.members.add(member);
	}

	public Boolean getAmMember() {
		return amMember;
	}

	////////////////////////////////////////////////////

	public void setAmMember(Boolean amMember) {
		this.amMember = amMember;
	}

	public Register copyFrom(Register register) {
		return this;
	}

	public boolean isUserAllowedToEdit(User user) {
		if (this.getInstitution() == null) {
			return false;
		}
		for (Role r : user.getRoles()) {
			if (r.getDiscriminator() == RoleDiscriminator.ADMINISTRATOR) {
				return true;
			}
			if (r.getDiscriminator() == RoleDiscriminator.INSTITUTION_MANAGER) {
				InstitutionManager im = (InstitutionManager) r;
				if (im.getInstitution().getId().equals(this.getInstitution().getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
