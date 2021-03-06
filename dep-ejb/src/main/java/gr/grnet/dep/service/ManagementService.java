package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class ManagementService extends CommonService {

	@Resource
	SessionContext sc;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	@EJB
	AuthenticationService authenticationService;

	@EJB
	MailService mailService;

	@Inject
	protected Logger logger;

	protected static Configuration conf;

	@EJB
	CandidateService candidateService;

	@EJB
	PositionService positionService;

	@EJB
	CandidacyService candidacyService;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void massCreateProfessorDomesticAccounts() {
        @SuppressWarnings("unchecked")
        List<ProfessorDomesticData> pdData = em.createQuery(
                "select pdd from ProfessorDomesticData pdd " +
                        "where NOT EXISTS ( " +
                        "	select u.id from User u where u.contactInfo.email = pdd.email " +
                        ") " +
                        "order by pdd.email ")
                .getResultList();

        massCreateProfessorDomesticAccounts(pdData);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<ProfessorDomesticData> massCreateProfessorDomesticAccounts(List<ProfessorDomesticData> pdData) {

        List<ProfessorDomesticData> createdAccountsList = new ArrayList<>();

        logger.fine("CREATING " + pdData.size() + " PROFESSOR DOMESTIC ACCOUNTS");
        for (ProfessorDomesticData data : pdData) {
            User u = authenticationService.findAccountByProfessorDomesticData(data);
            if (u == null) {
                logger.fine("CREATING PROFESSOR DOMESTIC: " + data.getEmail());
                u = authenticationService.createProfessorDomesticAccount(data);
                createdAccountsList.add(data);
                logger.fine("CREATING PROFESSOR DOMESTIC: " + u.getId() + " " + u.getPrimaryRole() + " " + u.getRoles().size());
            } else {
                logger.fine("SKIPPING PROFESSOR DOMESTIC: " + data.getEmail());
            }
        }
        return createdAccountsList;
    }

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void massSendLoginEmails() {
		@SuppressWarnings("unchecked")
		List<Long> users = em.createQuery(
				"select u.id from User u " +
						"where u.authenticationType = :authenticationType " +
						"and u.permanentAuthToken is not null " +
						"and u.loginEmailSent = false ")
				.setParameter("authenticationType", AuthenticationType.EMAIL)
				.getResultList();

		for (Long userId : users) {
			mailService.sendLoginEmail(userId, false);
		}

	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void massSendReminderLoginEmails() {
		@SuppressWarnings("unchecked")
		List<Long> users = em.createQuery(
				"select u.id from User u " +
						"join u.roles r " +
						"where u.authenticationType = :authenticationType " +
						"and u.permanentAuthToken is not null " +
						"and u.loginEmailSent = true " +
						"and (r.discriminator = :roleDiscriminator and r.status = :roleStatus) ")
				.setParameter("authenticationType", AuthenticationType.EMAIL)
				.setParameter("roleDiscriminator", RoleDiscriminator.PROFESSOR_DOMESTIC)
				.setParameter("roleStatus", RoleStatus.UNAPPROVED)
				.getResultList();

		for (Long userId : users) {
			mailService.sendReminderLoginEmail(userId, false);
		}

	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void massSendShibbolethConnectEmails(Collection<Long> institutionIds) {
		if (institutionIds.isEmpty()) {
			logger.fine("massSendShibbolethConnectEmails: Empty Institutions Array");
			return;
		}
		logger.fine("massSendShibbolethConnectEmails: Institutions = " + Arrays.toString(institutionIds.toArray()));
		@SuppressWarnings("unchecked")
		List<Long> users = em.createQuery(
				"select u.id from ProfessorDomestic pd " +
						"join pd.user u " +
						"where u.authenticationType = :uAuthType " +
						"and u.status = :userStatus " +
						"and u.permanentAuthToken is not null " +
						"and pd.institution.authenticationType = :iAuthType " +
						"and pd.institution.id in (:institutionIds) ")
				.setParameter("uAuthType", AuthenticationType.EMAIL)
				.setParameter("iAuthType", AuthenticationType.SHIBBOLETH)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("institutionIds", institutionIds)
				.getResultList();
		logger.fine("massSendShibbolethConnectEmails: Sending to " + users.size() + " users");
		for (Long userId : users) {
			mailService.sendShibbolethConnectEmail(userId, false);
		}
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void massSendReminderFinalizeRegistrationEmails() {
		List<Long> users = em.createQuery(
				"select u.id from ProfessorDomestic pd " +
						"join pd.user u " +
						"where u.authenticationType = :uAuthType " +
						"and u.status = :userStatus " +
						"and u.permanentAuthToken is not null " +
						"and pd.status = :roleStatus ", Long.class)
				.setParameter("uAuthType", AuthenticationType.EMAIL)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.UNAPPROVED)
				.getResultList();
		logger.fine("massSendReminderFinalizeRegistrationEmails: Sending to " + users.size() + " users");
		for (Long userId : users) {
			mailService.sendReminderFinalizeRegistrationEmail(userId, false);
		}
	}


	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void massSendEvaluationEmails() {
		logger.fine("massSendEvaluationEmails:");
		Set<Long> users = new HashSet<>();

		// 1. Candidates
		users.addAll(em.createQuery(
				"select u.id from Candidate r " +
						"join r.user u " +
						"where u.status = :userStatus " +
						"and r.status = :roleStatus ", Long.class)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.getResultList());
		// 2. Institution Managers
		users.addAll(em.createQuery(
				"select u.id from InstitutionManager r " +
						"join r.user u " +
						"where u.status = :userStatus " +
						"and r.status = :roleStatus ", Long.class)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.getResultList());
		// 3. Professor Domestic
		users.addAll(em.createQuery(
				"select u.id from ProfessorDomestic r " +
						"join r.user u " +
						"where u.status = :userStatus " +
						"and r.status = :roleStatus ", Long.class)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.getResultList());
		// 4. Professor Foreign, that speak greek
		users.addAll(em.createQuery(
				"select u.id from ProfessorForeign r " +
						"join r.user u " +
						"where u.status = :userStatus " +
						"and r.status = :roleStatus " +
						"and r.speakingGreek = true ", Long.class)
				.setParameter("userStatus", UserStatus.ACTIVE)
				.setParameter("roleStatus", RoleStatus.ACTIVE)
				.getResultList());

		// Remove already filled:
		users.removeAll(em.createQuery(
				"select e.user.id from Evaluation e ", Long.class)
				.getResultList());

		logger.fine("massSendEvaluationEmails: Sending to " + users.size() + " users");
		for (Long userId : users) {
			mailService.sendEvaluationEmail(userId, false);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void upperCaseSubject(Long subjectId) {
		Subject subject = em.find(Subject.class, subjectId);
		String newName = StringUtil.toUppercaseNoTones(subject.getName(), new Locale("el"));
		if (newName.equals(subject.getName())) {
			logger.fine("upperCaseSubject - Skipping    [" + subject.getId() + "]\t" + subject.getName());
			return;
		}
		// 1. Search if another with the new name exists
		try {
			Subject otherSubject = (Subject) em.createQuery(
					"select s from Subject s " +
							"where s.name = :name ")
					.setParameter("name", newName)
					.getSingleResult();
			// Transfer all entities to this otherSubject
			logger.fine("upperCaseSubject - Transfering [" + subject.getId() + "]\t" + subject.getName() + "\t-> [" + otherSubject.getId() + "]\t" + otherSubject.getName());

			int i = em.createNativeQuery("update ProfessorDomestic set subject_id = :otherSubject where subject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " ProfessorDomestic.subject");

			i = em.createNativeQuery("update ProfessorDomestic set fekSubject_id = :otherSubject where fekSubject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " ProfessorDomestic.fekSubject");

			i = em.createNativeQuery("update ProfessorForeign p set subject_id = :otherSubject where subject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " ProfessorForeign.subject");

			i = em.createNativeQuery("update Candidacy set snapshot_subject_id = :otherSubject where snapshot_subject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " Candidacy.subject");

			i = em.createNativeQuery("update Candidacy set snapshot_fekSubject_id = :otherSubject where snapshot_fekSubject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " Candidacy.fekSubject");

			i = em.createNativeQuery("update Position set subject_id = :otherSubject where subject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " Position.subject");

			i = em.createNativeQuery("update Register set subject_id = :otherSubject where subject_id = :subject")
					.setParameter("otherSubject", otherSubject.getId())
					.setParameter("subject", subject.getId())
					.executeUpdate();
			logger.fine("upperCaseSubject - Updated " + i + " Candidacy.subject");

			// Delete old subject
			em.remove(subject);
		} catch (NoResultException e) {
			// Just save with uppercase letter
			logger.fine("upperCaseSubject - Updating    [" + subject.getId() + "]\t" + subject.getName() + "\t-> " + newName);
			subject.setName(newName);
		}
	}

	public Candidacy createCandidacy(Long positionId, Long candidateId, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
		// Authenticate
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new NotEnabledException("insufficient.privileges");
		}
		// find candidacy
		Candidate candidate = candidateService.getCandidate(candidateId);
		// find position
		Position position = positionService.getPositionById(positionId);


		try {
			Candidacy existingCandidacy = em.createQuery(
					"select c from Candidacy c " +
							"where c.candidate.id = :candidateId " +
							"and c.candidacies.position.id = :positionId", Candidacy.class)
					.setParameter("candidateId", candidate.getId())
					.setParameter("positionId", position.getId())
					.getSingleResult();
			// Return Results
			existingCandidacy.getCandidacyEvalutionsDueDate();
			return existingCandidacy;
		} catch (NoResultException e) {
		}

		// Create
		Candidacy candidacy = new Candidacy();
		candidacy.setCandidate(candidate);
		candidacy.setCandidacies(position.getPhase().getCandidacies());
		candidacy.setDate(new Date());
		candidacy.setOpenToOtherCandidates(false);
		candidacy.setPermanent(false);
		candidacy.getProposedEvaluators().clear();
		validateCandidacy(candidacy, candidate, true); // isNew
		updateSnapshot(candidacy, candidate);

		em.persist(candidacy);
		em.flush();

		// Return Results
		candidacy = em.find(Candidacy.class, candidacy.getId());
		candidacy.getCandidacyEvalutionsDueDate();

		return candidacy;
	}

	public void deleteCandidacy(Long id, User loggedOn) throws NotFoundException, ValidationException, NotEnabledException {
		// Authenticate
		if (!loggedOn.hasActiveRole(RoleDiscriminator.ADMINISTRATOR)) {
			throw new NotEnabledException("insufficient.privileges");
		}
		// get candidacy
		final Candidacy existingCandidacy = candidacyService.getCandidacy(id, false);

		// Update
		for (FileHeader fh : existingCandidacy.getFiles()) {
			deleteCompletely(fh);
		}
		for (CandidacyEvaluator eval : existingCandidacy.getProposedEvaluators()) {
			for (FileHeader fh : eval.getFiles()) {
				deleteCompletely(fh);
			}
		}
		existingCandidacy.getCandidacies().getCandidacies().remove(existingCandidacy);
		em.remove(existingCandidacy);
		em.flush();
	}

}
