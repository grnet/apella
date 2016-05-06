package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;
import java.util.logging.Logger;

public class SynchronizeJiraIssuesJob implements Job {

	private static Logger log = Logger.getLogger(SynchronizeJiraIssuesJob.class.getName());

	@EJB
	private QuartzService service;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		log.fine("SynchronizeJiraIssuesJob: Starting.");
		long duration = System.currentTimeMillis();
		int removed = 0;
		// 1. Remove Candidacy
		service.synchronizeJiraIssuesJob();
		duration = System.currentTimeMillis() - duration;
		log.fine("SynchronizeJiraIssuesJob: Finished in " + duration + " msec");
	}

}
