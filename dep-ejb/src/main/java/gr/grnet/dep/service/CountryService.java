package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Country;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class CountryService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public List<Country> getAll() {

        List<Country> countries = em.createQuery("Select c " +
                "from Country c " +
                "order by c.name, c.code", Country.class)
                .getResultList();

        return countries;
    }


}
