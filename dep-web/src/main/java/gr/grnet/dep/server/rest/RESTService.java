package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.AuthenticationService;
import gr.grnet.dep.service.JiraService;
import gr.grnet.dep.service.MailService;
import gr.grnet.dep.service.ReportService;
import gr.grnet.dep.service.UtilityService;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorForeign;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;
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
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes({ MediaType.APPLICATION_JSON })
public class RESTService {

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

	/**
	 * ***************************
	 * File Functions *************
	 * ****************************
	 */

	protected Response sendFileBody(FileBody fb) {
		try {
			final String fullPath = WebConstants.FILES_PATH + File.separator + fb.getStoredFilePath();

			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream outputStream) throws IOException {
					final FileInputStream fileInputStream = new FileInputStream(new File(fullPath));
					try {
						// Buffer size set to 10MB
						int bufferSize = 1024 * 1024 * 10;
						int buffer = 1;
						int rs = fileInputStream.read();

						// Stream File
						while (rs != -1) {
							outputStream.write(rs);
							rs = fileInputStream.read();
							buffer++;
							// Flush the output stream every 10MB
							if (buffer == bufferSize) {
								buffer = 1;
								outputStream.flush();
							}
						}
						outputStream.flush();
					} finally {
						// Close handlers:
						fileInputStream.close();
					}
				}
			};

			return Response.ok(stream)
					.type(MediaType.APPLICATION_OCTET_STREAM)
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fb.getOriginalFilename(), "UTF-8") + "\"")
					.build();
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


	public List<FileItem> readMultipartFormData(HttpServletRequest request) {
		try {
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
			servletFileUpload.setFileSizeMax(50 * 1024 * 1024); // 54.428.800
			servletFileUpload.setSizeMax(50 * 1024 * 1024);
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
			String result;
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
