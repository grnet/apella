package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Subject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Path("/subject")
@Stateless
public class SubjectRESTService extends RESTService {

	@Inject
	private Logger log;

	/**
	 * Returns all subjects
	 *
	 * @param query
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Subject> get(@QueryParam("query") String query) {
		List<Subject> result = null;
		if (query == null) {
			result = em.createQuery("select distinct s from Subject s ")
					.getResultList();
		} else {
			result = em.createQuery("select s from Subject s where s.name like :query")
					.setParameter("query", query + "%")
					.getResultList();
		}
		return result;
	}

	/**
	 * Returns specified subject
	 *
	 * @param id
	 * @return
	 * @HTTP 404 X-Error-Code: wrong.subject.id
	 */

	@GET
	@Path("/{id:[0-9]+}")
	public Subject get(@PathParam("id") long id) {
		try {
			return (Subject) em.createQuery(
					"select i from Subject i " +
							"where i.id = :id")
					.setParameter("id", id)
					.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.subject.id");
		}
	}
}
