package gr.grnet.dep.server.rest;

import com.fasterxml.jackson.annotation.JsonView;
import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionCommittee.DetailedPositionCommitteeView;
import gr.grnet.dep.service.model.PositionCommitteeMember.DetailedPositionCommitteeMemberView;
import gr.grnet.dep.service.model.PositionCommitteeMember.MemberType;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.file.FileHeader.SimpleFileHeaderView;
import gr.grnet.dep.service.model.file.FileType;
import gr.grnet.dep.service.model.file.PositionCommitteeFile;
import gr.grnet.dep.service.util.DateUtil;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/position/{id:[0-9][0-9]*}/committee")
@Stateless
public class PositionCommitteeRESTService extends RESTService {

	@Inject
	private Logger log;

	/*********************
	 * Register Members **
	 *********************/

	/**
	 * Returns the list of registries associated with specified position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return A list of registerMember
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/register")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Collection<Register> getPositionCommiteeRegisters(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)
				&& !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<Register> registers = em.createQuery(
				"select r from Register r " +
						"where r.permanent = true " +
						"and r.institution.id = :institutionId ", Register.class)
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.getResultList();

		return registers;
	}

	/**
	 * Returns the list of register members associates with specified position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return A list of registerMember
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/register/{registerId:[0-9]+}/member")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public Collection<RegisterMember> getPositionCommiteeRegisterMembers(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("registerId") Long registerId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)
				&& !loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Prepare Query
		List<RegisterMember> registerMembers = em.createQuery(
				"select m from RegisterMember m  " +
						"join m.professor.user.roles r " +
						"where m.register.permanent = true " +
						"and m.register.institution.id = :institutionId " +
						"and m.register.id = :registerId " +
						"and m.professor.status = :status ", RegisterMember.class)
				.setParameter("registerId", registerId)
				.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
				.setParameter("status", RoleStatus.ACTIVE)
				.getResultList();

		// Execute
		for (RegisterMember r : registerMembers) {
			Professor p = (Professor) r.getProfessor();
			p.setCommitteesCount(Professor.countActiveCommittees(p));
			p.setEvaluationsCount(Professor.countActiveEvaluations(p));
		}
		return registerMembers;
	}

	/*********************
	 * Committee *********
	 *********************/

	/**
	 * Returns the committee description of the specified position
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee get(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = null;
		try {
			existingCommittee = em.createQuery(
					"select pcom from PositionCommittee pcom " +
							"left join fetch pcom.members pmem " +
							"where pcom.id = :committeeId", PositionCommittee.class)
					.setParameter("committeeId", committeeId)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingCommittee.containsMember(loggedOn) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		return existingCommittee;
	}

	/**
	 * Returns information of the specified member of the committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param cmId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.position.commitee.member.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/member/{cmId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeMemberView.class})
	public PositionCommitteeMember getPositionCommitteeMember(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("cmId") Long cmId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment())) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		for (PositionCommitteeMember existingMember : existingCommittee.getMembers()) {
			if (existingMember.getId().equals(cmId)) {
				existingMember.getRegisterMember().getProfessor().getUser().getRoles();
				return existingMember;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.position.commitee.member.id");
	}

	/**
	 * Updates the specified committee of the position
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param newCommittee
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.register.member.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: committee.missing.apofasi.systasis.epitropis
	 * @HTTP 409 X-Error-Code: member.is.evaluator
	 * @HTTP 409 X-Error-Code: max.regular.members.failed
	 * @HTTP 409 X-Error-Code: max.substitute.members.failed
	 * @HTTP 409 X-Error-Code: min.internal.regular.members.failed
	 * @HTTP 409 X-Error-Code: min.internal.substitute.members.failed
	 * @HTTP 409 X-Error-Code: min.external.regular.members.failed
	 * @HTTP 409 X-Error-Code: min.external.substitute.members.failed
	 */
	@PUT
	@Path("/{committeeId:[0-9]+}")
	@JsonView({DetailedPositionCommitteeView.class})
	public PositionCommittee update(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, PositionCommittee newCommittee) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.CONFLICT, "wrong.position.committee.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI) || existingPosition.getPhase().getCandidacies() == null) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		// Retrieve new Members from Institution's Register
		Map<Long, PositionCommitteeMember> existingCommitteeMemberAsMap = new HashMap<Long, PositionCommitteeMember>();
		for (PositionCommitteeMember existingCommitteeMember : existingCommittee.getMembers()) {
			existingCommitteeMemberAsMap.put(existingCommitteeMember.getRegisterMember().getId(), existingCommitteeMember);
		}

		// Retrieve new Members from Institution's Register
		Map<Long, MemberType> newCommitteeMemberAsMap = new HashMap<Long, MemberType>();
		for (PositionCommitteeMember newCommitteeMember : newCommittee.getMembers()) {
			newCommitteeMemberAsMap.put(newCommitteeMember.getRegisterMember().getId(), newCommitteeMember.getType());
		}
		List<RegisterMember> newRegisterMembers = new ArrayList<RegisterMember>();

		if (!newCommitteeMemberAsMap.isEmpty()) {
			TypedQuery<RegisterMember> query = em.createQuery(
					"select distinct m from Register r " +
							"join r.members m " +
							"where r.permanent = true " +
							"and r.institution.id = :institutionId " +
							"and m.professor.status = :status " +
							"and m.id in (:registerIds)", RegisterMember.class)
					.setParameter("institutionId", existingPosition.getDepartment().getSchool().getInstitution().getId())
					.setParameter("status", RoleStatus.ACTIVE)
					.setParameter("registerIds", newCommitteeMemberAsMap.keySet());
			newRegisterMembers.addAll(query.getResultList());
		}
		if (newCommittee.getMembers().size() != newRegisterMembers.size()) {
			throw new RestException(Status.NOT_FOUND, "wrong.register.member.id");
		}
		// Validate New Committee
		if (newCommittee.getMembers().size() > 0 &&
				newCommittee.getCandidacyEvalutionsDueDate() == null) {
			throw new RestException(Status.CONFLICT, "committee.missing.candidacyEvalutions.dueDate");
		}

		// Να μην είναι δυνατή η συμπλήρωση της Επιτροπής εάν δεν έχει συμπληρωθεί το πεδίο "Απόφαση Σύστασης Επιτροπής".
		if (newCommittee.getMembers().size() > 0 &&
				FileHeader.filter(existingCommittee.getFiles(), FileType.APOFASI_SYSTASIS_EPITROPIS).isEmpty()) {
			throw new RestException(Status.CONFLICT, "committee.missing.apofasi.systasis.epitropis");
		}

		// Check committee structure
		int countRegular = 0;
		int countSubstitute = 0;
		int countInternalRegular = 0;
		int countInternalSubstitute = 0;
		int countExternalRegular = 0;
		int countExternalRegularForeign = 0;
		int countExternalSubstitute = 0;
		int countExternalSubstituteForeign = 0;
		for (RegisterMember newRegisterMember : newRegisterMembers) {
			MemberType type = newCommitteeMemberAsMap.get(newRegisterMember.getId());
			switch (type) {
				case REGULAR:
					countRegular++;
					if (newRegisterMember.isExternal()) {
						countExternalRegular++;
						if (newRegisterMember.getProfessor().getUser().hasActiveRole(RoleDiscriminator.PROFESSOR_FOREIGN)) {
							countExternalRegularForeign++;
						}
					} else {
						countInternalRegular++;
					}
					break;
				case SUBSTITUTE:
					countSubstitute++;
					if (newRegisterMember.isExternal()) {
						countExternalSubstitute++;
						if (newRegisterMember.getProfessor().getUser().hasActiveRole(RoleDiscriminator.PROFESSOR_FOREIGN)) {
							countExternalSubstituteForeign++;
						}
					} else {
						countInternalSubstitute++;
					}
					break;
			}
		}
		if (countRegular != PositionCommitteeMember.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "max.regular.members.failed");
		}
		if (countSubstitute != PositionCommitteeMember.MAX_MEMBERS) {
			throw new RestException(Status.CONFLICT, "max.substitute.members.failed");
		}
		if (countInternalRegular < PositionCommitteeMember.MIN_INTERNAL) {
			throw new RestException(Status.CONFLICT, "min.internal.regular.members.failed");
		}
		if (countInternalSubstitute < PositionCommitteeMember.MIN_INTERNAL) {
			throw new RestException(Status.CONFLICT, "min.internal.substitute.members.failed");
		}
		if (countExternalRegular < PositionCommitteeMember.MIN_EXTERNAL) {
			throw new RestException(Status.CONFLICT, "min.external.regular.members.failed");
		}
		if (countExternalRegularForeign == 0) {
			throw new RestException(Status.CONFLICT, "min.external.regular.foreign.members.failed");
		}
		if (countExternalSubstitute < PositionCommitteeMember.MIN_EXTERNAL) {
			throw new RestException(Status.CONFLICT, "min.external.substitute.members.failed");
		}
		if (countExternalSubstituteForeign == 0) {
			throw new RestException(Status.CONFLICT, "min.external.substitute.foreign.members.failed");
		}

		// Keep these to send mails
		Set<Long> addedMemberIds = new HashSet<Long>();
		Set<Long> removedMemberIds = new HashSet<Long>();
		removedMemberIds.addAll(existingCommitteeMemberAsMap.keySet());
		boolean committeeMeetingDateUpdated = (newCommittee.getCommitteeMeetingDate() != null) &&
				(DateUtil.compareDates(existingCommittee.getCommitteeMeetingDate(), newCommittee.getCommitteeMeetingDate()) != 0);

		// Update
		try {
			existingCommittee.copyFrom(newCommittee);

			existingCommittee.getMembers().clear();
			for (RegisterMember newRegisterMember : newRegisterMembers) {
				PositionCommitteeMember newCommitteeMember = null;
				if (existingCommitteeMemberAsMap.containsKey(newRegisterMember.getId())) {
					newCommitteeMember = existingCommitteeMemberAsMap.get(newRegisterMember.getId());
				} else {
					newCommitteeMember = new PositionCommitteeMember();
					newCommitteeMember.setRegisterMember(newRegisterMember);
					addedMemberIds.add(newRegisterMember.getId());
				}
				newCommitteeMember.setType(newCommitteeMemberAsMap.get(newRegisterMember.getId()));
				existingCommittee.addMember(newCommitteeMember);

				removedMemberIds.remove(newRegisterMember.getId());
			}

			final PositionCommittee savedCommittee = em.merge(existingCommittee);
			em.flush();

			// Send E-Mails
			for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
				if (addedMemberIds.contains(member.getRegisterMember().getId())) {
					if (member.getType().equals(MemberType.REGULAR)) {
						// positionCommittee.create.regular.member@member
						mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
								"default.subject",
								"positionCommittee.create.regular.member@member",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
										put("position", savedCommittee.getPosition().getName());

										put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

										put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
										put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
									}
								}));
					} else {
						// positionCommittee.create.substitute.member@member
						mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
								"default.subject",
								"positionCommittee.create.substitute.member@member",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
										put("position", savedCommittee.getPosition().getName());

										put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

										put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
										put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
									}
								}));
					}
				}
			}
			for (Long registerMemberID : removedMemberIds) {
				final PositionCommitteeMember removedMember = existingCommitteeMemberAsMap.get(registerMemberID);
				if (removedMember.getType().equals(MemberType.REGULAR)) {
					// positionCommittee.remove.regular.member@member
					mailService.postEmail(removedMember.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.remove.regular.member@member",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", removedMember.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", removedMember.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				} else {
					// positionCommittee.remove.substitute.member@member
					mailService.postEmail(removedMember.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.remove.substitute.member@member",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", removedMember.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", removedMember.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", removedMember.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			}
			if (!addedMemberIds.isEmpty() && removedMemberIds.isEmpty()) {
				// positionCommittee.create.members@candidates
				for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
					mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.create.members@candidates",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
									put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
									put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			}
			if (!removedMemberIds.isEmpty()) {
				//positionCommittee.remove.members@candidates
				for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
					mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.remove.members@candidates",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
									put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
									put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				//positionCommittee.remove.members@evaluators
				if (savedCommittee.getPosition().getPhase().getEvaluation() != null) {
					for (final PositionEvaluator member : savedCommittee.getPosition().getPhase().getEvaluation().getEvaluators()) {
						//positionCommittee.remove.members@members
						mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
								"default.subject",
								"positionCommittee.remove.members@evaluators",
								Collections.unmodifiableMap(new HashMap<String, String>() {

									{
										put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
										put("position", savedCommittee.getPosition().getName());

										put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
										put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
										put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
										put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
										put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

										put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
										put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
										put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
										put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
										put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
									}
								}));
					}
				}
				//positionCommittee.remove.members@members
				for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.remove.members@members",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}

			}
			if (committeeMeetingDateUpdated) {
				// positionCommittee.update.committeeMeetingDate@members
				for (final PositionCommitteeMember member : savedCommittee.getMembers()) {
					mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.update.committeeMeetingDate@members",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
									put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
									put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
				// positionCommittee.update.committeeMeetingDate@candidates
				for (final Candidacy candidacy : savedCommittee.getPosition().getPhase().getCandidacies().getCandidacies()) {
					mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
							"default.subject",
							"positionCommittee.update.committeeMeetingDate@candidates",
							Collections.unmodifiableMap(new HashMap<String, String>() {

								{
									put("positionID", StringUtil.formatPositionID(savedCommittee.getPosition().getId()));
									put("position", savedCommittee.getPosition().getName());

									put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
									put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
									put("institution_el", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
									put("school_el", savedCommittee.getPosition().getDepartment().getSchool().getName().get("el"));
									put("department_el", savedCommittee.getPosition().getDepartment().getName().get("el"));

									put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
									put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
									put("institution_en", savedCommittee.getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
									put("school_en", savedCommittee.getPosition().getDepartment().getSchool().getName().get("en"));
									put("department_en", savedCommittee.getPosition().getDepartment().getName().get("en"));
								}
							}));
				}
			}

			// End: Send E-Mails

			// Return result
			return savedCommittee;
		} catch (PersistenceException e) {
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**********************
	 * File Functions *****
	 **********************/

	/**
	 * Returns the list of file descriptions associated with position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/file")
	@JsonView({SimpleFileHeaderView.class})
	public Collection<PositionCommitteeFile> getFiles(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingCommittee.containsMember(loggedOn) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		return FileHeader.filterDeleted(existingCommittee.getFiles());
	}

	/**
	 * Return the file description of the file with given id and associated with
	 * given position committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public PositionCommitteeFile getFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingCommittee.containsMember(loggedOn) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
		for (PositionCommitteeFile file : files) {
			if (file.getId().equals(fileId)) {
				return file;
			}
		}
		throw new RestException(Status.NOT_FOUND, "wrong.file.id");
	}

	/**
	 * Return the file data of the file with given id and associated with
	 * given position committtee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @param bodyId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}/body/{bodyId:[0-9]+}")
	public Response getFileBody(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @PathParam("bodyId") Long bodyId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		// Validate:
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_MANAGER) &&
				!loggedOn.hasActiveRole(RoleDiscriminator.MINISTRY_ASSISTANT) &&
				!loggedOn.isAssociatedWithDepartment(existingPosition.getDepartment()) &&
				!existingCommittee.containsMember(loggedOn) &&
				!(existingPosition.getPhase().getEvaluation() != null && existingPosition.getPhase().getEvaluation().containsEvaluator(loggedOn)) &&
				!existingPosition.getPhase().getCandidacies().containsCandidate(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		// Return Result
		Collection<PositionCommitteeFile> files = FileHeader.filterDeleted(existingCommittee.getFiles());
		for (FileHeader file : files) {
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

	/**
	 * Handles upload of a new file associated with given position committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: committee.missing.committee.meeting.day
	 */
	@POST
	@Path("/{committeeId:[0-9]+}/file")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String createFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.CONFLICT, "wrong.position.committee.phase");
		}
		// Validate:
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
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
		// 2) Να μην είναι δυνατή η συμπλήρωση των πεδίων 
		// "Πρακτικό Συνεδρίασης Επιτροπής για Αξιολογητές" και "Αίτημα Επιτροπής για Αξιολογητές" εάν δεν έχει συμπληρωθεί το πεδίο "Ημερομηνία Συνεδρίασης Επιτροπής".
		if ((type.equals(FileType.PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES) || type.equals(FileType.AITIMA_EPITROPIS_PROS_AKSIOLOGITES))
				&& existingCommittee.getCommitteeMeetingDate() == null) {
			throw new RestException(Status.CONFLICT, "committee.missing.committee.meeting.day");
		}

		try {
			// Check number of file types
			Set<PositionCommitteeFile> pcFiles = FileHeader.filterIncludingDeleted(existingCommittee.getFiles(), type);
			PositionCommitteeFile existingFile = checkNumberOfFileTypes(PositionCommitteeFile.fileTypes, type, pcFiles);
			if (existingFile != null) {
				return _updateFile(loggedOn, fileItems, existingFile);
			}
			// Create
			final PositionCommitteeFile pcFile = new PositionCommitteeFile();
			pcFile.setCommittee(existingCommittee);
			pcFile.setOwner(loggedOn);
			saveFile(loggedOn, fileItems, pcFile);
			existingCommittee.addFile(pcFile);
			em.flush();

			// Send E-Mails
			// position.upload@committee
			for (final PositionCommitteeMember member : pcFile.getCommittee().getPosition().getPhase().getCommittee().getMembers()) {
				mailService.postEmail(member.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.upload@committee",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
								put("position", pcFile.getCommittee().getPosition().getName());

								put("firstname_el", member.getRegisterMember().getProfessor().getUser().getFirstname("el"));
								put("lastname_el", member.getRegisterMember().getProfessor().getUser().getLastname("el"));
								put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

								put("firstname_en", member.getRegisterMember().getProfessor().getUser().getFirstname("en"));
								put("lastname_en", member.getRegisterMember().getProfessor().getUser().getLastname("en"));
								put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));
							}
						}));
			}
			// position.upload@candidates
			for (final Candidacy candidacy : pcFile.getCommittee().getPosition().getPhase().getCandidacies().getCandidacies()) {
				mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.upload@candidates",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
								put("position", pcFile.getCommittee().getPosition().getName());

								put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
								put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
								put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

								put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
								put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
								put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));
							}
						}));
			}
			// position.upload@evaluators
			for (final PositionEvaluator evaluator : pcFile.getCommittee().getPosition().getPhase().getEvaluation().getEvaluators()) {
				mailService.postEmail(evaluator.getRegisterMember().getProfessor().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.upload@evaluators",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("positionID", StringUtil.formatPositionID(pcFile.getCommittee().getPosition().getId()));
								put("position", pcFile.getCommittee().getPosition().getName());

								put("firstname_el", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("el"));
								put("lastname_el", evaluator.getRegisterMember().getProfessor().getUser().getLastname("el"));
								put("institution_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("el"));
								put("department_el", pcFile.getCommittee().getPosition().getDepartment().getName().get("el"));

								put("firstname_en", evaluator.getRegisterMember().getProfessor().getUser().getFirstname("en"));
								put("lastname_en", evaluator.getRegisterMember().getProfessor().getUser().getLastname("en"));
								put("institution_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", pcFile.getCommittee().getPosition().getDepartment().getSchool().getName().get("en"));
								put("department_en", pcFile.getCommittee().getPosition().getDepartment().getName().get("en"));
							}
						}));
			}

			return toJSON(pcFile, SimpleFileHeaderView.class);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Handles upload that updates an existing file associated with position
	 * committee
	 *
	 * @param authToken
	 * @param positionId
	 * @param committeeId
	 * @param fileId
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 * @HTTP 400 X-Error-Code: missing.file.type
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 * @HTTP 409 X-Error-Code: wrong.file.type
	 */
	@POST
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
	public String updateFile(@QueryParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("id") Long positionId, @PathParam("committeeId") Long committeeId, @PathParam("fileId") Long fileId, @Context HttpServletRequest request) throws FileUploadException, IOException {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.CONFLICT, "wrong.position.committee.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
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
		try {
			if (!PositionCommitteeFile.fileTypes.containsKey(type)) {
				throw new RestException(Status.CONFLICT, "wrong.file.type");
			}
			PositionCommitteeFile committeeFile = null;
			for (PositionCommitteeFile file : existingCommittee.getFiles()) {
				if (file.getId().equals(fileId)) {
					committeeFile = file;
					break;
				}
			}
			if (committeeFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			// Update
			return _updateFile(loggedOn, fileItems, committeeFile);
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}

	/**
	 * Removes the specified file
	 *
	 * @param authToken
	 * @param committeeId
	 * @param positionId
	 * @param fileId
	 * @return
	 * @HTTP 403 X-Error-Code: insufficient.privileges
	 * @HTTP 404 X-Error-Code: wrong.position.committee.id
	 * @HTTP 404 X-Error-Code: wrong.position.id
	 * @HTTP 404 X-Error-Code: wrong.file.id
	 * @HTTP 409 X-Error-Code: wrong.position.committee.phase
	 * @HTTP 409 X-Error-Code: wrong.position.status
	 */
	@DELETE
	@Path("/{committeeId:[0-9]+}/file/{fileId:[0-9]+}")
	@JsonView({SimpleFileHeaderView.class})
	public Response deleteFile(@HeaderParam(WebConstants.AUTHENTICATION_TOKEN_HEADER) String authToken, @PathParam("committeeId") Long committeeId, @PathParam("id") long positionId, @PathParam("fileId") Long fileId) {
		User loggedOn = getLoggedOn(authToken);
		PositionCommittee existingCommittee = em.find(PositionCommittee.class, committeeId);
		if (existingCommittee == null) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.committee.id");
		}
		Position existingPosition = existingCommittee.getPosition();
		if (!existingPosition.getId().equals(positionId)) {
			throw new RestException(Status.NOT_FOUND, "wrong.position.id");
		}
		if (!existingPosition.getPhase().getCommittee().getId().equals(committeeId)) {
			throw new RestException(Status.FORBIDDEN, "wrong.position.committee.phase");
		}
		if (!existingPosition.isUserAllowedToEdit(loggedOn)) {
			throw new RestException(Status.FORBIDDEN, "insufficient.privileges");
		}
		if (!existingPosition.getPhase().getStatus().equals(PositionStatus.EPILOGI)) {
			throw new RestException(Status.CONFLICT, "wrong.position.status");
		}
		try {
			PositionCommitteeFile committeeFile = null;
			for (PositionCommitteeFile file : existingCommittee.getFiles()) {
				if (file.getId().equals(fileId)) {
					committeeFile = file;
					break;
				}
			}
			if (committeeFile == null) {
				throw new RestException(Status.NOT_FOUND, "wrong.file.id");
			}
			PositionCommitteeFile cf = deleteAsMuchAsPossible(committeeFile);
			if (cf == null) {
				existingCommittee.getFiles().remove(committeeFile);
			}
			return Response.noContent().build();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new RestException(Status.BAD_REQUEST, "persistence.exception");
		}
	}
}
