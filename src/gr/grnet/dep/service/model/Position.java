package gr.grnet.dep.service.model;

import gr.grnet.dep.service.util.SimpleDateDeserializer;
import gr.grnet.dep.service.util.SimpleDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonView;

@Entity
@XmlRootElement
public class Position {

	public static interface PublicPositionView {
	};

	public static interface PositionView extends PublicPositionView {
	};

	public static interface MemberPositionView extends PositionView {
	};

	public static interface CandidatePositionView extends PositionView {
	};

	public static interface DetailedPositionView extends PositionView {
	};

	public enum PositionStatus {
		ENTAGMENI,
		ANOIXTI,
		EPILOGI,
		STELEXOMENI,
		ANAPOMPI,
		CANCELLED
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

	@ManyToOne
	private Sector sector;

	private String fek;

	@Temporal(TemporalType.DATE)
	private Date fekSentDate;

	@ManyToOne(cascade = CascadeType.ALL)
	private PositionPhase phase;

	@OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("order ASC")
	private List<PositionPhase> phases = new ArrayList<PositionPhase>();

	@ManyToOne
	private User createdBy;

	@Transient
	private Boolean canSubmitCandidacy = Boolean.TRUE;

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

	public Sector getSector() {
		return sector;
	}

	public void setSector(Sector sector) {
		this.sector = sector;
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

	@JsonView({PublicPositionView.class})
	public Boolean getCanSubmitCandidacy() {
		return canSubmitCandidacy;
	}

	public void setCanSubmitCandidacy(Boolean canSubmitCandidacy) {
		this.canSubmitCandidacy = canSubmitCandidacy;
	}

	public PositionPhase getPhase() {
		return phase;
	}

	public void setPhase(PositionPhase phase) {
		this.phase = phase;
	}

	@XmlTransient
	@JsonIgnore
	public List<PositionPhase> getPhases() {
		return phases;
	}

	public void setPhases(List<PositionPhase> phases) {
		this.phases = phases;
	}

	@JsonView({PositionView.class})
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	/////////////////////////////////////////////////////
	@JsonView({PositionView.class})
	public Map<Integer, PositionStatus> getPhasesMap() {
		Map<Integer, PositionStatus> phasesMap = new TreeMap<Integer, PositionStatus>();
		for (PositionPhase phase : this.phases) {
			phasesMap.put(phase.getOrder(), phase.getStatus());
		}
		return phasesMap;
	}

	public void addPhase(PositionPhase phase) {
		phase.setPosition(this);
		phase.setOrder(this.getPhases().size());
		phase.setCreatedAt(new Date());
		phase.setUpdatedAt(new Date());

		this.phases.add(phase);
		this.phase = phase;
	}

	public Position as(Integer order) {
		Position position = new Position();
		// Copy basic fields
		position.setDepartment(this.department);
		position.setDescription(this.description);
		position.setFek(this.fek);
		position.setFekSentDate(this.fekSentDate);
		position.setId(this.id);
		position.setName(this.name);
		position.setPermanent(this.permanent);
		position.setSubject(this.subject);
		// Add selected phase
		for (PositionPhase phase : this.phases) {
			if (phase.getOrder().equals(order)) {
				position.setPhase(phase);
				break;
			}
		}
		position.setPhases(this.phases);

		return position;
	}

	public void copyFrom(Position position) {
		this.name = position.getName();
		this.description = position.getDescription();
		this.fek = position.getFek();
		this.fekSentDate = position.getFekSentDate();
		if (this.phase.getCandidacies() != null) {
			this.phase.getCandidacies().copyFrom(position.getPhase().getCandidacies());
		}
	}

}
