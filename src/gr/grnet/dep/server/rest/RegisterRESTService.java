package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.Professor;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Register;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.Register.RegisterView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
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

	/**
	 * Returns all registers
	 * 
	 * @param authToken
	 * @return
	 */
	@GET
	@JsonView({RegisterView.class})
	public Collection<Register> getAll(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		getLoggedOn(authToken);
		@SuppressWarnings("unchecked")
		Collection<Register> registers = (Collection<Register>) em.createQuery(
			"select r from Register r " +
				"where r.permanent = true")
			.getResultList();
		return registers;
	}

	/**
	 * Returns register with given ID
	 * 
	 * @param authToken
	 * @param id
	 * @return
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 */
	@GET
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRegisterView.class})
	public Register get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
		getLoggedOn(authToken);
		try {
			Register r = (Register) em.createQuery(
				"select r from Register r " +
					"where r.id=:id")
				.setParameter("id", id)
				.getSingleResult();

			addCanBeDeletedInfo(r);
			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}

	}

	private void addCanBeDeletedInfo(Register register) {
		// TODO: One query, avoid laizy initialization
		for (RegisterMember member : register.getMembers()) {
			boolean canBeDeleted = member.getCommittees().isEmpty() && member.getEvaluations().isEmpty() && member.getCandidacyEvaluations().isEmpty();
			member.setCanBeDeleted(canBeDeleted);
		}
	}

	/**
	 * Creates a new Register, non-finalized
	 * 
	 * @param authToken
	 * @param newRegister
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.department.id
	 * @HTTP 409 X-Error-Code: register.already.exists
	 */
	@POST
	@JsonView({DetailedRegisterView.class})
	public Register create(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, Register newRegister) {
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

			addCanBeDeletedInfo(newRegister);
			return newRegister;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Updates and finalizes the Register with the given ID
	 * 
	 * @param authToken
	 * @param id
	 * @param register
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 * @HTTP 404 X-Error-Code: wrong.professor.id
	 * @HTTP 409 X-Error-Code: register.institution.change
	 */
	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@JsonView({DetailedRegisterView.class})
	public Register update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id, Register register) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, id);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) && !loggedOn.isInstitutionUser(register.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRegister.getInstitution().getId().equals(register.getInstitution().getId())) {
			throw new RestException(Status.CONFLICT, "register.institution.change");
		}

		// Retrieve existing Members
		Map<Long, RegisterMember> existingMembersAsMap = new HashMap<Long, RegisterMember>();
		for (RegisterMember existingMember : existingRegister.getMembers()) {
			existingMembersAsMap.put(existingMember.getProfessor().getId(), existingMember);
		}
		// Retrieve new Member Professor Ids
		Set<Long> newMemberIds = new HashSet<Long>();
		for (RegisterMember newCommitteeMember : register.getMembers()) {
			newMemberIds.add(newCommitteeMember.getProfessor().getId());
		}
		List<Professor> newRegisterMembers = new ArrayList<Professor>();
		if (!newMemberIds.isEmpty()) {
			Query query = em.createQuery(
				"select distinct p from Professor p " +
					"where p.id in (:ids)")
				.setParameter("ids", newMemberIds);
			newRegisterMembers.addAll(query.getResultList());
		}
		if (newMemberIds.size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.professor.id");
		}
		// Calculate Added and Removed Professor IDs
		Set<Long> addedProfessorIds = new HashSet<Long>();
		addedProfessorIds.addAll(newMemberIds);
		addedProfessorIds.removeAll(existingMembersAsMap.keySet());
		Set<Long> removedProfessorIds = new HashSet<Long>();
		removedProfessorIds.addAll(existingMembersAsMap.keySet());
		removedProfessorIds.removeAll(newMemberIds);
		// Validate removal
		if (removedProfessorIds.size() > 0) {
			try {
				em.createQuery("select pcm from PositionCommitteeMember pcm " +
					"where pcm.registerMember.register.id = :registerId " +
					"and pcm.registerMember.professor.id in (:professorIds)")
					.setParameter("registerId", existingRegister.getId())
					.setParameter("status", PositionStatus.EPILOGI)
					.setParameter("professorIds", removedProfessorIds)
					.setMaxResults(1)
					.getSingleResult();
				throw new RestException(Status.CONFLICT, "professor.is.committee.member");
			} catch (NoResultException e) {
			}
			try {
				em.createQuery("select e.id from PositionEvaluator e " +
					"where e.registerMember.register.id = :registerId " +
					"and e.registerMember.professor.id in (:professorIds)")
					.setParameter("registerId", existingRegister.getId())
					.setParameter("status", PositionStatus.EPILOGI)
					.setParameter("professorIds", removedProfessorIds)
					.setMaxResults(1)
					.getSingleResult();
				throw new RestException(Status.CONFLICT, "professor.is.evaluator");
			} catch (NoResultException e) {
			}
			try {
				em.createQuery("select e.id from CandidacyEvaluator e " +
					"where e.registerMember.register.id = :registerId " +
					"and e.registerMember.professor.id in (:professorIds)")
					.setParameter("registerId", existingRegister.getId())
					.setParameter("professorIds", removedProfessorIds)
					.setMaxResults(1)
					.getSingleResult();
				throw new RestException(Status.CONFLICT, "professor.is.candidacy.evaluator");
			} catch (NoResultException e) {
			}
		}
		// Validate addition
		for (Professor p : newRegisterMembers) {
			if (addedProfessorIds.contains(p.getId()) &&
				!p.getStatus().equals(RoleStatus.ACTIVE)) {
				throw new RestException(Status.CONFLICT, "professor.is.inactive");
			}
		}
		// Create new list, without replacing existing members 
		Map<Long, RegisterMember> newMembersAsMap = new HashMap<Long, RegisterMember>();
		for (Professor existingProfessor : newRegisterMembers) {
			if (existingMembersAsMap.containsKey(existingProfessor.getId())) {
				newMembersAsMap.put(existingProfessor.getId(), existingMembersAsMap.get(existingProfessor.getId()));
			} else {
				RegisterMember newMember = new RegisterMember();
				newMember.setRegister(existingRegister);
				newMember.setProfessor(existingProfessor);
				switch (existingProfessor.getDiscriminator()) {
					case PROFESSOR_DOMESTIC:
						newMember.setExternal(existingRegister.getInstitution().getId() != ((ProfessorDomestic) existingProfessor).getDepartment().getSchool().getInstitution().getId());
						break;
					case PROFESSOR_FOREIGN:
						newMember.setExternal(true);
						break;
					default:
						break;
				}
				newMembersAsMap.put(existingProfessor.getId(), newMember);
			}
		}

		try {
			// Update
			existingRegister = existingRegister.copyFrom(register);
			existingRegister.getMembers().clear();
			for (RegisterMember member : newMembersAsMap.values()) {
				existingRegister.addMember(member);
			}
			existingRegister.setPermanent(true);
			existingRegister = em.merge(existingRegister);

			em.flush();

			// Send E-Mails:
			//1. To Added Members:
			for (Long professorId : addedProfessorIds) {
				final RegisterMember savedMember = newMembersAsMap.get(professorId);
				if (savedMember.isExternal()) {
					// register.create.register.member.external@member
					mailService.postEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"register.create.register.member.external@member",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", savedMember.getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", savedMember.getProfessor().getUser().getBasicInfo().getLastname());
								put("institution", savedMember.getRegister().getInstitution().getName());
							}
						}));
				} else {
					// register.create.register.member.internal@member
					mailService.postEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"register.create.register.member.internal@member",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", savedMember.getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", savedMember.getProfessor().getUser().getBasicInfo().getLastname());
								put("institution", savedMember.getRegister().getInstitution().getName());
							}
						}));
				}
			}
			//2. To Removed Members:
			for (Long professorID : removedProfessorIds) {
				final RegisterMember removedMember = existingMembersAsMap.get(professorID);
				if (removedMember.isExternal()) {
					// register.remove.register.member.external@member
					mailService.postEmail(removedMember.getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"register.remove.register.member.external@member",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", removedMember.getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", removedMember.getProfessor().getUser().getBasicInfo().getLastname());
								put("institution", removedMember.getRegister().getInstitution().getName());
							}
						}));
				} else {
					// register.remove.register.member.internal@member
					mailService.postEmail(removedMember.getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"register.remove.register.member.internal@member",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("firstname", removedMember.getProfessor().getUser().getBasicInfo().getFirstname());
								put("lastname", removedMember.getProfessor().getUser().getBasicInfo().getLastname());
								put("institution", removedMember.getRegister().getInstitution().getName());
							}
						}));
				}
			}
			// End: Send E-Mails

			// Return Result
			addCanBeDeletedInfo(existingRegister);
			return existingRegister;
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Removes the Register with the given ID
	 * 
	 * @param authToken
	 * @param id
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 */
	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public void delete(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") long id) {
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

	/**
	 * Returns the list of Register Members of this Register
	 * 
	 * @param authToken
	 * @param registerId
	 * @return
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 */
	@GET
	@Path("/{id:[0-9]+}/members")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<RegisterMember> getRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
		getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		return register.getMembers();
	}

	/**
	 * Returns the specific member of this register with the given ID
	 * 
	 * @param authToken
	 * @param registerId
	 * @param memberId
	 * @return
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 * @HTTP 404 X-Error-Code: wrong.register.member.id
	 */
	@GET
	@Path("/{id:[0-9]+}/members/{memberId:[0-9]+}")
	@JsonView({DetailedRegisterMemberView.class})
	public RegisterMember getRegisterMember(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @PathParam("memberId") Long memberId) {
		getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Validate:
		if (register == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		for (RegisterMember member : register.getMembers()) {
			if (member.getId().equals(memberId)) {
				return member;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
	}

	/**
	 * Returns a list of professors that can be added in this register
	 * 
	 * @param authToken
	 * @param registerId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 */
	@GET
	@Path("/{id:[0-9]+}/professor")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<Role> getRegisterProfessors(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
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
		return professors;
	}

}
