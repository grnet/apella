package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.InstitutionFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.codehaus.jackson.map.annotate.JsonView;

@Path("/institution")
@Stateless
public class InstitutionRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@SuppressWarnings("unchecked")
	public List<Institution> getAll() {
		return (List<Institution>) em.createQuery(
			"select distinct i from Institution i ")
			.getResultList();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	public Institution get(@PathParam("id") long id) {
		try {
			return (Institution) em.createQuery(
				"select i from Institution i " +
					"where i.id = :id")
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/
	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<InstitutionFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long institutionId) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		institution.getFiles().size();
		return institution.getFiles();
	}

	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long institutionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (InstitutionFile file : institution.getFiles()) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long institutionId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		for (InstitutionFile file : institution.getFiles()) {
			if (file.getId().equals(fileId)) {
				if (file.getId().equals(fileId)) {
					for (FileBody fb : file.getBodies()) {
						if (fb.getId().equals(bodyId)) {
							return sendFileBody(fb);
						}
					}
				}
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@POST
	@Path("/{id:[0-9][0-9]*}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long institutionId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type:
		FileType type = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
				break;
			}
		}
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		// Check number of file types
		Set<InstitutionFile> existingFiles = FileHeader.filter(institution.getFiles(), type);
		if (!InstitutionFile.fileTypes.containsKey(type) || existingFiles.size() >= InstitutionFile.fileTypes.get(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		// Create
		try {
			InstitutionFile institutionFile = new InstitutionFile();
			institutionFile.setInstitution(institution);
			institutionFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, institutionFile);
			institution.addFile(institutionFile);
			em.flush();

			return toJSON(institutionFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@POST
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long institutionId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Parse Request
		List<FileItem> fileItems = readMultipartFormData(request);
		// Find required type:
		FileType type = null;
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField() && fileItem.getFieldName().equals("type")) {
				type = FileType.valueOf(fileItem.getString("UTF-8"));
				break;
			}
		}
		// Extra Validation
		if (type == null) {
			throw new RestException(Status.BAD_REQUEST, "missing.file.type");
		}
		if (!InstitutionFile.fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		InstitutionFile institutionFile = null;
		for (InstitutionFile file : institution.getFiles()) {
			if (file.getId().equals(fileId)) {
				institutionFile = file;
				break;
			}
		}
		if (institutionFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}

		// Update
		try {
			saveFile(loggedOn, fileItems, institutionFile);
			em.flush();
			institutionFile.getBodies().size();
			return toJSON(institutionFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Deletes the last body of given file, if possible.
	 * 
	 * @param authToken
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{id:[0-9]+}/file/{fileId:([0-9]+)?}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long institutionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, institutionId);
		// Validate:
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.institution.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			InstitutionFile institutionFile = null;
			for (InstitutionFile file : institution.getFiles()) {
				if (file.getId().equals(fileId)) {
					institutionFile = file;
					break;
				}
			}
			if (institutionFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			File file = deleteFileBody(institutionFile);
			file.delete();
			if (institutionFile.getCurrentBody() == null) {
				// Remove from Position
				institution.getFiles().remove(institutionFile);
				return Response.noContent().build();
			} else {
				return Response.ok(institutionFile).build();
			}
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
