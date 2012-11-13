package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.CommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.CommitteeMember.ProfessorCommitteesView;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class Committee {

	public static interface DetailedCommitteeView {
	};

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne
	private Position position;

	@OneToMany(mappedBy = "committee", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionPhase> phases = new HashSet<PositionPhase>();

	@Temporal(TemporalType.DATE)
	private Date committeeMeetingDate; // Ημερομηνία Συνεδρίασης επιτροπής

	@OneToMany(mappedBy = "committee", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionCommitteeFile> files = new HashSet<PositionCommitteeFile>();

	@OneToMany(mappedBy = "committee")
	private List<CommitteeMember> members = new ArrayList<CommitteeMember>();

	@Temporal(TemporalType.DATE)
	private Date createdAt;

	@Temporal(TemporalType.DATE)
	private Date updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({DetailedCommitteeView.class, DetailedPositionCommitteeMemberView.class, ProfessorCommitteesView.class})
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@XmlTransient
	public Set<PositionPhase> getPhases() {
		return phases;
	}

	public void setPhases(Set<PositionPhase> phases) {
		this.phases = phases;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCommitteeMeetingDate() {
		return committeeMeetingDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCommitteeMeetingDate(Date committeeMeetingDate) {
		this.committeeMeetingDate = committeeMeetingDate;
	}

	@XmlTransient
	public Set<PositionCommitteeFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionCommitteeFile> files) {
		this.files = files;
	}

	public void addFile(PositionCommitteeFile file) {
		file.setCommittee(this);
		this.files.add(file);
	}

	@XmlTransient
	public List<CommitteeMember> getMembers() {
		return members;
	}

	public void setMembers(List<CommitteeMember> members) {
		this.members = members;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getCreatedAt() {
		return createdAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	//////////////////////////////////////////////

	public void copyFrom(Committee other) {
		this.setCommitteeMeetingDate(other.getCommitteeMeetingDate());
		this.setUpdatedAt(new Date());
	}

	public void initializeCollections() {
		this.files.size();
		this.members.size();
	}

	public boolean containsMember(User user) {
		for (CommitteeMember member : this.members) {
			if (member.getProfessor().getUser().getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}
}
