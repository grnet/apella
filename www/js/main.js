// APELLA Application Routers:
App = {
	allowedRoles : [ "PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "CANDIDATE", "INSTITUTION_MANAGER", "DEPARTMENT_MANAGER", "INSTITUTION_ASSISTANT", "MINISTRY_MANAGER" ],
	
	blockUI : function() {
		$.blockUI({
			message : $("<img src=\"css/images/loader.gif\" />"),
			showOverlay : true,
			centerY : false,
			css : {
				'z-index' : 2000,
				width : '30%',
				top : '1%',
				left : '35%',
				padding : 0,
				margin : 0,
				textAlign : 'center',
				color : '#000',
				border : 'none',
				backgroundColor : 'none',
				cursor : 'wait'
			},
			overlayCSS : {
				'z-index' : 1999,
				backgroundColor : 'none',
				opacity : 1.0
			},
		});
	},
	
	unblockUI : function() {
		$.unblockUI();
	}
};

App.RegistrationRouter = Backbone.Router.extend({
	
	initialize : function() {
		_.extend(this, Backbone.Events);
		_.bindAll(this, "showRegisterView", "showVerificationView", "showRegisterSelectView");
		$(document).ajaxStart(App.blockUI);
		$(document).ajaxStop(App.unblockUI);
		
		App.institutions = new App.Institutions();
		
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
				$("#featured").html("ΣΦΑΛΜΑ " + resp.status);
				self.currentView = undefined;
			}
		});
		return undefined;
	}
});

App.RegistryRouter = Backbone.Router.extend({
	initialize : function() {
		var self = this;
		
		_.extend(self, Backbone.Events);
		_.bindAll(self, "showLoginView", "showHomeView", "showAccountView", "showProfileView", "showRequestsView", "start");
		$(document).ajaxStart(App.blockUI);
		$(document).ajaxStop(App.unblockUI);
		
		// Init LoggedOnUser
		App.loggedOnUser = new App.User();
		App.loggedOnUser.on("user:loggedon", self.start);
		App.loggedOnUser.fetch({
			url : "/dep/rest/user/loggedon",
			cache : false,
			success : function(model, resp) {
				App.loggedOnUser.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				self.showLoginView();
			}
		});
	},
	
	routes : {
		"" : "showHomeView",
		"account" : "showAccountView",
		"profile" : "showProfileView",
		"profile/:roleId" : "showProfileView",
		"requests" : "showRequestsView"
	},
	
	start : function(eventName, authToken) {
		var self = this;
		// Check that this is not an ADMINISTRATOR
		if (App.loggedOnUser.hasRole("ADMINISTRATOR")) {
			App.loggedOnUser = new App.User();
			App.loggedOnUser.on("user:loggedon", self.start);
			self.showLoginView();
			var popup = new App.PopupView({
				type : "warning",
				message : $.i18n.prop("error.administrator.login")
			});
			popup.show();
			
			return;
		}
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
		$("#featured").unbind();
		$("#featured").empty();
		$("#featured").removeClass("well");
		$("#sidebar").unbind();
		$("#sidebar").empty();
		$("#sidebar").removeClass("well");
		$("#content").unbind();
		$("#content").empty();
		$("#content").removeClass("well");
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
		this.clear();
		var homeView = new App.HomeView({
			model : App.loggedOnUser
		});
		var announcementsView = new App.AnnouncementListView({
			collection : App.roles
		});
		
		$("#featured").html(homeView.render().el);
		$("#featured").append(announcementsView.el);
		App.roles.fetch({
			cache : false
		});
		
		this.currentView = homeView;
		return homeView;
	},
	
	showAccountView : function() {
		this.clear();
		var accountView = new App.AccountView({
			model : App.loggedOnUser
		});
		$("#content").append(accountView.render().el);
		this.currentView = accountView;
		return accountView;
	},
	
	showProfileView : function(roleId) {
		this.clear();
		App.roles.on("role:selected", function(role) {
			var roleView = new App.RoleEditView({
				model : role
			});
			// Update history
			if (role.id) {
				App.router.navigate("profile/" + role.id, {
					trigger : false
				});
			} else {
				App.router.navigate("profile", {
					trigger : false
				});
			}
			$("#content").unbind();
			$("#content").empty();
			$("#content").html(roleView.render().el);
		});
		var roleListView = new App.RoleListView({
			collection : App.roles,
			user : App.loggedOnUser.get("id")
		});
		$("#sidebar").addClass("well");
		$("#sidebar").html(roleListView.render().el);
		// Refresh roles from server
		App.roles.fetch({
			cache : false,
			success : function() {
				if (_.isUndefined(roleId)) {
					App.roles.trigger("role:selected", App.roles.at(0));
				} else {
					App.roles.trigger("role:selected", App.roles.get(roleId));
				}
			}
		});
		this.currentView = roleListView;
		return roleListView;
	},
	
	showRequestsView : function() {
		this.clear();
		$("#content").html("<h1>REQUESTS</h1>");
	}

});

App.AdminRouter = Backbone.Router.extend({
	
	initialize : function() {
		var self = this;
		
		_.extend(this, Backbone.Events);
		_.bindAll(this, "start", "showLoginView", "showHomeView", "showUserSearchView", "showUserView");
		$(document).ajaxStart(App.blockUI);
		$(document).ajaxStop(App.unblockUI);
		
		// Init LoggedOnUser
		App.loggedOnUser = new App.User();
		App.loggedOnUser.on("user:loggedon", self.start);
		App.loggedOnUser.fetch({
			url : "/dep/rest/user/loggedon",
			cache : false,
			success : function(model, resp) {
				App.loggedOnUser.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				self.showLoginView();
			}
		});
	},
	
	routes : {
		"" : "showHomeView",
		"account" : "showAccountView",
		"users" : "showUserSearchView",
		"users/:query" : "showUserSearchView",
		"user/:id" : "showUserView"
	},
	
	start : function(eventName, authToken) {
		var self = this;
		// Check that user is indeed Administrator
		if (!App.loggedOnUser.hasRole("ADMINISTRATOR")) {
			App.loggedOnUser = new App.User();
			App.loggedOnUser.on("user:loggedon", self.start);
			self.showLoginView();
			
			var popup = new App.PopupView({
				type : "warning",
				message : $.i18n.prop("error.insufficient.privileges")
			});
			popup.show();
			
			return;
		}
		App.loggedOnUser.off("user:loggedon", self.start);
		
		// Create Header, Menu, and other side content and
		// bind them to the same loggedOnUser model
		var adminmenuView = new App.AdminMenuView({
			model : App.loggedOnUser
		});
		adminmenuView.render();
		var usermenuView = new App.UserMenuView({
			model : App.loggedOnUser
		});
		usermenuView.render();
		
		// Start Routing
		Backbone.history.start();
	},
	
	clear : function() {
		$("#featured").unbind();
		$("#featured").empty();
		$("#featured").removeClass("well");
		$("#sidebar").unbind();
		$("#sidebar").empty();
		$("#sidebar").removeClass("well");
		$("#content").unbind();
		$("#content").empty();
		$("#content").removeClass("well");
	},
	
	showLoginView : function() {
		this.clear();
		var loginView = new App.AdminLoginView({
			model : App.loggedOnUser
		});
		$("#featured").html(loginView.render().el);
		this.currentView = loginView;
		return loginView;
	},
	
	showHomeView : function() {
		this.clear();
		$("#content").html("<h1>HOME</h1>");
	},
	
	showAccountView : function() {
		this.clear();
		var accountView = new App.AccountView({
			model : App.loggedOnUser
		});
		$("#content").append(accountView.render().el);
		this.currentView = accountView;
		return accountView;
	},
	
	showUserSearchView : function(query) {
		var self = this;
		self.clear();
		var users = new App.Users();
		users.on("user:selected", function(user) {
			if (user) {
				self.showUserView(user.id, user);
				self.navigate("user/" + user.id, {
					trigger : false
				});
			}
		}, this);
		var userSearchView = new App.UserSearchView({
			"query" : query ? JSON.parse(query) : undefined,
			collection : users
		});
		var userListView = new App.UserListView({
			collection : users
		});
		$("#sidebar").addClass("well");
		$("#sidebar").append(userSearchView.render().el);
		$("#content").append(userListView.render().el);
	},
	
	showUserView : function(id, user) {
		this.clear();
		if (_.isUndefined(user)) {
			user = new App.User({
				"id" : id
			});
		}
		var roles = new App.Roles();
		roles.user = id;
		
		var userView = new App.UserView({
			model : user
		});
		var roleView = new App.RoleView({
			collection : roles
		});
		user.fetch({
			cache : false
		});
		roles.fetch({
			cache : false
		});
		$("#sidebar").addClass("well");
		$("#sidebar").html(userView.el);
		$("#content").html(roleView.el);
	},

});