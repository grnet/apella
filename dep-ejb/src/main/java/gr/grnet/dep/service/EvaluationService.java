package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.evaluation.Evaluation;
import gr.grnet.dep.service.model.evaluation.EvaluationQuestion;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Random;

@Stateless
public class EvaluationService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}


	public User findUserById(Long userId) {
		try {
			User user = em.createQuery(
					"from User u " +
							"left join fetch u.roles " +
							"where u.status = :status " +
							"and u.id = :userId", User.class)
					.setParameter("status", User.UserStatus.ACTIVE)
					.setParameter("userId", userId)
					.getSingleResult();
			return user;
		} catch (NoResultException e) {
			return null;
		}
	}

	public Evaluation findEvaluationByUserID(Long userId) {
		try {
			Evaluation eval = em.createQuery(
					"from Evaluation eval " +
							"where eval.user.id =  :userId " +
							"and eval.year= :year ", Evaluation.class)
					.setParameter("userId", userId)
					.setParameter("year", 2014)
					.getSingleResult();
			return eval;
		} catch (NoResultException e) {
			return null;
		}
	}

	public Evaluation saveEvaluation(Evaluation eval) {
		//TODO:
		Evaluation evaluation = em.merge(eval);
		return evaluation;
	}

	public List<EvaluationQuestion> getEvaluationQuestions(Role.RoleDiscriminator role) {
		if (Role.RoleDiscriminator.PROFESSOR_FOREIGN.equals(role)) {
			role = Role.RoleDiscriminator.PROFESSOR_DOMESTIC;
		}
		List<EvaluationQuestion> questions = em.createQuery(
				"from EvaluationQuestion eval " +
						"where eval.role =  :role ", EvaluationQuestion.class)
				.setParameter("role", role)
				.getResultList();
		return questions;
	}

	public EvaluationQuestion getEvaluationQuestion(Long questionId) {
		EvaluationQuestion question = em.find(EvaluationQuestion.class, questionId);
		return question;
	}

	// Encryption Functions:

	public static Long decryptID(String encryptedUserId) {
		if (encryptedUserId.length() != 24) {
			throw new IllegalArgumentException("Invalid cipher text");
		}
		try {
			int salt = (int) Long.parseLong(encryptedUserId.substring(0, 8), 16);
			long cipher = Long.parseLong(encryptedUserId.substring(8, 16), 16) << 32 | Long.parseLong(encryptedUserId.substring(16), 16);
			long otp = generatePad(salt);
			return cipher ^ otp;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex value: " + e.getMessage());
		}
	}

	public static String encryptID(Long id) {
		int salt = (new Random()).nextInt();
		long otp = generatePad(salt);
		long cipher = id ^ otp;
		return String.format("%08x%016x", salt, cipher);
	}

	private static final String topSecret = "56rty4qw23b4e85ersye";

	private static long generatePad(int salt) {
		String saltString = Integer.toString(salt);
		String lpad = saltString + topSecret;
		String rpad = topSecret + saltString;
		return ((long) lpad.hashCode()) << 32 | (long) rpad.hashCode();
	}


	public static void main(String[] args) {
		System.out.println("32 : " + encryptID(32l));
		System.out.println("30a4d55a9f44845a0b7271fc : " + decryptID("30a4d55a9f44845a0b7271fc"));
	}
}
