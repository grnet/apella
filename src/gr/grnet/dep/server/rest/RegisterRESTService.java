package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.RegisterMember;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response.Status;

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
				"where r.permanent = true")
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
		try {
			em.createQuery("from Register r " +
				"where r.institution.id = :institutionId " +
				"and r.permanent = true ")
				.setParameter("institutionId", institution.getId())
				.setMaxResults(1)
				.getSingleResult();
			throw new RestException(Status.CONFLICT, "register.already.exists");
		} catch (NoResultException e) {
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
			em.remove(register);
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/***********************
	 * Members Functions ***
	 ***********************/

	@GET
	@Path("/{id:[0-9]+}/members")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<RegisterMember> getRegisterMembers(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
		getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		for (RegisterMember member : register.getMembers()) {
			member.getProfessor().initializeCollections();
		}
		return register.getMembers();
	}

	@GET
	@Path("/{id:[0-9]+}/members/{memberId:[0-9]+}")
	@JsonView({DetailedRegisterMemberView.class})
	public RegisterMember getRegisterMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("memberId") Long memberId) {
		getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		for (RegisterMember member : register.getMembers()) {
			if (member.getId().equals(memberId)) {
				member.getProfessor().initializeCollections();
				return member;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
	}

	@POST
	@Path("/{id:[0-9]+}/members")
	@JsonView({DetailedRegisterMemberView.class})
	public RegisterMember createRegisterMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, RegisterMember newMember) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, registerId);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(existingRegister.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		Professor existingProfessor = em.find(Professor.class, newMember.getProfessor().getId());
		if (existingProfessor == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		if (!existingProfessor.getStatus().equals(RoleStatus.ACTIVE)) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.status");
		}
		// Check if already exists:
		for (RegisterMember member : existingRegister.getMembers()) {
			if (member.getProfessor().getId().equals(existingProfessor.getId())) {
				throw new RestException(Status.CONFLICT, "register.member.already.exists");
			}
		}
		// Update
		try {
			newMember.setRegister(existingRegister);
			newMember.setProfessor(existingProfessor);
			switch (existingProfessor.getDiscriminator()) {
				case PROFESSOR_DOMESTIC:
					newMember.setExternal(existingRegister.getInstitution().getId() != ((ProfessorDomestic) existingProfessor).getInstitution().getId());
					break;
				case PROFESSOR_FOREIGN:
					newMember.setExternal(true);
					break;
				default:
					break;
			}
			final RegisterMember savedMember = em.merge(newMember);
			existingRegister.addMember(savedMember);
			em.flush();

			// Send E-Mails
			if (savedMember.isExternal()) {
				// register.create.register.member.external@member
				sendEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
					"default.subject",
					"register.create.register.member.external@member",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", savedMember.getProfessor().getUser().getUsername());
							put("institution", savedMember.getRegister().getInstitution().getName());
						}
					}));
			} else {
				// register.create.register.member.internal@member
				sendEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
					"default.subject",
					"register.create.register.member.internal@member",
					Collections.unmodifiableMap(new HashMap<String, String>() {

						{
							put("username", savedMember.getProfessor().getUser().getUsername());
							put("institution", savedMember.getRegister().getInstitution().getName());
						}
					}));
			}
			// End: Send E-Mails

			// Return result
			savedMember.getProfessor().initializeCollections();
			return savedMember;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@DELETE
	@Path("/{id:[0-9]+}/members/{memberId:[0-9][0-9]*}")
	public void removeRegisterMember(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("memberId") Long memberId) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, registerId);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(existingRegister.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Check if it exists:
		RegisterMember existingMember = null;
		for (RegisterMember member : existingRegister.getMembers()) {
			if (member.getId().equals(memberId)) {
				existingMember = member;
				break;
			}
		}
		if (existingMember == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}

		// Send E-Mails
		final RegisterMember emailMember = existingMember;
		if (emailMember.isExternal()) {
			// register.remove.register.member.external@member
			sendEmail(emailMember.getProfessor().getUser().getContactInfo().getEmail(),
				"default.subject",
				"register.remove.register.member.external@member",
				Collections.unmodifiableMap(new HashMap<String, String>() {

					{
						put("username", emailMember.getProfessor().getUser().getUsername());
						put("institution", emailMember.getRegister().getInstitution().getName());
					}
				}));
		} else {
			// register.remove.register.member.internal@member
			sendEmail(emailMember.getProfessor().getUser().getContactInfo().getEmail(),
				"default.subject",
				"register.remove.register.member.internal@member",
				Collections.unmodifiableMap(new HashMap<String, String>() {

					{
						put("username", emailMember.getProfessor().getUser().getUsername());
						put("institution", emailMember.getRegister().getInstitution().getName());
					}
				}));
		}
		// End: Send E-Mails

		// Remove
		try {
			existingRegister.getMembers().remove(existingMember);
			em.remove(existingMember);
			em.flush();

		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	@GET
	@Path("/{id:[0-9]+}/professor")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<Role> getRegisterProfessors(@HeaderParam(TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, registerId);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(existingRegister.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);

		List<Role> professors = em.createQuery(
			"select r from Role r " +
				"where r.id is not null " +
				"and r.discriminator in (:discriminators) " +
				"and r.status = :status")
			.setParameter("discriminators", discriminatorList)
			.setParameter("status", RoleStatus.ACTIVE)
			.getResultList();

		// Execute
		for (Role r : professors) {
			Professor p = (Professor) r;
			r.initializeCollections();
		}
		return professors;
	}

}
