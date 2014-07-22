package gr.grnet.dep.server.rest.exceptions;

import org.jboss.resteasy.spi.NoLogWebApplicationException;

import javax.ejb.ApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


@ApplicationException
public class RestException extends NoLogWebApplicationException {

	private static final long serialVersionUID = 5519873880766238330L;

	public RestException(Status status) {
		super(Response.status(status)
				.header("X-Error-Code", "")
				.build());
	}

	public RestException(Status status, String errorCode) {
		super(Response.status(status)
				.entity("{\"error\":\"" + errorCode + "\"}")
				.header("X-Error-Code", errorCode)
				.build());
	}

}
