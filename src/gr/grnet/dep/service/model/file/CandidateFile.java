package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.Candidate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@DiscriminatorValue("CandidateFile")
@XmlRootElement
public class CandidateFile extends FileHeader {

	private static final long serialVersionUID = -1061757000349917774L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.BIOGRAFIKO, 1);
			put(FileType.TAYTOTHTA, 1);
			put(FileType.FORMA_SYMMETOXIS, 1);
			put(FileType.BEBAIWSH_STRATIOTIKIS_THITIAS, 1);
			put(FileType.PTYXIO, Integer.MAX_VALUE);
			put(FileType.DIMOSIEYSI, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private Candidate candidate;

	@JsonView({DetailedFileHeaderView.class})
	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

}
