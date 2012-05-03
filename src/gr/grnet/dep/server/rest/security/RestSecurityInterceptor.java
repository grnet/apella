package gr.grnet.dep.server.rest.security;

import gr.grnet.dep.server.rest.UserRESTService;
import gr.grnet.dep.service.model.User;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class RestSecurityInterceptor implements PreProcessInterceptor, AcceptedByMethod {

	public static final String TOKEN_HEADER = "X-Auth-Token";

	@PersistenceContext(unitName = "depdb")
	private EntityManager em;

	@Inject
	private Logger logger;

	@Override
	/**
	 * If this method returns true, Resteasy will add this interceptor to the JAX-RS method's call chain.
	 * If it returns false then it won't be added to the call chain.
	 */
	public boolean accept(@SuppressWarnings("rawtypes") Class declaring, Method method) {
		if (!declaring.equals(UserRESTService.class))
			return true;
		// It is UserRESTService
		try {
			// Allow UserRESTService.login method to pass through unauthenticated
			Method loginMethod = UserRESTService.class.getMethod("login", new Class<?>[] {String.class, String.class});
			// Allow UserRESTService.create method to pass through unauthenticated
			Method createMethod = UserRESTService.class.getMethod("create", new Class<?>[] {User.class});
			// Allow UserRESTService.verify method to pass through unauthenticated
			Method verifyMethod = UserRESTService.class.getMethod("verify", new Class<?>[] {User.class});
			if (method.equals(loginMethod) || method.equals(createMethod) || method.equals(verifyMethod))
				return false;
		} catch (SecurityException e) {
			logger.log(Level.SEVERE, "", e);
		} catch (NoSuchMethodException e) {
			logger.log(Level.SEVERE, "", e);
		}
		return true;
	}

	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws UnauthorizedException {

		String authToken = null;

		HttpHeaders headers = request.getHttpHeaders();
		if (headers == null) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}

		List<String> tokens = headers.getRequestHeader(TOKEN_HEADER);
		if (tokens != null) {
			authToken = tokens.get(0);
		}

		if (authToken == null && headers.getCookies().containsKey("_dep_a")) {
			authToken = headers.getCookies().get("_dep_a").getValue();
		}

		if (authToken == null) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}

		try {
			User user = (User) em.createQuery(
				"from User u left join fetch u.roles " +
					"where u.active=true " +
					"and u.authToken = :authToken")
				.setParameter("authToken", authToken)
				.getSingleResult();
			request.setAttribute("user", user);
			return null;
		} catch (NoResultException e) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}
}
