package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.RegisterFile;

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
import javax.ws.rs.PUT;
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

@Path("/register")
@Stateless
public class RegisterRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@JsonView({DetailedRegisterView.class})
	public Collection<Register> getAll(@HeaderParam(TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		@SuppressWarnings("unchecked")
		Collection<Register> registers = (Collection<Register>) em.createQuery(
			"select r from Register r " +
				"left join fetch r.files f ")
			.getResultList();
		return registers;
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRegisterView.class})
	public Register get(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		try {
			Register r = (Register) em.createQuery(
				"select r from Register r " +
					"left join fetch r.files f " +
					"where r.id=:id")
				.setParameter("id", id)
				.getSingleResult();
			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}

	}

	@POST
	@JsonView({DetailedRegisterView.class})
	public Register create(@HeaderParam(TOKEN_HEADER) String authToken, Register newRegister) {
		User loggedOn = getLoggedOn(authToken);
		Institution institution = em.find(Institution.class, newRegister.getInstitution().getId());
		// Validate
		if (institution == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.department.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(institution)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Update
		try {
			newRegister.setInstitution(institution);
			newRegister.setPermanent(false);
			em.persist(newRegister);
			em.flush();

			newRegister.initializeCollections();
			return newRegister;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRegisterView.class})
	public Register update(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id, Register register) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, id);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRegister.getInstitution().getId().equals(register.getInstitution().getId())) {
			throw new RestException(Status.CONFLICT, "register.institution.change");
		}

		try {
			// Update
			existingRegister = existingRegister.copyFrom(register);
			existingRegister.setPermanent(true);
			existingRegister = em.merge(existingRegister);
			em.flush();
			// Return Result
			existingRegister.initializeCollections();
			return existingRegister;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, id);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			// Delete:
			em.remove(register);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 ***********************/

	@GET
	@Path("/{id:[0-9][0-9]*}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<RegisterFile> getFiles(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		// Return Result
		register.getFiles().size();
		return register.getFiles();
	}

	@GET
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public FileHeader getFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		// Return Result
		for (RegisterFile file : register.getFiles()) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		// Return Result
		for (RegisterFile file : register.getFiles()) {
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
	public String createFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
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
		Set<RegisterFile> existingFiles = FileHeader.filter(register.getFiles(), type);
		if (!RegisterFile.fileTypes.containsKey(type) || existingFiles.size() >= RegisterFile.fileTypes.get(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		// Create
		try {
			RegisterFile registerFile = new RegisterFile();
			registerFile.setRegister(register);
			registerFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, registerFile);
			register.addFile(registerFile);
			em.flush();

			return toJSON(registerFile, SimpleFileHeaderView.class);
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
	public String updateFile(@QueryParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
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
		if (!RegisterFile.fileTypes.containsKey(type)) {
			throw new RestException(Status.CONFLICT, "wrong.file.type");
		}
		RegisterFile registerFile = null;
		for (RegisterFile file : register.getFiles()) {
			if (file.getId().equals(fileId)) {
				registerFile = file;
				break;
			}
		}
		if (registerFile == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.file.id");
		}

		// Update
		try {
			saveFile(loggedOn, fileItems, registerFile);
			em.flush();
			registerFile.getBodies().size();
			return toJSON(registerFile, SimpleFileHeaderView.class);
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
	public Response deleteFile(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") long registerId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		try {
			RegisterFile registerFile = null;
			for (RegisterFile file : register.getFiles()) {
				if (file.getId().equals(fileId)) {
					registerFile = file;
					break;
				}
			}
			if (registerFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			File file = deleteFileBody(registerFile);
			file.delete();
			if (registerFile.getCurrentBody() == null) {
				// Remove from Register
				register.getFiles().remove(registerFile);
				return Response.noContent().build();
			} else {
				return Response.ok(registerFile).build();
			}
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
