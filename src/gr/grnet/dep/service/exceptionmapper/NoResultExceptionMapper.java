package gr.grnet.dep.service.exceptionmapper;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoResultExceptionMapper implements ExceptionMapper<NoResultException> {
	
   public Response toResponse(NoResultException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
   }
   
}
