package gr.grnet.dep.service;

import gr.grnet.dep.service.model.Department;
import gr.grnet.dep.service.model.Sector;
import gr.grnet.dep.service.model.Subject;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import gr.grnet.dep.service.util.StringUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@Stateless
public class UtilityService {

	@PersistenceContext(unitName = "apelladb")
	protected EntityManager em;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Subject supplementSubject(Subject subject) {
		if (subject == null || subject.getName() == null || subject.getName().trim().isEmpty()) {
			return null;
		} else {
			String name = StringUtil.toUppercaseNoTones(subject.getName(), new Locale("el"));
			try {
				Subject existingSubject = (Subject) em.createQuery(
						"select s from Subject s " +
								"where s.name = :name ")
						.setParameter("name", name)
						.getSingleResult();

				return existingSubject;
			} catch (NoResultException e) {
				Subject newSubject = new Subject();
				newSubject.setName(name);
				em.persist(newSubject);
				return newSubject;
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Collection<Subject> supplementSubjects(Collection<Subject> subjects) {
		if (subjects.isEmpty()) {
			return subjects;
		}
		Collection<Subject> supplemented = new ArrayList<Subject>();
		for (Subject subject : subjects) {
			supplemented.add(supplementSubject(subject));
		}
		return supplemented;
	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Collection<Department> supplementDepartments(Collection<Department> departments) {
		if (departments.isEmpty()) {
			return departments;
		}
		Collection<Long> departmentIds = new ArrayList<Long>();
		for (Department department : departments) {
			departmentIds.add(department.getId());
		}
		return em.createQuery(
				"from Department d " +
						"where d.id in (:departmentIds)")
				.setParameter("departmentIds", departmentIds)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Collection<Sector> supplementSectors(Collection<Sector> sectors) {
		if (sectors.isEmpty()) {
			return sectors;
		}
		Collection<Long> sectorIds = new ArrayList<Long>();
		for (Sector sector : sectors) {
			sectorIds.add(sector.getId());
		}
		return em.createQuery(
				"from Sector s " +
						"where s.id in (:sectorIds)")
				.setParameter("sectorIds", sectorIds)
				.getResultList();
	}
}
