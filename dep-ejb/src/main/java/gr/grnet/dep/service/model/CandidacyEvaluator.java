package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.Candidacy.MediumCandidacyView;
import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.file.PositionCandidaciesFile;
import gr.grnet.dep.service.util.CompareUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
public class CandidacyEvaluator implements Serializable {

	private static final long serialVersionUID = -8436764731554690767L;

	public static final int MAX_MEMBERS = 2;

	public static interface CandidacyEvaluatorView {
	}

	public static interface DetailedCandidacyEvaluatorView extends CandidacyEvaluatorView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private RegisterMember registerMember;

	@ManyToOne
	private Candidacy candidacy;

	@OneToMany(mappedBy = "evaluator", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionCandidaciesFile> files = new HashSet<PositionCandidaciesFile>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({CandidacyEvaluatorView.class})
	public Candidacy getCandidacy() {
		return candidacy;
	}

	public void setCandidacy(Candidacy candidacy) {
		this.candidacy = candidacy;
	}

	@JsonView({MediumCandidacyView.class, CandidacyEvaluatorView.class, DetailedPositionCandidaciesView.class})
	public RegisterMember getRegisterMember() {
		return this.registerMember;
	}

	public void setRegisterMember(RegisterMember registerMember) {
		this.registerMember = registerMember;
	}

	//////////////////////////////////////

	public Set<PositionCandidaciesFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionCandidaciesFile> files) {
		this.files = files;
	}

	public boolean isMissingRequiredField() {
		return this.registerMember == null;
	}

	public static boolean compareCriticalFields(CandidacyEvaluator a, CandidacyEvaluator b) {
		return CompareUtil.equalsIgnoreNull(a.getRegisterMember().getId(), b.getRegisterMember().getId());
	}
}
