package gr.grnet.dep.service.model.file;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.PositionComplementaryDocuments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("ComplementaryDocumentsFile")
@XmlRootElement
public class ComplementaryDocumentsFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.DIOIKITIKO_EGGRAFO, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private PositionComplementaryDocuments complementaryDocuments;

	@JsonView({DetailedFileHeaderView.class})
	public PositionComplementaryDocuments getComplementaryDocuments() {
		return complementaryDocuments;
	}

	public void setComplementaryDocuments(PositionComplementaryDocuments complementaryDocuments) {
		this.complementaryDocuments = complementaryDocuments;
	}

}
