package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionCategory;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.ShibbolethInformation;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

	public User findAccountByUsername(String username) {
		try {
			return (User) em.createQuery(
				"from User u " +
					"left join fetch u.roles " +
					"where u.username = :username")
				.setParameter("username", username)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByShibbolethInfo(ShibbolethInformation shibbolethInfo) {
		try {
			return (User) em.createQuery(
				"select u from User u " +
					"where u.shibbolethInfo.remoteUser = :remoteUser")
				.setParameter("remoteUser", shibbolethInfo.getRemoteUser())
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByPermanentAuthToken(String permanentAuthToken) {
		try {
			return (User) em.createQuery(
				"select u from User u " +
					"where u.permanentAuthToken = :permanentAuthToken")
				.setParameter("permanentAuthToken", permanentAuthToken)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByProfessorDomesticData(ProfessorDomesticData data) {
		try {
			User u = (User) em.createQuery(
				"select u from User u " +
					"where u.contactInfo.email = :email")
				.setParameter("email", data.getEmail())
				.getSingleResult();
			return u;
		} catch (NoResultException e) {
			return null;
		}
	}

	public User createProfessorDomesticAccount(ShibbolethInformation shibbolethInfo) {
		// Create User from Shibboleth Fields 
		User u = new User();
		u.setAuthenticationType(AuthenticationType.SHIBBOLETH);
		u.setCreationDate(new Date());
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

		u = em.merge(u);

		return u;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public User createProfessorDomesticAccount(ProfessorDomesticData data) {
		// Create User from Data 
		User u = new User();
		u.setAuthenticationType(AuthenticationType.EMAIL);

		u.getBasicInfo().setFirstname(data.getFirstname());
		u.getBasicInfo().setLastname(data.getLastname());
		u.getBasicInfo().setFathername(data.getFathername());
		u.getContactInfo().setEmail(data.getEmail());

		u.setCreationDate(new Date());
		u.setStatus(UserStatus.ACTIVE);
		u.setStatusDate(new Date());

		// Add Primary Role
		ProfessorDomestic pd = new ProfessorDomestic();
		pd.setInstitution(data.getInstitution());
		pd.setDepartment(data.getDepartment());
		pd.setRank(data.getRank());
		pd.setFek(data.getFek());
		Subject fekSubject = new Subject();
		fekSubject.setName(data.getFekSubject());
		pd.setFekSubject(supplementSubject(fekSubject));

		pd.setStatus(RoleStatus.UNAPPROVED);
		pd.setStatusDate(new Date());
		u.addRole(pd);

		// Add Second Role : CANDIDATE
		Candidate secondRole = new Candidate();
		secondRole.setStatus(RoleStatus.UNAPPROVED);
		secondRole.setStatusDate(new Date());
		u.addRole(secondRole);

		// Save to get ID
		u = em.merge(u);
		em.flush();

		// Now Add permanentAuthToken
		u.setPermanentAuthToken(generatePermanentAuthenticationToken(u.getId(), u.getContactInfo().getEmail()));
		u.setLoginEmailSent(Boolean.FALSE);
		u = em.merge(u);

		return u;
	}

	private Subject supplementSubject(Subject subject) {
		if (subject == null || subject.getName() == null || subject.getName().trim().isEmpty()) {
			return null;
		} else {
			try {
				Subject existingSubject = (Subject) em.createQuery(
					"select s from Subject s " +
						"where s.name = :name ")
					.setParameter("name", subject.getName())
					.getSingleResult();
				return existingSubject;
			} catch (NoResultException e) {
				Subject newSubject = new Subject();
				newSubject.setName(subject.getName());
				em.persist(newSubject);
				return newSubject;
			}
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
		User u = findAccountByShibbolethInfo(shibbolethInfo);
		// Create if necessary
		if (u == null) {
			if (institution.getCategory().equals(InstitutionCategory.INSTITUTION)) {
				// Do not create account for INSTITUTION
				throw new ServiceException("email.login.required");
			}
			if (institution.getCategory().equals(InstitutionCategory.RESEARCH_CENTER)) {
				// Create research account
				u = createProfessorDomesticAccount(shibbolethInfo);
			}
		}
		// Check Status
		if (!u.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
		}
		if (!u.getAuthenticationType().equals(AuthenticationType.SHIBBOLETH)) {
			throw new ServiceException(u.getAuthenticationType().toString().toLowerCase() + ".login.required");
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

	public User connectEmailToShibbolethAccount(String permanentAuthToken, ShibbolethInformation shibbolethInfo) throws ServiceException {
		//1. Validate Shibboleth Fields
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
		// Search for existing shibboleth user
		if (findAccountByShibbolethInfo(shibbolethInfo) != null) {
			throw new ServiceException("shibboleth.account.already.exists");
		}
		User emailAccount = findAccountByPermanentAuthToken(permanentAuthToken);
		if (emailAccount == null) {
			throw new ServiceException("wrong.authentication.token");
		}
		// Check Status
		if (!emailAccount.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + emailAccount.getStatus().toString().toLowerCase());
		}
		if (!emailAccount.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
			throw new ServiceException(emailAccount.getAuthenticationType().toString().toLowerCase() + ".login.required");
		}

		try {
			// 3. Connect email account to shibboleth
			emailAccount.setAuthenticationType(AuthenticationType.SHIBBOLETH);
			emailAccount.setShibbolethInfo(shibbolethInfo);
			emailAccount.setAuthToken(generateAuthenticationToken(emailAccount.getId()));
			emailAccount = em.merge(emailAccount);

			em.flush();

			// Return result
			return emailAccount;
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new ServiceException("persistence.exception");
		}

	}

	/////////////////////////////////////////////////////////////////

	public User doEmailLogin(String permanentAuthToken) throws ServiceException {
		//1. Find User
		User u = findAccountByPermanentAuthToken(permanentAuthToken);
		if (u == null) {
			throw new ServiceException("wrong.authentication.token");
		}
		// 2. Check Status
		if (!u.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
		}
		if (!u.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
			throw new ServiceException(u.getAuthenticationType().toString().toLowerCase() + ".login.required");
		}

		try {
			// 3. Do Login
			u.setAuthToken(generateAuthenticationToken(u.getId()));
			u = em.merge(u);

			// 4. Return Result
			return u;
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new ServiceException("persistence.exception");
		}
	}

	/////////////////////////////////////////////////////////////////

	public User doUsernameLogin(String username, String password) throws ServiceException {
		// Search User
		User u = findAccountByUsername(username);
		if (u == null) {
			throw new ServiceException("login.wrong.username");
		}
		// Check Status
		if (!u.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + u.getStatus().toString().toLowerCase());
		}
		if (!u.getAuthenticationType().equals(AuthenticationType.USERNAME)) {
			throw new ServiceException(u.getAuthenticationType().toString().toLowerCase() + ".login.required");
		}
		// Check Password
		if (!u.getPassword().equals(encodePassword(password, u.getPasswordSalt()))) {
			throw new ServiceException("login.wrong.password");
		}
		try {
			// Login
			u.setAuthToken(generateAuthenticationToken(u.getId()));
			u = em.merge(u);
			// Return User
			return u;
		} catch (PersistenceException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
			throw new ServiceException("persistence.exception");
		}
	}

	/////////////////////////////////////////////////////////////////

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
			String token = new String(Base64.encodeBase64(hash), "ISO-8859-1");
			return token;
		} catch (NoSuchAlgorithmException e) {
			throw new EJBException(e);
		} catch (InvalidKeyException e) {
			throw new EJBException(e);
		} catch (UnsupportedEncodingException e) {
			throw new EJBException(e);
		}
	}
}
