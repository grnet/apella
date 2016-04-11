package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Candidate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@Stateless
public class CandidateService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;


    public Candidate getCandidate(Long id) throws NotFoundException {

        Candidate candidate = em.find(Candidate.class, id);

        if (candidate == null) {
            throw new NotFoundException("wrong.candidate.id");
        }

        return candidate;
    }


}
