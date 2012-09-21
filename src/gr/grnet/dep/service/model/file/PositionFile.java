package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Position;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("PositionFile")
public class PositionFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			// TODO:
			put(FileType.ANAFORA, 1);
			put(FileType.EISIGITIKI_ANAFORA, 1);
			put(FileType.FEK, 1);
			put(FileType.PROFILE, 1);
			put(FileType.MITROO, 1);
			put(FileType.PRAKTIKO_SYNEDRIASIS, 1);
			put(FileType.TEKMIRIOSI_AKSIOLOGITI, 1);
			put(FileType.AITIMA_EPITROPIS, 1);
			put(FileType.AKSIOLOGISI, 1);
			put(FileType.EISIGISI, 1);
			put(FileType.PROSKLISI_KOSMITORA, 1);
			put(FileType.PRAKTIKO_EPILOGIS, 1);
			put(FileType.DIAVIVASTIKO_PRAKTIKOU, 1);
			put(FileType.PRAKSI_DIORISMOU, 1);
			put(FileType.APOFASI_ANAPOMPIS, 1);
			put(FileType.DIOIKITIKO_EGGRAFO, 1);
			put(FileType.ORGANISMOS, 1);
			put(FileType.ESWTERIKOS_KANONISMOS, 1);
		}
	});

	@ManyToOne
	private Position position;

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
