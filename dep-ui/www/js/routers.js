/*global define */
define(["jquery", "underscore", "backbone", "application", "models", "views", "text!tpl/landing-page.html", "text!tpl/user-registration-success.html"
], function ($, _, Backbone, App, Models, Views, tpl_landing_page, tpl_user_registration_success) {
    "use strict";

    var Routers = {};

    Routers.Router = Backbone.Router.extend({

        initialize: function () {
            var languageView;
            _.extend(this, Backbone.Events);
            $(document).ajaxStart(App.blockUI);
            $(document).ajaxStop(App.unblockUI);

            App.locale = App.utils.getLocale();
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

            App.locale = App.utils.getLocale();
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

            App.locale = App.utils.getLocale();

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
                "showInstitutionRegulatoryFrameworkView", "showCandidateCandidacyView", "showCandidacyView", "showUserSearchView", "showIssueListView", "showDataExportsView",
                "showStatisticsView", "showAdminCandidaciesView", "showDomesticProfessorsCreateAccountsView", "showShibbolethUsersChangeAuthenticationView", "showUpdateInstitutionAndDepartmentNamesView", "start");

            self.on("route", function (routefn) {
                self.setTitle(routefn);
            });

            $(document).ajaxStart(App.blockUI);
            $(document).ajaxStop(App.unblockUI);

            App.locale = App.utils.getLocale();

            languageView = new Views.LanguageView({});
            languageView.render();

            // Init LoggedOnUser
            App.loggedOnUser = new Models.User();
            App.loggedOnUser.on("user:loggedon", self.start);
            App.loggedOnUser.fetch({
                url: "/dep/rest/user/loggedon",
                cache: false,
                wait: true,
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
            "searchusers": "showUserSearchView",
            "administrators": "showAdministratorsView",
            "administrators/:userId": "showAdministratorsView",
            "dataExports": "showDataExportsView",
            "statistics": "showStatisticsView",
            "adminCandidacies": "showAdminCandidaciesView",
            "createDomesticProfessorAccounts": "showDomesticProfessorsCreateAccountsView",
            "revertShibbolethAuthentication": "showShibbolethUsersChangeAuthenticationView",
            "updateInstitutionAndDepartmentNames": "showUpdateInstitutionAndDepartmentNamesView"
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
            self.refreshBreadcrumb([$.i18n.prop('menu_home')]);

            $("#featured").html(homeView.render().el);
            App.roles.fetch({
                cache: false,
                reset: true,
                wait: true
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
            this.refreshBreadcrumb([$.i18n.prop('menu_account')]);
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
            self.refreshBreadcrumb([$.i18n.prop('menu_shibolethAccount')]);
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
                self.refreshBreadcrumb([$.i18n.prop('menu_profile'), $.i18n.prop(role.get('discriminator'))]);
                $("#content").html(roleView.render().el);

                self.navigate("profile/" + role.id, {
                    trigger: false
                });
            });

            rolesView = new Views.RoleTabsView({
                collection: App.roles
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_profile')]);
            $("#featured").html(rolesView.el);

            App.roles.fetch({
                cache: false,
                reset: true,
                wait: true,
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
                self.refreshBreadcrumb([$.i18n.prop('menu_user'), user.getDisplayName()]);
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
                wait: true,
                success: function (model, resp) {
                    roles.fetch({
                        cache: false,
                        reset: true,
                        wait: true,
                        success: function (collection, response) {
                            displayContent();
                        }
                    });
                }
            });
        },

        showAdministratorsView: function (userId) {
            var self = this;
            var accountView;
            var administrators;
            var adminitratorsView;

            self.clear();

            administrators = new Models.Users();
            administrators.on("user:selected", function (user) {
                if (user) {
                    // Clean up
                    if (accountView) {
                        accountView.close();
                    }
                    accountView = new Views.AdministratorAccountView({
                        model: user
                    });

                    // Add
                    $("#content").unbind();
                    $("#content").empty();
                    $("#content").append(accountView.render().el);

                    self.refreshBreadcrumb([$.i18n.prop('menu_administrators'), user.getDisplayName()]);
                    self.navigate("administrators" + (user.id ? ('/' + user.id) : ''), {
                        trigger: false
                    });
                    App.utils.scrollTo(accountView.$el);

                }
            }, this);

            adminitratorsView = new Views.AdministratorListView({
                collection: administrators
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_iassistants')]);
            $("#featured").append(adminitratorsView.el);

            administrators.fetch({
                cache: false,
                reset: true,
                wait: true,
                data: {
                    role: 'ADMINISTRATOR'
                },
                success: function () {
                    if (!_.isUndefined(userId)) {
                        administrators.trigger("user:selected", administrators.get(userId));
                    }
                }
            });

            self.currentView = adminitratorsView;
        },

        showInstitutionAssistantsView: function (userId) {
            var self = this;
            var accountView;
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
                    accountView = new Views.AssistantAccountView({
                        model: user
                    });

                    // Add
                    $("#content").unbind();
                    $("#content").empty();
                    $("#content").append(accountView.render().el);

                    self.refreshBreadcrumb([$.i18n.prop('menu_iassistants'), user.getDisplayName()]);
                    self.navigate("iassistants" + (user.id ? ('/' + user.id) : ''), {
                        trigger: false
                    });
                    App.utils.scrollTo(accountView.$el);

                }
            }, this);

            assistantsView = new Views.InstitutionAssistantListView({
                collection: assistants
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_iassistants')]);
            $("#featured").append(assistantsView.el);

            assistants.fetch({
                cache: false,
                reset: true,
                wait: true,
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
                    accountView = new Views.AssistantAccountView({
                        model: user
                    });

                    // Add
                    $("#content").unbind();
                    $("#content").empty();
                    $("#content").append(accountView.render().el);

                    self.refreshBreadcrumb([$.i18n.prop('menu_massistants'), user.getDisplayName()]);
                    self.navigate("massistants" + (user.id ? ('/' + user.id) : ''), {
                        trigger: false
                    });
                    App.utils.scrollTo(accountView.$el);
                }
            }, this);

            assistantsView = new Views.MinistryAssistantListView({
                collection: assistants
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_massistants')]);
            $("#featured").append(assistantsView.el);

            assistants.fetch({
                cache: false,
                reset: true,
                wait: true,
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
                    silent: true,
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
                        $("#content").unbind();
                        $("#content").empty();
                        $("#content").html(positionView.el);
                        positionView.render();

                        self.refreshBreadcrumb([$.i18n.prop('menu_positions'), position.get("name")]);
                        App.utils.scrollTo(positionView.$el);

                    }
                });
            });

            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_positions')]);

            positionListView.render();
            $("#featured").html(positionListView.el);
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
                wait: true,
                success: function () {
                    self.refreshBreadcrumb([$.i18n.prop('menu_position'), position.get("name")]);
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

                register.fetch({
                    cache: false,
                    silent: true,
                    wait: true,
                    success: function () {
                        $("#content").unbind();
                        $("#content").empty();
                        $("#content").html(registerView.el);

                        self.refreshBreadcrumb([$.i18n.prop('menu_registers'), register.get("title")]);
                        App.utils.scrollTo(registerView.$el);
                    }
                });

                // Select Edit or Simple View based on loggedOnUser
                if (register.isEditableBy(App.loggedOnUser)) {
                    registerView = new Views.RegisterEditView({
                        model: register,
                        collection: new Models.RegisterMembers({},{
                            register: register.id
                        })
                    });
                } else {
                    registerView = new Views.RegisterView({
                        model: register,
                        collection: new Models.RegisterMembers({},{
                            register: register.id
                        })
                    });
                }

                registerView.render();
            });

            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_registers')]);
            $("#featured").html(registerListView.el);
            registerListView.render();
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
            self.refreshBreadcrumb([$.i18n.prop('menu_professorCommittees')]);
            $("#content").html(professorCommitteesView.el);

            // Refresh professorCommittees from server
            professorCommittees.fetch({
                cache: false,
                reset: true,
                wait: true,
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
            // 1. Models and Views
            // Collections:
            var professorPositionEvaluations = new Models.ProfessorPositionEvaluations({}, {
                professor: App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") ? App.loggedOnUser.getRole("PROFESSOR_DOMESTIC").id : App.loggedOnUser.getRole("PROFESSOR_FOREIGN").id
            });
            var professorCandidacyEvaluations = new Models.ProfessorCandidacyEvaluations({}, {
                professor: App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") ? App.loggedOnUser.getRole("PROFESSOR_DOMESTIC").id : App.loggedOnUser.getRole("PROFESSOR_FOREIGN").id
            });
            // View
            var professorEvaluationsView = new Views.ProfessorEvaluationsView({
                positionEvaluations: professorPositionEvaluations,
                candidacyEvaluations: professorCandidacyEvaluations
            });
            // 2. Init
            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_professorEvaluations')]);
            $("#content").html(professorEvaluationsView.el);

            // 3. Get Data
            // Refresh professorPositionEvaluations from server
            professorPositionEvaluations.fetch({
                cache: false,
                reset: true,
                wait: true,
                error: function (model, resp, options) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            // Refresh professorCandidacyEvaluations from server
            professorCandidacyEvaluations.fetch({
                cache: false,
                reset: true,
                wait: true,
                error: function (model, resp, options) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });

            // 4. Return result
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
                institutionRF.fetch({
                    cache: false,
                    silent: true,
                    wait: true,
                    success: function () {
                        $("#content").html(institutionRegulatoryFrameworkView.el);
                        institutionRegulatoryFrameworkView.render();

                        self.refreshBreadcrumb([$.i18n.prop('menu_regulatoryframeworks')]);
                        // Update history
                        App.router.navigate("regulatoryframeworks/" + institutionRF.id, {
                            trigger: false
                        });
                        App.utils.scrollTo(institutionRegulatoryFrameworkView.$el);
                    }
                });
            });
            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_regulatoryframeworks')]);
            $("#featured").html(institutionRegulatoryFrameworkListView.el);

            // Refresh institutionRFs from server
            institutionRFs.fetch({
                cache: false,
                reset: true,
                wait: true,
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
                    wait: true,
                    type: 'POST',
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

            self.refreshBreadcrumb([$.i18n.prop('menu_sposition')]);

            $("#featured").append(positionSearchCriteriaView.render().el);
            $("#content").append(positionSearchResultView.el);

            // Refresh Data - triggers change to render view
            criteria.fetch({
                cache: false,
                wait: true
            });

            self.currentView = [positionSearchCriteriaView, positionSearchResultView];
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
                candidacy.fetch({
                    cache: false,
                    silent: true,
                    wait: true,
                    success: function () {
                        $("#content").unbind();
                        $("#content").empty();
                        $("#content").html(candidacyEditView.el);
                        candidacyEditView.render();

                        self.refreshBreadcrumb([$.i18n.prop('menu_candidateCandidacies'), candidacy.id]);
                        App.router.navigate("candidateCandidacies/" + candidacy.id, {
                            trigger: false
                        });
                        App.utils.scrollTo(candidacyEditView.$el);
                    }
                });
            });

            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_candidateCandidacies')]);
            $("#featured").html(candidateCandidacyListView.el);

            // Refresh candidateCandidacies from server
            candidateCandidacies.fetch({
                cache: false,
                reset: true,
                wait: true,
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
            self.refreshBreadcrumb([$.i18n.prop('menu_candidacy'), candidacy.id]);
            $("#content").html(candidacyView.el);
            candidacy.fetch({
                cache: false,
                wait: true,
                success: function (model, response, options) {
                    self.refreshBreadcrumb([$.i18n.prop('menu_candidacy'), (model.get("snapshot").basicInfo.firstname + " " + model.get("snapshot").basicInfo.lastname)]);
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
            self.refreshBreadcrumb([$.i18n.prop('menu_issues')]);
            $("#content").html(issueListView.el);
            // Refresh registries from server
            issues.url = issues.url + "/user/" + App.loggedOnUser.get("id") + "/issue";
            issues.fetch({
                cache: false,
                reset: true,
                wait: true,
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

        showUserSearchView: function () {
            var self = this;
            var userSearchView;

            self.clear();
            userSearchView = new Views.UserSearchView({
                'searchURL': (new Models.Users()).url + "/search"
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_searchusers')]);
            $("#content").html(userSearchView.render().el);

            self.currentView = [userSearchView];
        },

        showDataExportsView: function () {
            var self = this;
            var dataExportsView;
            self.clear();

            dataExportsView = new Views.DataExportsView({});
            self.refreshBreadcrumb([$.i18n.prop('menu_dataExports')]);

            $("#content").html(dataExportsView.render().el);

            self.currentView = dataExportsView;
        },

        showStatisticsView: function () {
            var self = this;
            var statistics = new Models.Statistics();
            var statisticsView = new Views.StatisticsView({
                model: statistics
            });

            self.clear();
            $("#content").html(statisticsView.el);
            self.refreshBreadcrumb([$.i18n.prop('menu_statistics')]);

            statistics.fetch({
                cache: false,
                wait: true,
                success: function (model, response, options) {
                    statisticsView.render();
                }
            });

            self.currentView = [statisticsView];
        },

        showAdminCandidaciesView: function () {
            var self = this;
            var candidacies = new Models.IncompleteCandidacies();
            var incompleteCandidacyListView = new Views.AdminCandidacyListView({
                collection: candidacies
            });

            self.clear();
            self.refreshBreadcrumb([$.i18n.prop('menu_adminCandidacies')]);
            $("#content").html(incompleteCandidacyListView.el);
            candidacies.fetch({
                cache: false,
                wait: true,
                success: function (model, response, options) {
                    incompleteCandidacyListView.render();
                }
            });
            self.currentView = incompleteCandidacyListView;
        },

        showDomesticProfessorsCreateAccountsView: function () {
            var self = this;
            var showDomesticProfessorsCreateAccountsView;

            self.clear();
            showDomesticProfessorsCreateAccountsView = new Views.showDomesticProfessorsCreateAccountsView({
                'searchURL': (new Models.Users()).url + "/search"
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_createDomesticProfessorAccounts')]);
            $("#content").html(showDomesticProfessorsCreateAccountsView.render().el);

            self.currentView = [showDomesticProfessorsCreateAccountsView];
        },

        showShibbolethUsersChangeAuthenticationView:  function () {
            var self = this;
            var showShibbolethUsersChangeAuthenticationView;

            var users = new Models.Users();

            self.clear();
            showShibbolethUsersChangeAuthenticationView = new Views.ShowShibbolethUsersChangeAuthenticationView({
                collection: users
            });
            self.refreshBreadcrumb([$.i18n.prop('menu_createDomesticProfessorAccounts')]);
            $("#content").html(showShibbolethUsersChangeAuthenticationView.render().el);

            self.currentView = [showShibbolethUsersChangeAuthenticationView];
        },

        showUpdateInstitutionAndDepartmentNamesView: function () {
            var self = this;

            var showInstitutionDepartmentListView = new Views.ShowInstitutionDepartmentListView();

            self.clear();
            $("#content").html(showInstitutionDepartmentListView.el);

            self.refreshBreadcrumb([$.i18n.prop('menu_updateInstitutionAndDepartmentNames')]);

            showInstitutionDepartmentListView.render();

            self.currentView = [showInstitutionDepartmentListView];
        }


    });

    return Routers;
});
