package gr.grnet.dep.service.job;

import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RemoveNonPermanentJob implements Job {

	@Inject
	private Logger log;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		//TODO: Actually Remove Non-Permanent Entries
		Date fireTime = ctx.getFireTime();
		Date previousFireTime = ctx.getPreviousFireTime();
		if (previousFireTime == null) {
			//First time this is null, we do nothing.
			return;
		}
		long duration = System.currentTimeMillis();
		log.info("NotifyOnPendingExchangesJob: Sending emails.");
		duration = System.currentTimeMillis() - duration;
		log.info("NotifyOnPendingExchangesJob: Sent in " + duration + " msec");
	}

}
