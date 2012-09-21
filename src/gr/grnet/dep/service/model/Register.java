package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.RegisterFile;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"department_id"})})
public class Register implements Serializable {

	private static final long serialVersionUID = 6147648073392341863L;

	public static interface DetailedRegisterView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	private Department department;

	@OneToMany(mappedBy = "register", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RegisterFile> files = new HashSet<RegisterFile>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Set<RegisterFile> getFiles() {
		return files;
	}

	public void setFiles(Set<RegisterFile> files) {
		this.files = files;
	}

	public Register copyFrom(Register register) {
		setDepartment(register.getDepartment());
		return this;
	}
}
