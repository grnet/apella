package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.CandidacyEvaluator;
import gr.grnet.dep.service.model.PositionCommitteeMember;
import gr.grnet.dep.service.model.PositionEvaluator;
import gr.grnet.dep.service.model.Professor;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class ProfessorService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public Professor getProfessor(Long id) throws NotFoundException {
        // find professor
        Professor professor = em.find(Professor.class, id);

        if (professor == null) {
            throw new NotFoundException("wrong.professor.id");
        }

        return professor;
    }

    public List<PositionCommitteeMember> getCommittees(Long professorId) {
        List<PositionCommitteeMember> committees = em.createQuery(
                "select pcm from PositionCommitteeMember pcm " +
                        "left join fetch pcm.committee co " +
                        "left join fetch co.position po " +
                        "left join fetch po.phase ph " +
                        "left join fetch ph.candidacies pca " +
                        "where pcm.registerMember.professor.id = :professorId ", PositionCommitteeMember.class)
                .setParameter("professorId", professorId)
                .getResultList();

        return committees;
    }

    public List<PositionEvaluator> getEvaluations(Long professorId) {
        List<PositionEvaluator> evaluations = em.createQuery(
                "select pe from PositionEvaluator pe " +
                        "left join fetch pe.evaluation ev " +
                        "left join fetch ev.position po " +
                        "left join fetch po.phase ph " +
                        "left join fetch ph.candidacies pca " +
                        "where pe.registerMember.professor.id = :professorId ", PositionEvaluator.class)
                .setParameter("professorId", professorId)
                .getResultList();

        return evaluations;
    }

    public List<CandidacyEvaluator> getCandidacyEvaluators(Long professorId) {
        List<CandidacyEvaluator> evaluations = em.createQuery(
                "select ce from CandidacyEvaluator ce " +
                        "join ce.candidacy c " +
                        "join c.candidacies.position po " +
                        "join po.phase ph " +
                        "join ph.candidacies pca " +
                        "where ce.registerMember.professor.id = :professorId ", CandidacyEvaluator.class)
                .setParameter("professorId", professorId)
                .getResultList();

        return evaluations;
    }

}
