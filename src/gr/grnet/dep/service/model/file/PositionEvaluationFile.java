package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.PositionEvaluation;

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
public class PositionEvaluationFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.AKSIOLOGISI_PROTOU_AKSIOLOGITI, 1);
			put(FileType.AKSIOLOGISI_DEUTEROU_AKSIOLOGITI, 1);
		}
	});

	@ManyToOne
	private PositionEvaluation evaluation;

	@ManyToOne
	private CandidacyEvaluator candidacyEvaluator;

	@JsonView({DetailedFileHeaderView.class})
	public PositionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(PositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

}
