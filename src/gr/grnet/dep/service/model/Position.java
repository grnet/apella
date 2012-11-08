package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Entity
public class Position {

	public static interface PublicPositionView {
	};

	public static interface DetailedPositionView extends PublicPositionView {
	};

	public enum PositionStatus {
		ENTAGMENI,
		ANOIXTI,
		EPILOGI,
		STELEXOMENI,
		ANAPOMPI
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	private boolean permanent;

	private String name;

	@Column(columnDefinition = " character varying(4000)")
	private String description;

	@ManyToOne(optional = false)
	private Department department;

	@ManyToOne(cascade = CascadeType.ALL)
	private Subject subject;

	private String fek;

	@Temporal(TemporalType.DATE)
	private Date fekSentDate;

	private PositionPhase phase;

	@OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PositionPhase> phases = new ArrayList<PositionPhase>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
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

	@JsonSerialize(using = SimpleDateSerializer.class)
	public Date getFekSentDate() {
		return fekSentDate;
	}

	@JsonDeserialize(using = SimpleDateDeserializer.class)
	public void setFekSentDate(Date fekSentDate) {
		this.fekSentDate = fekSentDate;
	}

	public PositionPhase getPhase() {
		return phase;
	}

	public void setPhase(PositionPhase phase) {
		this.phase = phase;
	}

	public void copyFrom(Position position) {
		this.name = position.getName();
		this.description = position.getDescription();
		if (this.subject == null) {
			this.subject = position.getSubject();
		} else {
			this.subject.setName(position.getSubject().getName());
		}
		this.fek = position.getFek();
		this.fekSentDate = position.getFekSentDate();
		// TODO:
	}

	public void initializeCollections() {
		// TODO:
	}
}
