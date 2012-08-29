package gr.grnet.dep.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

	@ManyToOne
	private FileHeader registerFile;

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

	public FileHeader getRegisterFile() {
		return registerFile;
	}

	public void setRegisterFile(FileHeader registerFile) {
		this.registerFile = registerFile;
	}

	public Register copyFrom(Register register) {
		setDepartment(register.getDepartment());
		return this;
	}
}
