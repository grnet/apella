package gr.grnet.dep.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;

public class RemoveNonPermanentJob implements Job {

	private static Logger log = Logger.getLogger(RemoveNonPermanentJob.class.getName());

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
		log.info("RemoveNonPermanentJob: Starting cleanup.");
		long duration = System.currentTimeMillis();
		int removed = 0;
		// 1. Remove Candidacy
		//removed = service().deleteCandidacies();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Candidacies");
		// 2. Remove InstitutionRegulatoryFramework
		removed = service().deleteInstitutionRegulatoryFrameworks();
		log.info("RemoveNonPermanentJob: Removed " + removed + " InstitutionRegulatoryFrameworks");
		// 3. Remove Position
		removed = service().deletePositions();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Positions");
		// 4. Remove Register
		removed = service().deleteRegisters();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Registers");
		duration = System.currentTimeMillis() - duration;
		log.info("RemoveNonPermanentJob: Finished clean up in " + duration + " msec");
	}

}
