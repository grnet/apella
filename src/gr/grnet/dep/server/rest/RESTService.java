package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Context;

import org.jboss.resteasy.spi.HttpRequest;

public class RESTService {

	@SuppressWarnings("unused")
	@Inject
	private Logger logger;

	@Context
	HttpRequest request;

	User getLoggedOn() {
		return (User) request.getAttribute("user");
	}

}
