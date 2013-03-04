package gr.grnet.dep.service.job;

import gr.grnet.dep.server.rest.RESTService;
import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.file.FileBody;
import gr.grnet.dep.service.model.file.FileHeader;
import gr.grnet.dep.service.util.DEPConfigurationFactory;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.configuration.ConfigurationException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RemoveNonPermanentJob implements Job {

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	private static Logger staticLogger = Logger.getLogger(RESTService.class.getName());

	static String savePath;

	static {
		try {
			savePath = DEPConfigurationFactory.getServerConfiguration().getString("files.path");
			new File(savePath).mkdirs();
		} catch (ConfigurationException e) {
			staticLogger.log(Level.SEVERE, "RESTService init: ", e);
		}
	}

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		log.info("RemoveNonPermanentJob: Starting cleanup.");
		long duration = System.currentTimeMillis();
		int removed = 0;
		// 1. Remove Candidacy
		removed = deleteCandidacies();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Candidacies");
		// 2. Remove InstitutionRegulatoryFramework
		removed = deleteInstitutionRegulatoryFrameworks();
		log.info("RemoveNonPermanentJob: Removed " + removed + " InstitutionRegulatoryFrameworks");
		// 3. Remove Position
		removed = deletePositions();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Positions");
		// 4. Remove Register
		removed = deleteRegisters();
		log.info("RemoveNonPermanentJob: Removed " + removed + " Registers");
		duration = System.currentTimeMillis() - duration;
		log.info("RemoveNonPermanentJob: Finished clean up in " + duration + " msec");
	}

	private int deleteCandidacies() {
		// May Contain files, delete files first
		@SuppressWarnings("unchecked")
		List<Candidacy> candidacies = em.createQuery(
			"select from Candidacy c where c.permanent = false")
			.getResultList();

		int i = 0;
		for (Candidacy candidacy : candidacies) {
			for (FileHeader fh : candidacy.getFiles()) {
				deleteCompletely(fh);
			}
			em.remove(candidacy);
			i++;
		}
		return i;
	}

	private int deletePositions() {
		// No files at this stage (ENTAGMENI before pressing save), we can run a bulk delete
		int i = em.createQuery(
			"delete from Position p where p.permanent = false")
			.executeUpdate();
		return i;
	}

	private int deleteInstitutionRegulatoryFrameworks() {
		// No files, we can run a bulk delete
		int i = em.createQuery(
			"delete from InstitutionRegulatoryFramework irf where irf.permanent = false")
			.executeUpdate();
		return i;
	}

	private int deleteRegisters() {
		// No files, we can run a bulk delete
		int i = em.createQuery(
			"delete from Register r where r.permanent = false")
			.executeUpdate();
		return i;
	}

	///////////////////////////////////////////////////

	private void deleteCompletely(FileHeader fh) throws RestException {
		List<FileBody> fileBodies = fh.getBodies();
		for (int i = fileBodies.size() - 1; i >= 0; i--) {
			File file = deleteFileBody(fh);
			file.delete();
		}
	}

	private <T extends FileHeader> File deleteFileBody(T fh) throws RestException {
		int size = fh.getBodies().size();
		// Delete the file itself, if possible.
		FileBody fb = fh.getCurrentBody();
		// Reference physical file
		String fullPath = savePath + File.separator + fb.getStoredFilePath();
		File file = new File(fullPath);
		// Delete
		fh.getBodies().remove(size - 1);
		fh.setCurrentBody(null);
		em.remove(fb);
		if (size > 1) {
			fh.setCurrentBody(fh.getBodies().get(size - 2));
		} else {
			em.remove(fh);
		}
		//Return physical file, should be deleted by caller of function
		return file;
	}

}
