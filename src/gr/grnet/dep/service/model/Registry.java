package gr.grnet.dep.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Registry {

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(optional = false)
	private Department department;

	@OneToOne
	private FileHeader registryFile;

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

	public FileHeader getRegistryFile() {
		return registryFile;
	}

	public void setRegistryFile(FileHeader registryFile) {
		this.registryFile = registryFile;
	}

	public Registry copyFrom(Registry registry) {
		setDepartment(registry.getDepartment());
		return this;
	}
}
