package gr.grnet.dep.service;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Stateless
public class CheckConnectionService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    public boolean testConnection() {
        try {
            em.createQuery(
                    "Select a.id from Administrator a ", Long.class)
                    .setMaxResults(1)
                    .getSingleResult();

            return true;
        } catch (NoResultException e) {
            return false;
        }
    }



}
