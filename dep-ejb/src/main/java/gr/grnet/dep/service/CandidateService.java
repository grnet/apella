package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Candidacy;
import gr.grnet.dep.service.model.Candidate;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.logging.Logger;

@Stateless
public class CandidateService extends CommonService {

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

    public List<Candidacy> getCandidaciesForSpecificCandidate(Long candidateId, String open, User loggedOn) throws NotFoundException, NotEnabledException {
        // get candidate
        Candidate candidate = getCandidate(candidateId);

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR) &&
                !candidate.getUser().getId().equals(loggedOn.getId())) {
            throw new NotEnabledException("insufficient.privileges");
        }

        String queryString = "from Candidacy c " +
                "left join fetch c.candidate.user.roles cerls " +
                "left join fetch c.candidacies ca " +
                "left join fetch ca.position po " +
                "where c.candidate.id = :candidateId " +
                "and c.permanent = true ";

        if (open != null) {
            queryString += " and c.candidacies.closingDate >= :now";
        }

        // Hide withdrawn from candidate
        if (candidate.getUser().getId().equals(loggedOn.getId())) {
            queryString += " and c.withdrawn = false";
        }
        TypedQuery<Candidacy> query = em.createQuery(queryString, Candidacy.class)
                .setParameter("candidateId", candidate.getId());

        if (open != null) {
            Date now = new Date();
            query.setParameter("now", now);
        }

        List<Candidacy> retv = query.getResultList();
        List<Long> candidaciesThatCanAddEvaluators = canAddEvaluators(retv);
        List<Long> candidaciesThatNominationCommitteeConverged = hasNominationCommitteeConverged(retv);

        for (Candidacy c : retv) {
            c.setNominationCommitteeConverged(candidaciesThatNominationCommitteeConverged.contains(c.getId()));
            c.setCanAddEvaluators(candidaciesThatCanAddEvaluators.contains(c.getId()));
        }

        return retv;
    }

    private List<Long> hasNominationCommitteeConverged(List<Candidacy> candidacies) {
        Set<Long> ids = new HashSet<Long>();
        for (Candidacy c : candidacies) {
            ids.add(c.getId());
        }
        if (ids.isEmpty()) {
            return new ArrayList<Long>();
        }
        List<Long> data = em.createQuery(
                "select c.id from Candidacy c " +
                        "join c.candidacies.position.phase.nomination no " +
                        "where no.nominationCommitteeConvergenceDate IS NOT NULL " +
                        "and no.nominationCommitteeConvergenceDate < :now " +
                        "and c.id in (:ids) ", Long.class)
                .setParameter("ids", ids)
                .setParameter("now", new Date())
                .getResultList();
        return data;
    }

    private List<Long> canAddEvaluators(List<Candidacy> candidacies) {
        Set<Long> ids = new HashSet<Long>();
        for (Candidacy c : candidacies) {
            ids.add(c.getId());
        }
        if (ids.isEmpty()) {
            return new ArrayList<Long>();
        }
        List<Long> data = em.createQuery(
                "select c.id from Candidacy c " +
                        "join c.candidacies.position.phase.committee co " +
                        "where co.members IS NOT EMPTY " +
                        "and co.candidacyEvalutionsDueDate >= :now " +
                        "and c.id in (:ids) ", Long.class)
                .setParameter("ids", ids)
                .setParameter("now", new Date())
                .getResultList();
        return data;
    }


}
