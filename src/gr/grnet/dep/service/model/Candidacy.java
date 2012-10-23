package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.file.CandidateFile;
import gr.grnet.dep.service.model.file.FileBody;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class Candidacy {

	public static interface SimpleCandidacyView {
	};
	
	public static interface DetailedCandidacyView extends SimpleCandidacyView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	Date date;

	@ManyToOne
	private Candidate candidate;

	// Inverse to Position
	@Basic(optional = false)
	@Column(name = "position_id")
	private Long position;
	
	
	static class CandidacySnapshot {
		private String username;
		@Embedded
		private BasicInformation basicInfo = new BasicInformation();
		@Embedded
		private BasicInformation basicInfoLatin = new BasicInformation();
		@Embedded
		private ContactInformation contactInfo = new ContactInformation();
		@ManyToMany
		private Set<FileBody> files = new HashSet<FileBody>();
		
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonView({DetailedCandidacyView.class})
	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	@JsonView(DetailedCandidacyView.class)
	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}
	
	@JsonView(DetailedCandidacyView.class)
	public CandidacySnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(CandidacySnapshot snapshot) {
		this.snapshot = snapshot;
	}



	public void initializeSnapshot() {
		snapshot.getFiles().size();
	}
	
	public void clearSnapshot() {
		if (snapshot!=null)
			snapshot.clearFiles();
		snapshot = new CandidacySnapshot();
	}
	
	public void updateSnapshot(Candidate candidate) {
		User user = candidate.getUser();
		snapshot.setUsername(user.getUsername());
		snapshot.setBasicInfo(user.getBasicInfo());
		snapshot.setBasicInfoLatin(user.getBasicInfoLatin());
		snapshot.setContactInfo(user.getContactInfo());
		for (CandidateFile cf: candidate.getFiles()) {
			snapshot.addFile(cf.getCurrentBody());
		}
		
	}

	

}
