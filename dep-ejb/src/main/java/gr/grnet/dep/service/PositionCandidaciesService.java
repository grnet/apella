package gr.grnet.dep.service;

import gr.grnet.dep.service.model.file.PositionCandidaciesFile;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class PositionCandidaciesService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;


    public  List<PositionCandidaciesFile> getPositionCandidaciesFiles(Long existingCandidaciesId, Long candidacyId) {

        List<PositionCandidaciesFile> result = em.createQuery(
                "select pcf from PositionCandidaciesFile pcf " +
                        "where pcf.deleted = false " +
                        "and pcf.candidacies.id = :pcId " +
                        "and pcf.evaluator.candidacy.id = :cid ", PositionCandidaciesFile.class)
                .setParameter("pcId", existingCandidaciesId)
                .setParameter("cid", candidacyId)
                .getResultList();

        return result;
    }



}
