App = {
	allowedRoles : [ "CANDIDATE", "PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "INSTITUTION_MANAGER", "DEPARTMENT_MANAGER", "INSTITUTION_ASSISTANT", "MINISTRY_MANAGER" ]
};

App.RegistrationRouter = Backbone.Router.extend({
	
	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showRegisterView", "showVerificationView", "showRegisterSelectView");
		Backbone.history.start();
	},
	
	routes : {
		"email=:email&verification=:verificationNumber" : "showVerificationView",
		"" : "showRegisterSelectView",
		"profile=:role" : "showRegisterView"
	},
	
	showRegisterSelectView : function() {
		var userRegistrationSelectView = new App.UserRegistrationSelectView({});
		$("#featured").html(userRegistrationSelectView.render().el);
		this.currentView = userRegistrationSelectView;
		return userRegistrationSelectView;
	},
	
	showRegisterView : function(role) {
		if (_.indexOf(App.allowedRoles, role) >= 0) {
			var userRegistration = new App.User({
				"roles" : [ {
					"discriminator" : role
				} ]
			});
			var userRegistrationView = new App.UserRegistrationView({
				model : userRegistration
			});
			$("#featured").html(userRegistrationView.render().el);
			this.currentView = userRegistrationView;
			return userRegistrationView;
		} else {
			$("#featured").empty();
			this.currentView = undefined;
			return undefined;
		}
	},
	
	showVerificationView : function(email, verificationNumber) {
		var self = this;
		
		var userRegistration = new User({
			"username" : email,
			"verificationNumber" : verificationNumber
		});
		
		userRegistration.verify({
			success : function(model, resp) {
				var userVerificationView = new App.UserVerificationView({
					model : userRegistration
				});
				$("#featured").html(userVerificationView.render().el);
				self.currentView = userVerificationView;
			},
			error : function(model, resp, options) {
				console.log(resp.status);
				console.log(resp);
				$("#featured").html("ΣΦΑΛΜΑ " + resp.status);
				self.currentView = undefined;
			}
		});
		return undefined;
	}
});

App.Router = Backbone.Router.extend({
	initialize : function() {
		var self = this;
		
		_.extend(self, Backbone.Events);
		_.bindAll(self, "showLoginView", "showHomeView", "start");
		
		// Init LoggedOnUser
		App.loggedOnUser = new App.User();
		App.loggedOnUser.on("user:loggedon", self.start);
		App.loggedOnUser.fetch({
			url : "/dep/rest/user/loggedon",
			success : function(model, resp) {
				console.log("Succesful Login");
				console.log(resp);
				App.loggedOnUser.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				console.log("Fetch User Error");
				self.showLoginView();
			}
		});
	},
	
	routes : {
		"" : "showHomeView",
		"user" : "showUserView",
		"profile" : "showProfileView",
		"profile/:roleId" : "showProfileView",
		"requests" : "showRequestsView"
	},
	
	start : function(eventName, authToken) {
		var self = this;
		console.log("Start called");
		
		App.loggedOnUser.off("user:loggedon", self.start);
		
		// Add necessary data
		App.roles = new App.Roles();
		App.roles.user = App.loggedOnUser.get("id");
		
		// Create Header, Menu, and other side content and
		// bind them to the same loggedOnUser model
		var menuView = new App.MenuView({
			model : App.loggedOnUser
		});
		menuView.render();
		var usermenuView = new App.UserMenuView({
			model : App.loggedOnUser
		});
		usermenuView.render();
		
		// Start Routing
		Backbone.history.start();
	},
	
	clear : function() {
		$("#featured").empty();
		$("#sidebar").empty();
		$("#content").empty();
	},
	
	showLoginView : function() {
		this.clear();
		var loginView = new App.LoginView({
			model : App.loggedOnUser
		});
		$("#featured").html(loginView.render().el);
		this.currentView = loginView;
		return loginView;
	},
	
	showHomeView : function() {
		console.log("showHomeView");
		this.clear();
		var homeView = new App.HomeView({
			model : App.loggedOnUser
		});
		$("#featured").append(homeView.render().el);
		this.currentView = homeView;
		return homeView;
	},
	
	showUserView : function() {
		console.log("showUserView");
		this.clear();
		var userView = new App.UserView({
			model : App.loggedOnUser
		});
		$("#content").append(userView.render().el);
		this.currentView = userView;
		return userView;
	},
	
	showProfileView : function(roleId) {
		console.log("showProfileView");
		this.clear();
		var roleListView = new App.RoleListView({
			collection : App.roles,
			user : App.loggedOnUser.get("id")
		});
		$("#sidebar").html(roleListView.render().el);
		// Refresh roles from server
		App.roles.fetch({
			success : function() {
				if (_.isUndefined(roleId)) {
					roleListView.displayRole(roleListView.collection.at(0));
				} else {
					roleListView.displayRole(roleListView.collection.get(roleId));
				}
			}
		});
		this.currentView = roleListView;
		return roleListView;
	},
	
	showRequestsView : function() {
		console.log("showRequestsView");
		this.clear();
		$("#content").html("<h1>REQUESTS</h1>");
	}

});