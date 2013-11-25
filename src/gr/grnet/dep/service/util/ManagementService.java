package gr.grnet.dep.service.util;

import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.User;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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

	@Inject
	protected Logger logger;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	public void massCreateProfessorDomesticAccounts() {
		List<ProfessorDomesticData> pdData = em.createQuery(
			"select pdd from ProfessorDomesticData pdd")
			.getResultList();

		for (ProfessorDomesticData data : pdData) {
			User u = authenticationService.createProfessorDomesticAccount(data);
			logger.info("CREATED PROFESSOR DOMESTIC: " + u.getId() + " " + u.getPrimaryRole() + " " + u.getRoles().size());
		}
	}

}
