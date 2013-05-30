package gr.grnet.dep.service.job;

import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendEmailsJob implements Job {

	private static Logger log = Logger.getLogger(SendEmailsJob.class.getName());

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
		log.info("SendEmailsJob: Starting ...");
		long duration = System.currentTimeMillis();
		int sent = 0;
		sent = service().sendEmails();
		log.info("SendEmailsJob: Sent " + sent + " Emails");
		duration = System.currentTimeMillis() - duration;
		log.info("SendEmailsJob: Finished in " + duration + " msec");
	}

}
