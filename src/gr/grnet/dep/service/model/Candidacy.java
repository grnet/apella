package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.CandidacyFile;
import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class Candidacy {

	public static interface DetailedCandidacyView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private boolean permanent;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	@ManyToOne
	private Candidate candidate;

	@ManyToOne
	private PositionCandidacies candidacies;

	@OneToMany(mappedBy = "candidacy", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CandidacyFile> files = new HashSet<CandidacyFile>();

	@OneToMany(mappedBy = "candidacy", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CandidacyEvaluator> proposedEvaluators = new HashSet<CandidacyEvaluator>();

	public static class CandidacySnapshot {

		private String username;

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

		// For ProfessorDomestic
		@ManyToOne
		private Institution institution;

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

		public ContactInformation getContactInfo() {
			return contactInfo;
		}

		public void setContactInfo(ContactInformation contactInfo) {
			this.contactInfo = contactInfo;
		}

		public Institution getInstitution() {
			return institution;
		}

		public void setInstitution(Institution institution) {
			this.institution = institution;
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

	}

	@Embedded
	private CandidacySnapshot snapshot;

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

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public PositionCandidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(PositionCandidacies candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView({DetailedCandidacyView.class})
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

	///////////////////////////////////////////////////////////////

	@XmlTransient
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
		for (CandidateFile cf : candidate.getFiles()) {
			snapshot.addFile(cf.getCurrentBody());
		}
	}

	public void updateSnapshot(ProfessorDomestic professor) {
		snapshot.setInstitution(professor.getInstitution());
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

	public void initializeCollections() {
		this.candidate.getFiles().size();
		this.snapshot.getFiles().size();
		this.proposedEvaluators.size();
	}

}
