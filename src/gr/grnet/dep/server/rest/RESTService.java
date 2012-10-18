package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@Produces({MediaType.APPLICATION_JSON})
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

	/**
	 * The default MIME type for files without an explicit one.
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	protected static final String TOKEN_HEADER = "X-Auth-Token";

	protected static final String ERROR_CODE_HEADER = "X-Error-Code";

	private static Logger staticLogger = Logger.getLogger(RESTService.class.getName());

	static String savePath;

	static {
		try {
			savePath = DEPConfigurationFactory.getServerConfiguration().getString("files.path");
			new File(savePath).mkdirs();
		} catch (ConfigurationException e) {
			staticLogger.log(Level.SEVERE, "RESTService init: ", e);
		}
	}

	protected User getLoggedOn(String authToken) throws RestException {
		if (authToken == null) {
			throw new RestException(Status.UNAUTHORIZED, "login.missing.token");
		}
		try {
			User user = (User) em.createQuery(
				"from User u left join fetch u.roles " +
					"where u.status = :status " +
					"and u.authToken = :authToken")
				.setParameter("status", UserStatus.ACTIVE)
				.setParameter("authToken", authToken)
				.getSingleResult();
			return user;
		} catch (NoResultException e) {
			throw new RestException(Status.UNAUTHORIZED, "login.invalid.token");
		}
	}

	protected String readShibbolethField(HttpServletRequest request, String attributeName, String headerName) {
		try {
			Object value = request.getAttribute(attributeName);
			String field = null;
			if (value == null || value.toString().isEmpty()) {
				value = request.getHeader(headerName);
			}
			if (value != null && !value.toString().isEmpty()) {
				field = new String(value.toString().getBytes("ISO-8859-1"), "UTF-8");
				if (field.indexOf(";") != -1) {
					field = field.substring(0, field.indexOf(";"));
				}
			}
			return field;
		} catch (UnsupportedEncodingException e) {
			staticLogger.log(Level.SEVERE, "decodeAttribute: ", e);
			return null;
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
		return DEFAULT_MIME_TYPE;
	}

	private String getSubdirForId(Long id) {
		long subdir = id % 100;
		return (subdir < 10) ? "0" + subdir : String.valueOf(subdir);
	}

	private String suggestFilename(Long id, String prefix, String originalName) throws IOException {
		int dotPoint = originalName.lastIndexOf('.');
		String extension = "";
		if (dotPoint > -1) {
			extension = originalName.substring(dotPoint);
		}
		String subPath = getSubdirForId(id);
		// Create if it does not exist
		new File(savePath + File.separator + subPath).mkdirs();

		return subPath + File.separator + prefix + "-" + id + extension;
	}

	public List<FileItem> readMultipartFormData(HttpServletRequest request) {
		try {
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
			servletFileUpload.setSizeMax(30 * 1024 * 1024);
			servletFileUpload.setHeaderEncoding("UTF-8");
			@SuppressWarnings("unchecked")
			List<FileItem> fileItems = servletFileUpload.parseRequest(request);
			return fileItems;
		} catch (FileUploadException e) {
			logger.log(Level.SEVERE, "Error encountered while parsing the request", e);
			throw new RestException(Status.INTERNAL_SERVER_ERROR, "generic");
		}
	}

	public void saveFile(User loggedOn, List<FileItem> fileItems, FileHeader header) throws IOException {

		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.getId().equals(header.getOwner().getId())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
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
					File file = new File(savePath + newFilename);

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
	 * 
	 * @param fh
	 * @return
	 */
	public Response deleteFileBody(FileHeader fh) {
		int size = fh.getBodies().size();
		// Delete the file itself, if possible.
		FileBody fb = fh.getCurrentBody();
		logger.info("Attempting to delete FileBody id=" + fb.getId() + " of FileHeader id=" + fh.getId());
		String fullPath = savePath + File.separator + fb.getStoredFilePath();
		File file = new File(fullPath);

		fh.getBodies().remove(size - 1);
		fh.setCurrentBody(null);
		try {
			em.remove(fb);
			em.flush();
		} catch (PersistenceException e) {
			// Assume it's a constraint violation
			logger.info("Could not delete FileBody id=" + fb.getId() + ". Constraint violation.");
			return Response.status(Status.CONFLICT).header(ERROR_CODE_HEADER, "file.in.use").build();
		}
		if (size > 1) {
			fh.setCurrentBody(fh.getBodies().get(size - 2));
		} else {
			em.remove(fh);
		}
		boolean deleted = file.delete();
		if (!deleted) {
			logger.log(Level.WARNING, "Could not delete file '" + fullPath + "'.");
			// Do something? What?
		}
		if (size > 1) {
			logger.info("Deleted FileBody id=" + fb.getId());
			return Response.ok(fh).build();
		} else {
			logger.info("Deleted FileBody id=" + fb.getId() + " PLUS FileHeader id=" + fh.getId());
			return Response.noContent().build();
		}
	}

	public Response sendFileBody(FileBody fb) {
		try {
			String fullPath = savePath + File.separator + fb.getStoredFilePath();
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

	public String toJSON(Object object, Class<?> view) throws JsonMappingException {
		try {
			ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
			ObjectMapper mapper = resolver.getContext(object.getClass());
			if (view == null) {
				return mapper.writeValueAsString(object);
			} else {
				return mapper.writerWithView(view).writeValueAsString(object);
			}
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
