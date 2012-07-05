package gr.grnet.dep.service.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.map.annotate.JsonView;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class Position {
	
	// define 3 json views
	public static interface IdPositionView {
	}; // shows only id view of a Candidacy

	public static interface SimplePositionView extends IdPositionView {
	}; // shows a summary view of a Candidacy

	public static interface DetailedPositionView extends SimplePositionView {
	};
	
	

	@Id
	@GeneratedValue
	private Long id;

	@SuppressWarnings("unused")
	@Version
	private int version;

	@NotNull
	@NotEmpty
	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = " character varying(4000)")
	private String description;

	@ManyToOne(optional = false)
	private Department department;

	@ManyToOne(optional = false)
	private Subject subject;

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private PositionStatus status = PositionStatus.ANAMONI_EGKRISIS;

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private PositionStatus deanStatus = PositionStatus.ANAMONI_EGKRISIS;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "position")
	private Set<Candidacy> candidacies = new HashSet<Candidacy>();

	@ManyToOne
	private FileHeader prosklisiKosmitora;

	@ManyToOne
	private FileHeader recommendatoryReport;

	@ManyToOne
	private FileHeader recommendatoryReportSecond;

	@Temporal(TemporalType.DATE)
	private Date fekSentDate;

	private String fek;

	@ManyToOne
	private FileHeader fekFile;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView(SimplePositionView.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonView(SimplePositionView.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonView(SimplePositionView.class)
	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@JsonView(SimplePositionView.class)
	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	@JsonView(SimplePositionView.class)
	public PositionStatus getStatus() {
		return status;
	}

	public void setStatus(PositionStatus status) {
		this.status = status;
	}

	@XmlTransient
	public Set<Candidacy> getCandidacies() {
		return candidacies;
	}

	public void setCandidacies(Set<Candidacy> candidacies) {
		this.candidacies = candidacies;
	}

	@JsonView(SimplePositionView.class)
	public FileHeader getRecommendatoryReport() {
		return recommendatoryReport;
	}

	public void setRecommendatoryReport(FileHeader recommendatoryReport) {
		this.recommendatoryReport = recommendatoryReport;
	}

	@JsonView(SimplePositionView.class)
	public FileHeader getRecommendatoryReportSecond() {
		return recommendatoryReportSecond;
	}

	public void setRecommendatoryReportSecond(FileHeader recommendatoryReportSecond) {
		this.recommendatoryReportSecond = recommendatoryReportSecond;
	}

	@JsonView(SimplePositionView.class)
	public PositionStatus getDeanStatus() {
		return deanStatus;
	}

	public void setDeanStatus(PositionStatus deanStatus) {
		this.deanStatus = deanStatus;
	}

	@JsonView(SimplePositionView.class)
	public FileHeader getProsklisiKosmitora() {
		return prosklisiKosmitora;
	}

	public void setProsklisiKosmitora(FileHeader prosklisiKosmitora) {
		this.prosklisiKosmitora = prosklisiKosmitora;
	}

	@JsonView(SimplePositionView.class)
	public Date getFekSentDate() {
		return fekSentDate;
	}

	public void setFekSentDate(Date fekSentDate) {
		this.fekSentDate = fekSentDate;
	}

	@JsonView(SimplePositionView.class)
	public String getFek() {
		return fek;
	}

	public void setFek(String fek) {
		this.fek = fek;
	}

	@JsonView(SimplePositionView.class)
	public FileHeader getFekFile() {
		return fekFile;
	}

	public void setFekFile(FileHeader fekFile) {
		this.fekFile = fekFile;
	}

}
