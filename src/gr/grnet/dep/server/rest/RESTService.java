package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.*;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes({MediaType.APPLICATION_JSON})
public class RESTService {

	@Resource
	SessionContext sc;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	@Inject
	protected Logger logger;

	@Context
	protected Providers providers;

	@EJB
	AuthenticationService authenticationService;

	@EJB
	MailService mailService;

	@EJB
	JiraService jiraService;

	@EJB
	ReportService reportService;

	@EJB
	UtilityService utilityService;

	/**
	 * Check FileType to upload agrees with max number and direct caller.
	 *
	 * @param fileTypes Map<FileType, Integer>
	 * @param type      type to check
	 * @param files     existing files
	 * @return null to continue and create file; existing file to switch to
	 * update
	 * @throws RestException if wrong file type
	 */
	protected <T extends FileHeader> T checkNumberOfFileTypes(Map<FileType, Integer> fileTypes, FileType type, Set<T> files) throws RestException {
		if (!fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		Set<T> existingFiles = null;
		if (fileTypes.get(type) == 1) {
			existingFiles = FileHeader.filterIncludingDeleted(files, type);
			if (existingFiles.size() >= 1) {
				T existingFile = existingFiles.iterator().next();
				if (existingFile.isDeleted()) {
					// Reuse!
					existingFile.undelete();
					return existingFile;
				} else {
					throw new RestException(Status.CONFLICT, "wrong.file.type");
				}
			}
		} else {
			existingFiles = FileHeader.filter(files, type);
			if (existingFiles.size() >= fileTypes.get(type))
				throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		return null;
	}

	<T extends FileHeader> String _updateFile(User loggedOn, List<FileItem> fileItems, T file) throws IOException {
		try {
			saveFile(loggedOn, fileItems, file);
			em.flush();
			file.getBodies().size();
			return toJSON(file, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * ***************************
	 * Login Functions ************
	 * ****************************
	 */

	protected User getLoggedOn(String authToken) throws RestException {
		try {
			return authenticationService.getLoggedOn(authToken);
		} catch (ServiceException e) {
			throw new RestException(Status.UNAUTHORIZED, e.getMessage());
		}
	}

	/******************************
	 * Candidacy Snapshot *********
	 ******************************/

	/**
	 * Check that a (possible) Candidacy passes some basic checks before
	 * creation / update. Takes into account all roles
	 *
	 * @param candidacy
	 * @param candidate
	 */
	protected void validateCandidacy(Candidacy candidacy, Candidate candidate, boolean isNew) {
		if (!candidate.getStatus().equals(RoleStatus.ACTIVE)) {
			throw new RestException(Status.CONFLICT, "validation.candidacy.inactive.role");
		}
		if (isNew) {
			if (FileHeader.filter(candidate.getFiles(), FileType.DIMOSIEYSI).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.dimosieysi");
			}
			if (FileHeader.filter(candidate.getFiles(), FileType.BIOGRAFIKO).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.cv");
			}
			if (FileHeader.filter(candidate.getFiles(), FileType.PTYXIO).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.ptyxio");
			}
		} else {
			for (FileHeader fh : candidacy.getSnapshotFiles()) {
				logger.info(fh.getId() + " " + fh.getType() + " " + fh.isDeleted());
			}

			if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.DIMOSIEYSI).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.dimosieysi");
			}
			if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.BIOGRAFIKO).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.cv");
			}
			if (FileHeader.filterIncludingDeleted(candidacy.getSnapshotFiles(), FileType.PTYXIO).size() == 0) {
				throw new RestException(Status.CONFLICT, "validation.candidacy.no.ptyxio");
			}
		}
	}

	/**
	 * Hold on to a snapshot of candidate's details in given candidacy. Takes
	 * into account all roles
	 *
	 * @param candidacy
	 * @param candidate
	 */
	protected void updateSnapshot(Candidacy candidacy, Candidate candidate) {
		candidacy.clearSnapshot();
		candidacy.updateSnapshot(candidate);
		User user = candidate.getUser();
		ProfessorDomestic professorDomestic = (ProfessorDomestic) user.getActiveRole(RoleDiscriminator.PROFESSOR_DOMESTIC);
		ProfessorForeign professorForeign = (ProfessorForeign) user.getActiveRole(RoleDiscriminator.PROFESSOR_FOREIGN);
		if (professorDomestic != null) {
			candidacy.updateSnapshot(professorDomestic);
		} else if (professorForeign != null) {
			candidacy.updateSnapshot(professorForeign);
		}
	}

	protected void updateOpenCandidacies(Candidate candidate) {
		// Get Open Candidacies
		List<Candidacy> openCandidacies = em.createQuery(
				"from Candidacy c where c.candidate = :candidate " +
						"and c.candidacies.closingDate >= :now", Candidacy.class)
				.setParameter("candidate", candidate)
				.setParameter("now", new Date())
				.getResultList();

		// Validate all candidacies
		for (Candidacy candidacy : openCandidacies) {
			validateCandidacy(candidacy, candidate, true);
		}
		// If all valid, update
		for (Candidacy candidacy : openCandidacies) {
			updateSnapshot(candidacy, candidate);
		}
	}

	/**
	 * ***************************
	 * File Functions *************
	 * ****************************
	 */

	public File saveFile(User loggedOn, List<FileItem> fileItems, FileHeader header) throws IOException {
		try {
			File file = null;
			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField()) {
					logger.info("Incoming text data: '" + fileItem.getFieldName() + "'=" + fileItem.getString("UTF-8") + "\n");
					if (fileItem.getFieldName().equals("type")) {
						header.setType(FileType.valueOf(fileItem.getString("UTF-8")));
					} else if (fileItem.getFieldName().equals("name")) {
						header.setName(fileItem.getString("UTF-8"));
					} else if (fileItem.getFieldName().equals("description")) {
						header.setDescription(fileItem.getString("UTF-8"));
					}
				} else {
					FileBody body = new FileBody();
					header.addBody(body);
					em.persist(header);
					em.persist(body);
					em.flush(); // Get an id

					String filename = fileItem.getName();
					String newFilename = suggestFilename(body.getId(), "upl", filename);
					file = new File(WebConstants.FILES_PATH + newFilename);

					String mimeType = fileItem.getContentType();
					if (StringUtils.isEmpty(mimeType) || "application/octet-stream".equals(mimeType)
							|| "application/download".equals(mimeType) || "application/force-download".equals(mimeType)
							|| "octet/stream".equals(mimeType) || "application/unknown".equals(mimeType)) {
						body.setMimeType(identifyMimeType(filename));
					} else {
						body.setMimeType(mimeType);
					}
					body.setOriginalFilename(fileItem.getName());
					body.setStoredFilePath(newFilename);
					body.setFileSize(fileItem.getSize());
					body.setDate(new Date());

					fileItem.write(file);
				}
			}

			return file;
		} catch (FileUploadException ex) {
			logger.log(Level.SEVERE, "Error encountered while parsing the request", ex);
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error encountered while uploading file", e);
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
		}
	}

	/**
	 * Deletes the last body of given file, if possible.
	 * If no bodies are left, deletes header too.
	 *
	 * @param fh
	 * @return
	 * @throws RestException (Status.CONFLICT) if body cannot be deleted because
	 *                       it is in use
	 */
	protected <T extends FileHeader> File deleteFileBody(T fh) throws RestException {
		int size = fh.getBodies().size();
		// Delete the file itself, if possible.
		FileBody fb = fh.getCurrentBody();

		// Validate:
		try {
			em.createQuery("select c.id from Candidacy c " +
					"left join c.snapshot.files fb " +
					"where fb.id = :bodyId", Long.class)
					.setParameter("bodyId", fb.getId())
					.setMaxResults(1)
					.getSingleResult();
			logger.log(Level.INFO, "Could not delete FileBody id=" + fb.getId() + ". Constraint violation. ");
			throw new RestException(Status.CONFLICT, "file.in.use");
		} catch (NoResultException e) {
		}
		// Reference physical file
		String fullPath = WebConstants.FILES_PATH + File.separator + fb.getStoredFilePath();
		File file = new File(fullPath);
		// Delete
		fh.getBodies().remove(size - 1);
		fh.setCurrentBody(null);
		em.remove(fb);
		if (size > 1) {
			fh.setCurrentBody(fh.getBodies().get(size - 2));
		} else {
			em.remove(fh);
		}
		//Return physical file, should be deleted by caller of function
		return file;
	}

	/**
	 * Delete given FileHeader and all bodies and physical files.
	 *
	 * @param fh FileHeader
	 * @throws RestException (Status.CONFLICT) if FileHeader cannot be deleted
	 *                       because some body is in use
	 */
	protected void deleteCompletely(FileHeader fh) throws RestException {
		List<FileBody> fileBodies = fh.getBodies();
		for (int i = fileBodies.size() - 1; i >= 0; i--) {
			File file = deleteFileBody(fh);
			file.delete();
		}
	}

	/**
	 * Delete as many bodies and physical files of given FileHeader as possible.
	 * If no bodies are left, deletes header too.
	 * If a body that is in use is found, deletions stop there:
	 * The body and previous ones are left untouched, and
	 * FileHeader is just marked as deleted.
	 * No Exception is thrown.
	 *
	 * @param fh FileHeader
	 */
	protected <T extends FileHeader> T deleteAsMuchAsPossible(T fh) {
		List<FileBody> fileBodies = fh.getBodies();
		for (int i = fileBodies.size() - 1; i >= 0; i--) {
			try {
				File file = deleteFileBody(fh);
				file.delete();
			} catch (RestException e) {
				fh.delete();
				return fh;
			}
		}
		return null;
	}

	protected Response sendFileBody(FileBody fb) {
		try {
			String fullPath = WebConstants.FILES_PATH + File.separator + fb.getStoredFilePath();
			return Response.ok(new FileInputStream(new File(fullPath)))
					.type(MediaType.APPLICATION_OCTET_STREAM)
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fb.getOriginalFilename(), "UTF-8") + "\"")
					.build();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "sendFileBody", e);
			throw new EJBException(e);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "sendFileBody", e);
			throw new EJBException(e);
		}
	}

	protected void copyFile(String sourceFilename, String targetFilename) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			File sourceFile = new File(WebConstants.FILES_PATH + sourceFilename);
			File targetFile = new File(WebConstants.FILES_PATH + targetFilename);
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(targetFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Helper method for identifying mime type by examining the filename
	 * extension
	 *
	 * @param filename
	 * @return the mime type
	 */
	private String identifyMimeType(String filename) {
		if (filename.indexOf('.') != -1) {
			String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ENGLISH);
			if (".doc".equals(extension))
				return "application/msword";
			else if (".xls".equals(extension))
				return "application/vnd.ms-excel";
			else if (".ppt".equals(extension))
				return "application/vnd.ms-powerpoint";
			else if (".pdf".equals(extension))
				return "application/pdf";
			else if (".gif".equals(extension))
				return "image/gif";
			else if (".jpg".equals(extension) || ".jpeg".equals(extension) || ".jpe".equals(extension))
				return "image/jpeg";
			else if (".tiff".equals(extension) || ".tif".equals(extension))
				return "image/tiff";
			else if (".png".equals(extension))
				return "image/png";
			else if (".bmp".equals(extension))
				return "image/bmp";
		}
		// when all else fails assign the default mime type
		return WebConstants.DEFAULT_MIME_TYPE;
	}

	private String getSubdirForId(Long id) {
		long subdir = id % 100;
		return (subdir < 10) ? "0" + subdir : String.valueOf(subdir);
	}

	protected String suggestFilename(Long id, String prefix, String originalName) throws IOException {
		int dotPoint = originalName.lastIndexOf('.');
		String extension = "";
		if (dotPoint > -1) {
			extension = originalName.substring(dotPoint);
		}
		String subPath = getSubdirForId(id);
		// Create if it does not exist
		new File(WebConstants.FILES_PATH + File.separator + subPath).mkdirs();

		return subPath + File.separator + prefix + "-" + id + extension;
	}

	public List<FileItem> readMultipartFormData(HttpServletRequest request) {
		try {
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
			servletFileUpload.setFileSizeMax(30 * 1024 * 1024);
			servletFileUpload.setSizeMax(30 * 1024 * 1024);
			servletFileUpload.setHeaderEncoding("UTF-8");
			@SuppressWarnings("unchecked")
			List<FileItem> fileItems = servletFileUpload.parseRequest(request);
			return fileItems;
		} catch (FileUploadBase.SizeLimitExceededException e) {
			throw new RestException(Status.BAD_REQUEST, "file.size.exceeded");
		} catch (FileUploadException e) {
			logger.log(Level.SEVERE, "Error encountered while parsing the request", e);
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
		}
	}

	/**
	 * ***************************
	 * JSON Utility Functions *****
	 * ****************************
	 */

	public String toJSON(Object object, Class<?> view) {
		try {
			ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
			logger.info(resolver.getClass().toString());
			ObjectMapper mapper = resolver.getContext(object.getClass());
			String result = null;
			if (view == null) {
				result = mapper.writeValueAsString(object);
			} else {
				result = mapper.writerWithView(view).writeValueAsString(object);
			}

			return result;
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
		return null;
	}

	public <T> T fromJSON(Class<T> clazz, String json) {
		try {
			ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
			ObjectMapper mapper = resolver.getContext(clazz);
			T result = mapper.readValue(json, clazz);
			return result;
		} catch (JsonGenerationException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (JsonMappingException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
		}
		return null;
	}

}
