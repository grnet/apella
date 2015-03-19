/*global define */
define(["jquery", "underscore", "backbone", "application", "models",
    "text!tpl/announcement-list.html", "text!tpl/confirm.html", "text!tpl/file.html", "text!tpl/file-edit.html", "text!tpl/file-list.html", "text!tpl/file-list-edit.html",
    "text!tpl/home.html", "text!tpl/login-admin.html", "text!tpl/login-main.html", "text!tpl/popup.html", "text!tpl/professor-list.html", "text!tpl/register-edit.html",
    "text!tpl/register-list.html", "text!tpl/role-edit.html", "text!tpl/role-tabs.html", "text!tpl/role.html", "text!tpl/user-edit.html", "text!tpl/user-list.html",
    "text!tpl/user-registration-select.html", "text!tpl/user-registration-success.html", "text!tpl/user-registration.html", "text!tpl/user-role-info.html",
    "text!tpl/user-search.html", "text!tpl/user-verification.html", "text!tpl/user.html", "text!tpl/language.html", "text!tpl/professor-committees.html",
    "text!tpl/professor-evaluations.html", "text!tpl/register.html", "text!tpl/institution-regulatory-framework.html", "text!tpl/institution-regulatory-framework-edit.html",
    "text!tpl/position-search-criteria.html", "text!tpl/position-search-result.html", "text!tpl/candidacy-edit.html", "text!tpl/candidate-candidacy-list.html",
    "text!tpl/candidacy.html", "text!tpl/candidacy-update-confirm.html", "text!tpl/institution-regulatory-framework-list.html",
    "text!tpl/register-edit-professor-list.html", "text!tpl/overlay.html", "text!tpl/position-main-edit.html", "text!tpl/position-candidacies-edit.html",
    "text!tpl/position-committee-edit.html", "text!tpl/position-committee-member-edit.html", "text!tpl/position-evaluation-edit.html",
    "text!tpl/position-evaluation-edit-register-member-list.html", "text!tpl/position-evaluation-evaluator-edit.html", "text!tpl/position-edit.html", "text!tpl/position-list.html",
    "text!tpl/position-committee-edit-register-member-list.html", "text!tpl/position.html", "text!tpl/position-candidacies.html", "text!tpl/position-committee.html",
    "text!tpl/position-evaluation.html", "text!tpl/position-nomination.html", "text!tpl/position-complementaryDocuments.html", "text!tpl/position-nomination-edit.html",
    "text!tpl/position-complementaryDocuments-edit.html", "text!tpl/department-select.html", "text!tpl/department.html", "text!tpl/user-helpdesk.html",
    "text!tpl/position-helpdesk.html", "text!tpl/jira-issue-edit.html", "text!tpl/jira-issue-list.html", "text!tpl/jira-issue.html", "text!tpl/jira-issue-public-edit.html", "text!tpl/data-exports.html", "text!tpl/statistics.html", "text!tpl/position-committee-edit-confirm.html", "text!tpl/incomplete-candidacy-list.html", "text!tpl/evaluator-select.html", "text!tpl/evaluator.html", "text!tpl/confirm-withdraw-candidacy.html", "text!tpl/domestic-professors-create.html", "text!tpl/domestic-professors-created-list.html", "text!tpl/candidate-candidacy-history-actions-list.html"
], function ($, _, Backbone, App, Models, tpl_announcement_list, tpl_confirm, tpl_file, tpl_file_edit, tpl_file_list, tpl_file_list_edit, tpl_home, tpl_login_admin, tpl_login_main, tpl_popup, tpl_professor_list, tpl_register_edit, tpl_register_list, tpl_role_edit, tpl_role_tabs, tpl_role, tpl_user_edit, tpl_user_list, tpl_user_registration_select, tpl_user_registration_success, tpl_user_registration, tpl_user_role_info, tpl_user_search, tpl_user_verification, tpl_user, tpl_language, tpl_professor_committees, tpl_professor_evaluations, tpl_register, tpl_institution_regulatory_framework, tpl_institution_regulatory_framework_edit, tpl_position_search_criteria, tpl_position_search_result, tpl_candidacy_edit, tpl_candidate_candidacy_list, tpl_candidacy, tpl_candidacy_update_confirm, tpl_institution_regulatory_framework_list, tpl_register_edit_professor_list, tpl_overlay, tpl_position_main_edit, tpl_position_candidacies_edit, tpl_position_committee_edit, tpl_position_committee_member_edit, tpl_position_evaluation_edit, tpl_position_evaluation_edit_register_member_list, tpl_position_evaluation_evaluator_edit, tpl_position_edit, tpl_position_list, tpl_position_committee_edit_register_member_list, tpl_position, tpl_position_candidacies, tpl_position_committee, tpl_position_evaluation, tpl_position_nomination, tpl_position_complementaryDocuments, tpl_position_nomination_edit, tpl_position_complementaryDocuments_edit, tpl_department_select, tpl_department, tpl_user_helpdesk, tpl_position_helpdesk, tpl_jira_issue_edit, tpl_jira_issue_list, tpl_jira_issue, tpl_jira_issue_public_edit, tpl_data_exports, tpl_statistics, tpl_position_committee_edit_confirm, tpl_incomplete_candidacy_list, tpl_evaluator_select, tpl_evaluator, tpl_confirm_withdraw_candidacy, tpl_domestic_professors_create, tpl_domestic_professors_created_list, candidate_candidacy_history_actions_list) {

    "use strict";
    /** ****************************************************************** */

    var Views = {};

    // Add some precompiled templates in _. so that they are accesible inside
    // other templates
    _.extend(_, {
        templates: {
            department: _.template(tpl_department),
            evaluator: _.template(tpl_evaluator)
        }
    });

    /***************************************************************************
     * BaseView ***************************************************************
     **************************************************************************/
    Views.BaseView = Backbone.View.extend({
        className: "span12",

        innerViews: [],

        fileViews: {},

        initialize: function () {
            var self = this;
            _.bindAll(self, "render", "addFile", "addFileList", "addFileEdit", "addFileListEdit", "addTitle", "close", "closeInnerViews");
            self.addTitle();
        },

        addFile: function (collection, type, $el, options) {
            var self = this;
            var fileView;
            var file = collection.find(function (model) {
                return _.isEqual(model.get("type"), type);
            });
            options = options || {};
            if (_.isUndefined(file)) {
                file = new Models.File({
                    "type": type
                });
            }
            file.urlRoot = collection.url;
            if ($el.data("fileView")) {
                self.fileViews[$el.data("fileView")].close();
            }
            fileView = new Views.FileView(_.extend({
                model: file,
                el: $el[0]
            }, options));
            fileView.render();

            $el.data("fileView", fileView.cid);
            self.fileViews[fileView.cid] = fileView;
        },

        addFileEdit: function (collection, type, $el, options) {
            var self = this;
            var fileView;
            var file = collection.find(function (model) {
                return _.isEqual(model.get("type"), type);
            });
            options = options || {};
            if (_.isUndefined(file)) {
                file = new Models.File({
                    "type": type
                });
            }
            file.urlRoot = collection.url;
            if ($el.data("fileView")) {
                self.fileViews[$el.data("fileView")].close();
            }
            fileView = new Views.FileEditView(_.extend({
                model: file,
                el: $el[0]
            }, options));
            fileView.render();

            $el.data("fileView", fileView.cid);
            self.fileViews[fileView.cid] = fileView;
        },

        addFileList: function (collection, type, $el, options) {
            var self = this;
            var fileListView;
            var files = new Models.Files();
            options = options || {};
            options.name = $el.attr("id");

            files.type = type;
            files.url = collection.url;
            _.each(collection.filter(function (model) {
                return _.isEqual(model.get("type"), type);
            }), function (model) {
                files.add(model);
            });
            if ($el.data("fileView")) {
                self.fileViews[$el.data("fileView")].close();
            }
            fileListView = new Views.FileListView(_.extend({
                collection: files,
                el: $el[0]
            }, options));
            fileListView.render();

            $el.data("fileView", fileListView.cid);
            self.fileViews[fileListView.cid] = fileListView;
        },

        addFileListEdit: function (collection, type, $el, options) {
            var self = this;
            var fileListView;
            var files = new Models.Files();
            options = options || {};

            files.type = type;
            files.url = collection.url;
            _.each(collection.filter(function (model) {
                return _.isEqual(model.get("type"), type);
            }), function (model) {
                files.add(model);
            });
            if ($el.data("fileView")) {
                self.fileViews[$el.data("fileView")].close();
            }
            fileListView = new Views.FileListEditView(_.extend({
                collection: files,
                el: $el[0]
            }, options));
            fileListView.render();

            $el.data("fileView", fileListView.cid);
            self.fileViews[fileListView.cid] = fileListView;
        },

        addTitle: function () {
            var self = this;
            if (self.options.title) {
                self.$el.prepend("<h2>" + self.options.title + "</h2>");
            }
        },

        closeInnerViews: function () {
            var self = this;
            var fileView;
            _.each(self.innerViews, function (innerView) {
                innerView.close();
            });
            for (fileView in self.fileViews) {
                if (self.fileViews.hasOwnProperty(fileView)) {
                    self.fileViews[fileView].close();
                }
            }
            self.innerViews = [];
            self.fileViews = {};
        }
    });

    /***************************************************************************
     * MenuView ********************************************************
     **************************************************************************/
    Views.MenuView = Views.BaseView.extend({
        el: "ul#menu",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "highlightCurrent");
            this.model.bind('change', this.render);

            Backbone.history.on("route", this.highlightCurrent);
        },

        events: {},

        render: function () {
            var self = this;
            var menuItems = [];
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();

            // Shibboleth Login
            if (self.model.isAccountIncomplete()) {
                return self; // Do not add any menu-items
            }

            if (!self.model.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                menuItems.push("profile");
            }
            if (self.model.hasRoleWithStatus("PROFESSOR_DOMESTIC", "ACTIVE")) {
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("professorCommittees");
                menuItems.push("professorEvaluations");
            }
            if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("professorCommittees");
                menuItems.push("professorEvaluations");
            }
            if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("sposition");
                menuItems.push("candidateCandidacies");
            }
            if (self.model.hasRoleWithStatus("INSTITUTION_MANAGER", "ACTIVE")) {
                menuItems.push("iassistants");
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("positions");
            }
            if (self.model.hasRoleWithStatus("INSTITUTION_ASSISTANT", "ACTIVE")) {
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("positions");
            }
            if (self.model.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE")) {
                menuItems.push("massistants");
                menuItems.push("searchusers");
                menuItems.push("statistics");
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("positions");
            }
            if (self.model.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
                menuItems.push("searchusers");
                menuItems.push("statistics");
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("positions");
            }
            if (self.model.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                if (self.model.getRole('ADMINISTRATOR').superAdministrator) {
                    menuItems.push("administrators");
                    menuItems.push("dataExports");
                    menuItems.push("adminCandidacies");
                }
                menuItems.push("searchusers");
                menuItems.push("statistics");
                menuItems.push("regulatoryframeworks");
                menuItems.push("registers");
                menuItems.push("positions");
            }

            self.$el.append('<li><a href="#">' + $.i18n.prop('menu_home') + '</a></li>');
            _.each(_.uniq(menuItems), function (menuItem) {
                self.$el.append('<li><a href="#' + menuItem + '">' + $.i18n.prop('menu_' + menuItem) + '</a></li>');
            });

            return self;
        },

        highlightCurrent: function () {
            var self = this;
            var menuItem = window.location.hash.split("/")[0]; // Only first part until '/'
            if (menuItem.length === 0) {
                menuItem = '#';
            }
            self.$("li.active").removeClass("active");
            self.$("a[href=" + menuItem + "]").parent("li").addClass("active");
        },

        close: function () {
            Backbone.history.off("route", this.highlightCurrent);
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();

        }

    });

    /***************************************************************************
     * LanguageView ************************************************************
     **************************************************************************/
    Views.LanguageView = Views.BaseView.extend({
        el: "div#language",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "selectLanguage");
            this.template = _.template(tpl_language);
        },

        events: {
            "click a:not(.active)": "selectLanguage"
        },

        render: function () {
            var self = this;
            var language = App.utils.getLocale();
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template());
            if (!language) {
                language = "el";
            }
            self.$("a[data-language=" + language + "]").addClass("active");
            return self;
        },

        selectLanguage: function (event) {
            var language = $(event.currentTarget).data('language');
            // Set Language Cookie:
            App.utils.addCookie('apella-lang', language, 365);
            // Trigger refresh
            window.location.reload();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }

    });

    /***************************************************************************
     * UserMenuView ************************************************************
     **************************************************************************/
    Views.UserMenuView = Views.BaseView.extend({
        el: "li#user-menu",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "logout");

            this.model.bind('change', this.render);
        },

        events: {
            "click a#logout": "logout"
        },

        render: function () {
            var self = this;
            var displayname = self.model.getDisplayName();
            self.$el.empty();
            self.$el.append("<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\"> <i class=\"icon-user\"></i> " + displayname + "<span class=\"caret\"></span></a>");
            self.$el.append("<ul class=\"dropdown-menu\">");
            // Incomplete Account
            if (!self.model.isAccountIncomplete()) {
                self.$el.find("ul").append('<li><a href="#account">' + $.i18n.prop('menu_account') + '</a>');
                self.$el.find("ul").append('<li><a href="#issues">' + $.i18n.prop('menu_issues') + '</a>');
            }
            // Add Logout
            self.$el.find("ul").append('<li><a id="logout" >' + $.i18n.prop('menu_logout') + '</a>');
            return self;
        },

        logout: function () {
            App.loggedOnUser.logout({}, {
                wait: true,
                success: function () {
                    // Remove X-Auth-Token
                    $.ajaxSetup({
                        headers: {}
                    });
                    // Remove auth cookie
                    document.cookie = "_dep_a=-1;expires=0;path=/";
                    // Send Redirect
                    window.location.href = window.location.pathname;
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * LoginView **********************************************************
     **************************************************************************/
    Views.LoginView = Views.BaseView.extend({
        tagName: "div",

        validatorLogin: undefined,
        validatorResetPasswordForm: undefined,
        validatorResendVerificationEmail: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "showResetPassword", "showResendVerificationEmailForm", "resetPassword", "resendVerificationEmail", "login");
            this.template = _.template(tpl_login_main);
            this.model.bind('change', this.render);
        },

        events: {
            "click a#login": function () {
                this.$("form#loginForm").submit();
            },
            "click a#resetPassword": function () {
                this.$("form#resetPasswordForm").submit();
            },
            "click a#resendVerificationEmail": function () {
                this.$("form#resendVerificationEmailForm").submit();
            },
            "click a#forgotPassword": "showResetPassword",
            "click a#haveNotReceivedVerification": "showResendVerificationEmailForm",
            "submit form#loginForm": "login",
            "submit form#resetPasswordForm": "resetPassword",
            "submit form#resendVerificationEmailForm": "resendVerificationEmail"
        },

        render: function () {
            var self = this;
            var tpl_data;

            self.$el.empty();
            self.addTitle();

            tpl_data = self.model.toJSON();
            self.$el.append(self.template(tpl_data));

            self.$("#resetPasswordForm").hide();
            self.$("#resendVerificationEmailForm").hide();

            self.validatorLogin = $("form#loginForm", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    username: {
                        required: true
                    },
                    password: {
                        required: true,
                        minlength: 5
                    }
                },
                messages: {
                    username: {
                        required: $.i18n.prop('validation_username')
                    },
                    password: {
                        required: $.i18n.prop('validation_required'),
                        minlength: $.i18n.prop('validation_minlength', 5)
                    }
                }
            });

            self.validatorResetPasswordForm = $("form#resetPasswordForm", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).addClass("error");
                },
                unhighlight: function (element) {
                    $(element).removeClass("error");
                },
                rules: {
                    email: {
                        required: true,
                        email: true
                    }
                },
                messages: {
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email')
                    }
                }
            });

            self.validatorResendVerificationEmail = $("form#resendVerificationEmailForm", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).addClass("error");
                },
                unhighlight: function (element) {
                    $(element).removeClass("error");
                },
                rules: {
                    email: {
                        required: true,
                        email: true
                    }
                },
                messages: {
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email')
                    }
                }
            });

            return self;
        },

        showResetPassword: function () {
            var self = this;
            self.$("#resetPasswordForm").toggle();
            self.$("#resendVerificationEmailForm").hide();
        },

        showResendVerificationEmailForm: function () {
            var self = this;
            self.$("#resendVerificationEmailForm").toggle();
            self.$("#resetPasswordForm").hide();
        },

        login: function () {
            var self = this;
            var username = self.$('form#loginForm input[name=username]').val();
            var password = self.$('form#loginForm input[name=password]').val();

            // Save to model
            self.model.login({
                "username": username,
                "password": password
            }, {
                wait: true,
                success: function () {
                    // Notify AppRouter to start Application (fill
                    // Header and
                    // handle
                    // history token)
                    self.model.trigger("user:loggedon");
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        resetPassword: function () {
            var self = this;
            var email = self.$('form#resetPasswordForm input[name=email]').val();

            // Save to model
            self.model.resetPassword({
                "email": email
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("PasswordReset")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        resendVerificationEmail: function () {
            var self = this;
            var email = self.$('form#resendVerificationEmailForm input[name=email]').val();

            // Save to model
            self.model.resendVerificationEmail({
                "email": email
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("VerificationEmailResent")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * AdminLoginView **********************************************************
     **************************************************************************/
    Views.AdminLoginView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "login");
            this.template = _.template(tpl_login_admin);
            this.model.bind('change', this.render);
        },

        events: {
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "login"
        },

        render: function () {
            var self = this;
            var propName;

            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(this.model.toJSON()));

            self.validator = $("form", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    username: {
                        required: true
                    },
                    password: {
                        required: true,
                        minlength: 5
                    }
                },
                messages: {
                    username: {
                        required: $.i18n.prop('validation_username')
                    },
                    password: {
                        required: $.i18n.prop('validation_required'),
                        minlength: $.i18n.prop('validation_minlength', 5)
                    }
                }
            });
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }

            return this;
        },

        login: function () {
            var self = this;
            var username = self.$('form input[name=username]').val();
            var password = self.$('form input[name=password]').val();

            // Save to model
            self.model.login({
                "username": username,
                "password": password
            }, {
                wait: true,
                success: function () {
                    // Notify AppRouter to start Application (fill
                    // Header and
                    // handle
                    // history token)
                    self.model.trigger("user:loggedon");
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PopupView ***************************************************************
     **************************************************************************/
    Views.PopupView = Views.BaseView.extend({
        tagName: "div",

        className: "alert alert-block alert-popup fade in",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show", "handleKeyUp", "handleHistoryChange", "stopTimer", "startTimer");
            this.template = _.template(tpl_popup);

            $('body').on("keyup", this.handleKeyUp);
            App.router.on("all", this.handleHistoryChange);
        },

        events: {
            "mouseenter": "stopTimer",
            "mouseleave": "startTimer"
        },

        render: function () {
            var self = this;

            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template({
                message: this.options.message
            }));
            switch (self.options.type) {
                case 'info':
                    self.$el.addClass("alert-info");
                    break;
                case 'success':
                    self.$el.addClass("alert-success");
                    break;
                case 'warning':
                    self.$el.addClass("alert-danger");
                    break;
                case 'error':
                    self.$el.addClass("alert-error");
                    break;
                default:
                    break;
            }
            return this;
        },

        show: function () {
            var self = this;
            self.render();
            $('div#alerts').append(self.el);
            // Start timer:
            self.startTimer();
        },

        handleKeyUp: function (event) {
            var self = this;
            if (event.keyCode === 27) {
                // On escape close
                self.$el.fadeTo(500, 0).slideUp(500, function () {
                    self.close();
                });
            }
        },

        handleHistoryChange: function () {
            var self = this;
            // On escape close
            self.$el.fadeTo(500, 0).slideUp(500, function () {
                self.close();
            });
        },

        stopTimer: function () {
            var self = this;
            // Stop timer:
            if (self.timer) {
                window.clearTimeout(self.timer);
                self.timer = undefined;
            }
        },

        startTimer: function () {
            var self = this;
            // Restart timer:
            self.stopTimer();
            self.timer = window.setTimeout(function () {
                self.$el.fadeTo(500, 0).slideUp(500, function () {
                    self.close();
                });
            }, 5000);

        },

        close: function () {
            this.stopTimer();
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
            $('body').off("keyup", this.handleKeyUp);
            App.router.off("all", this.handleHistoryChange);
        }
    });

    /***************************************************************************
     * ConfirmView *************************************************************
     **************************************************************************/
    Views.ConfirmView = Views.BaseView.extend({
        tagName: "div",

        className: "modal",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show");
            this.template = _.template(tpl_confirm);
        },

        events: {
            "click a#yes": function () {
                this.$el.modal('hide');
                if (_.isFunction(this.options.yes)) {
                    this.options.yes();
                }
            }
        },

        render: function () {
            var self = this;
            self.$el.empty();
            self.$el.append(self.template({
                title: self.options.title,
                message: self.options.message
            }));
        },
        show: function () {
            var self = this;
            self.render();
            self.$el.modal();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * OverlayView *************************************************************
     **************************************************************************/
    Views.OverlayView = Views.BaseView.extend({
        tagName: "div",

        className: "modal fade",

        innerView: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_overlay);
            this.innerView = this.options.innerView;
        },

        events: {},

        render: function () {
            var self = this;
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template({}));
            self.$("div.modal-body div.row-fluid").html(self.innerView.render().el);
            self.$el.modal();
            self.$el.on('hidden', self.close);
        },

        close: function () {
            var self = this;
            self.innerView.close();
            self.$el.off('hidden', self.close);
            self.$el.modal("hide");
            self.$el.unbind();
            self.$el.remove();
        }
    });

    /***************************************************************************
     * DepartmentSelectView ****************************************************
     **************************************************************************/
    Views.DepartmentSelectView = Views.BaseView.extend({

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "onToggleEdit", "onSelectDepartment", "toggleEdit", "select", "clear");
            this.template = _.template(tpl_department_select);
            this.collection.bind("reset", this.render, this);

            this.$input = $(this.el);
            this.$input.before("<div id=\"" + this.$input.attr("name") + "\"></div>");
            this.setElement(this.$input.prev("#" + this.$input.attr("name")));

            this.model = this.collection.get(this.$input.val()) || new Models.Department();
        },

        events: {
            "click a#selectDepartment": "onSelectDepartment",
            "click a#toggleEdit": "onToggleEdit"
        },

        render: function () {
            var self = this;
            var tpl_data;

            // Prepare Data
            tpl_data = {
                editable: self.options.editable,
                departments: (function () {
                    var result = [];
                    _.each(self.collection.models, function (model) {
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            result.push(item);
                        }
                    });
                    return result;
                }())
            };

            // Render
            self.closeInnerViews();
            self.$el.empty();
            self.$el.append(this.template(tpl_data));
            self.$("#departmentDescription").html(_.templates.department(self.model.toJSON()));

            // Initialize Plugins
            if (!$.fn.DataTable.fnIsDataTable(self.$("table#departments-table"))) {
                self.$("table#departments-table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            self.$("div#departments-table_wrapper").hide();

            self.select(self.$input.val());
            // Return result
            return self;
        },

        onToggleEdit: function () {
            var self = this;
            self.toggleEdit();
        },

        onSelectDepartment: function (event) {
            var self = this;
            var id = $(event.currentTarget).attr('data-department-id');
            self.select(id);
        },

        toggleEdit: function (show) {
            var self = this;
            if (_.isUndefined(show)) {
                self.$("div#departments-table_wrapper").toggle(400);
            } else if (show) {
                self.$("div#departments-table_wrapper").show(400);
            } else {
                self.$("div#departments-table_wrapper").hide(400);
            }
        },

        select: function (departmentId) {
            var self = this;
            var selectedModel;
            if (departmentId) {
                selectedModel = self.collection.get(departmentId);
                if (selectedModel && !_.isEqual(selectedModel.id, self.$input.val())) {
                    self.model = selectedModel;
                    self.$input.val(selectedModel.id).trigger("change").trigger("input");
                    $('input[name=institution]').val(selectedModel.attributes.school.institution.id);
                    self.$("#departmentDescription").html(_.templates.department(self.model.toJSON()));
                    self.$("div#departments-table_wrapper").hide(400);
                }
            } else {
                self.clear();
            }
        },

        clear: function () {
            var self = this;
            self.model = new Models.Department();
            self.$input.val(undefined).trigger("change").trigger("input");
            self.$("#departmentDescription").html(_.templates.department(self.model.toJSON()));
            self.$("div#departments-table_wrapper").hide(400);
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserRegistrationSelectView **********************************************
     **************************************************************************/
    Views.UserRegistrationSelectView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_registration_select);
        },

        events: {},

        render: function () {
            var self = this;
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template({
                roles: App.usernameRegistrationRoles
            }));
            return this;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserRegistrationView ****************************************************
     **************************************************************************/
    Views.UserRegistrationView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "changeInstitution", "submit", "selectInstitution");
            this.template = _.template(tpl_user_registration);
            this.model.bind('change', this.render);
        },

        events: {
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form#userForm", this.el).submit();
            },
            "click a#selectInstitution": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                this.selectInstitution(event);
            },
            "submit form#userForm": "submit",
            "change select[name=institution]": "changeInstitution"
        },

        render: function () {
            var self = this;
            var propName;
            var role = self.model.get('roles')[0];
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(role));

            if (role.discriminator === "PROFESSOR_DOMESTIC") {
                // Set UI components, only shibboleth login is allowed
                self.$("#shibbolethLoginInstructions").show();
                self.$("form#institutionForm").hide();
                self.$("form#userForm").hide();
            } else if (role.discriminator === "INSTITUTION_MANAGER") {
                // Especially for INSTITUTION_MANAGER there is a demand to
                // select institution first
                // Add institutions in selector:
                App.institutions = App.institutions || new Models.Institutions();
                App.institutions.fetch({
                    cache: true,
                    reset: true,
                    success: function (collection) {
                        self.$("select[name='institution']").empty();
                        self.$("select[name='institution']").append("<option value=\"\">" + $.i18n.prop("PleaseSelectInstitution") + "</option>");
                        _.each(collection.filter(function (institution) {
                            return _.isEqual(institution.get("category"), "INSTITUTION");
                        }), function (institution) {
                            if (_.isObject(role.institution) && _.isEqual(institution.id, role.institution.id)) {
                                self.$("select[name='institution']").append("<option value='" + institution.get("id") + "' selected>" + institution.getName(App.locale) + "</option>");
                            } else {
                                self.$("select[name='institution']",
                                    self.$el).append("<option value='" + institution.get("id") + "'>" + institution.getName(App.locale) + "</option>");
                            }
                        });
                        self.$("select[name='institution']").trigger("change", {
                            triggeredBy: "application"
                        });
                    },
                    error: function (model, resp) {
                        var popup = new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                        });
                        popup.show();
                    }
                });
                // Set UI components
                if (role.institution) {
                    self.$("form#institutionForm").hide();
                    self.$("form#userForm").show();
                } else {
                    self.$("form#institutionForm").show();
                    self.$("form#userForm").hide();
                }
                self.$("#shibbolethLoginInstructions").hide();
            } else {
                // Set UI components
                self.$("#shibbolethLoginInstructions").hide();
                self.$("form#institutionForm").hide();
                self.$("form#userForm").show();
            }

            // Add validator
            self.validator = self.$("form#userForm").validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    username: {
                        required: true,
                        minlength: 5,
                        onlyLatin: true
                    },
                    firstname: "required",
                    lastname: "required",
                    fathername: "required",
                    firstnamelatin: {
                        requiredIfOtherGreek: "form input[name=firstname]",
                        onlyLatin: true
                    },
                    lastnamelatin: {
                        requiredIfOtherGreek: "form input[name=lastname]",
                        onlyLatin: true
                    },
                    fathernamelatin: {
                        requiredIfOtherGreek: "form input[name=fathername]",
                        onlyLatin: true
                    },
                    identification: {
                        required: !(role.discriminator === 'PROFESSOR_DOMESTIC' || role.discriminator === 'PROFESSOR_FOREIGN')
                    },
                    password: {
                        required: true,
                        pwd: true,
                        minlength: 5
                    },
                    confirm_password: {
                        required: true,
                        minlength: 5,
                        equalTo: "form input[name=password]"
                    },
                    email: {
                        required: true,
                        email: true,
                        minlength: 2
                    },
                    mobile: {
                        required: true,
                        number: true,
                        minlength: 10
                    },
                    phone: {
                        required: _.isEqual(role.discriminator, "INSTITUTION_MANAGER"),
                        number: true,
                        minlength: 10
                    }
                },
                messages: {
                    username: {
                        required: $.i18n.prop('validation_username'),
                        minlength: $.i18n.prop('validation_minlength', 5),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    firstname: $.i18n.prop('validation_firstname'),
                    lastname: $.i18n.prop('validation_lastname'),
                    fathername: $.i18n.prop('validation_fathername'),
                    firstnamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_firstnamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    lastnamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_lastnamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    fathernamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_fathernamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    identification: {
                        required: $.i18n.prop('validation_required')
                    },
                    password: {
                        required: $.i18n.prop('validation_required'),
                        pwd: $.i18n.prop('validation_password'),
                        minlength: $.i18n.prop('validation_minlength', 5)
                    },
                    confirm_password: {
                        required: $.i18n.prop('validation_required'),
                        minlength: $.i18n.prop('validation_minlength', 5),
                        equalTo: $.i18n.prop('validation_confirmpassword')
                    },
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email'),
                        minlength: $.i18n.prop('validation_minlength', 2)
                    },
                    mobile: {
                        required: $.i18n.prop('validation_mobile'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_minlength', 10)
                    },
                    phone: {
                        required: $.i18n.prop('validation_phone'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_phone')
                    }
                }
            });
            // Tooltips
            self.$("i[rel=popover]").popover({
                html: 'true',
                trigger: 'hover'
            });
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }

            return self;
        },

        changeInstitution: function () {
            var self = this;
            if (_.isEqual(self.$("select[name=institution]").val(), "")) {
                self.$("a#selectInstitution").attr("disabled", true);
            } else {
                self.$("a#selectInstitution").removeAttr("disabled");
            }
        },

        submit: function (event) {
            var self = this;

            // Read Input
            var username = self.$('form input[name=username]').val();
            var firstname = self.$('form input[name=firstname]').val();
            var lastname = self.$('form input[name=lastname]').val();
            var fathername = self.$('form input[name=fathername]').val();
            var firstnamelatin = self.$('form input[name=firstnamelatin]').val();
            var lastnamelatin = self.$('form input[name=lastnamelatin]').val();
            var fathernamelatin = self.$('form input[name=fathernamelatin]').val();
            var identification = self.$('form input[name=identification]').val();
            var password = self.$('form input[name=password]').val();
            var mobile = self.$('form input[name=mobile]').val();
            var phone = self.$('form input[name=phone]').val();
            var email = self.$('form input[name=email]').val();

            // Validate

            // Save to model
            self.model.save({
                "username": username,
                "identification": identification,
                "basicInfo": {
                    "firstname": firstname,
                    "lastname": lastname,
                    "fathername": fathername
                },
                "basicInfoLatin": {
                    "firstname": firstnamelatin,
                    "lastname": lastnamelatin,
                    "fathername": fathernamelatin
                },
                "contactInfo": {
                    "email": email,
                    "mobile": mobile,
                    "phone": phone
                },
                "password": password
            }, {
                wait: true,
                success: function () {
                    App.router.navigate("success", {
                        trigger: true
                    });
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        selectInstitution: function () {
            var self = this;
            var role = self.model.get('roles')[0];
            var institutionId = self.$("select[name=institution]").val();
            role.institution = App.institutions.get(institutionId).toJSON();
            self.model.trigger("change");
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserVerificationView ****************************************************
     **************************************************************************/
    Views.UserVerificationView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_verification);
            this.model.bind('change', this.render);
        },

        render: function () {
            var self = this;
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(this.model.toJSON()));
            return this;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }

    });

    /***************************************************************************
     * HomeView ****************************************************************
     **************************************************************************/
    Views.HomeView = Views.BaseView.extend({
        tagName: "div",

        className: "hero-unit",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_home);
            this.model.bind('change', this.render);
        },

        events: {},

        render: function () {
            var self = this;
            var tiles = [];
            tiles.push({
                link: "account"
            });
            if (!self.model.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                tiles.push({
                    link: "profile"
                });
            }
            if (self.model.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE") && self.model.getRole('ADMINISTRATOR').superAdministrator) {
                tiles.push({
                    link: "administrators"
                });
            }
            if (self.model.hasRoleWithStatus("PROFESSOR_DOMESTIC", "ACTIVE")) {
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "professorCommittees"
                });
                tiles.push({
                    link: "professorEvaluations"
                });
            }
            if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "professorCommittees"
                });
                tiles.push({
                    link: "professorEvaluations"
                });
            }
            if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "sposition"
                });
                tiles.push({
                    link: "candidateCandidacies"
                });
            }
            if (self.model.hasRoleWithStatus("INSTITUTION_MANAGER", "ACTIVE")) {
                tiles.push({
                    link: "iassistants"
                });
                tiles.push({
                    link: "regulatoryframeworks"
                });
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "positions"
                });
            }
            if (self.model.hasRoleWithStatus("INSTITUTION_ASSISTANT", "ACTIVE")) {
                tiles.push({
                    link: "regulatoryframeworks"
                });
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "positions"
                });
            }
            if (self.model.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE")) {
                tiles.push({
                    link: "massistants"
                });
                tiles.push({
                    link: "searchusers"
                });
                tiles.push({
                    link: "statistics"
                });
                tiles.push({
                    link: "regulatoryframeworks"
                });
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "positions"
                });
            }
            if (self.model.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
                tiles.push({
                    link: "searchusers"
                });
                tiles.push({
                    link: "statistics"
                });
                tiles.push({
                    link: "regulatoryframeworks"
                });
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "positions"
                });
            }
            if (self.model.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                if (self.model.getRole('ADMINISTRATOR').superAdministrator) {
                    tiles.push({
                        link: "administrators"
                    });
                    tiles.push({
                        link: "adminCandidacies"
                    });
                    tiles.push({
                        link: "dataExports"
                    });
                }
                tiles.push({
                    link: "searchusers"
                });
                tiles.push({
                    link: "statistics"
                });
                tiles.push({
                    link: "regulatoryframeworks"
                });
                tiles.push({
                    link: "registers"
                });
                tiles.push({
                    link: "positions"
                });
            }

            tiles = _.uniq(tiles, false, function (tile) {
                return tile.link;
            });
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(_.extend(this.model.toJSON(), {
                "tiles": (function () {
                    var result = [];
                    var row = 0;
                    var col = 0;
                    while (tiles.length) {
                        if (!result[row]) {
                            result[row] = [];
                        }
                        result[row].push(tiles.shift());
                        col = (col + 1) % 3;
                        if (col === 0) {
                            row += 1;
                        }
                    }
                    return result;
                }())
            })));
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

// AccountView
    Views.AccountView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "change", "submit", "remove", "status", "cancel", "applyRules", "validatorRules");
            this.template = _.template(tpl_user_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "click a[data-action=status]": "status",
            "click a#remove": "remove",
            "click a#cancel": "cancel",
            "submit form": "submit"
        },

        applyRules: function () {
            var self = this;
            // Actions:
            self.$("a#remove").hide();
            self.$("a#status").addClass("disabled");
            // Fields:
            if (self.model.isNew()) {
                // Creating a user (e.g. new Institution Assistant)
                self.$("input[name=username]").removeAttr("disabled");
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
            } else if (self.model.get("authenticationType") === 'EMAIL' && self.model.isAccountIncomplete()) {
                // Incomplete Account created manually
                self.$("input[name=username]").attr("disabled", true);
                if (self.model.get("basicInfo").firstname) {
                    self.$("input[name=firstname]").attr("disabled", true); // Completed by helpdesk
                } else {
                    self.$("input[name=firstname]").removeAttr("disabled");
                }
                if (self.model.get("basicInfo").lastname) {
                    self.$("input[name=lastname]").attr("disabled", true);  // Completed by helpdesk
                } else {
                    self.$("input[name=lastname]").removeAttr("disabled");
                }
                if (self.model.get("basicInfo").fathername) {
                    self.$("input[name=fathername]").attr("disabled", true);  // Completed by helpdesk
                } else {
                    self.$("input[name=fathername]").removeAttr("disabled");
                }
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
            } else if (self.model.get("authenticationType") === 'SHIBBOLETH' && self.model.isAccountIncomplete()) {
                // Incomplete Account created by Shibboleth
                self.$("input[name=username]").removeAttr("disabled");
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
            } else if (self.model.get("authenticationType") === 'USERNAME' && self.model.get("status") === "UNAPPROVED") {
                self.$("input[name=username]").attr("disabled", true);
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
            } else {
                // Nothing is missing and account is ACTIVE
                self.$("input[name=username]").attr("disabled", true);
                self.$("input[name=firstname]").attr("disabled", true);
                self.$("input[name=lastname]").attr("disabled", true);
                self.$("input[name=fathername]").attr("disabled", true);
                self.$("input[name=firstnamelatin]").attr("disabled", true);
                self.$("input[name=lastnamelatin]").attr("disabled", true);
                self.$("input[name=fathernamelatin]").attr("disabled", true);
                self.$("input[name=identification]").attr("disabled", true);
            }
        },

        validatorRules: function () {
            var self = this;
            return {
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    username: {
                        required: true,
                        minlength: 5,
                        onlyLatin: true
                    },
                    firstname: "required",
                    lastname: "required",
                    fathername: "required",
                    firstnamelatin: {
                        requiredIfOtherGreek: "form input[name=firstname]",
                        onlyLatin: true
                    },
                    lastnamelatin: {
                        requiredIfOtherGreek: "form input[name=lastname]",
                        onlyLatin: true
                    },
                    fathernamelatin: {
                        requiredIfOtherGreek: "form input[name=fathername]",
                        onlyLatin: true
                    },
                    identification: {
                        required: !(self.model.hasRole('PROFESSOR_DOMESTIC') || self.model.hasRole('PROFESSOR_FOREIGN') || self.model.hasRole('ADMINISTRATOR'))
                    },
                    password: {
                        required: self.model.isNew(),
                        pwd: true,
                        minlength: 5
                    },
                    confirm_password: {
                        required: self.model.isNew(),
                        minlength: 5,
                        equalTo: "form input[name=password]"
                    },
                    mobile: {
                        required: !(self.model.hasRole('ADMINISTRATOR')),
                        number: true,
                        minlength: 10
                    },
                    phone: {
                        required: (self.model.hasRole("INSTITUTION_MANAGER") || self.model.hasRole("INSTITUTION_ASSISTANT")),
                        number: true,
                        minlength: 10
                    },
                    email: {
                        required: true,
                        email: true,
                        minlength: 2
                    }
                },
                messages: {
                    username: {
                        required: $.i18n.prop('validation_username'),
                        minlength: $.i18n.prop('validation_minlegth', 5),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    firstname: $.i18n.prop('validation_firstname'),
                    lastname: $.i18n.prop('validation_lastname'),
                    fathername: $.i18n.prop('validation_fathername'),
                    firstnamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_firstnamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    lastnamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_lastnamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    fathernamelatin: {
                        requiredIfOtherGreek: $.i18n.prop('validation_fathernamelatin'),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    identification: {
                        required: $.i18n.prop('validation_identification')
                    },
                    password: {
                        required: $.i18n.prop('validation_required'),
                        pwd: $.i18n.prop('validation_password'),
                        minlength: $.i18n.prop('validation_minlength', 5)
                    },
                    confirm_password: {
                        required: $.i18n.prop('validation_required'),
                        minlength: $.i18n.prop('validation_minlength', 5),
                        equalTo: $.i18n.prop('validation_confirmpassword')
                    },
                    mobile: {
                        required: $.i18n.prop('validation_mobile'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_minlength', 10)
                    },
                    phone: {
                        required: $.i18n.prop('validation_phone'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_phone')
                    },
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email'),
                        minlength: $.i18n.prop('validation_minlength', 2)
                    }
                }
            };
        },

        render: function () {
            var self = this;
            var propName;
            var tpl_data = _.extend(self.model.toJSON(), {
                canConnectToShibboleth: self.model.canConnectToShibboleth()
            });
            // 1. Render
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(tpl_data));
            // 2. Check State to enable/disable fields
            self.applyRules();
            // 3. Add Validator
            self.validator = self.$("form").validate(self.validatorRules());
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }
            // Tooltips
            self.$("i[rel=popover]").popover({
                html: 'true',
                trigger: 'hover'
            });
            // Disable Save Button until user changes a field,
            // user does not have permanent field
            self.$("a#save").attr("disabled", true);
            self.$("span#accounthelpdesk").show();

            // Return
            return self;
        },

        cancel: function () {
            var self = this;
            if (self.validator) {
                self.validator.resetForm();
                self.render();
            }
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        submit: function () {
            var self = this;
            // Read Input
            var username = self.$('form input[name=username]').val();
            var firstname = self.$('form input[name=firstname]').val();
            var lastname = self.$('form input[name=lastname]').val();
            var fathername = self.$('form input[name=fathername]').val();
            var firstnamelatin = self.$('form input[name=firstnamelatin]').val();
            var lastnamelatin = self.$('form input[name=lastnamelatin]').val();
            var fathernamelatin = self.$('form input[name=fathernamelatin]').val();
            var identification = self.$('form input[name=identification]').val();
            var password = self.$('form input[name=password]').val();
            var email = self.$('form input[name=email]').val();
            var mobile = self.$('form input[name=mobile]').val();
            var phone = self.$('form input[name=phone]').val();

            // Validate

            // Save to model
            self.model.save({
                "username": username,
                "identification": identification,
                "basicInfo": {
                    "firstname": firstname,
                    "lastname": lastname,
                    "fathername": fathername
                },
                "basicInfoLatin": {
                    "firstname": firstnamelatin,
                    "lastname": lastnamelatin,
                    "fathername": fathernamelatin
                },
                "contactInfo": {
                    "email": email,
                    "mobile": mobile,
                    "phone": phone
                },
                "password": password
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    self.model.trigger("sync:save");
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            return false;
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        status: function (event) {
            var self = this;
            self.model.status({
                "status": $(event.currentTarget).attr('status')
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * IncompleteAccountView ***************************************************
     **************************************************************************/
    Views.IncompleteAccountView = Views.AccountView.extend({
        initialize: function (options) {
            this._super('initialize', [options]);
        },

        applyRules: function () {
            var self = this;
            self.$("a#status").addClass("disabled");
            self.$("a#save").show();
            self.$("a#remove").hide();

            self.$("input[name=username]").attr("disabled", true);
            if (self.model.get("missingRequiredFields")) {
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
            } else {
                self.$("input[name=firstname]").attr("disabled", true);
                self.$("input[name=lastname]").attr("disabled", true);
                self.$("input[name=fathername]").attr("disabled", true);
                self.$("input[name=firstnamelatin]").attr("disabled", true);
                self.$("input[name=lastnamelatin]").attr("disabled", true);
                self.$("input[name=fathernamelatin]").attr("disabled", true);
                self.$("input[name=identification]").attr("disabled", true);
            }
        },

        render: function (eventName) {
            var self = this;
            self._super('render', [eventName]);
            self.$("span#accounthelpdesk").hide();
            return self;
        }
    });

    /***************************************************************************
     * AdminAccountView ********************************************************
     **************************************************************************/
    Views.AdminAccountView = Views.AccountView.extend({
        initialize: function (options) {
            this._super('initialize', [options]);
        },

        applyRules: function () {
            var self = this;
            if (self.model.hasRole("INSTITUTION_ASSISTANT") || self.model.hasRole("MINISTRY_ASSISTANT")) {
                self.$("a#status").addClass("disabled");
                self.$("input").attr("disabled", true);
                self.$("a#save").hide();
                self.$("a#remove").hide();
            } else {
                self.$("input").attr("disabled", true);
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=email]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");

                self.$("a#status").removeClass("disabled");
                self.$("a#save").show();
                self.$("a#remove").hide();
            }
        },

        validatorRules: function () {
            var self = this;
            return {
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    username: {
                        minlength: 5,
                        onlyLatin: true
                    },
                    firstnamelatin: {
                        onlyLatin: true
                    },
                    lastnamelatin: {
                        onlyLatin: true
                    },
                    fathernamelatin: {
                        onlyLatin: true
                    },
                    identification: {},
                    password: {
                        required: self.model.isNew(),
                        pwd: true,
                        minlength: 5
                    },
                    confirm_password: {
                        required: self.model.isNew(),
                        minlength: 5,
                        equalTo: "form input[name=password]"
                    },
                    mobile: {
                        number: true,
                        minlength: 10
                    },
                    phone: {
                        number: true,
                        minlength: 10
                    },
                    email: {
                        email: true,
                        minlength: 2
                    }
                },
                messages: {
                    username: {
                        minlength: $.i18n.prop('validation_minlegth', 5),
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    firstnamelatin: {
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    lastnamelatin: {
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    fathernamelatin: {
                        onlyLatin: $.i18n.prop('validation_latin')
                    },
                    password: {
                        required: $.i18n.prop('validation_required'),
                        pwd: $.i18n.prop('validation_password'),
                        minlength: $.i18n.prop('validation_minlength', 5)
                    },
                    confirm_password: {
                        required: $.i18n.prop('validation_required'),
                        minlength: $.i18n.prop('validation_minlength', 5),
                        equalTo: $.i18n.prop('validation_confirmpassword')
                    },
                    mobile: {
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_minlength', 10)
                    },
                    phone: {
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_phone')
                    },
                    email: {
                        email: $.i18n.prop('validation_email'),
                        minlength: $.i18n.prop('validation_minlength', 2)
                    }
                }
            };
        },

        render: function (eventName) {
            var self = this;
            self._super('render', [eventName]);
            self.$("span#accounthelpdesk").hide();
            return self;
        }
    });

    /***************************************************************************
     * UserView ****************************************************************
     **************************************************************************/
    Views.UserView = Views.BaseView.extend({
        tagName: "div",

        options: {
            editable: true
        },

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user);
            this.model.bind("change", this.render, this);
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.$el.html(self.template(self.model.toJSON()));

            // Add Role Views
            self.collection.each(function (role) {
                var roleView = new Views.RoleView({
                    collection: self.collection,
                    model: role
                });
                self.$("#roles").append(roleView.render().el);
                self.innerViews.push(roleView);
            });
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserHelpdeskView ********************************************************
     **************************************************************************/
    Views.UserHelpdeskView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        events: {
            "click #resendLoginEmail": "resendLoginEmail",
            "click #resendReminderLoginEmail": "resendReminderLoginEmail",
            "click #resendEvaluationEmail": "resendEvaluationEmail"
        },

        initialize: function (options) {
            var self = this;

            self._super('initialize', [options]);
            _.bindAll(self, "openIssue", "resendLoginEmail", "resendReminderLoginEmail", "resendEvaluationEmail", "resetIssue");
            self.template = _.template(tpl_user_helpdesk);
            self.issueModel = new Models.JiraIssue();
            self.issueModel.url = self.issueModel.urlRoot + "admin";
            self.issueModel.on("sync", function () {
                self.resetIssue();
            });
        },

        resetIssue: function () {
            var self = this;
            self.issueModel.set(_.defaults({
                status: 'OPEN',
                role: self.model.get('primaryRole'),
                fullname: self.model.get('firstname')[App.locale] + ' ' + self.model.get('lastname')[App.locale],
                mobile: self.model.get('contactInfo').mobile,
                email: self.model.get('contactInfo').email
            }, self.issueModel.defaults));
        },

        render: function () {
            var self = this;
            var userView = new Views.AdminAccountView({
                model: self.model
            });
            var roleViews = self.collection.map(function (role) {
                return new Views.AdminRoleEditView({
                    className: "row-fluid",
                    collection: self.collection,
                    model: role
                });
            });
            var jiraIssueEditView = new Views.JiraIssueEditView({
                model: self.issueModel
            });

            self.closeInnerViews();
            self.$el.empty();
            self.$el.html(self.template());

            // Add User View
            self.$("#userContent").html(userView.render().el);
            self.innerViews.push(userView);
            // Add Role Views
            _.each(roleViews, function (roleView) {
                var discriminator = roleView.model.get("discriminator");
                self.$("#optionsTab").append('<li><a href="#profile_' + discriminator + '" data-toggle="tab">' + $.i18n.prop(discriminator) + '</a></li>');
                self.$("#optionsContent").append('<div class="tab-pane" id="profile_' + discriminator + '"></div>');
                self.$("#profile_" + discriminator).html(roleView.render().el);
                self.innerViews.push(roleView);
            });
            // Add JiraIssueEditView
            self.resetIssue();
            self.$("#issue").html(jiraIssueEditView.render().el);
            self.$("#helpdeskTab").appendTo(self.$("#optionsTab")); // Move as last element in tabs
            self.$("#helpdeskContent").appendTo(self.$("#optionsContent")); // Move as last element in content
            self.innerViews.push(jiraIssueEditView);

            return self;
        },

        openIssue: function () {
            var self = this;
            var jiraIssue = new Models.JiraIssue({
                status: "OPEN",
                type: self.$("form[name=jira] select[name=type]").val(),
                call: self.$("form[name=jira] select[name=call]").val(),
                summary: self.$("form[name=jira] input[name=summary]").val(),
                description: self.$("form[name=jira] textarea[name=description]").val()
            });

            jiraIssue.save({}, {
                wait: true,
                success: function () {
                    var popup;
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        resendLoginEmail: function () {
            var self = this;
            self.model.resendLoginEmail({}, {
                wait: true,
                createLoginLink: self.$("input[name=createLoginLink]").is(':checked'),
                success: function () {
                    var popup;
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        resendReminderLoginEmail: function () {
            var self = this;
            self.model.resendReminderLoginEmail({}, {
                wait: true,
                success: function () {
                    var popup;
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        resendEvaluationEmail: function () {
            var self = this;
            self.model.resendEvaluationEmail({}, {
                wait: true,
                success: function () {
                    var popup;
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserSearchView **********************************************************
     **************************************************************************/
    Views.UserSearchView = Views.BaseView.extend({
        tagName: "div",

        className: "span12",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "search", "handleKeyUp");
            this.template = _.template(tpl_user_search);
            this.templateRoleInfo = _.template(tpl_user_role_info);
        },

        events: {
            "click a#search": "search",
            "keyup form": "handleKeyUp"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                displayCreateDomesticButton : App.loggedOnUser.hasRole("ADMINISTRATOR") ? App.loggedOnUser.getRole("ADMINISTRATOR").superAdministrator : false
            }
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.html(self.template(tpl_data));

            // Fill Institutions
            App.institutions = App.institutions || new Models.Institutions();
            App.institutions.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    var $select = self.$("select[name='institution']");

                    $select.append('<option value="">--</option>');
                    collection.each(function (institution) {
                        $select.append('<option value="' + institution.get("id") + '">' + institution.getName(App.locale) + '</option>');
                    });
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });

            // Initialize DataTable
            self.$("table#usersTable").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                }
            });
            return self;
        },

        search: function () {
            var self = this;

            function readFormValues() {
                var formData = [
                    {
                        name: "username",
                        value: self.$('form input[name=username]').val()
                    },
                    {
                        name: "firstname",
                        value: self.$('form input[name=firstname]').val()
                    },
                    {
                        name: "lastname",
                        value: self.$('form input[name=lastname]').val()
                    },
                    {
                        name: "mobile",
                        value: self.$('form input[name=mobile]').val()
                    },
                    {
                        name: "email",
                        value: self.$('form input[name=email]').val()
                    },
                    {
                        name: "status",
                        value: self.$('form select[name=status]').val()
                    },
                    {
                        name: "role",
                        value: self.$('form select[name=role]').val()
                    },
                    {
                        name: "roleStatus",
                        value: self.$('form select[name=roleStatus]').val()
                    },
                    {
                        name: "institution",
                        value: self.$("form select[name=institution]").val()
                    }
                ];
                var user = self.$('form input[name=user]').val();
                formData.push({
                    name: "user",
                    value: /^\d+$/.test(user) ? user : ""
                });
                return formData;
            }

            var searchData = readFormValues();
            // Init DataTables with a custom callback to get results
            self.$("table#usersTable").dataTable().fnDestroy();
            self.$("table#usersTable").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "id", 'sWidth': '1px'},
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "username"},
                    {"mData": "status"},
                    {"mData": "role", 'sWidth': '25%', "bSortable": false},
                    {"mData": "roleStatus", "bSortable": false}
                ],
                "bProcessing": true,
                "bServerSide": true,
                "sAjaxSource": self.options.searchURL,
                "fnServerData": function (sSource, aoData, fnCallback) {
                    /* Add some extra data to the sender */
                    $.ajax({
                        "dataType": 'json',
                        "type": "POST",
                        "url": sSource,
                        "data": aoData.concat(searchData),
                        "success": function (json) {
                            // Read Data
                            json.aaData = _.map(json.records, function (user) {
                                return {
                                    "id": '<a href="#user/' + user.id + '" target="user">' + user.id + '</a>',
                                    "firstname": user.firstname[App.locale],
                                    "lastname": user.lastname[App.locale],
                                    "username": user.username || "",
                                    "status": $.i18n.prop('status' + user.status),
                                    "role": self.templateRoleInfo(user),
                                    "roleStatus": $.i18n.prop('status' + _.find(user.roles, function (r) {
                                        return r.discriminator === user.primaryRole;
                                    }).status)
                                };
                            });
                            fnCallback(json);
                        }
                    });
                }
            });
        },

        handleKeyUp: function (event) {
            var self = this;
            if (event.keyCode === 13) {
                // On enter submit
                self.search();
            }
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * UserRoleInfoView ********************************************************
     **************************************************************************/
    Views.UserRoleInfoView = Views.BaseView.extend({
        tagName: "p",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_role_info);
            this.model.bind('change', this.render, this);
        },

        render: function () {
            var self = this;
            var tpl_data = {
                roles: self.model.get("roles")
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RoleTabsView ************************************************************
     **************************************************************************/
    Views.RoleTabsView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "select", "highlightSelected");
            this.template = _.template(tpl_role_tabs);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
            this.collection.bind("role:selected", this.highlightSelected, this);
        },

        events: {
            "click a.selectRole": "select"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                roles: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item = model.toJSON();
                        item.cid = model.cid;
                        result.push(item);
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(tpl_data));
            return self;
        },

        select: function (event, role) {
            var self = this;
            var selectedModel = role || self.collection.get($(event.target).attr('role'));
            if (selectedModel) {
                self.collection.trigger("role:selected", selectedModel);
            }
        },

        highlightSelected: function (role) {
            var self = this;
            self.$("li.active").removeClass("active");
            self.$("a[role=" + role.cid + "]").parent("li").addClass("active");
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RoleView ****************************************************************
     **************************************************************************/
    Views.RoleView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this.template = _.template(tpl_role);
            this._super('initialize', [options]);
            this.model.bind("change", this.render, this);
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            if (self.model.get("discriminator") !== "ADMINISTRATOR") {
                self.$el.append(self.template(self.model.toJSON()));

                switch (self.model.get("discriminator")) {
                    case "CANDIDATE":
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFile(collection, "TAYTOTHTA", self.$("#tautotitaFile"), {
                                    withMetadata: false
                                });
                                self.addFile(collection, "BEBAIWSH_STRATIOTIKIS_THITIAS", self.$("#bebaiwsiStratiwtikisThitiasFile"), {
                                    withMetadata: false
                                });
                                self.addFile(collection, "FORMA_SYMMETOXIS", self.$("#formaSymmetoxisFile"), {
                                    withMetadata: false
                                });
                                self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
                                    withMetadata: false
                                });
                                self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
                                    withMetadata: true
                                });
                                self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
                                    withMetadata: true
                                });
                            }
                        });
                        break;
                    case "PROFESSOR_DOMESTIC":
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFile(collection, "PROFILE", self.$("#profileFile"), {
                                    withMetadata: false
                                });
                                self.addFile(collection, "FEK", self.$("#fekFile"), {
                                    withMetadata: false
                                });
                            }
                        });
                        break;
                    case "PROFESSOR_FOREIGN":
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFile(collection, "PROFILE", self.$("#profileFile"), {
                                    withMetadata: false
                                });
                            }
                        });
                        break;
                    case "INSTITUTION_MANAGER":
                        break;
                    case "INSTITUTION_ASSISTANT":
                        break;
                    case "MINISTRY_MANAGER":
                        break;
                    case "MINISTRY_ASSISTANT":
                        break;
                    default:
                        break;
                }
            }
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RoleEditView ************************************************************
     **************************************************************************/
    Views.RoleEditView = Views.BaseView.extend({
        tagName: "div",

        id: "roleview",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "change", "isEditable", "validationRules", "beforeUpload", "beforeDelete", "submit", "cancel");
            this.template = _.template(tpl_role_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "click a[data-action=status]": "status",
            "click a#remove": "remove",
            "click a#cancel": "cancel",
            "submit form": "submit"
        },

        isEditable: function (field) {
            var self = this;

            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    switch (field) {
                        case "tautotitaFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "bebaiwsiStratiwtikisThitiasFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "formaSymmetoxisFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "biografikoFile":
                            return true;
                        case "ptyxioFileList":
                            return true;
                        case "dimosieusiFileList":
                            return true;
                        default:
                            break;
                    }
                    break;
                case "PROFESSOR_DOMESTIC":
                    switch (field) {
                        case "department":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("department").id)); // Not Completed from Manual Insertion
                        case "rank":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("rank").id)); // Not Completed from Manual Insertion
                        case "fek":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("fek"))); // Not Completed from Manual Insertion
                        case "fekCheckbox":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("fekSubject"))); // Not Completed from Manual Insertion
                        case "fekSubject":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("fekSubject"))) && // Not Completed from Manual
                                (_.isObject(self.model.get("fekSubject")) || (_.isUndefined(self.model.get("fekSubject")) && _.isUndefined(self.model.get("subject"))));  // subject is not defined
                        case "subject":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && !(_.isEqual(self.model.get("user").authenticationType,
                                    "EMAIL") && !_.isUndefined(self.model.get("fekSubject"))) && !self.isEditable("fekSubject"); // fekSubject is not defined
                        case "hasOnlineProfile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "profileURL":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") &&
                                self.model.get("hasOnlineProfile"); // has online profile is true
                        case "profileFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "fekFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "hasAcceptedTerms":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        default:
                            break;
                    }
                    break;
                case "PROFESSOR_FOREIGN":
                    switch (field) {
                        case "institution":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "country":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "hasOnlineProfile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "profileURL":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED") && self.model.get("hasOnlineProfile");
                        case "profileFile":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "rank":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "subject":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "speakingGreek":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "hasAcceptedTerms":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        default:
                            break;
                    }
                    break;
                case "INSTITUTION_MANAGER":
                    switch (field) {
                        case "institution":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "verificationAuthority":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "verificationAuthorityName":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatefirstname":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatelastname":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatefathername":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatefirstnamelatin":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatelastnamelatin":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternatefathernamelatin":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        case "alternateemail":
                            return true;
                        case "alternatemobile":
                            return true;
                        case "alternatephone":
                            return true;
                        default:
                            break;
                    }
                    break;
                case "INSTITUTION_ASSISTANT":
                    switch (field) {
                        case "institution":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        default:
                            break;
                    }
                    break;
                case "MINISTRY_MANAGER":
                    switch (field) {
                        case "ministry":
                            return _.isEqual(self.model.get("status"), "UNAPPROVED");
                        default:
                            break;
                    }
                    break;
                case "MINISTRY_ASSISTANT":
                    break;
                default:
                    break;
            }
            return false;
        },

        validationRules: function () {
            var self = this;
            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            tautotitaFile: "required",
                            formaSymmetoxisFile: "required"
                        },
                        messages: {
                            tautotitaFile: $.i18n.prop('validation_file'),
                            formaSymmetoxisFile: $.i18n.prop('validation_file')
                        }
                    };
                case "PROFESSOR_DOMESTIC":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            department: "required",
                            profileURL: {
                                required: "input[name=hasOnlineProfile]:not(:checked)",
                                url: true
                            },
                            rank: "required",
                            fek: "required",
                            fekSubject: {
                                "required": "input[name=fekCheckbox]:not(:checked)"
                            },
                            subject: {
                                "required": "input[name=fekCheckbox]:checked"
                            },
                            "hasAcceptedTerms": "required"
                        },
                        messages: {
                            department: $.i18n.prop('validation_department'),
                            profileURL: {
                                required: $.i18n.prop('validation_required'),
                                url: $.i18n.prop('validation_profileURL')
                            },
                            rank: $.i18n.prop('validation_rank'),
                            subject: $.i18n.prop('validation_subject'),
                            fek: $.i18n.prop('validation_required'),
                            fekSubject: $.i18n.prop('validation_fekSubject'),
                            hasAcceptedTerms: $.i18n.prop('validation_hasAcceptedTerms')
                        }
                    };
                case "PROFESSOR_FOREIGN":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            institution: "required",
                            profileURL: {
                                required: "input[name=hasOnlineProfile]:not(:checked)",
                                url: true
                            },
                            country: "required",
                            rank: "required",
                            subject: "required",
                            speakingGreek: "required",
                            hasAcceptedTerms: "required"

                        },
                        messages: {
                            institution: $.i18n.prop('validation_institution'),
                            profileURL: $.i18n.prop('validation_profileURL'),
                            country: $.i18n.prop('validation_country'),
                            rank: $.i18n.prop('validation_rank'),
                            subject: $.i18n.prop('validation_subject'),
                            speakingGreek: $.i18n.prop('validation_speakingGreek'),
                            hasAcceptedTerms: $.i18n.prop('validation_hasAcceptedTerms')
                        }
                    };
                case "INSTITUTION_MANAGER":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            institution: "required",
                            verificationAuthority: "required",
                            verificationAuthorityName: "required",
                            alternatefirstname: "required",
                            alternatelastname: "required",
                            alternatefathername: "required",
                            alternatefirstnamelatin: {
                                required: true,
                                onlyLatin: true
                            },
                            alternatelastnamelatin: {
                                required: true,
                                onlyLatin: true
                            },
                            alternatefathernamelatin: {
                                required: true,
                                onlyLatin: true
                            },
                            alternateemail: {
                                required: true,
                                email: true,
                                minlength: 2
                            },
                            alternatemobile: {
                                required: true,
                                number: true,
                                minlength: 10
                            },
                            alternatephone: {
                                required: true,
                                number: true,
                                minlength: 10
                            }
                        },
                        messages: {
                            institution: $.i18n.prop('validation_institution'),
                            verificationAuthority: $.i18n.prop('validation_verificationAuthority'),
                            verificationAuthorityName: $.i18n.prop('validation_verificationAuthorityName'),
                            alternatefirstname: $.i18n.prop('validation_firstname'),
                            alternatelastname: $.i18n.prop('validation_lastname'),
                            alternatefathername: $.i18n.prop('validation_fathername'),
                            alternatefirstnamelatin: {
                                requiredIfOtherGreek: "form input[name=alternatefirstname]",
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatelastnamelatin: {
                                requiredIfOtherGreek: "form input[name=alternatelastname]",
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatefathernamelatin: {
                                requiredIfOtherGreek: "form input[name=alternatefathername]",
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatemobile: {
                                required: $.i18n.prop('validation_mobile'),
                                number: $.i18n.prop('validation_number'),
                                minlength: $.i18n.prop('validation_minlength', 10)
                            },
                            alternatephone: {
                                required: $.i18n.prop('validation_phone'),
                                number: $.i18n.prop('validation_number'),
                                minlength: $.i18n.prop('validation_minlength', 10)
                            },
                            alternateemail: {
                                required: $.i18n.prop('validation_email'),
                                email: $.i18n.prop('validation_email'),
                                minlength: $.i18n.prop('validation_minlength', 2)
                            }
                        }
                    };
                case "INSTITUTION_ASSISTANT":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            institution: "required"
                        },
                        messages: {
                            institution: $.i18n.prop('validation_institution')
                        }
                    };
                case "MINISTRY_MANAGER":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            ministry: "required"
                        },
                        messages: {
                            ministry: $.i18n.prop('validation_ministry')
                        }
                    };
                case "MINISTRY_ASSISTANT":
                    return {};
                default:
                    return {};
            }
        },

        beforeUpload: function (data, upload) {
            var self = this;
            var candidate = self.collection.find(function (role) {
                return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
            });
            var openCandidacies;
            if (candidate) {
                openCandidacies = new Models.CandidateCandidacies({}, {
                    candidate: App.loggedOnUser.getRole("CANDIDATE").id
                });
                openCandidacies.fetch({
                    data: {
                        "open": "true"
                    },
                    cache: false,
                    reset: true,
                    success: function (collection) {
                        var candidacyUpdateConfirmView;
                        if (collection.length > 0) {
                            candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
                                "collection": collection,
                                "answer": function (confirm) {
                                    if (confirm) {
                                        _.extend(data.formData, {
                                            "updateCandidacies": true
                                        });
                                    }
                                    upload(data);
                                }
                            });
                            candidacyUpdateConfirmView.show();
                        } else {
                            upload(data);
                        }
                    }
                });
            } else {
                upload(data);
            }
        },

        beforeDelete: function (file, doDelete) {
            var self = this;
            var candidate = self.collection.find(function (role) {
                return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
            });
            var openCandidacies;
            if (candidate) {
                openCandidacies = new Models.CandidateCandidacies({}, {
                    candidate: App.loggedOnUser.getRole("CANDIDATE").id
                });
                openCandidacies.fetch({
                    data: {
                        "open": "true"
                    },
                    cache: false,
                    reset: true,
                    success: function (collection) {
                        var candidacyUpdateConfirmView;
                        if (collection.length > 0) {
                            candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
                                "collection": collection,
                                "answer": function (confirm) {
                                    if (confirm) {
                                        doDelete({
                                            "updateCandidacies": true
                                        });
                                    } else {
                                        doDelete();
                                    }
                                }
                            });
                            candidacyUpdateConfirmView.show();
                        } else {
                            doDelete();
                        }
                    }
                });
            } else {
                doDelete();
            }
        },

        render: function () {
            var self = this;
            var tpl_data;
            var propName;
            var files;
            var departmentSelectView;
            // Close inner views (fileviews)
            self.closeInnerViews();
            // Re-render
            tpl_data = _.extend(self.model.toJSON(), {
                "primary": _.isEqual(self.model.get("discriminator"), self.model.get("user").primaryRole)
            });
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            // Apply Global Rules
            self.$("a#status").addClass("disabled");

            // Apply Specific Rule and fields per discriminator
            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    if (self.model.has("id")) {
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFileEdit(collection, "TAYTOTHTA", self.$("input[name=tautotitaFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("tautotitaFile")
                                });
                                self.addFileEdit(collection, "BEBAIWSH_STRATIOTIKIS_THITIAS", self.$("input[name=bebaiwsiStratiwtikisThitiasFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("bebaiwsiStratiwtikisThitiasFile")
                                });
                                self.addFileEdit(collection, "FORMA_SYMMETOXIS", self.$("input[name=formaSymmetoxisFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("formaSymmetoxisFile")
                                });
                                self.addFileEdit(collection, "BIOGRAFIKO", self.$("input[name=biografikoFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("biografikoFile"),
                                    beforeUpload: self.beforeUpload
                                });
                                self.addFileListEdit(collection, "PTYXIO", self.$("input[name=ptyxioFileList]"), {
                                    withMetadata: true,
                                    editable: self.isEditable("ptyxioFileList"),
                                    beforeUpload: self.beforeUpload,
                                    beforeDelete: self.beforeDelete
                                });
                                self.addFileListEdit(collection, "DIMOSIEYSI", self.$("input[name=dimosieusiFileList]"), {
                                    withMetadata: true,
                                    editable: self.isEditable("dimosieusiFileList"),
                                    beforeUpload: self.beforeUpload,
                                    beforeDelete: self.beforeDelete
                                });
                            }
                        });
                    } else {
                        self.$("#tautotitaFile").html($.i18n.prop("PressSave"));
                        self.$("#bebaiwsiStratiwtikisThitiasFile").html($.i18n.prop("PressSave"));
                        self.$("#formaSymmetoxisFile").html($.i18n.prop("PressSave"));
                        self.$("#biografikoFile").html($.i18n.prop("PressSave"));
                        self.$("#ptyxioFileList").html($.i18n.prop("PressSave"));
                        self.$("#dimosieusiFileList").html($.i18n.prop("PressSave"));
                    }
                    self.validator = $("form", this.el).validate(self.validationRules());
                    break;
                case "PROFESSOR_DOMESTIC":
                    App.departments = App.departments || new Models.Departments();
                    App.ranks = App.ranks || new Models.Ranks();
                    // Create Selector for departments
                    departmentSelectView = new Views.DepartmentSelectView({
                        el: self.$("input[name=department]"),
                        collection: App.departments,
                        editable: self.isEditable("department")
                    });
                    // Fetch Extra data
                    App.departments.fetch({
                        cache: true,
                        reset: true
                    });

                    App.ranks.fetch({
                        cache: true,
                        reset: true,
                        success: function (collection) {
                            self.$("select[name='rank']").empty();
                            $("select[name='rank']", self.$el).append("<option value=\"\">--</option>");
                            _.each(collection.filter(function (rank) {
                                switch (self.model.get("institution").category) {
                                    case "INSTITUTION":
                                        return _.isEqual(rank.get("category"), "PROFESSOR");
                                    case "RESEARCH_CENTER":
                                        return _.isEqual(rank.get("category"), "RESEARCHER");
                                }
                            }), function (rank) {
                                if (_.isObject(self.model.get("rank")) && _.isEqual(rank.id, self.model.get("rank").id)) {
                                    $("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "' selected>" + rank.getName(App.locale) + "</option>");
                                } else {
                                    $("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "'>" + rank.getName(App.locale) + "</option>");
                                }
                            });

                            self.$("select[name='rank']").trigger("change", {
                                triggeredBy: "application"
                            });

                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });

                    // Add Files
                    if (self.model.has("id")) {
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFileEdit(collection, "PROFILE", self.$("input[name=profileFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("profileFile")
                                });
                                self.addFileEdit(collection, "FEK", self.$("input[name=fekFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("fekFile")
                                });
                            }
                        });
                    } else {
                        $("#fekFile", self.$el).html($.i18n.prop("PressSave"));
                        $("#profileFile", self.$el).html($.i18n.prop("PressSave"));
                        $("#dimosieusiFileList", self.$el).html($.i18n.prop("PressSave"));
                    }

                    // Enable typeahead for Subjects:
                    self.$('input[name=subject], input[name=fekSubject]').typeahead({
                        source: function (query, process) {
                            var subjects = new Models.Subjects();
                            subjects.fetch({
                                cache: false,
                                reset: true,
                                data: {
                                    "query": query
                                },
                                success: function (collection) {
                                    var data = collection.pluck("name");
                                    process(data);
                                }
                            });
                        }
                    });

                    self.validator = $("form", this.el).validate(self.validationRules());

                    // On Rank change need to update departmentSelector
                    self.$("select[name='rank']").on("change", (function () {
                        // Keep previous values in this closure
                        var previousRank = App.ranks.get(self.$("select[name=rank]").val()) || new Models.Rank();

                        return function () {
                            // Trigger change on DepartmentSelectView
                            var rank = App.ranks.get(self.$("select[name=rank]").val()) || new Models.Rank();
                            var department = App.departments.get(self.$("input[name=department]").val());
                            if (!_.isEqual(rank.get("category"), previousRank.get("category"))) {
                                // If rank.category changes, close edit and
                                // re-render table with new Institution
                                // categories
                                departmentSelectView.render();
                                // If necessary clear selection
                                if (department) {
                                    if (_.isEqual(rank.get("category"), "PROFESSOR") && _.isEqual(department.get("school").institution.category, "RESEARCH_CENTER")) {
                                        departmentSelectView.select(undefined);
                                    } else if (_.isEqual(rank.get("category"), "RESEARCHER") && _.isEqual(department.get("school").institution.category, "INSTITUTION")) {
                                        departmentSelectView.select(undefined);
                                    }
                                }
                            }
                            previousRank = rank;
                        };

                    }()));
                    // OnlineProfile XOR ProfileFile
                    self.$("input[name=hasOnlineProfile]").change(function () {
                        if ($(this).is(":checked")) {
                            self.$("input[name=profileURL]").focus().val("").attr("disabled", true);
                        } else {
                            self.$("input[name=profileURL]").removeAttr("disabled");
                        }
                    });
                    self.$("input[name=fekCheckbox]").change(function () {
                        if ($(this).is(":checked")) {
                            self.$("textarea[name=fekSubject]").attr("disabled", true).val("");
                            self.$("textarea[name=subject]").removeAttr("disabled");
                        } else {
                            self.$("textarea[name=fekSubject]").removeAttr("disabled");
                            self.$("textarea[name=subject]").attr("disabled", true).val("");
                        }
                    });
                    // Subject XOR FekSubject
                    if (_.isObject(self.model.get("subject"))) {
                        self.$("input[name=fekCheckbox]").attr("checked", true);
                        self.$("textarea[name=fekSubject]").attr("disabled", true).val("");
                        self.$("textarea[name=subject]").removeAttr("disabled");
                    } else {
                        self.$("input[name=fekCheckbox]").removeAttr("checked");
                        self.$("textarea[name=fekSubject]").removeAttr("disabled");
                        self.$("textarea[name=subject]").attr("disabled", true).val("");
                    }
                    break;
                case "PROFESSOR_FOREIGN":
                    App.ranks = App.ranks || new Models.Ranks();
                    App.ranks.fetch({
                        cache: true,
                        reset: true,
                        success: function (collection) {
                            collection.each(function (rank) {
                                if (_.isObject(self.model.get("rank")) && _.isEqual(rank.id, self.model.get("rank").id)) {
                                    $("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "' selected>" + rank.getName(App.locale) + "</option>");
                                } else {
                                    $("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "'>" + rank.getName(App.locale) + "</option>");
                                }
                            });
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                    App.countries = App.countries || new Models.Countries();
                    App.countries.fetch({
                        cache: true,
                        reset: true,
                        success: function (collection) {
                            $("select[name='country']", self.$el).append("<option value=''>--</option>");
                            collection.each(function (country) {
                                if (_.isObject(self.model.get("country")) && _.isEqual(country.get("code"), self.model.get("country").code)) {
                                    $("select[name='country']", self.$el).append("<option value='" + country.get("code") + "' selected>" + country.get("name") + "</option>");
                                } else {
                                    $("select[name='country']", self.$el).append("<option value='" + country.get("code") + "'>" + country.get("name") + "</option>");
                                }
                            });
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                    // Enable typeahead for Subjects:
                    self.$('input[name=subject]').typeahead({
                        source: function (query, process) {
                            var subjects = new Models.Subjects();
                            subjects.fetch({
                                cache: false,
                                reset: true,
                                data: {
                                    "query": query
                                },
                                success: function (collection) {
                                    var data = collection.pluck("name");
                                    process(data);
                                }
                            });
                        }
                    });
                    if (self.model.has("id")) {
                        files = new Models.Files();
                        files.url = self.model.url() + "/file";
                        files.fetch({
                            cache: false,
                            success: function (collection) {
                                self.addFileEdit(collection, "PROFILE", self.$("input[name=profileFile]"), {
                                    withMetadata: false,
                                    editable: self.isEditable("profileFile")
                                });
                            }
                        });
                    } else {
                        self.$("#profileFile").html($.i18n.prop("PressSave"));
                    }

                    self.validator = $("form", this.el).validate(self.validationRules());

                    // OnlineProfile XOR ProfileFile
                    self.$("input[name=hasOnlineProfile]").change(function () {
                        if ($(this).is(":checked")) {
                            self.$("input[name=profileURL]").focus().val("").attr("disabled", true);
                        } else {
                            self.$("input[name=profileURL]").removeAttr("disabled");
                        }
                    });
                    break;
                case "INSTITUTION_MANAGER":
                    self.$("select[name='verificationAuthority']").change(function () {
                        var authority = self.$("select[name='verificationAuthority']").val();
                        self.$("label[for='verificationAuthorityName']").html($.i18n.prop('VerificationAuthorityName') + " " + $.i18n.prop('VerificationAuthority' + authority));
                        self.$("a[id^=forma_]*").hide();
                        self.$("a#forma_" + authority).show();
                    });
                    self.$("select[name='verificationAuthority']").val(self.model.get("verificationAuthority"));

                    self.$("select[name='institution']").change(function () {
                        self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());
                    });
                    App.institutions = App.institutions || new Models.Institutions();
                    App.institutions.fetch({
                        cache: true,
                        reset: true,
                        success: function (collection) {
                            _.each(collection.filter(function (institution) {
                                return _.isEqual(institution.get("category"), "INSTITUTION");
                            }), function (institution) {
                                if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
                                    $("select[name='institution']",
                                        self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.getName(App.locale) + "</option>");
                                } else {
                                    $("select[name='institution']",
                                        self.$el).append("<option value='" + institution.get("id") + "'>" + institution.getName(App.locale) + "</option>");
                                }
                            });
                            self.$("select[name='institution']").trigger("change", {
                                triggeredBy: "application"
                            });
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });

                    self.validator = $("form", this.el).validate(self.validationRules());

                    self.$("select[name='verificationAuthority']").trigger("change", {
                        triggeredBy: "application"
                    });
                    break;

                case "INSTITUTION_ASSISTANT":
                    self.$("select[name='institution']").change(function () {
                        self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());
                    });
                    App.institutions = App.institutions || new Models.Institutions();
                    App.institutions.fetch({
                        cache: true,
                        reset: true,
                        success: function (collection) {
                            _.each(collection.filter(function (institution) {
                                return _.isEqual(institution.get("category"), "INSTITUTION");
                            }), function (institution) {
                                if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
                                    $("select[name='institution']",
                                        self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.getName(App.locale) + "</option>");
                                } else {
                                    $("select[name='institution']",
                                        self.$el).append("<option value='" + institution.get("id") + "'>" + institution.getName(App.locale) + "</option>");
                                }
                            });
                            self.$("select[name='institution']").trigger("change", {
                                triggeredBy: "application"
                            });
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });

                    self.validator = $("form", this.el).validate(self.validationRules());

                    break;

                case "MINISTRY_MANAGER":

                    self.validator = $("form", this.el).validate(self.validationRules());
                    break;
                case "MINISTRY_ASSISTANT":
                    break;
                default:
                    break;
            }
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required !== undefined) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }
            // Set isEditable to fields
            self.$("select, input, textarea").each(function () {
                var field = $(this).attr("name");
                if (self.isEditable(field)) {
                    $(this).removeAttr("disabled");
                } else {
                    $(this).attr("disabled", true);
                }
            });
            // Disable Save Button until user changes a field,
            // roles do not have permanent field
            self.$("a#save").attr("disabled", true);
            //Tooltips
            self.$("i[rel=popover]").popover({
                html: 'true',
                trigger: 'hover'
            });
            return self;
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        submit: function (event) {
            var self = this;
            var candidate;
            var openCandidacies;
            var values = {};

            function doSave(values, updateCandidacies) {
                self.model.save(values, {
                    url: self.model.url() + (updateCandidacies ? "?updateCandidacies=true" : ""),
                    wait: true,
                    success: function () {
                        var popup = new Views.PopupView({
                            type: "success",
                            message: $.i18n.prop("Success")
                        });
                        popup.show();
                    },
                    error: function (model, resp) {
                        var popup = new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                        });
                        popup.show();
                    }
                });
            }

            // Read Input
            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    break;
                case "PROFESSOR_DOMESTIC":
                    values.department = {
                        "id": self.$('form input[name=department]').val()
                    };
                    values.institution = {
                        "id": self.$('input[name=institution]').val()
                    }
                    values.rank = {
                        "id": self.$('form select[name=rank]').val()
                    };
                    values.hasOnlineProfile = self.$('form input[name=hasOnlineProfile]').is(':not(:checked)');
                    values.profileURL = self.$('form input[name=profileURL]').val();
                    values.fek = self.$('form input[name=fek]').val();
                    if (self.$('form textarea[name=fekSubject]').val() !== '') {
                        values.fekSubject = {
                            "id": self.model.has("fekSubject") ? self.model.get("fekSubject").id : undefined,
                            "name": self.$('form textarea[name=fekSubject]').val()
                        };
                        values.subject = undefined;
                    }
                    if (self.$('form textarea[name=subject]').val() !== '') {
                        values.subject = {
                            "id": self.model.has("subject") ? self.model.get("subject").id : undefined,
                            "name": self.$('form textarea[name=subject]').val()
                        };
                        values.fekSubject = undefined;
                    }
                    values.hasAcceptedTerms = self.$('form input[name=hasAcceptedTerms]').is(':checked');
                    break;
                case "PROFESSOR_FOREIGN":
                    values.institution = self.$('form input[name=institution]').val();
                    values.hasOnlineProfile = self.$('form input[name=hasOnlineProfile]').is(':not(:checked)');
                    values.profileURL = self.$('form input[name=profileURL]').val();
                    values.rank = {
                        "id": self.$('form select[name=rank]').val()
                    };
                    values.country = {
                        "code": self.$('form select[name=country]').val()
                    };
                    values.subject = {
                        "id": self.model.has("subject") ? self.model.get("subject").id : undefined,
                        "name": self.$('form textarea[name=subject]').val()
                    };
                    values.speakingGreek = self.$('form select[name=speakingGreek]').val();
                    values.hasAcceptedTerms = self.$('form input[name=hasAcceptedTerms]').is(':checked');
                    break;
                case "INSTITUTION_MANAGER":
                    values.institution = {
                        "id": self.$('form select[name=institution]').val()
                    };
                    values.verificationAuthority = self.$('form select[name=verificationAuthority]').val();
                    values.verificationAuthorityName = self.$('form input[name=verificationAuthorityName]').val();
                    values.alternateBasicInfo = {
                        "firstname": self.$('form input[name=alternatefirstname]').val(),
                        "lastname": self.$('form input[name=alternatelastname]').val(),
                        "fathername": self.$('form input[name=alternatefathername]').val()
                    };
                    values.alternateBasicInfoLatin = {
                        "firstname": self.$('form input[name=alternatefirstnamelatin]').val(),
                        "lastname": self.$('form input[name=alternatelastnamelatin]').val(),
                        "fathername": self.$('form input[name=alternatefathernamelatin]').val()
                    };
                    values.alternateContactInfo = {
                        "email": self.$('form input[name=alternateemail]').val(),
                        "mobile": self.$('form input[name=alternatemobile]').val(),
                        "phone": self.$('form input[name=alternatephone]').val()
                    };
                    break;
                case "INSTITUTION_ASSISTANT":
                    values.institution = {
                        "id": self.$('form select[name=institution]').val()
                    };
                    break;

                case "MINISTRY_MANAGER":
                    values.ministry = self.$('form input[name=ministry]').val();
                    break;
                case "MINISTRY_ASSISTANT":
                    break;
                default:
                    break;
            }

            // Check if candidate
            candidate = self.collection.find(function (role) {
                return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
            });
            if (candidate) {
                // Check for open candidacies
                openCandidacies = new Models.CandidateCandidacies({}, {
                    candidate: candidate.get("id")
                });
                openCandidacies.fetch({
                    data: {
                        "open": "true"
                    },
                    cache: false,
                    success: function (collection) {
                        var candidacyUpdateConfirmView;
                        if (collection.length > 0) {
                            // Found Open Candidacies, show CandidacyUpdateConfirmView
                            if (App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                                // Changes by helpdesk always update candidacies
                                doSave(values, true);
                            } else {
                                // Ask User if he wants to update candidacies
                                candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
                                    "collection": collection,
                                    "answer": function (confirm) {
                                        doSave(values, confirm);
                                    }
                                });
                                candidacyUpdateConfirmView.show();
                            }
                        } else {
                            // Did not find Open Candidacies
                            doSave(values);
                        }
                    }
                });
            } else {
                // Did not have candidate profile
                doSave(values);
            }
            event.preventDefault();
            return false;
        },

        cancel: function () {
            var self = this;
            if (self.validator) {
                self.validator.resetForm();
            }
            self.render();
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        status: function (event) {
            var self = this;
            self.model.status({
                "status": $(event.currentTarget).attr('status')
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * AdminRoleEditView *******************************************************
     **************************************************************************/
    Views.AdminRoleEditView = Views.RoleEditView.extend({
        initialize: function (options) {
            this._super('initialize', [options]);
        },

        isEditable: function (field) {
            var self = this;
            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    switch (field) {
                        case "tautotitaFile":
                            return true;
                        case "formaSymmetoxisFile":
                            return true;
                        case "status":
                            return true;
                        default:
                            break;
                    }
                    break;
                case "PROFESSOR_DOMESTIC":
                    switch (field) {
                        // All editable, these two fields need special treatment since they are mutually exclusive
                        case "fekSubject":
                            return (_.isObject(self.model.get("fekSubject")) || (_.isUndefined(self.model.get("fekSubject")) && _.isUndefined(self.model.get("subject"))));  // subject is not defined
                        case "subject":
                            return (!_.isUndefined(self.model.get("fekSubject"))) && !self.isEditable("fekSubject"); // fekSubject is not defined
                        default:
                            break;
                    }
                    return true;
                case "PROFESSOR_FOREIGN":
                    return true;
                case "INSTITUTION_MANAGER":
                    switch (field) {
                        case "alternateemail":
                        case "alternatemobile":
                        case "alternatephone":
                            return false;
                        default:
                            return true;
                    }
                    break;
                case "INSTITUTION_ASSISTANT":
                    return false;
                case "MINISTRY_ASSISTANT":
                    return false;
                default:
                    switch (field) {
                        case "status":
                            return true;
                        default:
                            break;
                    }
                    break;
            }
            return false;
        },

        validationRules: function () {
            var self = this;
            switch (self.model.get("discriminator")) {
                case "CANDIDATE":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {},
                        messages: {}
                    };
                case "PROFESSOR_DOMESTIC":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            profileURL: {
                                url: true
                            }
                        },
                        messages: {
                            profileURL: {
                                required: $.i18n.prop('validation_required')
                            }
                        }
                    };
                case "PROFESSOR_FOREIGN":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            profileURL: {
                                url: true
                            }
                        },
                        messages: {
                            profileURL: $.i18n.prop('validation_profileURL')
                        }
                    };
                case "INSTITUTION_MANAGER":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {
                            alternatefirstnamelatin: {
                                onlyLatin: true
                            },
                            alternatelastnamelatin: {
                                onlyLatin: true
                            },
                            alternatefathernamelatin: {
                                onlyLatin: true
                            },
                            alternateemail: {
                                email: true,
                                minlength: 2
                            },
                            alternatemobile: {
                                number: true,
                                minlength: 10
                            },
                            alternatephone: {
                                number: true,
                                minlength: 10
                            }
                        },
                        messages: {
                            alternatefirstnamelatin: {
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatelastnamelatin: {
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatefathernamelatin: {
                                onlyLatin: $.i18n.prop('validation_latin')
                            },
                            alternatemobile: {
                                number: $.i18n.prop('validation_number'),
                                minlength: $.i18n.prop('validation_minlength', 10)
                            },
                            alternatephone: {
                                number: $.i18n.prop('validation_number'),
                                minlength: $.i18n.prop('validation_minlength', 10)
                            },
                            alternateemail: {
                                email: $.i18n.prop('validation_email'),
                                minlength: $.i18n.prop('validation_minlength', 2)
                            }
                        }
                    };
                case "INSTITUTION_ASSISTANT":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {},
                        messages: {}
                    };
                case "MINISTRY_MANAGER":
                    return {
                        errorElement: "span",
                        errorClass: "help-inline",
                        highlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").addClass("error");
                        },
                        unhighlight: function (element) {
                            $(element).parent(".controls").parent(".control-group").removeClass("error");
                        },
                        rules: {},
                        messages: {}
                    };
                case "MINISTRY_ASSISTANT":
                    return {};
                default:
                    return {};
            }
        },

        render: function (eventName) {
            var self = this;
            self._super('render', [eventName]);
            if (self.isEditable("status")) {
                self.$("a#status").removeClass("disabled");
            }
            self.$("span#accounthelpdesk").hide();
            return self;
        }
    });

    /***************************************************************************
     * FileView ****************************************************************
     **************************************************************************/
    Views.FileView = Views.BaseView.extend({
        tagName: "div",

        className: "",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_file);
            this.model.bind('change', this.render, this);
        },

        events: {},

        render: function () {
            var self = this;
            self.closeInnerViews();
            if (self.model.isNew()) {
                self.$el.empty();
                self.addTitle();
                self.$el.append("-");
            } else {
                var tpl_data = {
                    withMetadata: self.options.withMetadata,
                    file: self.model.toJSON()
                };
                if (_.isObject(tpl_data.file.currentBody)) {
                    tpl_data.file.currentBody.url = self.model.url() + "/body/" + tpl_data.file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
                }
                self.$el.empty();
                self.addTitle();
                self.$el.append(self.template(tpl_data));
            }

            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.$el.unbind();
            this.$el.empty();
        }
    });

    /***************************************************************************
     * FileEditView ************************************************************
     **************************************************************************/
    Views.FileEditView = Views.BaseView.extend({

        uploader: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "deleteFile", "toggleUpload");
            this.template = _.template(tpl_file_edit);
            this.model.bind('change', this.render, this);

            this.$input = $(this.el);
            this.$input.before("<div id=\"" + this.$input.attr("name") + "\"></div>");
            this.setElement(this.$input.prev("#" + this.$input.attr("name")));
        },

        events: {
            "click a#delete": "deleteFile",
            "click a#toggleUpload": "toggleUpload"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                editable: self.options.editable,
                withMetadata: self.options.withMetadata,
                file: self.model.toJSON()
            };
            if (_.isObject(tpl_data.file.currentBody)) {
                tpl_data.file.currentBody.url = self.model.url() + "/body/" + tpl_data.file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
            }
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            self.$input.focus().val(self.model.get("id"));

            self.$('#uploader div.progress').hide();
            // Initialize FileUpload Modal
            self.$("#uploader").modal({
                show: false
            });
            self.$("#uploader").on('hidden', function () {
                if (self.model.changed) {
                    self.model.trigger("change");
                }
            });
            self.$("#uploader a#close").on("click", function () {
                self.$("#uploader").modal('hide');
            });
            // Initialize FileUpload widget
            self.$('#uploader input[name=file]').fileupload({
                dataType: 'json',
                url: self.model.url() + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
                replaceFileInput: false,
                forceIframeTransport: true,
                multipart: true,
                maxFileSize: 30000000,
                add: function (e, data) {
                    function upload() {
                        data.formData = _.extend({
                            "type": self.$("#uploader input[name=file_type]").val(),
                            "name": self.$("#uploader input[name=file_name]").val(),
                            "description": self.$("#uploader textarea[name=file_description]").val()
                        }, self.$input.data());

                        if (_.isFunction(self.options.beforeUpload)) {
                            self.options.beforeUpload(data, function (data) {
                                self.$('#uploader div.progress').show();
                                self.$("#uploader a#upload").unbind("click");
                                data.submit();
                            });
                        } else {
                            self.$('#uploader div.progress').show();
                            self.$("a#upload").unbind("click");
                            data.submit();
                        }
                    }

                    self.$("a#upload").unbind("click");
                    self.$("a#upload").bind("click", upload);
                },
                progressall: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    self.$('#uploader div.progress .bar').css('width', progress + '%');
                },
                done: function (e, data) {
                    self.$('div.progress').fadeOut('slow', function () {
                        self.$('#uploader div.progress .bar').css('width', '0%');
                        self.$("#uploader").modal("hide");
                        if (!!data.result.error) {
                            new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + data.result.error)
                            }).show();
                        } else {
                            self.model.set(data.result, {
                                silent: true
                            });
                        }
                    });

                },
                fail: function (e, data) {
                    // Won't happen due to forceIframeTransport and response not being empty
                }
            });
            return self;
        },

        toggleUpload: function () {
            var self = this;
            self.$("#uploader").modal("toggle");
        },

        deleteFile: function () {
            var self = this;
            var doDelete = function (options) {
                var tmp;
                options = _.extend({}, options);
                tmp = {
                    type: self.model.get("type"),
                    url: self.model.url,
                    urlRoot: self.model.urlRoot
                };
                self.model.destroy({
                    url: self.model.url() + (options.updateCandidacies ? "?updateCandidacies=true" : ""),
                    wait: true,
                    success: function () {
                        var popup;
                        // Reset Object to empty (without id) status
                        self.model.urlRoot = tmp.urlRoot;
                        self.model.url = tmp.url;
                        self.model.set(_.extend(self.model.defaults, {
                            "type": tmp.type
                        }), {
                            silent: false
                        });
                        popup = new Views.PopupView({
                            type: "success",
                            message: $.i18n.prop("Success")
                        });
                        popup.show();
                    },
                    error: function (model, resp) {
                        var popup = new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                        });
                        popup.show();
                    }
                });
            };
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    if (_.isFunction(self.options.beforeDelete)) {
                        self.options.beforeDelete(self.model, doDelete);
                    } else {
                        doDelete();
                    }
                }
            });
            confirm.show();
        },

        close: function () {
            this.closeInnerViews();
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * FileListView ************************************************************
     **************************************************************************/
    Views.FileListView = Views.BaseView.extend({
        tagName: "div",

        className: "",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_file_list);
            this.collection.bind('reset', this.render, this);
        },

        events: {
            "mouseover table tr": function (event) {
                $(event.currentTarget).find("span.badge").addClass("label-inverse");
            },
            "mouseout table tr": function (event) {
                $(event.currentTarget).find("span.badge").removeClass("label-inverse");
            }
        },

        render: function () {
            var self = this;
            var tpl_data = {
                type: self.collection.type,
                withMetadata: self.options.withMetadata,
                files: []
            };
            self.collection.each(function (model) {
                var file = model.toJSON();
                if (_.isObject(file.currentBody)) {
                    file.currentBody.url = model.url() + "/body/" + file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
                }
                tpl_data.files.push(file);
            });
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "bPaginate": false,
                    "bFilter": false,
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.$el.unbind();
            this.$el.empty();
        }
    });

    /***************************************************************************
     * FileListEditView ********************************************************
     **************************************************************************/
    Views.FileListEditView = Views.BaseView.extend({
        tagName: "div",

        className: "",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "toggleUpload", "deleteFile");
            this.template = _.template(tpl_file_list_edit);
            this.collection.bind('reset', this.render, this);
            this.collection.bind('remove', this.render, this);
            this.collection.bind('add', this.render, this);

            this.$input = $(this.el);
            this.$input.before("<div id=\"" + this.$input.attr("name") + "\"></div>");
            this.setElement(this.$input.prev("#" + this.$input.attr("name")));
        },

        events: {
            "click a#delete": "deleteFile",
            "click a#toggleUpload": "toggleUpload",
            "mouseover table tr": function (event) {
                $(event.currentTarget).find("span.badge").addClass("label-inverse");
            },
            "mouseout table tr": function (event) {
                $(event.currentTarget).find("span.badge").removeClass("label-inverse");
            }
        },

        render: function () {
            var self = this;
            var tpl_data = {
                editable: self.options.editable,
                type: self.collection.type,
                withMetadata: self.options.withMetadata,
                files: []
            };
            self.collection.each(function (model) {
                var file = model.toJSON();
                if (_.isObject(file.currentBody)) {
                    file.currentBody.url = model.url() + "/body/" + file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
                }
                tpl_data.files.push(file);
            });
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();

            self.$el.append(self.template(tpl_data));
            self.$input.val(self.collection.length);

            // Add DataTables for sorting:
            if (!$.fn.DataTable.fnIsDataTable(self.$("table.file-table-edit"))) {
                self.$("table.file-table-edit").dataTable({
                    "bPaginate": false,
                    "bFilter": false,
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            // Initialize FileUpload Modal
            self.$("#uploader").modal({
                show: false
            });
            self.$('#uploader div.progress').hide();
            self.$("#uploader").on('hidden', function () {
                self.collection.trigger("reset");
            });
            self.$("#uploader a#close").on("click", function () {
                self.$("#uploader").modal('hide');
            });
            // Initialize FileUpload widget
            self.$('#uploader input[name=file]').fileupload({
                dataType: 'json',
                url: self.collection.url + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
                replaceFileInput: false,
                forceIframeTransport: true,
                maxFileSize: 30000000,
                add: function (e, data) {
                    function upload() {
                        data.formData = _.extend({
                            "type": self.$("#uploader input[name=file_type]").val(),
                            "name": self.$("#uploader input[name=file_name]").val(),
                            "description": self.$("#uploader textarea[name=file_description]").val()
                        }, self.$input.data());
                        if (_.isFunction(self.options.beforeUpload)) {
                            self.options.beforeUpload(data, function () {
                                self.$('#uploader div.progress').show();
                                self.$("#uploader a#upload").unbind("click");
                                data.submit();
                            });
                        } else {
                            self.$('#uploader div.progress').show();
                            self.$("a#upload").unbind("click");
                            data.submit();
                        }
                    }

                    self.$("a#upload").unbind("click");
                    self.$("a#upload").bind("click", upload);
                },
                progressall: function (e, data) {
                    var progress = parseInt(data.loaded / data.total * 100, 10);
                    self.$('#uploader div.progress .bar').css('width', progress + '%');
                },
                done: function (e, data) {
                    self.$('div.progress').fadeOut('slow', function () {
                        var newFile;
                        self.$('#uploader div.progress .bar').css('width', '0%');
                        self.$("#uploader").modal("hide");
                        if (!!data.result.error) {
                            new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + data.result.error)
                            }).show();
                        } else {
                            newFile = new Models.File(data.result);
                            newFile.urlRoot = self.collection.url;
                            self.collection.add(newFile, {
                                silent: true
                            });
                        }
                    });

                },
                fail: function (e, data) {
                    // Won't happen due to forceIframeTransport and response not being empty
                }
            });

            return self;
        },

        toggleUpload: function () {
            var self = this;
            self.$("#uploader").modal("toggle");
        },

        deleteFile: function (event) {
            var self = this;
            var selectedModel = self.collection.get($(event.currentTarget).data('fileId'));
            var doDelete = function (options) {
                options = _.extend({}, options);
                selectedModel.destroy({
                    url: selectedModel.url() + (options.updateCandidacies ? "?updateCandidacies=true" : ""),
                    wait: true,
                    success: function () {
                        var popup;
                        popup = new Views.PopupView({
                            type: "success",
                            message: $.i18n.prop("Success")
                        });
                        popup.show();
                    },
                    error: function (model, resp) {
                        var popup = new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                        });
                        popup.show();
                    }
                });
            };
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    if (_.isFunction(self.options.beforeDelete)) {
                        self.options.beforeDelete(self.model, doDelete);
                    } else {
                        doDelete();
                    }
                }
            });
            confirm.show();
        },

        close: function () {
            this.closeInnerViews();
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * AdministratorListView ***************************************************
     **************************************************************************/
    Views.AdministratorListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_list);
            this.roleInfoTemplate = _.template(tpl_user_role_info);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
        },

        events: {
            "click a#select": "select",
            "click a#createAdministrator": "createAdministrator"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                users: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.roleInfo = self.roleInfoTemplate({
                                roles: item.roles
                            });
                            result.push(item);
                        }
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            // Add Actions:
            self.$("#actions").html('<div class="btn-group input-append">' +
            '<a id="createAdministrator" class="btn btn-small add-on"><i class="icon-plus"></i> ' + $.i18n.prop('btn_create') + ' </a>' +
            '</div>');
            return self;
        },

        select: function (event) {
            var selectedModel = this.collection.get($(event.currentTarget).attr('user'));
            this.collection.trigger("user:selected", selectedModel);
        },

        createAdministrator: function () {
            var user = new Models.User({
                "authenticationType": "USERNAME",
                "roles": [
                    {
                        "discriminator": "ADMINISTRATOR"
                    }
                ]
            });
            this.collection.add(user);
            this.collection.trigger("user:selected", user);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind("change", this.render, this);
            this.collection.unbind("reset", this.render, this);
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * AssistantListView *******************************************************
     **************************************************************************/
    Views.InstitutionAssistantListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_list);
            this.roleInfoTemplate = _.template(tpl_user_role_info);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
        },

        events: {
            "click a#select": "select",
            "click a#createInstitutionAssistant": "createInstitutionAssistant"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                users: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.roleInfo = self.roleInfoTemplate({
                                roles: item.roles
                            });
                            result.push(item);
                        }
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            // Add Actions:
            self.$("#actions").html("<div class=\"btn-group input-append\"><a id=\"createInstitutionAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_ia') + " </a></div><div class=\"btn-group input-append\"></div>");
            return self;
        },

        select: function (event) {
            var selectedModel = this.collection.get($(event.currentTarget).attr('user'));
            this.collection.trigger("user:selected", selectedModel);
        },

        createInstitutionAssistant: function () {
            var institutions = App.loggedOnUser.getAssociatedInstitutions();
            var user = new Models.User({
                "authenticationType": "USERNAME",
                "roles": [
                    {
                        "discriminator": "INSTITUTION_ASSISTANT",
                        "institution": institutions[0]
                    }
                ]
            });
            this.collection.add(user);
            this.collection.trigger("user:selected", user);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind("change", this.render, this);
            this.collection.unbind("reset", this.render, this);
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * AssistantListView *******************************************************
     **************************************************************************/
    Views.MinistryAssistantListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_user_list);
            this.roleInfoTemplate = _.template(tpl_user_role_info);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
        },

        events: {
            "click a#select": "select",
            "click a#createMinistryAssistant": "createMinistryAssistant"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                users: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.roleInfo = self.roleInfoTemplate({
                                roles: item.roles
                            });
                            result.push(item);
                        }
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            // Add Actions:
            self.$("#actions").html("<div class=\"btn-group input-append\"><a id=\"createMinistryAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_ma') + " </a></div><div class=\"btn-group input-append\"></div>");
            return self;
        },

        select: function (event) {
            var selectedModel = this.collection.get($(event.currentTarget).attr('user'));
            this.collection.trigger("user:selected", selectedModel);
        },

        createMinistryAssistant: function () {
            var user = new Models.User({
                "authenticationType": "USERNAME",
                "roles": [
                    {
                        "discriminator": "MINISTRY_ASSISTANT"
                    }
                ]
            });
            this.collection.add(user);
            this.collection.trigger("user:selected", user);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind("change", this.render, this);
            this.collection.unbind("reset", this.render, this);
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * AdministratorAccountView ************************************************
     **************************************************************************/
    Views.AdministratorAccountView = Views.AccountView.extend({
        initialize: function (options) {
            this._super('initialize', [options]);
        },

        applyRules: function () {
            var self = this;
            // Actions:
            if (self.model.isNew()) {
                self.$("a#status").addClass("disabled");
            } else {
                self.$("a#status").removeClass("disabled");
            }
            self.$("select,input,textarea").removeAttr("disabled");
            self.$("a#save").show();
            self.$("a#remove").show();
        },

        render: function (eventName) {
            var self = this;
            self._super('render', [eventName]);
            self.$("span#accounthelpdesk").hide();
            return self;
        }
    });

    /***************************************************************************
     * AssistantAccountView ****************************************************
     **************************************************************************/
    Views.AssistantAccountView = Views.AccountView.extend({
        initialize: function (options) {
            this._super('initialize', [options]);
        },

        applyRules: function () {
            var self = this;
            // Actions:
            if (self.model.isNew()) {
                self.$("a#status").addClass("disabled");
                self.$("select,input,textarea").removeAttr("disabled");
            } else {
                self.$("a#status").removeClass("disabled");
                self.$("select,input,textarea").attr("disabled", true);
                self.$("input[name=username]").removeAttr("disabled");
                self.$("input[name=firstname]").removeAttr("disabled");
                self.$("input[name=lastname]").removeAttr("disabled");
                self.$("input[name=fathername]").removeAttr("disabled");
                self.$("input[name=firstnamelatin]").removeAttr("disabled");
                self.$("input[name=lastnamelatin]").removeAttr("disabled");
                self.$("input[name=fathernamelatin]").removeAttr("disabled");
                self.$("input[name=identification]").removeAttr("disabled");
                self.$("input[name=email]").removeAttr("disabled");
            }
            self.$("a#save").show();
            self.$("a#remove").hide();
        },

        render: function (eventName) {
            var self = this;
            self._super('render', [eventName]);
            self.$("span#accounthelpdesk").hide();
            return self;
        }
    });

    /***************************************************************************
     * PositionListView ********************************************************
     **************************************************************************/
    Views.PositionListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "renderActions", "selectPosition", "createPosition");
            this.template = _.template(tpl_position_list);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
        },

        events: {
            "click a#createPosition": "createPosition",
            "click a#selectPosition": "selectPosition"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                positions: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.canEdit = model.isEditableBy(App.loggedOnUser);
                            result.push(item);
                        }
                    });
                    return result;
                }()),
                exportUrl: self.collection.url + "/export?X-Auth-Token=" + encodeURIComponent(App.authToken)
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(tpl_data));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
                self.$("table thead input").keyup(function () {
                    self.$("table").dataTable().fnFilter(this.value, self.$("table thead input").index(this));
                });
            }
            // Actions
            self.renderActions();
            return self;
        },

        renderActions: function () {
            var self = this;
            if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
                return;
            }
            self.$("#actions").html('<select class="input-xxlarge pull-left" name="department"></select>');
            self.$("#actions").append('<a id="createPosition" class="btn"><i class="icon-plus"></i> ' + $.i18n.prop('btn_create_position') + '</a>');

            // Departments
            App.departments = App.departments || new Models.Departments();
            App.departments.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    _.each(self.$selectize, function (element) {
                        element.selectize.destroy();
                    });
                    self.$selectize = self.$("select[name=department]").selectize({
                        valueField: 'id',
                        diacritics: true,
                        create: false,
                        hideSelected: true,
                        sortField: 'lname',
                        searchField: ['lname'],
                        options: _.map(collection.filter(function (department) {
                            return App.loggedOnUser.isAssociatedWithDepartment(department);
                        }), function (department) {
                            return _.extend(department.toJSON(), {
                                'lname': department.get('name')[App.locale]
                            });
                        }),
                        render: {
                            item: function (item) { // Shows when
                                // selected
                                return _.templates.department(item);
                            },
                            option: function (item) { // Shows in
                                // dropddown
                                return _.templates.department(item);
                            }
                        }
                    });
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        selectPosition: function (event, position) {
            var self = this;
            var selectedModel = position || self.collection.get($(event.currentTarget).attr('data-position-cid'));
            if (selectedModel) {
                self.collection.trigger("position:selected", selectedModel);
            }
        },

        createPosition: function () {
            var self = this;
            var newPosition;
            // Validate:
            var departmentId = self.$("select[name='department']").val();
            if (!departmentId || _.isEqual(departmentId, "-1")) {
                return;
            }
            // Create:
            newPosition = new Models.Position();
            newPosition.save({
                department: {
                    id: departmentId
                }
            }, {
                wait: true,
                success: function () {
                    self.collection.add(newPosition);
                    self.selectPosition(undefined, newPosition);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionView ************************************************************
     **************************************************************************/
    Views.PositionView = Views.BaseView.extend({
        tagName: "div",

        id: "positionview",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "renderCandidacies", "renderCommittee", "renderEvaluation", "renderNomination", "renderComplementaryDocuments");
            this.template = _.template(tpl_position);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);

        },

        events: {},

        render: function () {
            var self = this;
            var tpl_data = self.model.toJSON();
            // Remove CreatedBy if loggedOn===owner of Position
            if (self.model.get("createdBy").id === App.loggedOnUser.get("id")) {
                tpl_data.createdBy.id = undefined;
            }

            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            // Dependencies:
            self.renderCandidacies(self.$("#positionCandidacies"));
            self.renderCommittee(self.$("#positionCommittee"));
            self.renderEvaluation(self.$("#positionEvaluation"));
            self.renderNomination(self.$("#positionNomination"));
            self.renderComplementaryDocuments(self.$("#positionComplementaryDocuments"));
            // End of associations
            return self;
        },

        renderCandidacies: function ($el) {
            var self = this;
            var positionCandidaciesView;
            var positionCandidacies = new Models.PositionCandidacies({
                id: self.model.get("phase").candidacies.id,
                position: {
                    id: self.model.get("id")
                }
            });
            positionCandidaciesView = new Views.PositionCandidaciesView({
                position: self.model,
                model: positionCandidacies
            });
            $el.html(positionCandidaciesView.el);
            positionCandidacies.fetch({
                cache: false
            });
            self.innerViews.push(positionCandidaciesView);
        },

        renderCommittee: function ($el) {
            var self = this;
            var positionCommittee;
            var positionCommitteeView;
            if (!self.model.get("phase").committee) {
                return;
            }
            positionCommittee = new Models.PositionCommittee({
                id: self.model.get("phase").committee.id,
                position: {
                    id: self.model.get("id")
                }
            });
            positionCommitteeView = new Views.PositionCommitteeView({
                model: positionCommittee
            });
            $el.html(positionCommitteeView.el);
            positionCommittee.fetch({
                cache: false
            });
            self.innerViews.push(positionCommitteeView);
        },

        renderEvaluation: function ($el) {
            var self = this;
            var positionEvaluation;
            var positionEvaluationView;
            if (!self.model.get("phase").evaluation) {
                return;
            }
            positionEvaluation = new Models.PositionEvaluation({
                id: self.model.get("phase").evaluation.id,
                position: {
                    id: self.model.get("id")
                }
            });
            positionEvaluationView = new Views.PositionEvaluationView({
                model: positionEvaluation
            });
            $el.html(positionEvaluationView.el);
            positionEvaluation.fetch({
                cache: false
            });
            self.innerViews.push(positionEvaluationView);
        },

        renderNomination: function ($el) {
            var self = this;
            var positionNomination;
            var positionNominationView;
            if (!self.model.get("phase").nomination) {
                return;
            }
            positionNomination = new Models.PositionNomination({
                id: self.model.get("phase").nomination.id,
                position: {
                    id: self.model.get("id")
                }
            });
            positionNominationView = new Views.PositionNominationView({
                model: positionNomination
            });
            $el.html(positionNominationView.el);
            positionNomination.fetch({
                cache: false
            });
            self.innerViews.push(positionNominationView);
        },

        renderComplementaryDocuments: function ($el) {
            var self = this;
            var positionComplementaryDocuments;
            var positionComplementaryDocumentsView;

            if (!self.model.get("phase").complementaryDocuments) {
                return;
            }
            positionComplementaryDocuments = new Models.PositionComplementaryDocuments({
                id: self.model.get("phase").complementaryDocuments.id,
                position: {
                    id: self.model.get("id")
                }
            });
            positionComplementaryDocumentsView = new Views.PositionComplementaryDocumentsView({
                model: positionComplementaryDocuments
            });
            $el.html(positionComplementaryDocumentsView.el);
            positionComplementaryDocuments.fetch({
                cache: false
            });
            self.innerViews.push(positionComplementaryDocumentsView);
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionEditView ********************************************************
     **************************************************************************/
    Views.PositionEditView = Views.BaseView.extend({
        tagName: "div",

        id: "positionEditView",

        tab: "main",

        phases: {
            "ENTAGMENI": ["ANOIXTI"],
            "ANOIXTI": ["CANCELLED"],
            "KLEISTI": ["EPILOGI"],
            "EPILOGI": ["ANAPOMPI", "STELEXOMENI", "CANCELLED"],
            "ANAPOMPI": ["EPILOGI"],
            "STELEXOMENI": ["CANCELLED"],
            "CANCELLED": ["EPILOGI"]
        },

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "addPhase", "showTab", "showMainTab", "showCandidaciesTab", "showCommitteeTab", "showEvaluationTab", "showNominationTab",
                "showComplementaryDocumentsTab");
            this.template = _.template(tpl_position_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {
            "click a#selectTab": "showTab",
            "click a#addPhase": "addPhase"
        },

        render: function () {
            var self = this;
            var positionStatus = self.model.get("phase").clientStatus;
            var previousStatus = self.model.get("phasesMap")[Math.max(_.size(self.model.get("phasesMap")) - 2, 0)];
            var tpl_data = self.model.toJSON();

            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            // Phase:
            self.$("a#addPhase").each(function () {
                var linkStatus = $(this).data("phaseStatus");
                if (positionStatus === 'CANCELLED' && previousStatus === 'ANOIXTI') {
                    //1. Check the special ANOIXTI->CANCELLED->nowhere case
                    $(this).hide();
                } else {
                    //2. Check the transitions map
                    if (!_.any(self.phases[positionStatus], function (nextStatus) {
                            return _.isEqual(linkStatus, nextStatus);
                        })) {
                        $(this).hide();
                    }
                    ;
                }
            });
            // Tabs:
            if (_.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI")) {
                self.$("#positionTabs a[data-target=committee]").parent("li").addClass("disabled");
                self.$("#positionTabs a[data-target=evaluation]").parent("li").addClass("disabled");
                self.$("#positionTabs a[data-target=nomination]").parent("li").addClass("disabled");
            }

            // Show Tab:
            setTimeout(function () {
                self.showTab(undefined, self.options.tab);
            }, 0);

            return self;
        },

        showTab: function (event, target) {
            var self = this;
            target = target || $(event.currentTarget).data('target');
            if (event && self.$("#positionTabs a[data-target=" + target + "]").parent("li").hasClass("disabled")) {
                event.preventDefault();
                return;
            }
            // Clear inner views
            self.closeInnerViews();
            // Update tab display
            self.$("#positionTabs a#selectTab").parent("li").removeClass("active");
            self.$("#positionTabs a#selectTab[data-target=" + target + "]").parent("li").addClass("active");
            App.router.navigate("positions/" + self.model.get("id") + "/" + target, {
                trigger: false
            });
            // Add inner view
            switch (target) {
                case "main":
                    self.showMainTab($("#positionTabContent"));
                    break;
                case "candidacies":
                    self.showCandidaciesTab($("#positionTabContent"));
                    break;
                case "committee":
                    self.showCommitteeTab($("#positionTabContent"));
                    break;
                case "evaluation":
                    self.showEvaluationTab($("#positionTabContent"));
                    break;
                case "nomination":
                    self.showNominationTab($("#positionTabContent"));
                    break;
                case "complementaryDocuments":
                    self.showComplementaryDocumentsTab($("#positionTabContent"));
                    break;
                default:
                    self.showMainTab($("#positionTabContent"));
                    break;
            }
        },

        showMainTab: function ($el) {
            var self = this;
            var positionMainEditView;
            $el.html("Main");
            positionMainEditView = new Views.PositionMainEditView({
                model: self.model
            });
            $el.html(positionMainEditView.el);
            positionMainEditView.render();
            self.innerViews.push(positionMainEditView);
        },

        showCandidaciesTab: function ($el) {
            var self = this;
            var positionCandidacies = new Models.PositionCandidacies({
                id: self.model.get("phase").candidacies.id,
                position: {
                    id: self.model.get("id")
                }
            });
            var positionCandidaciesEditView = new Views.PositionCandidaciesEditView({
                model: positionCandidacies
            });
            positionCandidacies.fetch({
                cache: false,
                success: function () {
                    $el.html(positionCandidaciesEditView.render().el);
                }
            });
            self.innerViews.push(positionCandidaciesEditView);
        },

        showCommitteeTab: function ($el) {
            var self = this;
            var positionCommittee = new Models.PositionCommittee({
                id: self.model.get("phase").committee.id,
                position: {
                    id: self.model.get("id")
                }
            });
            var positionCommitteeEditView = new Views.PositionCommitteeEditView({
                model: positionCommittee
            });
            positionCommittee.fetch({
                cache: false,
                success: function () {
                    $el.html(positionCommitteeEditView.render().el);
                }
            });

            self.innerViews.push(positionCommitteeEditView);
        },

        showEvaluationTab: function ($el) {
            var self = this;
            var positionEvaluation = new Models.PositionEvaluation({
                id: self.model.get("phase").evaluation.id,
                position: {
                    id: self.model.get("id")
                }
            });
            var positionEvaluationEditView = new Views.PositionEvaluationEditView({
                model: positionEvaluation
            });
            positionEvaluation.fetch({
                cache: false,
                success: function () {
                    $el.html(positionEvaluationEditView.render().el);
                }
            });

            self.innerViews.push(positionEvaluationEditView);
        },

        showNominationTab: function ($el) {
            var self = this;
            var positionNomination = new Models.PositionNomination({
                id: self.model.get("phase").nomination.id,
                position: {
                    id: self.model.get("id")
                }
            });
            var positionNominationEditView = new Views.PositionNominationEditView({
                model: positionNomination
            });
            positionNomination.fetch({
                cache: false,
                success: function () {
                    $el.html(positionNominationEditView.render().el);
                }
            });

            self.innerViews.push(positionNominationEditView);
        },

        showComplementaryDocumentsTab: function ($el) {
            var self = this;
            var positionComplementaryDocuments = new Models.PositionComplementaryDocuments({
                id: self.model.get("phase").complementaryDocuments.id,
                position: {
                    id: self.model.get("id")
                }
            });
            var positionComplementaryDocumentsEditView = new Views.PositionComplementaryDocumentsEditView({
                model: positionComplementaryDocuments
            });
            positionComplementaryDocuments.fetch({
                cache: false,
                success: function () {
                    $el.html(positionComplementaryDocumentsEditView.render().el);
                }
            });

            self.innerViews.push(positionComplementaryDocumentsEditView);
        },

        addPhase: function (event) {
            var self = this;
            var newStatus = $(event.currentTarget).data('phaseStatus');
            self.model.phase({
                "phase": {
                    "status": newStatus
                }
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    Views.PositionMainEditView = Views.BaseView.extend({
        tagName: "div",

        id: "positionmaineditview",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "change", "isEditable", "submit", "cancel");
            this.template = _.template(tpl_position_main_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);

            App.sectors = App.sectors || new Models.Sectors();
            this.assistants = new Models.Users();
            this.assistants.url = this.model.url() + "/assistants";
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#cancel": "cancel",
            "click a#remove": "remove",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        isEditable: function (field) {
            var self = this;
            if (_.isEqual(self.model.get("phase").status, "ANAPOMPI") || _.isEqual(self.model.get("phase").status, "STELEXOMENI")) {
                return false;
            }
            switch (field) {
                // Fields
                case "name":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "department":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "description":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "subject":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "area":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "sector":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "fek":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "fekSentDate":
                    return _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
                case "openingDate":
                    return App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE") || !self.model.get("permanent");
                case "closingDate":
                    return App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE") || !self.model.get("permanent");
                case "assistant":
                    return App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE") || !self.model.get("permanent");
                case "assistants":
                    return App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE") || !self.model.get("permanent");
                default:
                    break;
            }
            return false;
        },

        render: function () {
            var self = this;
            var propName;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Sector options
            self.$("select[name='sector']").change(function () {
                self.$("select[name='sector']").next(".help-block").html(self.$("select[name='area'] option:selected").text() + " / " + self.$("select[name='sector'] option:selected").text());
            });
            self.$("select[name='area']").change(function () {
                var selectedAreaId;
                self.$("select[name='sector']").empty();
                selectedAreaId = self.$("select[name='area']").val();
                self.$("select[name='sector']").append("<option value=''>--</option>");
                App.sectors.filter(function (sector) {
                    return sector.get('areaId') === +selectedAreaId;
                }).forEach(function (sector) {
                    if (_.isObject(self.model.get("sector")) && _.isEqual(self.model.get("sector").id, sector.get("id"))) {
                        self.$("select[name='sector']").append("<option value='" + sector.get("id") + "' selected>" + sector.get("name")[App.locale].subject + "</option>");
                    } else {
                        self.$("select[name='sector']").append("<option value='" + sector.get("id") + "'>" + sector.get("name")[App.locale].subject + "</option>");
                    }
                });
                self.$("select[name='sector']").trigger("change", {
                    triggeredBy: "application"
                });
            });
            App.sectors.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    var areas = collection.map(function (sector) {
                        return {
                            areaId: sector.get("areaId"),
                            name: sector.get("name")[App.locale].area
                        };
                    });
                    areas = _.uniq(areas, function (area) {
                        return area.areaId;
                    });
                    self.$("select[name='area']").append("<option value=''>--</option>");
                    _.each(areas, function (area) {
                        if (_.isObject(self.model.get("sector")) && _.isEqual(self.model.get("sector").areaId, area.areaId)) {
                            self.$("select[name='area']").append("<option value='" + area.areaId + "' selected>" + area.name + "</option>");
                        } else {
                            self.$("select[name='area']").append("<option value='" + area.areaId + "'>" + area.name + "</option>");
                        }
                    });
                    self.$("select[name='area']").trigger("change", {
                        triggeredBy: "application"
                    });
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            // Add Assistants:
            self.assistants.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    var $select = self.$('div#assistants');
                    $select.empty();
                    collection.each(function (user) {
                        var selected = _.any(self.model.get('assistants'), function (assistant) {
                            return assistant.id === user.get('id');
                        }) ? 'checked' : '';
                        $select.append('<label class="checkbox">' +
                        '<input type = "checkbox" name="assistant" value="' + user.get('id') + '" ' + selected + '>' + user.getDisplayName() +
                        '</label >');
                    });
                    if (self.isEditable('assistant')) {
                        $select.find('input').removeAttr("disabled");
                    } else {
                        $select.find('input').attr("disabled", true);
                    }
                }
            });

            // Set isEditable to fields
            self.$("select, input, textarea").each(function () {
                var field = $(this).attr("name");
                if (self.isEditable(field)) {
                    $(this).removeAttr("disabled");
                } else {
                    $(this).attr("disabled", true);
                }
            });
            self.$("div.multiple-select").each(function () {
                var field = $(this).attr("id");
                if (self.isEditable(field)) {
                    $(this).removeClass("uneditable-input");
                } else {
                    $(this).addClass("uneditable-input");
                }
            });
            // Set Buttons:
            if (_.isEqual(self.model.get("phase").status, "ANAPOMPI") || _.isEqual(self.model.get("phase").status, "OLOKLIROMENI")) {
                self.$("a#save").hide();
            }
            if (!_.isEqual(self.model.get("phase").status, "ENTAGMENI")) {
                self.$("a#remove").hide();
            }
            // DatePicker
            self.$("input[data-input-type=date]").datepicker({
                onClose: function () {
                    $(this).parents("form").validate().element(this);
                }
            });
            // Enable typeahead for Subjects:
            self.$('textarea[name=subject]').typeahead({
                source: function (query, process) {
                    var subjects = new Models.Subjects();
                    subjects.fetch({
                        cache: false,
                        reset: true,
                        data: {
                            "query": query
                        },
                        success: function (collection) {
                            var data = collection.pluck("name");
                            process(data);
                        }
                    });
                }
            });
            // Validation
            self.validator = $("form", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    name: "required",
                    description: "required",
                    department: "required",
                    subject: "required",
                    sector: "required",
                    status: "required",
                    fek: {
                        required: true,
                        url: true
                    },
                    fekSentDate: "required",
                    openingDate: {
                        "required": true,
                        "dateAfter": [self.$("input[name=fekSentDate]"), 1]
                    },
                    closingDate: {
                        "required": true,
                        "dateAfter": [self.$("input[name=openingDate]"), 29]
                    }
                },
                messages: {
                    name: $.i18n.prop('validation_positionName'),
                    description: $.i18n.prop('validation_description'),
                    department: $.i18n.prop('validation_department'),
                    subject: $.i18n.prop('validation_subject'),
                    sector: $.i18n.prop('validation_sector'),
                    status: $.i18n.prop('validation_positionStatus'),
                    fek: $.i18n.prop('validation_fek'),
                    fekSentDate: $.i18n.prop('validation_fekSentDate'),
                    openingDate: {
                        required: $.i18n.prop('validation_openingDate'),
                        dateAfter: $.i18n.prop('validation_openingDate_dateAfter')
                    },
                    closingDate: {
                        required: $.i18n.prop('validation_closingDate'),
                        dateAfter: $.i18n.prop('validation_closingDate_dateAfter')
                    }
                }
            });
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required !== undefined) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }
            // Disable Save Button until user changes a field,
            // don't for non-permanent
            if (self.model.get("permanent")) {
                self.$("a#save").attr("disabled", true);
            }
            return self;
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        submit: function (event) {
            var self = this;
            var values = {
                phase: {
                    candidacies: {}
                }
            };
            // Read Input
            values.name = self.$('form input[name=name]').val();
            values.description = self.$('form textarea[name=description]').val();
            values.department = {
                "id": self.$('form select[name=department]').val()
            };
            values.sector = {
                "id": self.$('form select[name=sector]').val()
            };
            values.subject = {
                "id": self.model.has("subject") ? self.model.get("subject").id : undefined,
                "name": self.$('form textarea[name=subject]').val()
            };
            values.fek = self.$('form input[name=fek]').val();
            values.fekSentDate = self.$('form input[name=fekSentDate]').val();
            values.phase.candidacies.openingDate = self.$('form input[name=openingDate]').val();
            values.phase.candidacies.closingDate = self.$('form input[name=closingDate]').val();
            values.assistants = self.$('form input[name=assistant]:checked').map(function () {
                return {
                    id: this.value
                };
            }).get();
            // Save to model
            self.model.save(values, {
                wait: true,
                success: function () {
                    var popup;
                    App.router.navigate("positions/" + self.model.id + "/main", {
                        trigger: false
                    });
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        cancel: function () {
            var self = this;
            if (self.validator) {
                self.validator.resetForm();
            }
            self.render();
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup;
                            App.router.navigate("positions", {
                                trigger: false
                            });
                            popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionCommitteeView ***************************************************
     **************************************************************************/
    Views.PositionCommitteeView = Views.BaseView.extend({
        tagName: "div",

        uploader: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_position_committee);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {},

        render: function () {
            var self = this;
            var tpl_data, files;
            self.closeInnerViews();
            tpl_data = self.model.toJSON();
            tpl_data.members = _.sortBy(tpl_data.members, function (committeeMember) {
                return committeeMember.type + (committeeMember.registerMember.external ? "1" : "0") + committeeMember.registerMember.id;
            });
            if (App.loggedOnUser.isAssociatedWithDepartment(self.model.get("position").department) ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE") ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
                _.each(tpl_data.members, function (member) {
                    member.access = "READ_FULL";
                });
            }
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            // Add Files
            if (self.model.has("id")) {
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFileList(collection, "APOFASI_SYSTASIS_EPITROPIS", self.$("#apofasiSystasisEpitropisFileList"), {
                            withMetadata: true
                        });
                        self.addFile(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile"), {
                            withMetadata: true
                        });
                        self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
                            withMetadata: true
                        });
                    }
                });
            }

            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionCommitteeEditView ***********************************************
     **************************************************************************/
    Views.PositionCommitteeEditView = Views.BaseView.extend({
        tagName: "div",

        uploader: undefined,

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(this, "change", "renderCommitteeMembers", "isEditable", "toggleRegisterMembers", "addMembers", "removeMember", "submit", "cancel");
            self.template = _.template(tpl_position_committee_edit);
            self.templateRow = _.template(tpl_position_committee_member_edit);

            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);

            // Initialize Registers, no request is performed until render
            self.registerMembers = new Models.PositionCommitteeRegisterMembers();
            self.registerMembers.on("members:add", self.addMembers);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#toggleRegisterMembers": "toggleRegisterMembers",
            "click a#removeMember": "removeMember",
            "click a#saveCommittee": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        isEditable: function (element) {
            var self = this;
            switch (element) {
                case "positionCommittee":
                    return self.model.get("position").phase.status === "EPILOGI";
                case "apofasiSystasisEpitropisFileList":
                    return self.model.get("position").phase.status === "EPILOGI";
                case "praktikoSynedriasisEpitropisGiaAksiologitesFile":
                    return self.model.get("position").phase.status === "EPILOGI" && self.model.get("committeeMeetingDate");
                case "aitimaEpitropisProsAksiologitesFile":
                    return self.model.get("position").phase.status === "EPILOGI" && self.model.get("committeeMeetingDate");
                default:
                    return self.model.get("position").phase.status === "EPILOGI";
            }
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Existing Committee Members:
            self.renderCommitteeMembers();

            // Add Files
            if (self.model.has("id")) {
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFileListEdit(collection, "APOFASI_SYSTASIS_EPITROPIS", self.$("input[name=apofasiSystasisEpitropisFileList]"), {
                            withMetadata: true,
                            editable: self.isEditable("apofasiSystasisEpitropisFileList")
                        });
                        self.addFileEdit(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("input[name=praktikoSynedriasisEpitropisGiaAksiologitesFile]"), {
                            withMetadata: true,
                            editable: self.isEditable("praktikoSynedriasisEpitropisGiaAksiologitesFile")
                        });
                        self.addFileEdit(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("input[name=aitimaEpitropisProsAksiologitesFile]"), {
                            withMetadata: true,
                            editable: self.isEditable("aitimaEpitropisProsAksiologitesFile")
                        });
                    }
                });
            }
            // Add RegisterMembers (for adding/removing)
            if (self.isEditable("positionCommittee")) {
                // Inner View
                if (self.registerMembersView) {
                    self.registerMembersView.close();
                }
                self.registerMembersView = new Views.PositionCommitteeEditRegisterMembersView({
                    model: self.model,
                    collection: self.registerMembers

                });
                self.$("div#committee-register-members").hide();
                self.$("div#committee-register-members").html(self.registerMembersView.el);
                self.$("select").removeAttr("disabled");
                self.$("a.btn").show();

                self.registerMembersView.render();
            } else {
                self.$("div#committee-register-members").hide();
                self.$("select").attr("disabled", true);
                self.$("a.btn").hide();
            }
            // DatePicker
            self.$("input[data-input-type=date]").datepicker({
                onClose: function () {
                    $(this).parents("form").validate().element(this);
                }
            });
            // Disable Save Button until user changes a field
            self.$("a#saveCommittee").attr("disabled", true);

            return self;
        },

        renderCommitteeMembers: function () {
            var self = this;
            self.$("div#positionCommittee table tbody").empty();
            _.each(_.sortBy(self.model.get("members"), function (committeeMember) {
                return committeeMember.type + (committeeMember.registerMember.external ? "1" : "0") + committeeMember.registerMember.id;
            }), function (committeeMember, index) {
                self.$("div#positionCommittee table tbody").append(self.templateRow(_.extend(committeeMember, {
                    "index": index + 1
                })));
            });
        },

        toggleRegisterMembers: function () {
            var self = this;
            self.$("div#committee-register-members").slideToggle({
                'complete': function () {
                    self.$("a#toggleRegisterMembers").toggleClass('active');
                }
            });
        },

        addMembers: function (newCommitteeMembers) {
            var self = this;
            var popup, i;
            // Search for duplicates
            if (_.some(self.model.get("members"), function (existingMember) {
                    return _.some(newCommitteeMembers, function (newCommitteeMember) {
                        return _.isEqual(existingMember.registerMember.id, newCommitteeMember.registerMember.id);
                    });

                })) {
                popup = new Views.PopupView({
                    type: "error",
                    message: $.i18n.prop("error.member.already.exists")
                });
                popup.show();
                return;
            }
            // Add new members
            for (i = 0; i < newCommitteeMembers.length; i += 1) {
                self.model.get("members").push(newCommitteeMembers[i]);
            }
            self.model.trigger("change:members");
            self.change($.Event("change"), {
                triggeredBy: "user"
            });
            self.renderCommitteeMembers();
            // Scroll To top of table, to see added members
            window.scrollTo(0, self.$("div#positionCommittee").parent().position().top - 50);
            self.toggleRegisterMembers();
        },

        removeMember: function (event) {
            var self = this;
            var registerMemberId = $(event.currentTarget).data("registerMemberId");
            var committee = self.model.get("members");
            var position = _.indexOf(committee, _.find(committee, function (member) {
                return _.isEqual(member.registerMember.id, registerMemberId);
            }));
            committee.splice(position, 1);
            self.renderCommitteeMembers();
            self.model.trigger("change:members");
            self.change($.Event("change"), {
                triggeredBy: "user"
            });
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#saveCommittee").removeAttr("disabled");
        },

        submit: function () {
            function committeeStructure(members) {
                var structure = {
                    countRegular: 0,
                    countSubstitute: 0,
                    countInternalRegular: 0,
                    countInternalSubstitute: 0,
                    countExternalRegular: 0,
                    countExternalRegularForeign: 0,
                    countExternalSubstitute: 0,
                    countExternalSubstituteForeign: 0
                };
                _.forEach(members, function (member) {
                    switch (member.type) {
                        case 'REGULAR':
                            structure.countRegular++;
                            if (member.registerMember.external) {
                                structure.countExternalRegular++;
                                if (member.registerMember.professor.discriminator === 'PROFESSOR_FOREIGN') {
                                    structure.countExternalRegularForeign++;
                                }
                            } else {
                                structure.countInternalRegular++;
                            }
                            break;
                        case 'SUBSTITUTE':
                            structure.countSubstitute++;
                            if (member.registerMember.external) {
                                structure.countExternalSubstitute++;
                                if (member.registerMember.professor.discriminator === 'PROFESSOR_FOREIGN') {
                                    structure.countExternalSubstituteForeign++;
                                }
                            } else {
                                structure.countInternalSubstitute++;
                            }
                            break;
                    }
                });
                return structure;
            }

            function doSave(values) {
                self.model.save(values, {
                    wait: true,
                    success: function () {
                        var popup = new Views.PopupView({
                            type: "success",
                            message: $.i18n.prop("Success")
                        });
                        popup.show();
                    },
                    error: function (model, resp) {
                        var popup = new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                        });
                        popup.show();
                    }
                });
            }


            var self = this;
            var values = {
                committeeMeetingDate: self.$("input[name=committeeMeetingDate]").val(),
                candidacyEvalutionsDueDate: self.$("input[name=candidacyEvalutionsDueDate]").val(),
                members: self.model.get("members")
            };
            // 1. Check Committee structure and display warnings as necessary
            var structure = committeeStructure(values.members);
            var warnings = [];
            if (structure.countInternalRegular < 1) {
                warnings.push('error.min.internal.regular.members.failed');
            }
            if (structure.countInternalRegular !== structure.countInternalSubstitute ||
                structure.countExternalRegular !== structure.countExternalSubstitute) {
                warnings.push('error.internal.equals.external.members.failed');
            }
            if (warnings.length > 0) {
                // Show warning confirmation
                var confirm = new Views.PositionCommitteeEditConfirmView({
                    messages: warnings,
                    yes: function () {
                        doSave(values);
                    }
                });
                confirm.show();
            } else {
                doSave(values);
            }
        },

        cancel: function () {
            var self = this;
            self.model.fetch({
                cache: false
            });
        },

        close: function () {
            this.closeInnerViews();
            this.registerMembers.off("member:selected");
            if (this.registerMembersView) {
                this.registerMembersView.close();
            }
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionCommitteeEditConfirmView ****************************************
     **************************************************************************/
    Views.PositionCommitteeEditConfirmView = Views.BaseView.extend({
        tagName: "div",

        className: "modal",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show");
            this.template = _.template(tpl_position_committee_edit_confirm);
        },

        events: {
            "click a#yes": function () {
                this.$el.modal('hide');
                if (_.isFunction(this.options.yes)) {
                    this.options.yes();
                }
            }
        },

        render: function () {
            var self = this;
            self.$el.empty();
            self.$el.append(self.template({
                messages: self.options.messages
            }));
        },
        show: function () {
            var self = this;
            self.render();
            self.$el.modal();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionCommitteeEditRegisterMembersView ********************************
     **************************************************************************/

    Views.PositionCommitteeEditRegisterMembersView = Views.BaseView.extend({
        tagName: "div",

        className: "span12 well",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "showRegisterMembers", "addMembers");
            this.template = _.template(tpl_position_committee_edit_register_member_list);

            this.registries = new Models.Registries();
            this.registries.url = this.model.url() + "/register";

            this.model.bind("change:members", this.render, this);
        },

        events: {
            "click a#selectRegister": "showRegisterMembers",
            "click a#addMembers": "addMembers"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template());
            self.registries.fetch({
                reset: true,
                cache: true,
                success: function (collection) {
                    var $select = self.$("select[name=register]");
                    $select.empty();
                    $select.append('<option value="">--</option>');
                    collection.each(function (register) {
                        $select.append('<option value="' + register.get('id') + '">' + register.get("subject").name + '</option>');
                    });
                    $select.chosen({width: "100%"});
                }
            });
            self.$("table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "select"},
                    {"mData": "id"},
                    {"mData": "external"},
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "role"},
                    {"mData": "institution"},
                    {"mData": "committees"}
                ]
            });
            return self;
        },

        showRegisterMembers: function () {
            var self = this;
            var registerId = self.$("select[name=register]").val();
            if (!registerId) {
                // CLEAN TABLE
                self.$("table").dataTable().fnClearTable();
                return;
            }
            self.$("table").dataTable().fnDestroy();
            self.$("table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "select"},
                    {"mData": "id"},
                    {"mData": "external"},
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "role"},
                    {"mData": "institution"},
                    {"mData": "committees"}
                ],
                "bProcessing": true,
                "sAjaxSource": self.model.url() + "/register/" + registerId + "/member",
                "fnServerData": function (sSource, aoData, fnCallback) {
                    self.collection.url = sSource;
                    self.collection.fetch({
                        reset: true,
                        cache: true,
                        success: function (collection) {
                            var data = {
                                aaData: collection.map(function (registerMember) {
                                    var isMember = _.some(self.model.get("members"), function (member) {
                                        return _.isEqual(member.registerMember.professor.id, registerMember.get('professor').id);
                                    });
                                    return {
                                        select: isMember ? '' :
                                        '<select name="selectMember" class="input-small">' +
                                        '<option value="NONE">----</option>' +
                                        '<option value="REGULAR" data-model-id="' + registerMember.get('id') + '" data-type="REGULAR">' + $.i18n.prop('PositionCommitteeMemberTypeREGULAR') + '</option>' +
                                        '<option value="SUBSTITUTE" data-model-id="' + registerMember.get('id') + '" data-type="SUBSTITUTE">' + $.i18n.prop("PositionCommitteeMemberTypeSUBSTITUTE") + '</option>' +
                                        '</select>',
                                        id: '<a href = "#user/' + registerMember.get('professor').user.id + '">' + registerMember.get('professor').user.id + '</a>',
                                        external: registerMember.get('external') ? $.i18n.prop('RegisterMemberExternal') : $.i18n.prop('RegisterMemberInternal'),
                                        firstname: registerMember.get('professor').user.firstname[App.locale],
                                        lastname: registerMember.get('professor').user.lastname[App.locale],
                                        role: $.i18n.prop(registerMember.get('professor').discriminator),
                                        institution: registerMember.get('professor').discriminator === 'PROFESSOR_FOREIGN' ? registerMember.get('professor').institution : _.templates.department(registerMember.get('professor').department),
                                        committees: registerMember.get('professor').committeesCount
                                    };

                                })
                            };
                            fnCallback(data);
                        }
                    });
                }

            });
        },

        addMembers: function () {
            var self = this;
            var committeeMembers = [];

            // Use dataTable to select elements, as pagination removes them from
            // DOM
            self.$("table").dataTable().$('select[name=selectMember] option:selected').each(function () {
                var selectedOption, id, type, model, committeeMember;
                selectedOption = $(this);
                id = selectedOption.data('modelId');
                if (!id) {
                    return;
                }
                type = selectedOption.data('type');
                model = self.collection.get(id);
                committeeMember = {
                    type: type,
                    registerMember: model.toJSON()
                };
                committeeMembers.push(committeeMember);
            });
            self.collection.trigger("members:add", committeeMembers);
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionEvaluationView **************************************************
     **************************************************************************/
    Views.PositionEvaluationView = Views.BaseView.extend({
        tagName: "div",

        uploader: undefined,

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_evaluation);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        events: {},

        render: function () {
            var self = this;
            var tpl_data;
            self.closeInnerViews();
            tpl_data = self.model.toJSON();
            if (App.loggedOnUser.isAssociatedWithDepartment(self.model.get("position").department) ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE") ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
                _.each(tpl_data.evaluators, function (evaluator) {
                    evaluator.access = "READ_FULL";
                });
            }
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            // Add Files
            _.each(self.model.get("evaluators"), function (evaluator) {
                var positionEvaluator = new Models.PositionEvaluator(_.extend(evaluator, {
                    evaluation: self.model.toJSON()
                })), files = new Models.Files();
                files.url = positionEvaluator.url() + "/file";
                files.fetch({
                    cache: false,
                    reset: true,
                    success: function (collection) {
                        self.addFileList(collection, "AKSIOLOGISI", self.$("#positionEvaluatorFiles_" + positionEvaluator.get("position")).find("#aksiologisiFileList"), {
                            withMetadata: true
                        });
                    }
                });
            });
            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionEvaluationEditView **********************************************
     **************************************************************************/
    Views.PositionEvaluationEditView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(this, "change", "renderEvaluators", "isEditable", "toggleRegisterMembers", "addMember", "removeMember", "submit", "cancel");
            self.template = _.template(tpl_position_evaluation_edit);
            self.templateRow = _.template(tpl_position_evaluation_evaluator_edit);

            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);

            // Initialize Registers, no request is performed until render
            self.registerMembers = new Models.PositionEvaluationRegisterMembers();
            self.registerMembers.on("member:add", self.addMember);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#toggleRegisterMembers": "toggleRegisterMembers",
            "click a#cancel": "cancel",
            "click a#removeMember": "removeMember",
            "click a#saveEvaluation": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        isEditable: function (element) {
            var self = this;
            switch (element) {
                case "positionEvaluation":
                    return self.model.get("position").phase.status === "EPILOGI" && self.model.get("canUpdateEvaluators");
                case "aksiologisiFileList":
                    return self.model.get("position").phase.status === "EPILOGI" && self.model.get("canUploadEvaluations");
                default:
                    return self.model.get("position").phase.status === "EPILOGI";
            }
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Existing Evaluators:
            self.renderEvaluators();

            // Add RegisterMembers (for adding/removing)
            if (self.isEditable("positionEvaluation")) {
                // Inner View
                if (self.registerMembersView) {
                    self.registerMembersView.close();
                }
                self.registerMembersView = new Views.PositionEvaluationEditRegisterMembersView({
                    model: self.model,
                    collection: self.registerMembers
                });
                self.$("div#evaluation-register-members").hide();
                self.$("div#evaluation-register-members").html(self.registerMembersView.el);
                self.$("a.btn").show();

                self.registerMembersView.render();
            } else {
                self.$("div#evaluation-register-members").hide();
                self.$("select").attr("disabled", true);
                self.$("a.btn").hide();
            }
            // DatePicker
            self.$("input[data-input-type=date]").datepicker({
                onClose: function () {
                    $(this).parents("form").validate().element(this);
                }
            });
            // Disable Save Button until user changes a field
            self.$("a#saveEvaluation").attr("disabled", true);

            return self;
        },

        renderEvaluators: function () {
            var self = this;
            var files;
            self.$("div#positionEvaluator_0").empty();
            self.$("div#positionEvaluator_1").empty();
            _.each(self.model.get("evaluators"), function (evaluator) {
                var $el = self.$("div#positionEvaluator_" + evaluator.position);
                $el.html(self.templateRow(evaluator));
                // Add files
                if (evaluator.id) {
                    files = new Models.Files();
                    files.url = self.model.url() + "/evaluator/" + evaluator.id + "/file";
                    files.fetch({
                        cache: false,
                        success: function (collection) {
                            self.addFileListEdit(collection, "AKSIOLOGISI", $el.find("input[name=aksiologisiFileList]"), {
                                withMetadata: true,
                                editable: self.isEditable("aksiologisiFileList")
                            });
                        }
                    });
                } else {
                    $el.find("#aksiologisiFileList").html($.i18n.prop("PressSave"));
                }
            });
        },

        toggleRegisterMembers: function () {
            var self = this;
            self.$("div#evaluation-register-members").toggle();
            self.$("a#toggleRegisterMembers").toggleClass('active');
        },

        addMember: function (evaluator) {
            var self = this;
            if (_.any(self.model.get("evaluators"), function (member) {
                    return _.isEqual(member.registerMember.professor.id, evaluator.registerMember.professor.id);
                })) {
                var popup = new Views.PopupView({
                    type: "error",
                    message: $.i18n.prop("error.member.already.exists")
                });
                popup.show();
                return;
            }

            self.model.get("evaluators")[evaluator.position] = evaluator;

            self.model.trigger("change:members");
            self.change($.Event("change"), {
                triggeredBy: "user"
            });
            self.renderEvaluators();
        },

        removeMember: function (event) {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    var registerMemberId = $(event.currentTarget).data("registerMemberId");
                    var evaluators = self.model.get("evaluators");
                    var position = _.indexOf(evaluators, _.find(evaluators, function (member) {
                        return _.isEqual(member.registerMember.id, registerMemberId);
                    }));
                    evaluators.splice(position, 1);

                    // Render
                    self.renderEvaluators();
                    self.model.trigger("change:members");
                    self.change($.Event("change"), {
                        triggeredBy: "user"
                    });
                }
            });
            confirm.show();
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#saveEvaluation").removeAttr("disabled");
        },

        submit: function () {
            var self = this;
            self.model.save({}, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        cancel: function () {
            var self = this;
            self.model.fetch({
                cache: false
            });
        },

        close: function () {
            this.closeInnerViews();
            this.registerMembers.off("member:selected");
            if (this.registerMembersView) {
                this.registerMembersView.close();
            }
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();

        }
    });

    /***************************************************************************
     * PositionEvaluationEditRegisterMembersView *******************************
     **************************************************************************/

    Views.PositionEvaluationEditRegisterMembersView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "showRegisterMembers", "addMember");
            this.template = _.template(tpl_position_evaluation_edit_register_member_list);

            this.registries = new Models.Registries();
            this.registries.url = this.model.url() + "/register";

            this.model.bind("change:members", this.render, this);
        },

        events: {
            "click a#selectRegister": "showRegisterMembers",
            "click a#addMember": "addMember"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template());
            self.registries.fetch({
                reset: true,
                cache: true,
                success: function (collection) {
                    var $select = self.$("select[name=register]");
                    $select.empty();
                    $select.append('<option value="">--</option>');
                    collection.each(function (register) {
                        $select.append('<option value="' + register.get('id') + '">' + register.get("subject").name + '</option>');
                    });
                    $select.chosen({width: "100%"});
                }
            });
            self.$("table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "id"},
                    {"mData": "external"},
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "role"},
                    {"mData": "institution"},
                    {"mData": "select"}
                ]
            });
            return self;
        },

        showRegisterMembers: function () {
            var self = this;
            var registerId = self.$("select[name=register]").val();
            if (!registerId) {
                // CLEAN TABLE
                self.$("table").dataTable().fnClearTable();
                return;
            }
            self.$("table").dataTable().fnDestroy();
            self.$("table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "id"},
                    {"mData": "external"},
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "role"},
                    {"mData": "institution"},
                    {"mData": "select"}
                ],
                "bProcessing": true,
                "sAjaxSource": self.model.url() + "/register/" + registerId + "/member",
                "fnServerData": function (sSource, aoData, fnCallback) {
                    self.collection.url = sSource;
                    self.collection.fetch({
                        reset: true,
                        cache: true,
                        success: function (collection) {
                            var data = {
                                aaData: _.map(collection.filter(function (registerMember) {
                                        // Keep only external members
                                        return registerMember.get('external');
                                    }),
                                    function (registerMember) {
                                        var isMember = _.some(self.model.get("evaluators"), function (evaluator) {
                                            return _.isEqual(evaluator.registerMember.professor.id, registerMember.get('professor').id);
                                        });
                                        return {
                                            id: '<a href = "#user/' + registerMember.get('professor').user.id + '">' + registerMember.get('professor').user.id + '</a>',
                                            external: registerMember.get('external') ? $.i18n.prop('RegisterMemberExternal') : $.i18n.prop('RegisterMemberInternal'),
                                            firstname: registerMember.get('professor').user.firstname[App.locale],
                                            lastname: registerMember.get('professor').user.lastname[App.locale],
                                            role: $.i18n.prop(registerMember.get('professor').discriminator),
                                            institution: registerMember.get('professor').discriminator === 'PROFESSOR_FOREIGN' ? registerMember.get('professor').institution : _.templates.department(registerMember.get('professor').department),
                                            select: isMember ? '' :
                                            '<div class="btn-group">' +
                                            '<a class = "btn btn-small btn-success dropdown-toggle" data-toggle="dropdown" >' + $.i18n.prop('btn_select') + '<span class="caret"></span></a>' +
                                            '<ul class="dropdown-menu">' +
                                            '<li><a id="addMember" data-model-id="' + registerMember.get('id') + '" data-position="0"><i class="icon-plus"></i>' + $.i18n.prop('PositionEvaluatorFirst') + '</a></li>' +
                                            '<li><a id="addMember" data-model-id="' + registerMember.get('id') + '" data-position="1"><i class="icon-plus"></i>' + $.i18n.prop('PositionEvaluatorSecond') + '</a></li>' +
                                            '</ul>' +
                                            '</div>'
                                        };
                                    })
                            };
                            fnCallback(data);
                        }
                    });
                }

            });
        },

        addMember: function (event) {
            var self = this;
            var id = $(event.currentTarget).data('modelId');
            var position = $(event.currentTarget).data('position'); // First or second evaluator
            var selectedModel = self.collection.get(id);
            var evaluator = {
                position: position,
                registerMember: selectedModel.toJSON()
            };
            self.collection.trigger("member:add", evaluator);
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PositionCandidaciesView *************************************************
     **************************************************************************/
    Views.PositionCandidaciesView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_candidacies);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        events: {
            "click #candidacyStatusActionsHistory" : "candidacyStatusActionsHistory"
        },

        render: function () {
            var self = this;
            var tpl_data;
            self.closeInnerViews();
            tpl_data = _.extend(self.model.toJSON(), {
                showEvaluators: _.some(self.model.get("candidacies"), function (candidacy) {
                    return !_.isUndefined(candidacy.proposedEvaluators);
                })
            });
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table#positionCandidatesTable"))) {
                self.$("table#positionCandidatesTable").dataTable({
                    "aaSorting": [[3, "asc"]],
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            return self;
        },

        candidacyStatusActionsHistory: function (event) {
            var candidacyId = $(event.currentTarget).data('candidacyId');
            var candidacyStatusModelList = new Models.CandidacyStatusList({},{
                candidacyId: candidacyId
            });
            candidacyStatusModelList.fetch({
                cache: false,
                wait: true,
                success: function (collection) {
                    var confirm = new Views.CandidateCandidacyActionsHistoryView({
                        title: $.i18n.prop('actionsHistoryTitle'),
                        statusHistoryList: collection.toJSON()
                    });
                    confirm.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind("destroy", this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionCandidaciesEditView *********************************************
     **************************************************************************/
    Views.PositionCandidaciesEditView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_candidacies_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        events: {
            "click #candidacyStatusActionsHistory" : "candidacyStatusActionsHistory"
        },

        isEditable: function (element) {
            var self = this;
            if (element === 'eisigisiDepYpopsifiouFileList') {
                // The nominationCommitteeConverged is equal for all candidacies no need to search in array
                return self.model.get("candidacies").length > 0 ? !self.model.get("candidacies")[0].nominationCommitteeConverged : false;
            }
            return self.model.get("position").phase.status === 'EPILOGI';
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Init jQuery.widgets
            if (!$.fn.DataTable.fnIsDataTable(self.$("table#candidaciesTable"))) {
                self.$("table#candidaciesTable").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "aaSorting": [[3, "asc"]],
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            // Add files
            if (self.model.has("id")) {
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        _.each(self.model.get("candidacies"), function (candidacy) {
                            _.each(candidacy.proposedEvaluators, function (proposedEvaluator) {
                                var filteredFiles = new Models.Files(collection.filter(function (file) {
                                    return file.get("evaluator").id === proposedEvaluator.id;
                                }));
                                filteredFiles.url = collection.url;
                                self.addFileListEdit(filteredFiles, "EISIGISI_DEP_YPOPSIFIOU",
                                    self.$("input[name=eisigisiDepYpopsifiouFileList][data-candidacy-evaluator-id=" + proposedEvaluator.id + "]"), {
                                        withMetadata: true,
                                        editable: self.isEditable("eisigisiDepYpopsifiouFileList")
                                    });
                            });
                        });
                    }
                });
            }

            return self;
        },

        candidacyStatusActionsHistory: function (event) {
            var candidacyId = $(event.currentTarget).data('candidacyId');
            var candidacyStatusModelList = new Models.CandidacyStatusList({},{
                candidacyId: candidacyId
            });
            candidacyStatusModelList.fetch({
                cache: false,
                wait: true,
                success: function (collection) {
                    var confirm = new Views.CandidateCandidacyActionsHistoryView({
                        title: $.i18n.prop('actionsHistoryTitle'),
                        statusHistoryList: collection.toJSON()
                    });
                    confirm.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind("destroy", this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionNominationView **************************************************
     **************************************************************************/
    Views.PositionNominationView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_nomination);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));
            // Add Files
            files = new Models.Files();
            files.url = self.model.url() + "/file";
            files.fetch({
                cache: false,
                success: function (collection) {
                    self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
                        withMetadata: true
                    });
                    self.addFile(collection, "PRAKTIKO_EPILOGIS", self.$("#praktikoEpilogisFile"), {
                        withMetadata: true
                    });
                    self.addFile(collection, "DIAVIVASTIKO_PRAKTIKOU", self.$("#diavivastikoPraktikouFile"), {
                        withMetadata: true
                    });
                    self.addFile(collection, "PRAKSI_DIORISMOU", self.$("#praksiDiorismouFile"), {
                        withMetadata: true
                    });

                    self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
                        withMetadata: true
                    });
                }
            });
            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind("destroy", this.close, this);
            this.$el.unbind();
            this.$el.remove();

        }
    });

    /***************************************************************************
     * PositionNominationEditView **********************************************
     **************************************************************************/
    Views.PositionNominationEditView = Views.BaseView.extend({
        tagName: "div",

        uploader: undefined,

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(this, "change", "isEditable", "submit", "cancel");
            self.template = _.template(tpl_position_nomination_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);

            self.positionCandidacies = new Models.Candidacies({}, {
                position: self.model.get("position").id
            });
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#saveNomination": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        isEditable: function (element) {
            var self = this;
            switch (element) {
                case "praktikoEpilogisFile":
                case "diavivastikoPraktikouFile":
                    return (self.model.get("position").phase.status === "EPILOGI" || self.model.get("position").phase.status === "STELEXOMENI") && self.model.get("nominationCommitteeConvergenceDate");
                case "praksiDiorismouFile":
                case "nominationFEK":
                    return self.model.get("position").phase.status === "STELEXOMENI";
                default:
                    return self.model.get("position").phase.status === "EPILOGI" || self.model.get("position").phase.status === "STELEXOMENI";
            }
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Nominated and Second Nominated:
            self.$("select[name='nominatedCandidacy']").change(function () {
                self.$("select[name='nominatedCandidacy']").next(".help-block").html(self.$("select[name='nominatedCandidacy'] option:selected").text());
            });
            self.$("select[name='secondNominatedCandidacy']").change(function () {
                self.$("select[name='secondNominatedCandidacy']").next(".help-block").html(self.$("select[name='secondNominatedCandidacy'] option:selected").text());
            });
            self.positionCandidacies.fetch({
                cache: false,
                wait: true,
                success: function (collection) {
                    var nominatedCandidacyId = self.model.has("nominatedCandidacy") ? self.model.get("nominatedCandidacy").id : undefined;
                    var secondNominatedCandidacyId = self.model.has("secondNominatedCandidacy") ? self.model.get("secondNominatedCandidacy").id : undefined;
                    // Clean
                    self.$("select[name='nominatedCandidacy']").empty();
                    self.$("select[name='secondNominatedCandidacy']").empty();
                    // Add Candidacies in selector:
                    self.$("select[name='nominatedCandidacy']").append("<option value=''>--</option>");
                    self.$("select[name='secondNominatedCandidacy']").append("<option value=''>--</option>");
                    collection.each(function (candidacy) {
                        if (_.isEqual(candidacy.id, nominatedCandidacyId)) {
                            self.$("select[name='nominatedCandidacy']").append("<option value='" + candidacy.get("id") + "' selected>" + candidacy.get("snapshot").firstname[App.locale] + " " + candidacy.get("snapshot").lastname[App.locale] + "</option>");
                        } else {
                            self.$("select[name='nominatedCandidacy']").append("<option value='" + candidacy.get("id") + "'>" + candidacy.get("snapshot").firstname[App.locale] + " " + candidacy.get("snapshot").lastname[App.locale] + "</option>");
                        }
                        if (_.isEqual(candidacy.id, secondNominatedCandidacyId)) {
                            self.$("select[name='secondNominatedCandidacy']").append("<option value='" + candidacy.get("id") + "' selected>" + candidacy.get("snapshot").firstname[App.locale] + " " + candidacy.get("snapshot").lastname[App.locale] + "</option>");
                        } else {
                            self.$("select[name='secondNominatedCandidacy']").append("<option value='" + candidacy.get("id") + "'>" + candidacy.get("snapshot").firstname[App.locale] + " " + candidacy.get("snapshot").lastname[App.locale] + "</option>");
                        }
                    });
                    self.$("select[name='nominatedCandidacy']").trigger("change", {
                        triggeredBy: "application"
                    });
                    self.$("select[name='secondNominatedCandidacy']").trigger("change", {
                        triggeredBy: "application"
                    });
                }
            });
            // Add Files
            files = new Models.Files();
            files.url = self.model.url() + "/file";
            files.fetch({
                cache: false,
                success: function (collection) {
                    self.addFileEdit(collection, "PROSKLISI_KOSMITORA", self.$("input[name=prosklisiKosmitoraFile]"), {
                        withMetadata: true,
                        editable: self.isEditable("prosklisiKosmitoraFile")
                    });

                    self.addFileListEdit(collection, "PRAKTIKO_EPILOGIS", self.$("input[name=praktikoEpilogisFile]"), {
                        withMetadata: true,
                        editable: self.isEditable("praktikoEpilogisFile")
                    });
                    self.addFileEdit(collection, "DIAVIVASTIKO_PRAKTIKOU", self.$("input[name=diavivastikoPraktikouFile]"), {
                        withMetadata: true,
                        editable: self.isEditable("diavivastikoPraktikouFile")
                    });
                    self.addFileEdit(collection, "PRAKSI_DIORISMOU", self.$("input[name=praksiDiorismouFile]"), {
                        withMetadata: true,
                        editable: self.isEditable("praksiDiorismouFile")
                    });

                    self.addFileEdit(collection, "APOFASI_ANAPOMPIS", self.$("input[name=apofasiAnapompisFile]"), {
                        withMetadata: true,
                        editable: self.isEditable("apofasiAnapompisFile")
                    });
                }
            });
            // DatePicker
            self.$("input[data-input-type=date]").datepicker({
                onClose: function () {
                    $(this).parents("form").validate().element(this);
                }
            });
            // Set isEditable to fields
            self.$("select, input, textarea").each(function () {
                var field = $(this).attr("name");
                if (self.isEditable(field)) {
                    $(this).removeAttr("disabled");
                } else {
                    $(this).attr("disabled", true);
                }
            });
            // Disable Save Button until user changes a field
            self.$("a#saveNomination").attr("disabled", true);

            return self;
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#saveNomination").removeAttr("disabled");
        },

        submit: function () {
            var self = this;
            var values = {
                nominationCommitteeConvergenceDate: self.$('form input[name=nominationCommitteeConvergenceDate]').val(),
                nominationToETDate: self.$('form input[name=nominationToETDate]').val(),
                nominationFEK: self.$('form input[name=nominationFEK]').val(),
                nominatedCandidacy: {
                    id: self.$('form select[name=nominatedCandidacy]').val()
                },
                secondNominatedCandidacy: {
                    id: self.$('form select[name=secondNominatedCandidacy]').val()
                }
            };
            self.model.save(values, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        cancel: function () {
            var self = this;
            self.model.fetch({
                cache: false
            });
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind("destroy", this.close, this);
            this.$el.unbind();
            this.$el.remove();

        }
    });

    /***************************************************************************
     * PositionComplementaryDocumentsView **************************************
     **************************************************************************/
    Views.PositionComplementaryDocumentsView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_complementaryDocuments);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Files
            if (self.model.has("id")) {
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
                            withMetadata: true
                        });
                    }
                });
            }
            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionComplementaryDocumentsEditView **********************************
     **************************************************************************/
    Views.PositionComplementaryDocumentsEditView = Views.BaseView.extend({
        tagName: "div",

        uploader: undefined,

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "cancel");
            self.template = _.template(tpl_position_complementaryDocuments_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        events: {},

        isEditable: function () {
            var self = this;
            return _.indexOf(["ENTAGMENI", "ANOIXTI", "EPILOGI", "STELEXOMENI", "ANAPOMPI"], self.model.get("position").phase.status) >= 0;
        },

        render: function () {
            var self = this;
            var files;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Add Files
            if (self.model.has("id")) {
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFileListEdit(collection, "DIOIKITIKO_EGGRAFO", self.$("input[name=dioikitikoEggrafoFileList]"), {
                            withMetadata: true,
                            editable: self.isEditable("dioikitikoEggrafoFileList")
                        });
                    }
                });
            }
            return self;
        },

        cancel: function () {
            var self = this;
            self.model.fetch({
                cache: false
            });
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.model.unbind('destory', this.close, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionHelpdeskView ****************************************************
     **************************************************************************/
    Views.PositionHelpdeskView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        events: {},

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_position_helpdesk);

            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        render: function () {
            var self = this;
            var positionView = new Views.PositionView({
                model: self.model
            });
            var positionEditView = new Views.PositionMainEditView({
                model: self.model
            });
            self.closeInnerViews();
            self.$el.empty();
            self.$el.html(self.template());

            // Add Position View
            self.$("#position").html(positionView.render().el);
            self.innerViews.push(positionView);
            // Add Position Main Edit View
            self.$("#positionMainEdit").html(positionEditView.render().el);
            self.innerViews.push(positionEditView);

            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();

            this.model.unbind('change', this.render, this);
            this.model.unbind("destroy", this.close, this);
        }
    });

    /***************************************************************************
     * RegisterListView ********************************************************
     **************************************************************************/
    Views.RegisterListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "renderActions", "selectRegister", "createRegister");
            this.template = _.template(tpl_register_list);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
            this.collection.bind("add", this.render, this);
            this.collection.bind("remove", this.render, this);
        },

        events: {
            "click a#createRegister": "createRegister",
            "click a#select": "selectRegister"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                canExportGeneric: App.loggedOnUser.hasRole("INSTITUTION_MANAGER") || App.loggedOnUser.hasRole("ADMINISTRATOR"),
                exportGenericUrl: (new Models.Register()).urlRoot + "/professorsexport?X-Auth-Token=" + encodeURIComponent(App.authToken),
                showAmMember: App.loggedOnUser.hasRole("PROFESSOR_DOMESTIC") || App.loggedOnUser.hasRole("PROFESSOR_FOREIGN"),
                registries: (function () {
                    var result = [];
                    var gCanExport =
                        App.loggedOnUser.hasRole("MINISTRY_MANAGER") ||
                        App.loggedOnUser.hasRole("MINISTRY_ASSISTANT") ||
                        App.loggedOnUser.hasRole("ADMINISTRATOR");

                    self.collection.each(function (model) {
                        var canEdit = model.isEditableBy(App.loggedOnUser);
                        var canExport = App.loggedOnUser.isAssociatedWithInstitution(model.get("institution"));
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.canExport = gCanExport || canExport;
                            item.exportUrl = model.url() + "/export?X-Auth-Token=" + encodeURIComponent(App.authToken);
                            item.canEdit = canEdit;

                            result.push(item);
                        }
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(this.template(tpl_data));

            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
                self.$("table thead input").keyup(function () {
                    self.$("table").dataTable().fnFilter(this.value, self.$("table thead input").index(this));
                });
            }
            // Add Actions
            self.renderActions();
            return self;
        },

        renderActions: function () {
            var self = this;
            if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
                return;
            }
            self.$("#actions").append("<div class=\"btn-group\"><input type=\"hidden\" name=\"institution\" /><a id=\"createRegister\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_register') + " </a></div>");
            // Add institutions in selector:
            App.institutions = App.institutions || new Models.Institutions();
            App.institutions.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    var institution = collection.find(function (institution) {
                        return App.loggedOnUser.isAssociatedWithInstitution(institution);
                    });
                    self.$("#actions input[name=institution]").val(institution.get("id"));
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        selectRegister: function (event, register) {
            var selectedModel = register || this.collection.get($(event.currentTarget).attr('data-register-cid'));
            if (selectedModel) {
                this.collection.trigger("register:selected", selectedModel);
            }
        },

        createRegister: function () {
            var self = this;
            var newRegister = new Models.Register();
            newRegister.save({
                institution: {
                    id: self.$("input[name='institution']").val()
                }
            }, {
                wait: true,
                success: function () {
                    self.collection.add(newRegister);
                    self.selectRegister(undefined, newRegister);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RegisterView ************************************************************
     **************************************************************************/
    Views.RegisterView = Views.BaseView.extend({
        tagName: "div",

        id: "registerview",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_register);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {},

        render: function () {
            var self = this;
            var tpl_data;
            self.$el.empty();
            self.addTitle();

            // Prepare tpl_data
            tpl_data = self.model.toJSON();
            if (App.loggedOnUser.isAssociatedWithInstitution(self.model.get("institution")) ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE") ||
                App.loggedOnUser.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE") ||
                App.loggedOnUser.hasRoleWithStatus("ADMINISTRATOR", "ACTIVE")) {
                _.each(tpl_data.members, function (member) {
                    member.access = "READ_FULL";
                });
            }

            // Add to element
            self.$el.append(self.template(tpl_data));

            // Init jQuery.widgets
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            return self;
        },

        close: function () {
            this.model.unbind("change");
            this.model.unbind("destroy");
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RegisterEditView ********************************************************
     **************************************************************************/
    Views.RegisterEditView = Views.BaseView.extend({
        tagName: "div",

        id: "registerview",

        validator: undefined,

        initialize: function (options) {
            var self = this;

            this._super('initialize', [options]);
            _.bindAll(this, "change", "renderMembers", "toggleAddMember", "submit", "remove", "cancel", "addMembers", "removeMember");
            self.template = _.template(tpl_register_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);

            // Initialize Professor, no request is performed until render
            self.professors = new Models.Professors();
            self.professors.url = self.model.url() + "/professor";
            self.professors.on("selected", self.addMembers);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#cancel": "cancel",
            "click a#remove": "remove",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                this.$("form").submit();
            },
            "submit form": "submit",
            "click a#toggleAddMember": "toggleAddMember",
            "click a#removeMember": "removeMember"
        },

        render: function () {
            var self = this;
            var propName;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Existing Members
            self.renderMembers();

            // Widgets
            self.validator = self.$("form[name=registerForm]").validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    'institution': 'required',
                    'subject': 'required'
                },
                messages: {
                    'institution': $.i18n.prop('validation_institution'),
                    'subject': $.i18n.prop('validation_required')
                }
            });
            self.$('input[name=subject]').typeahead({
                source: function (query, process) {
                    var subjects = new Models.Subjects();
                    subjects.fetch({
                        cache: false,
                        reset: true,
                        data: {
                            "query": query
                        },
                        success: function (collection) {
                            var data = collection.pluck("name");
                            process(data);
                        }
                    });
                }
            });

            // Professors View
            if (self.professorListView) {
                self.professorListView.close();
            }
            self.professorListView = new Views.RegisterEditProfessorListView({
                model: self.model, // This is needed to allow disable button for existing members
                collection: self.professors
            });
            self.$("div#register-professor-list").hide();
            self.$("div#register-professor-list").html(self.professorListView.render().el);

            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required !== undefined) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }
            // Disable Save Button until user changes a field,
            // don't for non-permanent
            if (self.model.get("permanent")) {
                self.$("a#save").attr("disabled", true);
            }
            return self;
        },

        renderMembers: function () {
            var self = this;
            if ($.fn.DataTable.fnIsDataTable(self.$("div#registerMembers table")[0])) {
                self.$("div#registerMembers table").dataTable().fnDestroy();
            }
            self.$("#registerMembers table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "id", "sType": "html"},
                    {"mData": "profile"},
                    {"mData": "rank"},
                    {"mData": "institution"},
                    {"mData": "subject"},
                    {"mData": "external"},
                    {"mData": "options", "sType": "html"}
                ],
                "aaData": _.map(self.model.get("members"), function (member) {
                    return {
                        external: $.i18n.prop(member.external ? "Yes" : "No"),
                        id: '<a href="#user/' + member.professor.user.id + '" target="user">' + member.professor.user.id + '</a>',
                        firstname: member.professor.user.firstname[App.locale],
                        lastname: member.professor.user.lastname[App.locale],
                        profile: $.i18n.prop(member.professor.discriminator),
                        rank: member.professor.rank ? member.professor.rank.name[App.locale] : '',
                        institution: _.isEqual(member.professor.discriminator,
                            'PROFESSOR_FOREIGN') ? member.professor.institution : _.templates.department(member.professor.department),
                        subject: ((_.isObject(member.professor.subject) ? member.professor.subject.name : '') + ' ' + (_.isObject(member.professor.fekSubject) ? member.professor.fekSubject.name : '')).trim(),
                        options: member.canBeDeleted ? '<a id="removeMember" class="btn btn-mini btn-danger" data-professor-id="' + member.professor.id + '" data-toggle="tooltip" title="' + $.i18n.prop('btn_remove_member') + '"><i class="icon-remove icon-white"></i></a>' : ''
                    };
                })
            });
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        toggleAddMember: function () {
            var self = this;
            self.$("div#register-professor-list").slideToggle({
                complete: function () {
                    var toggleButton = self.$("a#toggleAddMember");
                    toggleButton.toggleClass('active');
                }
            });
        },

        addMembers: function (professors) {
            var self = this;
            var popup;
            if (_.any(self.model.get("members"), function (existingMember) {
                    return professors.some(function (professor) {
                        return _.isEqual(existingMember.professor.id, professor.get("id"));
                    });
                })) {
                popup = new Views.PopupView({
                    type: "error",
                    message: $.i18n.prop("error.member.already.exists")
                });
                popup.show();
                return;
            }
            // Add new members
            professors.each(function (professor) {
                self.model.get("members").push({
                    "register": {
                        id: self.model.get("id")
                    },
                    "professor": professor.toJSON(),
                    internal: undefined,
                    canBeDeleted: true
                });
            });
            self.model.trigger("change:members");
            self.change($.Event("change"), {
                triggeredBy: "user"
            });
            self.renderMembers();
            // Scroll To top of table, to see added members
            window.scrollTo(0, self.$("div#registerMembers").parent().position().top - 50);
            self.toggleAddMember($.Event("click"));
        },

        removeMember: function (event) {
            var self = this;
            var professorId = $(event.currentTarget).data("professorId");
            var registerMembers = self.model.get("members");
            var index = _.indexOf(registerMembers, _.find(registerMembers, function (member) {
                return _.isEqual(member.professor.id, professorId);
            }));
            registerMembers.splice(index, 1);
            self.model.trigger("change:members");
            self.renderMembers();

            self.change($.Event("change"), {
                triggeredBy: "user"
            });
        },

        submit: function (event) {
            var self = this;
            var values = {};
            // Read Input
            values.institution = {
                "id": self.$('form input[name=institution]').val()
            };
            values.members = self.model.get("members");
            values.subject = {
                name: self.$('form input[name=subject]').val()
            };
            // Save to model
            self.model.save(values, {
                wait: true,
                success: function () {
                    var popup;
                    App.router.navigate("registers/" + self.model.id, {
                        trigger: false
                    });
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup;
                            App.router.navigate("registers", {
                                trigger: false
                            });
                            popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        cancel: function () {
            var self = this;
            if (self.validator) {
                self.validator.resetForm();
            }
            self.render();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * RegisterEditProfessorListView *******************************************
     **************************************************************************/

    Views.RegisterEditProfessorListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "search", "selectProfessors", "handleKeyUp", "displaySelected");
            this.template = _.template(tpl_register_edit_professor_list);
            this.model.bind("change:members", this.render, this);
        },

        events: {
            "click a#search": "search",
            "click a#addSelected": "selectProfessors",
            "keyup #filter": "handleKeyUp",
            "click input[type='checkbox']": "displaySelected"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.addTitle();
            self.$el.html(self.template());

            App.ranks = App.ranks || new Models.Ranks();
            App.ranks.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    self.$("select[name='rank']").empty();
                    self.$("select[name='rank']").append("<option value=''>--</option>");
                    collection.each(function (rank) {
                        self.$("select[name='rank']").append("<option value='" + rank.get("id") + "'>" + rank.getName(App.locale) + "</option>");
                    });
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });

            // Initialize DataTable
            self.$("table#usersTable").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                }
            });
            return self;
        },

        search: function () {
            var self = this;

            function readFormValues() {
                var formData = [
                    {
                        name: "firstname",
                        value: self.$('input[name=firstname]').val()
                    },
                    {
                        name: "lastname",
                        value: self.$('input[name=lastname]').val()
                    },
                    {
                        name: "role",
                        value: self.$('select[name=role]').val()
                    },
                    {
                        name: "rank",
                        value: self.$('select[name=rank]').val()
                    },
                    {
                        name: "institution",
                        value: self.$('input[name=institution]').val()
                    },
                    {
                        name: "subject",
                        value: self.$('input[name=subject]').val()
                    }
                ];
                var user = self.$('input[name=user]').val();
                formData.push({
                    name: "user",
                    value: /^\d+$/.test(user) ? user : ""
                });
                return formData;
            }

            var searchData = readFormValues();
            // Init DataTables with a custom callback to get results
            self.$("table").dataTable().fnDestroy();
            self.$("table").dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sSearch": $.i18n.prop("dataTable_sSearch"),
                    "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                    "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                    "sInfo": $.i18n.prop("dataTable_sInfo"),
                    "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                    "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                    "oPaginate": {
                        sFirst: $.i18n.prop("dataTable_sFirst"),
                        sPrevious: $.i18n.prop("dataTable_sPrevious"),
                        sNext: $.i18n.prop("dataTable_sNext"),
                        sLast: $.i18n.prop("dataTable_sLast")
                    }
                },
                "aoColumns": [
                    {"mData": "firstname"},
                    {"mData": "lastname"},
                    {"mData": "id", "sType": "html"},
                    {"mData": "profile"},
                    {"mData": "rank", "bSortable": false},
                    {"mData": "institution", "bSortable": false},
                    {"mData": "subject", "bSortable": false},
                    {"mData": "options", "sType": "html", "bSortable": false}
                ],
                "bProcessing": true,
                "bServerSide": true,
                "sAjaxSource": self.collection.url,
                "fnServerData": function (sSource, aoData, fnCallback) {
                    /* Add some extra data to the sender */
                    $.ajax({
                        "dataType": 'json',
                        "type": "POST",
                        "url": sSource,
                        "data": aoData.concat(searchData),
                        "success": function (json) {
                            // Read Data
                            json.aaData = _.map(json.records, function (professor) {
                                return {
                                    firstname: professor.user.firstname[App.locale],
                                    lastname: professor.user.lastname[App.locale],
                                    id: '<a href="#user/' + professor.user.id + '" target="user">' + professor.user.id + '</a>',
                                    profile: $.i18n.prop(professor.discriminator),
                                    rank: professor.rank ? professor.rank.name[App.locale] : '',
                                    institution: _.isEqual(professor.discriminator, 'PROFESSOR_FOREIGN') ? professor.institution : _.templates.department(professor.department),
                                    subject: ((_.isObject(professor.subject) ? professor.subject.name : '') + ' ' + (_.isObject(professor.fekSubject) ? professor.fekSubject.name : '')).trim(),
                                    options: _.some(self.model.get("members"), function (member) {
                                        return _.isEqual(member.professor.id, professor.id);
                                    }) ? '' : '<input type="checkbox" value="' + professor.id + '" data-model-id="' + professor.id + '"/>'
                                };
                            });
                            fnCallback(json);
                            self.displaySelected();
                        }
                    });
                }
            });
        },

        displaySelected: function () {
            var self = this;
            var count = self.$("table input[type=checkbox]:checked").length;
            self.$("#displaySelected").text('(' + count + ')');
        },

        selectProfessors: function (event) {
            var self = this;
            var selectedProfessors = [];
            event.preventDefault();
            // Use dataTable to select elements, as pagination removes them from
            // DOM
            self.$("table").dataTable().$('input[type=checkbox]:checked').each(function () {
                var selectedCheckbox, id;
                selectedCheckbox = $(this);
                id = selectedCheckbox.data('modelId');
                if (!id) {
                    return;
                }
                selectedProfessors.push(id);
            });
            if (selectedProfessors.length > 0) {
                self.collection.fetch({
                    cache: false,
                    reset: true,
                    wait: true,
                    data: {
                        "prof": selectedProfessors
                    },
                    processData: true,
                    success: function (collection) {
                        self.collection.trigger("selected", collection);
                    }
                });
            }

        },

        handleKeyUp: function (event) {
            var self = this;
            if (event.keyCode === 13) {
                // On enter submit
                self.search();
            }
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * ProfessorListView *******************************************************
     **************************************************************************/

    Views.ProfessorListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "showDetails", "select");
            this.template = _.template(tpl_professor_list);
            this.collection.bind("change", this.render, this);
            this.collection.bind("reset", this.render, this);
        },

        events: {
            "click a#select": "select"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                professors: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var item = model.toJSON();
                        item.cid = model.cid;
                        result.push(item);
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            return self;
        },

        showDetails: function (event, professor) {
            var self = this;
            var selectedModel = professor || self.collection.get($(event.currentTarget).data('modelCid'));
            if (selectedModel) {
                self.collection.trigger("professor:selected", professor);
            }
        },

        select: function (event, professor) {
            var selectedModel = professor || this.collection.get($(event.currentTarget).data('modelCid'));
            if (selectedModel) {
                this.collection.trigger("role:selected", selectedModel);
            }
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * ProfessorCommitteesView *************************************************
     **************************************************************************/
    Views.ProfessorCommitteesView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "select");
            self.template = _.template(tpl_professor_committees);
            self.collection.bind('reset', self.render, self);
        },

        events: {
            "click a#select": "select"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template({
                committees: self.collection.toJSON()
            }));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            return self;
        },

        select: function (eventPositionCommitteeRegisterMembers, positionCommitteeMember) {
            var self = this;
            var selectedModel = positionCommitteeMember || self.collection.get($(event.currentTarget).data('committeeMemberId'));
            if (selectedModel) {
                self.collection.trigger("positionCommitteeMember:selected", selectedModel);
            }
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind('reset', this.render, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * ProfessorEvaluationsView ************************************************
     **************************************************************************/
    Views.ProfessorEvaluationsView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_professor_evaluations);
            self.options.positionEvaluations.bind('reset', self.render, self);
            self.options.candidacyEvaluations.bind('reset', self.render, self);
        },

        events: {
            "click a#select": "select",
            "click #candidacyStatusActionsHistory" : "candidacyStatusActionsHistory"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();

            self.$el.append(self.template({
                positionEvaluations: self.options.positionEvaluations.toJSON(),
                candidacyEvaluations: self.options.candidacyEvaluations.toJSON()
            }));

            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            return self;
        },

        candidacyStatusActionsHistory: function (event) {
            var candidacyId = $(event.currentTarget).data('candidacyId');
            var candidacyStatusModelList = new Models.CandidacyStatusList({},{
                candidacyId: candidacyId
            });
            candidacyStatusModelList.fetch({
                cache: false,
                wait: true,
                success: function (collection) {
                    var confirm = new Views.CandidateCandidacyActionsHistoryView({
                        title: $.i18n.prop('actionsHistoryTitle'),
                        statusHistoryList: collection.toJSON()
                    });
                    confirm.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            this.options.positionEvaluations.unbind('reset', self.render, self);
            this.options.candidacyEvaluations.unbind('reset', self.render, self);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * InstitutionRegulatoryFrameworkListView **********************************
     **************************************************************************/
    Views.InstitutionRegulatoryFrameworkListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "renderActions", "select", "create");
            self.template = _.template(tpl_institution_regulatory_framework_list);
            self.collection.bind('reset', self.render, self);
            self.collection.bind('add', self.render, self);
            self.collection.bind('remove', self.render, self);
        },

        events: {
            "click a#selectInstitutionRF": "select",
            "click a#createInstitutionRF": "create"
        },

        render: function () {
            var self = this;
            var tpl_data = {
                institutionRFs: (function () {
                    var result = [];
                    self.collection.each(function (model) {
                        var canEdit = model.isEditableBy(App.loggedOnUser);
                        var item;
                        if (model.has("id")) {
                            item = model.toJSON();
                            item.cid = model.cid;
                            item.canEdit = canEdit;
                            result.push(item);
                        }
                    });
                    return result;
                }())
            };
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(tpl_data));

            // Widgets
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            // Add Actions
            self.renderActions();
            return self;
        },

        renderActions: function () {
            var self = this;
            if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
                return;
            }
            if (self.collection.any(function (register) {
                    return App.loggedOnUser.isAssociatedWithInstitution(register.get("institution"));
                })) {
                return;
            }
            self.$("#actions").append("<div class=\"btn-group\"><input type=\"hidden\" name=\"institution\" /><a id=\"createInstitutionRF\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_institutionrf') + " </a></div>");
            // Add institutions in selector:
            App.institutions = App.institutions || new Models.Institutions();
            App.institutions.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    var institution = collection.find(function (institution) {
                        return App.loggedOnUser.isAssociatedWithInstitution(institution);
                    });
                    self.$("#actions input[name=institution]").val(institution.get("id"));
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        select: function (event, institutionRF) {
            var self = this;
            var selectedModel = institutionRF || self.collection.get($(event.currentTarget).data('institutionrfCid'));
            if (selectedModel) {
                self.collection.trigger("institutionRF:selected", selectedModel);
            }
        },

        create: function () {
            var self = this;
            var newIRF = new Models.InstitutionRegulatoryFramework();
            newIRF.save({
                institution: {
                    id: self.$("input[name='institution']").val()
                }
            }, {
                wait: true,
                success: function () {
                    self.collection.add(newIRF);
                    self.select(undefined, newIRF);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind("reset");
            this.collection.unbind("add");
            this.collection.unbind("remove");
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * InstitutionRegulatoryFrameworkEditView **********************************
     **************************************************************************/
    Views.InstitutionRegulatoryFrameworkEditView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(this, "change", "submit", "cancel", "remove");
            self.template = _.template(tpl_institution_regulatory_framework_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#cancel": "cancel",
            "click a#remove": "remove",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        render: function () {
            var self = this;
            var propName;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));
            self.validator = $("form", this.el).validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {
                    "organismosURL": {
                        "required": true,
                        "url": true
                    },
                    "eswterikosKanonismosURL": {
                        "required": true,
                        "url": true
                    }
                },
                messages: {
                    "organismosURL": $.i18n.prop('validation_organismosURL'),
                    "eswterikosKanonismosURL": $.i18n.prop('validation_eswterikosKanonismosURL')
                }
            });
            // Highlight Required
            if (self.validator) {
                for (propName in self.validator.settings.rules) {
                    if (self.validator.settings.rules.hasOwnProperty(propName)) {
                        if (self.validator.settings.rules[propName].required !== undefined) {
                            self.$("label[for=" + propName + "]").addClass("strong");
                        }
                    }
                }
            }
            // Disable Save Button until user changes a field,
            // don't for non-permanent
            if (self.model.get("permanent")) {
                self.$("a#save").attr("disabled", true);
            }

            return self;
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        submit: function (event) {
            var self = this;
            var values = {};
            // Read Input
            values.organismosURL = self.$('form input[name=organismosURL]').val();
            values.eswterikosKanonismosURL = self.$('form input[name=eswterikosKanonismosURL]').val();
            // Save to model
            self.model.save(values, {
                wait: true,
                success: function () {
                    var popup;
                    App.router.navigate("regulatoryframeworks/" + self.model.id, {
                        trigger: false
                    });
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        cancel: function () {
            var self = this;
            if (self.validator) {
                self.validator.resetForm();
            }
            self.render();
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSure'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup;
                            App.router.navigate("regulatoryframeworks", {
                                trigger: false
                            });
                            popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind("change");
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * InstitutionRegulatoryFrameworkView **************************************
     **************************************************************************/
    Views.InstitutionRegulatoryFrameworkView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_institution_regulatory_framework);
            self.model.bind('change', self.render, self);
        },

        events: {},

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));
            return self;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind("change");
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionSearchCriteriaView **********************************************
     **************************************************************************/
    Views.PositionSearchCriteriaView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "change", "renderDepartments", "renderSectors", "readValues", "search", "submit");
            self.template = _.template(tpl_position_search_criteria);
            self.model.bind('change', self.render, self);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#addDepartment": "addDepartment",
            "click a#removeDepartment": "removeDepartment",
            "click a#addSubject": "addSubject",
            "click a#removeSubject": "removeSubject",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit",
            "click a#search": "search"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));
            // Add Departments to selector:
            App.departments = App.departments || new Models.Departments();
            App.departments.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    self.renderDepartments(collection);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            // Add Sectors
            App.sectors = App.sectors || new Models.Sectors();
            App.sectors.fetch({
                cache: true,
                reset: true,
                success: function (collection) {
                    self.renderSectors(collection);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });

            // Disable Save Button until user changes a field,
            self.$("a#save").attr("disabled", true);

            return self;
        },

        renderDepartments: function (departments) {
            var self = this;
            var treeData = departments.reduce(function (memo, department) {
                var institution_node, school_node;
                var school = department.get("school");
                var institution = school.institution;
                if (institution.category === 'RESEARCH_CENTER') {
                    // Skip research centers
                    return memo;
                }
                // 1. Create/Find InstitutionNode
                institution_node = _.find(memo, function (item) {
                    return item.key === institution.id;
                });
                if (!institution_node) {
                    institution_node = {
                        type: 'institution',
                        title: institution.name[App.locale],
                        key: institution.id,
                        tooltip: $.i18n.prop("Institution"),
                        expand: false,
                        isFolder: true,
                        unselectable: false,
                        children: []
                    };
                    memo.push(institution_node);
                }
                // 2. Create/Find SchoolNode in InstitutionNode's children
                school_node = _.find(institution_node.children, function (item) {
                    return item.key === school.id;
                });
                if (!school_node) {
                    school_node = {
                        type: 'school',
                        title: school.name[App.locale],
                        key: school.id,
                        tooltip: $.i18n.prop("School"),
                        expand: false,
                        isFolder: true,
                        unselectable: false,
                        hideCheckbox: (school.name[App.locale] === '-'),
                        children: []
                    };
                    institution_node.children.push(school_node);
                }
                // 3. Create DepartmentNode in SchoolNode's Children
                school_node.children.push({
                    type: 'department',
                    title: department.getName(App.locale),
                    key: department.get("id"),
                    tooltip: $.i18n.prop("Department"),
                    hideCheckbox: (department.getName(App.locale) === '-'),
                    select: _.any(self.model.get("departments"), function (selectedDepartment) {
                        return _.isEqual(selectedDepartment.id, department.get("id"));
                    })
                });
                // Return memo
                return memo;
            }, []);

            self.$("#departmentsTree").dynatree({
                checkbox: true,
                selectMode: 3,
                children: treeData,
                onSelect: function (flag, node) {
                    var selectedNodes = node.tree.getSelectedNodes();
                    var count = _.countBy(selectedNodes, function (selectedNode) {
                        return selectedNode.data.type;
                    });
                    self.change($.Event(), {
                        triggredBy: "user"
                    });
                    self.$("label[for=departmentsTree] span").html(count.department || 0);
                },
                onPostInit: function () {
                    var selectedNodes = this.getSelectedNodes();
                    var count = _.countBy(selectedNodes, function (selectedNode) {
                        return selectedNode.data.type;
                    });
                    self.$("label[for=departmentsTree] span").html(count.department || 0);
                }
            });
        },

        renderSectors: function (sectors) {
            var self = this;
            var treeData = sectors.reduce(function (memo, sector) {
                var sectorId = sector.get("id");
                var areaId = sector.get("areaId");
                var areaName = sector.get("name")[App.locale].area;
                var node = _.find(memo, function (item) {
                    return item.key === areaId;
                });
                if (!node) {
                    node = {
                        title: areaName,
                        key: areaId,
                        expand: false,
                        isFolder: true,
                        unselectable: false,
                        children: []
                    };
                    memo.push(node);
                }
                node.children.push({
                    title: sector.get("name")[App.locale].subject,
                    key: sectorId,
                    select: _.any(self.model.get("sectors"), function (selectedSector) {
                        return _.isEqual(selectedSector.id, sectorId);
                    })
                });
                return memo;
            }, []);

            self.$("#sectorsTree").dynatree({
                checkbox: true,
                selectMode: 3,
                children: treeData,
                onSelect: function (flag, node) {
                    var selectedNodes = node.tree.getSelectedNodes();
                    var count = _.countBy(selectedNodes, function (selectedNode) {
                        return selectedNode.data.isFolder ? 'area' : 'subject';
                    });
                    self.change($.Event(), {
                        triggredBy: "user"
                    });
                    self.$("label[for=sectorsTree] span").html(count.subject || 0);
                },
                onPostInit: function () {
                    var selectedNodes = this.getSelectedNodes();
                    var count = _.countBy(selectedNodes, function (selectedNode) {
                        return selectedNode.data.isFolder ? 'area' : 'subject';
                    });
                    self.$("label[for=sectorsTree] span").html(count.subject || 0);
                }
            });
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        readValues: function () {
            var self = this;
            return {
                departments: _.map(_.filter(self.$("#departmentsTree").dynatree("getTree").getSelectedNodes(), function (node) {
                    return node.data.type === 'department';
                }), function (node) {
                    return {
                        id: node.data.key
                    };
                }),
                sectors: _.map(_.filter(self.$("#sectorsTree").dynatree("getTree").getSelectedNodes(), function (node) {
                    return !node.data.isFolder;
                }), function (node) {
                    return {
                        id: node.data.key
                    };
                })
            };
        },

        search: function () {
            var self = this;
            self.model.trigger("criteria:search", self.readValues());
        },

        submit: function (event) {
            var self = this;
            // Save to model
            self.model.save(self.readValues(), {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                    self.$("a#save").attr("disabled", true);
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        close: function () {
            this.closeInnerViews();
            this.model.unbind('change', this.render, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * PositionSearchResultView ************************************************
     **************************************************************************/
    Views.PositionSearchResultView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            self.template = _.template(tpl_position_search_result);
            self.collection.bind('reset', self.render, self);
        },

        events: {
            "click a#selectPosition": "select"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template({
                positions: self.collection.toJSON()
            }));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    },
                    "aoColumnDefs": [
                        {"sType": "date-eu", "aTargets": [6, 7]}
                    ]
                });
            }
            return self;
        },

        select: function (event, position) {
            var self = this;
            var selectedModel = position || self.collection.get($(event.currentTarget).data('positionId'));
            self.collection.trigger("position:selected", selectedModel);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind('reset', this.render, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * CandidateCandidacyListView **********************************************
     **************************************************************************/
    Views.CandidateCandidacyListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "select");
            self.template = _.template(tpl_candidate_candidacy_list);
            self.collection.bind('reset', self.render, self);
            self.collection.bind('remove', self.render, self);
            self.collection.bind('add', self.render, self);
        },

        events: {
            "click a#selectCandidacy": "select"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template({
                candidacies: self.collection.toJSON()
            }));
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }
            return self;
        },

        select: function (event, candidacy) {
            var self = this;
            var selectedModel = candidacy || self.collection.get($(event.currentTarget).data('candidacyId'));
            self.collection.trigger("candidacy:selected", selectedModel);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind('reset', this.render, this);
            this.collection.unbind('remove', this.render, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * CandidacyEditView *******************************************************
     **************************************************************************/
    Views.CandidacyEditView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;

            self._super('initialize', [options]);
            _.bindAll(this, "renderRegisterMembers", "change", "isEditable", "isEnabled", "submit", "cancel");
            self.template = _.template(tpl_candidacy_edit);
            self.model.bind('change', self.render, self);
            self.model.bind("destroy", self.close, self);

            // Initialize Registers, no request is performed until render
            self.registerMembers = new Models.CandidacyRegisterMembers();
            self.registerMembers.url = self.model.url() + "/register";
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#cancel": "cancel",
            "click a#remove": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                this.remove();
            },
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        isEditable: function (field) {
            var self = this;
            switch (field) {
                case "openToOtherCandidates":
                    return _.isEqual(self.model.get("candidacies").position.phase.clientStatus, "ANOIXTI");
                case "ekthesiAutoaksiologisisFile":
                    return _.isEqual(self.model.get("candidacies").position.phase.clientStatus, "ANOIXTI");
                case "evaluator":
                    return self.model.get("canAddEvaluators");
                case "sympliromatikaEggrafaFileList":
                    return !self.model.get("nominationCommitteeConverged");
                default:
                    break;
            }
            // We return true by default here, so that input elements in datatable are not disables
            // This requires that all fields in form are listed in the switch above
            return true;
        },

        isEnabled: function (buttonType) {
            var self = this;
            switch (buttonType) {
                case "save":
                    return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
                case "remove":
                    return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI") ||
                        _.isEqual(self.model.get("candidacies").position.phase.status, "EPILOGI");
                case "toggleEdit":
                case "clear":
                case "searchEvaluator":
                case "selectEvaluator":
                    return self.model.get("canAddEvaluators");
                default:
                    break;
            }
            return false;
        },

        render: function () {
            var self = this;
            var files;
            var sfiles;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();

            self.$el.append(self.template(self.model.toJSON()));

            // Fill Selectors with registerMembers
            self.renderRegisterMembers();

            // Files
            if (self.model.has("id")) {
                // Snapshot Files
                sfiles = new Models.Files();
                sfiles.url = self.model.url() + "/snapshot/file";
                sfiles.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
                            withMetadata: false
                        });
                        self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
                            withMetadata: true
                        });
                        self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
                            withMetadata: true
                        });
                    }
                });
                // Candidacy Files
                files = new Models.Files();
                files.url = self.model.url() + "/file";
                files.fetch({
                    cache: false,
                    success: function (collection) {
                        self.addFileEdit(collection, "EKTHESI_AUTOAKSIOLOGISIS", self.$("input[name=ekthesiAutoaksiologisisFile]"), {
                            withMetadata: true,
                            editable: self.isEditable("ekthesiAutoaksiologisisFile")
                        });
                        self.addFileListEdit(collection, "SYMPLIROMATIKA_EGGRAFA", self.$("input[name=sympliromatikaEggrafaFileList]"), {
                            withMetadata: true,
                            editable: self.isEditable("sympliromatikaEggrafaFileList")
                        });

                    }
                });
            } else {
                self.$("#mitrooFileList").html($.i18n.prop("PressSave"));
            }

            // Set isEditable to fields
            self.$("select, input, textarea").each(function () {
                var field = $(this).attr("name");
                if (self.isEditable(field)) {
                    $(this).removeAttr("disabled");
                } else {
                    $(this).attr("disabled", true);
                }
            });
            // Validator
            self.validator = self.$("form").validate({
                errorElement: "span",
                errorClass: "help-inline",
                highlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").addClass("error");
                },
                unhighlight: function (element) {
                    $(element).parent(".controls").parent(".control-group").removeClass("error");
                },
                rules: {},
                messages: {}
            });
            // Tooltips
            self.$("i[rel=popover]").popover({
                html: 'true',
                trigger: 'hover'
            });
            // Set isEnabled to buttons
            self.$("a.btn").each(function () {
                var buttonType = $(this).attr("id");
                if (self.isEnabled(buttonType)) {
                    $(this).removeAttr("disabled");
                } else {
                    $(this).attr("disabled", true);
                }
            });
            // Disable Save Button until user changes a field,
            // don't for non-permanent
            if (self.model.get("permanent")) {
                self.$("a#save").attr("disabled", true);
            }
            // Hide remove button, until user presses first save
            if (!self.model.get("permanent")) {
                self.$("a#remove").hide();
            }

            return self;
        },

        renderRegisterMembers: function () {
            var self = this;
            var evaluator0_SelectView = new Views.EvaluatorSelectView({
                el: self.$("input[name=evaluator_0]"),
                collection: self.registerMembers,
                editable: self.isEditable("evaluator"),
                specificEvaluator: self.model.get("proposedEvaluators")[0]
            });
            var evaluator1_SelectView = new Views.EvaluatorSelectView({
                el: self.$("input[name=evaluator_1]"),
                collection: self.registerMembers,
                editable: self.isEditable("evaluator"),
                proposedEvaluators: self.model.get("proposedEvaluators"),
                specificEvaluator: self.model.get("proposedEvaluators")[1]
            });

            evaluator0_SelectView.render();
            evaluator1_SelectView.render();
        },

        change: function (event, data) {
            var self = this;
            if ((data && _.isEqual(data.triggeredBy, "application")) || $(event.currentTarget).attr('type') === 'hidden') {
                return;
            }
            self.$("a#save").removeAttr("disabled");
        },

        submit: function (event) {
            var self = this;
            var values = {};
            // Read Input
            values.openToOtherCandidates = self.$('form input[name=openToOtherCandidates]').is(':checked');
            values.proposedEvaluators = [
                {
                    registerMember: {
                        id: self.$('form input[name=evaluator_0]').val()
                    }
                },
                {
                    registerMember: {
                        id: self.$('form input[name=evaluator_1]').val()
                    }
                }
            ];
            // Save to model
            self.model.save(values, {
                wait: true,
                success: function () {
                    var popup;
                    App.router.navigate("candidateCandidacies/" + self.model.id, {
                        trigger: false
                    });
                    popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            event.preventDefault();
            return false;
        },

        cancel: function () {
            var self = this;
            self.render();
        },

        remove: function () {
            var self = this;
            var confirm = new Views.ConfirmWithdrawCandidacyView({
                title: $.i18n.prop('Confirm'),
                message: $.i18n.prop('AreYouSureToWithdrawCandidacyMessage'),
                messageDetails: $.i18n.prop('AreYouSureToWithdrawCandidacyMessageDetails'),
                yes: function () {
                    self.model.destroy({
                        wait: true,
                        success: function () {
                            var popup;
                            App.router.navigate("candidateCandidacies", {
                                trigger: false
                            });
                            popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
                }
            });
            confirm.show();
            return false;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * CandidacyView ***********************************************************
     **************************************************************************/
    Views.CandidacyView = Views.BaseView.extend({
        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_candidacy);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {},

        render: function () {
            var self = this;
            var files;
            var sfiles;
            var efiles;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template(self.model.toJSON()));

            // Snapshot Files
            sfiles = new Models.Files();
            sfiles.url = self.model.url() + "/snapshot/file";
            sfiles.fetch({
                cache: false,
                success: function (collection) {
                    self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
                        withMetadata: false
                    });
                    self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
                        withMetadata: true
                    });
                    self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
                        withMetadata: true
                    });
                }
            });
            // Candidacy Files
            files = new Models.Files();
            files.url = self.model.url() + "/file";
            files.fetch({
                cache: false,
                success: function (collection) {
                    self.addFile(collection, "EKTHESI_AUTOAKSIOLOGISIS", self.$("#ekthesiAutoaksiologisisFile"), {
                        withMetadata: true
                    });
                    self.addFileList(collection, "SYMPLIROMATIKA_EGGRAFA", self.$("#sympliromatikaEggrafaFileList"), {
                        withMetadata: true
                    });
                }
            });

            // Evaluation Files (only if proposed Evaluators is defined and not empty)
            if (self.model.has("proposedEvaluators") && self.model.get("proposedEvaluators").length > 0) {
                efiles = new Models.Files();
                efiles.url = self.model.url() + "/evaluation/file";
                efiles.fetch({
                    cache: false,
                    success: function (collection) {
                        _.each(self.model.get("proposedEvaluators"), function (proposedEvaluator) {
                            var filteredFiles = new Models.Files(collection.filter(function (file) {
                                return file.get("evaluator").id === proposedEvaluator.id;
                            }));
                            filteredFiles.url = collection.url;
                            self.addFileList(filteredFiles, "EISIGISI_DEP_YPOPSIFIOU",
                                self.$("div#eisigisiDepYpopsifiouFileList[data-candidacy-evaluator-id=" + proposedEvaluator.id + "]"), {
                                    withMetadata: true
                                });
                        });
                    }
                });
            }

            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * CandidacyUpdateConfirmView **********************************************
     **************************************************************************/
    Views.CandidacyUpdateConfirmView = Views.BaseView.extend({
        tagName: "div",

        className: "modal",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show");
            this.template = _.template(tpl_candidacy_update_confirm);
        },

        events: {
            "click a#yes": function () {
                this.$el.modal('hide');
                if (_.isFunction(this.options.answer)) {
                    this.options.answer(true);
                }
            },
            "click a#no": function () {
                this.$el.modal('hide');
                if (_.isFunction(this.options.answer)) {
                    this.options.answer(false);
                }
            }
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.append(self.template({
                candidacies: self.collection.toJSON()
            }));
        },
        show: function () {
            this.render();
            this.$el.modal();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * JiraIssueView ***********************************************************
     **************************************************************************/
    Views.JiraIssueView = Views.BaseView.extend({

        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_jira_issue);
        },

        events: {},

        render: function () {
            var self = this;
            // 1. Render
            self.$el.html(self.template(self.model.toJSON()));
            // 2. Return
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * JiraIssueEditView *******************************************************
     **************************************************************************/
    Views.JiraIssueEditView = Views.BaseView.extend({

        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "submit");
            this.template = _.template(tpl_jira_issue_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        validatorRules: function () {
            return {
                errorElement: "span",
                errorClass: "text-error",
                rules: {
                    call: "required",
                    role: "required",
                    type: "required",
                    fullname: "required",
                    mobile: {
                        required: true,
                        number: true,
                        minlength: 10
                    },
                    email: {
                        required: true,
                        email: true,
                        minlength: 2
                    },
                    summary: "required",
                    description: {
                        required: true,
                        maxlength: 10000
                    }
                },
                messages: {
                    call: $.i18n.prop('validation_required'),
                    role: $.i18n.prop('validation_required'),
                    type: $.i18n.prop('validation_required'),
                    fullname: $.i18n.prop('validation_required'),
                    mobile: {
                        required: $.i18n.prop('validation_mobile'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_minlength', 10)
                    },
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email'),
                        minlength: $.i18n.prop('validation_minlength', 2)
                    },
                    summary: $.i18n.prop('validation_required'),
                    description: {
                        required: $.i18n.prop('validation_required'),
                        maxlength: $.i18n.prop('validation_jira_description')
                    }
                }
            };
        },

        render: function () {
            var self = this;
            var propName;
            self.closeInnerViews();
            // 1. Render
            self.$el.html(self.template(self.model.toJSON()));
            // 2. Set values on select items
            self.$("select[name=call]").val(self.model.get("call"));
            self.$("select[name=role]").val(self.model.get("role"));
            self.$("select[name=type]").val(self.model.get("type"));
            // 3. Init plugins
            self.validator = self.$("form").validate(self.validatorRules());
            for (propName in self.validator.settings.rules) {
                if (self.validator.settings.rules.hasOwnProperty(propName)) {
                    if (self.validator.settings.rules[propName].required) {
                        self.$("label[for=" + propName + "]").addClass("strong");
                    }
                }
            }
            // 4. Return
            return self;
        },

        submit: function () {
            var self = this;
            // Read Input
            var call = self.$('form select[name=call]').val();
            var role = self.$('form select[name=role]').val();
            var type = self.$('form select[name=type]').val();
            var fullname = self.$('form input[name=fullname]').val();
            var mobile = self.$('form input[name=mobile]').val();
            var email = self.$('form input[name=email]').val();
            var summary = self.$('form input[name=summary]').val();
            var description = self.$('form textarea[name=description]').val();

            // Save to model
            self.model.save({
                call: call,
                role: role,
                type: type,
                fullname: fullname,
                mobile: mobile,
                email: email,
                summary: summary,
                description: description
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop("Success")
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            return false;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * PublicJiraIssueEditView *************************************************
     **************************************************************************/
    Views.PublicJiraIssueEditView = Views.BaseView.extend({

        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "submit");
            this.template = _.template(tpl_jira_issue_public_edit);
            this.model.bind('change', this.render, this);
            this.model.bind("destroy", this.close, this);
        },

        events: {
            "change select,input:not([type=file]),textarea": "change",
            "click a#save": function (event) {
                if ($(event.currentTarget).attr("disabled")) {
                    event.preventDefault();
                    return;
                }
                $("form", this.el).submit();
            },
            "submit form": "submit"
        },

        validatorRules: function () {
            return {
                errorElement: "span",
                errorClass: "text-error",
                rules: {
                    call: "required",
                    role: "required",
                    type: "required",
                    fullname: "required",
                    mobile: {
                        required: true,
                        number: true,
                        minlength: 10
                    },
                    email: {
                        required: true,
                        email: true,
                        minlength: 2
                    },
                    summary: "required",
                    description: "required"
                },
                messages: {
                    call: $.i18n.prop('validation_required'),
                    role: $.i18n.prop('validation_required'),
                    type: $.i18n.prop('validation_required'),
                    fullname: $.i18n.prop('validation_required'),
                    mobile: {
                        required: $.i18n.prop('validation_mobile'),
                        number: $.i18n.prop('validation_number'),
                        minlength: $.i18n.prop('validation_minlength', 10)
                    },
                    email: {
                        required: $.i18n.prop('validation_email'),
                        email: $.i18n.prop('validation_email'),
                        minlength: $.i18n.prop('validation_minlength', 2)
                    },
                    summary: $.i18n.prop('validation_required'),
                    description: $.i18n.prop('validation_required')
                }
            };
        },

        render: function () {
            var self = this;
            var propName;
            self.closeInnerViews();
            // 1. Render
            self.$el.html(self.template(self.model.toJSON()));
            // 2. Set values on select items
            self.$("select[name=call]").val(self.model.get("call"));
            self.$("select[name=role]").val(self.model.get("role"));
            self.$("select[name=type]").val(self.model.get("type"));
            // 3. Init plugins
            self.validator = self.$("form").validate(self.validatorRules());
            for (propName in self.validator.settings.rules) {
                if (self.validator.settings.rules.hasOwnProperty(propName)) {
                    if (self.validator.settings.rules[propName].required) {
                        self.$("label[for=" + propName + "]").addClass("strong");
                    }
                }
            }
            // 4. Return
            return self;
        },

        submit: function () {
            var self = this;
            // Read Input
            var call = self.$('form select[name=call]').val();
            var role = self.$('form select[name=role]').val();
            var type = self.$('form select[name=type]').val();
            var fullname = self.$('form input[name=fullname]').val();
            var mobile = self.$('form input[name=mobile]').val();
            var email = self.$('form input[name=email]').val();
            var summary = self.$('form input[name=summary]').val();
            var description = self.$('form textarea[name=description]').val();

            // Save to model
            self.model.save({
                call: call,
                role: role,
                type: type,
                fullname: fullname,
                mobile: mobile,
                email: email,
                summary: summary,
                description: description
            }, {
                wait: true,
                success: function () {
                    var popup = new Views.PopupView({
                        type: "success",
                        message: $.i18n.prop('JiraIssueSubmitSuccess') + '<br/><br/>' + $.i18n.prop('GoToApellaPortalText')
                    });
                    popup.show();
                },
                error: function (model, resp) {
                    var popup = new Views.PopupView({
                        type: "error",
                        message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                    });
                    popup.show();
                }
            });
            return false;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /**************************************************************************
     * JiraIssueListView ******************************************************
     **************************************************************************/
    Views.JiraIssueListView = Views.BaseView.extend({
        tagName: "div",

        initialize: function (options) {
            var self = this;
            this._super('initialize', [options]);
            _.bindAll(self, "showIssue", "showCreateIssue");
            self.template = _.template(tpl_jira_issue_list);

            self.collection.bind('reset', self.render, self);
            self.collection.bind('add', self.render, self);
            self.collection.bind('remove', self.render, self);
        },

        events: {
            "click a#select": "showIssue",
            "click a#createIssue": "showCreateIssue"
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.addTitle();

            // Render template
            self.$el.html(self.template({
                issues: self.collection.toJSON()
            }));

            // Init plugins
            if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                self.$("table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    }
                });
            }

            return self;
        },

        showIssue: function (event, issue) {
            var self = this;
            var selectedModel = issue || self.collection.get($(event.currentTarget).data('issueId'));
            var view = new Views.JiraIssueView({
                model: selectedModel
            });
            self.closeInnerViews();
            self.$("#issue").html(view.render().el);
        },

        showCreateIssue: function () {
            var self = this;
            var view;
            var model = new Models.JiraIssue({
                status: "OPEN",
                role: App.loggedOnUser.get('primaryRole'),
                fullname: App.loggedOnUser.get('firstname')[App.locale] + ' ' + App.loggedOnUser.get('lastname')[App.locale],
                mobile: App.loggedOnUser.get('contactInfo').mobile,
                email: App.loggedOnUser.get('contactInfo').email
            });
            model.url = model.urlRoot + "user/" + App.loggedOnUser.get("id") + "/issue";
            view = new Views.JiraIssueEditView({
                model: model
            });
            model.on("sync", function () {
                self.collection.add(model);
                view.close();
            });

            self.closeInnerViews();
            self.$("#issue").html(view.render().el);
        },

        close: function () {
            this.closeInnerViews();
            this.collection.unbind('add', this.render, this);
            this.collection.unbind('reset', this.render, this);
            this.collection.unbind('remove', this.render, this);
            this.$el.unbind();
            this.$el.remove();
        }
    });

    /***************************************************************************
     * DataExportsView *********************************************************
     **************************************************************************/
    Views.DataExportsView = Views.BaseView.extend({

        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_data_exports);
        },

        events: {},

        render: function () {
            var self = this;
            // 1. Render
            self.$el.html(self.template({
                urlPrefix: '/dep/rest/exports',
                types: [
                    "institution-manager",
                    "institution-assistant",
                    "professor-domestic",
                    "professor-foreign",
                    "candidate",
                    "institution-regulatory-framework",
                    "register",
                    "register-member",
                    "position-evaluator",
                    "position-committee-member",
                    "candidacy"
                ],
                urlSuffix: '?X-Auth-Token=' + encodeURIComponent(App.authToken)
            }));
            // 2. Return
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * StatisticsView *********************************************************
     **************************************************************************/
    Views.StatisticsView = Views.BaseView.extend({

        tagName: "div",

        validator: undefined,

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_statistics);
        },

        events: {},

        render: function () {
            var self = this;
            var tplData = self.model.toJSON();
            // 1. Render
            self.$el.html(self.template(self.model.toJSON()));
            // 2. Return
            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * CandidaciesAdminListView ************************************************
     **************************************************************************/
    Views.AdminCandidacyListView = Views.BaseView.extend({
            tagName: "div",

            validator: undefined,

            initialize: function (options) {
                var self = this;
                self._super('initialize', [options]);
                _.bindAll(self, "submitCandidacy", "toggleDisplayForm");
                self.template = _.template(tpl_incomplete_candidacy_list);
            },

            events: {
                "click a#displayForm": "toggleDisplayForm",
                "click a#save": function (event) {
                    $("form", this.el).submit();
                },
                "click a#submit": "submitCandidacy",
                "submit form": "submit"
            },

            render: function () {
                var self = this;
                self.closeInnerViews();
                self.$el.empty();
                self.$el.append(self.template({
                    candidacies: self.collection.toJSON()
                }));
                if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
                    self.$("table").dataTable({
                        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                        "sPaginationType": "bootstrap",
                        "aaSorting": [[6, "asc"]],
                        "oLanguage": {
                            "sSearch": $.i18n.prop("dataTable_sSearch"),
                            "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                            "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                            "sInfo": $.i18n.prop("dataTable_sInfo"),
                            "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                            "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                            "oPaginate": {
                                sFirst: $.i18n.prop("dataTable_sFirst"),
                                sPrevious: $.i18n.prop("dataTable_sPrevious"),
                                sNext: $.i18n.prop("dataTable_sNext"),
                                sLast: $.i18n.prop("dataTable_sLast")
                            }
                        }
                    });
                }
                self.validator = $("form#createCandidacyForm", this.el).validate({
                    errorElement: "span",
                    errorClass: "help-inline",
                    highlight: function (element) {
                        // $(element).addClass("error");
                        $(element).parent(".controls").parent(".control-group").addClass("error");
                    },
                    unhighlight: function (element) {
                        //$(element).removeClass("error");
                        $(element).parent(".controls").parent(".control-group").removeClass("error");
                    },
                    rules: {
                        userId: "required",
                        positionId: "required"
                    },
                    messages: {
                        userId: $.i18n.prop('validation_required'),
                        positionId: $.i18n.prop('validation_required')
                    }
                });

                if (self.validator) {
                    var propName;
                    for (propName in self.validator.settings.rules) {
                        if (self.validator.settings.rules.hasOwnProperty(propName)) {
                            if (self.validator.settings.rules[propName].required) {
                                self.$("label[for=" + propName + "]").addClass("strong");
                            }
                        }
                    }
                }

                return self;
            },

            toggleDisplayForm: function () {
                if ($('form#createCandidacyForm').hasClass('hidden')) {
                    $('form#createCandidacyForm').removeClass('hidden');
                } else {
                    $('form#createCandidacyForm').addClass('hidden');
                }
            },

            submitCandidacy: function (event) {
                var self = this;
                var selectedCandidacy = new Models.Candidacy();
                selectedCandidacy.url = selectedCandidacy.url() + '/submitcandidacy';
                var positionId = $(event.currentTarget).data('positionId');
                var userId = $(event.currentTarget).data('userId');

                if (positionId === null || positionId === undefined) {
                    positionId = self.$('form input[name=positionId]').val();
                }

                if (userId === null || userId === undefined) {
                    userId = self.$('form input[name=userId]').val();
                }
                selectedCandidacy.save({
                        candidacies: {
                            position: {
                                id: positionId
                            }
                        },
                        candidate: {
                            discriminator: "CANDIDATE",
                            user: {
                                id: userId
                            }
                        }
                    },
                    {
                        wait: true,
                        success: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "success",
                                message: $.i18n.prop("Success")
                            });
                            popup.show();
                            var rowId = resp.candidate.user.id + '_' + resp.candidacies.position.id;
                            if (($('#incompleteCandidaciesTable').find('#' + rowId)).length > 0) {
                                self.collection.remove($('#incompleteCandidaciesTable').find('#' + rowId).data('candidacyId'));
                                self.render();
                            }
                        },
                        error: function (model, resp) {
                            var popup = new Views.PopupView({
                                type: "error",
                                message: $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
                            });
                            popup.show();
                        }
                    });
            },

            submit: function (event) {
                this.submitCandidacy(event);
            },

            close: function () {
                this.closeInnerViews();
                this.$el.unbind();
                this.$el.remove();
            }
        }
    );

    /***************************************************************************
     * EvaluatorSelectView ****************************************************
     **************************************************************************/
    Views.EvaluatorSelectView = Views.BaseView.extend({

        initialize: function (options) {
            var self = this;
            self._super('initialize', [options]);
            _.bindAll(self, "onToggleEdit", "onSelectEvaluator", "toggleEdit", "select", "clear");
            self.template = _.template(tpl_evaluator_select);
            self.collection.bind("reset", self.render, self);
            self.collection.bind("add", self.render, self);

            self.specificEvaluator = self.options.specificEvaluator;
            self.$input = $(self.el);
            self.$input.before("<div id=\"" + self.$input.attr("name") + "\"></div>");
            self.setElement(self.$input.prev("#" + self.$input.attr("name")));

            self.emptyModel = new Models.RegisterMember({
                id: undefined,
                professor: {
                    user: {
                        firstname: {
                            el: '',
                            en: ''
                        },
                        lastname: {
                            el: '',
                            en: ''
                        }
                    },
                    discriminator: '',
                    institution: ''
                }
            });

            // Set Value
            if (self.specificEvaluator === undefined) {
                $("input[name=" + self.$input.attr("name") + "]").val('');
            } else {
                $("input[name=" + self.$input.attr("name") + "]").val(self.specificEvaluator.registerMember.id);
                self.model = self.specificEvaluator.registerMember;
            }
        },

        events: {
            "click a#selectEvaluator": "onSelectEvaluator",
            "click a#toggleEdit": "onToggleEdit",
            "click a#clear": "clear",
            "click #searchEvaluator": "searchEvaluator"
        },

        render: function () {
            var self = this;
            var tpl_data;

            if (self.$input.val() !== '' && self.model === undefined) {
                return self;
            }

            // Prepare Data
            tpl_data = {
                editable: self.options.editable
            };
            // Render
            self.closeInnerViews();
            self.$el.empty();
            self.$el.append(this.template(tpl_data));
            self.select(self.$input.val());
            self.$("#evaluatorDescription").html(_.templates.evaluator(self.model));

            var localeData = [];
            localeData.push({
                name: "locale",
                value: App.locale
            })

            // Initialize Plugins
            if (!$.fn.DataTable.fnIsDataTable(self.$("table.evaluators-table"))) {
                self.$("table.evaluators-table").dataTable({
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "aaSorting": [[1, "asc"]],
                    "oLanguage": {
                        "sSearch": $.i18n.prop("dataTable_sSearch"),
                        "sLengthMenu": $.i18n.prop("dataTable_sLengthMenu"),
                        "sZeroRecords": $.i18n.prop("dataTable_sZeroRecords"),
                        "sInfo": $.i18n.prop("dataTable_sInfo"),
                        "sInfoEmpty": $.i18n.prop("dataTable_sInfoEmpty"),
                        "sInfoFiltered": $.i18n.prop("dataTable_sInfoFiltered"),
                        "iDeferLoading": 0,
                        "oPaginate": {
                            sFirst: $.i18n.prop("dataTable_sFirst"),
                            sPrevious: $.i18n.prop("dataTable_sPrevious"),
                            sNext: $.i18n.prop("dataTable_sNext"),
                            sLast: $.i18n.prop("dataTable_sLast")
                        }
                    },
                    "aoColumns": [
                        {"mData": "register"},
                        {"mData": "lastname"},
                        {"mData": "firstname"},
                        {"mData": "discriminator"},
                        {"mData": "institution", "bSortable": false},
                        {"mData": "options", "sType": "html", "bSortable": false}
                    ],
                    "bProcessing": true,
                    "bServerSide": true,
                    "sAjaxSource": self.collection.url + '/search',
                    "fnServerData": function (sSource, aoData, fnCallback) {
                        $.ajax({
                            "type": "POST",
                            "url": sSource,
                            "data": aoData.concat(localeData),
                            "success": function (json) {
                                // Read Data
                                self.collection = json.records;
                                json.aaData = _.map(json.records, function (evaluator) {
                                    return {
                                        register: evaluator.register.subject.name,
                                        lastname: evaluator.professor.user.lastname[App.locale],
                                        firstname: evaluator.professor.user.firstname[App.locale],
                                        discriminator: $.i18n.prop(evaluator.professor.discriminator),
                                        institution: _.isEqual(evaluator.professor.discriminator,
                                            'PROFESSOR_FOREIGN') ? evaluator.professor.institution : _.templates.department(evaluator.professor.department),
                                        options: '<p align="center"><a id="selectEvaluator" class="btn btn-mini" data-evaluator-id="' + evaluator.id + '"><i class="icon-eye-open"></i>' + $.i18n.prop('btn_select') + '</a></p>'
                                    };
                                });
                                fnCallback(json);
                            }
                        });
                    }
                });

                var filter = self.$('div.dataTables_filter');
                $('<label>&nbsp;<a id="searchEvaluator" class="btn btn-mini" style="float: right; margin-left: 5px"><i class="icon-search"></i>' + $.i18n.prop("btn_search") + '</a></label>').prependTo(filter);

                self.$(".dataTables_filter input").unbind();
                self.$(".dataTables_filter input").bind('keyup', function (e) {
                    if (e.keyCode == 13) {
                        self.$("table.evaluators-table").dataTable().fnFilter($(this).val());
                    }
                });
            }

            self.$("div.dataTables_wrapper").hide();

            // Return result
            return self;
        },

        searchEvaluator: function () {
            var self = this;
            self.$("table.evaluators-table").dataTable().fnFilter(self.$(".dataTables_filter input").val());
        },

        onToggleEdit: function () {
            var self = this;
            self.toggleEdit();
        },

        onSelectEvaluator: function (event) {
            var self = this;
            var id = $(event.currentTarget).attr('data-evaluator-id');
            self.select(id);
        },

        toggleEdit: function (show) {
            var self = this;
            if (_.isUndefined(show)) {
                self.$("div.dataTables_wrapper").toggle(400);
            } else if (show) {
                self.$("div.dataTables_wrapper").show(400);
            } else {
                self.$("div.dataTables_wrapper").hide(400);
            }
        },

        clear: function () {
            var self = this;
            self.model = self.emptyModel;
            self.$input.val('').trigger("change").trigger("input");
            self.$("#evaluatorDescription").html(_.templates.evaluator(self.model.toJSON()));
            self.$input.parent().find('a#clear').hide();
            self.$("div.dataTables_wrapper").hide(400);
        },

        select: function (evaluatorId) {
            var self = this;
            var selectedModel;
            if (evaluatorId) {
                var counter;
                //selectedModel = self.collection.get(evaluatorId);
                for (counter in self.collection) {
                    if (self.collection[counter].id === parseInt(evaluatorId)) {
                        selectedModel = self.collection[counter];
                    }
                }
                if (selectedModel && !_.isEqual(selectedModel.id, self.$input.val())) {
                    self.model = selectedModel;
                    self.$input.val(selectedModel.id).trigger("change").trigger("input");
                    self.$("#evaluatorDescription").html(_.templates.evaluator(self.model));
                    self.$input.parent().find('a#clear').show();
                    self.$("div.dataTables_wrapper").hide(400);
                }
            } else {
                self.clear();
            }
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * ConfirmWithdrawCandidacyView ********************************************
     **************************************************************************/
    Views.ConfirmWithdrawCandidacyView = Views.BaseView.extend({
        tagName: "div",

        className: "modal",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show");
            this.template = _.template(tpl_confirm_withdraw_candidacy);
        },

        events: {
            "click a#yes": function () {
                this.$el.modal('hide');
                if (_.isFunction(this.options.yes)) {
                    this.options.yes();
                }
            }
        },

        render: function () {
            var self = this;
            self.$el.empty();
            self.$el.append(self.template({
                title: self.options.title,
                message: self.options.message,
                messageDetails: self.options.messageDetails
            }));
        },
        show: function () {
            var self = this;
            self.render();
            self.$el.modal();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    /***************************************************************************
     * showDomesticProfessorsCreateAccountsView ********************************
     **************************************************************************/
    Views.showDomesticProfessorsCreateAccountsView = Views.BaseView.extend({
        tagName: "div",

        className: "span12",

        initialize: function (options) {
            this._super('initialize', [options]);
            this.template = _.template(tpl_domestic_professors_create);
            this.createdProfessorsTemplate = _.template(tpl_domestic_professors_created_list);
        },

        render: function () {
            var self = this;
            self.closeInnerViews();
            self.$el.empty();
            self.addTitle();
            self.$el.html(self.template({}));

            // Initialize FileUpload widget
            self.$('#uploader input[name=file]').fileupload({
                dataType: 'json',
                url: '/dep/rest/domesticprofessor/createaccount' + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
                replaceFileInput: false,
                forceIframeTransport: true,
                multipart: true,
                maxFileSize: 30000000,
                add: function (e, data) {
                    function upload() {
                        self.$("a#upload").unbind("click");
                        data.submit();
                    }

                    self.$("a#upload").unbind("click");
                    self.$("a#upload").bind("click", upload);
                },
                done: function (e, data) {
                    self.$("#domesticProfessors").empty();
                    if (!!data.result.error || data.result.length == 0) {
                        var message = data.result.length == 0 ? "error.csvFile.already.existing.accounts" : "error." + data.result.error;
                        new Views.PopupView({
                            type: "error",
                            message: $.i18n.prop(message)
                        }).show();
                    } else {
                        new Views.PopupView({
                            type: "success",
                            message: $.i18n.prop("csvFile.upload.success")
                        }).show();
                        var result = {
                            domesticProfessors: data.result
                        }
                        self.$("#domesticProfessors").html(self.createdProfessorsTemplate(result));
                    }
                },
                fail: function (e, data) {
                    self.$("#domesticProfessors").empty();
                }
            });

            return self;
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });


    /***************************************************************************
     * CandidateCandidacyActionsHistory ****************************************
     **************************************************************************/
    Views.CandidateCandidacyActionsHistoryView = Views.BaseView.extend({
        tagName: "div",

        className: "modal",

        initialize: function (options) {
            this._super('initialize', [options]);
            _.bindAll(this, "show");
            this.template = _.template(candidate_candidacy_history_actions_list);
        },

        events: {
        },

        render: function () {
            var self = this;
            self.$el.empty();
            self.$el.append(self.template({
                title: self.options.title,
                statusHistoryList: self.options.statusHistoryList
            }));
        },
        show: function () {
            var self = this;
            self.render();
            self.$el.modal();
        },

        close: function () {
            this.closeInnerViews();
            $(this.el).unbind();
            $(this.el).remove();
        }
    });

    return Views;

});
