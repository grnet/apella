var AppRouter = Backbone.Router.extend({
	data : {
		loggedOnUser : new User()
	},

	initialize : function() {
		var self = this;

		_.extend(self, Backbone.Events);
		_.bindAll(self, "showLoginView", "showHomeView", "start");

		// Get LoggedOnUser
		self.data.loggedOnUser.on("user:loggedon", self.start);
		self.data.loggedOnUser.fetch({
			url : "/rest/user/loggedon",
			success : function(model, resp) {
				var authToken = resp.getResponseHeader('X-Auth-Token');
				self.data.loggedOnUser.trigger("user:loggedon", authToken);
				// Start Application
				console.log("Fetch User success");
				self.start();
			},
			error : function(model, resp, options) {
				console.log("Fetch User Error");
				self.showLoginView();
			}
		});
	},

	routes : {
		"" : "showHomeView",
		"profile" : "showProfileView",
		"requests" : "showRequestsView",
	},

	start : function(eventName, authToken) {
		var self = this;
		console.log("Start called");

		self.data.loggedOnUser.off("user:loggedon", self.start);
		// Add Authentication Header in all Ajax Requests
		$.ajaxSetup({
			headers : {
				"X-Auth-Token" : authToken
			}
		});
		// Create Header, Menu, and other side content and
		// bind them to the same loggedOnUser model
		var menuView = new MenuView({
			model : self.data.loggedOnUser
		});
		$("#nav").html(menuView.render().el);

		// Start Routing
		Backbone.history.start();
	},

	showLoginView : function() {
		var self = this;

		var loginView = new LoginView({
			model : self.data.loggedOnUser
		});

		$("#content").html(loginView.render().el);

		this.currentView = loginView;
		return loginView;
	},

	showHomeView : function() {
		console.log("showHomeView");
		$("#content").html("<h1>Home</h1>");
	},

	showProfileView : function() {
		console.log("showProfileView");
		$("#content").html("<h1>PROFILE</h1>");
	},

	showRequestsView : function() {
		console.log("showRequestsView");
		$("#content").html("<h1>REQUESTS</h1>");
	}
});

$(document).ready(function() {
	tpl.loadTemplates([ "login", "popup" ], function() {
		app = new AppRouter();
	});
});
