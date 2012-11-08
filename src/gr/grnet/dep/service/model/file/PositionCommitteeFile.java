package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Committee;

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
public class PositionCommitteeFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.APOFASI_SYSTASIS_EPITROPIS, 1);
			put(FileType.PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES, 1);
			put(FileType.TEKMIRIOSI_EPITROPIS_GIA_AKSIOLOGITES, 1);
			put(FileType.AITIMA_EPITROPIS_PROS_AKSIOLOGITES, 1);
		}
	});

	@ManyToOne
	private Committee committee;

	@JsonView({DetailedFileHeaderView.class})
	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

}
