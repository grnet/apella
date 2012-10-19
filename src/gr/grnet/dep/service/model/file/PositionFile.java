package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Position;

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
public class PositionFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES, 1);
			put(FileType.TEKMIRIOSI_EPITROPIS_GIA_AKSIOLOGITES, 1);
			put(FileType.AITIMA_EPITROPIS_PROS_AKSIOLOGITES, 1);
			put(FileType.AKSIOLOGISI_PROTOU_AKSIOLOGITI, Integer.MAX_VALUE);
			put(FileType.AKSIOLOGISI_DEUTEROU_AKSIOLOGITI, Integer.MAX_VALUE);
			put(FileType.PROSKLISI_KOSMITORA, 1);
			put(FileType.EISIGISI_DEP_YPOPSIFIOU, Integer.MAX_VALUE);
			put(FileType.PRAKTIKO_EPILOGIS, 1);
			put(FileType.DIAVIVASTIKO_PRAKTIKOU, 1);
			put(FileType.PRAKSI_DIORISMOU, 1);
			put(FileType.DIOIKITIKO_EGGRAFO, Integer.MAX_VALUE);
			put(FileType.APOFASI_ANAPOMPIS, 1);
		}
	});

	@ManyToOne
	private Position position;

	@JsonView({DetailedFileHeaderView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
