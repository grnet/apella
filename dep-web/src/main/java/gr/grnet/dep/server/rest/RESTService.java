package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.*;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.net.URLEncoder;
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
						// Buffer size set to 1MB
						int bufferSize = 1024 * 1024 * 1;
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
			throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "persistence.exception");
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
	 * ***************************
	 * JSON Utility Functions *****
	 * ****************************
	 */

	public String toJSON(Object object, Class<?> view) {
		try {
			ContextResolver<ObjectMapper> resolver = providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
			logger.fine(resolver.getClass().toString());
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
