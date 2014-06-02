package gr.grnet.dep.server.servlet;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.ShibbolethInformation;
import gr.grnet.dep.service.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;

@WebServlet(
		name = "shibbolethLoginServlet",
		urlPatterns = {"/shibboleth/*"})
public class ShibbolethLogin extends BaseHttpServlet {

	private static final long serialVersionUID = -8201973038949627229L;

	private ResourceBundle resources = ResourceBundle.getBundle("gr.grnet.dep.service.util.dep-login", new Locale("el"));

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = getRelativePath(request);
		// Dispatch Request
		if (path.startsWith("/login")) {
			doGetLogin(request, response);
		} else if (path.startsWith("/connect")) {
			doGetConnect(request, response);
		} else if (path.startsWith("/test")) {
			doGetTest(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	public void doGetTest(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		Map<String, String[]> parameters = request.getParameterMap();
		for (String paramName : parameters.keySet()) {
			result = result.concat("Parameter:\t" + paramName + " = " + Arrays.toString(parameters.get(paramName)) + "\n");
		}
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String headerName = headers.nextElement();
			result = result.concat("Header:\t\t" + headerName + " = " + request.getHeader(headerName) + "\n");
		}
		Enumeration<String> attributes = request.getAttributeNames();
		while (attributes.hasMoreElements()) {
			String attributeName = attributes.nextElement();
			result = result.concat("Attribute:\t" + attributeName + " = " + request.getAttribute(attributeName) + "\n");
		}

		// Create Response:
		request.setAttribute("testResult", result);
		sendPage(request, response, "shibbolethTest.jsp");
	}

	public void doGetLogin(HttpServletRequest request, HttpServletResponse response) {
		// 1. Read Attributes from request:
		ShibbolethInformation shibbolethInfo = readShibbolethFields(request);
		URI nextURL;
		try {
			nextURL = new URI(request.getParameter("nextURL") == null ? "" : request.getParameter("nextURL"));
		} catch (URISyntaxException e1) {
			sendErrorPage(request, response, "malformed.next.url");
			return;
		}

		// 2. Login User
		try {
			User u = authenticationService.doShibbolethLogin(shibbolethInfo);

			// Send Response - Redirect to application
			Cookie cookie = new Cookie("_dep_a", u.getAuthToken());
			cookie.setPath("/");
			response.addCookie(cookie);
			response.addHeader(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken());
			response.sendRedirect(nextURL.toString());
		} catch (IOException e) {
			sendErrorPage(request, response, e.getMessage());
		} catch (ServiceException e1) {
			String message = resources.getString("error." + e1.getErrorKey());
			sendErrorPage(request, response, message);
		}
	}

	public void doGetConnect(HttpServletRequest request, HttpServletResponse response) {
		// 1. Read Attributes from request:
		ShibbolethInformation shibbolethInfo = readShibbolethFields(request);
		String permanentAuthToken = request.getParameter("user") == null ? "" : request.getParameter("user");
		URI nextURL;
		try {
			nextURL = new URI(request.getParameter("nextURL") == null ? "" : request.getParameter("nextURL"));
		} catch (URISyntaxException e1) {
			sendErrorPage(request, response, "malformed.next.url");
			return;
		}
		// 2. Login User
		try {
			User u = authenticationService.connectEmailToShibbolethAccount(permanentAuthToken, shibbolethInfo);
			// Send Response - Redirect to application
			Cookie cookie = new Cookie("_dep_a", u.getAuthToken());
			cookie.setPath("/");
			response.addCookie(cookie);
			response.addHeader(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken());
			response.sendRedirect(nextURL.toString());
		} catch (IOException e) {
			sendErrorPage(request, response, e.getMessage());
		} catch (ServiceException e1) {
			String message = resources.getString("error." + e1.getErrorKey());
			sendErrorPage(request, response, message);
		}
	}

	private ShibbolethInformation readShibbolethFields(HttpServletRequest request) {
		ShibbolethInformation result = new ShibbolethInformation();

		result.setGivenName(readShibbolethField(request, "givenName"));
		result.setSn(readShibbolethField(request, "sn"));
		result.setSchacHomeOrganization(readShibbolethField(request, "schac-home-organization"));
		result.setRemoteUser(readShibbolethField(request, "REMOTE_USER"));

		String affiliation = null;
		affiliation = readShibbolethField(request, "unscoped-affiliation");
		if (affiliation == null) {
			affiliation = readShibbolethField(request, "primary-affiliation");
		}
		if (affiliation == null) {
			affiliation = readShibbolethField(request, "affiliation");
		}
		if (affiliation != null && affiliation.toLowerCase().contains("faculty")) {
			affiliation = "faculty";
		}
		result.setAffiliation(affiliation);

		return result;
	}

	private String readShibbolethField(HttpServletRequest request, String headerName) {
		try {
			Object value = request.getHeader(headerName);
			String field = null;
			if (value != null && !value.toString().isEmpty()) {
				field = new String(value.toString().getBytes("ISO-8859-1"), "UTF-8");
			}
			return field;
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "decodeAttribute: ", e);
			return null;
		}
	}

}
