package gr.grnet.dep.service.model;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * The mutable part of the structure of a file on the GSS service.
 */
@Entity
public final class FileBody implements Serializable {
	
	@Inject
	@Transient
	private Logger logger;

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
	@SuppressWarnings("unused")
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
	 * 
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	public FileHeader getHeader() {
		return header;
	}
	public void setHeader(FileHeader header) {
		this.header = header;
	}

	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getStoredFilePath() {
		return storedFilePath;
	}
	public void setStoredFilePath(String storedFilePath) {
		this.storedFilePath = storedFilePath;
	}

	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	

}
