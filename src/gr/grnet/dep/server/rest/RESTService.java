package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.NoLogWebApplicationException;

public class RESTService {

	@PersistenceContext(unitName = "depdb")
	protected EntityManager em;

	@Inject
	protected Logger logger;

	protected static final String TOKEN_HEADER = "X-Auth-Token";

	protected User getLoggedOn(String authToken) throws NoLogWebApplicationException {

		if (authToken == null) {
			throw new NoLogWebApplicationException(Status.UNAUTHORIZED);
		}

		try {
			User user = (User) em.createQuery(
				"from User u left join fetch u.roles " +
					"where u.active=true " +
					"and u.authToken = :authToken")
				.setParameter("authToken", authToken)
				.getSingleResult();

			return user;
		} catch (NoResultException e) {
			throw new NoLogWebApplicationException(Status.UNAUTHORIZED);
		}
	}

}
