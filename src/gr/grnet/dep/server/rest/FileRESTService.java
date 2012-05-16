package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.FileBody;
import gr.grnet.dep.service.model.FileBody.DetailedFileBodyView;
import gr.grnet.dep.service.model.FileHeader;
import gr.grnet.dep.service.model.FileHeader.DetailedFileHeaderView;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/file")
@Stateless
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class FileRESTService extends RESTService {
	

	/**
	 * The default MIME type for files without an explicit one.
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@Inject
	private Logger logger;

	
	private static Logger staticLogger = Logger.getLogger(RESTService.class.getName());
	
	
	private static String savePath;
	
	
	
	
	static {
		try {
			savePath = DEPConfigurationFactory.getServerConfiguration().getString("files.path");
			new File(savePath).mkdirs();
		} catch (ConfigurationException e) {
			staticLogger.log(Level.SEVERE, "RESTService init: ", e);
		}
	}
	
	
	/**
	 * Helper method for identifying mime type by examining the filename extension
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
		if (dotPoint > -1)
			extension = originalName.substring(dotPoint);
		String subPath =  getSubdirForId(id) + File.separator;
		// Create if it does not exist
		new File(savePath + File.separator + subPath).mkdirs();

		
		return subPath + File.separator + prefix + "-" + id + extension;
	}
	
	
	public FileBody uploadFile(HttpServletRequest request, FileHeader header) throws FileUploadException, IOException 
	{
		FileBody body = null;
		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
		servletFileUpload.setSizeMax(30 * 1024 * 1024);
		servletFileUpload.setHeaderEncoding("UTF-8");
		@SuppressWarnings("unchecked")
		List<FileItem> fileItems = servletFileUpload.parseRequest(request);

		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField()) {
				logger.info("Incoming text data: '"+fileItem.getFieldName()+"'="+fileItem.getString("UTF-8")+"\n");
			} else {
				body = new FileBody();
				if (header==null) {
					header = new FileHeader();
					//header.setName(name);
					//header.setDescription(description);
				}
				header.addBody(body);
				em.persist(header);
				em.persist(body);
				em.flush(); // Get an id
				
				String filename = fileItem.getName();
				String newFilename = suggestFilename(body.getId(), "upl", filename);
				File file = new File(savePath + newFilename);
				StringBuilder sb = (new StringBuilder("Saving file. Original filename='")).append(filename)
							.append("', filename='").append(file.getCanonicalPath()).append("' ");
				
				String mimeType = fileItem.getContentType();
				if (StringUtils.isEmpty(mimeType) || "application/octet-stream".equals(mimeType)
							|| "application/download".equals(mimeType) || "application/force-download".equals(mimeType)
							|| "octet/stream".equals(mimeType) || "application/unknown".equals(mimeType))
					body.setMimeType(identifyMimeType(filename));
				else
					body.setMimeType(mimeType);
				body.setOriginalFilename(fileItem.getName());
				body.setStoredFilePath(newFilename);
				body.setFileSize(fileItem.getSize());
				body.setDate(new Date());
				
			    try {		    	
			        fileItem.write(file);
			        
			        
			        sb.append("OK.");
			        logger.info(sb.toString());
				} catch (FileUploadException ex) {
					sb.append("ERROR");
					logger.info(sb.toString());
					logger.log(Level.SEVERE, "Error encountered while parsing the request", ex);
					throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
				}
			    catch (Exception e) {
			    	sb.append("ERROR");
					logger.info(sb.toString());
			    	logger.log(Level.SEVERE, "Error encountered while uploading file", e);
				    throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			    }
			}
		}
		return body;
	}


	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedFileHeaderView.class})
	public FileHeader get(@PathParam("id") long id) {
		FileHeader fh = (FileHeader) em.createQuery(
			"from FileHeader fh left join fetch fh.bodies " +
			"where fh.id=:id")
			.setParameter("id", id)
			.getSingleResult();
//TODO: Security check		
		/*User loggedOn = getLoggedOn();
		if (!loggedOn.hasRole(RoleDiscriminator.ADMINISTRATOR)
					&& cy.getUser()!=loggedOn.getId())
			throw new WebApplicationException(Status.FORBIDDEN);*/
		
		return fh;
	}

	
	
	@POST
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({ DetailedFileBodyView.class })
	public FileBody createFile(@Context HttpServletRequest request) throws FileUploadException, IOException 
	{
		return uploadFile(request, null);
	}
	
	
	@POST
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("multipart/form-data")
	@Produces({MediaType.APPLICATION_JSON})
	@JsonView({ DetailedFileBodyView.class })
	public FileBody updateBody(@PathParam("id") Long headerId, @Context HttpServletRequest request) throws FileUploadException, IOException 
	{
		FileHeader fh = (FileHeader) em.find(FileHeader.class, headerId);
		return uploadFile(request, fh);
	}
	
	
}
