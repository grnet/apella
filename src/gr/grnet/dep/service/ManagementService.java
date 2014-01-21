package gr.grnet.dep.service;

import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
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
}
