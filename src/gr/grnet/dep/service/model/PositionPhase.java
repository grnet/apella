package gr.grnet.dep.service.model;

import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.file.PositionFile;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class PositionPhase {

	@Id
	private Long id;

	@ManyToOne
	private Position position;

	private PositionStatus status;

	@OrderColumn
	private Integer order;

	@ManyToOne
	private Candidacies candidacies;

	@ManyToOne
	private Committee committee;

	@ManyToOne
	private Nomination nomination;

	@OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PositionFile> files = new HashSet<PositionFile>();

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

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public PositionStatus getStatus() {
		return status;
	}

	public void setStatus(PositionStatus status) {
		this.status = status;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Candidacies getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Candidacies candidacies) {
		this.candidacies = candidacies;
	}

	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Nomination getNomination() {
		return nomination;
	}

	public void setNomination(Nomination nomination) {
		this.nomination = nomination;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<PositionFile> getFiles() {
		return files;
	}

	public void setFiles(Set<PositionFile> files) {
		this.files = files;
	}

	public void addFile(PositionFile file) {
		file.setPhase(this);
		file.setPosition(this.position);
		this.files.add(file);
	}
}
