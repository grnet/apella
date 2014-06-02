package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Candidacy;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("CandidacyFile")
@XmlRootElement
public class CandidacyFile extends FileHeader {

	private static final long serialVersionUID = -1061757000349917774L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.EKTHESI_AUTOAKSIOLOGISIS, 1);
			put(FileType.SYMPLIROMATIKA_EGGRAFA, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private Candidacy candidacy;

	@JsonView({DetailedFileHeaderView.class})
	public Candidacy getCandidacy() {
		return candidacy;
	}

	public void setCandidacy(Candidacy candidacy) {
		this.candidacy = candidacy;
	}

}
