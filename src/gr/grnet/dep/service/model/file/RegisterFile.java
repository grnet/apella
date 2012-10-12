package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Register;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@DiscriminatorValue("RegisterFile")
@XmlRootElement
public class RegisterFile extends FileHeader {

	private static final long serialVersionUID = 3451155353115781870L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.MITROO, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private Register register;

	@JsonView({DetailedFileHeaderView.class})
	public Register getRegister() {
		return register;
	}

	public void setRegister(Register register) {
		this.register = register;
	}

}
