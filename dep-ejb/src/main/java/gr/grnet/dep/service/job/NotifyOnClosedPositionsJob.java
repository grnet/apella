package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;
import java.util.logging.Logger;

public class NotifyOnClosedPositionsJob implements Job {

	private static Logger log = Logger.getLogger(OpenPositionsJob.class.getName());

	@EJB
	private QuartzService service;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		long duration = System.currentTimeMillis();
		log.info("NotifyOnClosedPositionsJob: Sending emails.");
		int notifications = service.notifyOnClosedPositions();
		duration = System.currentTimeMillis() - duration;
		log.info("NotifyOnClosedPositionsJob: Sent email for " + notifications + " positions in " + duration + " msec");
	}

}
