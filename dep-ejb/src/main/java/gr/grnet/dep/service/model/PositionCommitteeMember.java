package gr.grnet.dep.service.model;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.PositionCommittee.DetailedPositionCommitteeView;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@XmlRootElement
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = {"committee_id", "registerMember_id"})
})
public class PositionCommitteeMember implements Serializable {

	private static final long serialVersionUID = -1339853623265264893L;

	public static interface PositionCommitteeMemberView {
	}

	public static interface DetailedPositionCommitteeMemberView extends PositionCommitteeMemberView {
	}

	public static final int MAX_MEMBERS = 7;

	public static final int MIN_EXTERNAL = 3;

	public static final int MIN_INTERNAL = 1;

	public enum MemberType {
		REGULAR, SUBSTITUTE
	}

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@Enumerated(EnumType.STRING)
	private MemberType type;

	@ManyToOne
	private PositionCommittee committee;

	@ManyToOne
	private RegisterMember registerMember;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({PositionCommitteeMemberView.class})
	public PositionCommittee getCommittee() {
		return committee;
	}

	public void setCommittee(PositionCommittee committee) {
		this.committee = committee;
	}

	@JsonView({DetailedPositionCommitteeView.class, DetailedPositionCommitteeMemberView.class})
	public RegisterMember getRegisterMember() {
		return registerMember;
	}

	public void setRegisterMember(RegisterMember registerMember) {
		this.registerMember = registerMember;
	}

	public MemberType getType() {
		return type;
	}

	public void setType(MemberType type) {
		this.type = type;
	}

}
