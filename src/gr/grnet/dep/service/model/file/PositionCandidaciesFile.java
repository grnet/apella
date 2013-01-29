package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.PositionCandidacies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@DiscriminatorValue("PositionProposalFile")
@XmlRootElement
public class PositionCandidaciesFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.EISIGISI_DEP_YPOPSIFIOU, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private PositionCandidacies candidacies;

	@ManyToOne
	private CandidacyEvaluator evaluator;

	@JsonView({DetailedFileHeaderView.class})
	public PositionCandidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(PositionCandidacies candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView({SimpleFileHeaderView.class})
	public CandidacyEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(CandidacyEvaluator evaluator) {
		this.evaluator = evaluator;
	}

}
