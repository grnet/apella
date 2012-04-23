var DEPRegistrationRouter = Backbone.Router.extend({

	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showRegisterView", "showVerificationView");
	},

	routes : {
		"email=:email&verification=:verificationNumber" : "showVerificationView",
		"profile=:role" : "showRegisterView"
	},

	allowedRoles : [ "professor", "helpdesk" ],

	showRegisterView : function(role) {
		if (_.indexOf(this.allowedRoles, role) >= 0) {
			var userRegistration = new UserRegistration({
				roles : [ role ]
			});
			var userRegistrationView = new UserRegistrationView({
				model : userRegistration
			});
			$("#content").html(userRegistrationView.render().el);
			this.currentView = userRegistrationView;
			return userRegistrationView;
		} else {
			$("#content").empty();
			this.currentView = undefined;
			return undefined;
		}
	},

	showVerificationView : function(email, verificationNumber) {
		var self = this;

		var userRegistration = new UserRegistration({
			"email" : email,
			"verificationNumber" : verificationNumber
		});

		userRegistration.verify({
			success : function(model, resp) {
				var userVerificationView = new UserVerificationView({
					model : userRegistration
				});
				$("#content").html(userVerificationView.render().el);
				self.currentView = userVerificationView;
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
				console.log(resp);

				$("#content").html("ΣΦΑΛΜΑ");
				self.currentView = undefined;
			}
		});
		return "";
	}

});

$(document).ready(function() {
	tpl.loadTemplates([ 'user-registration', 'user-verification', 'popup' ], function() {

		app = new DEPRegistrationRouter();

		Backbone.history.start();

	});
});
