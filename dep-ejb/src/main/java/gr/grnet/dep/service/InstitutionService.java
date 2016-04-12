package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.School;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.logging.Logger;

@Stateless
public class InstitutionService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public Collection<Institution> getAll() {
        return em.createQuery(
                "select distinct i " +
                        "from Institution i ", Institution.class)
                .getResultList();
    }


    public Institution getInstitution(Long id) throws NotFoundException {
        // find institution
        Institution institution = em.find(Institution.class, id);

        if (institution == null) {
            throw new NotFoundException("wrong.institution.id");
        }
        return institution;
    }

    public Institution update(Long id, Institution institutionToUpdate) throws NotFoundException, ValidationException {

        Institution institution = getInstitution(id);

        if (institutionToUpdate.getName() == null ||
                StringUtils.isEmpty(institutionToUpdate.getName().get("el")) || StringUtils.isEmpty(institutionToUpdate.getName().get("en"))) {
            throw new ValidationException("names.missing");
        }
        // set new name
        institution.setName(institutionToUpdate.getName());

        return institution;
    }

    public Collection<School> getSchools(Long id) {
        return em.createQuery(
                "select distinct s " +
                        "from School s " +
                        "where s.institution.id = :id", School.class)
                .setParameter("id", id)
                .getResultList();

    }


}
