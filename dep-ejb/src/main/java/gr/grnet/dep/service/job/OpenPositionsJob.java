package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;
import java.util.logging.Logger;

public class OpenPositionsJob implements Job {

	private static Logger log = Logger.getLogger(OpenPositionsJob.class.getName());

	@EJB
	private QuartzService service;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		log.fine("OpenPositionsJob: Starting ...");
		long duration = System.currentTimeMillis();
		int removed = 0;
		// 1. Remove Candidacy
		removed = service.openPositions();
		log.fine("OpenPositionsJob: Opened " + removed + " Positions");
		duration = System.currentTimeMillis() - duration;
		log.fine("OpenPositionsJob: Finished in " + duration + " msec");
	}

}
