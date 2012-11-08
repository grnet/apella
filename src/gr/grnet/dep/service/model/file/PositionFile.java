package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.PositionPhase;

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
			put(FileType.AKSIOLOGISI_PROTOU_AKSIOLOGITI, Integer.MAX_VALUE);
			put(FileType.AKSIOLOGISI_DEUTEROU_AKSIOLOGITI, Integer.MAX_VALUE);
			put(FileType.EISIGISI_DEP_YPOPSIFIOU, Integer.MAX_VALUE);
			put(FileType.DIOIKITIKO_EGGRAFO, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private Position position;

	@ManyToOne
	private PositionPhase phase;

	@JsonView({DetailedFileHeaderView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public PositionPhase getPhase() {
		return phase;
	}

	public void setPhase(PositionPhase phase) {
		this.phase = phase;
	}

}
