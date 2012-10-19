package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Institution;

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
public class InstitutionFile extends FileHeader {

	private static final long serialVersionUID = 3451155353115781870L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.ORGANISMOS, 1);
			put(FileType.ESWTERIKOS_KANONISMOS, 1);
		}
	});

	@ManyToOne
	private Institution institution;

	@JsonView({DetailedFileHeaderView.class})
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

}
