package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionEvaluation.DetailedPositionEvaluationView;
import gr.grnet.dep.service.model.file.PositionEvaluatorFile;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@XmlRootElement
public class PositionEvaluator implements Serializable {

	private static final long serialVersionUID = 8362685537946833737L;

	public static interface PositionEvaluatorView {
	};

	public static interface DetailedPositionEvaluatorView extends PositionEvaluatorView {
	};

	public static final int MAX_MEMBERS = 2;

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private Long position;

	@ManyToOne
	private PositionEvaluation evaluation;

	@ManyToOne
	private RegisterMember registerMember;

	@OneToMany(mappedBy = "evaluator", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionEvaluatorFile> files = new HashSet<PositionEvaluatorFile>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	@JsonView({PositionEvaluatorView.class})
	public PositionEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(PositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonView({DetailedPositionEvaluationView.class, PositionEvaluatorView.class})
	public RegisterMember getRegisterMember() {
		return registerMember;
	}

	public void setRegisterMember(RegisterMember registerMember) {
		this.registerMember = registerMember;
	}

	@XmlTransient
	public Set<PositionEvaluatorFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionEvaluatorFile> files) {
		this.files = files;
	}

	////////////////////////////////////////////

	public void addFile(PositionEvaluatorFile file) {
		file.setEvaluator(this);
		this.files.add(file);
	}

	public void initializeCollections() {
		this.registerMember.initializeCollections();
	}

}
