package gr.grnet.dep.service;

import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import gr.grnet.dep.service.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

@Stateless
public class ManagementService {

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

		logger.info("CREATING " + pdData.size() + " PROFESSOR DOMESTIC ACCOUNTS");
		for (ProfessorDomesticData data : pdData) {
			User u = authenticationService.findAccountByProfessorDomesticData(data);
			if (u == null) {
				logger.info("CREATING PROFESSOR DOMESTIC: " + data.getEmail());
				u = authenticationService.createProfessorDomesticAccount(data);
				logger.info("CREATING PROFESSOR DOMESTIC: " + u.getId() + " " + u.getPrimaryRole() + " " + u.getRoles().size());
			} else {
				logger.info("SKIPPING PROFESSOR DOMESTIC: " + data.getEmail());
			}
		}
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
			logger.info("massSendShibbolethConnectEmails: Empty Institutions Array");
			return;
		}
		logger.info("massSendShibbolethConnectEmails: Institutions = " + Arrays.toString(institutionIds.toArray()));
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
		logger.info("massSendShibbolethConnectEmails: Sending to " + users.size() + " users");
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
		logger.info("massSendReminderFinalizeRegistrationEmails: Sending to " + users.size() + " users");
		for (Long userId : users) {
			mailService.sendReminderFinalizeRegistrationEmail(userId, false);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void upperCaseSubject(Long subjectId) {
		Subject subject = em.find(Subject.class, subjectId);
		String newName = StringUtil.toUppercaseNoTones(subject.getName(), new Locale("el"));
		if (newName.equals(subject.getName())) {
			logger.info("upperCaseSubject - Skipping    [" + subject.getId() + "]\t" + subject.getName());
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
			logger.info("upperCaseSubject - Transfering [" + subject.getId() + "]\t" + subject.getName() + "\t-> [" + otherSubject.getId() + "]\t" + otherSubject.getName());

			int i = em.createNativeQuery("update ProfessorDomestic set subject_id = :otherSubject where subject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " ProfessorDomestic.subject");

			i = em.createNativeQuery("update ProfessorDomestic set fekSubject_id = :otherSubject where fekSubject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " ProfessorDomestic.fekSubject");

			i = em.createNativeQuery("update ProfessorForeign p set subject_id = :otherSubject where subject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " ProfessorForeign.subject");

			i = em.createNativeQuery("update Candidacy set snapshot_subject_id = :otherSubject where snapshot_subject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " Candidacy.subject");

			i = em.createNativeQuery("update Candidacy set snapshot_fekSubject_id = :otherSubject where snapshot_fekSubject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " Candidacy.fekSubject");

			i = em.createNativeQuery("update Position set subject_id = :otherSubject where subject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " Position.subject");

			i = em.createNativeQuery("update Register set subject_id = :otherSubject where subject_id = :subject")
				.setParameter("otherSubject", otherSubject.getId())
				.setParameter("subject", subject.getId())
				.executeUpdate();
			logger.info("upperCaseSubject - Updated " + i + " Candidacy.subject");

			// Delete old subject
			em.remove(subject);
		} catch (NoResultException e) {
			// Just save with uppercase letter
			logger.info("upperCaseSubject - Updating    [" + subject.getId() + "]\t" + subject.getName() + "\t-> " + newName);
			subject.setName(newName);
		}
	}
}
