package gr.grnet.dep.service.model.file;

import gr.grnet.dep.service.model.User;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
public class FileHeader implements Serializable {

	private static final long serialVersionUID = -4813579007397289759L;

	public static interface SimpleFileHeaderView {
	}

	;

	public static interface DetailedFileHeaderView extends SimpleFileHeaderView {
	}

	;

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking.
	 */
	@SuppressWarnings("unused")
	@Version
	private int version;

	@Enumerated(EnumType.STRING)
	private FileType type;

	/**
	 * An optional description. (E.g. "My CV")
	 */
	@Column(columnDefinition = " character varying(4000)")
	private String description;

	/**
	 * The file name.
	 */
	@Column(name = "name")
	private String name;

	/**
	 * Is this file temporarily deleted?
	 * The columnDefinition is postgres specific, if deployment database is
	 * changed this shall be changed too
	 */
	@Column(columnDefinition = " boolean DEFAULT false")
	private boolean deleted = false;

	/**
	 * The bodies of this file. A List so we can keep order.
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "header")
	@OrderBy("version")
	private List<FileBody> bodies = new ArrayList<FileBody>();

	/**
	 * The current (most recent) body of this file.
	 */
	@ManyToOne
	private FileBody currentBody;

	/**
	 * The owner of this file.
	 */
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false)
	private User owner;

	public Long getId() {
		return id;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public void setId(Long id) {
		this.id = id;
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@JsonView({DetailedFileHeaderView.class})
	public List<FileBody> getBodies() {
		return bodies;
	}

	public void setBodies(List<FileBody> bodies) {
		this.bodies = bodies;
	}

	public FileBody getCurrentBody() {
		return currentBody;
	}

	public void setCurrentBody(FileBody currentBody) {
		this.currentBody = currentBody;
	}

	@JsonView({DetailedFileHeaderView.class})
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	/**
	 * Add a body to list of bodies.
	 *
	 * @param body InfoItemBody The body to add.
	 */
	public void addBody(final FileBody body) {
		if (body == null) {
			throw new IllegalArgumentException("Can't add a null FileBody.");
		}
		// Remove from old header
		if (body.getHeader() != null) {
			throw new IllegalArgumentException("Trying to add a FileBody that already belongs to a FileHeader.");
		}
		// Set child in parent
		getBodies().add(body);
		// Set parent in child
		body.setHeader(this);

		// Update version number
		if (currentBody == null) {
			body.setVersion(1);
		} else {
			body.setVersion(currentBody.getVersion() + 1);
		}
		currentBody = body;
	}

	public static <T extends FileHeader> T filterOne(Set<T> files, FileType type) {
		Set<T> result = new HashSet<T>();
		for (T file : files) {
			if (!file.isDeleted() && file.getType().equals(type)) {
				result.add(file);
			}
		}
		return result.isEmpty() ? null : result.iterator().next();
	}

	public static <T extends FileHeader> T filterOneIncludingDeleted(Set<T> files, FileType type) {
		Set<T> result = new HashSet<T>();
		for (T file : files) {
			if (file.getType().equals(type)) {
				result.add(file);
			}
		}
		return result.isEmpty() ? null : result.iterator().next();
	}

	public static <T extends FileHeader> Set<T> filter(Set<T> files, FileType type) {
		Set<T> result = new HashSet<T>();
		for (T file : files) {
			if (!file.isDeleted() && file.getType().equals(type)) {
				result.add(file);
			}
		}
		return result;
	}

	public static <T extends FileHeader> Set<T> filterIncludingDeleted(Set<T> files, FileType type) {
		Set<T> result = new HashSet<T>();
		for (T file : files) {
			if (file.getType().equals(type)) {
				result.add(file);
			}
		}
		return result;
	}

	public static <T extends FileHeader> Set<T> filterDeleted(Set<T> files) {
		Set<T> result = new HashSet<T>();
		for (T file : files) {
			if (!file.isDeleted()) {
				result.add(file);
			}
		}
		return result;
	}

	public static <T extends FileHeader> Set<Long> ids(Set<T> files) {
		Set<Long> result = new HashSet<Long>();
		for (T file : files) {
			if (!file.isDeleted()) {
				result.add(file.getId());
			}
		}
		return result;
	}

	public void delete() {
		setDeleted(true);
	}

	public void undelete() {
		setDeleted(false);
	}

}
