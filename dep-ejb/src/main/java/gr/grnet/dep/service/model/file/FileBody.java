package gr.grnet.dep.service.model.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.service.model.Position.DetailedPositionView;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.Role.DetailedRoleView;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

/**
 * The mutable part of the structure of a file.
 */
@Entity
@XmlRootElement
public final class FileBody implements Serializable {

	private static final long serialVersionUID = -3464243460747321945L;

	// define 2 json views
	public static interface SimpleFileBodyView {
	}

	// shows a summary view of a FileHeader/FileBody
	public static interface DetailedFileBodyView extends SimpleFileBodyView {
	}

	/**
	 * The persistence ID of the object.
	 */
	@Id
	@GeneratedValue
	private Long id;

	/**
	 * Version field for optimistic locking. Renamed to avoid conflict with file
	 * body version.
	 */
	@Version
	private int dbVersion;

	/**
	 * The version of the file, not the JPA version field!
	 */
	private int version;

	/**
	 * The header of the file.
	 */
	@ManyToOne
	private FileHeader header;

	/**
	 * The MIME type of this file.
	 */
	private String mimeType;

	/**
	 * The original filename (with which file was uploaded)
	 */
	private String originalFilename;

	/**
	 * The full file path (path+filename) under which file is currently stored
	 * in local file system
	 */
	private String storedFilePath;

	/**
	 * The file size in bytes
	 */
	private long fileSize;

	/**
	 * The date of this version.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonView({SimpleFileHeaderView.class, SimpleFileBodyView.class, DetailedRoleView.class, DetailedRegisterView.class, DetailedPositionView.class, DetailedPositionCommitteeMemberView.class})
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@JsonView({DetailedFileBodyView.class})
	public FileHeader getHeader() {
		return header;
	}

	public void setHeader(FileHeader header) {
		this.header = header;
	}

	@JsonView({SimpleFileHeaderView.class, SimpleFileBodyView.class, DetailedRoleView.class, DetailedRegisterView.class, DetailedPositionView.class, DetailedPositionCommitteeMemberView.class})
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@JsonView({SimpleFileHeaderView.class, SimpleFileBodyView.class, DetailedRoleView.class, DetailedRegisterView.class, DetailedPositionView.class, DetailedPositionCommitteeMemberView.class})
	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	@XmlTransient
	@JsonIgnore
	public String getStoredFilePath() {
		return storedFilePath;
	}

	public void setStoredFilePath(String storedFilePath) {
		this.storedFilePath = storedFilePath;
	}

	@JsonView({SimpleFileHeaderView.class, SimpleFileBodyView.class, DetailedRoleView.class, DetailedRegisterView.class, DetailedPositionView.class, DetailedPositionCommitteeMemberView.class})
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@JsonView({SimpleFileHeaderView.class, SimpleFileBodyView.class, DetailedRoleView.class, DetailedRegisterView.class, DetailedPositionView.class, DetailedPositionCommitteeMemberView.class})
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
