package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionCandidacies.DetailedPositionCandidaciesView;
import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.util.SimpleDateSerializer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;
import org.hibernate.annotations.FilterDef;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

@Entity
@XmlRootElement
@FilterDef(name = "filterPermanent", defaultCondition = "permanent = 'true'")
public class Candidacy {

	public static interface CandidacyView {
	}

	public static interface MediumCandidacyView extends CandidacyView {
	}

	public static interface DetailedCandidacyView extends MediumCandidacyView {
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private boolean permanent;

	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	boolean openToOtherCandidates = false;

	@ManyToOne
	private Candidate candidate;

	@ManyToOne
	private PositionCandidacies candidacies;

	@OneToMany(mappedBy = "candidacy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<CandidacyFile> files = new HashSet<CandidacyFile>();

	@OneToMany(mappedBy = "candidacy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<CandidacyEvaluator> proposedEvaluators = new HashSet<CandidacyEvaluator>();

	@Transient
	Boolean allowedToSee;

	@Transient
	Boolean canAddEvaluators;

	@Transient
	Boolean nominationCommitteeConverged;

	public static class CandidacySnapshot {

		private String username;

		@Transient
		private Map<String, String> firstname = new HashMap<String, String>();

		@Transient
		private Map<String, String> lastname = new HashMap<String, String>();

		@Transient
		private Map<String, String> fathername = new HashMap<String, String>();

		@Embedded
		private BasicInformation basicInfo = new BasicInformation();

		@Embedded
		private BasicInformation basicInfoLatin = new BasicInformation();

		@Embedded
		@AttributeOverrides({
				@AttributeOverride(name = "email", column = @Column(unique = false))
		})
		private ContactInformation contactInfo = new ContactInformation();

		@ManyToMany
		@JoinTable(inverseJoinColumns = {@JoinColumn(name = "files_id")})
		private Set<FileBody> files = new HashSet<FileBody>();

		@ManyToOne
		private Department department;

		@ManyToOne
		private Rank rank;

		@ManyToOne
		private Subject subject;

		private String fek;

		@ManyToOne
		private Subject fekSubject;

		// For ProfessorForeign
		private String institutionString;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public BasicInformation getBasicInfo() {
			return basicInfo;
		}

		public void setBasicInfo(BasicInformation basicInfo) {
			this.basicInfo = basicInfo;
		}

		public BasicInformation getBasicInfoLatin() {
			return basicInfoLatin;
		}

		public void setBasicInfoLatin(BasicInformation basicInfoLatin) {
			this.basicInfoLatin = basicInfoLatin;
		}

		@JsonView({DetailedCandidacyView.class})
		public ContactInformation getContactInfo() {
			return contactInfo;
		}

		public void setContactInfo(ContactInformation contactInfo) {
			this.contactInfo = contactInfo;
		}

		public Department getDepartment() {
			return department;
		}

		public void setDepartment(Department department) {
			this.department = department;
		}

		public Rank getRank() {
			return rank;
		}

		public void setRank(Rank rank) {
			this.rank = rank;
		}

		public Subject getSubject() {
			return subject;
		}

		public void setSubject(Subject subject) {
			this.subject = subject;
		}

		public String getFek() {
			return fek;
		}

		public void setFek(String fek) {
			this.fek = fek;
		}

		public Subject getFekSubject() {
			return fekSubject;
		}

		public void setFekSubject(Subject fekSubject) {
			this.fekSubject = fekSubject;
		}

		public String getInstitutionString() {
			return institutionString;
		}

		public void setInstitutionString(String institutionString) {
			this.institutionString = institutionString;
		}

		@XmlTransient
		@JsonIgnore
		public Set<FileBody> getFiles() {
			return files;
		}

		public void setFiles(Set<FileBody> files) {
			this.files = files;
		}

		public void clearFiles() {
			getFiles().clear();
		}

		public void addFile(FileBody body) {
			getFiles().add(body);
		}

		/////////////////////////////////

		public Map<String, String> getFirstname() {
			return firstname;
		}

		public Map<String, String> getLastname() {
			return lastname;
		}

		public Map<String, String> getFathername() {
			return fathername;
		}

		@XmlTransient
		@JsonIgnore
		public String getFirstname(String locale) {
			return firstname.get(locale);
		}

		@XmlTransient
		@JsonIgnore
		public String getLastname(String locale) {
			return lastname.get(locale);

		}

		@XmlTransient
		@JsonIgnore
		public String getFathername(String locale) {
			return fathername.get(locale);
		}

	}

	@Embedded
	private CandidacySnapshot snapshot;

	@PostPersist
	@PostUpdate
	@PostLoad
	private void refreshLocalizedNames() {
		snapshot.firstname.put("el", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getFirstname() : null);
		if (this.snapshot.basicInfoLatin != null &&
				this.snapshot.basicInfoLatin.getFirstname() != null &&
				!this.snapshot.basicInfoLatin.getFirstname().isEmpty()) {
			snapshot.firstname.put("en", this.snapshot.basicInfoLatin != null ? this.snapshot.basicInfoLatin.getFirstname() : null);
		} else {
			snapshot.firstname.put("en", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getFirstname() : null);
		}
		snapshot.lastname.put("el", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getLastname() : null);
		if (this.snapshot.basicInfoLatin != null &&
				this.snapshot.basicInfoLatin.getLastname() != null &&
				!this.snapshot.basicInfoLatin.getLastname().isEmpty()) {
			snapshot.lastname.put("en", this.snapshot.basicInfoLatin != null ? this.snapshot.basicInfoLatin.getLastname() : null);
		} else {
			snapshot.lastname.put("en", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getLastname() : null);
		}
		snapshot.fathername.put("el", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getFathername() : null);
		if (this.snapshot.basicInfoLatin != null &&
				this.snapshot.basicInfoLatin.getFathername() != null &&
				!this.snapshot.basicInfoLatin.getFathername().isEmpty()) {
			snapshot.fathername.put("en", this.snapshot.basicInfoLatin != null ? this.snapshot.basicInfoLatin.getFathername() : null);
		} else {
			snapshot.fathername.put("en", this.snapshot.basicInfo != null ? this.snapshot.basicInfo.getFathername() : null);
		}
	}

	public Long getId() {
		return id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isOpenToOtherCandidates() {
		return openToOtherCandidates;
	}

	public void setOpenToOtherCandidates(boolean openToOtherCandidates) {
		this.openToOtherCandidates = openToOtherCandidates;
	}

	@JsonView({MediumCandidacyView.class})
	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	@JsonView({MediumCandidacyView.class})
	public PositionCandidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(PositionCandidacies candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView({DetailedPositionCandidaciesView.class, MediumCandidacyView.class})
	public Set<CandidacyEvaluator> getProposedEvaluators() {
		return proposedEvaluators;
	}

	public void setProposedEvaluators(Set<CandidacyEvaluator> proposedEvaluators) {
		this.proposedEvaluators = proposedEvaluators;
	}

	public void addProposedEvaluator(CandidacyEvaluator evaluator) {
		this.proposedEvaluators.add(evaluator);
		evaluator.setCandidacy(this);
	}

	public CandidacySnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(CandidacySnapshot snapshot) {
		this.snapshot = snapshot;
	}

	@XmlTransient
	@JsonIgnore
	public Set<CandidacyFile> getFiles() {
		return files;
	}

	public void setFiles(Set<CandidacyFile> files) {
		this.files = files;
	}

	public void addFile(CandidacyFile file) {
		this.files.add(file);
		file.setCandidacy(this);
	}

	// Transient

	public Boolean getAllowedToSee() {
		return allowedToSee;
	}

	public void setAllowedToSee(Boolean allowedToSee) {
		this.allowedToSee = allowedToSee;
	}

	public Boolean getCanAddEvaluators() {
		return canAddEvaluators;
	}

	public void setCanAddEvaluators(Boolean canAddEvaluators) {
		this.canAddEvaluators = canAddEvaluators;
	}

	public Boolean getNominationCommitteeConverged() {
		return nominationCommitteeConverged;
	}

	public void setNominationCommitteeConverged(Boolean nominationCommitteeConverged) {
		this.nominationCommitteeConverged = nominationCommitteeConverged;
	}

	///////////////////////////////////////////////////////////////

	@JsonView({DetailedCandidacyView.class})
	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCandidacyEvalutionsDueDate() {
		return this.getCandidacies().getPosition().getPhase().getCommittee() != null ?
				this.getCandidacies().getPosition().getPhase().getCommittee().getCandidacyEvalutionsDueDate() : null;
	}

	@XmlTransient
	@JsonIgnore
	public Set<CandidateFile> getSnapshotFiles() {
		Set<CandidateFile> result = new HashSet<CandidateFile>();
		for (FileBody body : snapshot.getFiles()) {
			CandidateFile cf = new CandidateFile();
			cf.setDeleted(body.getHeader().isDeleted());
			cf.setDescription(body.getHeader().getDescription());
			cf.setId(body.getHeader().getId());
			cf.setName(body.getHeader().getName());
			cf.setOwner(body.getHeader().getOwner());
			cf.setType(body.getHeader().getType());

			cf.setCurrentBody(body);
			result.add(cf);
		}
		return result;
	}

	public void clearSnapshot() {
		if (snapshot != null) {
			snapshot.clearFiles();
		}
		snapshot = new CandidacySnapshot();
	}

	public void updateSnapshot(Candidate candidate) {
		User user = candidate.getUser();
		snapshot.setUsername(user.getUsername());
		snapshot.setBasicInfo(user.getBasicInfo());
		snapshot.setBasicInfoLatin(user.getBasicInfoLatin());
		snapshot.setContactInfo(user.getContactInfo());
		for (CandidateFile cf : FileHeader.filterDeleted(candidate.getFiles())) {
			snapshot.addFile(cf.getCurrentBody());
		}
	}

	public void updateSnapshot(ProfessorDomestic professor) {
		snapshot.setDepartment(professor.getDepartment());
		snapshot.setRank(professor.getRank());
		snapshot.setSubject(professor.getSubject());
		snapshot.setFek(professor.getFek());
		snapshot.setFekSubject(professor.getFekSubject());
	}

	public void updateSnapshot(ProfessorForeign professor) {
		snapshot.setInstitutionString(professor.getInstitution());
		snapshot.setRank(professor.getRank());
		snapshot.setSubject(professor.getSubject());
	}
}
