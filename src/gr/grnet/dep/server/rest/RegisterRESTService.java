package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Register.DetailedRegisterView;
import gr.grnet.dep.service.model.Register.RegisterView;
import gr.grnet.dep.service.model.RegisterMember.DetailedRegisterMemberView;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.util.CompareUtil;
import gr.grnet.dep.service.util.StringUtil;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		User loggedOn = getLoggedOn(authToken);
		Collection<Register> registers = em.createQuery(
				"select r from Register r " +
						"where r.permanent = true", Register.class)
				.getResultList();

		Collection<Long> loggedOnRegisterIds = em.createQuery(
				"select distinct(rm.register.id) from RegisterMember rm " +
						"where rm.professor.user.id = :userId", Long.class)
				.setParameter("userId", loggedOn.getId())
				.getResultList();

		for (Register r : registers) {
			r.setAmMember(loggedOnRegisterIds.contains(r.getId()));
		}

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
			Register r = em.createQuery(
					"select r from Register r " +
							"left join fetch r.members rm " +
							"left join fetch rm.professor p " +
							"left join fetch p.user u " +
							"left join fetch u.roles rls " +
							"where r.id=:id", Register.class)
					.setParameter("id", id)
					.getSingleResult();

			addCanBeDeletedInfo(r);
			return r;
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}

	}

	private void addCanBeDeletedInfo(Register register) {
		List<Long> nonRemovableMemberIds = em.createQuery(
				"select rm.id from RegisterMember rm " +
						"where rm.register.id = :registerId " +
						"and (" +
						"	exists (select pcm.id from PositionCommitteeMember pcm where pcm.registerMember.id = rm.id ) " +
						"	or exists (select pe.id from PositionEvaluator pe where pe.registerMember.id = rm.id ) " +
						"	or exists (select ce.id from CandidacyEvaluator ce where ce.registerMember.id = rm.id ) " +
						")", Long.class)
				.setParameter("registerId", register.getId())
				.getResultList();

		for (RegisterMember member : register.getMembers()) {
			boolean cannotBeDeleted = nonRemovableMemberIds.contains(member.getId());
			member.setCanBeDeleted(!cannotBeDeleted);
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
		if (!newRegister.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Update
		try {
			newRegister.setInstitution(institution);
			newRegister.setSubject(null);
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
		Register existingRegister = null;
		try {
			existingRegister = em.createQuery(
					"select r from Register r " +
							"left join fetch r.members rm " +
							"left join fetch rm.professor p " +
							"left join fetch p.user u " +
							"left join fetch u.roles rls " +
							"where r.id=:id", Register.class)
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		// Validate:
		if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingRegister.getInstitution().getId().equals(register.getInstitution().getId())) {
			throw new RestException(Status.CONFLICT, "register.institution.change");
		}
		if (register.getSubject() == null || register.getSubject().getName() == null) {
			throw new RestException(Status.BAD_REQUEST, "register.missing.subject");
		}
		if (!CompareUtil.equalsIgnoreNull(register.getSubject(), existingRegister.getSubject())) {
			// Validate subject change
			try {
				em.createQuery(
						"select r.id from Register r " +
								"where r.subject.name = :name " +
								"and r.institution.id = :institutionId " +
								"and r.id != :registerId ", Long.class)
						.setParameter("institutionId", existingRegister.getInstitution().getId())
						.setParameter("name", register.getSubject().getName())
						.setParameter("registerId", existingRegister.getId())
						.setMaxResults(1)
						.getSingleResult();
				throw new RestException(Status.CONFLICT, "register.subject.unavailable");
			} catch (NoResultException e) {
			}
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
			TypedQuery<Professor> query = em.createQuery(
					"select distinct p from Professor p " +
							"where p.id in (:ids)", Professor.class)
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
				em.createQuery("select pcm.id from PositionCommitteeMember pcm " +
						"where pcm.registerMember.register.id = :registerId " +
						"and pcm.registerMember.professor.id in (:professorIds)", Long.class)
						.setParameter("registerId", existingRegister.getId())
						.setParameter("professorIds", removedProfessorIds)
						.setMaxResults(1)
						.getSingleResult();
				throw new RestException(Status.CONFLICT, "professor.is.committee.member");
			} catch (NoResultException e) {
			}
			try {
				em.createQuery("select e.id from PositionEvaluator e " +
						"where e.registerMember.register.id = :registerId " +
						"and e.registerMember.professor.id in (:professorIds)", Long.class)
						.setParameter("registerId", existingRegister.getId())
						.setParameter("professorIds", removedProfessorIds)
						.setMaxResults(1)
						.getSingleResult();
				throw new RestException(Status.CONFLICT, "professor.is.evaluator");
			} catch (NoResultException e) {
			}
			try {
				em.createQuery("select e.id from CandidacyEvaluator e " +
						"where e.registerMember.register.id = :registerId " +
						"and e.registerMember.professor.id in (:professorIds)", Long.class)
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
						newMember.setExternal(!existingRegister.getInstitution().getId().equals(((ProfessorDomestic) existingProfessor).getDepartment().getSchool().getInstitution().getId()));
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
			existingRegister.setSubject(utilityService.supplementSubject(register.getSubject()));
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
									put("firstname_el", savedMember.getProfessor().getUser().getFirstname("el"));
									put("lastname_el", savedMember.getProfessor().getUser().getLastname("el"));
									put("institution_el", savedMember.getRegister().getInstitution().getName().get("el"));

									put("firstname_en", savedMember.getProfessor().getUser().getFirstname("en"));
									put("lastname_en", savedMember.getProfessor().getUser().getLastname("en"));
									put("institution_en", savedMember.getRegister().getInstitution().getName().get("en"));

									put("discipline", savedMember.getRegister().getSubject() != null ? savedMember.getRegister().getSubject().getName() : "");
								}
							}));
				} else {
					// register.create.register.member.internal@member
					mailService.postEmail(savedMember.getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"register.create.register.member.internal@member",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("firstname_el", savedMember.getProfessor().getUser().getFirstname("el"));
									put("lastname_el", savedMember.getProfessor().getUser().getLastname("el"));
									put("institution_el", savedMember.getRegister().getInstitution().getName().get("el"));

									put("firstname_en", savedMember.getProfessor().getUser().getFirstname("en"));
									put("lastname_en", savedMember.getProfessor().getUser().getLastname("en"));
									put("institution_en", savedMember.getRegister().getInstitution().getName().get("en"));

									put("discipline", savedMember.getRegister().getSubject() != null ? savedMember.getRegister().getSubject().getName() : "");
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
									put("firstname_el", removedMember.getProfessor().getUser().getFirstname("el"));
									put("lastname_el", removedMember.getProfessor().getUser().getLastname("el"));
									put("institution_el", removedMember.getRegister().getInstitution().getName().get("el"));

									put("firstname_en", removedMember.getProfessor().getUser().getFirstname("en"));
									put("lastname_en", removedMember.getProfessor().getUser().getLastname("en"));
									put("institution_en", removedMember.getRegister().getInstitution().getName().get("en"));

									put("discipline", removedMember.getRegister().getSubject() != null ? removedMember.getRegister().getSubject().getName() : "");
								}
							}));
				} else {
					// register.remove.register.member.internal@member
					mailService.postEmail(removedMember.getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"register.remove.register.member.internal@member",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("firstname_el", removedMember.getProfessor().getUser().getFirstname("el"));
									put("lastname_el", removedMember.getProfessor().getUser().getLastname("el"));
									put("institution_el", removedMember.getRegister().getInstitution().getName().get("el"));

									put("firstname_en", removedMember.getProfessor().getUser().getFirstname("en"));
									put("lastname_en", removedMember.getProfessor().getUser().getLastname("en"));
									put("institution_en", removedMember.getRegister().getInstitution().getName().get("en"));

									put("discipline", removedMember.getRegister().getSubject() != null ? removedMember.getRegister().getSubject().getName() : "");
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
		if (!register.isUserAllowedToEdit(loggedOn)) {
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

	@GET
	@Path("/{id:[0-9]+}/professor")
	@JsonView({DetailedRegisterMemberView.class})
	public Collection<Role> getRegisterProfessors(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @QueryParam("prof[]") List<Long> roleIds) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, registerId);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);

		if (roleIds != null && !roleIds.isEmpty()) {
			List<Role> professors = em.createQuery(
					"select r from Role r " +
							"where r.id in (:ids) " +
							"and r.discriminator in (:discriminators) " +
							"and r.status = :status", Role.class)
					.setParameter("discriminators", discriminatorList)
					.setParameter("status", RoleStatus.ACTIVE)
					.setParameter("ids", roleIds)
					.getResultList();

			return professors;
		} else {
			return new ArrayList<Role>();
		}
	}

	/**
	 * Returns a paginated list of professors that can be added in this register
	 *
	 * @param authToken
	 * @param registerId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.register.id
	 */
	@POST
	@Path("/{id:[0-9]+}/professor")
	@JsonView({DetailedRegisterMemberView.class})
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public SearchData<Role> searchRegisterProfessors(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId, @Context HttpServletRequest request) {
		User loggedOn = getLoggedOn(authToken);
		Register existingRegister = em.find(Register.class, registerId);
		// Validate:
		if (existingRegister == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.id");
		}
		if (!existingRegister.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// 1. Read parameters:
		Long userId = request.getParameter("user").matches("\\d+") ? Long.valueOf(request.getParameter("user")) : null;
		String firstname = request.getParameter("firstname");
		String lastname = request.getParameter("lastname");
		String role = request.getParameter("role");
		Long rankId = request.getParameter("rank").matches("\\d+") ? Long.valueOf(request.getParameter("rank")) : null;
		String institution = request.getParameter("institution");
		String subject = request.getParameter("subject");

		// Ordering
		String orderNo = request.getParameter("iSortCol_0");
		String orderField = request.getParameter("mDataProp_" + orderNo);
		String orderDirection = request.getParameter("sSortDir_0");
		// Pagination
		int iDisplayStart = Integer.valueOf(request.getParameter("iDisplayStart"));
		int iDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));
		// DataTables:
		String sEcho = request.getParameter("sEcho");

		// Prepare Query

		StringBuilder searchQueryString = new StringBuilder(
				"select rl.id from Role rl " +
						"where rl.id is not null " +
						"and rl.discriminator in (:discriminators) " +
						"and rl.status = :status ");

		if (userId != null) {
			searchQueryString.append(" and rl.user.id = :userId ");
		}
		if (firstname != null && !firstname.isEmpty()) {
			searchQueryString.append(" and rl.user.basicInfo.firstname like :firstname");
		}
		if (lastname != null && !lastname.isEmpty()) {
			searchQueryString.append(" and rl.user.basicInfo.lastname like :lastname");
		}
		if (role != null && !role.isEmpty()) {
			searchQueryString.append(" and rl.discriminator = :role ");
		}
		if (rankId != null) {
			searchQueryString.append(" and ( " +
					"	exists (" +
					"		select pd.id from ProfessorDomestic pd " +
					"		where pd.id = rl.id " +
					"		and pd.rank.id = :rankId " +
					"	) " +
					"	or exists (" +
					"		select pf.id from ProfessorForeign pf " +
					"		where pf.id = rl.id " +
					"		and pf.rank.id = :rankId " +
					"	) " +
					") ");
		}
		if (institution != null && !institution.isEmpty()) {
			searchQueryString.append(" and (" +
					"	exists (" +
					"		select pd.id from ProfessorDomestic pd " +
					"		join pd.department.name dname " +
					"		join pd.department.school.name sname " +
					"		join pd.department.school.institution.name iname " +
					"		where pd.id = rl.id " +
					"		and ( dname like :institution " +
					"			or sname like :institution " +
					"			or iname like :institution " +
					"		)" +
					"	) " +
					"	or exists (" +
					"		select pf.id from ProfessorForeign pf " +
					"		where pf.id = rl.id " +
					"		and pf.institution like :institution " +
					"	) " +
					") ");
		}
		if (subject != null && !subject.isEmpty()) {
			searchQueryString.append(" and (" +
					"	exists (" +
					"		select pd.id from ProfessorDomestic pd " +
					"		left join pd.fekSubject fsub " +
					"		left join pd.subject sub " +
					"		where pd.id = rl.id " +
					"		and ( fsub.name like :subject or sub.name like :subject) " +
					"	) " +
					"	or exists (" +
					"		select pf.id from ProfessorForeign pf " +
					"		left join pf.subject sub " +
					"		where pf.id = rl.id " +
					"		and sub.name like :subject " +
					"	) " +
					") ");
		}

		// Query Sorting
		String orderString;
		if (orderField != null && !orderField.isEmpty()) {
			if (orderField.equals("id")) {
				orderString = "order by r.user.id " + orderDirection;
			} else if (orderField.equals("profile")) {
				orderString = "order by r.discriminator " + orderDirection + ", r.user.id " + orderDirection;
			} else if (orderField.equals("firstname")) {
				orderString = "order by r.user.basicInfo.firstname " + orderDirection + " , r.user.basicInfo.lastname, r.user.id ";
			} else {
				// lastname is default
				orderString = "order by r.user.basicInfo.lastname " + orderDirection + " , r.user.basicInfo.firstname, r.user.id ";
			}
		} else {
			orderString = "order by r.user.basicInfo.lastname " + orderDirection + " , r.user.basicInfo.firstname, r.user.id ";
		}

		Query countQuery = em.createQuery(
				"select count(id) from Role r " +
						"where r.id in ( " +
						searchQueryString.toString() +
						" ) ");
		TypedQuery<Role> searchQuery = em.createQuery(
				"select r from Role r " +
						"left join fetch r.user u " +
						"where r.id in ( " +
						searchQueryString.toString() +
						" ) " +
						orderString, Role.class);

		// Parameters:
		List<RoleDiscriminator> discriminatorList = new ArrayList<Role.RoleDiscriminator>();
		discriminatorList.add(RoleDiscriminator.PROFESSOR_DOMESTIC);
		discriminatorList.add(RoleDiscriminator.PROFESSOR_FOREIGN);
		searchQuery.setParameter("discriminators", discriminatorList);
		countQuery.setParameter("discriminators", discriminatorList);

		searchQuery.setParameter("status", RoleStatus.ACTIVE);
		countQuery.setParameter("status", RoleStatus.ACTIVE);

		if (userId != null) {
			searchQuery.setParameter("userId", userId);
			countQuery.setParameter("userId", userId);
		}
		if (firstname != null && !firstname.isEmpty()) {
			searchQuery.setParameter("firstname", "%" + StringUtil.toUppercaseNoTones(firstname, new Locale("el")) + "%");
			countQuery.setParameter("firstname", "%" + StringUtil.toUppercaseNoTones(firstname, new Locale("el")) + "%");
		}
		if (lastname != null && !lastname.isEmpty()) {
			searchQuery.setParameter("lastname", "%" + StringUtil.toUppercaseNoTones(lastname, new Locale("el")) + "%");
			countQuery.setParameter("lastname", "%" + StringUtil.toUppercaseNoTones(lastname, new Locale("el")) + "%");
		}
		if (role != null && !role.isEmpty()) {
			searchQuery.setParameter("role", RoleDiscriminator.valueOf(role));
			countQuery.setParameter("role", RoleDiscriminator.valueOf(role));
		}
		if (rankId != null) {
			searchQuery.setParameter("rankId", rankId);
			countQuery.setParameter("rankId", rankId);
		}
		if (institution != null && !institution.isEmpty()) {
			searchQuery.setParameter("institution", "%" + StringUtil.toUppercaseNoTones(institution, new Locale("el")) + "%");
			countQuery.setParameter("institution", "%" + StringUtil.toUppercaseNoTones(institution, new Locale("el")) + "%");
		}
		if (subject != null && !subject.isEmpty()) {
			searchQuery.setParameter("subject", "%" + StringUtil.toUppercaseNoTones(subject, new Locale("el")) + "%");
			countQuery.setParameter("subject", "%" + StringUtil.toUppercaseNoTones(subject, new Locale("el")) + "%");
		}
		// Execute
		Long totalRecords = (Long) countQuery.getSingleResult();
		List<Role> paginatedProfessors = searchQuery
				.setFirstResult(iDisplayStart)
				.setMaxResults(iDisplayLength)
				.getResultList();

		// Return Result
		SearchData<Role> result = new SearchData<Role>();
		result.setiTotalRecords(totalRecords);
		result.setiTotalDisplayRecords(totalRecords);
		result.setsEcho(Integer.valueOf(sEcho));
		result.setRecords(paginatedProfessors);

		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/professorsexport")
	public Response getProfessorsExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken) {
		User loggedOn = getLoggedOn(authToken);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.INSTITUTION_MANAGER)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Generate Document
		try {
			InputStream is = reportService.createProfessorDataExcel();
			String filename = "generic_register.xls";

			// Return response
			return Response.ok(is)
					.type(MediaType.APPLICATION_OCTET_STREAM)
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"")
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "getDocument", e);
			throw new EJBException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{id:[0-9]+}/export")
	public Response getRegisterExport(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long registerId) {
		User loggedOn = getLoggedOn(authToken);
		Register register = em.find(Register.class, registerId);
		// Authorize
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithInstitution(register.getInstitution())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Generate Document
		try {
			InputStream is = reportService.createRegisterExportExcel(registerId);
			String filename = "register_" +
					(register.getInstitution().getSchacHomeOrganization() == null ? register.getInstitution().getId() : register.getInstitution().getSchacHomeOrganization()) +
					".xls";

			// Return response
			return Response.ok(is)
					.type(MediaType.APPLICATION_OCTET_STREAM)
					.header("charset", "UTF-8")
					.header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"")
					.build();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "getDocument", e);
			throw new EJBException(e);
		}
	}
}
