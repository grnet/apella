package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Sector;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class SectorService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;


    public List<Sector> getAll() {
        List<Sector> sectorList = em.createQuery("from Sector s " +
                "left join fetch s.name sname " +
                "order by s.areaId, s.subjectId", Sector.class)
                .getResultList();

        return sectorList;
    }

    public Sector get(Long id) throws NotFoundException {

        Sector sector = em.find(Sector.class, id);

        if (sector == null) {
            throw new NotFoundException("wrong.sector.id");
        }

        return sector;
    }

    public Sector create(Sector sector) throws ValidationException {
        // validate
        validate(sector);

        // get the max results from the respective fields in order to create the new sectors
        Map<String, Long> map = em.createQuery("Select new MAP ( MAX(s.id) as sector_id, MAX(s.subjectId) as subject_id) " +
                "from Sector s", Map.class)
                .getSingleResult();

        Long maxId = map.get("sector_id");
        Long maxSubjectId = map.get("subject_id");

        sector.setId(maxId + 1);
        sector.setSubjectId(maxSubjectId + 1);

        em.persist(sector);
        em.flush();

        return sector;
    }

    public Sector update(Long id, Sector sectorToUpdate) throws NotFoundException, ValidationException {
        // get sector
        Sector sector = get(id);
        // check if sector is linked to position
        boolean isSectorLinkedToPosition = isSectorLinkedToPosition(sector.getId());

        if (isSectorLinkedToPosition) {
            throw new ValidationException("edit.sector.assigned.to.position");
        }

        boolean isSectorLinkedToPositionSearchCriteria = isSectorLinkedToPositionSearchCriteria(sector.getId());

        if (isSectorLinkedToPositionSearchCriteria) {
            throw new ValidationException("edit.sector.assigned.to.position");
        }

        //validate
        validate(sectorToUpdate);

        sector.setName(sectorToUpdate.getName());
        sector.setAreaId(sectorToUpdate.getAreaId());
        sector.setSubjectId(sectorToUpdate.getSubjectId());

        return sector;
    }

    public void delete(Long id) throws ValidationException, NotFoundException {
        // get sector
        Sector sector = get(id);
        // check if sector is linked to position
        boolean isSectorLinkedToPosition = isSectorLinkedToPosition(sector.getId());

        if (isSectorLinkedToPosition) {
            throw new ValidationException("delete.sector.assigned.to.position");
        }

        boolean isSectorLinkedToPositionSearchCriteria = isSectorLinkedToPositionSearchCriteria(sector.getId());

        if (isSectorLinkedToPositionSearchCriteria) {
            throw new ValidationException("delete.sector.assigned.to.position");
        }

        em.remove(sector);
        em.flush();
    }



    private void validate(Sector sector) throws ValidationException {

        String[] locales = { "el", "en" };

        boolean validationPassed = sector.getName() != null && sector.getName().get(locales[0]) != null && sector.getName().get(locales[1]) != null;

        if (validationPassed) {
            // check inside properties
            for (String locale : locales) {
                validationPassed = StringUtils.isNotEmpty(sector.getName().get(locale).getArea()) && StringUtils.isNotEmpty(sector.getName().get(locale).getSubject());
                if (!validationPassed) {
                    break;
                }
            }
        }

        if (!validationPassed) {
            throw new ValidationException("names.missing");
        }
    }

    private boolean isSectorLinkedToPosition(Long sectorId) {
        try {
            em.createQuery("Select p.id from Position p " +
                    "where p.sector.id = :sectorId")
                    .setParameter("sectorId", sectorId)
                    .setMaxResults(1)
                    .getSingleResult();

            return true;

        } catch (NoResultException e) {
            return false;
        }
    }

    private boolean isSectorLinkedToPositionSearchCriteria(Long sectorId) {
        try {
            em.createQuery("Select distinct u.id " +
                    "from PositionSearchCriteria u join u.sectors s " +
                    "where s.id = :sectorId ", Long.class)
                    .setParameter("sectorId", sectorId)
                    .setMaxResults(1)
                    .getSingleResult();

            return true;

        } catch (NoResultException e) {
            return false;
        }
    }


}
