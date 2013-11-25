package gr.grnet.dep.server.servlet;

import gr.grnet.dep.service.util.AuthenticationService;
import gr.grnet.dep.service.util.DEPConfigurationFactory;
import gr.grnet.dep.service.util.JiraService;
import gr.grnet.dep.service.util.MailService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public abstract class BaseHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 7536110129027726947L;

	@Inject
	protected Logger logger;

	@EJB
	AuthenticationService authenticationService;

	@EJB
	MailService mailService;

	@EJB
	JiraService jiraService;

	protected static Configuration conf;

	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
		} catch (ConfigurationException e) {
		}
	}

	protected void sendPage(HttpServletRequest request, HttpServletResponse response, String page) {
		try {
			RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/WEB-INF/pages/" + page);
			rd.forward(request, response);
		} catch (ServletException e) {
			sendErrorPage(request, response, e.getMessage());
		} catch (IOException e) {
			sendErrorPage(request, response, e.getMessage());
		}
	}

	protected void sendErrorPage(HttpServletRequest request, HttpServletResponse response, String errorMessage) {
		try {
			request.setAttribute("errorMessage", errorMessage);
			getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/pages/error.jsp").forward(request, response);
		} catch (ServletException e) {
			logger.log(Level.SEVERE, "Error Sending Error Page", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error Sending Error Page", e);
		}
	}

	protected String getRelativePath(HttpServletRequest request) {
		// Remove the servlet path from the request URI.
		String p = request.getRequestURI();
		String servletPath = request.getContextPath() + request.getServletPath();
		String result = p.substring(servletPath.length());
		if (result == null || result.equals("")) {
			result = "/";
		}
		return result;
	}
}
