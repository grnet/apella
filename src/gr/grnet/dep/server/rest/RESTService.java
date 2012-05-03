package gr.grnet.dep.server.rest;

import gr.grnet.dep.service.model.User;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;


public class RESTService {


	@SuppressWarnings("unused")
	@Inject
	private Logger logger;
	
	
	@Context HttpServletRequest request;


	User getLoggedOn() {
		return (User) request.getAttribute("user");
	}

	
	
}
