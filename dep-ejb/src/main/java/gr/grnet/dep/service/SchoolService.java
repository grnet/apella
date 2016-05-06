package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotEnabledException;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.School;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class SchoolService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    public List<School> getAll() {

        List<School> schoolList = em.createQuery(
                "select distinct i " +
                        "from School i ", School.class)
                .getResultList();

        return schoolList;
    }

    public School get(Long id) throws NotFoundException {
        try {
            School school = em.createQuery(
                    "select i from School i " +
                            "where i.id = :id", School.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return school;

        } catch (NoResultException e) {
            throw new NotFoundException("wrong.school.id");
        }
    }

    public School update(Long id, School schoolToUpdate, User loggedOn) throws NotEnabledException, NotFoundException, ValidationException {

        if (!loggedOn.hasActiveRole(Role.RoleDiscriminator.ADMINISTRATOR)) {
            throw new NotEnabledException("insufficient.privileges");
        }

        // get school
        School school = get(id);

        if (schoolToUpdate.getName() == null ||
                StringUtils.isEmpty(schoolToUpdate.getName().get("el")) || StringUtils.isEmpty(schoolToUpdate.getName().get("en"))) {
            throw new ValidationException("names.missing");
        }

        school.setName(schoolToUpdate.getName());

        return school;
    }

    public List<Department> getDepartments(Long id) {

        List<Department> departments = em.createQuery(
                "Select d from Department d " +
                        "where d.school.id = :id", Department.class)
                .setParameter("id", id)
                .getResultList();

        return departments;
    }

}
