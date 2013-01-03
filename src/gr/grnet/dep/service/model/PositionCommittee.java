package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionCommitteeMember.ProfessorCommitteesView;
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
public class PositionCommittee {

	public static interface PositionCommitteeView {
	};

	public static interface DetailedPositionCommitteeView extends PositionCommitteeView {
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
	private List<PositionCommitteeMember> members = new ArrayList<PositionCommitteeMember>();

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

	@JsonView({DetailedPositionCommitteeView.class, DetailedPositionCommitteeMemberView.class, ProfessorCommitteesView.class})
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

	@JsonView({DetailedPositionCommitteeView.class})
	public List<PositionCommitteeMember> getMembers() {
		return members;
	}

	public void setMembers(List<PositionCommitteeMember> members) {
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

	public void copyFrom(PositionCommittee other) {
		this.setCommitteeMeetingDate(other.getCommitteeMeetingDate());
		this.setUpdatedAt(new Date());
	}

	public void initializeCollections() {
		this.files.size();
		this.members.size();
	}

	public boolean containsMember(User user) {
		for (PositionCommitteeMember member : this.members) {
			if (member.getRegisterMember().getProfessor().getUser().getId().equals(user.getId())) {
				return true;
			}
		}
		return false;
	}
}
