package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.AuthenticationType;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionCategory;
import gr.grnet.dep.service.model.ProfessorDomestic;
import gr.grnet.dep.service.model.ProfessorDomesticData;
import gr.grnet.dep.service.model.Role.RoleDiscriminator;
import gr.grnet.dep.service.model.Role.RoleStatus;
import gr.grnet.dep.service.model.ShibbolethInformation;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.User.UserStatus;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import javax.annotation.Resource;
import javax.crypto.Cipher;
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
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	@EJB
	UtilityService utilityService;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	public User findAccountByAuthenticationToken(String authToken) {
		try {
			User user = em.createQuery(
					"from User u " +
							"left join fetch u.roles " +
							"where u.status = :status " +
							"and u.authToken = :authToken", User.class)
					.setParameter("status", UserStatus.ACTIVE)
					.setParameter("authToken", authToken)
					.getSingleResult();
			return user;
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByUsername(String username) {
		try {
			return em.createQuery(
					"from User u " +
							"left join fetch u.roles rls " +
							"where u.username = :username", User.class)
					.setParameter("username", username)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByShibbolethInfo(ShibbolethInformation shibbolethInfo) {
		try {
			return em.createQuery(
					"select u from User u " +
							"left join fetch u.roles rls " +
							"where u.shibbolethInfo.remoteUser = :remoteUser", User.class)
					.setParameter("remoteUser", shibbolethInfo.getRemoteUser())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByPermanentAuthToken(String permanentAuthToken) {
		try {
			return em.createQuery(
					"select u from User u " +
							"left join fetch u.roles " +
							"where u.permanentAuthToken = :permanentAuthToken", User.class)
					.setParameter("permanentAuthToken", permanentAuthToken)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public User findAccountByProfessorDomesticData(ProfessorDomesticData data) {
		try {
			User u = em.createQuery(
					"select u from User u " +
							"left join fetch u.roles rls " +
							"where u.contactInfo.email = :email", User.class)
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
		pd.setFekSubject(utilityService.supplementSubject(fekSubject));

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
			throw new ServiceException("login.account.status." + u.getStatus());
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
		//1. Read and Validate Shibboleth Fields
		logger.info("Read shibboleth: " + shibbolethInfo.toString());
		if (shibbolethInfo.isMissingRequiredFields()) {
			throw new ServiceException("shibboleth.fields.error");
		}
		if (!shibbolethInfo.getAffiliation().equals("faculty")) {
			throw new ServiceException("wrong.affiliation");
		}
		// Find Institution from schacHomeOrganization
		Institution shibbolethInstitution = findInstitutionBySchacHomeOrganization(shibbolethInfo.getSchacHomeOrganization());
		if (shibbolethInstitution == null) {
			throw new ServiceException("wrong.home.organization");
		}

		// 2. Search for existing shibboleth user
		if (findAccountByShibbolethInfo(shibbolethInfo) != null) {
			throw new ServiceException("shibboleth.account.already.exists");
		}

		// 3. Search for existing email user
		User emailAccount = findAccountByPermanentAuthToken(permanentAuthToken);
		if (emailAccount == null) {
			throw new ServiceException("wrong.authentication.token");
		}
		if (!emailAccount.getStatus().equals(UserStatus.ACTIVE)) {
			throw new ServiceException("login.account.status." + emailAccount.getStatus());
		}
		if (!emailAccount.getPrimaryRole().equals(RoleDiscriminator.PROFESSOR_DOMESTIC)) {
			throw new ServiceException("login.account.profile." + emailAccount.getPrimaryRole());
		}
		if (!emailAccount.getAuthenticationType().equals(AuthenticationType.EMAIL)) {
			throw new ServiceException(emailAccount.getAuthenticationType().toString().toLowerCase() + ".login.required");
		}
		// 4. Compare email and shibboleth fields
		logger.info("Connecting: [" + emailAccount.getId() + "]" + emailAccount.getPrimaryRole() + " to " + shibbolethInfo.toString());
		Institution emailUserInstitution = ((ProfessorDomestic) emailAccount.getRole(RoleDiscriminator.PROFESSOR_DOMESTIC)).getInstitution();
		if (emailUserInstitution == null || !emailUserInstitution.getId().equals(shibbolethInstitution.getId())) {
			throw new ServiceException("mismatch.home.organization");
		}

		// 5. Connect email account to shibboleth
		emailAccount.setAuthenticationType(AuthenticationType.SHIBBOLETH);
		emailAccount.setShibbolethInfo(shibbolethInfo);
		emailAccount.setAuthToken(generateAuthenticationToken(emailAccount.getId()));
		emailAccount = em.merge(emailAccount);
		// 6. Return result
		return emailAccount;
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
			throw new ServiceException("login.account.status." + u.getStatus());
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
			throw new ServiceException("login.account.status." + u.getStatus());
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

	public User getLoggedOn(String authToken) throws ServiceException {
		if (authToken == null) {
			throw new ServiceException("login.missing.token");
		}
		if (!isValidAuthenticationToken(authToken)) {
			throw new ServiceException("login.invalid.token");
		}
		User user = findAccountByAuthenticationToken(authToken);
		if (user == null) {
			throw new ServiceException("login.invalid.token");
		}
		return user;

	}

	public void logout(String authToken) throws ServiceException {
		if (authToken == null) {
			throw new ServiceException("login.missing.token");
		}
		User user = findAccountByAuthenticationToken(authToken);
		if (user == null) {
			throw new ServiceException("login.invalid.token");
		}
		user.setAuthToken(null);
	}

	/////////////////////////////////////////////////////////////////

	private Institution findInstitutionBySchacHomeOrganization(String schacHomeOrganization) {
		try {
			return em.createQuery(
					"from Institution i " +
							"where i.schacHomeOrganization = :schacHomeOrganization ", Institution.class)
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

	////////////////////////////////////////////////////////////////

	private static final long MAX_AGE = 7200000L; //2 Hours  2*60*60*1000 = 7.200.000

	private static byte[] tempAuthTokenSecretKey = {
			0x76, 0x69, 0x59, 0x43, 0x68, 0x71, 0x40, 0x54, 0x62, 0x52, 0x12, 0x25, 0x54, 0x3b, 0x35, 0x39
	};//"thisIsASecretKey";

	/**
	 * Generates the token used after each login
	 *
	 * @param userId
	 * @return
	 */
	private String generateAuthenticationToken(Long userId) {
		String strToEncrypt = userId + "/" + Long.toString(new SecureRandom().nextLong()) + "/" + System.currentTimeMillis();
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			final SecretKeySpec secretKey = new SecretKeySpec(tempAuthTokenSecretKey, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			final String encryptedString = new String(Base64.encodeBase64(cipher.doFinal(strToEncrypt.getBytes()), false, true), "ISO-8859-1");
			return encryptedString;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean isValidAuthenticationToken(String authToken) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			final SecretKeySpec secretKey = new SecretKeySpec(tempAuthTokenSecretKey, "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(authToken)));
			String[] tokens = decryptedString.split("/");
			if (tokens.length < 3) {
				return false;
			}
			long tokenTime = Long.parseLong(tokens[2]);
			if (System.currentTimeMillis() - tokenTime > MAX_AGE) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static final SecretKeySpec permanentAuthTokenSecretKey = new SecretKeySpec("APELLA3EMAIL6LOGIN9SECRET2KEY8".getBytes(), "HmacSHA256");

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
			sha256_HMAC.init(permanentAuthTokenSecretKey);
			byte[] hash = sha256_HMAC.doFinal(unencoded.getBytes());
			String token = new String(Base64.encodeBase64(hash, false, true), "ISO-8859-1");
			return token;
		} catch (NoSuchAlgorithmException e) {
			throw new EJBException(e);
		} catch (InvalidKeyException e) {
			throw new EJBException(e);
		} catch (UnsupportedEncodingException e) {
			throw new EJBException(e);
		}
	}


	/////////
	public static void main(String[] args) {
		String passwd = "anglen";
		String passwordsalt="Smn8RBDY5lWHHQ==";


		AuthenticationService as = new AuthenticationService();
		String endoded = as.encodePassword(passwd, passwordsalt);

		System.out.println("" + endoded);

	}
}
