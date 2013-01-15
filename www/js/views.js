define([ "jquery", "underscore", "backbone", "application", "models", "text!tpl/announcement-list.html", "text!tpl/confirm.html", "text!tpl/file-edit.html", "text!tpl/file-multiple-edit.html", "text!tpl/home.html", "text!tpl/login-admin.html", "text!tpl/login-main.html", "text!tpl/popup.html", "text!tpl/professor-list.html", "text!tpl/register-edit.html", "text!tpl/register-list.html", "text!tpl/role-edit.html", "text!tpl/role-tabs.html", "text!tpl/role.html", "text!tpl/user-edit.html", "text!tpl/user-list.html", "text!tpl/user-registration-select.html", "text!tpl/user-registration-success.html", "text!tpl/user-registration.html", "text!tpl/user-role-info.html", "text!tpl/user-search.html", "text!tpl/user-verification.html", "text!tpl/user.html", "text!tpl/language.html", "text!tpl/professor-committees.html", "text!tpl/professor-evaluations.html", "text!tpl/register.html", "text!tpl/institution-regulatory-framework.html", "text!tpl/institution-regulatory-framework-edit.html", "text!tpl/position-search-criteria.html", "text!tpl/position-search-result.html", "text!tpl/candidacy-edit.html", "text!tpl/candidate-candidacy-list.html", "text!tpl/candidacy.html",
	"text!tpl/candidacy-update-confirm.html", "text!tpl/institution-regulatory-framework-list.html", "text!tpl/register-members.html", "text!tpl/register-members-edit.html", "text!tpl/register-members-edit-professor-list.html", "text!tpl/register-member-edit.html", "text!tpl/overlay.html", "text!tpl/position-main-edit.html", "text!tpl/position-candidacies-edit.html", "text!tpl/position-committee-edit.html", "text!tpl/position-committee-member-edit.html", "text!tpl/position-evaluation-edit.html", "text!tpl/position-evaluation-edit-register-member-list.html", "text!tpl/position-evaluation-evaluator-edit.html", "text!tpl/position-edit.html", "text!tpl/position-list.html", "text!tpl/position-committee-edit-register-member-list.html", "text!tpl/position.html", "text!tpl/position-committee.html", "text!tpl/position-nomination-edit.html", "text!tpl/position-complementaryDocuments-edit.html" ], function($, _, Backbone, App, Models, tpl_announcement_list, tpl_confirm, tpl_file_edit, tpl_file_multiple_edit, tpl_home, tpl_login_admin, tpl_login_main, tpl_popup, tpl_professor_list, tpl_register_edit, tpl_register_list, tpl_role_edit, tpl_role_tabs, tpl_role, tpl_user_edit, tpl_user_list,
	tpl_user_registration_select, tpl_user_registration_success, tpl_user_registration, tpl_user_role_info, tpl_user_search, tpl_user_verification, tpl_user, tpl_language, tpl_professor_committees, tpl_professor_evaluations, tpl_register, tpl_institution_regulatory_framework, tpl_institution_regulatory_framework_edit, tpl_position_search_criteria, tpl_position_search_result, tpl_candidacy_edit, tpl_candidate_candidacy_list, tpl_candidacy, tpl_candidacy_update_confirm, tpl_institution_regulatory_framework_list, tpl_register_members, tpl_register_members_edit, tpl_register_members_edit_professor_list, tpl_register_member_edit, tpl_overlay, tpl_position_main_edit, tpl_position_candidacies_edit, tpl_position_committee_edit, tpl_position_committee_member_edit, tpl_position_evaluation_edit, tpl_position_evaluation_edit_register_member_list, tpl_position_evaluation_evaluator_edit, tpl_position_edit, tpl_position_list, tpl_position_committee_edit_register_member_list, tpl_position, tpl_position_committee, tpl_position_nomination_edit, tpl_position_complementaryDocuments_edit) {

	/** ****************************************************************** */

	var Views = {};

	/***************************************************************************
	 * BaseView ***************************************************************
	 **************************************************************************/
	Views.BaseView = Backbone.View.extend({
		className : "span12",

		innerViews : [],

		addFile : function(collection, type, $el, options) {
			var self = this;
			var fileView;
			var file = collection.find(function(model) {
				return _.isEqual(model.get("type"), type);
			});
			options = options ? options : {};
			if (_.isUndefined(file)) {
				file = new Models.File({
					"type" : type
				});
			}
			file.urlRoot = collection.url;
			fileView = new Views.FileView(_.extend({
				model : file
			}, options));
			$el.html(fileView.render().el);
			self.innerViews.push(fileView);
		},

		addFileList : function(collection, type, $el, options) {
			var self = this;
			var files = new Models.Files();
			options = options ? options : {};

			files.type = type;
			files.url = collection.url;
			_.each(collection.filter(function(model) {
				return _.isEqual(model.get("type"), type);
			}), function(model) {
				files.add(model);
			});
			var fileListView = new Views.FileListView(_.extend({
				collection : files
			}, options));
			$el.html(fileListView.render().el);
		}
	});

	/***************************************************************************
	 * MenuView ********************************************************
	 **************************************************************************/
	Views.MenuView = Views.BaseView.extend({
		el : "ul#menu",

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.model.bind('change', this.render);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			var menuItems = [];
			self.$el.empty();

			menuItems.push("profile");
			if (self.model.hasRoleWithStatus("PROFESSOR_DOMESTIC", "ACTIVE")) {
				menuItems.push("registers");
				menuItems.push("professorCommittees");
				menuItems.push("professorEvaluations");
			}
			if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
				menuItems.push("registers");
				menuItems.push("professorCommittees");
				menuItems.push("professorEvaluations");
			}
			if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
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
				menuItems.push("regulatoryframeworks");
				menuItems.push("registers");
				menuItems.push("positions");
			}
			if (self.model.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
				menuItems.push("regulatoryframeworks");
				menuItems.push("registers");
				menuItems.push("positions");
			}
			_.each(_.uniq(menuItems), function(menuItem) {
				self.$el.append("<li><a href=\"\#" + menuItem + "\">" + $.i18n.prop("menu_" + menuItem) + "</a></li>");
			});

			return this;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}

	});

	/***************************************************************************
	 * LanguageView ************************************************************
	 **************************************************************************/
	Views.LanguageView = Views.BaseView.extend({
		el : "div#language",

		initialize : function() {
			_.bindAll(this, "render", "selectLanguage", "close");
			this.template = _.template(tpl_language);
		},

		events : {
			"click a:not(.active)" : "selectLanguage"
		},

		render : function(eventName) {
			var self = this;
			var language = App.utils.getCookie("apella-lang");
			self.$el.html(self.template());
			if (!language) {
				language = "el";
			}
			self.$("a[data-language=" + language + "]").addClass("active");
			return self;
		},

		selectLanguage : function(event) {
			var language = $(event.currentTarget).data('language');
			// Set Language Cookie:
			App.utils.addCookie('apella-lang', language);
			// Trigger refresh
			location.reload(true);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}

	});

	/***************************************************************************
	 * AdminMenuView ***********************************************************
	 **************************************************************************/
	Views.AdminMenuView = Views.BaseView.extend({
		el : "ul#menu",

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.model.bind('change', this.render);
		},

		events : {},

		render : function(eventName) {
			this.$el.empty();
			this.$el.append("<ul class=\"nav\">");
			this.$el.find("ul").append("<li><a href=\"\#users\">" + $.i18n.prop('adminmenu_users') + "</a></li>");
			return this;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}

	});

	/***************************************************************************
	 * UserMenuView ************************************************************
	 **************************************************************************/
	Views.UserMenuView = Views.BaseView.extend({
		el : "li#user-menu",

		initialize : function() {
			_.bindAll(this, "render", "logout", "close");
			this.model.bind('change', this.render);
		},

		events : {
			"click a#logout" : "logout"
		},

		render : function(eventName) {
			this.$el.empty();
			this.$el.append("<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\"> <i class=\"icon-user\"></i> " + this.model.get("username") + "<span class=\"caret\"></span></a>");
			this.$el.append("<ul class=\"dropdown-menu\">");
			this.$el.find("ul").append("<li><a href=\"\#account\">" + $.i18n.prop('menu_account') + "</a>");
			// Add Logout
			this.$el.find("ul").append("<li><a id=\"logout\" >" + $.i18n.prop('menu_logout') + "</a>");
			return this;
		},

		logout : function(event) {
			// Remove X-Auth-Token
			$.ajaxSetup({
				headers : {}
			});
			// Remove auth cookie
			document.cookie = "_dep_a=-1;expires=0;path=/";
			// Send Redirect
			window.location.href = window.location.pathname;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * LoginView **********************************************************
	 **************************************************************************/
	Views.LoginView = Views.BaseView.extend({
		tagName : "div",

		validatorLogin : undefined,
		validatorResetPasswordForm : undefined,
		validatorResendVerificationEmail : undefined,

		initialize : function() {
			_.bindAll(this, "render", "showResetPassword", "showResendVerificationEmailForm", "resetPassword", "resendVerificationEmail", "login", "close");
			this.template = _.template(tpl_login_main);
			this.model.bind('change', this.render);
		},

		events : {
			"click a#login" : function() {
				this.$("form#loginForm").submit();
			},
			"click a#resetPassword" : function() {
				this.$("form#resetPasswordForm").submit();
			},
			"click a#resendVerificationEmail" : function() {
				this.$("form#resendVerificationEmailForm").submit();
			},
			"click a#forgotPassword" : "showResetPassword",
			"click a#haveNotReceivedVerification" : "showResendVerificationEmailForm",
			"submit form#loginForm" : "login",
			"submit form#resetPasswordForm" : "resetPassword",
			"submit form#resendVerificationEmailForm" : "resendVerificationEmail"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			self.$("#resetPasswordForm").hide();
			self.$("#resendVerificationEmailForm").hide();

			self.validatorLogin = $("form#loginForm", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					username : {
						required : true,
						minlength : 2
					},
					password : {
						required : true,
						minlength : 5
					}
				},
				messages : {
					username : {
						required : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
					},
					password : {
						required : $.i18n.prop('validation_required'),
						minlength : $.i18n.prop('validation_minlength', 5)
					}
				}
			});

			self.validatorResetPasswordForm = $("form#resetPasswordForm", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).removeClass("error");
				},
				rules : {
					username : {
						required : true,
						minlength : 2
					}
				},
				messages : {
					username : {
						required : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
					}
				}
			});

			self.validatorResendVerificationEmail = $("form#resendVerificationEmailForm", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).removeClass("error");
				},
				rules : {
					username : {
						required : true,
						minlength : 2
					}
				},
				messages : {
					username : {
						required : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
					}
				}
			});

			return self;
		},

		showResetPassword : function(event) {
			var self = this;
			self.$("#resetPasswordForm").toggle();
			self.$("#resendVerificationEmailForm").hide();
		},

		showResendVerificationEmailForm : function(event) {
			var self = this;
			self.$("#resendVerificationEmailForm").toggle();
			self.$("#resetPasswordForm").hide();
		},

		login : function(event) {
			var self = this;
			var username = self.$('form#loginForm input[name=username]').val();
			var password = self.$('form#loginForm input[name=password]').val();

			// Save to model
			self.model.login({
				"username" : username,
				"password" : password
			}, {
				wait : true,
				success : function(model, resp) {
					// Notify AppRouter to start Application (fill
					// Header and
					// handle
					// history token)
					self.model.trigger("user:loggedon");
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		resetPassword : function(event) {
			var self = this;
			var username = self.$('form#resetPasswordForm input[name=username]').val();

			// Save to model
			self.model.resetPassword({
				"username" : username
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("PasswordReset")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		resendVerificationEmail : function(event) {
			var self = this;
			var username = self.$('form#resendVerificationEmailForm input[name=username]').val();

			// Save to model
			self.model.resendVerificationEmail({
				"username" : username
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("VerificationEmailResent")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * AdminLoginView **********************************************************
	 **************************************************************************/
	Views.AdminLoginView = Views.BaseView.extend({
		tagName : "div",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "login", "close");
			this.template = _.template(tpl_login_admin);
			this.model.bind('change', this.render);
		},

		events : {
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "login"
		},

		render : function(eventName) {
			var self = this;

			self.$el.html(this.template(this.model.toJSON()));

			this.validator = $("form", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					username : {
						required : true,
						minlength : 2
					},
					password : {
						required : true,
						minlength : 5
					}
				},
				messages : {
					username : {
						required : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
					},
					password : {
						required : $.i18n.prop('validation_required'),
						minlength : $.i18n.prop('validation_minlength', 5)
					}
				}
			});

			return this;
		},

		login : function(event) {
			var self = this;
			var username = self.$('form input[name=username]').val();
			var password = self.$('form input[name=password]').val();

			// Save to model
			self.model.login({
				"username" : username,
				"password" : password
			}, {
				wait : true,
				success : function(model, resp) {
					// Notify AppRouter to start Application (fill
					// Header and
					// handle
					// history token)
					self.model.trigger("user:loggedon");
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PopupView ***************************************************************
	 **************************************************************************/
	Views.PopupView = Views.BaseView.extend({
		tagName : "div",

		className : "alert alert-block alert-popup fade in",

		initialize : function() {
			this.template = _.template(tpl_popup);
			_.bindAll(this, "render", "show", "close");
		},

		events : {},

		render : function(eventName) {
			var self = this;

			self.$el.html(this.template({
				message : this.options.message
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
			}
			return this;
		},

		show : function() {
			var self = this;
			self.render();
			$('div#alerts').append(self.el);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * ConfirmView *************************************************************
	 **************************************************************************/
	Views.ConfirmView = Views.BaseView.extend({
		tagName : "div",

		className : "modal",

		initialize : function() {
			this.template = _.template(tpl_confirm);
			_.bindAll(this, "render", "show", "close");
		},

		events : {
			"click a#yes" : function(event) {
				this.$el.modal('hide');
				if (_.isFunction(this.options.yes)) {
					this.options.yes();
				}
			}
		},

		render : function(eventName) {
			$(this.el).html(this.template({
				title : this.options.title,
				message : this.options.message
			}));
		},
		show : function() {
			this.render();
			this.$el.modal();
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * OverlayView *************************************************************
	 **************************************************************************/
	Views.OverlayView = Views.BaseView.extend({
		tagName : "div",

		className : "modal fade",

		innerView : undefined,

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_overlay);

			this.innerView = this.options.innerView;
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({}));
			self.$("div.modal-body div.row-fluid").html(self.innerView.render().el);
			self.$el.modal();
			self.$el.on('hidden', self.close);
		},

		close : function() {
			var self = this;
			self.innerView.close();
			self.$el.off('hidden', self.close);
			self.$el.modal("hide");
			self.$el.unbind();
			self.$el.remove();
		}
	});

	/***************************************************************************
	 * UserRegistrationSelectView **********************************************
	 **************************************************************************/
	Views.UserRegistrationSelectView = Views.BaseView.extend({
		tagName : "div",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_user_registration_select);
		},

		events : {},

		render : function(eventName) {
			$(this.el).html(this.template({
				roles : App.allowedRoles
			}));
			return this;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * UserRegistrationView ****************************************************
	 **************************************************************************/
	Views.UserRegistrationView = Views.BaseView.extend({
		tagName : "div",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "submit", "selectInstitution", "close");
			this.template = _.template(tpl_user_registration);
			this.model.bind('change', this.render);
		},

		events : {
			"click a#save" : function() {
				this.$("form#userForm").submit();
			},
			"click a#selectInstitution" : "selectInstitution",
			"submit form#userForm" : "submit"
		},

		render : function(event) {
			var self = this;
			var role = self.model.get('roles')[0];
			self.$el.html(self.template(role));

			if (role.discriminator === "PROFESSOR_DOMESTIC") {
				// Especially for PROFESSOR_DOMESTIC there
				// is a demand to select
				// institution first in case their institution supports
				// Shibboleth
				// Login
				// Add institutions in selector:
				App.institutions = App.institutions ? App.institutions : new Models.Institutions();
				App.institutions.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(institution) {
							if (_.isObject(role.institution) && _.isEqual(institution.id, role.institution.id)) {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
							} else {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
							}
						});
						self.$("select[name='institution']").change();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
				// Set UI components
				if (role.institution && role.institution.registrationType === "REGISTRATION_FORM") {
					self.$("#shibbolethLoginInstructions").hide();
					self.$("form#institutionForm").hide();
					self.$("form#userForm").show();
				} else if (role.institution && role.institution.registrationType === "SHIBBOLETH") {
					self.$("#shibbolethLoginInstructions").show();
					self.$("form#institutionForm").hide();
					self.$("form#userForm").hide();
				} else {
					self.$("#shibbolethLoginInstructions").hide();
					self.$("form#institutionForm").show();
					self.$("form#userForm").hide();
				}
			} else if (role.discriminator === "INSTITUTION_MANAGER") {
				// Especially for INSTITUTION_MANAGER there is a demand
				// to
				// select institution first

				// Add institutions in selector:
				App.institutions = App.institutions ? App.institutions : new Models.Institutions();
				App.institutions.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(institution) {
							if (_.isObject(role.institution) && _.isEqual(institution.id, role.institution.id)) {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
							} else {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
							}
						});
						self.$("select[name='institution']").change();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					username : {
						required : true,
						email : true,
						minlength : 2
					},
					firstname : "required",
					lastname : "required",
					fathername : "required",
					firstnamelatin : {
						required : !_.isEqual(role.discriminator, "PROFESSOR_FOREIGN"),
						onlyLatin : true
					},
					lastnamelatin : {
						required : !_.isEqual(role.discriminator, "PROFESSOR_FOREIGN"),
						onlyLatin : true
					},
					fathernamelatin : {
						required : !_.isEqual(role.discriminator, "PROFESSOR_FOREIGN"),
						onlyLatin : true
					},
					identification : "required",
					password : {
						required : true,
						pwd : true,
						minlength : 5
					},
					confirm_password : {
						required : true,
						minlength : 5,
						equalTo : "form input[name=password]"
					},
					email : {
						required : true,
						email : true,
						minlength : 2
					},
					mobile : {
						required : true,
						number : true,
						minlength : 10
					},
					address_street : "required",
					address_number : "required",
					address_zip : "required",
					address_city : "required",
					address_country : "required"
				},
				messages : {
					username : {
						required : $.i18n.prop('validation_username'),
						email : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
					},
					firstname : $.i18n.prop('validation_firstname'),
					lastname : $.i18n.prop('validation_lastname'),
					fathername : $.i18n.prop('validation_fathername'),
					firstnamelatin : {
						required : $.i18n.prop('validation_firstnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					lastnamelatin : {
						required : $.i18n.prop('validation_lastnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					fathernamelatin : {
						required : $.i18n.prop('validation_fathernamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					identification : {
						required : $.i18n.prop('validation_required')
					},
					password : {
						required : $.i18n.prop('validation_required'),
						pwd : $.i18n.prop('validation_password'),
						minlength : $.i18n.prop('validation_minlength', 5)
					},
					confirm_password : {
						required : $.i18n.prop('validation_required'),
						minlength : $.i18n.prop('validation_minlength', 5),
						equalTo : $.i18n.prop('validation_confirmpassword')
					},
					email : {
						required : $.i18n.prop('validation_email'),
						email : $.i18n.prop('validation_email'),
						minlength : $.i18n.prop('validation_minlength', 2)
					},
					mobile : {
						required : $.i18n.prop('validation_phone'),
						number : $.i18n.prop('validation_number'),
						minlength : $.i18n.prop('validation_minlength', 10)
					},
					address_street : $.i18n.prop('validation_street'),
					address_number : $.i18n.prop('validation_number'),
					address_zip : $.i18n.prop('validation_zip'),
					address_city : $.i18n.prop('validation_city'),
					address_country : $.i18n.prop('validation_country')
				}
			});

			return self;
		},

		submit : function(event) {
			var self = this;

			// Read Input
			var username = self.$('form input[name=username]').val();
			var firstname = self.$('form input[name=firstname]').val();
			var lastname = self.$('form input[name=lastname]').val();
			var fathername = self.$('form input[name=fathername]').val();
			var firstnamelatin = self.$('form input[name=firstnamelatin]').val();
			var lastnamelatin = self.$('form input[name=lastnamelatin]').val();
			var fathernamelatin = self.$('form input[name=fathernamelatin]').val();
			var password = self.$('form input[name=password]').val();
			var mobile = self.$('form input[name=mobile]').val();
			var email = self.$('form input[name=email]').val();
			var address_street = self.$('form input[name=address_street]').val();
			var address_number = self.$('form input[name=address_number]').val();
			var address_zip = self.$('form input[name=address_zip]').val();
			var address_city = self.$('form input[name=address_city]').val();
			var address_country = self.$('form input[name=address_country]').val();

			// Validate

			// Save to model
			self.model.save({
				"username" : username,
				"basicInfo" : {
					"firstname" : firstname,
					"lastname" : lastname,
					"fathername" : fathername
				},
				"basicInfoLatin" : {
					"firstname" : firstnamelatin,
					"lastname" : lastnamelatin,
					"fathername" : fathernamelatin
				},
				"contactInfo" : {
					"address" : {
						"street" : address_street,
						"number" : address_number,
						"zip" : address_zip,
						"city" : address_city,
						"country" : address_country
					},
					"email" : email,
					"mobile" : mobile
				},
				"password" : password
			}, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("success", {
						trigger : true
					});
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		selectInstitution : function() {
			var self = this;
			var role = self.model.get('roles')[0];
			var institutionId = self.$("select[name=institution]").val();
			role.institution = App.institutions.get(institutionId).toJSON();
			self.model.trigger("change");
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * UserVerificationView ****************************************************
	 **************************************************************************/
	Views.UserVerificationView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			this.template = _.template(tpl_user_verification);
			this.model.bind('change', this.render);
			_.bindAll(this, "render", "close");
		},

		render : function(eventName) {
			$(this.el).html(this.template(this.model.toJSON()));
			return this;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}

	});

	/***************************************************************************
	 * HomeView ****************************************************************
	 **************************************************************************/
	Views.HomeView = Views.BaseView.extend({
		tagName : "div",

		className : "span12 hero-unit",

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_home);
			this.model.bind('change', this.render);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			var tiles = [];
			tiles.push({
				link : "account"
			});
			tiles.push({
				link : "profile"
			});
			if (self.model.hasRoleWithStatus("PROFESSOR_DOMESTIC", "ACTIVE")) {
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "professorCommittees"
				});
				tiles.push({
					link : "professorEvaluations"
				});
			}
			if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "professorCommittees"
				});
				tiles.push({
					link : "professorEvaluations"
				});
			}
			if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "sposition"
				});
				tiles.push({
					link : "candidateCandidacies"
				});
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_MANAGER", "ACTIVE")) {
				tiles.push({
					link : "iassistants"
				});
				tiles.push({
					link : "regulatoryframeworks"
				});
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "positions"
				});
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_ASSISTANT", "ACTIVE")) {
				tiles.push({
					link : "regulatoryframeworks"
				});
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "positions"
				});
			}
			if (self.model.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE")) {
				tiles.push({
					link : "massistants"
				});
				tiles.push({
					link : "regulatoryframeworks"
				});
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "positions"
				});
			}
			if (self.model.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
				tiles.push({
					link : "regulatoryframeworks"
				});
				tiles.push({
					link : "registers"
				});
				tiles.push({
					link : "positions"
				});
			}
			tiles = _.uniq(tiles, false, function(tile) {
				return tile.link;
			});
			self.$el.html(this.template(_.extend(this.model.toJSON(), {
				"tiles" : (function() {
					var result = [];
					var row = 0;
					var col = 0;
					while (tiles.length) {
						if (!result[row]) {
							result[row] = [];
						}
						result[row].push(tiles.shift());
						col = (col + 1) % 3;
						if (col == 0) {
							row = row + 1;
						}
					}
					return result;
				})()
			})));
			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	// AccountView
	Views.AccountView = Views.BaseView.extend({
		tagName : "div",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "submit", "remove", "status", "cancel", "close", "applyRules");
			this.template = _.template(tpl_user_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"click a[data-action=status]" : "status",
			"click a#remove" : "remove",
			"click a#cancel" : "cancel",
			"submit form" : "submit"
		},

		applyRules : function() {
			var self = this;
			// Actions:
			self.$("a#remove").hide();
			self.$("a#status").addClass("disabled");
			// Fields:
			if (self.model.isNew()) {
				self.$("input[name=username]").removeAttr("disabled");
				self.$("input[name=firstname]").removeAttr("disabled");
				self.$("input[name=lastname]").removeAttr("disabled");
				self.$("input[name=fathername]").removeAttr("disabled");
				self.$("input[name=firstnamelatin]").removeAttr("disabled");
				self.$("input[name=lastnamelatin]").removeAttr("disabled");
				self.$("input[name=fathernamelatin]").removeAttr("disabled");
				self.$("input[name=identification]").removeAttr("disabled");
			} else if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
				self.$("input[name=username]").attr("disabled", true);
				self.$("input[name=firstname]").removeAttr("disabled");
				self.$("input[name=lastname]").removeAttr("disabled");
				self.$("input[name=fathername]").removeAttr("disabled");
				self.$("input[name=firstnamelatin]").removeAttr("disabled");
				self.$("input[name=lastnamelatin]").removeAttr("disabled");
				self.$("input[name=fathernamelatin]").removeAttr("disabled");
				self.$("input[name=identification]").removeAttr("disabled");
			} else {
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

		render : function(eventName) {
			var self = this;
			// 1. Render
			self.$el.html(this.template(self.model.toJSON()));
			// 2. Check State to enable/disable fields
			self.applyRules();
			// 3. Add Validator
			self.validator = self.$("form").validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					username : "required",
					firstname : "required",
					lastname : "required",
					fathername : "required",
					firstnamelatin : {
						required : true,
						onlyLatin : true
					},
					lastnamelatin : {
						required : true,
						onlyLatin : true
					},
					fathernamelatin : {
						required : true,
						onlyLatin : true
					},
					identification : {
						required : true
					},
					password : {
						required : self.model.isNew(),
						pwd : true,
						minlength : 5
					},
					confirm_password : {
						required : self.model.isNew(),
						minlength : 5,
						equalTo : "form input[name=password]"
					},
					mobile : {
						required : true,
						number : true,
						minlength : 10
					},
					email : {
						required : true,
						email : true,
						minlength : 2
					},
					address_street : "required",
					address_number : "required",
					address_zip : "required",
					address_city : "required",
					address_country : "required"
				},
				messages : {
					username : $.i18n.prop('validation_username'),
					firstname : $.i18n.prop('validation_firstname'),
					lastname : $.i18n.prop('validation_lastname'),
					fathername : $.i18n.prop('validation_fathername'),
					firstnamelatin : {
						required : $.i18n.prop('validation_firstnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					lastnamelatin : {
						required : $.i18n.prop('validation_lastnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					fathernamelatin : {
						required : $.i18n.prop('validation_fathernamelatin'),
						onlyLatin : $.i18n.prop('validation_latin')
					},
					identification : {
						required : $.i18n.prop('validation_identification')
					},
					password : {
						required : $.i18n.prop('validation_required'),
						pwd : $.i18n.prop('validation_password'),
						minlength : $.i18n.prop('validation_minlength', 5)
					},
					confirm_password : {
						required : $.i18n.prop('validation_required'),
						minlength : $.i18n.prop('validation_minlength', 5),
						equalTo : $.i18n.prop('validation_confirmpassword')
					},
					mobile : {
						required : $.i18n.prop('validation_phone'),
						number : $.i18n.prop('validation_number'),
						minlength : $.i18n.prop('validation_minlength', 10)
					},
					email : {
						required : $.i18n.prop('validation_email'),
						email : $.i18n.prop('validation_email'),
						minlength : $.i18n.prop('validation_minlength', 2)
					},
					address_street : $.i18n.prop('validation_street'),
					address_number : $.i18n.prop('validation_address'),
					address_zip : $.i18n.prop('validation_zip'),
					address_city : $.i18n.prop('validation_city'),
					address_country : $.i18n.prop('validation_country')
				}
			});
			// Return
			return self;
		},

		cancel : function(event) {
			var self = this;
			if (self.validator) {
				self.validator.resetForm();
				self.render();
			}
		},

		submit : function(event) {
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
			var address_street = self.$('form input[name=address_street]').val();
			var address_number = self.$('form input[name=address_number]').val();
			var address_zip = self.$('form input[name=address_zip]').val();
			var address_city = self.$('form input[name=address_city]').val();
			var address_country = self.$('form input[name=address_country]').val();

			// Validate

			// Save to model
			self.model.save({
				"username" : username,
				"identification" : identification,
				"basicInfo" : {
					"firstname" : firstname,
					"lastname" : lastname,
					"fathername" : fathername
				},
				"basicInfoLatin" : {
					"firstname" : firstnamelatin,
					"lastname" : lastnamelatin,
					"fathername" : fathernamelatin
				},
				"contactInfo" : {
					"address" : {
						"street" : address_street,
						"number" : address_number,
						"zip" : address_zip,
						"city" : address_city,
						"country" : address_country
					},
					"email" : email,
					"mobile" : mobile
				},
				"password" : password
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			return false;
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							var popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
							popup.show();
						},
						error : function(model, resp, options) {
							var popup = new Views.PopupView({
								type : "error",
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		status : function(event) {
			var self = this;
			self.model.status({
				"status" : $(event.currentTarget).attr('status')
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * AdminAccountView ********************************************************
	 **************************************************************************/
	Views.AdminAccountView = Views.AccountView.extend({
		initialize : function(options) {
			this._super('initialize', [ options ]);
		},

		applyRules : function() {
			var self = this;
			self.$("input").attr("disabled", true);
			self.$("input[name=username]").removeAttr("disabled");
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
		},

		render : function(eventName) {
			return this._super('render', [ eventName ]);
		}
	});

	/***************************************************************************
	 * UserView ****************************************************************
	 **************************************************************************/
	Views.UserView = Views.BaseView.extend({
		tagName : "div",

		options : {
			editable : true
		},

		initialize : function() {
			this.template = _.template(tpl_user);
			_.bindAll(this, "render", "close");
			this.model.bind("change", this.render, this);
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * UserSearchView **********************************************************
	 **************************************************************************/
	Views.UserSearchView = Views.BaseView.extend({
		tagName : "div",

		className : "span12 well",

		initialize : function() {
			_.bindAll(this, "render", "search", "close");
			this.template = _.template(tpl_user_search);
		},

		events : {
			"click a#search" : "search"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({}));
			if (self.options.query) {
				$('form input[name=username]', this.el).val(self.options.query['username']);
				$('form input[name=firstname]', this.el).val(self.options.query['firstname']);
				$('form input[name=lastname]', this.el).val(self.options.query['lastname']);
				$('form select[name=status]', this.el).val(self.options.query['status']);
				$('form select[name=role]', this.el).val(self.options.query['role']);
				$('form select[name=roleStatus]', this.el).val(self.options.query['roleStatus']);

				self.search();
			}
			return self;
		},
		search : function() {
			var self = this;
			var searchData = {
				username : self.$('form input[name=username]').val(),
				firstname : self.$('form input[name=firstname]').val(),
				lastname : self.$('form input[name=lastname]').val(),
				status : self.$('form select[name=status]').val(),
				role : self.$('form select[name=role]').val(),
				roleStatus : $('form select[name=roleStatus]').val()
			};
			App.router.navigate("users/" + JSON.stringify(searchData), {
				trigger : false
			});
			self.collection.fetch({
				cache : false,
				data : searchData
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * UserListView ************************************************************
	 **************************************************************************/
	Views.UserListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "select", "close");
			this.template = _.template(tpl_user_list);
			this.roleInfoTemplate = _.template(tpl_user_role_info);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a" : "select"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				users : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							item.roleInfo = self.roleInfoTemplate({
								roles : item.roles
							});
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		select : function(event) {
			var selectedModel = this.collection.getByCid($(event.currentTarget).attr('user'));
			this.collection.trigger("user:selected", selectedModel);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * UserRoleInfoView ********************************************************
	 **************************************************************************/
	Views.UserRoleInfoView = Views.BaseView.extend({
		tagName : "p",

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_user_role_info);
			this.model.bind('change', this.render, this);
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				roles : self.model.get("roles")
			};
			self.$el.html(self.template(tpl_data));
			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RoleTabsView ************************************************************
	 **************************************************************************/
	Views.RoleTabsView = Backbone.View.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "select", "highlightSelected", "close");
			this.template = _.template(tpl_role_tabs);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
			this.collection.bind("role:selected", this.highlightSelected, this);
		},

		events : {
			"click a.selectRole" : "select"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				roles : (function() {
					var result = [];
					self.collection.each(function(model) {
						var item = model.toJSON();
						item.cid = model.cid;
						result.push(item);
					});
					return result;
				})()
			};
			self.$el.html(this.template(tpl_data));
			return self;
		},

		select : function(event, role) {
			var self = this;
			var selectedModel = role ? role : self.collection.getByCid($(event.target).attr('role'));
			if (selectedModel) {
				self.collection.trigger("role:selected", selectedModel);
			}
		},

		highlightSelected : function(role) {
			var self = this;
			self.$("li.active").removeClass("active");
			self.$("a[role=" + role.cid + "]").parent("li").addClass("active");
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RoleView ****************************************************************
	 **************************************************************************/
	Views.RoleView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			this.template = _.template(tpl_role);
			_.bindAll(this, "render", "close");
			this.model.bind("change", this.render, this);
		},

		render : function(eventName) {
			var self = this;
			self.$el.empty();
			if (self.model.get("discriminator") !== "ADMINISTRATOR") {
				self.$el.append($(self.template(self.model.toJSON())));

				switch (self.model.get("discriminator")) {
				case "CANDIDATE":
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "TAYTOTHTA", self.$("#tautotitaFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFile(collection, "BEBAIWSH_STRATIOTIKIS_THITIAS", self.$("#bebaiwsiStratiwtikisThitiasFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFile(collection, "FORMA_SYMMETOXIS", self.$("#formaSymmetoxisFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
								withMetadata : true,
								editable : false
							});
							self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
								withMetadata : true,
								editable : false
							});
						}
					});
					break;
				case "PROFESSOR_DOMESTIC":
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFile(collection, "FEK", self.$("#fekFile"), {
								withMetadata : false,
								editable : false
							});
							self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
								withMetadata : true,
								editable : false
							});
						}
					});
					break;
				case "PROFESSOR_FOREIGN":
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"), {
								withMetadata : false,
								editable : false
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
				}

			}
			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RoleEditView ************************************************************
	 **************************************************************************/
	Views.RoleEditView = Views.BaseView.extend({
		tagName : "div",

		id : "roleview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "isEditable", "beforeUpload", "beforeDelete", "submit", "cancel", "addFile", "addFileList", "close");
			this.template = _.template(tpl_role_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"click a[data-action=status]" : "status",
			"click a#remove" : "remove",
			"click a#cancel" : "cancel",
			"submit form" : "submit"
		},

		isEditable : function(field) {
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
				}
				break;
			case "PROFESSOR_DOMESTIC":
				switch (field) {
				case "institution":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "department":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "profileURL":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "profileFile":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "rank":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fek":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fekFile":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fekCheckbox":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fekSubject":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "subject":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				}
				break;
			case "PROFESSOR_FOREIGN":
				switch (field) {
				case "institution":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "profileURL":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "profileFile":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "rank":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "subject":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
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
				case "phone":
					return true;
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
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternatemobile":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternateaddress_street":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternateaddress_number":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternateaddress_zip":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternateaddress_city":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "alternateaddress_country":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				}
				break;
			case "INSTITUTION_ASSISTANT":
				switch (field) {
				case "institution":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "phone":
					return true;
				}
				break;
			case "MINISTRY_MANAGER":
				switch (field) {
				case "ministry":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				}
				break;
			case "MINISTRY_ASSISTANT":
				break;
			}
			return false;
		},

		beforeUpload : function(data, upload) {
			var self = this;
			var candidate = self.collection.find(function(role) {
				return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
			});
			if (candidate) {
				openCandidacies = new Models.CandidateCandidacies({}, {
					candidate : App.loggedOnUser.getRole("CANDIDATE").id
				});
				openCandidacies.fetch({
					data : {
						"open" : "true"
					},
					cache : false,
					success : function(collection, resp) {
						var candidacyUpdateConfirmView = undefined;
						if (collection.length > 0) {
							candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
								"collection" : collection,
								"answer" : function(confirm) {
									if (confirm) {
										_.extend(data.formData, {
											"updateCandidacies" : true
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

		beforeDelete : function(file, doDelete) {
			var self = this;
			var candidate = self.collection.find(function(role) {
				return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
			});
			if (candidate) {
				openCandidacies = new Models.CandidateCandidacies({}, {
					candidate : App.loggedOnUser.getRole("CANDIDATE").id
				});
				openCandidacies.fetch({
					data : {
						"open" : "true"
					},
					cache : false,
					success : function(collection, resp) {
						var candidacyUpdateConfirmView = undefined;
						if (collection.length > 0) {
							candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
								"collection" : collection,
								"answer" : function(confirm) {
									if (confirm) {
										doDelete({
											"updateCandidacies" : true
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

		render : function(eventName) {
			var self = this;
			// Close inner views (fileviews)
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			self.innerViews = [];

			// Re-render
			tpl_data = _.extend(self.model.toJSON(), {
				"primary" : self.model.isPrimary()
			});
			self.$el.html(self.template(tpl_data));

			// Apply Global Rules
			self.$("a#status").addClass("disabled");

			// Apply Specific Rule and fields per discriminator
			switch (self.model.get("discriminator")) {
			case "CANDIDATE":
				if (self.model.get("id") !== undefined) {
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "TAYTOTHTA", self.$("#tautotitaFile"), {
								withMetadata : false,
								editable : self.isEditable("tautotitaFile")
							});
							self.addFile(collection, "BEBAIWSH_STRATIOTIKIS_THITIAS", self.$("#bebaiwsiStratiwtikisThitiasFile"), {
								withMetadata : false,
								editable : self.isEditable("bebaiwsiStratiwtikisThitiasFile")
							});
							self.addFile(collection, "FORMA_SYMMETOXIS", self.$("#formaSymmetoxisFile"), {
								withMetadata : false,
								editable : self.isEditable("formaSymmetoxisFile")
							});
							self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
								withMetadata : false,
								editable : self.isEditable("biografikoFile"),
								beforeUpload : self.beforeUpload,
								beforeDelete : self.beforeDelete
							});
							self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
								withMetadata : true,
								editable : self.isEditable("ptyxioFileList"),
								beforeUpload : self.beforeUpload,
								beforeDelete : self.beforeDelete
							});
							self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
								withMetadata : true,
								editable : self.isEditable("dimosieusiFileList"),
								beforeUpload : self.beforeUpload,
								beforeDelete : self.beforeDelete
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
				break;
			case "PROFESSOR_DOMESTIC":
				// Bind change on institution selector to update
				// department
				// selector
				self.$("select[name='department']").change(function(event) {
					self.$("select[name='department']").next(".help-block").html(self.$("select[name='department'] option:selected").text());
				});
				self.$("select[name='institution']").change(function() {
					self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());

					App.departments = App.departments ? App.departments : new Models.Departments();
					App.departments.fetch({
						cache : true,
						success : function(collection, resp) {
							self.$("select[name='department']").empty();
							var selectedInstitution = parseInt($("select[name='institution']", self.$el).val());
							collection.filter(function(department) {
								return department.get('institution').id === selectedInstitution;
							}).forEach(function(department) {
								if (_.isObject(self.model.get("department")) && _.isEqual(department.id, self.model.get("department").id)) {
									self.$("select[name='department']").append("<option value='" + department.get("id") + "' selected>" + department.get("department") + "</option>");
								} else {
									self.$("select[name='department']").append("<option value='" + department.get("id") + "'>" + department.get("department") + "</option>");
								}
							});
							self.$("select[name='department']").change();
						},
						error : function(model, resp, options) {
							var popup = new Views.PopupView({
								type : "error",
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				});
				App.institutions = App.institutions ? App.institutions : new Models.Institutions();
				App.institutions.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(institution) {
							if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
							} else {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
							}
						});
						self.$("select[name='institution']").change();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});

				App.ranks = App.ranks ? App.ranks : new Models.Ranks();
				App.ranks.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(rank) {
							if (_.isObject(self.model.get("rank")) && _.isEqual(rank.id, self.model.get("rank").id)) {
								$("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "' selected>" + rank.get("name") + "</option>");
							} else {
								$("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "'>" + rank.get("name") + "</option>");
							}
						});
						self.$("select[name='rank']").change();

					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});

				if (self.model.has("id")) {
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"), {
								withMetadata : false,
								editable : self.isEditable("profileFile")
							});
							self.addFile(collection, "FEK", self.$("#fekFile"), {
								withMetadata : false,
								editable : self.isEditable("fekFile")
							});
						}
					});
				} else {
					$("#fekFile", self.$el).html($.i18n.prop("PressSave"));
					$("#profileFile", self.$el).html($.i18n.prop("PressSave"));
					$("#dimosieusiFileList", self.$el).html($.i18n.prop("PressSave"));
				}
				self.validator = $("form", this.el).validate({
					errorElement : "span",
					errorClass : "help-inline",
					highlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").addClass("error");
					},
					unhighlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").removeClass("error");
					},
					rules : {
						institution : "required",
						profileURL : {
							required : true,
							url : true
						},
						rank : "required",
						fek : "required",
						fekSubject : {
							"required" : "input[name=fekCheckbox]:not(:checked)"
						},
						subject : {
							"required" : "input[name=fekCheckbox]:checked"
						}
					},
					messages : {
						institution : $.i18n.prop('validation_institution'),
						profileURL : $.i18n.prop('validation_profileURL'),
						rank : $.i18n.prop('validation_rank'),
						subject : $.i18n.prop('validation_subject'),
						fek : $.i18n.prop('validation_fek'),
						fekSubject : $.i18n.prop('validation_fekSubject')
					}
				});
				self.$("input[name=fekCheckbox]").change(function(event) {
					if ($(this).is(":checked")) {
						self.$("textarea[name=fekSubject]").attr("disabled", true);
						self.$("textarea[name=fekSubject]").val("");
						self.$("textarea[name=subject]").removeAttr("disabled");
					} else {
						self.$("textarea[name=fekSubject]").removeAttr("disabled");
						self.$("textarea[name=subject]").attr("disabled", true);
						self.$("textarea[name=subject]").val("");
					}
				});
				self.$("input[name=fekCheckbox]").attr("checked", _.isObject(self.model.get("subject")));
				self.$("input[name=fekCheckbox]").change();
				break;
			case "PROFESSOR_FOREIGN":
				App.ranks = App.ranks ? App.ranks : new Models.Ranks();
				App.ranks.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(rank) {
							if (_.isObject(self.model.get("rank")) && _.isEqual(rank.id, self.model.get("rank").id)) {
								$("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "' selected>" + rank.get("name") + "</option>");
							} else {
								$("select[name='rank']", self.$el).append("<option value='" + rank.get("id") + "'>" + rank.get("name") + "</option>");
							}
						});
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
				if (self.model.has("id")) {
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"), {
								withMetadata : false,
								editable : self.isEditable("profileFile")
							});
						}
					});
				} else {
					self.$("#profileFile").html($.i18n.prop("PressSave"));
				}

				self.validator = $("form", this.el).validate({
					errorElement : "span",
					errorClass : "help-inline",
					highlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").addClass("error");
					},
					unhighlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").removeClass("error");
					},
					rules : {
						institution : "required",
						profileURL : {
							required : true,
							url : true
						},
						rank : "required",
						subject : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution'),
						profileURL : $.i18n.prop('validation_profileURL'),
						rank : $.i18n.prop('validation_rank'),
						subject : $.i18n.prop('validation_subject')
					}
				});
				break;
			case "INSTITUTION_MANAGER":
				self.$("select[name='verificationAuthority']").val(self.model.get("verificationAuthority"));
				self.$("select[name='verificationAuthority']").change(function(event) {
					var authority = self.$("select[name='verificationAuthority']").val();
					self.$("label[for='verificationAuthorityName']").html($.i18n.prop('VerificationAuthorityName') + " " + $.i18n.prop('VerificationAuthority' + authority));
					self.$("a[id^=forma_]*").hide();
					self.$("a#forma_" + authority).show();
				});

				self.$("select[name='institution']").change(function(event) {
					self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());
				});
				App.institutions = App.institutions ? App.institutions : new Models.Institutions();
				App.institutions.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(institution) {
							if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
							} else {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
							}
						});
						self.$("select[name='institution']").change();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
				self.validator = $("form", this.el).validate({
					errorElement : "span",
					errorClass : "help-inline",
					highlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").addClass("error");
					},
					unhighlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").removeClass("error");
					},
					rules : {
						institution : "required",
						verificationAuthority : "required",
						verificationAuthorityName : "required",
						phone : {
							required : true,
							number : true,
							minlength : 10
						},
						alternatefirstname : "required",
						alternatelastname : "required",
						alternatefathername : "required",
						alternatefirstnamelatin : {
							required : true,
							onlyLatin : true
						},
						alternatelastnamelatin : {
							required : true,
							onlyLatin : true
						},
						alternatefathernamelatin : {
							required : true,
							onlyLatin : true
						},
						alternateemail : {
							required : true,
							email : true,
							minlength : 2
						},
						alternatemobile : {
							required : true,
							number : true,
							minlength : 10
						},
						alternateaddress_street : "required",
						alternateaddress_number : "required",
						alternateaddress_zip : "required",
						alternateaddress_city : "required",
						alternateaddress_country : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution'),
						verificationAuthority : $.i18n.prop('validation_verificationAuthority'),
						verificationAuthorityName : $.i18n.prop('validation_verificationAuthorityName'),
						phone : {
							required : $.i18n.prop('validation_phone'),
							number : $.i18n.prop('validation_number'),
							minlength : $.i18n.prop('validation_minlength', 10)
						},
						alternatefirstname : $.i18n.prop('validation_firstname'),
						alternatelastname : $.i18n.prop('validation_lastname'),
						alternatefathername : $.i18n.prop('validation_fathername'),
						alternatefirstnamelatin : {
							required : $.i18n.prop('validation_firstnamelatin'),
							onlyLatin : $.i18n.prop('validation_latin')
						},
						alternatelastnamelatin : {
							required : $.i18n.prop('validation_lastnamelatin'),
							onlyLatin : $.i18n.prop('validation_latin')
						},
						alternatefathernamelatin : {
							required : $.i18n.prop('validation_fathernamelatin'),
							onlyLatin : $.i18n.prop('validation_latin')
						},
						alternatemobile : {
							required : $.i18n.prop('validation_phone'),
							number : $.i18n.prop('validation_number'),
							minlength : $.i18n.prop('validation_minlength', 10)
						},
						alternateemail : {
							required : $.i18n.prop('validation_email'),
							email : $.i18n.prop('validation_email'),
							minlength : $.i18n.prop('validation_minlength', 2)
						},
						alternateaddress_street : $.i18n.prop('validation_street'),
						alternateaddress_number : $.i18n.prop('validation_address'),
						alternateaddress_zip : $.i18n.prop('validation_zip'),
						alternateaddress_city : $.i18n.prop('validation_city'),
						alternateaddress_country : $.i18n.prop('validation_country')
					}
				});
				self.$("select[name='verificationAuthority']").change();
				break;

			case "INSTITUTION_ASSISTANT":
				self.$("select[name='institution']").change(function(event) {
					self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());
				});
				App.institutions = App.institutions ? App.institutions : new Models.Institutions();
				App.institutions.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(institution) {
							if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
							} else {
								$("select[name='institution']", self.$el).append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
							}
						});
						self.$("select[name='institution']").change();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
				self.validator = $("form", this.el).validate({
					errorElement : "span",
					errorClass : "help-inline",
					highlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").addClass("error");
					},
					unhighlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").removeClass("error");
					},
					rules : {
						institution : "required",
						phone : {
							required : true,
							number : true,
							minlength : 10
						}
					},
					messages : {
						institution : $.i18n.prop('validation_institution'),
						phone : {
							required : $.i18n.prop('validation_phone'),
							number : $.i18n.prop('validation_number'),
							minlength : $.i18n.prop('validation_minlength', 10)
						}
					}
				});
				break;

			case "MINISTRY_MANAGER":
				self.validator = $("form", this.el).validate({
					errorElement : "span",
					errorClass : "help-inline",
					highlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").addClass("error");
					},
					unhighlight : function(element, errorClass, validClass) {
						$(element).parent(".controls").parent(".control-group").removeClass("error");
					},
					rules : {
						ministry : "required"
					},
					messages : {
						ministry : $.i18n.prop('validation_ministry')
					}
				});
				break;
			case "MINISTRY_ASSISTANT":
				break;
			}

			// Set isEditable to fields
			self.$("select, input, textarea").each(function(index) {
				var field = $(this).attr("name");
				if (self.isEditable(field)) {
					$(this).removeAttr("disabled");
				} else {
					$(this).attr("disabled", true);
				}
			});

			self.$('a[rel=popover]').popover();
			return self;
		},

		submit : function(event) {
			var self = this;
			var candidate = undefined;
			var openCandidacies = undefined;
			var values = {};
			// Read Input
			switch (self.model.get("discriminator")) {
			case "CANDIDATE":
				break;
			case "PROFESSOR_DOMESTIC":
				values.institution = {
					"id" : self.$('form select[name=institution]').val()
				};
				values.department = {
					"id" : self.$('form select[name=department]').val()
				};
				values.rank = {
					"id" : self.$('form select[name=rank]').val()
				};
				values.profileURL = self.$('form input[name=profileURL]').val();
				values.fek = self.$('form input[name=fek]').val();
				if (self.$('form textarea[name=fekSubject]').val() !== '') {
					values.fekSubject = {
						"id" : self.model.has("fekSubject") ? self.model.get("fekSubject").id : undefined,
						"name" : self.$('form textarea[name=fekSubject]').val()
					};
				}
				if (self.$('form textarea[name=subject]').val() !== '') {
					values.subject = {
						"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
						"name" : self.$('form textarea[name=subject]').val()
					};
				}
				break;
			case "PROFESSOR_FOREIGN":
				values.institution = self.$('form input[name=institution]').val();
				values.profileURL = self.$('form input[name=profileURL]').val();
				values.rank = {
					"id" : self.$('form select[name=rank]').val()
				};
				values.subject = {
					"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
					"name" : self.$('form textarea[name=subject]').val()
				};
				break;
			case "INSTITUTION_MANAGER":
				values.institution = {
					"id" : self.$('form select[name=institution]').val()
				};
				values.verificationAuthority = self.$('form select[name=verificationAuthority]').val();
				values.verificationAuthorityName = self.$('form input[name=verificationAuthorityName]').val();
				values.phone = self.$('form input[name=phone]').val();
				values.alternateBasicInfo = {
					"firstname" : self.$('form input[name=alternatefirstname]').val(),
					"lastname" : self.$('form input[name=alternatelastname]').val(),
					"fathername" : self.$('form input[name=alternatefathername]').val()
				};
				values.alternateBasicInfoLatin = {
					"firstname" : self.$('form input[name=alternatefirstnamelatin]').val(),
					"lastname" : self.$('form input[name=alternatelastnamelatin]').val(),
					"fathername" : self.$('form input[name=alternatefathernamelatin]').val()
				};
				values.alternateContactInfo = {
					"address" : {
						"street" : self.$('form input[name=alternateaddress_street]').val(),
						"number" : self.$('form input[name=alternateaddress_number]').val(),
						"zip" : self.$('form input[name=alternateaddress_zip]').val(),
						"city" : self.$('form input[name=alternateaddress_city]').val(),
						"country" : self.$('form input[name=alternateaddress_country]').val()
					},
					"email" : self.$('form input[name=alternateemail]').val(),
					"mobile" : self.$('form input[name=alternatemobile]').val()
				};
				break;
			case "INSTITUTION_ASSISTANT":
				values.institution = {
					"id" : self.$('form select[name=institution]').val()
				};
				values.phone = self.$('form input[name=phone]').val();
				break;

			case "MINISTRY_MANAGER":
				values.ministry = self.$('form input[name=ministry]').val();
				break;
			case "MINISTRY_ASSISTANT":
				break;
			}
			// Save to model
			candidate = self.collection.find(function(role) {
				return (role.get("discriminator") === "CANDIDATE" && role.get("status") === "ACTIVE");
			});
			if (candidate) {
				openCandidacies = new Models.CandidateCandidacies({}, {
					candidate : App.loggedOnUser.getRole("CANDIDATE").id
				});
				openCandidacies.fetch({
					data : {
						"open" : "true"
					},
					cache : false,
					success : function(collection, resp) {
						var candidacyUpdateConfirmView = undefined;
						if (collection.length > 0) {
							candidacyUpdateConfirmView = new Views.CandidacyUpdateConfirmView({
								"collection" : collection,
								"answer" : function(confirm) {
									self.model.save(values, {
										url : self.model.url() + "?updateCandidacies=" + confirm,
										wait : true,
										success : function(model, resp) {
											var popup = new Views.PopupView({
												type : "success",
												message : $.i18n.prop("Success")
											});
											popup.show();
										},
										error : function(model, resp, options) {
											var popup = new Views.PopupView({
												type : "error",
												message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
											});
											popup.show();
										}
									});
								}
							});
							candidacyUpdateConfirmView.show();

						} else {
							self.model.save(values, {
								wait : true,
								success : function(model, resp) {
									var popup = new Views.PopupView({
										type : "success",
										message : $.i18n.prop("Success")
									});
									popup.show();
								},
								error : function(model, resp, options) {
									var popup = new Views.PopupView({
										type : "error",
										message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
									});
									popup.show();
								}
							});
						}
					}
				});
			} else {
				self.model.save(values, {
					wait : true,
					success : function(model, resp) {
						var popup = new Views.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
			}
			event.preventDefault();
			return false;
		},

		cancel : function(event) {
			var self = this;
			if (self.validator) {
				self.validator.resetForm();
			}
			self.render();
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							var popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
							popup.show();
						},
						error : function(model, resp, options) {
							var popup = new Views.PopupView({
								type : "error",
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		status : function(event) {
			var self = this;
			self.model.status({
				"status" : $(event.currentTarget).attr('status')
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			_.each(this.innerViews, function(innerView) {
				innerView.close();
			});
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * AdminRoleEditView *******************************************************
	 **************************************************************************/
	Views.AdminRoleEditView = Views.RoleEditView.extend({
		initialize : function(options) {
			this._super('initialize', [ options ]);
		},

		isEditable : function(field) {
			var self = this;
			switch (self.model.get("discriminator")) {
			case "CANDIDATE":
				switch (field) {
				case "tautotitaFile":
					return true;
				}
				break;
			}
			return false;
		},

		render : function(eventName) {
			var self = this;
			self._super('render', [ eventName ]);
			self.$("a#status").removeClass("disabled");
			self.$("a#save").hide();
			return self;
		}
	});

	/***************************************************************************
	 * FileView ****************************************************************
	 **************************************************************************/
	Views.FileView = Views.BaseView.extend({
		tagName : "div",

		className : "",

		initialize : function() {
			this.template = _.template(tpl_file_edit);
			_.bindAll(this, "render", "deleteFile", "toggleUpload", "close");
			this.model.bind('change', this.render, this);
		},

		events : {
			"click a#delete" : "deleteFile",
			"click a#toggleUpload" : "toggleUpload"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				withMetadata : self.options.withMetadata,
				file : self.model.toJSON()
			};
			if (_.isObject(tpl_data.file.currentBody)) {
				tpl_data.file.currentBody.url = self.model.url() + "/body/" + tpl_data.file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
			}
			self.$el.html(self.template(tpl_data));

			// Options
			if (self.options.editable) {
				self.$('a#delete').show();
				self.$("a#toggleUpload").show();
			} else {
				self.$('a#delete').hide();
				self.$("a#toggleUpload").hide();
			}
			if (self.options.withMetadata) {
				self.$("#uploader input[name=file_name]").show();
				self.$("#uploader textarea[name=file_description]").show();
			} else {
				self.$("#uploader input[name=file_name]").hide();
				self.$("#uploader textarea[name=file_description]").hide();
			}
			self.$('#uploader div.progress').hide();

			// Initialize FileUpload Modal
			self.$("#uploader").modal({
				show : false
			});
			self.$("#uploader").on('hidden', function() {
				if (self.model.changed) {
					self.model.trigger("change");
				}
			});
			// Initialize FileUpload widget
			self.$('#uploader input[name=file]').fileupload({
				dataType : 'json',
				url : self.model.url() + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
				replaceFileInput : false,
				forceIframeTransport : true,
				multipart : true,
				maxFileSize : 30000000,
				add : function(e, data) {
					self.$("a#upload").bind("click", function(e) {
						data.formData = {
							"type" : self.$("#uploader input[name=file_type]").val(),
							"name" : self.$("#uploader input[name=file_name]").val(),
							"description" : self.$("#uploader textarea[name=file_description]").val()
						};
						if (_.isFunction(self.options.beforeUpload)) {
							self.options.beforeUpload(data, function(data) {
								self.$('#uploader div.progress').show();
								self.$("#uploader a#upload").unbind("click");
								data.submit();
							});
						} else {
							self.$('#uploader div.progress').show();
							self.$("a#upload").unbind("click");
							data.submit();
						}
					});
				},
				progressall : function(e, data) {
					var progress = parseInt(data.loaded / data.total * 100, 10);
					self.$('#uploader div.progress .bar').css('width', progress + '%');
				},
				done : function(e, data) {
					self.model.set(data.result, {
						silent : true
					});
					self.$('div.progress').fadeOut('slow', function() {
						self.$('#uploader div.progress .bar').css('width', '0%');
						self.$("#uploader").modal("hide");
					});
				},
				fail : function(e, data) {
					self.$('#uploader div.progress').fadeOut('slow', function() {
						self.$('div.progress .bar').css('width', '0%');
						self.$("#uploader").modal("hide");
					});
					var resp = data.jqXHR;
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			return self;
		},

		toggleUpload : function(event) {
			var self = this;
			self.$("#uploader").modal("toggle");
		},

		deleteFile : function(event) {
			var self = this;
			var doDelete = function(options) {
				options = _.extend({}, options);
				var tmp = {
					type : self.model.get("type"),
					url : self.model.url,
					urlRoot : self.model.urlRoot
				};
				self.model.destroy({
					url : self.model.url() + (options.updateCandidacies ? "?updateCandidacies=true" : ""),
					wait : true,
					success : function(model, resp) {
						var popup;
						if (_.isNull(resp)) {
							// Reset Object to empty (without id)
							// status
							self.model.urlRoot = tmp.urlRoot;
							self.model.url = self.model.url;
							self.model.set(_.extend(self.model.defaults, {
								"type" : tmp.type
							}), {
								silent : false
							});
							popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
						} else {
							self.model.set(resp);
							popup = new Views.PopupView({
								type : "warning",
								message : $.i18n.prop("FileReverted")
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
			};
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					if (_.isFunction(self.options.beforeDelete)) {
						self.options.beforeDelete(self.model, doDelete);
					} else {
						doDelete();
					}
				}
			});
			confirm.show();
		},

		close : function(eventName) {
			this.$('input[name=file]').fileupload("destroy");
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * FileListView ************************************************************
	 **************************************************************************/
	Views.FileListView = Views.BaseView.extend({
		tagName : "div",

		className : "",

		initialize : function() {
			this.template = _.template(tpl_file_multiple_edit);
			_.bindAll(this, "render", "toggleUpload", "deleteFile", "close");
			this.collection.bind('reset', this.render, this);
			this.collection.bind('remove', this.render, this);
			this.collection.bind('add', this.render, this);
		},

		events : {
			"click a#delete" : "deleteFile",
			"click a#toggleUpload" : "toggleUpload"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				type : self.collection.type,
				withMetadata : self.options.withMetadata,
				files : []
			};
			self.collection.each(function(model) {
				var file = model.toJSON();
				if (_.isObject(file.currentBody)) {
					file.currentBody.url = model.url() + "/body/" + file.currentBody.id + "?X-Auth-Token=" + encodeURIComponent(App.authToken);
				}
				tpl_data.files.push(file);
			});
			self.$el.html(self.template(tpl_data));

			// Options
			if (self.options.editable) {
				self.$('a#delete').show();
				self.$('a#toggleUpload').show();
			} else {
				self.$('a#delete').hide();
				self.$('a#toggleUpload').hide();
			}

			if (self.options.withMetadata) {
				self.$("#uploader input[name=file_name]").show();
				self.$("#uploader textarea[name=file_description]").show();
			} else {
				self.$("#uploader input[name=file_name]").hide();
				self.$("#uploader textarea[name=file_description]").hide();
			}
			self.$('#uploader div.progress').hide();

			// Initialize FileUpload Modal
			self.$("#uploader").modal({
				show : false
			});
			self.$("#uploader").on('hidden', function() {
				self.collection.trigger("reset");
			});
			// Initialize FileUpload widget
			self.$('#uploader input[name=file]').fileupload({
				dataType : 'json',
				url : self.collection.url + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
				replaceFileInput : false,
				forceIframeTransport : true,
				maxFileSize : 30000000,
				add : function(e, data) {
					self.$("#uploader a#upload").bind("click", function(e) {
						data.formData = {
							"type" : self.$("#uploader input[name=file_type]").val(),
							"name" : self.$("#uploader input[name=file_name]").val(),
							"description" : self.$("#uploader textarea[name=file_description]").val()
						};
						if (_.isFunction(self.options.beforeUpload)) {
							self.options.beforeUpload(data, function() {
								self.$('#uploader div.progress').show();
								self.$("#uploader a#upload").unbind("click");
								data.submit();
							});
						} else {
							self.$('#uploader div.progress').show();
							self.$("#uploader a#upload").unbind("click");
							data.submit();
						}
					});
				},
				progressall : function(e, data) {
					var progress = parseInt(data.loaded / data.total * 100, 10);
					self.$('#uploader div.progress .bar').css('width', progress + '%');
				},
				done : function(e, data) {
					self.$('#uploader div.progress').fadeOut('slow', function() {
						self.$('#uploader div.progress .bar').css('width', '0%');
						var newFile = new Models.File(data.result);
						newFile.urlRoot = self.collection.url;
						self.collection.add(newFile, {
							silent : true
						});
						self.$("#uploader").modal("hide");
					});
				},
				fail : function(e, data) {
					var resp = data.jqXHR;
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
					self.$('#uploader #progress.bar').hide('slow', function() {
						self.$('#uploader #progress .bar').css('width', '0%');
						self.$("#uploader").modal("hide");
					});
				}
			});

			return self;
		},

		toggleUpload : function(event) {
			var self = this;
			self.$("#uploader").modal("toggle");
		},

		deleteFile : function(event) {
			var self = this;
			var selectedModel = self.collection.get($(event.currentTarget).data('fileId'));
			var doDelete = function(options) {
				options = _.extend({}, options);
				selectedModel.destroy({
					url : selectedModel.url() + (options.updateCandidacies ? "?updateCandidacies=true" : ""),
					wait : true,
					success : function(model, resp) {
						var popup;
						if (_.isNull(resp)) {
							popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
						} else {
							selectedModel.set(resp);
							self.collection.add(selectedModel);
							popup = new Views.PopupView({
								type : "warning",
								message : $.i18n.prop("FileReverted")
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new Views.PopupView({
							type : "error",
							message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
			};
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					if (_.isFunction(self.options.beforeDelete)) {
						self.options.beforeDelete(self.model, doDelete);
					} else {
						doDelete();
					}
				}
			});
			confirm.show();
		},

		close : function(eventName) {
			this.$el.dataTable("destroy");
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * AnnouncementListView ****************************************************
	 **************************************************************************/
	Views.AnnouncementListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			this.template = _.template(tpl_announcement_list);
			_.bindAll(this, "render", "close");
			this.collection.bind('reset', this.render);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			// 1. Create JSON:
			var data = {
				announcements : []
			};
			self.collection.each(function(role) {
				if (role.get("status") == "UNVERIFIED") {
					data.announcements.push({
						text : $.i18n.prop('AnnouncementRoleStatus' + role.get("status"), $.i18n.prop(role.get('discriminator'))),
						url : "#profile"
					});
				}
			});
			// 2. Show
			self.$el.html(self.template(data));

			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}

	});

	/***************************************************************************
	 * AssistantListView *******************************************************
	 **************************************************************************/
	Views.InstitutionAssistantListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "select", "close");
			this.template = _.template(tpl_user_list);
			this.roleInfoTemplate = _.template(tpl_user_role_info);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#select" : "select",
			"click a#createInstitutionAssistant" : "createInstitutionAssistant"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				users : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							item.roleInfo = self.roleInfoTemplate({
								roles : item.roles
							});
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			// Add Actions:
			self.$("#actions").html("<div class=\"btn-group input-append\"><a id=\"createInstitutionAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_ia') + " </a></div><div class=\"btn-group input-append\"></div>");
			return self;
		},

		select : function(event) {
			var selectedModel = this.collection.getByCid($(event.currentTarget).attr('user'));
			this.collection.trigger("user:selected", selectedModel);
		},

		createInstitutionAssistant : function(event) {
			var institutions = App.loggedOnUser.getAssociatedInstitutions();
			var user = new Models.User({
				"roles" : [ {
					"discriminator" : "INSTITUTION_ASSISTANT",
					"institution" : institutions[0]
				} ]
			});
			this.collection.add(user);
			this.collection.trigger("user:selected", user);
		},

		close : function() {
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
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "select", "close");
			this.template = _.template(tpl_user_list);
			this.roleInfoTemplate = _.template(tpl_user_role_info);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#select" : "select",
			"click a#createMinistryAssistant" : "createMinistryAssistant"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				users : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							item.roleInfo = self.roleInfoTemplate({
								roles : item.roles
							});
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			// Add Actions:
			self.$("#actions").html("<div class=\"btn-group input-append\"><a id=\"createMinistryAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_ma') + " </a></div><div class=\"btn-group input-append\"></div>");
			return self;
		},

		select : function(event) {
			var selectedModel = this.collection.getByCid($(event.currentTarget).attr('user'));
			this.collection.trigger("user:selected", selectedModel);
		},

		createMinistryAssistant : function(event) {
			var user = new Models.User({
				"roles" : [ {
					"discriminator" : "MINISTRY_ASSISTANT"
				} ]
			});
			this.collection.add(user);
			this.collection.trigger("user:selected", user);
		},

		close : function() {
			this.collection.unbind("change", this.render, this);
			this.collection.unbind("reset", this.render, this);
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * AssistantAccountView ****************************************************
	 **************************************************************************/
	Views.AssistantAccountView = Views.AccountView.extend({
		initialize : function(options) {
			this._super('initialize', [ options ]);
		},

		applyRules : function() {
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
			}
			self.$("a#save").show();
			self.$("a#remove").hide();
		},

		render : function(eventName) {
			return this._super('render', [ eventName ]);
		}
	});

	/***************************************************************************
	 * PositionListView ********************************************************
	 **************************************************************************/
	Views.PositionListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "renderActions", "selectPosition", "createPosition", "close");
			this.template = _.template(tpl_position_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
		},

		events : {
			"click a#createPosition" : "createPosition",
			"click a#selectPosition" : "selectPosition"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				positions : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(this.template(tpl_data));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			// Actions
			self.renderActions();
			return self;
		},

		renderActions : function() {
			if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
				return;
			}
			self.$("#actions").html("<select class=\"input-xlarge\" name=\"department\"></select>");
			self.$("#actions").append("<a id=\"createPosition\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_position') + "</a>");
			// Departments
			App.departments = App.departments ? App.departments : new Models.Departments();
			App.departments.fetch({
				cache : true,
				success : function(collection, resp) {
					_.each(collection.filter(function(department) {
						return App.loggedOnUser.isAssociatedWithDepartment(department);
					}), function(department) {
						self.$("select[name='department']").append("<option value='" + department.get("id") + "'>" + department.get("department") + "</option>");
					});
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		selectPosition : function(event, position) {
			var self = this;
			var selectedModel = position ? position : self.collection.getByCid($(event.currentTarget).attr('data-position-cid'));
			if (selectedModel) {
				self.collection.trigger("position:selected", selectedModel);
			}
		},

		createPosition : function(event) {
			var self = this;
			var newPosition = new Models.Position();
			newPosition.save({
				department : {
					id : self.$("select[name='department']").val()
				}
			}, {
				wait : true,
				success : function(model, resp) {
					self.collection.add(newPosition);
					self.selectPosition(undefined, newPosition);
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionView ************************************************************
	 **************************************************************************/
	Views.PositionView = Views.BaseView.extend({
		tagName : "div",

		id : "positionview",

		fileDiscriminators : [ "committee", "evaluation", "complementaryDocuments", "nomination" ],

		initialize : function() {
			_.bindAll(this, "render", "addCommitteeView", "addCandidacyListView", "addFile", "addFileList", "close");
			this.template = _.template(tpl_position);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			self.innerViews = [];
			self.$el.html(self.template(self.model.toJSON()));
			// Dependencies (Files, Committee):
			var files = {};
			_.each(self.fileDiscriminators, function(fileDiscriminator) {
				switch (fileDiscriminator) {
				case "committee":
					if (!self.model.get("phase").committee) {
						return;
					}
					break;
				case "evaluation":
					if (!self.model.get("phase").evaluation) {
						return;
					}
					break;
				case "complementaryDocuments":
					if (!self.model.get("phase").complementaryDocuments) {
						return;
					}
					break;
				case "nomination":
					if (!self.model.get("phase").nomination) {
						return;
					}
					break;
				}
				files[fileDiscriminator] = new Models.Files();
				files[fileDiscriminator].url = self.model.url() + "/" + fileDiscriminator + "/file";
				files[fileDiscriminator].fetch({
					cache : false,
					success : function(collection, response) {
						switch (fileDiscriminator) {
						case "committee":
							self.addFile(collection, "APOFASI_SYSTASIS_EPITROPIS", self.$("#apofasiSystasisEpitropisFileList"), {
								withMetadata : true,
								editable : false
							});
							self.addFile(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile"), {
								withMetadata : true,
								editable : false
							});
							self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
								withMetadata : true,
								editable : false
							});
							break;
						case "evaluation":
							self.addFileList(collection, "AKSIOLOGISI_PROTOU_AKSIOLOGITI", self.$("#aksiologisiProtouAksiologitiFileList"), {
								withMetadata : true,
								editable : false
							});
							self.addFileList(collection, "AKSIOLOGISI_DEUTEROU_AKSIOLOGITI", self.$("#aksiologisiDeuterouAksiologitiFileList"), {
								withMetadata : true,
								editable : false
							});
							break;
						case "complementaryDocuments":
							self.addFileList(collection, "EISIGISI_DEP_YPOPSIFIOU", self.$("#eisigisiDEPYpopsifiouFileList"), {
								withMetadata : true,
								editable : false
							});
							self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
								withMetadata : true,
								editable : false
							});
							break;
						case "nomination":
							self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
								withMetadata : true,
								editable : false
							});
							self.addFileList(collection, "PRAKTIKO_EPILOGIS", self.$("#praktikoEpilogisFile"), {
								withMetadata : true,
								editable : false
							});
							self.addFile(collection, "DIAVIVASTIKO_PRAKTIKOU", self.$("#diavivastikoPraktikouFile"), {
								withMetadata : true,
								editable : false
							});
							self.addFile(collection, "PRAKSI_DIORISMOU", self.$("#praksiDiorismouFile"), {
								withMetadata : true,
								editable : false
							});

							self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
								withMetadata : true,
								editable : false
							});
							break;
						}
					}
				});
			});
			self.addEvaluatorsView(self.$("#positionEvaluators"));
			self.addCommitteeView(self.$("#positionCommittee"));
			self.addCandidacyListView(self.$("#positionCandidacyList"));
			// End of associations
			return self;
		},

		addEvaluatorsView : function($el) {
			var self = this;
			var evaluators = new Models.PositionEvaluators({}, {
				position : self.model.get("id")
			});
			var evaluatorsView = new Views.PositionEvaluatorsView({
				position : self.model,
				collection : evaluators
			});

			$el.html(evaluatorsView.el);
			evaluators.fetch({
				cache : false
			});
		},

		addCommitteeView : function($el) {
			var self = this;
			var committee = new Models.PositionCommittee({}, {
				position : self.model.get("id")
			});
			var committeeView = new Views.PositionCommitteeView({
				position : self.model,
				collection : committee
			});

			$el.html(committeeView.el);
			committee.fetch({
				cache : false
			});
		},

		addCandidacyListView : function($el) {
			var self = this;
			var candidacyView = undefined;
			var positionCandidacies = new Models.PositionCandidacies({}, {
				position : self.model.get("id")
			});
			positionCandidacies.on("candidacy:selected", function(candidacy) {
				if (candidacyView) {
					candidacyView.model.trigger("candidacy:deselected", candidacyView.model);
					candidacyView.close();
				}
				candidacy.fetch({
					cache : false,
					success : function() {
						candidacyView = new Views.CandidacyView({
							model : candidacy
						});
						self.$("div[data-candidacy-id=" + candidacy.get("id") + "]").html(candidacyView.render().el);
						self.$("td[data-candidacy-id=" + candidacy.get("id") + "]").show();
					}
				});
			});
			positionCandidacies.on("candidacy:deselected", function(candidacy) {
				if (candidacyView) {
					candidacyView.close();
				}
				self.$("td[data-candidacy-id=" + candidacy.get("id") + "]").hide();
				self.$("div[data-candidacy-id=" + candidacy.get("id") + "]").html();
			});
			var positionCandidacyListView = new Views.PositionCandidacyListView({
				position : self.model,
				collection : positionCandidacies
			});
			$el.html(positionCandidacyListView.el);
			positionCandidacies.fetch({
				cache : false,
				success : function() {
					positionCandidacies.trigger("reset");
				}
			});
		},

		close : function() {
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionEditView ********************************************************
	 **************************************************************************/
	Views.PositionEditView = Views.BaseView.extend({
		tagName : "div",

		id : "positionEditView",

		tab : "main",

		phases : {
			"ENTAGMENI" : [ "ANOIXTI" ],
			"ANOIXTI" : [ "EPILOGI" ],
			"EPILOGI" : [ "ANAPOMPI", "STELEXOMENI", "CANCELLED" ],
			"ANAPOMPI" : [ "EPILOGI" ],
			"STELEXOMENI" : [],
			"CANCELLED" : [ "EPILOGI" ]
		},

		initialize : function() {
			_.bindAll(this, "render", "addPhase", "showTab", "showMainTab", "showCandidaciesTab", "showCommitteeTab", "showEvaluationTab", "showNominationTab", "showComplementaryDocumentsTab", "close");
			this.template = _.template(tpl_position_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#selectTab" : "showTab",
			"click a#addPhase" : "addPhase",
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			// Phase:
			self.$("a#addPhase").each(function() {
				var status = $(this).data("phaseStatus");
				if (_.any(self.phases[self.model.get("phase").status], function(nextStatus) {
					return _.isEqual(status, nextStatus);
				})) {
					$(this).show();
				} else {
					$(this).hide();
				}
			});
			// Tabs:
			if (_.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI")) {
				self.$("#positionTabs a[data-target=committee]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=evaluation]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=nomination]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=complementaryDocuments]").parent("li").addClass("disabled");
			}

			// Show Tab:
			setTimeout(function() {
				self.showTab(undefined, self.options.tab);
			}, 0);

			return self;
		},

		showTab : function(event, target) {
			var self = this;
			target = target || $(event.currentTarget).data('target');
			if (event && self.$("#positionTabs a[data-target=" + target + "]").parent("li").hasClass("disabled")) {
				event.preventDefault();
				return;
			}
			self.$("#positionTabs a#selectTab").parent("li").removeClass("active");
			self.$("#positionTabs a#selectTab[data-target=" + target + "]").parent("li").addClass("active");
			App.router.navigate("positions/" + self.model.get("id") + "/" + target, {
				trigger : false
			});
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

		showMainTab : function($el) {
			var self = this;
			$el.html("Main");
			var positionMainEditView = new Views.PositionMainEditView({
				model : self.model
			});
			$el.html(positionMainEditView.el);
			positionMainEditView.render();
		},

		showCandidaciesTab : function($el) {
			var self = this;
			var positionCandidaciesEditView = undefined;
			var candidacyView = undefined;
			var positionCandidacies = new Models.PositionCandidacies({}, {
				position : self.model.get("id")
			});
			positionCandidacies.on("candidacy:selected", function(candidacy) {
				if (candidacyView) {
					candidacyView.model.trigger("candidacy:deselected", candidacyView.model);
					candidacyView.close();
				}
				candidacy.fetch({
					cache : false,
					success : function() {
						candidacyView = new Views.CandidacyView({
							model : candidacy
						});
						self.$("div[data-candidacy-id=" + candidacy.get("id") + "]").html(candidacyView.render().el);
						self.$("td[data-candidacy-id=" + candidacy.get("id") + "]").show();
					}
				});
			});
			positionCandidacies.on("candidacy:deselected", function(candidacy) {
				if (candidacyView) {
					candidacyView.close();
				}
				self.$("td[data-candidacy-id=" + candidacy.get("id") + "]").hide();
				self.$("div[data-candidacy-id=" + candidacy.get("id") + "]").html();
			});
			positionCandidaciesEditView = new Views.PositionCandidaciesEditView({
				position : self.model,
				collection : positionCandidacies
			});
			$el.html(positionCandidaciesEditView.el);
			positionCandidacies.fetch();
		},

		showCommitteeTab : function($el) {
			var self = this;
			var positionCommittee = new Models.PositionCommittee({
				id : self.model.get("phase").committee.id,
				position : {
					id : self.model.get("id")
				}
			});
			var positionCommitteeEditView = new Views.PositionCommitteeEditView({
				model : positionCommittee
			});
			positionCommittee.fetch({
				cache : false,
				success : function(model, resp) {
					$el.html(positionCommitteeEditView.render().el);
				}
			});
		},

		showEvaluationTab : function($el) {
			var self = this;
			var positionEvaluation = new Models.PositionEvaluation({
				id : self.model.get("phase").evaluation.id,
				position : {
					id : self.model.get("id")
				}
			});
			var positionEvaluationEditView = new Views.PositionEvaluationEditView({
				model : positionEvaluation
			});
			positionEvaluation.fetch({
				cache : false,
				success : function(model, resp) {
					$el.html(positionEvaluationEditView.render().el);
				}
			});
		},

		showNominationTab : function($el) {
			var self = this;
			var positionNomination = new Models.PositionNomination({
				id : self.model.get("phase").nomination.id,
				position : {
					id : self.model.get("id")
				}
			});
			var positionNominationEditView = new Views.PositionNominationEditView({
				model : positionNomination
			});
			positionNomination.fetch({
				cache : false,
				success : function(model, resp) {
					$el.html(positionNominationEditView.render().el);
				}
			});
		},

		showComplementaryDocumentsTab : function($el) {
			var self = this;
			var positionComplementaryDocuments = new Models.PositionComplementaryDocuments({
				id : self.model.get("phase").complementaryDocuments.id,
				position : {
					id : self.model.get("id")
				}
			});
			var positionComplementaryDocumentsEditView = new Views.PositionComplementaryDocumentsEditView({
				model : positionComplementaryDocuments
			});
			positionComplementaryDocuments.fetch({
				cache : false,
				success : function(model, resp) {
					$el.html(positionComplementaryDocumentsEditView.render().el);
				}
			});
		},

		addPhase : function(event) {
			var self = this;
			var newStatus = $(event.currentTarget).data('phaseStatus');
			self.model.phase({
				"phase" : {
					"status" : newStatus
				}
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	Views.PositionMainEditView = Views.BaseView.extend({
		tagName : "div",

		id : "positionmaineditview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "isEditable", "submit", "cancel", "close");
			this.template = _.template(tpl_position_main_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#cancel" : "cancel",
			"click a#remove" : "remove",
			"click a#save" : function() {
				var self = this;
				self.$("form").submit();
			},
			"submit form" : "submit"
		},

		isEditable : function(field) {
			var self = this;
			if (_.isEqual(self.model.get("phase").status, "ANAPOMPI") || _.isEqual(self.model.get("phase").status, "STELEXOMENI")) {
				return false;
			}
			switch (field) {
			// Fields
			case "name":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "department":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "description":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "subject":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "fek":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "fekSentDate":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "openingDate":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			case "closingDate":
				return self.model.isNew() || _.isEqual(self.model.get("phase").status, "ENTAGMENI") || _.isEqual(self.model.get("phase").status, "ANOIXTI");
			}
			return false;
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			// Set isEditable to fields
			self.$("select, input, textarea").each(function(index) {
				var field = $(this).attr("name");
				if (self.isEditable(field)) {
					$(this).removeAttr("disabled");
				} else {
					$(this).attr("disabled", true);
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
				onClose : function(dateText, inst) {
					$(this).parents("form").validate().element(this);
				}
			});
			// Validation
			self.validator = $("form", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					name : "required",
					description : "required",
					department : "required",
					subject : "required",
					status : "required",
					fek : {
						required : true,
						url : true
					},
					fekSentDate : "required",
					openingDate : {
						"required" : true,
						"dateAfter" : [ self.$("input[name=fekSentDate]"), 1 ]
					},
					closingDate : {
						"required" : true,
						"dateAfter" : [ self.$("input[name=openingDate]"), 30 ]
					}
				},
				messages : {
					name : $.i18n.prop('validation_positionName'),
					description : $.i18n.prop('validation_description'),
					department : $.i18n.prop('validation_department'),
					subject : $.i18n.prop('validation_subject'),
					status : $.i18n.prop('validation_positionStatus'),
					fek : $.i18n.prop('validation_fek'),
					fekSentDate : $.i18n.prop('validation_fekSentDate'),
					openingDate : {
						required : $.i18n.prop('validation_openingDate'),
						dateAfter : $.i18n.prop('validation_openingDate_dateAfter')
					},
					closingDate : {
						required : $.i18n.prop('validation_closingDate'),
						dateAfter : $.i18n.prop('validation_closingDate_dateAfter')
					}
				}
			});
			return self;
		},

		submit : function(event) {
			var self = this;
			var values = {
				phase : {
					candidacies : {}
				}
			};
			// Read Input
			values.name = self.$('form input[name=name]').val();
			values.description = self.$('form textarea[name=description]').val();
			values.department = {
				"id" : self.$('form select[name=department]').val()
			};
			values.subject = {
				"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
				"name" : self.$('form textarea[name=subject]').val()
			};
			values.fek = self.$('form input[name=fek]').val();
			values.fekSentDate = self.$('form input[name=fekSentDate]').val();
			values.phase.candidacies.openingDate = self.$('form input[name=openingDate]').val();
			values.phase.candidacies.closingDate = self.$('form input[name=closingDate]').val();

			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("positions/" + self.model.id + "/main", {
						trigger : false
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
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		cancel : function(event) {
			var self = this;
			if (self.validator) {
				self.validator.resetForm();
			}
			self.render();
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							App.router.navigate("positions", {
								trigger : false
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
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionCommitteeView ***************************************************
	 **************************************************************************/
	Views.PositionCommitteeView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_committee);
			_.bindAll(self, "render", "close");
			self.collection.bind('reset', this.render, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				committee : self.collection.toJSON()
			}));
			return self;
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionCommitteeEditView ***********************************************
	 **************************************************************************/
	Views.PositionCommitteeEditView = Views.BaseView.extend({
		tagName : "div",

		uploader : undefined,

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_committee_edit);
			self.templateRow = _.template(tpl_position_committee_member_edit);

			_.bindAll(self, "render", "isEditable", "toggleRegisterMembers", "addMember", "removeMember", "submit", "cancel", "close");
			self.model.bind('change', self.render, self);
			self.model.bind("destroy", self.close, self);

			// Initialize Registers, no request is performed until render
			self.registerMembers = new Models.PositionCommitteeRegisterMembers();
			self.registerMembers.url = self.model.url() + "/register";
			self.registerMembers.on("member:add", function(registerMember, type) {
				var committeeMember = {
					type : type,
					registerMember : registerMember.toJSON()
				};
				self.addMember(committeeMember);
			});
		},

		events : {
			"click a#toggleRegisterMembers" : "toggleRegisterMembers",
			"click a#removeMember" : "removeMember",
			"click a#saveCommittee" : function() {
				var self = this;
				self.$("form").submit();
			},
			"submit form" : "submit"
		},

		isEditable : function(element) {
			var self = this;
			return self.model.get("position").phase.status === "EPILOGI";
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Existing Committee Members:
			_.each(self.model.get("members"), function(committeeMember) {
				addMember(committeeMember);
			});

			// Add Files
			if (self.model.has("id")) {
				var files = new Models.Files();
				files.url = self.model.url() + "/file";
				files.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFile(collection, "APOFASI_SYSTASIS_EPITROPIS", self.$("#apofasiSystasisEpitropisFileList"), {
							withMetadata : true,
							editable : self.isEditable("apofasiSystasisEpitropisFileList")
						});
						self.addFile(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile"), {
							withMetadata : true,
							editable : self.isEditable("praktikoSynedriasisEpitropisGiaAksiologitesFile")
						});
						self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
							withMetadata : true,
							editable : self.isEditable("aitimaEpitropisProsAksiologitesFile")
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
					collection : self.registerMembers
				});
				self.$("div#committee-register-members").hide();
				self.$("div#committee-register-members").html(self.registerMembersView.el);
				self.$("select").removeAttr("disabled");
				self.$("a.btn").show();

				self.registerMembers.fetch();
			} else {
				self.$("div#committee-register-members").hide();
				self.$("select").attr("disabled", true);
				self.$("a.btn").hide();
			}
			// DatePicker
			self.$("input[data-input-type=date]").datepicker({
				onClose : function(dateText, inst) {
					$(this).parents("form").validate().element(this);
				}
			});
			return self;
		},

		toggleRegisterMembers : function(event) {
			var self = this;
			self.$("div#committee-register-members").toggle();
			self.$("a#toggleRegisterMembers").toggleClass('active');
		},

		addMember : function(committeeMember) {
			var self = this;
			self.$("div#positionCommittee table tbody").append(self.templateRow(committeeMember));
		},

		removeMember : function(event) {
			var self = this;
			$(event.currentTarget).parents("tr").remove();
		},

		submit : function(event) {
			var self = this;
			var values = {
				committeeMeetingDate : self.$("input[name=committeeMeetingDate]").val(),
				members : []
			};
			self.$("tr#positionCommitteeMember").each(function() {
				var committeeMember = {
					registerMember : {
						id : $(this).find("input[name=registerMemberId]").val()
					},
					type : $(this).find("select[name=type]").val()
				};
				values.members.push(committeeMember);
			});

			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		cancel : function(event) {
			self.model.fetch({
				cache : false
			});
		},

		close : function(eventName) {
			this.registerMembers.off("member:selected");
			this.registerMembersView.close();
			this.model.unbind('change', this.render, this);
			this.model.unbind('destory', this.close, this);
			this.$el.unbind();
			this.$el.remove();

		}
	});

	/***************************************************************************
	 * PositionCommitteeEditRegisterMembersView ********************************
	 **************************************************************************/

	Views.PositionCommitteeEditRegisterMembersView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "addMember", "close");
			this.template = _.template(tpl_position_committee_edit_register_member_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#addMember" : "addMember"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				members : (function() {
					var result = [];
					self.collection.each(function(model) {
						var item = model.toJSON();
						item.cid = model.cid;
						result.push(item);
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));

			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		addMember : function(event) {
			var self = this;
			var cid = $(event.currentTarget).data('modelCid')
			var selectedModel = self.collection.getByCid(cid);
			var type = $(event.currentTarget).data('type');
			self.collection.trigger("member:add", selectedModel, type);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionEvaluatorsView **************************************************
	 **************************************************************************/
	Views.PositionEvaluatorsView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_evaluators);
			_.bindAll(self, "render", "close");
			self.collection.bind('reset', this.render, this);
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				evaluators : self.collection.toJSON()
			}));
			return self;
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionEvaluationEditView **********************************************
	 **************************************************************************/
	Views.PositionEvaluationEditView = Views.BaseView.extend({
		tagName : "div",

		uploader : undefined,

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_evaluation_edit);
			self.templateRow = _.template(tpl_position_evaluation_evaluator_edit);

			_.bindAll(self, "render", "renderEvaluator", "isEditable", "toggleRegisterMembers", "addMember", "removeMember", "submit", "cancel", "close");
			self.model.bind('change', self.render, self);
			self.model.bind("destroy", self.close, self);

			// Initialize Registers, no request is performed until render
			self.registerMembers = new Models.PositionEvaluationRegisterMembers();
			self.registerMembers.url = self.model.url() + "/register";
			self.registerMembers.on("member:add", function(registerMember, position) {
				var evaluator = {
					position : position,
					registerMember : registerMember.toJSON()
				};
				self.addMember(evaluator);
			});
		},

		events : {
			"click a#toggleRegisterMembers" : "toggleRegisterMembers",
			"click a#removeMember" : "removeMember",
			"click a#saveEvaluation" : function() {
				var self = this;
				self.$("form").submit();
			},
			"submit form" : "submit"
		},

		isEditable : function(element) {
			var self = this;
			return self.model.get("position").phase.status === "EPILOGI";
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Existing Evaluators:
			_.each(self.model.get("evaluators"), function(evaluator) {
				self.addMember(evaluator);
			});
			// Add RegisterMembers (for adding/removing)
			if (self.isEditable("positionEvaluation")) {
				// Inner View
				if (self.registerMembersView) {
					self.registerMembersView.close();
				}
				self.registerMembersView = new Views.PositionEvaluationEditRegisterMembersView({
					collection : self.registerMembers
				});
				self.$("div#evaluation-register-members").hide();
				self.$("div#evaluation-register-members").html(self.registerMembersView.el);
				self.$("a.btn").show();

				self.registerMembers.fetch();
			} else {
				self.$("div#evaluation-register-members").hide();
				self.$("select").attr("disabled", true);
				self.$("a.btn").hide();
			}
			// DatePicker
			self.$("input[data-input-type=date]").datepicker({
				onClose : function(dateText, inst) {
					$(this).parents("form").validate().element(this);
				}
			});
			return self;
		},

		renderEvaluator : function($el, evaluator) {
			var self = this;
			$el.html(self.templateRow(evaluator.toJSON()));
			// Add files
			if (evaluator.has("id")) {
				var files = new Models.Files();
				files.url = evaluator.url() + "/file";
				files.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFile(collection, "AKSIOLOGISI", $el.find("#aksiologisiFileList"), {
							withMetadata : true,
							editable : self.isEditable("aksiologisiFileList")
						});
					}
				});
			} else {
				$el.find("#aksiologisiFileList").html($.i18n.prop("PressSave"));
			}
		},

		toggleRegisterMembers : function(event) {
			var self = this;
			self.$("div#evaluation-register-members").toggle();
			self.$("a#toggleRegisterMembers").toggleClass('active');
		},

		addMember : function(evaluator) {
			var self = this;
			var positionEvaluator = new Models.PositionEvaluator(_.extend(evaluator, {
				evaluation : self.model.toJSON()
			}));
			// Select element to replace, raise some alerts for files ...
			self.renderEvaluator(self.$("#positionEvaluator_" + positionEvaluator.get("position")), positionEvaluator);
		},

		removeMember : function(event) {
			var self = this;
			$(event.currentTarget).parents("table").remove();
		},

		submit : function(event) {
			var self = this;
			var values = {
				evaluators : []
			};
			self.$("[id^=positionEvaluator]").each(function() {
				var evaluator = {
					registerMember : {
						id : $(this).find("input[name=registerMemberId]").val()
					},
					position : $(this).find("input[name=position]").val()
				};
				if (evaluator.registerMember.id) {
					values.evaluators.push(evaluator);
				}
			});

			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		cancel : function(event) {
			self.model.fetch({
				cache : false
			});
		},

		close : function(eventName) {
			this.registerMembers.off("member:selected");
			this.registerMembersView.close();
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
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "addMember", "close");
			this.template = _.template(tpl_position_evaluation_edit_register_member_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#addMember" : "addMember"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				members : (function() {
					var result = [];
					self.collection.each(function(model) {
						var item = model.toJSON();
						item.cid = model.cid;
						result.push(item);
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));

			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		addMember : function(event) {
			var self = this;
			var cid = $(event.currentTarget).data('modelCid')
			var selectedModel = self.collection.getByCid(cid);
			var position = $(event.currentTarget).data('position');
			self.collection.trigger("member:add", selectedModel, position);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionCandidaciesEditView *********************************************
	 **************************************************************************/
	Views.PositionCandidaciesEditView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_candidacies_edit);
			_.bindAll(self, "render", "viewCandidacy", "closeCandidacy", "close");
			self.collection.bind('reset', this.render, this);
		},

		events : {
			"click a#viewCandidacy" : "viewCandidacy",
			"click a#closeCandidacy" : "closeCandidacy"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				candidacies : self.collection.toJSON()
			}));
			return self;
		},

		viewCandidacy : function(event, candidacy) {
			var self = this;
			var selectedModel = candidacy ? candidacy : self.collection.get($(event.currentTarget).data('candidacyId'));
			if (selectedModel) {
				self.collection.trigger("candidacy:selected", selectedModel);
			}
		},

		closeCandidacy : function(event, candidacy) {
			var self = this;
			var selectedModel = candidacy ? candidacy : self.collection.get($(event.currentTarget).data('candidacyId'));
			if (selectedModel) {
				self.collection.trigger("candidacy:deselected", selectedModel);
			}
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionNominationEditView **********************************************
	 **************************************************************************/
	Views.PositionNominationEditView = Views.BaseView.extend({
		tagName : "div",

		uploader : undefined,

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_nomination_edit);
			_.bindAll(self, "render", "isEditable", "submit", "cancel", "close");
			self.model.bind('change', self.render, self);
			self.model.bind("destroy", self.close, self);

			self.positionCandidacies = new Models.PositionCandidacies({}, {
				position : self.model.get("position").id
			});
		},

		events : {
			"click a#saveNomination" : function() {
				var self = this;
				self.$("form").submit();
			},
			"submit form" : "submit"
		},

		isEditable : function(element) {
			var self = this;
			return self.model.get("position").phase.status === "EPILOGI";
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Nominated and Second Nominated:
			self.$("select[name='nominatedCandidacy']").change(function(event) {
				self.$("select[name='nominatedCandidacy']").next(".help-block").html(self.$("select[name='nominatedCandidacy'] option:selected").text());
			});
			self.$("select[name='secondNominatedCandidacy']").change(function(event) {
				self.$("select[name='secondNominatedCandidacy']").next(".help-block").html(self.$("select[name='secondNominatedCandidacy'] option:selected").text());
			});
			self.positionCandidacies.fetch({
				cache : false,
				wait : true,
				success : function(collection, resp) {
					self.$("select[name='nominatedCandidacy']").empty();
					self.$("select[name='secondNominatedCandidacy']").empty();
					// Add Candidacies in selector:
					var nominatedCandidacyId = self.model.has("nominatedCandidacy") ? self.model.get("nominatedCandidacy").id : undefined;
					var secondNominatedCandidacyId = self.model.has("secondNominatedCandidacy") ? self.model.get("secondNominatedCandidacy").id : undefined;
					self.$("select[name='nominatedCandidacy']").append("<option value=''>--</option>");
					self.$("select[name='secondNominatedCandidacy']").append("<option value=''>--</option>");
					collection.each(function(candidacy) {
						if (_.isEqual(candidacy.id, nominatedCandidacyId)) {
							self.$("select[name='nominatedCandidacy']").append("<option value='" + candidacy.get("id") + "' selected>" + candidacy.get("snapshot").basicInfo.firstname + " " + candidacy.get("snapshot").basicInfo.lastname + " (" + candidacy.get("snapshot").username + ")" + "</option>");
						} else {
							self.$("select[name='nominatedCandidacy']").append("<option value='" + candidacy.get("id") + "'>" + candidacy.get("snapshot").basicInfo.firstname + " " + candidacy.get("snapshot").basicInfo.lastname + " (" + candidacy.get("snapshot").username + ")" + "</option>");
						}
						if (_.isEqual(candidacy.id, secondNominatedCandidacyId)) {
							self.$("select[name='secondNominatedCandidacy']").append("<option value='" + candidacy.get("id") + "' selected>" + candidacy.get("snapshot").basicInfo.firstname + " " + candidacy.get("snapshot").basicInfo.lastname + " (" + candidacy.get("snapshot").username + ")" + "</option>");
						} else {
							self.$("select[name='secondNominatedCandidacy']").append("<option value='" + candidacy.get("id") + "'>" + candidacy.get("snapshot").basicInfo.firstname + " " + candidacy.get("snapshot").basicInfo.lastname + " (" + candidacy.get("snapshot").username + ")" + "</option>");
						}
					});
					self.$("select[name='nominatedCandidacy']").change();
					self.$("select[name='secondNominatedCandidacy']").change();
				}
			});
			// Add Files
			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
						withMetadata : true,
						editable : self.isEditable("prosklisiKosmitoraFile")
					});

					self.addFileList(collection, "PRAKTIKO_EPILOGIS", self.$("#praktikoEpilogisFile"), {
						withMetadata : true,
						editable : self.isEditable("praktikoEpilogisFile")
					});
					self.addFile(collection, "DIAVIVASTIKO_PRAKTIKOU", self.$("#diavivastikoPraktikouFile"), {
						withMetadata : true,
						editable : self.isEditable("diavivastikoPraktikouFile")
					});
					self.addFile(collection, "PRAKSI_DIORISMOU", self.$("#praksiDiorismouFile"), {
						withMetadata : true,
						editable : self.isEditable("praksiDiorismouFile")
					});

					self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
						withMetadata : true,
						editable : self.isEditable("apofasiAnapompisFile")
					});
				}
			});
			// DatePicker
			self.$("input[data-input-type=date]").datepicker({
				onClose : function(dateText, inst) {
					$(this).parents("form").validate().element(this);
				}
			});
			return self;
		},
		submit : function(event) {
			var self = this;
			var values = {
				nominationCommitteeConvergenceDate : self.$('form input[name=nominationCommitteeConvergenceDate]').val(),
				nominationToETDate : self.$('form input[name=nominationToETDate]').val(),
				nominationFEK : self.$('form input[name=nominationFEK]').val(),
				nominatedCandidacy : {
					id : self.$('form select[name=nominatedCandidacy]').val()
				},
				secondNominatedCandidacy : {
					id : self.$('form select[name=secondNominatedCandidacy]').val()
				}
			};
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		cancel : function(event) {
			self.model.fetch({
				cache : false
			});
		},

		close : function(eventName) {
			this.model.unbind('change', self.render, self);
			this.model.unbind("destroy", self.close, self);
			this.$el.unbind();
			this.$el.remove();

		}
	});

	/***************************************************************************
	 * PositionComplementaryDocumentsEditView **********************************
	 **************************************************************************/
	Views.PositionComplementaryDocumentsEditView = Views.BaseView.extend({
		tagName : "div",

		uploader : undefined,

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_complementaryDocuments_edit);

			_.bindAll(self, "render", "cancel", "close");
			self.model.bind('change', self.render, self);
			self.model.bind("destroy", self.close, self);
		},

		events : {},

		isEditable : function(element) {
			var self = this;
			return self.model.get("position").phase.status === "EPILOGI";
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Files
			if (self.model.has("id")) {
				var files = new Models.Files();
				files.url = self.model.url() + "/file";
				files.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFileList(collection, "EISIGISI_DEP_YPOPSIFIOU", self.$("#eisigisiDEPYpopsifiouFileList"), {
							withMetadata : true,
							editable : self.isEditable("eisigisiDEPYpopsifiouFileList")
						});
						self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
							withMetadata : true,
							editable : self.isEditable("dioikitikoEggrafoFileList")
						});
					}
				});
			}
			return self;
		},

		cancel : function(event) {
			self.model.fetch({
				cache : false
			});
		},

		close : function(eventName) {
			this.model.unbind('change', this.render, this);
			this.model.unbind('destory', this.close, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * RegisterListView ********************************************************
	 **************************************************************************/
	Views.RegisterListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "renderActions", "selectRegister", "createRegister", "close");
			this.template = _.template(tpl_register_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
		},

		events : {
			"click a#createRegister" : "createRegister",
			"click a#select" : "selectRegister"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				registries : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(this.template(tpl_data));

			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			// Add Actions
			self.renderActions();
			return self;
		},

		renderActions : function() {
			var self = this;
			if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
				return;
			}
			if (self.collection.any(function(register) {
				return App.loggedOnUser.isAssociatedWithInstitution(register.get("institution"));
			})) {
				return;
			}
			self.$("#actions").append("<div class=\"btn-group\"><input type=\"hidden\" name=\"institution\" /><a id=\"createRegister\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_register') + " </a></div>");
			// Add institutions in selector:
			App.institutions = App.institutions ? App.institutions : new Models.Institutions();
			App.institutions.fetch({
				cache : true,
				success : function(collection, resp) {
					var institution = collection.find(function(institution) {
						return App.loggedOnUser.isAssociatedWithInstitution(institution);
					});
					self.$("#actions input[name=institution]").val(institution.get("id"));
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		selectRegister : function(event, register) {
			var selectedModel = register ? register : this.collection.getByCid($(event.currentTarget).attr('data-register-cid'));
			if (selectedModel) {
				this.collection.trigger("register:selected", selectedModel);
			}
		},

		createRegister : function(event) {
			var self = this;
			var newRegister = new Models.Register();
			newRegister.save({
				institution : {
					id : self.$("input[name='institution']").val()
				}
			}, {
				wait : true,
				success : function(model, resp) {
					self.collection.add(newRegister);
					self.selectRegister(undefined, newRegister);
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RegisterView ************************************************************
	 **************************************************************************/
	Views.RegisterView = Views.BaseView.extend({
		tagName : "div",

		id : "registerview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "addMembersView", "addFileList", "close");
			this.template = _.template(tpl_register);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			// Add Members
			self.addMembersView(self.$("#members"));
			return self;
		},

		addMembersView : function($el) {
			var self = this;
			var members = new Models.RegisterMembers({}, {
				register : self.model.get("id")
			});
			var registerMembersView = new Views.RegisterMembersView({
				register : self.model,
				collection : members
			});

			$el.html(registerMembersView.el);
			members.fetch({
				cache : false
			});
		},

		close : function() {
			this.model.unbind("change");
			this.model.unbind("destroy");
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RegisterMembersView *****************************************************
	 **************************************************************************/
	Views.RegisterMembersView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_register_members);
			_.bindAll(self, "render", "viewMember", "close");
			self.collection.bind('reset', this.render, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				members : self.collection.toJSON()
			}));
			return self;
		},

		viewMember : function(event, positionCommitteeMember) {
			var self = this;
			var selectedModel = positionCommitteeMember ? positionCommitteeMember : self.collection.get($(event.currentTarget).data('committeeMemberId'));
			if (selectedModel) {
				self.collection.trigger("positionCommitteeMember:selected", selectedModel);
			}
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * RegisterEditView ********************************************************
	 **************************************************************************/
	Views.RegisterEditView = Views.BaseView.extend({
		tagName : "div",

		id : "registerview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "submit", "remove", "cancel", "addFile", "addMembersView", "close");
			this.template = _.template(tpl_register_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#cancel" : "cancel",
			"click a#remove" : "remove",
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "submit"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			// Add Members
			if (self.model.has("id")) {
				self.addMembersView(self.$("#registerMembers"));
			} else {
				self.$("#registerMembers").html($.i18n.prop("PressSave"));
			}

			// Widgets
			self.validator = $("form", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					"title" : "required",
					"institution" : "required"
				},
				messages : {
					"title" : $.i18n.prop('validation_title'),
					"institution" : $.i18n.prop('validation_institution')
				}
			});
			return self;
		},

		addMembersView : function($el) {
			var self = this;
			var members = new Models.RegisterMembers({}, {
				register : self.model.get("id")
			});
			var registerMembersView = new Views.RegisterMembersEditView({
				register : self.model,
				collection : members
			});

			$el.html(registerMembersView.el);
			members.fetch({
				cache : false
			});
		},

		submit : function(event) {
			var self = this;
			var values = {};
			// Read Input
			values.title = self.$('form input[name=title]').val();
			values.institution = {
				"id" : self.$('form input[name=institution]').val()
			};
			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("registers/" + self.model.id, {
						trigger : false
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
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		cancel : function(event) {
			var self = this;
			if (self.validator) {
				self.validator.resetForm();
			}
			self.render();
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							App.router.navigate("registers", {
								trigger : false
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
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * RegisterMembersEditView *************************************************
	 **************************************************************************/
	Views.RegisterMembersEditView = Views.BaseView.extend({
		tagName : "div",

		uploader : undefined,

		initialize : function() {
			var self = this;

			self.template = _.template(tpl_register_members_edit);
			_.bindAll(self, "render", "allowedToEdit", "viewMember", "addMember", "removeMember", "toggleAddMember", "close");
			self.collection.bind('reset', this.render, this);
			self.collection.bind("change", this.render, this);
			self.collection.bind('remove', this.render, this);
			self.collection.bind('add', this.render, this);

			// Initialize Professor, no request is performed until
			// render
			self.professors = new Models.Professors();
			self.professors.url = self.options.register.url() + "/professor";
			self.professors.on("member:add", function(role, type) {
				self.addMember(role, type);
			});
		},

		events : {
			"click a#removeMember" : "removeMember",
			"click a#toggleAddMember" : "toggleAddMember",
			"click a#viewMember" : "viewMember"
		},

		allowedToEdit : function() {
			return true;
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				members : self.collection.toJSON()
			}));

			if (self.allowedToEdit()) {
				// Inner View
				if (self.professorListView) {
					self.professorListView.close();
				}
				self.professorListView = new Views.RegisterMembersEditProfessorListView({
					collection : self.professors
				});
				self.$("div#register-professor-list").hide();
				self.$("div#register-professor-list").html(self.professorListView.el);
				self.$("select").removeAttr("disabled");
				self.$("a.btn").show();

				self.professors.fetch();
			} else {
				self.$("div#committee-professor-list").hide();
				self.$("select").attr("disabled", true);
				self.$("a.btn").hide();
			}
			return self;
		},

		viewMember : function(event, member) {
			var self = this;
			var selectedModel = member ? member : self.collection.get($(event.currentTarget).data('memberId'));
			if (selectedModel) {
				self.collection.trigger("member:selected", selectedModel);
			}
		},

		toggleAddMember : function(event) {
			var self = this;
			self.$("div#register-professor-list").toggle();
			self.$("a#toggleAddMember").toggleClass('active');
		},

		addMember : function(professor) {
			var self = this;
			var registerMember = new Models.RegisterMember({
				"register" : {
					id : self.options.register.get("id")
				},
				"professor" : professor.toJSON()
			});
			// Save
			registerMember.save({}, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
					self.collection.add(registerMember);
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		removeMember : function(event) {
			var self = this;
			var selectedModel = self.collection.get($(event.currentTarget).data('memberId'));
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					selectedModel.destroy({
						wait : true,
						success : function(model, resp) {
							var popup = new Views.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
							popup.show();
						},
						error : function(model, resp, options) {
							var popup = new Views.PopupView({
								type : "error",
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
		},

		close : function(eventName) {
			this.professors.off("role:selected");
			this.professorListView.close();
			this.collection.unbind('reset', this.render, this);
			this.collection.unbind('remove', this.render, this);
			this.collection.unbind('add', this.render, this);
			this.$el.unbind();
			this.$el.remove();

		}
	});

	/***************************************************************************
	 * RegisterMemberEditView (Not Used) ***************************************
	 **************************************************************************/
	Views.RegisterMemberEditView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "addSubject", "removeSubject", "submit", "close");
			this.template = _.template(tpl_register_member_edit);
			this.model.bind('change', this.render, this);
		},

		events : {
			"click a#saveRegisterMember" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "submit",
			"click a#addSubject" : "addSubject",
			"click a#removeSubject" : "removeSubject"
		},

		render : function(eventName) {
			var self = this;
			// 1. Render
			self.$el.html(this.template(self.model.toJSON()));
			_.each(self.model.get("subjects"), function(subject) {
				self.addSubject(undefined, subject.name);
			});
			// Return
			return self;
		},

		addSubject : function(event, subject) {
			var self = this;
			subject = subject || self.$("input[name=suggestSubject]").val();
			self.$("#subjects").append("<div class=\"controls\" id=\"subject\"><div class=\"input-append\"><input type=\"text\" class=\"input-xlarge\" name=\"subject\" value=\"" + subject + "\" disabled /><a id=\"removeSubject\" data-subject=\"" + subject + "\" class=\"btn btn-danger\"><i class=\"icon-minus\"></i></a></div></div>");
			self.$("input[name=suggestSubject]").val("");
		},

		removeSubject : function(event) {
			$(event.currentTarget).parents("div#subject").remove();
		},

		submit : function(event) {
			var self = this;
			// Read Input
			var values = {
				subjects : []
			};
			self.$("form input[name=subject]").each(function(index) {
				values.subjects.push({
					name : $(this).val()
				});
			});
			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
					self.model.trigger("sync", model);
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			return false;
		},

		close : function() {
			this.model.unbind('change', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * RegisterMembersEditProfessorListView ************************************
	 **************************************************************************/

	Views.RegisterMembersEditProfessorListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "addMember", "close");
			this.template = _.template(tpl_register_members_edit_professor_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#addMember" : "addMember"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				professors : (function() {
					var result = [];
					self.collection.each(function(model) {
						var item = model.toJSON();
						item.cid = model.cid;
						result.push(item);
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));

			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		addMember : function(event) {
			var self = this;
			var cid = $(event.currentTarget).data('modelCid')
			var selectedModel = self.collection.getByCid(cid);
			var type = self.$("select[name=type][data-model-cid=" + cid + "]").val();
			self.collection.trigger("member:add", selectedModel, type);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * ProfessorListView *******************************************************
	 **************************************************************************/

	Views.ProfessorListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "showDetails", "select", "close");
			this.template = _.template(tpl_professor_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#select" : "select"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				professors : (function() {
					var result = [];
					self.collection.each(function(model) {
						var item = model.toJSON();
						item.cid = model.cid;
						result.push(item);
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));

			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		showDetails : function(event, professor) {
			var self = this;
			var selectedModel = professor ? professor : self.collection.getByCid($(event.currentTarget).data('modelCid'));
			if (selectedModel) {
				self.collection.trigger("professor:selected", professor);
			}
		},

		select : function(event, professor) {
			var selectedModel = professor ? professor : this.collection.getByCid($(event.currentTarget).data('modelCid'));
			if (selectedModel) {
				this.collection.trigger("role:selected", selectedModel);
			}
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * ProfessorCommitteesView *************************************************
	 **************************************************************************/
	Views.ProfessorCommitteesView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "select", "close");
			self.template = _.template(tpl_professor_committees);
			self.collection.bind('reset', self.render, self);
		},

		events : {
			"click a#select" : "select"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				committees : self.collection.toJSON()
			}));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		select : function(event, positionCommitteeMember) {
			var self = this;
			var selectedModel = positionCommitteeMember ? positionCommitteeMember : self.collection.get($(event.currentTarget).data('committeeMemberId'));
			if (selectedModel) {
				self.collection.trigger("positionCommitteeMember:selected", selectedModel);
			}
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * ProfessorEvaluationsView ************************************************
	 **************************************************************************/
	Views.ProfessorEvaluationsView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "select", "close");
			self.template = _.template(tpl_professor_evaluations);
			self.collection.bind('reset', self.render, self);
		},

		events : {
			"click a#select" : "select"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				evaluations : self.collection.toJSON()
			}));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		select : function(event, positionEvaluator) {
			var self = this;
			var selectedModel = positionEvaluator ? positionEvaluator : self.collection.get($(event.currentTarget).data('evaluatorId'));
			if (selectedModel) {
				self.collection.trigger("positionEvaluator:selected", selectedModel);
			}
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * InstitutionRegulatoryFrameworkListView **********************************
	 **************************************************************************/
	Views.InstitutionRegulatoryFrameworkListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "renderActions", "select", "create", "close");
			self.template = _.template(tpl_institution_regulatory_framework_list);
			self.collection.bind('reset', self.render, self);
			self.collection.bind('add', self.render, self);
			self.collection.bind('remove', self.render, self);
		},

		events : {
			"click a#selectInstitutionRF" : "select",
			"click a#createInstitutionRF" : "create"
		},

		render : function(eventName) {
			var self = this;
			var tpl_data = {
				institutionRFs : (function() {
					var result = [];
					self.collection.each(function(model) {
						if (model.has("id")) {
							var item = model.toJSON();
							item.cid = model.cid;
							result.push(item);
						}
					});
					return result;
				})()
			};
			self.$el.html(self.template(tpl_data));

			// Widgets
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}

			// Add Actions
			self.renderActions();
			return self;
		},

		renderActions : function() {
			var self = this;
			if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
				return;
			}
			if (self.collection.any(function(register) {
				return App.loggedOnUser.isAssociatedWithInstitution(register.get("institution"));
			})) {
				return;
			}
			self.$("#actions").append("<div class=\"btn-group\"><input type=\"hidden\" name=\"institution\" /><a id=\"createInstitutionRF\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_institutionrf') + " </a></div>");
			// Add institutions in selector:
			App.institutions = App.institutions ? App.institutions : new Models.Institutions();
			App.institutions.fetch({
				cache : true,
				success : function(collection, resp) {
					var institution = collection.find(function(institution) {
						return App.loggedOnUser.isAssociatedWithInstitution(institution);
					});
					self.$("#actions input[name=institution]").val(institution.get("id"));
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		select : function(event, institutionRF) {
			var self = this;
			var selectedModel = institutionRF ? institutionRF : self.collection.getByCid($(event.currentTarget).data('institutionrfCid'));
			if (selectedModel) {
				self.collection.trigger("institutionRF:selected", selectedModel);
			}
		},

		create : function(event) {
			var self = this;
			var newIRF = new Models.InstitutionRegulatoryFramework();
			newIRF.save({
				institution : {
					id : self.$("input[name='institution']").val()
				}
			}, {
				wait : true,
				success : function(model, resp) {
					self.collection.add(newIRF);
					self.select(undefined, newIRF);
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		},

		close : function(eventName) {
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
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(this, "render", "submit", "cancel", "remove", "close");
			self.template = _.template(tpl_institution_regulatory_framework_edit);
			self.model.bind('change', self.render, self);
			self.model.bind("destroy", self.close, self);
		},

		events : {
			"click a#cancel" : "cancel",
			"click a#remove" : "remove",
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "submit"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			self.validator = $("form", this.el).validate({
				errorElement : "span",
				errorClass : "help-inline",
				highlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").addClass("error");
				},
				unhighlight : function(element, errorClass, validClass) {
					$(element).parent(".controls").parent(".control-group").removeClass("error");
				},
				rules : {
					"organismosURL" : {
						"required" : true,
						"url" : true
					},
					"eswterikosKanonismosURL" : {
						"required" : true,
						"url" : true
					}
				},
				messages : {
					"organismosURL" : $.i18n.prop('validation_organismosURL'),
					"eswterikosKanonismosURL" : $.i18n.prop('validation_eswterikosKanonismosURL')
				}
			});

			return self;
		},

		submit : function(event) {
			var self = this;
			var values = {};
			// Read Input
			values.organismosURL = self.$('form input[name=organismosURL]').val();
			values.eswterikosKanonismosURL = self.$('form input[name=eswterikosKanonismosURL]').val();
			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("regulatoryframeworks/" + self.model.id, {
						trigger : false
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
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		cancel : function(event) {
			var self = this;
			if (self.validator) {
				self.validator.resetForm();
			}
			self.render();
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							App.router.navigate("regulatoryframeworks", {
								trigger : false
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
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		close : function(eventName) {
			this.model.unbind("change");
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * InstitutionRegulatoryFrameworkView **************************************
	 **************************************************************************/
	Views.InstitutionRegulatoryFrameworkView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "close");
			self.template = _.template(tpl_institution_regulatory_framework);
			self.model.bind('change', self.render, self);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			return self;
		},

		close : function(eventName) {
			this.model.unbind("change");
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionSearchCriteriaView **********************************************
	 **************************************************************************/
	Views.PositionSearchCriteriaView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "addSubject", "removeSubject", "addDepartment", "removeDepartment", "search", "submit", "close");
			self.template = _.template(tpl_position_search_criteria);
			self.model.bind('change', self.render, self);
		},

		events : {
			"click a#addDepartment" : "addDepartment",
			"click a#removeDepartment" : "removeDepartment",
			"click a#addSubject" : "addSubject",
			"click a#removeSubject" : "removeSubject",
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "submit",
			"click a#search" : "search"
		},

		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			// Add Departments to selector:
			App.departments = App.departments ? App.departments : new Models.Departments();
			App.departments.fetch({
				cache : true,
				success : function(collection, resp) {
					self.$("select[name=department]").empty();
					collection.forEach(function(department) {
						var selected = _.any(self.model.get("departments"), function(selectedDepartment) {
							return _.isEqual(selectedDepartment.id, department.get("id"));
						});
						var institution = department.get("institution").name;
						self.$("select[name=suggestDepartment]:not(:has(optgroup[label='" + institution + "']))").append("<optgroup label='" + institution + "'>");
						self.$("select[name=suggestDepartment] optgroup[label='" + institution + "']").append("<option value='" + department.get("id") + "' " + (selected ? "selected " : "") + "><span>" + department.get("department") + "</span></option>");
					});
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			// Add Departments:
			_.each(self.model.get("departments"), function(department) {
				self.addDepartment(undefined, department);
			});

			// Add Subjects:
			_.each(self.model.get("subjects"), function(subject) {
				self.addSubject(undefined, subject.name);
			});

			return self;
		},

		addSubject : function(event, subject) {
			var self = this;
			subject = subject || self.$("input[name=suggestSubject]").val();
			self.$("#subjects").prepend("<div id=\"subject\" class=\"input-append\"><input type=\"text\" class=\"input-xlarge\" name=\"subject\" value=\"" + subject + "\" disabled /><a id=\"removeSubject\" data-subject=\"" + subject + "\" class=\"btn btn-danger\"><i class=\"icon-minus\"></i></a></div>");
			self.$("input[name=suggestSubject]").val("");
		},

		removeSubject : function(event) {
			$(event.currentTarget).parents("div#subject").remove();
		},

		addDepartment : function(event, department) {
			var self = this;
			department = department || App.departments.get(self.$("select[name=suggestDepartment]").val()).toJSON();
			self.$("#departments").append("<div class=\"controls\"><div id=\"department\" class=\"input-append\"><input type=\"hidden\" class=\"input-xlarge\" name=\"department\" value=\"" + department.id + "\" /><span class=\"uneditable-input input-xlarge\" title=\"" + department.institution.name + "\">" + department.department + "</span><a id=\"removeDepartment\" class=\"btn btn-danger\"><i class=\"icon-minus\"></i></a></div>");
			self.$("select[name=suggestDepartment]").val("");
		},

		removeDepartment : function(event) {
			$(event.currentTarget).parents("div#department").remove();
		},

		search : function(event) {
			var self = this;
			var values = {
				departments : _.map(self.$("input[name=department]"), function(department) {
					return {
						id : $(department).val()
					};
				}),
				subjects : _.map(self.$("input[name=subject]"), function(subject) {
					return {
						name : $(subject).val()
					};
				})
			};
			self.model.trigger("criteria:search", values);
		},

		submit : function(event) {
			var self = this;
			var values = {
				departments : _.map(self.$("input[name=department]"), function(department) {
					return {
						id : $(department).val()
					};
				}),
				subjects : _.map(self.$("input[name=subject]"), function(subject) {
					return {
						name : $(subject).val()
					};
				})
			};
			// Read Input
			// Save to model
			self.model.save(values, {
				wait : true,
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		close : function(eventName) {
			this.model.unbind('change', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionSearchResultView ************************************************
	 **************************************************************************/
	Views.PositionSearchResultView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "select", "close");
			self.template = _.template(tpl_position_search_result);
			self.collection.bind('reset', self.render, self);
		},

		events : {
			"click a#selectPosition" : "select"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				positions : self.collection.toJSON()
			}));
			self.$("a#selectPosition").each(function() {
				var position = self.collection.get($(this).data("positionId"));
				if (position.get("phase").status === "ANOIXTI") {
					$(this).show();
				} else {
					$(this).hide();
				}
			});
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		select : function(event, position) {
			var self = this;
			var selectedModel = position ? position : self.collection.get($(event.currentTarget).data('positionId'));
			self.collection.trigger("position:selected", selectedModel);
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * CandidateCandidacyListView **********************************************
	 **************************************************************************/
	Views.CandidateCandidacyListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "select", "close");
			self.template = _.template(tpl_candidate_candidacy_list);
			self.collection.bind('reset', self.render, self);
		},

		events : {
			"click a#selectCandidacy" : "select"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				candidacies : self.collection.toJSON()
			}));
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sSearch" : $.i18n.prop("dataTable_sSearch"),
						"sLengthMenu" : $.i18n.prop("dataTable_sLengthMenu"),
						"sZeroRecords" : $.i18n.prop("dataTable_sZeroRecords"),
						"sInfo" : $.i18n.prop("dataTable_sInfo"),
						"sInfoEmpty" : $.i18n.prop("dataTable_sInfoEmpty"),
						"sInfoFiltered" : $.i18n.prop("dataTable_sInfoFiltered"),
						"oPaginate" : {
							sFirst : $.i18n.prop("dataTable_sFirst"),
							sPrevious : $.i18n.prop("dataTable_sPrevious"),
							sNext : $.i18n.prop("dataTable_sNext"),
							sLast : $.i18n.prop("dataTable_sLast")
						}
					}
				});
			}
			return self;
		},

		select : function(event, candidacy) {
			var self = this;
			var selectedModel = candidacy ? candidacy : self.collection.get($(event.currentTarget).data('candidacyId'));
			self.collection.trigger("candidacy:selected", selectedModel);
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * CandidacyEditView *******************************************************
	 **************************************************************************/
	Views.CandidacyEditView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "isEditable", "submit", "cancel", "addFile", "close");
			this.template = _.template(tpl_candidacy_edit);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {
			"click a#cancel" : "cancel",
			"click a#remove" : "remove",
			"click a#save" : function() {
				$("form", this.el).submit();
			},
			"submit form" : "submit"
		},

		isEditable : function(field) {
			var self = this;
			switch (field) {
			case "evaluator_fullname_0":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
			case "evaluator_fullname_1":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
			case "evaluator_email_0":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
			case "evaluator_email_1":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
			case "ekthesiAutoaksiologisisFile":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI");
			case "sympliromatikaEggrafaFileList":
				return _.isEqual(self.model.get("candidacies").position.phase.status, "ANOIXTI") || _.isEqual(self.model.get("candidacies").position.phase.status, "EPILOGI");
			}
			return false;
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			if (self.model.has("id")) {
				// Snapshot Files
				var sfiles = new Models.Files();
				sfiles.url = self.model.url() + "/snapshot/file";
				sfiles.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
							withMetadata : false,
							editable : false
						});
						self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
							withMetadata : true,
							editable : false
						});
						self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
							withMetadata : true,
							editable : false
						});
					}
				});
				// Candidacy Files
				var files = new Models.Files();
				files.url = self.model.url() + "/file";
				files.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFile(collection, "EKTHESI_AUTOAKSIOLOGISIS", self.$("#ekthesiAutoaksiologisisFile"), {
							withMetadata : true,
							editable : self.isEditable("ekthesiAutoaksiologisisFile")
						});
						self.addFileList(collection, "SYMPLIROMATIKA_EGGRAFA", self.$("#sympliromatikaEggrafaFileList"), {
							withMetadata : true,
							editable : self.isEditable("sympliromatikaEggrafaFileList")
						});

					}
				});
			} else {
				self.$("#mitrooFileList").html($.i18n.prop("PressSave"));
			}
			// Set isEditable to fields
			self.$("select, input, textarea").each(function(index) {
				var field = $(this).attr("name");
				if (self.isEditable(field)) {
					$(this).removeAttr("disabled");
				} else {
					$(this).attr("disabled", true);
				}
			});

			return self;
		},

		submit : function(event) {
			var self = this;
			var values = {};
			// Read Input
			values.proposedEvaluators = [ {
				fullname : self.$('form input[name=evaluator_fullname_0]').val(),
				email : self.$('form input[name=evaluator_email_0]').val()
			}, {
				fullname : self.$('form input[name=evaluator_fullname_1]').val(),
				email : self.$('form input[name=evaluator_email_1]').val()
			} ];
			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("candidateCandidacies/" + self.model.id, {
						trigger : false
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
						message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			event.preventDefault();
			return false;
		},

		cancel : function(event) {
			var self = this;
			self.render();
		},

		remove : function() {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							App.router.navigate("candidateCandidacies", {
								trigger : false
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
								message : $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * CandidacyView ***********************************************************
	 **************************************************************************/
	Views.CandidacyView = Views.BaseView.extend({
		tagName : "div",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "addFile", "close");
			this.template = _.template(tpl_candidacy);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Snapshot Files
			var sfiles = new Models.Files();
			sfiles.url = self.model.url() + "/snapshot/file";
			sfiles.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
						withMetadata : false,
						editable : false
					});
					self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
						withMetadata : true,
						editable : false
					});
					self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
						withMetadata : true,
						editable : false
					});
				}
			});
			// Candidacy Files
			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "EKTHESI_AUTOAKSIOLOGISIS", self.$("#ekthesiAutoaksiologisisFile"), {
						withMetadata : true,
						editable : false
					});
					self.addFileList(collection, "SYMPLIROMATIKA_EGGRAFA", self.$("#sympliromatikaEggrafaFileList"), {
						withMetadata : true,
						editable : false
					});
				}
			});
			return self;
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * CandidacyUpdateConfirmView **********************************************
	 **************************************************************************/
	Views.CandidacyUpdateConfirmView = Views.BaseView.extend({
		tagName : "div",

		className : "modal",

		initialize : function() {
			this.template = _.template(tpl_candidacy_update_confirm);
			_.bindAll(this, "render", "show", "close");
		},

		events : {
			"click a#yes" : function(event) {
				this.$el.modal('hide');
				if (_.isFunction(this.options.answer)) {
					this.options.answer(true);
				}
			},
			"click a#no" : function(event) {
				this.$el.modal('hide');
				if (_.isFunction(this.options.answer)) {
					this.options.answer(false);
				}
			}
		},

		render : function(eventName) {
			$(this.el).html(this.template({
				candidacies : this.collection.toJSON()
			}));
		},
		show : function() {
			this.render();
			this.$el.modal();
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	return Views;
});
