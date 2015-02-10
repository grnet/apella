<%@ page contentType="text/html; charset=UTF-8" session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>ΑΠΕΛΛΑ</title>
<link rel="stylesheet" href="<c:url value='/css/styles.css' />" type="text/css">
<script type="text/javascript" src="<c:url value='/js/jquery-1.9.1.js' />">
</script>
<script type="text/javascript">
	var questionsNumber = ${questions.size()};
	function checkForm(form) {
		var answeredAll = $("input[type=radio]:checked").length >= questionsNumber;
		if (answeredAll) {
			return confirm("Θέλετε να προχωρήσετε σε οριστική υποβολή των στοιχείων;");
		} else {
			alert("Παρακαλώ απαντήστε σε όλες τις ερωτήσεις");
			return false;
		}
		return false;
	}
</script>
</head>
<body>
	<div id="wrapper">

		<div id="header">
			<img alt="" src="<c:url value='/css/images/apella_logo.png' />" align="left">
        	<h1>ΕΡΩΤΗΜΑΤΟΛΟΓΙΟ ΑΞΙΟΛΟΓΗΣΗΣ ΑΠΕΛΛΑ</h1>
        	<div class="split"></div>
		</div>

		<div id="content">
			<h2>Παρακαλούμε να απαντήσετε στις παρακάτω ερωτήσεις:</h2>
			<form action="./${encryptedUserId}" method="post" onsubmit="return checkForm(this);">
			<ul class="unstyled">
				<c:forEach var="question" items="${questions}">
						<li>
						<b>${question.orderno}. ${question.question}</b>
						<ol type="A">
							<c:forEach var="answer" items="${question.possibleAnswers}">
							<li>
							<label>
								<input type="radio" name="question_${question.id}" value="${answer.key}" /> ${answer.value}
							</label>
							</li>
							</c:forEach>
						</ol>
						</li>
				</c:forEach>
			</ul>
			<p>
				<button type="submit">Αποστολή</button>
			</p>
			</form>
		</div>

		<div id="footer">
                <p>
                    <a href="http://www.minedu.gov.gr/"><img alt=""  style="height: 45px;" src="<c:url value='/css/images/logo_minedu.png' />"/></a>
                    <a href="https://www.grnet.gr/"><img alt="" src="<c:url value='/css/images/logo_grnet.png' />" /></a>
                    <a href="http://europa.eu"><img alt="" src="<c:url value='/css/images/logo_eu.jpeg' />" /></a>
                    <a href="http://www.epdm.gr/"><img alt="" src="<c:url value='/css/images/logo_epdm.jpg' />" /></a>
                    <a href="http://www.espa.gr"><img alt="" src="<c:url value='/css/images/logo_espa.jpeg' />" /></a>
                </p>
                <p>
                    <b>Με τη συγχρηματοδότηση της Ελλάδας & της Ευρωπαϊκής Ένωσης</b>
                </p>
            </div>
	</div>
</body>
</html>
