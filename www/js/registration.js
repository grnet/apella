var DEPRegistrationRouter = Backbone.Router.extend({

	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showRegisterView");

		//Header, Footer and other view initializations here
		$('#header').html(new HeaderView().render().el);
	},

	routes : {
		":role" : "showRegisterView"
	},

	showRegisterView : function(role) {
		var userRegistration = new UserRegistration({
			roles: [role]
		});
		var userRegistrationView = new UserRegistrationView({
			model: userRegistration
		});
		$("#content").html(userRegistrationView.render().el);
		this.currentView = userRegistrationView;
		return userRegistrationView;
	},

});

$(document).ready(function() {
	tpl.loadTemplates([ 'header', 'user-registration' ], function() {
		
		app = new DEPRegistrationRouter();
		
		Backbone.history.start();

	});
});
