package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Rank;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class RankService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public List<Rank> getAll() {
        List<Rank> ranks = em.createQuery(
                "select distinct r " +
                        "from Rank r ", Rank.class)
                .getResultList();

        return ranks;
    }


    public Rank get(Long id) throws NotFoundException {
        try {
            Rank rank = em.createQuery(
                    "select r from Rank r " +
                            "where r.id = :id", Rank.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return rank;
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.rank.id");
        }
    }

}
