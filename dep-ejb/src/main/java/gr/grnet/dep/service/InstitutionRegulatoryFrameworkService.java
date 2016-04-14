package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Institution;
import gr.grnet.dep.service.model.InstitutionRegulatoryFramework;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

@Stateless
public class InstitutionRegulatoryFrameworkService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private InstitutionService institutionService;

    public Collection<InstitutionRegulatoryFramework> getAll() {
        return em.createQuery(
                "select i from InstitutionRegulatoryFramework i " +
                        "where i.permanent = true", InstitutionRegulatoryFramework.class)
                .getResultList();
    }

    public InstitutionRegulatoryFramework get(Long id) throws NotFoundException {
        try {
            return em.createQuery(
                    "select i from InstitutionRegulatoryFramework i " +
                            "where i.id = :id", InstitutionRegulatoryFramework.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.institutionregulatoryframework.id");
        }
    }

    public InstitutionRegulatoryFramework create(InstitutionRegulatoryFramework newIRF) throws NotFoundException, ValidationException {

        // get institution
        Institution institution = institutionService.getInstitution(newIRF.getInstitution().getId());

        // set institution
        newIRF.setInstitution(institution);

        // Validate
        try {
            em.createQuery(
                    "select irf from InstitutionRegulatoryFramework irf " +
                            "where irf.institution.id = :institutionId " +
                            "and irf.permanent = true ", InstitutionRegulatoryFramework.class)
                    .setParameter("institutionId", institution.getId())
                    .getSingleResult();
            throw new ValidationException("institutionrf.already.exists");
        } catch (NoResultException ne) {
        }
        // Create
        newIRF.setCreatedAt(new Date());
        newIRF.setUpdatedAt(new Date());
        newIRF.setPermanent(false);

        newIRF = em.merge(newIRF);
        em.flush();

        return newIRF;
    }

    public InstitutionRegulatoryFramework update(Long id, InstitutionRegulatoryFramework newIRF) throws NotFoundException {
        // get
        InstitutionRegulatoryFramework existing = get(id);

        existing.setEswterikosKanonismosURL(newIRF.getEswterikosKanonismosURL());
        existing.setOrganismosURL(newIRF.getOrganismosURL());
        existing.setUpdatedAt(new Date());
        existing.setPermanent(true);
        em.flush();

        return existing;
    }

    public void delete(Long id) throws NotFoundException {
        // get the existing entity
        InstitutionRegulatoryFramework existing = get(id);

        // remove the object
        em.remove(existing);
        em.flush();
    }


}
