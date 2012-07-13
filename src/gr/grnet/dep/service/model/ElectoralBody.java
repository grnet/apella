package gr.grnet.dep.service.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.codehaus.jackson.map.annotate.JsonView;

@Entity
public class ElectoralBody {

	public static final int MAX_MEMBERS = 7;
	
	
	// define 3 json views
	public static interface IdElectoralBodyView {
	}; // shows only id view of a ElectoralBody

	public static interface SimpleElectoralBodyView extends IdElectoralBodyView {
	}; // shows a summary view of a ElectoralBody

	public static interface DetailedElectoralBodyView extends SimpleElectoralBodyView {
	};
	
	
	@Id
	@GeneratedValue
	private Long id;

	@Version
	private int version;

	@ManyToOne(optional = false)
	private Position position;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "electoralBody", orphanRemoval = true)
	private Set<ElectoralBodyMembership> members = new HashSet<ElectoralBodyMembership>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView(SimpleElectoralBodyView.class)
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@JsonView(SimpleElectoralBodyView.class)
	public Set<ElectoralBodyMembership> getMembers() {
		return members;
	}

	public void setMembers(Set<ElectoralBodyMembership> members) {
		this.members = members;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////

	
	public void addMember(Professor professor) {
		ElectoralBodyMembership ebm = new ElectoralBodyMembership(this, professor);
		members.add(ebm);
	}
	
	public ElectoralBodyMembership removeMember(Professor professor) {
		ElectoralBodyMembership removed = null;
		ElectoralBodyMembership membership = new ElectoralBodyMembership(this, professor);
		Iterator<ElectoralBodyMembership> it = getMembers().iterator();
		while (it.hasNext()) {
			ElectoralBodyMembership ebm = it.next();
			if (ebm.equals(membership)) {
				it.remove();
				removed = ebm;
			}
		}
		return removed;
	}

}
