App = {
	allowedRoles : [ "CANDIDATE", "PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "INSTITUTION_MANAGER", "DEPARTMENT_MANAGER", "INSTITUTION_ASSISTANT", "MINISTRY_MANAGER" ]
};

App.RegistrationRouter = Backbone.Router.extend({
	
	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showRegisterView", "showVerificationView");
		
		Backbone.history.start();
	},
	
	routes : {
		"email=:email&verification=:verificationNumber" : "showVerificationView",
		"profile=:role" : "showRegisterView"
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
		
		var userRegistration = new User({
			"username" : email,
			"verificationNumber" : verificationNumber
		});
		
		userRegistration.verify({
			success : function(model, resp) {
				var userVerificationView = new App.UserVerificationView({
					model : userRegistration
				});
				$("#content").html(userVerificationView.render().el);
				self.currentView = userVerificationView;
			},
			error : function(model, resp, options) {
				console.log(resp.status);
				console.log(resp);
				
				$("#content").html("ΣΦΑΛΜΑ");
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
		"profile" : "showProfileView",
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
	
	showLoginView : function() {
		var self = this;
		
		var loginView = new App.LoginView({
			model : App.loggedOnUser
		});
		
		$("#content").html(loginView.render().el);
		
		this.currentView = loginView;
		return loginView;
	},
	
	showHomeView : function() {
		var self = this;
		console.log("showHomeView");
		$("#content").empty();
		var userView = new App.UserView({
			model : App.loggedOnUser
		});
		$("#content").append(userView.render().el);
		this.currentView = userView;
		return userView;
	},
	
	showProfileView : function() {
		var self = this;
		console.log("showProfileView");
		$("#content").empty();
		$("#content").append("<div id=\"roleList\" class=\"well span3\">");
		$("#content").append("<div id=\"roleInfo\" class=\"span9\">");
		var roleListView = new App.RoleListView({
			el: $("#content #roleList")[0],
			collection : App.roles,
			user : App.loggedOnUser.get("id")
		});
		roleListView.render();
		// Refresh roles from server
		App.roles.fetch();
		
		this.currentView = roleListView;
		return roleListView;
	},
	
	showRequestsView : function() {
		console.log("showRequestsView");
		$("#content").html("<h1>REQUESTS</h1>");
	}

});