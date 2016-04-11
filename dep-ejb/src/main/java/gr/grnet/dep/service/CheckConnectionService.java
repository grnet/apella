package gr.grnet.dep.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@Stateless
public class CheckConnectionService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

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
