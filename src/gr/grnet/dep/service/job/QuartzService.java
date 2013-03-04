package gr.grnet.dep.service.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

@Singleton
@Startup
public class QuartzService {

	@Inject
	private Logger log;

	private static final String jndiName = "Quartz";

	private static final String propertiesFile = "gr/grnet/dep/service/job/quartz.properties";

	private StdSchedulerFactory schedulerFactory;

	@PostConstruct
	public void createQuartzService() throws SchedulerConfigException {
		log.info("Create QuartzService(" + jndiName + ")...");
		try {
			Properties properties = loadProperties(propertiesFile);
			schedulerFactory = new StdSchedulerFactory();

			schedulerFactory.initialize(properties);

			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.start();

		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to initialize Scheduler", e);
			throw new SchedulerConfigException("Failed to initialize Scheduler - ", e);
		}
		log.info("QuartzService(" + jndiName + ") created.");
	}

	@PreDestroy
	public void destroyService() throws SchedulerException {
		log.info("Destroy QuartzService(" + jndiName + ")...");
		Scheduler scheduler = schedulerFactory.getScheduler();
		scheduler.shutdown();
		schedulerFactory = null;
		log.info("QuartzService(" + jndiName + ") destroyed.");
	}

	public Scheduler getSheduler() throws SchedulerException {
		return schedulerFactory.getScheduler();
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

}
