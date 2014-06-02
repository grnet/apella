package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Professor;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("ProfessorFile")
@XmlRootElement
public class ProfessorFile extends FileHeader {

	private static final long serialVersionUID = -3078442579988112911L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.PROFILE, 1);
			put(FileType.FEK, 1);
		}
	});

	@ManyToOne
	private Professor professor;

	@JsonView({DetailedFileHeaderView.class})
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

}
