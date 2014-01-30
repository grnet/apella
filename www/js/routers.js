/*global define */
define([ "jquery", "underscore", "backbone", "application", "models", "views", "text!tpl/landing-page.html", "text!tpl/user-registration-success.html"
], function ($, _, Backbone, App, Models, Views, tpl_landing_page, tpl_user_registration_success) {
	"use strict";

	var Routers = {};

	Routers.Router = Backbone.Router.extend({

		initialize: function () {
			var languageView;
			_.extend(this, Backbone.Events);
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);

			languageView = new Views.LanguageView({});
			languageView.render();

			$("#content").html(_.template(tpl_landing_page));
		}
	});

	/**************************************************************************
	 * ************ Routers.RegistrationRouter ********************************
	 **************************************************************************/
	Routers.RegistrationRouter = Backbone.Router.extend({

		initialize: function () {
			var languageView;

			_.extend(this, Backbone.Events);
			_.bindAll(this, "showRegisterView", "showVerificationView", "showRegisterSelectView", "showRegisterSuccessView");
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);

			App.institutions = new Models.Institutions();

			languageView = new Views.LanguageView({});
			languageView.render();

			Backbone.history.start();
		},

		routes: {
			"username=:username&verification=:verificationNumber": "showVerificationView",
			"": "showRegisterSelectView",
			"profile=:role": "showRegisterView",
			"success": "showRegisterSuccessView"
		},

		clear: function () {
			var self = this;
			if (_.isArray(self.currentView)) {
				_.each(self.currentView, function (view) {
					view.close();
				});
			} else if (_.isObject(self.currentView)) {
				self.currentView.close();
			}
			self.currentView = undefined;
			$("#content").unbind();
			$("#content").empty();
		},

		showRegisterSelectView: function () {
			var userRegistrationSelectView = new Views.UserRegistrationSelectView({});
			$("#content").html(userRegistrationSelectView.render().el);
			this.currentView = userRegistrationSelectView;
		},

		showRegisterView: function (role) {
			var userRegistration;
			var userRegistrationView;
			this.clear();
			if (_.indexOf(App.usernameRegistrationRoles, role) >= 0) {
				userRegistration = new Models.User({
					"roles": [
						{
							"discriminator": role
						}
					]
				});
				userRegistrationView = new Views.UserRegistrationView({
					model: userRegistration
				});
				$("#content").html(userRegistrationView.render().el);
				this.currentView = userRegistrationView;
			} else {
				$("#content").empty();
				this.currentView = undefined;
			}
		},

		showRegisterSuccessView: function () {
			this.clear();
			$("#content").html(_.template(tpl_user_registration_success));
		},

		showVerificationView: function (username, verificationNumber) {
			var self = this;
			var userRegistration;

			self.clear();

			userRegistration = new Models.User({
				"username": username,
				"verificationNumber": verificationNumber
			});

			userRegistration.verify({
				wait: true,
				success: function (model, resp) {
					var userVerificationView = new Views.UserVerificationView({
						model: userRegistration
					});
					$("#content").html(userVerificationView.render().el);
					self.currentView = userVerificationView;
				},
				error: function (model, resp, options) {
					$("#content").html("<h2>" + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code")) + "</h2>");
					self.currentView = undefined;
				}
			});
		}
	});

	/**************************************************************************
	 * ************ Routers.HelpdeskRouter ************************************
	 **************************************************************************/
	Routers.HelpdeskRouter = Backbone.Router.extend({

		initialize: function () {
			var languageView;

			_.extend(this, Backbone.Events);
			_.bindAll(this, "showJiraIssueEditView");
			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);

			languageView = new Views.LanguageView({});
			languageView.render();

			Backbone.history.start();
		},

		routes: {
			"": "showJiraIssueEditView"
		},

		showJiraIssueEditView: function () {
			var self = this;
			var issueEditView;
			// 1. New Model
			var model = new Models.JiraIssue({
				status: "OPEN"
			});
			model.url = model.urlRoot + "public";
			model.on("sync", function (model) {
				// Reset Issue if users wants to create a new one
				model.set(_.defaults({
					status: "OPEN"
				}, model.defaults));
			});

			// 2. Init View
			issueEditView = new Views.PublicJiraIssueEditView({
				model: model
			});
			//3. Add to page
			$("#pageTitle").html($.i18n.prop('PublicJiraIssuesTitle'));
			$("#content").html(issueEditView.render().el);
			// 4. Set as currentView
			self.currentView = issueEditView;
		}

	});

	/**************************************************************************
	 * ************ Routers.ApellaRouter **************************************
	 **************************************************************************/
	Routers.ApellaRouter = Backbone.Router.extend({
		initialize: function () {
			var self = this;
			var languageView;

			_.extend(self, Backbone.Events);
			_.bindAll(self, "setTitle", "showLoginView", "showHomeView", "showAccountView", "showProfileView", "showUserView", "showInstitutionAssistantsView",
				"showMinistryAssistantsView", "showPositionView", "showPositionsView", "showRegistersView", "showProfessorCommitteesView", "showProfessorEvaluationsView",
				"showInstitutionRegulatoryFrameworkView", "showCandidateCandidacyView", "showCandidacyView", "showAdminUserSearchView", "showIssueListView", "start");

			self.on("route", function (routefn) {
				self.setTitle(routefn);
			});

			$(document).ajaxStart(App.blockUI);
			$(document).ajaxStop(App.unblockUI);

			languageView = new Views.LanguageView({});
			languageView.render();

			// Init LoggedOnUser
			App.loggedOnUser = new Models.User();
			App.loggedOnUser.on("user:loggedon", self.start);
			App.loggedOnUser.fetch({
				url: "/dep/rest/user/loggedon",
				cache: false,
				success: function (model, resp) {
					App.loggedOnUser.trigger("user:loggedon");
				},
				error: function (model, resp, options) {
					self.showLoginView();
				}
			});
		},

		routes: {
			"": "showHomeView",
			"completeAccount": "showIncompleteAccountView",
			"account": "showAccountView",
			"profile": "showProfileView",
			"profile/:roleId": "showProfileView",
			"user/:userId": "showUserView",
			"iassistants": "showInstitutionAssistantsView",
			"iassistants/:userId": "showInstitutionAssistantsView",
			"massistants": "showMinistryAssistantsView",
			"massistants/:userId": "showMinistryAssistantsView",
			"positions": "showPositionsView",
			"positions/:positionId": "showPositionsView",
			"positions/:positionId/:tab": "showPositionsView",
			"position/:positionId": "showPositionView",
			"position/:positionId/:order": "showPositionView",
			"registers": "showRegistersView",
			"registers/:registerId": "showRegistersView",
			"professorCommittees": "showProfessorCommitteesView",
			"professorEvaluations": "showProfessorEvaluationsView",
			"regulatoryframeworks": "showInstitutionRegulatoryFrameworkView",
			"regulatoryframeworks/:institutionId": "showInstitutionRegulatoryFrameworkView",
			"sposition": "showPositionSearchView",
			"candidateCandidacies": "showCandidateCandidacyView",
			"candidateCandidacies/:candidacyId": "showCandidateCandidacyView",
			"candidacy/:candidacyId": "showCandidacyView",
			"issues": "showIssueListView",

			// ADMIN ROUTES
			"adminusers": "showAdminUserSearchView",
			"adminusers/:query": "showAdminUserSearchView"
		},

		start: function (eventName, authToken) {
			var self = this;
			var menuView;
			var usermenuView;

			App.loggedOnUser.off("user:loggedon", self.start);

			// Add necessary data
			App.roles = new Models.Roles();
			App.roles.user = App.loggedOnUser.get("id");
			App.roles.on("sync", function () {
				// When user changes his profile, we re-fetch loggedOnUser ->
				// triggers re-render on Menus and permissions
				App.loggedOnUser.fetch({
					url: "/dep/rest/user/loggedon",
					wait: true,
					cache: false
				});
			});
			App.loggedOnUser.on("change", function () {
				window.console.log("loggedOn:changed");
			});
			// Create Header, Menu, and other side content and
			// bind them to the same loggedOnUser model
			menuView = new Views.MenuView({
				model: App.loggedOnUser
			});
			menuView.render();
			usermenuView = new Views.UserMenuView({
				model: App.loggedOnUser
			});

			usermenuView.render();
			$("ul.breadcrumb").show();

			// Start Routing
			Backbone.history.start();
			if (App.loggedOnUser.isAccountIncomplete()) {
				self.navigate("completeAccount", {
					trigger: true
				});
			}
		},

		clear: function () {
			var self = this;
			if (_.isArray(self.currentView)) {
				_.each(self.currentView, function (view) {
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

		setTitle: function (route) {
			var title = route.replace(/^show/, '').replace(/View/, 'Title');
			$("#pageTitle").html($.i18n.prop(title));
		},

		refreshBreadcrumb: function (tags) {
			$("ul.breadcrumb").empty();
			_.each(tags, function (tag) {
				if (tag) {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>" + tag + "</li>");
				} else {
					$("ul.breadcrumb").append("<li><span class=\"divider\">/</span>...</li>");
				}
			});
		},

		showLoginView: function () {
			var self = this;
			var loginView;

			self.clear();
			$("ul.breadcrumb").hide();
			loginView = new Views.LoginView({
				model: App.loggedOnUser
			});
			$("#featured").html(loginView.render().el);

			self.currentView = loginView;
		},

		showHomeView: function () {
			var self = this;
			var homeView;
			self.clear();
			homeView = new Views.HomeView({
				model: App.loggedOnUser
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_home') ]);

			$("#featured").html(homeView.render().el);
			App.roles.fetch({
				cache: false,
				reset: true
			});

			self.currentView = homeView;
		},

		showAccountView: function () {
			var self = this;
			var accountView;
			self.clear();
			accountView = new Views.AccountView({
				model: App.loggedOnUser
			});
			this.refreshBreadcrumb([ $.i18n.prop('menu_account') ]);
			$("#content").append(accountView.render().el);

			self.currentView = accountView;
		},

		showIncompleteAccountView: function () {
			var self = this;
			var accountView;
			self.clear();
			accountView = new Views.AccountView({
				model: App.loggedOnUser
			});
			// When sync completes user will have completed the account, direct to profile
			self.listenToOnce(App.loggedOnUser, "sync:save", function () {
				App.router.navigate("profile", {
					trigger: true
				});
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_shibolethAccount') ]);
			$("#content").append(accountView.render().el);

			self.currentView = accountView;
		},

		showProfileView: function (roleId) {
			var self = this;
			var rolesView;
			var roleView;
			self.clear();

			App.roles.on("role:selected", function (role) {
				if (roleView) {
					roleView.close();
				}
				roleView = new Views.RoleEditView({
					collection: App.roles,
					model: role
				});
				self.refreshBreadcrumb([ $.i18n.prop('menu_profile'), $.i18n.prop(role.get('discriminator')) ]);
				$("#content").html(roleView.render().el);

				self.navigate("profile/" + role.id, {
					trigger: false
				});
			});

			rolesView = new Views.RoleTabsView({
				collection: App.roles
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_profile') ]);
			$("#featured").html(rolesView.el);

			App.roles.fetch({
				cache: false,
				reset: true,
				success: function () {
					if (!_.isUndefined(roleId)) {
						App.roles.trigger("role:selected", App.roles.get(roleId));
					} else {
						App.roles.trigger("role:selected", App.roles.at(0));
					}
				}
			});
			self.currentView = rolesView;
		},

		showUserView: function (id) {
			var self = this;
			var user, roles;

			function displayContent() {
				var userView;
				self.clear();
				self.currentView = [];

				if (App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
					userView = new Views.UserHelpdeskView({
						model: user,
						collection: roles
					});
				} else {
					// Simple View Mode
					userView = new Views.UserView({
						model: user,
						collection: roles
					});
				}
				$("#content").html(userView.render().el);
				self.refreshBreadcrumb([ $.i18n.prop('menu_user'), user.getDisplayName() ]);
				self.currentView.push(userView);
			}

			// Create Models
			user = new Models.User({
				"id": id
			});
			roles = new Models.Roles();
			roles.user = id;
			// Fetch Data
			user.fetch({
				cache: false,
				success: function (model, resp) {
					roles.fetch({
						cache: false,
						reset: true,
						success: function (collection, response) {
							displayContent();
						}
					});
				}
			});
		},

		showInstitutionAssistantsView: function (userId) {
			var self = this;
			var accountView;
			var userRoleInfoView;
			var assistants;
			var assistantsView;

			self.clear();

			assistants = new Models.Users();
			assistants.on("user:selected", function (user) {
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
					self.refreshBreadcrumb([ $.i18n.prop('menu_iassistants'), user.getDisplayName() ]);

					accountView = new Views.AssistantAccountView({
						model: user
					});
					$("#content").append(accountView.render().el);

					if (!_.isUndefined(user.id)) {
						self.navigate("iassistants/" + user.id, {
							trigger: false
						});
					} else {
						self.navigate("iassistants", {
							trigger: false
						});
					}
				}
			}, this);

			assistantsView = new Views.InstitutionAssistantListView({
				collection: assistants
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_iassistants') ]);
			$("#featured").append(assistantsView.el);

			assistants.fetch({
				cache: false,
				reset: true,
				data: {
					im: App.loggedOnUser.getRole("INSTITUTION_MANAGER").id
				},
				success: function () {
					if (!_.isUndefined(userId)) {
						assistants.trigger("user:selected", assistants.get(userId));
					}
				}
			});

			self.currentView = assistantsView;
		},

		showMinistryAssistantsView: function (userId) {
			var self = this;
			var accountView;
			var userRoleInfoView;
			var assistants;
			var assistantsView;

			self.clear();

			assistants = new Models.Users();
			assistants.on("user:selected", function (user) {
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
					self.refreshBreadcrumb([ $.i18n.prop('menu_massistants'), user.getDisplayName() ]);

					userRoleInfoView = new Views.UserRoleInfoView({
						model: user
					});
					$("#content").append(userRoleInfoView.render().el);
					accountView = new Views.AssistantAccountView({
						model: user
					});
					$("#content").append(accountView.render().el);

					if (!_.isUndefined(user.id)) {
						self.navigate("massistants/" + user.id, {
							trigger: false
						});
					} else {
						self.navigate("massistants", {
							trigger: false
						});
					}
				}
			}, this);

			assistantsView = new Views.MinistryAssistantListView({
				collection: assistants
			});
			self.refreshBreadcrumb([ $.i18n.prop('menu_massistants') ]);
			$("#featured").append(assistantsView.el);

			assistants.fetch({
				cache: false,
				reset: true,
				data: {
					mm: App.loggedOnUser.getRole("MINISTRY_MANAGER").id
				},
				success: function () {
					if (!_.isUndefined(userId)) {
						assistants.trigger("user:selected", assistants.get(userId));
					}
				}
			});

			self.currentView = assistantsView;
		},

		showPositionsView: function (positionId, tab) {
			var self = this;
			var positions = new Models.Positions();
			var positionView;
			var positionListView = new Views.PositionListView({
				collection: positions,
				user: App.loggedOnUser.get("id")
			});

			positions.on("position:selected", function (position, tab) {
				if (positionView) {
					positionView.close();
				}
				// Fetch
				position.fetch({
					cache: false,
					wait: true,
					success: function () {
						// Select Edit, Helpdesk or Simple View based on loggedOnUser
						if (position.isEditableBy(App.loggedOnUser)) {
							positionView = new Views.PositionEditView({
								tab: tab || "main",
								model: position
							});
							// Update history
							App.router.navigate("positions/" + position.id + "/" + (tab || "main"), {
								trigger: false
							});
						} else if (App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
							positionView = new Views.PositionHelpdeskView({
								model: position
							});
							// Update history
							App.router.navigate("positions/" + position.id, {
								trigger: false
							});
						} else {
							positionView = new Views.PositionView({
								model: position
							});
							// Update history
							App.router.navigate("positions/" + position.id, {
								trigger: false
							});
						}
						// Add to UI
						self.refreshBreadcrumb([ $.i18n.prop('menu_positions'), position.get("name") ]);
						$("#content").unbind();
						$("#content").empty();
						$("#content").html(positionView.render().el);
					}
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_positions') ]);
			$("#featured").html(positionListView.el);

			// Refresh positions from server
			positions.fetch({
				cache: false,
				reset: true,
				success: function () {
					var selectedPosition;
					if (!_.isUndefined(positionId)) {
						selectedPosition = positions.get(positionId);
						if (!selectedPosition) {
							// Not permanent, so fetch it
							selectedPosition = new Models.Position({
								id: positionId
							});
							selectedPosition.fetch({
								cache: false,
								wait: true,
								success: function () {
									positions.add(selectedPosition);
									positions.trigger("position:selected", selectedPosition, tab);
								},
								error: function (model, resp, options) {
									var popup = new Views.PopupView({
										type: "error",
										message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							positions.trigger("position:selected", selectedPosition, tab);
						}
					}
				},
				error: function (model, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			this.currentView = positionListView;
		},

		showPositionView: function (positionId, order) {
			var self = this;
			var positionView;
			var position = new Models.Position({
				id: positionId
			});
			if (App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
				positionView = new Views.PositionHelpdeskView({
					model: position
				});
			} else {
				positionView = new Views.PositionView({
					model: position
				});
			}
			// Add to UI
			self.clear();
			$("#content").unbind();
			$("#content").empty();
			$("#content").html(positionView.el);
			// Fetch
			position.fetch({
				url: position.url() + (order ? "?order=" + order : ""),
				cache: false,
				success: function () {
					self.refreshBreadcrumb([ $.i18n.prop('menu_position'), position.get("name") ]);
				}
			});
			this.currentView = positionView;
		},

		showRegistersView: function (registerId) {
			var self = this;
			var registries = new Models.Registries();
			var registerView;
			var registerListView = new Views.RegisterListView({
				collection: registries
			});
			registries.on("register:selected", function (register) {
				if (registerView) {
					registerView.close();
				}
				// Select Edit or Simple View based on loggedOnUser
				if (register.isEditableBy(App.loggedOnUser)) {
					registerView = new Views.RegisterEditView({
						model: register
					});
				} else {
					registerView = new Views.RegisterView({
						model: register
					});
				}
				// Update history
				if (register.id) {
					App.router.navigate("registers/" + register.id, {
						trigger: false
					});
				} else {
					App.router.navigate("registers", {
						trigger: false
					});
				}

				$("#content").unbind();
				$("#content").empty();
				$("#content").html(registerView.render().el);

				register.fetch({
					cache: false,
					success: function () {
						self.refreshBreadcrumb([ $.i18n.prop('menu_registers'), register.get("title") ]);
					}
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_registers') ]);
			$("#featured").html(registerListView.el);
			// Refresh registries from server
			registries.fetch({
				cache: false,
				reset: true,
				data: {
					institution: (function () {
						var institutions = App.loggedOnUser.getAssociatedInstitutions();
						if (institutions) {
							return institutions[0];
						}
					}())
				},
				success: function () {
					if (!_.isUndefined(registerId)) {
						var selectedRegister = registries.get(registerId);
						if (!selectedRegister) {
							selectedRegister = new Models.Register({
								id: registerId
							});
							selectedRegister.fetch({
								cache: false,
								wait: true,
								success: function () {
									registries.add(selectedRegister);
									registries.trigger("register:selected", selectedRegister);
								},
								error: function (model, resp, options) {
									var popup = new Views.PopupView({
										type: "error",
										message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							registries.trigger("register:selected", selectedRegister);
						}
					}
				},
				error: function (model, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			this.currentView = registerListView;
		},

		showProfessorCommitteesView: function () {
			var self = this;
			var professorCommittees = new Models.ProfessorCommittees({}, {
				professor: App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") ? App.loggedOnUser.getRole("PROFESSOR_DOMESTIC").id : App.loggedOnUser.getRole("PROFESSOR_FOREIGN").id
			});
			var professorCommitteesView = new Views.ProfessorCommitteesView({
				collection: professorCommittees
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_professorCommittees') ]);
			$("#content").html(professorCommitteesView.el);

			// Refresh professorCommittees from server
			professorCommittees.fetch({
				cache: false,
				reset: true,
				error: function (collection, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			self.currentView = professorCommitteesView;
		},

		showProfessorEvaluationsView: function () {
			var self = this;
			var professorEvaluations = new Models.ProfessorEvaluations({}, {
				professor: App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") ? App.loggedOnUser.getRole("PROFESSOR_DOMESTIC").id : App.loggedOnUser.getRole("PROFESSOR_FOREIGN").id
			});
			var professorEvaluationsView = new Views.ProfessorEvaluationsView({
				collection: professorEvaluations
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_professorEvaluations') ]);
			$("#content").html(professorEvaluationsView.el);

			// Refresh professorCommittees from server
			professorEvaluations.fetch({
				cache: false,
				reset: true,
				error: function (model, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			self.currentView = professorEvaluationsView;
		},

		showInstitutionRegulatoryFrameworkView: function (institutionRFId) {
			var self = this;
			var institutionRFs = new Models.InstitutionRegulatoryFrameworks();
			var institutionRegulatoryFrameworkView;
			var institutionRegulatoryFrameworkListView = new Views.InstitutionRegulatoryFrameworkListView({
				collection: institutionRFs
			});
			institutionRFs.on("institutionRF:selected", function (institutionRF) {
				if (institutionRegulatoryFrameworkView) {
					institutionRegulatoryFrameworkView.close();
				}
				// Select Edit or Simple View based on loggedOnUser
				if (institutionRF.isEditableBy(App.loggedOnUser)) {
					institutionRegulatoryFrameworkView = new Views.InstitutionRegulatoryFrameworkEditView({
						model: institutionRF
					});
				} else {
					institutionRegulatoryFrameworkView = new Views.InstitutionRegulatoryFrameworkView({
						model: institutionRF
					});
				}
				self.refreshBreadcrumb([ $.i18n.prop('menu_regulatoryframeworks') ]);
				$("#content").html(institutionRegulatoryFrameworkView.render().el);
				// Update history
				App.router.navigate("regulatoryframeworks/" + institutionRF.id, {
					trigger: false
				});
				institutionRF.fetch({
					cache: false,
					error: function (model, resp, options) {
						var popup = new Views.PopupView({
							type: "error",
							message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
				cache: false,
				reset: true,
				success: function () {
					if (!_.isUndefined(institutionRFId)) {
						var selectedInstitutionRF = institutionRFs.get(institutionRFId);
						if (!selectedInstitutionRF) {
							selectedInstitutionRF = new Models.InstitutionRegulatoryFramework({
								id: institutionRFId
							});
							selectedInstitutionRF.fetch({
								cache: false,
								wait: true,
								success: function () {
									institutionRFs.add(selectedInstitutionRF);
									institutionRFs.trigger("institutionRF:selected", selectedInstitutionRF);
								},
								error: function (model, resp, options) {
									var popup = new Views.PopupView({
										type: "error",
										message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						} else {
							institutionRFs.trigger("institutionRF:selected", selectedInstitutionRF);
						}
					}
				},
				error: function (model, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			self.currentView = institutionRegulatoryFrameworkListView;
		},

		showPositionSearchView: function () {
			var self = this;
			var criteria = new Models.PositionSearchCriteria();
			var positions = new Models.Positions();
			var positionSearchCriteriaView;
			var positionSearchResultView;
			self.clear();
			// Models
			positions.url += "/criteria/search";

			// Event Handlers
			criteria.on("criteria:search", function (criteria) {
				positions.fetch({
					cache: false,
					reset: true,
					data: {
						"criteria": JSON.stringify(criteria)
					}
				});
			});
			positions.on("position:selected", function (position) {
				var newCandidacy;
				if (position) {
					newCandidacy = new Models.Candidacy();
					newCandidacy.save({
						candidate: App.loggedOnUser.getRole("CANDIDATE"),
						candidacies: {
							position: position.toJSON()
						}
					}, {
						wait: true,
						success: function (model, resp) {
							App.router.navigate("candidateCandidacies/" + model.id, {
								trigger: true
							});
						},
						error: function (model, resp, options) {
							var popup = new Views.PopupView({
								type: "error",
								message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			}, this);

			// Views
			positionSearchCriteriaView = new Views.PositionSearchCriteriaView({
				model: criteria
			});
			positionSearchResultView = new Views.PositionSearchResultView({
				collection: positions
			});

			self.refreshBreadcrumb([ $.i18n.prop('menu_sposition') ]);

			$("#featured").append(positionSearchCriteriaView.render().el);
			$("#content").append(positionSearchResultView.el);

			// Refresh Data - triggers change to render view
			criteria.fetch({
				cache: false
			});

			self.currentView = [ positionSearchCriteriaView, positionSearchResultView ];
		},

		showCandidateCandidacyView: function (candidacyId) {
			var self = this;
			var candidacyEditView;
			var candidateCandidacies = new Models.CandidateCandidacies({}, {
				candidate: App.loggedOnUser.getRole("CANDIDATE").id
			});
			var candidateCandidacyListView = new Views.CandidateCandidacyListView({
				collection: candidateCandidacies
			});
			candidateCandidacies.on("candidacy:selected", function (candidacy) {
				if (candidacyEditView) {
					candidacyEditView.close();
				}
				// Select Edit or Simple View based on loggedOnUser
				candidacyEditView = new Views.CandidacyEditView({
					model: candidacy
				});
				// Update history
				App.router.navigate("candidateCandidacies/" + candidacy.id, {
					trigger: false
				});

				self.refreshBreadcrumb([ $.i18n.prop('menu_candidateCandidacies'), candidacy.id ]);
				$("#content").unbind();
				$("#content").empty();
				$("#content").html(candidacyEditView.render().el);

				candidacy.fetch({
					cache: false
				});
			});

			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_candidateCandidacies') ]);
			$("#featured").html(candidateCandidacyListView.el);

			// Refresh candidateCandidacies from server
			candidateCandidacies.fetch({
				cache: false,
				reset: true,
				error: function (collection, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				},
				success: function (collection, resp, options) {
					var selectedCandidacy;
					if (!_.isUndefined(candidacyId)) {
						selectedCandidacy = candidateCandidacies.get(candidacyId);
						if (!selectedCandidacy) {
							selectedCandidacy = new Models.Candidacy({
								id: candidacyId
							});
							selectedCandidacy.fetch({
								cache: false,
								wait: true,
								success: function () {
									candidateCandidacies.add(selectedCandidacy);
									candidateCandidacies.trigger("candidacy:selected", selectedCandidacy);
								},
								error: function (model, resp, options) {
									var popup = new Views.PopupView({
										type: "error",
										message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
		},

		showCandidacyView: function (candidacyId) {
			var self = this;
			var candidacy = new Models.Candidacy({
				id: candidacyId
			});
			var candidacyView = new Views.CandidacyView({
				model: candidacy
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_candidacy'), candidacy.id ]);
			$("#content").html(candidacyView.el);
			candidacy.fetch({
				cache: false,
				wait: true,
				success: function (model, response, options) {
					self.refreshBreadcrumb([ $.i18n.prop('menu_candidacy'), (model.get("snapshot").basicInfo.firstname + " " + model.get("snapshot").basicInfo.lastname) ]);
				}
			});
			self.currentView = candidacyView;
		},

		showIssueListView: function () {
			var self = this;
			var issues = new Models.JiraIssues();
			var issueListView = new Views.JiraIssueListView({
				collection: issues
			});
			self.clear();
			self.refreshBreadcrumb([ $.i18n.prop('menu_issues') ]);
			$("#content").html(issueListView.el);
			// Refresh registries from server
			issues.url = issues.url + "/user/" + App.loggedOnUser.get("id") + "/issue";
			issues.fetch({
				cache: false,
				reset: true,
				error: function (collection, resp, options) {
					var popup = new Views.PopupView({
						type: "error",
						message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			this.currentView = issueListView;
		},

		/***********************************************************************
		 * ** ADMIN ONLY VIEWS ******
		 **********************************************************************/

		showAdminUserSearchView: function (query) {
			var self = this;
			var users;
			var userSearchView;
			var userListView;

			self.clear();
			users = new Models.Users();
			users.on("user:selected", function (user) {
				if (user) {
					self.showUserView(user.id, user);
					self.navigate("user/" + user.id, {
						trigger: false
					});
				}
			}, this);
			userSearchView = new Views.UserSearchView({
				"query": query ? JSON.parse(decodeURI(query)) : undefined,
				collection: users
			});
			userListView = new Views.UserListView({
				collection: users
			});

			self.refreshBreadcrumb([ $.i18n.prop('menu_adminusers') ]);
			$("#featured").append(userSearchView.render().el);
			$("#content").append(userListView.render().el);

			self.currentView = [ userSearchView, userListView ];
		}

	});

	return Routers;
});
