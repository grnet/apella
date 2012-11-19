package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Subject;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

@Path("/institution")
@Stateless
public class SubjectRESTService extends RESTService {

	@Inject
	private Logger log;

	@GET
	@SuppressWarnings("unchecked")
	public List<Subject> getAll() {
		return (List<Subject>) em.createQuery(
			"select distinct s from Subject s ")
			.getResultList();
	}

	@GET
	@Path("/search")
	@SuppressWarnings("unchecked")
	public List<Subject> search(@QueryParam("query") String query) {
		return (List<Subject>) em.createQuery(
			"select s from Subject s " +
				"where s.name like :query")
			.setParameter("query", "%" + query + "%")
			.getResultList();
	}

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
