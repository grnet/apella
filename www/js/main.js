var AppRouter = Backbone.Router.extend({

	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showLoginView", "login", "showHomeView");
		
		//Header, Footer and other view initializations here
		$('#header').html(new HeaderView().render().el);
	},

	routes : {
		"" : "home",
		"login" : "showLoginView",
	},
	
	goTo : function(event) {
		app.navigate(event.route);
	},

	showLoginView : function() {
		var loginView = new LoginView();
		
		loginView.bind("login_view:login", this.login, this);
		loginView.bind("login_view:goTo", this.goTo, this);
		
		$("#content").html(loginView.render().el);
		
		this.currentView = loginView;
		return loginView;
	},
	
	login: function(event) {
		console.log("AppRouter: received login event " + JSON.stringify(event));
	},

	showHomeView : function() {
		$("#content").html("<h1>Home</h1>");
	}
});

$(document).ready(function() {
	tpl.loadTemplates([ 'header', 'login'], function() {
		app = new AppRouter();
		Backbone.history.start();
	});
});
