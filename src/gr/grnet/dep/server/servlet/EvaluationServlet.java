package gr.grnet.dep.server.servlet;


import gr.grnet.dep.service.EvaluationService;
import gr.grnet.dep.service.model.Role;
import gr.grnet.dep.service.model.User;
import gr.grnet.dep.service.model.evaluation.Evaluation;
import gr.grnet.dep.service.model.evaluation.EvaluationAnswer;
import gr.grnet.dep.service.model.evaluation.EvaluationQuestion;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

@WebServlet(
		name = "evaluationsServlet",
		urlPatterns = {"/evaluation/*"})
public class EvaluationServlet extends BaseHttpServlet {

	private static final long serialVersionUID = -8201973038949627229L;

	@EJB
	EvaluationService service;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo.matches("/([0-9A-Fa-f])+")) {
			String encryptedUserId = pathInfo.split("/")[1];
			doGetEvaluation(encryptedUserId, request, response);
		} else {
			sendErrorPage(request, response, "Not Found");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo.matches("/([0-9A-Fa-f])+")) {
			String encryptedUserId = pathInfo.split("/")[1];
			doPostEvaluation(encryptedUserId, request, response);
		}
	}

	private void doGetEvaluation(String encryptedUserId, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// Get Data
		try {
			Long userId = EvaluationService.decryptID(encryptedUserId);
			User user = service.findUserById(userId);
			if (user == null) {
				sendErrorPage(request, response, "Not Found");
				return;
			}
			Role.RoleDiscriminator userRole = user.getPrimaryRole();
			if (!Role.RoleDiscriminator.CANDIDATE.equals(userRole) &&
					!Role.RoleDiscriminator.PROFESSOR_DOMESTIC.equals(userRole) &&
					!Role.RoleDiscriminator.PROFESSOR_FOREIGN.equals(userRole) &&
					!Role.RoleDiscriminator.INSTITUTION_MANAGER.equals(userRole)) {
				sendErrorPage(request, response, "Not Found");
				return;
			}
			Evaluation evaluation = service.findEvaluationByUserID(userId);
			if (evaluation != null) {
				sendPage(request, response, "evaluation-already-submitted.jsp");
				return;
			}
			List<EvaluationQuestion> questions = service.getEvaluationQuestions(user.getPrimaryRole());
			// Add data to request
			request.setAttribute("encryptedUserId", encryptedUserId);
			request.setAttribute("questions", questions);
			// Forward to JSP
			sendPage(request, response, "evaluation.jsp");
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private void doPostEvaluation(String encryptedUserId, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			// Get Data
			Long userId = EvaluationService.decryptID(encryptedUserId);
			User user = service.findUserById(userId);
			if (user == null) {
				sendErrorPage(request, response, "Not Found");
				return;
			}
			Role.RoleDiscriminator userRole = user.getPrimaryRole();
			if (!Role.RoleDiscriminator.CANDIDATE.equals(userRole) &&
					!Role.RoleDiscriminator.PROFESSOR_DOMESTIC.equals(userRole) &&
					!Role.RoleDiscriminator.PROFESSOR_FOREIGN.equals(userRole) &&
					!Role.RoleDiscriminator.INSTITUTION_MANAGER.equals(userRole)) {
				sendErrorPage(request, response, "Not Found");
				return;
			}
			Evaluation evaluation = service.findEvaluationByUserID(userId);
			if (evaluation != null) {
				sendPage(request, response, "evaluation-already-submitted.jsp");
				return;
			}
			evaluation = new Evaluation();
			evaluation.setUser(user);
			evaluation.setYear(2014);
			// Read form-data and set to entity
			for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
				String parameter = (String) e.nextElement();
				if (parameter.matches("(question_(\\d)+)")) {
					// Read question->answer
					Long questionId = Long.parseLong(parameter.split("_")[1]);
					Long answerCode = Long.parseLong(request.getParameter(parameter));
					EvaluationQuestion evaluationQuestion = service.getEvaluationQuestion(questionId);

					// Fill Answer object
					EvaluationAnswer answer = new EvaluationAnswer();
					answer.setEvaluation(evaluation);
					answer.setEvaluationQuestion(evaluationQuestion);
					answer.setQuestion(evaluationQuestion.getQuestion());
					answer.setCode(answerCode);
					answer.setAnswer(evaluationQuestion.getPossibleAnswers().get(answerCode));

					// Add to evaluation
					evaluation.getAnswers().add(answer);
				}
			}
			// Save:
			service.saveEvaluation(evaluation);
			// Redirect to success
			sendPage(request, response, "evaluation-success.jsp");
		} catch (IllegalArgumentException e) {
			sendErrorPage(request, response, "Bad Request");
		}
	}

}
