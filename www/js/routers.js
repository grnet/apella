define([ "jquery", "underscore", "backbone", "application", "models", "views", "text!tpl/user-registration-success.html" ], function($, _, Backbone, App, Models, Views, tpl_user_registration_success) {
	var Routers = {};
	
	Routers.RegistrationRouter = Backbone.Router.extend({
		
		initialize : function() {
			_.extend(this, Backbone.Events);
			_.bindAll(this, "showRegisterView", "showVerificationView", "showRegisterSelectView", "showRegisterSuccessView");
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);
			
			App.institutions = new Models.Institutions();
			var languageView = new Views.LanguageView({});
			languageView.render();
			
			Backbone.history.start();
		},
		
		routes : {
			"email=:email&verification=:verificationNumber" : "showVerificationView",
			"" : "showRegisterSelectView",
			"profile=:role" : "showRegisterView",
			"success" : "showRegisterSuccessView"
		},
		
		clear : function() {
			var self = this;
			if (_.isArray(self.currentView)) {
				_.each(self.currentView, function(view) {
					view.close();
				});
			} else if (_.isObject(self.currentView)) {
				self.currentView.close();
			}
			self.currentView = undefined;
			
			$("#featured").unbind();
			$("#featured").empty();
			$("#content").unbind();
			$("#content").empty();
		},
		
		showRegisterSelectView : function() {
			var userRegistrationSelectView = new Views.UserRegistrationSelectView({});
			$("#featured").html(userRegistrationSelectView.render().el);
			this.currentView = userRegistrationSelectView;
		},
		
		showRegisterView : function(role) {
			this.clear();
			if (_.indexOf(App.allowedRoles, role) >= 0) {
				var userRegistration = new Models.User({
					"roles" : [ {
						"discriminator" : role
					} ]
				});
				var userRegistrationView = new Views.UserRegistrationView({
					model : userRegistration
				});
				$("#featured").html(userRegistrationView.render().el);
				this.currentView = userRegistrationView;
			} else {
				$("#featured").empty();
				this.currentView = undefined;
			}
		},
		
		showRegisterSuccessView : function() {
			this.clear();
			$("#content").html(_.template(tpl_user_registration_success));
		},
		
		showVerificationView : function(email, verificationNumber) {
			var self = this;
			var userRegistration;
			
			self.clear();
			
			userRegistration = new Models.User({
				"username" : email,
				"verificationNumber" : verificationNumber
			});
			
			userRegistration.verify({
				wait : true,
				success : function(model, resp) {
					var userVerificationView = new Views.UserVerificationView({
						model : userRegistration
					});
					$("#content").html(userVerificationView.render().el);
					self.currentView = userVerificationView;
				},
				error : function(model, resp, options) {
					$("#content").html($.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code")));
					self.currentView = undefined;
				}
			});
		}
	});
	
	Routers.AdminRouter = Backbone.Router.extend({
		
		initialize : function() {
			var self = this;
			
			_.extend(this, Backbone.Events);
			_.bindAll(this, "start", "showLoginView", "showHomeView", "showUserSearchView", "showUserView");
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);
			
			var languageView = new Views.LanguageView({});
			languageView.render();
			
			// Init LoggedOnUser
			App.loggedOnUser = new Models.User();
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
				App.loggedOnUser = new Models.User();
				App.loggedOnUser.on("user:loggedon", self.start);
				self.showLoginView();
				
				var popup = new Views.PopupView({
					type : "warning",
					message : $.i18n.prop("error.insufficient.privileges")
				});
				popup.show();
				
				return;
			}
			App.loggedOnUser.off("user:loggedon", self.start);
			
			// Create Header, Menu, and other side content and
			// bind them to the same loggedOnUser model
			var adminmenuView = new Views.AdminMenuView({
				model : App.loggedOnUser
			});
			adminmenuView.render();
			var usermenuView = new Views.UserMenuView({
				model : App.loggedOnUser
			});
			usermenuView.render();
			
			// Start Routing
			Backbone.history.start();
		},
		
		clear : function() {
			var self = this;
			if (_.isArray(self.currentView)) {
				_.each(self.currentView, function(view) {
					view.close();
				});
			} else if (_.isObject(self.currentView)) {
				self.currentView.close();
			}
			self.currentView = undefined;
			
			$("#featured").unbind();
			$("#featured").empty();
			$("#content").unbind();
			$("#content").empty();
		},
		
		refreshBreadcrumb : function(tags) {
			$("ul.breadcrumb").empty();
			_.each(tags, function(tag) {
				if (tag) {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>" + tag + "</li>");
				} else {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>...</li>");
				}
			});
		},
		
		showLoginView : function() {
			this.clear();
			
			var loginView = new Views.AdminLoginView({
				model : App.loggedOnUser
			});
			$("#content").html(loginView.render().el);
			
			this.currentView = loginView;
		},
		
		showHomeView : function() {
			this.clear();
			this.refreshBreadcrumb([ $.i18n.prop('menu_home') ]);
			$("#content").html("<h1>HOME</h1>");
		},
		
		showAccountView : function() {
			this.clear();
			var accountView = new Views.AccountView({
				model : App.loggedOnUser
			});
			
			this.refreshBreadcrumb([ $.i18n.prop('menu_account') ]);
			$("#content").append(accountView.render().el);
			
			this.currentView = accountView;
		},
		
		showUserSearchView : function(query) {
			var self = this;
			self.clear();
			var users = new Models.Users();
			users.on("user:selected", function(user) {
				if (user) {
					self.showUserView(user.id, user);
					self.navigate("user/" + user.id, {
						trigger : false
					});
				}
			}, this);
			var userSearchView = new Views.UserSearchView({
				"query" : query ? JSON.parse(decodeURI(query)) : undefined,
				collection : users
			});
			var userListView = new Views.UserListView({
				collection : users
			});
			
			this.refreshBreadcrumb([ $.i18n.prop('adminmenu_users') ]);
			$("#featured").append(userSearchView.render().el);
			$("#content").append(userListView.render().el);
			
			this.currentView = [ userSearchView, userListView ];
		},
		
		showUserView : function(id) {
			var self = this;
			var user, userView;
			var roles, roleView;
			
			// Create Models
			user = new Models.User({
				"id" : id
			});
			roles = new Models.Roles();
			roles.user = id;
			
			// Create Views, Add them to page
			self.clear();
			
			// Refresh Data
			user.fetch({
				cache : false,
				success : function(model, resp) {
					userView = new Views.AdminAccountView({
						model : user
					});
					self.refreshBreadcrumb([ $.i18n.prop('adminmenu_users'), $.i18n.prop('menu_user'), user.get("username") ]);
					roles.fetch({
						cache : false,
						success : function(collection, response) {
							// We assume collection has ONE primary role,
							// after role redesign
							roleView = new Views.AdminRoleEditView({
								model : collection.at(0)
							});
							
							$("#featured").append(userView.render().el);
							$("#content").append(roleView.render().el);
							self.currentView = [ userView, roleView ];
						}
					});
				}
			});
		}
	
	});
	
	/***************************************************************************
	 * ************ Routers.Router ***************************
	 **************************************************************************/
	Routers.Router = Backbone.Router.extend({
		initialize : function() {
			var self = this;
			
			_.extend(self, Backbone.Events);
			_.bindAll(self, "showLoginView", "showHomeView", "showAccountView", "showProfileView", "showAssistantsView", "showPositionView", "showRegisterView", "start");
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);
			
			var languageView = new Views.LanguageView({});
			languageView.render();
			
			// Init LoggedOnUser
			App.loggedOnUser = new Models.User();
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
			"assistants" : "showAssistantsView",
			"assistants/:userId" : "showAssistantsView",
			"position" : "showPositionView",
			"position/:positionId" : "showPositionView",
			"register" : "showRegisterView",
			"register/:registerId" : "showRegisterView",
			"requests" : "showRequestsView"
		},
		
		start : function(eventName, authToken) {
			var self = this;
			// Check that this is not an ADMINISTRATOR
			if (App.loggedOnUser.hasRole("ADMINISTRATOR")) {
				App.loggedOnUser = new Models.User();
				App.loggedOnUser.on("user:loggedon", self.start);
				self.showLoginView();
				var popup = new Views.PopupView({
					type : "warning",
					message : $.i18n.prop("error.administrator.login")
				});
				popup.show();
				
				return;
			}
			App.loggedOnUser.off("user:loggedon", self.start);
			
			// Add necessary data
			App.roles = new Models.Roles();
			App.roles.user = App.loggedOnUser.get("id");
			
			// Create Header, Menu, and other side content and
			// bind them to the same loggedOnUser model
			var menuView = new Views.MenuView({
				model : App.loggedOnUser
			});
			menuView.render();
			var usermenuView = new Views.UserMenuView({
				model : App.loggedOnUser
			});
			usermenuView.render();
			$("ul.breadcrumb").show();
			
			// Start Routing
			Backbone.history.start();
		},
		
		clear : function() {
			var self = this;
			if (_.isArray(self.currentView)) {
				_.each(self.currentView, function(view) {
					view.close();
				});
			} else if (_.isObject(self.currentView)) {
				self.currentView.close();
			}
			self.currentView = undefined;
			
			$("ul.breadcrumb").empty();
			$("#featured").unbind();
			$("#featured").empty();
			$("#content").unbind();
			$("#content").empty();
		},
		
		refreshBreadcrumb : function(tags) {
			$("ul.breadcrumb").empty();
			_.each(tags, function(tag) {
				if (tag) {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>" + tag + "</li>");
				} else {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>...</li>");
				}
			});
		},
		
		showLoginView : function() {
			this.clear();
			$("ul.breadcrumb").hide();
			var loginView = new Views.LoginView({
				model : App.loggedOnUser
			});
			$("#featured").html(loginView.render().el);
			
			this.currentView = loginView;
		},
		
		showHomeView : function() {
			this.clear();
			var homeView = new Views.HomeView({
				model : App.loggedOnUser
			});
			var announcementsView = new Views.AnnouncementListView({
				collection : App.roles
			});
			
			this.refreshBreadcrumb([ $.i18n.prop('menu_home') ]);
			$("#featured").html(homeView.render().el);
			$("#content").html(announcementsView.el);
			App.roles.fetch({
				cache : false
			});
			
			this.currentView = homeView;
		},
		
		showAccountView : function() {
			this.clear();
			var accountView = new Views.AccountView({
				model : App.loggedOnUser
			});
			this.refreshBreadcrumb([ $.i18n.prop('menu_account') ]);
			$("#content").append(accountView.render().el);
			
			this.currentView = accountView;
		},
		
		showProfileView : function() {
			var self = this;
			self.clear();
			// Refresh roles from server
			App.roles.fetch({
				cache : false,
				success : function(collection, response) {
					// We assume collection has ONE primary role, after role
					// redesign
					var roleView = new Views.RoleEditView({
						model : collection.at(0)
					});
					$("#content").html(roleView.render().el);
				}
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_profile') ]);
		},
		
		showAssistantsView : function(userId) {
			var self = this;
			var accountView = undefined;
			var userRoleInfoView = undefined;
			self.clear();
			
			var assistants = new Models.Users();
			assistants.on("user:selected", function(user) {
				if (user) {
					// Clean up
					if (accountView) {
						accountView.close();
					}
					if (userRoleInfoView) {
						userRoleInfoView.close();
					}
					$("#content").unbind();
					$("#content").empty();
					
					// Add
					self.refreshBreadcrumb([ $.i18n.prop('menu_assistants'), user.get("username") ]);
					
					userRoleInfoView = new Views.UserRoleInfoView({
						model : user
					});
					$("#content").append(userRoleInfoView.render().el);
					accountView = new Views.AssistantAccountView({
						model : user,
					});
					$("#content").append(accountView.render().el);
					
					if (!_.isUndefined(user.id)) {
						self.navigate("assistants/" + user.id, {
							trigger : false
						});
					} else {
						self.navigate("assistants", {
							trigger : false
						});
					}
				}
			}, this);
			
			var assistantsView = new Views.AssistantsView({
				collection : assistants
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_assistants') ]);
			$("#featured").append(assistantsView.el);
			
			assistants.fetch({
				cache : false,
				data : {
					manager : _.find(App.loggedOnUser.get("roles"), function(role) {
						return role.discriminator === "INSTITUTION_MANAGER";
					}).id
				},
				success : function() {
					if (!_.isUndefined(userId)) {
						assistants.trigger("user:selected", assistants.get(userId));
					}
				}
			});
			
			self.currentView = assistantsView;
		},
		
		showPositionView : function(positionId) {
			var self = this;
			var positions = new Models.Positions();
			var positionView = undefined;
			var positionListView = new Views.PositionListView({
				collection : positions,
				user : App.loggedOnUser.get("id")
			});
			
			positions.on("position:selected", function(position) {
				if (positionView) {
					positionView.close();
				}
				positionView = new Views.PositionEditView({
					model : position
				});
				// Update history
				if (position.id) {
					App.router.navigate("position/" + position.id, {
						trigger : false
					});
				} else {
					App.router.navigate("position", {
						trigger : false
					});
				}
				self.refreshBreadcrumb([ $.i18n.prop('menu_position'), position.get("name") ]);
				$("#content").unbind();
				$("#content").empty();
				$("#content").html(positionView.el);
				positionView.render();
			});
			
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_position') ]);
			$("#featured").html(positionListView.el);
			
			// Refresh positions from server
			positions.fetch({
				cache : false,
				data : {
					user : App.loggedOnUser.get("id")
				},
				success : function() {
					if (_.isUndefined(positionId)) {
					} else {
						positions.trigger("position:selected", positions.get(positionId));
					}
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			
			this.currentView = positionListView;
		},
		
		showRegisterView : function(registerId) {
			var self = this;
			var registries = new Models.Registries();
			var registerView = undefined;
			var registerListView = new Views.RegisterListView({
				collection : registries
			});
			registries.on("register:selected", function(register) {
				if (registerView) {
					registerView.close();
				}
				registerView = new Views.RegisterEditView({
					model : register
				});
				// Update history
				if (register.id) {
					App.router.navigate("register/" + register.id, {
						trigger : false
					});
				} else {
					App.router.navigate("register", {
						trigger : false
					});
				}
				self.refreshBreadcrumb([ $.i18n.prop('menu_register'), register.get("id") ]);
				$("#content").unbind();
				$("#content").empty();
				$("#content").html(registerView.el);
				registerView.render();
			});
			
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_register') ]);
			$("#featured").html(registerListView.el);
			// Refresh registries from server
			registries.fetch({
				cache : false,
				data : {
					institution : (function() {
						var institutions = App.loggedOnUser.getAssociatedInstitutions();
						if (institutions) {
							return institutions[0];
						}
					})()
				},
				success : function() {
					if (_.isUndefined(registerId)) {
					} else {
						registries.trigger("register:selected", registries.get(registerId));
					}
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			
			this.currentView = registerListView;
		}
	
	});
	
	return Routers;
});