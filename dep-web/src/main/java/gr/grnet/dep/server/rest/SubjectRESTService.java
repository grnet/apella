package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.SubjectService;
import gr.grnet.dep.service.exceptions.NotFoundException;
import gr.grnet.dep.service.model.Subject;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;

@Path("/subject")
public class SubjectRESTService extends RESTService {

	@EJB
	private SubjectService subjectService;

	/**
	 * Returns all subjects
	 *
	 * @param query
	 * @return
	 */
	@GET
	@SuppressWarnings("unchecked")
	public Collection<Subject> get(@QueryParam("query") String query) {
		// fetch the list with the subjects
		Collection<Subject> result = subjectService.get(query);

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
			Subject subject = subjectService.get(id);

			return subject;
		} catch (NotFoundException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.subject.id");
		}
	}
}
