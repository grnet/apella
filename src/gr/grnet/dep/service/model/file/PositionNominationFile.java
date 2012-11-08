package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Nomination;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@DiscriminatorValue("PositionFile")
@XmlRootElement
public class PositionNominationFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.PROSKLISI_KOSMITORA, 1);
			put(FileType.PRAKTIKO_EPILOGIS, Integer.MAX_VALUE);
			put(FileType.DIAVIVASTIKO_PRAKTIKOU, 1);
			put(FileType.PRAKSI_DIORISMOU, 1);
			put(FileType.APOFASI_ANAPOMPIS, 1);
		}
	});

	@ManyToOne
	private Nomination nomination;

	@JsonView({DetailedFileHeaderView.class})
	public Nomination getNomination() {
		return nomination;
	}

	public void setNomination(Nomination nomination) {
		this.nomination = nomination;
	}

}
