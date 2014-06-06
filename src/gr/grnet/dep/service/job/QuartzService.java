package gr.grnet.dep.service.job;

import gr.grnet.dep.service.JiraService;
import gr.grnet.dep.service.MailService;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Position;
import gr.grnet.dep.service.model.Position.PositionStatus;
import gr.grnet.dep.service.model.PositionPhase;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.model.system.Notification;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import gr.grnet.dep.service.util.DateUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class QuartzService {

	private static final String jndiName = "Quartz";
	private static final String propertiesFile = "gr/grnet/dep/service/job/quartz.properties";
	static String savePath;
	private static Logger staticLog = Logger.getLogger(QuartzService.class.getName());

	static {
		try {
			savePath = DEPConfigurationFactory.getServerConfiguration().getString("files.path");
			new File(savePath).mkdirs();
		} catch (ConfigurationException e) {
			staticLog.log(Level.SEVERE, "RESTService init: ", e);
		}
	}

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;
	@Resource
	SessionContext sc;
	@EJB
	MailService mailService;
	@EJB
	JiraService jiraService;
	@Inject
	private Logger log;
	private StdSchedulerFactory schedulerFactory;

	@PostConstruct
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void createQuartzService() throws IllegalStateException {
		log.info("Create QuartzService(" + jndiName + ")...");
		try {
			Properties properties = loadProperties(propertiesFile);
			schedulerFactory = new StdSchedulerFactory();
			schedulerFactory.initialize(properties);
			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.start();

		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to initialize Scheduler", e);
			throw new IllegalStateException("Failed to initialize Scheduler - ", e);
		}
		log.info("QuartzService(" + jndiName + ") created.");
	}

	@PreDestroy
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void destroyService() throws IllegalStateException {
		try {
			log.info("Destroy QuartzService(" + jndiName + ")...");
			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.shutdown();
			schedulerFactory = null;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to initialize Scheduler", e);
			throw new IllegalStateException("Failed to destroy Scheduler - ", e);
		}
		log.info("QuartzService(" + jndiName + ") destroyed.");
	}

	private Properties loadProperties(String propFileName) throws IOException {
		Properties props = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream == null) {
			throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
		}
		props.load(inputStream);
		return props;
	}

	////////////////////////////////////////

	public int deleteCandidacies() {
		// May Contain files, delete files first
		@SuppressWarnings("unchecked")
		List<Candidacy> candidacies = em.createQuery(
				"from Candidacy c where c.permanent is false")
				.getResultList();

		int i = 0;
		for (Candidacy candidacy : candidacies) {
			for (FileHeader fh : candidacy.getFiles()) {
				deleteCompletely(fh);
			}
			em.remove(candidacy);
			i++;
		}
		return i;
	}

	public int deletePositions() {
		// Due to triggers (PositionPhase), cannot execute bulk delete
		@SuppressWarnings("unchecked")
		List<Position> positions = em.createQuery(
				"from Position p where p.permanent is false")
				.getResultList();
		int i = 0;
		for (Position position : positions) {
			em.remove(position);
			i++;
		}
		return i;
	}

	public int deleteInstitutionRegulatoryFrameworks() {
		// No files, we can run a bulk delete
		int i = em.createQuery(
				"delete from InstitutionRegulatoryFramework irf where irf.permanent is false")
				.executeUpdate();
		return i;
	}

	public int deleteRegisters() {
		// No files, we can run a bulk delete
		int i = em.createQuery(
				"delete from Register r where r.permanent is false")
				.executeUpdate();
		return i;
	}

	public int openPositions() {
		Date now = DateUtils.truncate(new Date(), Calendar.DATE);
		@SuppressWarnings("unchecked")
		List<Position> positions = em.createQuery(
				"from Position p where " +
						"p.permanent is true " +
						"and p.phase.status = :status " +
						"and p.phase.candidacies.openingDate <= :now " +
						"and p.phase.candidacies.closingDate > :now")
				.setParameter("status", PositionStatus.ENTAGMENI)
				.setParameter("now", now)
				.getResultList();
		int i = 0;
		try {
			for (Position position : positions) {
				log.log(Level.INFO, "Opening Position " + position.getId());

				// Same code exists in AddPhase of PositionRestService
				PositionPhase existingPhase = position.getPhase();
				PositionPhase newPhase = new PositionPhase();
				newPhase.setStatus(PositionStatus.ANOIXTI);
				newPhase.setCandidacies(existingPhase.getCandidacies());
				position.addPhase(newPhase);

				i++;
			}
			em.flush();
		} catch (PersistenceException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			sc.setRollbackOnly();
		}
		return i;
	}

	///////////////////////////////////////////////////

	public int sendEmails() {
		return mailService.sendPendingEmails();
	}

	///////////////////////////////////////////////////

	public int notifyOnClosedPositions() {
		Date toDate = DateUtil.removeTime(new Date());
		@SuppressWarnings("unchecked")
		List<Position> positions = em.createQuery(
				"from Position p where " +
						"p.permanent is true " +
						"and p.phase.status = :status " +
						"and p.phase.candidacies.closingDate < :toDate " +
						"and p.id not in ( " +
						"	select n.referredEntityId " +
						"	from Notification n " +
						"	where n.type = 'position.closed' " +
						") ")
				.setParameter("status", PositionStatus.ANOIXTI)
				.setParameter("toDate", DateUtil.removeTime(toDate))
				.getResultList();

		for (final Position position : positions) {
			for (final Candidacy candidacy : position.getPhase().getCandidacies().getCandidacies()) {
				//position.update.closingDate@candidates
				mailService.postEmail(candidacy.getCandidate().getUser().getContactInfo().getEmail(),
						"default.subject",
						"position.update.closingDate@candidates",
						Collections.unmodifiableMap(new HashMap<String, String>() {

							{
								put("position", position.getName());

								put("firstname_el", candidacy.getCandidate().getUser().getFirstname("el"));
								put("lastname_el", candidacy.getCandidate().getUser().getLastname("el"));
								put("institution_el", position.getDepartment().getSchool().getInstitution().getName().get("el"));
								put("school_el", position.getDepartment().getSchool().getName().get("el"));
								put("department_el", position.getDepartment().getName().get("el"));

								put("firstname_en", candidacy.getCandidate().getUser().getFirstname("en"));
								put("lastname_en", candidacy.getCandidate().getUser().getLastname("en"));
								put("institution_en", position.getDepartment().getSchool().getInstitution().getName().get("en"));
								put("school_en", position.getDepartment().getSchool().getName().get("en"));
								put("department_en", position.getDepartment().getName().get("en"));
							}
						}));
			}
			Notification notification = new Notification();
			notification.setDate(toDate);
			notification.setReferredEntityId(position.getId());
			notification.setStatus("SENT");
			notification.setType("position.closed");
			em.persist(notification);
		}

		return positions.size();
	}

	///////////////////////////////////////////////////

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchronizeJiraIssuesJob() {
		try {
			jiraService.synchronizeIssues();
		} catch (Exception e) {
			staticLog.log(Level.SEVERE, "synchronizeJiraIssuesJob Failed", e);
		}
	}

	///////////////////////////////////////////////////

	private void deleteCompletely(FileHeader fh) {
		List<FileBody> fileBodies = fh.getBodies();
		for (int i = fileBodies.size() - 1; i >= 0; i--) {
			File file = deleteFileBody(fh);
			file.delete();
		}
	}

	private <T extends FileHeader> File deleteFileBody(T fh) {
		int size = fh.getBodies().size();
		// Delete the file itself, if possible.
		FileBody fb = fh.getCurrentBody();
		// Reference physical file
		String fullPath = savePath + File.separator + fb.getStoredFilePath();
		File file = new File(fullPath);
		// Delete
		fh.getBodies().remove(size - 1);
		fh.setCurrentBody(null);
		em.remove(fb);
		if (size > 1) {
			fh.setCurrentBody(fh.getBodies().get(size - 2));
		} else {
			em.remove(fh);
		}
		//Return physical file, should be deleted by caller of function
		return file;
	}

}
