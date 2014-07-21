package gr.grnet.dep.service.model.file;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.PositionEvaluator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("PositionEvaluatorFile")
@XmlRootElement
public class PositionEvaluatorFile extends FileHeader {

	private static final long serialVersionUID = -3407870200478760438L;

	@SuppressWarnings("serial")
	public static final Map<FileType, Integer> fileTypes = Collections.unmodifiableMap(new HashMap<FileType, Integer>() {

		{
			put(FileType.AKSIOLOGISI, Integer.MAX_VALUE);
		}
	});

	@ManyToOne
	private PositionEvaluator evaluator;

	@JsonView({DetailedFileHeaderView.class})
	public PositionEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(PositionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

}
