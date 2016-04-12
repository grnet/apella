package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.*;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class PositionSearchService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    @EJB
    private CandidateService candidateService;

    @EJB
    private UtilityService utilityService;

    public PositionSearchCriteria get(Long id) throws NotFoundException {

        PositionSearchCriteria criteria = em.find(PositionSearchCriteria.class, id);

        if (criteria == null) {
            throw new NotFoundException("wrong.position.search.criteria.id");
        }

        return criteria;
    }

    public PositionSearchCriteria getByCandidate(Long candidateId) {
        try {
            PositionSearchCriteria criteria = em.createQuery(
                    "from PositionSearchCriteria c " +
                            "where c.candidate.id = :candidateId ", PositionSearchCriteria.class)
                    .setParameter("candidateId", candidateId)
                    .getSingleResult();

            return criteria;
        } catch (NoResultException e) {
            return new PositionSearchCriteria();
        }
    }


    public List<Position> search(PositionSearchCriteria criteria, User loggedOn) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = sdf.parse(sdf.format(new Date()));

        // Prepare Query
        Collection<Long> departmentIds = new ArrayList<Long>();
        departmentIds.add(-1L);

        for (Department department : criteria.getDepartments()) {
            departmentIds.add(department.getId());
        }

        Collection<Long> sectorIds = new ArrayList<Long>();
        sectorIds.add(-1L);

        for (Sector sector : criteria.getSectors()) {
            sectorIds.add(sector.getId());
        }

        String queryString = "from Position p " +
                "left join fetch p.assistants asts " +
                "left join fetch asts.roles astsr " +
                "where p.permanent = true " +
                "and p.phase.candidacies.closingDate >= :today ";

        if (departmentIds.size() > 1 || sectorIds.size() > 1) {
            queryString += "and (" +
                    "	p.department.id in (:departmentIds) " +
                    "	or p.sector.id in (:sectorIds) " +
                    ")";
        }

        TypedQuery<Position> query = em.createQuery(queryString, Position.class)
                .setParameter("today", today);

        if (departmentIds.size() > 1 || sectorIds.size() > 1) {
            query = query
                    .setParameter("departmentIds", departmentIds)
                    .setParameter("sectorIds", sectorIds);
        }

        // Execute Query
        List<Position> positions = query.getResultList();

        // Calculate CanSubmitCandidacy => set false if already submitted
        if (loggedOn.hasActiveRole(Role.RoleDiscriminator.CANDIDATE)) {
            Candidate candidate = (Candidate) loggedOn.getActiveRole(Role.RoleDiscriminator.CANDIDATE);
            for (Candidacy candidacy : candidate.getCandidacies()) {
                if (candidacy.isPermanent() && !candidacy.isWithdrawn()) {
                    for (Position position : positions) {
                        if (position.getId().equals(candidacy.getCandidacies().getPosition().getId())) {
                            position.setCanSubmitCandidacy(Boolean.FALSE);
                            break;
                        }
                    }
                }
            }
        }
        for (Position position : positions) {
            if (!position.getPhase().getClientStatus().equals(Position.PositionStatus.ANOIXTI.toString())) {
                position.setCanSubmitCandidacy(Boolean.FALSE);
            }
        }

        // Return Result
        return positions;
    }

    public PositionSearchCriteria create(PositionSearchCriteria newCriteria, Long candidateId) throws NotFoundException, ValidationException {
        // find candidate
        Candidate candidate = candidateService.getCandidate(candidateId);

        try {
            em.createQuery(
                    "from PositionSearchCriteria c " +
                            "where c.candidate.id = :candidateId ", PositionSearchCriteria.class)
                    .setParameter("candidateId", candidate.getId())
                    .getSingleResult();

            throw new ValidationException("already.exists");

        } catch (NoResultException e) {
            Collection<Department> departments = utilityService.supplementDepartments(newCriteria.getDepartments());
            Collection<Sector> sectors = utilityService.supplementSectors(newCriteria.getSectors());

            newCriteria.setCandidate(candidate);
            newCriteria.getDepartments().clear();
            newCriteria.getDepartments().addAll(departments);
            newCriteria.getSectors().clear();
            newCriteria.getSectors().addAll(sectors);

            newCriteria = em.merge(newCriteria);
            em.flush();

            return newCriteria;
        }
    }

    public PositionSearchCriteria update(Long id, PositionSearchCriteria newCriteria) throws NotFoundException {

        // get the object
        PositionSearchCriteria criteria = get(id);

        Collection<Department> departments = utilityService.supplementDepartments(newCriteria.getDepartments());
        Collection<Sector> sectors = utilityService.supplementSectors(newCriteria.getSectors());

        criteria.getDepartments().clear();
        criteria.getDepartments().addAll(departments);
        criteria.getSectors().clear();
        criteria.getSectors().addAll(sectors);

        em.flush();

        return criteria;
    }

}
