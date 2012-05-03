var AppRouter = Backbone.Router.extend({
	data : {
		loggedOnUser : new User(),
		roles : new Roles()
	},

	initialize : function() {
		var self = this;

		_.extend(self, Backbone.Events);
		_.bindAll(self, "showLoginView", "showHomeView", "start");

		// Get LoggedOnUser
		self.data.loggedOnUser.on("user:loggedon", self.start);
		self.data.loggedOnUser.fetch({
			url : "/dep/rest/user/loggedon",
			success : function(model, resp) {
				console.log("Succesful Login");
				console.log(resp);
				self.data.loggedOnUser.trigger("user:loggedon");
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

		// Add necessary data
		self.data.roles.references.user = self.data.loggedOnUser.get("id");

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
		var self = this;
		console.log("showHomeView");
		$("#content").empty();
		var userView = new UserView({
			model : self.data.loggedOnUser
		});
		$("#content").append(userView.render().el);
		this.currentView = userView;
		return userView;
	},

	showProfileView : function() {
		var self = this;
		console.log("showProfileView");
		$("#content").empty();
		var roleListView = new RoleListView({
			collection : self.data.roles,
			user : self.data.loggedOnUser.get("id")
		});
		$("#content").append(roleListView.render().el);
		// Refresh roles from server
		self.data.roles.fetch();

		this.currentView = roleListView;
		return roleListView;
	},

	showRequestsView : function() {
		console.log("showRequestsView");
		$("#content").html("<h1>REQUESTS</h1>");
	}
});

$(document).ready(function() {
	tpl.loadTemplates([ "login", "popup", "user", "role" ], function() {
		app = new AppRouter();
	});
});
