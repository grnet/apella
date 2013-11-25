package gr.grnet.dep.service.util;

import java.util.logging.Logger;

import javax.annotation.Resource;
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

	}

}
