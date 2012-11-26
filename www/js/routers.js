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

			$("#content").html("<div class=\"row-fluid\"><div id=\"user\" class=\"span5\"></div><div id=\"roles\" class=\"span7\"></div></div>");
			// Refresh Data
			user.fetch({
				cache : false,
				success : function(model, resp) {
					userView = new Views.AdminAccountView({
						className : "row-fluid",
						model : user
					});
					self.currentView = [ userView ];
					roles.fetch({
						cache : false,
						success : function(collection, response) {
							$("#content div#user").append(userView.render().el);
							collection.each(function(role) {
								roleView = new Views.AdminRoleEditView({
									className : "row-fluid",
									model : role
								});
								$("#content div#roles").append(roleView.render().el);
								self.currentView.push(roleView);
							});
						}
					});
					self.refreshBreadcrumb([ $.i18n.prop('adminmenu_users'), $.i18n.prop('menu_user'), user.get("username") ]);
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
			_.bindAll(self, "showLoginView", "showHomeView", "showAccountView", "showProfileView", "showUserView", "showInstitutionAssistantsView", "showMinistryAssistantsView", "showPositionView", "showPositionsView", "showRegistersView", "showProfessorCommitteesView", "showInstitutionRegulatoryFrameworkView", "showCandidateCandidacyView", "start");
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
			"profile/:roleId" : "showProfileView",
			"user/:userId" : "showUserView",
			"iassistants" : "showInstitutionAssistantsView",
			"iassistants/:userId" : "showInstitutionAssistantsView",
			"massistants" : "showMinistryAssistantsView",
			"massistants/:userId" : "showMinistryAssistantsView",
			"positions" : "showPositionsView",
			"positions/:positionId" : "showPositionsView",
			"position/:positionId" : "showPositionView",
			"registers" : "showRegistersView",
			"registers/:registerId" : "showRegistersView",
			"requests" : "showRequestsView",
			"professorCommittees" : "showProfessorCommitteesView",
			"regulatoryframeworks" : "showInstitutionRegulatoryFrameworkView",
			"regulatoryframeworks/:institutionId" : "showInstitutionRegulatoryFrameworkView",
			"sposition" : "showPositionSearchView",
			"sposition/:query" : "showPositionSearchView",
			"candidateCandidacies" : "showCandidateCandidacyView",
			"candidateCandidacies/:candidacyId" : "showCandidateCandidacyView"
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

		showProfileView : function(roleId) {
			var self = this;
			var rolesView = undefined;
			var roleView = undefined;
			self.clear();

			App.roles.on("role:selected", function(role) {
				if (roleView) {
					roleView.close();
				}
				roleView = new Views.RoleEditView({
					collection : App.roles,
					model : role
				});
				self.refreshBreadcrumb([ $.i18n.prop('menu_profile'), $.i18n.prop(role.get('discriminator')) ]);
				$("#content").html(roleView.render().el);

				self.navigate("profile/" + role.id, {
					trigger : false
				});
			});

			rolesView = new Views.RoleTabsView({
				collection : App.roles
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_profile') ]);
			$("#featured").html(rolesView.el);

			App.roles.fetch({
				cache : false,
				success : function() {
					if (!_.isUndefined(roleId)) {
						App.roles.trigger("role:selected", App.roles.get(roleId));
					} else {
						App.roles.trigger("role:selected", App.roles.at(0));
					}
				}
			});
			self.currentView = rolesView;
		},

		showUserView : function(id) {
			var self = this;
			var user, userView;
			var roles, roleViews = [];

			// Create Models
			user = new Models.User({
				"id" : id
			});
			roles = new Models.Roles();
			roles.user = id;

			// Create Views, Add them to page
			self.clear();

			$("#content").html("<div class=\"row-fluid\"><div id=\"user\" class=\"span5\"></div><div id=\"roles\" class=\"span7\"></div></div>");
			// Refresh Data
			user.fetch({
				cache : false,
				success : function(model, resp) {
					userView = new Views.UserView({
						className : "row-fluid",
						model : user
					});
					self.currentView = [ userView ];
					roles.fetch({
						cache : false,
						success : function(collection, response) {
							$("#content div#user").append(userView.render().el);
							collection.each(function(role) {
								roleView = new Views.RoleView({
									className : "row-fluid",
									model : role
								});
								$("#content div#roles").append(roleView.render().el);
								self.currentView.push(roleView);
							});
						}
					});
					self.refreshBreadcrumb([ $.i18n.prop('adminmenu_users'), $.i18n.prop('menu_user'), user.get("username") ]);
				}
			});
		},

		showInstitutionAssistantsView : function(userId) {
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
					self.refreshBreadcrumb([ $.i18n.prop('menu_iassistants'), user.get("username") ]);

					userRoleInfoView = new Views.UserRoleInfoView({
						model : user
					});
					$("#content").append(userRoleInfoView.render().el);
					accountView = new Views.AssistantAccountView({
						model : user
					});
					$("#content").append(accountView.render().el);

					if (!_.isUndefined(user.id)) {
						self.navigate("iassistants/" + user.id, {
							trigger : false
						});
					} else {
						self.navigate("iassistants", {
							trigger : false
						});
					}
				}
			}, this);

			var assistantsView = new Views.InstitutionAssistantListView({
				collection : assistants
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_iassistants') ]);
			$("#featured").append(assistantsView.el);

			assistants.fetch({
				cache : false,
				data : {
					im : App.loggedOnUser.getRole("INSTITUTION_MANAGER").id
				},
				success : function() {
					if (!_.isUndefined(userId)) {
						assistants.trigger("user:selected", assistants.get(userId));
					}
				}
			});

			self.currentView = assistantsView;
		},

		showMinistryAssistantsView : function(userId) {
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
					self.refreshBreadcrumb([ $.i18n.prop('menu_massistants'), user.get("username") ]);

					userRoleInfoView = new Views.UserRoleInfoView({
						model : user
					});
					$("#content").append(userRoleInfoView.render().el);
					accountView = new Views.AssistantAccountView({
						model : user
					});
					$("#content").append(accountView.render().el);

					if (!_.isUndefined(user.id)) {
						self.navigate("massistants/" + user.id, {
							trigger : false
						});
					} else {
						self.navigate("massistants", {
							trigger : false
						});
					}
				}
			}, this);

			var assistantsView = new Views.MinistryAssistantListView({
				collection : assistants
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_massistants') ]);
			$("#featured").append(assistantsView.el);

			assistants.fetch({
				cache : false,
				data : {
					mm : App.loggedOnUser.getRole("MINISTRY_MANAGER").id
				},
				success : function() {
					if (!_.isUndefined(userId)) {
						assistants.trigger("user:selected", assistants.get(userId));
					}
				}
			});

			self.currentView = assistantsView;
		},

		showPositionsView : function(positionId) {
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
				// Select Edit or Simple View based on loggedOnUser
				if (App.loggedOnUser.isAssociatedWithDepartment(position.get("department"))) {
					positionView = new Views.PositionEditView({
						model : position
					});
				} else {
					positionView = new Views.PositionView({
						model : position
					});
				}
				// Update history
				App.router.navigate("positions/" + position.id, {
					trigger : false
				});
				// Add to UI
				self.refreshBreadcrumb([ $.i18n.prop('menu_positions'), position.get("name") ]);
				$("#content").unbind();
				$("#content").empty();
				$("#content").html(positionView.el);

				// Fetch
				position.fetch({
					cache : false
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_positions') ]);
			$("#featured").html(positionListView.el);

			// Refresh positions from server
			positions.fetch({
				cache : false,
				success : function() {
					if (!_.isUndefined(positionId)) {
						var selectedPosition = positions.get(positionId);
						if (!selectedPosition) {
							selectedPosition = new Models.Position({
								id : positionId
							});
							selectedPosition.fetch({
								cache : false,
								wait : true,
								success : function() {
									positions.add(selectedPosition);
									positions.trigger("position:selected", selectedPosition);
								},
								error : function(model, resp, options) {
									var popup = new Views.PopupView({
										type : "error",
										message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							positions.trigger("position:selected", selectedPosition);
						}
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

		showPositionView : function(positionId) {
			var self = this;
			var position = new Models.Position({
				id : positionId
			});
			var positionView = new Views.PositionView({
				model : position
			});
			// Add to UI
			$("#content").unbind();
			$("#content").empty();
			$("#content").html(positionView.el);
			// Fetch
			position.fetch({
				cache : false,
				success : function() {
					self.refreshBreadcrumb([ $.i18n.prop('menu_position'), position.get("name") ]);
				}
			});
			this.currentView = positionView;
		},

		showRegistersView : function(registerId) {
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
				// Select Edit or Simple View based on loggedOnUser
				if (register.isNew() || App.loggedOnUser.isAssociatedWithInstitution(register.get("institution"))) {
					registerView = new Views.RegisterEditView({
						model : register
					});
				} else {
					registerView = new Views.RegisterView({
						model : register
					});
				}
				// Update history
				if (register.id) {
					App.router.navigate("registers/" + register.id, {
						trigger : false
					});
				} else {
					App.router.navigate("registers", {
						trigger : false
					});
				}

				$("#content").unbind();
				$("#content").empty();
				$("#content").html(registerView.el);

				register.fetch({
					cache : false,
					success : function() {
						self.refreshBreadcrumb([ $.i18n.prop('menu_registers'), register.get("title") ]);
						registerView.render();
					}
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_registers') ]);
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
					if (!_.isUndefined(registerId)) {
						var selectedRegister = registries.get(registerId);
						if (!selectedRegister) {
							selectedRegister = new Models.Register({
								id : registerId
							});
							selectedRegister.fetch({
								cache : false,
								wait : true,
								success : function() {
									registries.add(selectedRegister);
									registries.trigger("register:selected", selectedRegister);
								},
								error : function(model, resp, options) {
									var popup = new Views.PopupView({
										type : "error",
										message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							registries.trigger("register:selected", selectedRegister);
						}
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
		},

		showProfessorCommitteesView : function() {
			var self = this;
			var professorCommittees = new Models.ProfessorCommittees({}, {
				professor : App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") ? App.loggedOnUser.getRole("PROFESSOR_DOMESTIC").id : App.loggedOnUser.getRole("PROFESSOR_FOREIGN").id
			});
			var professorCommitteesView = new Views.ProfessorCommitteesView({
				collection : professorCommittees
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_professorCommittees') ]);
			$("#content").html(professorCommitteesView.el);

			// Refresh professorCommittees from server
			professorCommittees.fetch({
				cache : false,
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			self.currentView = professorCommitteesView;
		},

		showInstitutionRegulatoryFrameworkView : function(institutionRFId) {
			var self = this;
			var institutionRFs = new Models.InstitutionRegulatoryFrameworks();
			var institutionRegulatoryFrameworkView = undefined;
			var institutionRegulatoryFrameworkListView = new Views.InstitutionRegulatoryFrameworkListView({
				collection : institutionRFs
			});
			institutionRFs.on("institutionRF:selected", function(institutionRF) {
				if (institutionRegulatoryFrameworkView) {
					institutionRegulatoryFrameworkView.close();
				}
				// Select Edit or Simple View based on loggedOnUser
				institutionRegulatoryFrameworkView = App.loggedOnUser.isAssociatedWithInstitution(institutionRF.get("institution")) ? new Views.InstitutionRegulatoryFrameworkEditView({
					model : institutionRF
				}) : new Views.InstitutionRegulatoryFrameworkView({
					model : institutionRF
				});
				self.refreshBreadcrumb([ $.i18n.prop('menu_regulatoryframeworks') ]);
				$("#content").html(institutionRegulatoryFrameworkView.el);
				// Update history
				App.router.navigate("regulatoryframeworks/" + institutionRF.id, {
					trigger : false
				});
				institutionRF.fetch({
					cache : false,
					success : function(model, resp, options) {
						institutionRF.trigger("change");

					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_regulatoryframeworks') ]);
			$("#featured").html(institutionRegulatoryFrameworkListView.el);

			// Refresh institutionRFs from server
			institutionRFs.fetch({
				cache : false,
				success : function() {
					if (!_.isUndefined(institutionRFId)) {
						var selectedInstitutionRF = institutionRFs.get(institutionRFId);
						if (!selectedInstitutionRF) {
							selectedInstitutionRF = new Models.InstitutionRegulatoryFramework({
								id : institutionRFId
							});
							selectedInstitutionRF.fetch({
								cache : false,
								wait : true,
								success : function() {
									institutionRFs.add(selectedInstitutionRF);
									institutionRFs.trigger("institutionRF:selected", selectedInstitutionRF);
								},
								error : function(model, resp, options) {
									var popup = new Views.PopupView({
										type : "error",
										message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							institutionRFs.trigger("institutionRF:selected", selectedInstitutionRF);
						}
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
			self.currentView = institutionRegulatoryFrameworkListView;
		},

		showPositionSearchView : function(query) {
			var self = this;
			self.clear();

			var positions = new Models.Positions();
			positions.url = positions.url + "/search";
			positions.on("position:selected", function(position) {
				if (position) {
					var newCandidacy = new Models.Candidacy();
					newCandidacy.save({
						candidate : App.loggedOnUser.getRole("CANDIDATE"),
						candidacies : {
							position : position.toJSON()
						}
					}, {
						wait : true,
						success : function(model, resp) {
							App.router.navigate("candidateCandidacies/" + model.id, {
								trigger : true
							});
							var popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
							popup.show();
						},
						error : function(model, resp, options) {
							var popup = new Views.PopupView({
								type : "error",
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			}, this);
			var positionSearchView = new Views.PositionSearchView({
				"query" : query ? JSON.parse(decodeURI(query)) : undefined,
				collection : positions
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_sposition') ]);
			$("#content").append(positionSearchView.el);
			if (query) {
				positions.fetch({
					cache : false,
					data : JSON.parse(decodeURI(query))
				});
			} else {
				positions.fetch({
					cache : false
				});
			}
			self.currentView = positionSearchView;
		},

		showCandidateCandidacyView : function(candidacyId) {
			var self = this;
			var candidacyEditView = undefined;
			var candidateCandidacies = new Models.CandidateCandidacies({}, {
				candidate : App.loggedOnUser.getRole("CANDIDATE").id
			});
			var candidateCandidacyListView = new Views.CandidateCandidacyListView({
				collection : candidateCandidacies
			});
			candidateCandidacies.on("candidacy:selected", function(candidacy) {
				if (candidacyEditView) {
					candidacyEditView.close();
				}
				// Select Edit or Simple View based on loggedOnUser
				candidacyEditView = new Views.CandidacyEditView({
					model : candidacy
				});
				// Update history
				App.router.navigate("candidateCandidacies/" + candidacy.id, {
					trigger : false
				});

				self.refreshBreadcrumb([ $.i18n.prop('menu_candidateCandidacies'), candidacy.id ]);
				$("#content").unbind();
				$("#content").empty();
				$("#content").html(candidacyEditView.el);

				candidacy.fetch({
					cache : false,
					success : function() {
						candidacy.trigger("change");
					}
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_candidateCandidacies') ]);
			$("#featured").html(candidateCandidacyListView.el);

			// Refresh candidateCandidacies from server
			candidateCandidacies.fetch({
				cache : false,
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				},
				success : function() {
					if (!_.isUndefined(candidacyId)) {
						var selectedCandidacy = candidateCandidacies.get(candidacyId);
						if (!selectedCandidacy) {
							selectedCandidacy = new Models.Candidacy({
								id : candidacyId
							});
							selectedCandidacy.fetch({
								cache : false,
								wait : true,
								success : function() {
									candidateCandidacies.add(selectedCandidacy);
									candidateCandidacies.trigger("candidacy:selected", selectedCandidacy);
								},
								error : function(model, resp, options) {
									var popup = new Views.PopupView({
										type : "error",
										message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							candidateCandidacies.trigger("candidacy:selected", selectedCandidacy);
						}
					}
				}
			});
			self.currentView = candidateCandidacyListView;
		}

	});

	return Routers;
});