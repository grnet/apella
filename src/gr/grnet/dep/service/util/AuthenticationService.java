package gr.grnet.dep.service.util;

import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.ShibbolethInformation;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.model.UserRegistrationType;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

@Stateless
public class AuthenticationService {

	@Resource
	SessionContext sc;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	@Inject
	protected Logger logger;

	@Context
	protected Providers providers;

	@EJB
	MailService mailService;

	@EJB
	JiraService jiraService;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	/////////////////////////////////////////////////////////////////

	public User doShibbolethLogin(ShibbolethInformation shibbolethInfo) throws ServiceException {
		//1. Validate
		logger.info("Read shibboleth: " + shibbolethInfo.toString());
		if (shibbolethInfo.isMissingRequiredFields()) {
			throw new ServiceException("shibboleth.fields.error");
		}
		if (!shibbolethInfo.getAffiliation().equals("faculty")) {
			throw new ServiceException("wrong.affiliation");
		}
		// Find Institution from schacHomeOrganization
		Institution institution = findInstitutionBySchacHomeOrganization(shibbolethInfo.getSchacHomeOrganization());
		if (institution == null) {
			throw new ServiceException("wrong.home.organization");
		}
		// 2. Find/Create User
		User u = null;
		try {
			u = (User) em.createQuery("select u from User u " +
				"where u.shibbolethInfo.remoteUser = :remoteUser")
				.setParameter("remoteUser", shibbolethInfo.getRemoteUser())
				.getSingleResult();
		} catch (NoResultException e) {
			// Create User from Shibboleth Fields 
			u = new User();
			u.setRegistrationType(UserRegistrationType.SHIBBOLETH);
			u.getBasicInfo().setFirstname(shibbolethInfo.getGivenName());
			u.getBasicInfo().setLastname(shibbolethInfo.getSn());
			u.getBasicInfo().setFathername("");
			u.setRegistrationDate(new Date());
			u.setStatus(UserStatus.ACTIVE);
			u.setStatusDate(new Date());
			// Add Primary Role
			ProfessorDomestic pd = new ProfessorDomestic();
			pd.setInstitution(findInstitutionBySchacHomeOrganization(shibbolethInfo.getSchacHomeOrganization()));
			pd.setStatus(RoleStatus.UNAPPROVED);
			pd.setStatusDate(new Date());
			u.addRole(pd);
			// Add Second Role : CANDIDATE
			Candidate secondRole = new Candidate();
			secondRole.setStatus(RoleStatus.UNAPPROVED);
			secondRole.setStatusDate(new Date());
			u.addRole(secondRole);
		}

		// Check Status
		if (!u.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
		}

		// 3. Persist User
		try {
			// Update login and shibboleth fields
			u.setShibbolethInfo(shibbolethInfo);
			u.setAuthToken(generateAuthenticationToken(u.getId()));
			u = em.merge(u);

			em.flush();

			// Return result
			return u;
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new ServiceException("persistence.exception");
		}

	}

	private Institution findInstitutionBySchacHomeOrganization(String schacHomeOrganization) {
		try {
			return (Institution) em.createQuery(
				"from Institution i " +
					"where i.schacHomeOrganization = :schacHomeOrganization ")
				.setParameter("schacHomeOrganization", schacHomeOrganization)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	/////////////////////////////////////////////////////////////////

	public User doEmailLogin(String token) throws ServiceException {
		try {
			//1. Find User
			User u = (User) em.createQuery("select u from User u " +
				"where u.permanentAuthToken = :token")
				.setParameter("token", token)
				.getSingleResult();

			// 2. Check Status
			if (!u.getStatus().equals(UserStatus.ACTIVE)) {
				throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
			}

			// 3. Login
			u.setAuthToken(generateAuthenticationToken(u.getId()));
			u = em.merge(u);

			// 4. Return Result
			return u;
		} catch (NoResultException e) {
			throw new ServiceException("wrong.authentication.token");
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new ServiceException("persistence.exception");
		}
	}

	/////////////////////////////////////////////////////////////////

	public User doUsernameLogin(String username, String password) throws ServiceException {
		try {
			// Search User
			User u = (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", username)
				.getSingleResult();
			// Check Status
			if (!u.getStatus().equals(UserStatus.ACTIVE)) {
				throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
			}
			// Check Password
			if (!u.getPassword().equals(encodePassword(password, u.getPasswordSalt()))) {
				throw new ServiceException("login.wrong.password");
			}
			// Login
			u.setAuthToken(generateAuthenticationToken(u.getId()));
			u = em.merge(u);
			// Return User
			return u;
		} catch (NoResultException e) {
			throw new ServiceException("login.wrong.username");
		}
	}

	/////////////////////////////////////////////////////////////////

	public String encodePassword(String password, String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest(password.concat(salt).getBytes("ISO-8859-1"));
			return new String(Base64.encodeBase64(hash), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	public String generatePassword() {
		final String LETTERS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";
		final int PASSWORD_LENGTH = 8;

		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			String pw = "";
			for (int i = 0; i < PASSWORD_LENGTH; i++) {
				int index = (int) (sr.nextDouble() * LETTERS.length());
				pw += LETTERS.substring(index, index + 1);
			}
			return pw;
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		}
	}

	public String generatePasswordSalt() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[10];
			sr.nextBytes(salt);
			return new String(Base64.encodeBase64(salt), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	/**
	 * Generates the token used after each login
	 * 
	 * @param userId
	 * @return
	 */
	private String generateAuthenticationToken(Long userId) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] hash = md.digest((userId.toString() + System.currentTimeMillis()).getBytes("ISO-8859-1"));
			return new String(Base64.encodeBase64(hash), "ISO-8859-1");
		} catch (NoSuchAlgorithmException nsae) {
			throw new EJBException(nsae);
		} catch (UnsupportedEncodingException uee) {
			throw new EJBException(uee);
		}
	}

	private static final SecretKeySpec secretKey = new SecretKeySpec("APELLA3EMAIL6LOGIN9SECRET2KEY8".getBytes(), "HmacSHA256");

	/**
	 * Generates the permanent login used for email-link-authentication
	 * 
	 * @param userId
	 * @param email
	 * @return
	 */
	public String generatePermanentAuthenticationToken(Long userId, String email) {
		String unencoded = userId + "/" + email + "/" + System.currentTimeMillis();
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			sha256_HMAC.init(secretKey);
			byte[] hash = sha256_HMAC.doFinal(unencoded.getBytes());
			String token = Base64.encodeBase64String(hash);
			return token;
		} catch (NoSuchAlgorithmException e) {
			throw new EJBException(e);
		} catch (InvalidKeyException e) {
			throw new EJBException(e);
		}
	}

}
