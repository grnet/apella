package gr.grnet.dep.server.servlet;

import gr.grnet.dep.server.WebConstants;
import gr.grnet.dep.service.exceptions.ServiceException;
import gr.grnet.dep.service.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
	name = "emailLoginServlet",
	urlPatterns = {"/email/*"})
public class EmailLogin extends BaseHttpServlet {

	private static final long serialVersionUID = -8201973038949627229L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = getRelativePath(request);
		// Dispatch Request
		if (path.startsWith("/login")) {
			doGetLogin(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	public void doGetLogin(HttpServletRequest request, HttpServletResponse response) {
		// 1. Read Parameters from request:
		String emailLoginToken = request.getParameter("user") == null ? "" : request.getParameter("user");
		URI nextURL;
		try {
			nextURL = new URI(request.getParameter("nextURL") == null ? "" : request.getParameter("nextURL"));
		} catch (URISyntaxException e1) {
			sendErrorPage(request, response, "malformed.next.url");
			return;
		}

		// 2. Login User
		try {
			User u = authenticationService.doEmailLogin(emailLoginToken);

			// Send Response - Redirect to application
			Cookie cookie = new Cookie("_dep_a", u.getAuthToken());
			cookie.setPath("/");
			response.addCookie(cookie);
			response.addHeader(WebConstants.AUTHENTICATION_TOKEN_HEADER, u.getAuthToken());
			response.sendRedirect(nextURL.toString());
		} catch (IOException e) {
			sendErrorPage(request, response, e.getMessage());
		} catch (ServiceException e1) {
			sendErrorPage(request, response, e1.getErrorKey());
		}
	}
}
