package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Register;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("RegisterFile")
public class RegisterFile extends FileHeader {

	private static final long serialVersionUID = 3451155353115781870L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.MITROO, -1);
		}
	});

	@ManyToOne
	private Register register;

	public Register getRegister() {
		return register;
	}

	public void setRegister(Register register) {
		this.register = register;
	}

}
