package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;
import java.util.logging.Logger;

public class RemoveNonPermanentJob implements Job {

	private static Logger log = Logger.getLogger(RemoveNonPermanentJob.class.getName());

	@EJB
	private QuartzService service;

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		log.fine("RemoveNonPermanentJob: Starting cleanup.");
		long duration = System.currentTimeMillis();
		int removed = 0;
		// 1. Remove Candidacy
		//removed = service().deleteCandidacies();
		log.fine("RemoveNonPermanentJob: Removed " + removed + " Candidacies");
		// 2. Remove InstitutionRegulatoryFramework
		removed = service.deleteInstitutionRegulatoryFrameworks();
		log.fine("RemoveNonPermanentJob: Removed " + removed + " InstitutionRegulatoryFrameworks");
		// 3. Remove Position
		removed = service.deletePositions();
		log.fine("RemoveNonPermanentJob: Removed " + removed + " Positions");
		// 4. Remove Register
		removed = service.deleteRegisters();
		log.fine("RemoveNonPermanentJob: Removed " + removed + " Registers");
		duration = System.currentTimeMillis() - duration;
		log.fine("RemoveNonPermanentJob: Finished clean up in " + duration + " msec");
	}

}
