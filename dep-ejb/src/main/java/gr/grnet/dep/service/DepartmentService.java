package gr.grnet.dep.service;

import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.exceptions.ValidationException;
import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.logging.Logger;

@Stateless
public class DepartmentService extends CommonService {

    @PersistenceContext(unitName = "apelladb")
    protected EntityManager em;

    @Inject
    private Logger log;

    public Collection<Department> getAll() {
        return em.createQuery(
                "select distinct d from Department d " +
                        "left join fetch d.name dname " +
                        "left join fetch d.school s " +
                        "left join fetch s.name sname " +
                        "left join fetch s.institution i " +
                        "left join fetch i.name iname ", Department.class)
                .getResultList();
    }

    public Department get(Long id) throws NotFoundException {
        try {
            return em.createQuery(
                    "select distinct d from Department d " +
                            "left join fetch d.name dname " +
                            "left join fetch d.school s " +
                            "left join fetch s.name sname " +
                            "left join fetch s.institution i " +
                            "left join fetch i.name iname " +
                            "where d.id = :id", Department.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("wrong.department.id");
        }
    }

    public Department update(long id, Department departmentToUpdate) throws ValidationException, NotFoundException {
        // get Department
        Department department = em.find(Department.class, id);

        if (department == null) {
            throw new NotFoundException("wrong.department.id");
        }

        if (departmentToUpdate.getName() == null ||
                StringUtils.isEmpty(departmentToUpdate.getName().get("el")) || StringUtils.isEmpty(departmentToUpdate.getName().get("en"))) {
            throw new ValidationException("names.missing");
        }

        // update name
        department.setName(departmentToUpdate.getName());

        return department;
    }


}
