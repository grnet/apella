package gr.grnet.dep.service.job;

import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotifyOnClosedPositionsJob implements Job {

	private static Logger log = Logger.getLogger(OpenPositionsJob.class.getName());

	private QuartzService service;

	protected QuartzService service() {
		if (service == null) {
			try {
				InitialContext ic = new InitialContext();
				return (QuartzService) ic.lookup("java:global/DEPElections/ejbs/QuartzService");
			} catch (NamingException e) {
				throw new EJBException(e);
			}
		}
		return service;
	}

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		long duration = System.currentTimeMillis();
		log.info("NotifyOnClosedPositionsJob: Sending emails.");
		int notifications = service().notifyOnClosedPositions();
		duration = System.currentTimeMillis() - duration;
		log.info("NotifyOnClosedPositionsJob: Sent email for " + notifications + " positions in " + duration + " msec");
	}

}
