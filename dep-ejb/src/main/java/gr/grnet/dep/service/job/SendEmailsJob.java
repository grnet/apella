package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;
import java.util.logging.Logger;

public class SendEmailsJob implements Job {

	private static Logger log = Logger.getLogger(SendEmailsJob.class.getName());

	@EJB
	private QuartzService service;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		log.fine("SendEmailsJob: Starting ...");
		long duration = System.currentTimeMillis();
		int sent = 0;
		sent = service.sendEmails();
		log.fine("SendEmailsJob: Sent " + sent + " Emails");
		duration = System.currentTimeMillis() - duration;
		log.fine("SendEmailsJob: Finished in " + duration + " msec");
	}

}
