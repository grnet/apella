package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class SubjectService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public Collection<Subject> get(String query) {
        List<Subject> result;
        if (query == null) {
            result = em.createQuery("select distinct s from Subject s ", Subject.class)
                    .getResultList();
        } else {
            result = em.createQuery("select s from Subject s where s.name like :query", Subject.class)
                    .setParameter("query", query + "%")
                    .getResultList();
        }
        return result;
    }

    public Subject get(long id) throws NotFoundException {
        try {
            return em.createQuery(
                    "select i from Subject i " +
                            "where i.id = :id", Subject.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.subject.id");
        }
    }
}
