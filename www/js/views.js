define([ "jquery", "underscore", "backbone", "application", "models", "text!tpl/announcement-list.html", "text!tpl/confirm.html", "text!tpl/file-edit.html", "text!tpl/home.html", "text!tpl/login-admin.html", "text!tpl/login-main.html", "text!tpl/popup.html", "text!tpl/position-committee-edit.html", "text!tpl/position-edit.html", "text!tpl/position-list.html", "text!tpl/professor-list.html", "text!tpl/register-edit.html", "text!tpl/register-list.html", "text!tpl/role-edit.html", "text!tpl/role-tabs.html", "text!tpl/role.html", "text!tpl/user-edit.html", "text!tpl/user-list.html", "text!tpl/user-registration-select.html", "text!tpl/user-registration-success.html", "text!tpl/user-registration.html", "text!tpl/user-role-info.html", "text!tpl/user-search.html", "text!tpl/user-verification.html", "text!tpl/user.html", "text!tpl/language.html", "text!tpl/file-multiple-edit.html", "text!tpl/professor-committees.html", "text!tpl/position-committee-edit-professor-list.html", "text!tpl/position.html", "text!tpl/position-committee.html", "text!tpl/register.html", "text!tpl/institution-regulatory-framework.html", "text!tpl/institution-regulatory-framework-edit.html",
	"text!tpl/position-search.html", "text!tpl/candidacy-edit.html", "text!tpl/candidate-candidacy-list.html", "text!tpl/position-candidacy-list.html", "text!tpl/candidacy.html", "text!tpl/candidacy-update-confirm.html" ], function($, _, Backbone, App, Models, tpl_announcement_list, tpl_confirm, tpl_file_edit, tpl_home, tpl_login_admin, tpl_login_main, tpl_popup, tpl_position_committee_edit, tpl_position_edit, tpl_position_list, tpl_professor_list, tpl_register_edit, tpl_register_list, tpl_role_edit, tpl_role_tabs, tpl_role, tpl_user_edit, tpl_user_list, tpl_user_registration_select, tpl_user_registration_success, tpl_user_registration, tpl_user_role_info, tpl_user_search, tpl_user_verification, tpl_user, tpl_language, tpl_file_multiple_edit, tpl_professor_committees, tpl_position_committee_edit_professor_list, tpl_position, tpl_position_committee, tpl_register, tpl_institution_regulatory_framework, tpl_institution_regulatory_framework_edit, tpl_position_search, tpl_candidacy_edit, tpl_candidate_candidacy_list, tpl_position_candidacy_list, tpl_candidacy, tpl_candidacy_update_confirm) {

	/** **************************************************************** */

	var Views = {};

	/***************************************************************************
	 * BaseView ***********************************************************
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
		el : "div#menu",

		tagName : "ul",

		className : "nav",

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
				menuItems.push("professorCommittees");
			}
			if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
				menuItems.push("professorCommittees");
			}
			if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
				menuItems.push("sposition");
				menuItems.push("candidateCandidacies");
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_MANAGER", "ACTIVE")) {
				menuItems.push("iassistants");
				menuItems.push("regulatoryframework");
				menuItems.push("register");
				menuItems.push("position");
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_ASSISTANT", "ACTIVE")) {
				menuItems.push("regulatoryframework");
				menuItems.push("register");
				menuItems.push("position");
			}
			if (self.model.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE")) {
				menuItems.push("massistants");
				menuItems.push("register");
				menuItems.push("position");
			}
			if (self.model.hasRoleWithStatus("MINISTRY_ASSISTANT", "ACTIVE")) {
				menuItems.push("register");
				menuItems.push("position");
			}
			this.$el.append("<ul class=\"nav\">");
			_.each(_.uniq(menuItems), function(menuItem) {
				self.$("ul").append("<li><a href=\"\#" + menuItem + "\">" + $.i18n.prop("menu_" + menuItem) + "</a></li>");
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
		el : "div#menu",

		tagName : "ul",

		className : "nav",

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
		el : "div#user-menu",

		className : "nav",

		initialize : function() {
			_.bindAll(this, "render", "logout", "close");
			this.model.bind('change', this.render);
		},

		events : {
			"click a#logout" : "logout"
		},

		render : function(eventName) {
			this.$el.empty();
			this.$el.append("<a class=\"btn dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\"> <i class=\"icon-user\"></i> " + this.model.get("username") + "<span class=\"caret\"></span></a>");
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
					username : {
						required : $.i18n.prop('validation_username'),
						email : $.i18n.prop('validation_username'),
						minlength : $.i18n.prop('validation_minlength', 2)
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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

		initialize : function() {
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_home);
			this.model.bind('change', this.render);
		},

		events : {},

		render : function(eventName) {
			$(this.el).html(this.template(this.model.toJSON()));
			return this;
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
			} else if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
				self.$("input[name=username]").attr("disabled", true);
				self.$("input[name=firstname]").removeAttr("disabled");
				self.$("input[name=lastname]").removeAttr("disabled");
				self.$("input[name=fathername]").removeAttr("disabled");
				self.$("input[name=firstnamelatin]").removeAttr("disabled");
				self.$("input[name=lastnamelatin]").removeAttr("disabled");
				self.$("input[name=fathernamelatin]").removeAttr("disabled");
			} else {
				self.$("input[name=username]").attr("disabled", true);
				self.$("input[name=firstname]").attr("disabled", true);
				self.$("input[name=lastname]").attr("disabled", true);
				self.$("input[name=fathername]").attr("disabled", true);
				self.$("input[name=firstnamelatin]").attr("disabled", true);
				self.$("input[name=lastnamelatin]").attr("disabled", true);
				self.$("input[name=fathernamelatin]").attr("disabled", true);
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
				case "identification":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
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
				case "identification":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
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
				case "subject":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fekSubject":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				case "fekFile":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
				}
				break;
			case "PROFESSOR_FOREIGN":
				switch (field) {
				case "identification":
					return _.isEqual(self.model.get("status"), "UNAPPROVED");
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
				}
				break;
			case "INSTITUTION_ASSISTAN":
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
						identification : "required"
					},
					messages : {
						identification : $.i18n.prop('validation_identification')
					}
				});
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						identification : "required",
						institution : "required",
						profileURL : {
							required : true,
							url : true
						},
						rank : "required",
						subject : "required",
						fek : "required",
						fekSubject : "required"
					},
					messages : {
						identification : $.i18n.prop('validation_identification'),
						institution : $.i18n.prop('validation_institution'),
						profileURL : $.i18n.prop('validation_profileURL'),
						rank : $.i18n.prop('validation_rank'),
						subject : $.i18n.prop('validation_subject'),
						fek : $.i18n.prop('validation_fek'),
						fekSubject : $.i18n.prop('validation_fekSubject')
					}
				});

				break;
			case "PROFESSOR_FOREIGN":
				App.ranks = App.ranks ? App.ranks : new Models.Ranks();
				App.ranks.fetch({
					cache : true,
					success : function(collection, resp) {
						_.each(collection.filter(function(rank) {
							return _.isEqual(rank.get("category"), "PROFESSOR");
						}), function(rank) {
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						identification : "required",
						institution : "required",
						profileURL : {
							required : true,
							url : true
						},
						rank : "required",
						subject : "required"
					},
					messages : {
						identification : $.i18n.prop('validation_identification'),
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
					self.$("label[for='verificationAuthorityName']").html($.i18n.prop('VerificationAuthorityName') + " " + $.i18n.prop('VerificationAuthority' + self.$("select[name='verificationAuthority']").val()));
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						}
					},
					messages : {
						institution : $.i18n.prop('validation_institution'),
						verificationAuthority : $.i18n.prop('validation_verificationAuthority'),
						verificationAuthorityName : $.i18n.prop('validation_verificationAuthorityName'),
						phone : {
							required : $.i18n.prop('validation_phone'),
							number : $.i18n.prop('validation_number'),
							minlength : $.i18n.prop('validation_minlength', 10)
						}
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
				values.identification = self.$('form input[name=identification]').val();
				break;
			case "PROFESSOR_DOMESTIC":
				values.identification = self.$('form input[name=identification]').val();
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
				values.subject = {
					"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
					"name" : self.$('form textarea[name=subject]').val()
				};
				values.fek = self.$('form input[name=fek]').val();
				values.fekSubject = {
					"id" : self.model.has("fekSubject") ? self.model.get("fekSubject").id : undefined,
					"name" : self.$('form textarea[name=fekSubject]').val()
				};
				break;
			case "PROFESSOR_FOREIGN":
				values.identification = self.$('form input[name=identification]').val();
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
												message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
										message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
			// Cannot change any fields
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
			_.bindAll(this, "render", "deleteFile", "close");
			this.model.bind('change', this.render, this);
		},

		events : {
			"click a#delete" : "deleteFile"
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
				self.$("#uploader").show();
			} else {
				self.$('a#delete').hide();
				self.$("#uploader").hide();
			}
			if (self.options.withMetadata) {
				self.$("input[name=file_name]").show();
				self.$("textarea[name=file_description]").show();
			} else {
				self.$("input[name=file_name]").hide();
				self.$("textarea[name=file_description]").hide();
			}
			self.$('div.progress').hide();

			// Initialize FileUpload widget
			self.$('input[name=file]').fileupload({
				dataType : 'json',
				url : self.model.url() + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
				replaceFileInput : false,
				forceIframeTransport : true,
				multipart : true,
				add : function(e, data) {
					self.$("a#upload").bind("click", function(e) {
						data.formData = {
							"type" : self.$("input[name=file_type]").val(),
							"name" : self.$("input[name=file_name]").val(),
							"description" : self.$("textarea[name=file_description]").val()
						};
						if (_.isFunction(self.options.beforeUpload)) {
							self.options.beforeUpload(data, function(data) {
								self.$('div.progress').show();
								self.$("a#upload").unbind("click");
								data.submit();
							});
						} else {
							self.$('div.progress').show();
							self.$("a#upload").unbind("click");
							data.submit();
						}
					});
				},
				progressall : function(e, data) {
					var progress = parseInt(data.loaded / data.total * 100, 10);
					self.$('div.progress .bar').css('width', progress + '%');
				},
				done : function(e, data) {
					self.$('div.progress').fadeOut('slow', function() {
						self.$('div.progress .bar').css('width', '0%');
						self.model.set(data.result);
					});
				},
				fail : function(e, data) {
					self.$('div.progress').fadeOut('slow', function() {
						self.$('div.progress .bar').css('width', '0%');
					});
					var resp = data.jqXHR;
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
					self.$('#progress.bar').hide('slow', function() {
						self.$('#progress .bar').css('width', '0%');
					});

				}
			});
			return self;
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
			_.bindAll(this, "render", "deleteFile", "close");
			this.collection.bind('reset', this.render, this);
			this.collection.bind('remove', this.render, this);
			this.collection.bind('add', this.render, this);
		},

		events : {
			"click a#delete" : "deleteFile"
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
				self.$('#uploader').show();
			} else {
				self.$('a#delete').hide();
				self.$('#uploader').hide();
			}

			if (self.options.withMetadata) {
				self.$("input[name=file_name]").show();
				self.$("textarea[name=file_description]").show();
			} else {
				self.$("input[name=file_name]").hide();
				self.$("textarea[name=file_description]").hide();
			}
			self.$('div.progress').hide();
			// Initialize FileUpload widget
			self.$('input[name=file]').fileupload({
				dataType : 'json',
				url : self.collection.url + "?X-Auth-Token=" + encodeURIComponent(App.authToken),
				replaceFileInput : false,
				forceIframeTransport : true,
				add : function(e, data) {
					self.$("a#upload").bind("click", function(e) {
						data.formData = {
							"type" : self.$("input[name=file_type]").val(),
							"name" : self.$("input[name=file_name]").val(),
							"description" : self.$("textarea[name=file_description]").val()
						};
						if (_.isFunction(self.options.beforeUpload)) {
							self.options.beforeUpload(data, function() {
								self.$('div.progress').show();
								self.$("a#upload").unbind("click");
								data.submit();
							});
						} else {
							self.$('div.progress').show();
							self.$("a#upload").unbind("click");
							data.submit();
						}
					});
				},
				progressall : function(e, data) {
					var progress = parseInt(data.loaded / data.total * 100, 10);
					self.$('div.progress .bar').css('width', progress + '%');
				},
				done : function(e, data) {
					self.$('div.progress').fadeOut('slow', function() {
						self.$('div.progress .bar').css('width', '0%');
						var newFile = new Models.File(data.result);
						newFile.urlRoot = self.collection.url;
						self.collection.add(newFile);
					});
				},
				fail : function(e, data) {
					var resp = data.jqXHR;
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
					self.$('#progress.bar').hide('slow', function() {
						self.$('#progress .bar').css('width', '0%');
					});

				}
			});

			return self;
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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

		className : "span12 well",

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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "APOFASI_SYSTASIS_EPITROPIS", self.$("#apofasiSystasisEpitropisFileList"), {
						withMetadata : true,
						editable : false
					});
					self.addFile(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile"), {
						withMetadata : true,
						editable : false
					});
					self.addFile(collection, "TEKMIRIOSI_EPITROPIS_GIA_AKSIOLOGITES", self.$("#tekmiriosiEpitropisGiaAksiologitesFile"), {
						withMetadata : true,
						editable : false
					});
					self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
						withMetadata : true,
						editable : false
					});
					self.addFileList(collection, "AKSIOLOGISI_PROTOU_AKSIOLOGITI", self.$("#aksiologisiProtouAksiologitiFileList"), {
						withMetadata : true,
						editable : false
					});
					self.addFileList(collection, "AKSIOLOGISI_DEUTEROU_AKSIOLOGITI", self.$("#aksiologisiDeuterouAksiologitiFileList"), {
						withMetadata : true,
						editable : false
					});
					self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
						withMetadata : true,
						editable : false
					});
					self.addFileList(collection, "EISIGISI_DEP_YPOPSIFIOU", self.$("#eisigisiDEPYpopsifiouFileList"), {
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
					self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
						withMetadata : true,
						editable : false
					});
					self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
						withMetadata : true,
						editable : false
					});
				}
			});
			self.addCommitteeView(self.$("#positionCommittee"));
			self.addCandidacyListView(self.$("#positionCandidacyList"));
			// End of associations
			return self;
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
			var positionCandidacies = new Models.PositionCandidacies({}, {
				position : self.model.get("id")
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

		id : "positionview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "addCandidacyListView", "addCommitteeView", "isEditable", "submit", "cancel", "addFile", "addFileList", "close");
			this.template = _.template(tpl_position_edit);
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
			if (_.isEqual(self.model.get("status"), "OLOKLIROMENI") || _.isEqual(self.model.get("status"), "STELEXOMENI")) {
				return false;
			}
			switch (field) {
			// Fields
			case "name":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "department":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "description":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "subject":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "fek":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "fekSentDate":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "openingDate":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "closingDate":
				return self.model.isNew() || _.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI");
			case "committeeMeetingDate":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "nominationCommitteeConvergenceDate":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "nominationToETDate":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "nominationFEK":
				return _.isEqual(self.model.get("status"), "KLEISTI");
				// Files
			case "apofasiSystasisEpitropisFileList":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "prosklisiKosmitoraFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "eisigisiDEPYpopsifiouFileList":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "praktikoEpilogisFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "diavivastikoPraktikouFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "praksiDiorismouFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "dioikitikoEggrafoFileList":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "apofasiAnapompisFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "praktikoSynedriasisEpitropisGiaAksiologitesFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "tekmiriosiEpitropisGiaAksiologitesFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "aitimaEpitropisProsAksiologitesFile":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "aksiologisiProtouAksiologitiFileList":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "aksiologisiDeuterouAksiologitiFileList":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			case "positionCommittee":
				return _.isEqual(self.model.get("status"), "KLEISTI");
			}
			return false;
		},

		render : function(eventName) {
			var self = this;
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			self.innerViews = [];
			self.$el.html(self.template(self.model.toJSON()));

			// Departments
			self.$("select[name='department']").change(function(event) {
				self.$("select[name='department']").next(".help-block").html(self.$("select[name='department'] option:selected").text());
			});
			App.departments = App.departments ? App.departments : new Models.Departments();
			App.departments.fetch({
				cache : true,
				success : function(collection, resp) {
					_.each(collection.filter(function(department) {
						return App.loggedOnUser.isAssociatedWithDepartment(department);
					}), function(department) {
						if (_.isObject(self.model.get("department")) && _.isEqual(department.id, self.model.get("department").id)) {
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "' selected>" + department.get("institution").name + ": " + department.get("department") + "</option>");
						} else {
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "'>" + department.get("institution").name + ": " + department.get("department") + "</option>");
						}
					});
					self.$("select[name='department']").change();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});

			// Dependencies (Files, Committee, Candidacies):
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
						self.addFile(collection, "TEKMIRIOSI_EPITROPIS_GIA_AKSIOLOGITES", self.$("#tekmiriosiEpitropisGiaAksiologitesFile"), {
							withMetadata : true,
							editable : self.isEditable("tekmiriosiEpitropisGiaAksiologitesFile")
						});
						self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
							withMetadata : true,
							editable : self.isEditable("aitimaEpitropisProsAksiologitesFile")
						});
						self.addFileList(collection, "AKSIOLOGISI_PROTOU_AKSIOLOGITI", self.$("#aksiologisiProtouAksiologitiFileList"), {
							withMetadata : true,
							editable : self.isEditable("aksiologisiProtouAksiologitiFileList")
						});
						self.addFileList(collection, "AKSIOLOGISI_DEUTEROU_AKSIOLOGITI", self.$("#aksiologisiDeuterouAksiologitiFileList"), {
							withMetadata : true,
							editable : self.isEditable("aksiologisiDeuterouAksiologitiFileList")
						});
						self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
							withMetadata : true,
							editable : self.isEditable("prosklisiKosmitoraFile")
						});
						self.addFileList(collection, "EISIGISI_DEP_YPOPSIFIOU", self.$("#eisigisiDEPYpopsifiouFileList"), {
							withMetadata : true,
							editable : self.isEditable("eisigisiDEPYpopsifiouFileList")
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
						self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
							withMetadata : true,
							editable : self.isEditable("dioikitikoEggrafoFileList")
						});
						self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
							withMetadata : true,
							editable : self.isEditable("apofasiAnapompisFile")
						});
					}
				});
				self.addCommitteeView(self.$("#positionCommittee"));
				self.addCandidacyListView(self.$("#positionCandidacyList"));
			} else {
				self.$("#apofasiSystasisEpitropisFileList").html($.i18n.prop("PressSave"));
				self.$("#prosklisiKosmitoraFile").html($.i18n.prop("PressSave"));
				self.$("#eisigisiDEPYpopsifiouFileList").html($.i18n.prop("PressSave"));
				self.$("#praktikoEpilogisFile").html($.i18n.prop("PressSave"));
				self.$("#diavivastikoPraktikouFile").html($.i18n.prop("PressSave"));
				self.$("#praksiDiorismouFile").html($.i18n.prop("PressSave"));
				self.$("#dioikitikoEggrafoFileList").html($.i18n.prop("PressSave"));
				self.$("#apofasiAnapompisFile").html($.i18n.prop("PressSave"));
				self.$("#organismosFile").html($.i18n.prop("PressSave"));
				self.$("#eswterikosKanonismosFile").html($.i18n.prop("PressSave"));
				self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile").html($.i18n.prop("PressSave"));
				self.$("#tekmiriosiEpitropisGiaAksiologitesFile").html($.i18n.prop("PressSave"));
				self.$("#aitimaEpitropisProsAksiologitesFile").html($.i18n.prop("PressSave"));
				self.$("#aksiologisiProtouAksiologitiFileList").html($.i18n.prop("PressSave"));
				self.$("#aksiologisiDeuterouAksiologitiFileList").html($.i18n.prop("PressSave"));

				self.$("#positionCommittee").html($.i18n.prop("PressSave"));
				self.$("#positionCandidacyList").html("-");
			}
			// End of files

			// Set isEditable to fields
			self.$("select, input, textarea").each(function(index) {
				var field = $(this).attr("name");
				if (self.isEditable(field)) {
					$(this).removeAttr("disabled");
				} else {
					$(this).attr("disabled", true);
				}
			});
			if (_.isEqual(self.model.get("status"), "OLOKLIROMENI")) {
				self.$("a#save,a#remove").hide();
			}
			if (_.isEqual(self.model.get("status"), "ENTAGMENI") || _.isEqual(self.model.get("status"), "ANOIXTI")) {
				self.$("#positionTabs a[data-target=#committee]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=#evaluations]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=#proposals]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=#nomination]").parent("li").addClass("disabled");
				self.$("#positionTabs a[data-target=#rest]").parent("li").addClass("disabled");
			}
			// Widgets
			self.$("#positionTabs a").click(function(e) {
				e.preventDefault();
				if (!$(this).parent("li").hasClass("disabled")) {
					$(this).tab('show');
				}
			});
			self.$("input[data-input-type=date]").datepicker();
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
					fek : "required",
					fekSentDate : "required",
					openingDate : "required",
					closingDate : "required"
				},
				messages : {
					name : $.i18n.prop('validation_positionName'),
					description : $.i18n.prop('validation_description'),
					department : $.i18n.prop('validation_department'),
					subject : $.i18n.prop('validation_subject'),
					status : $.i18n.prop('validation_positionStatus'),
					fek : $.i18n.prop('validation_fek'),
					fekSentDate : $.i18n.prop('validation_fekSentDate'),
					openingDate : $.i18n.prop('validation_openingDate'),
					closingDate : $.i18n.prop('validation_closingDate')
				}
			});
			return self;
		},

		submit : function(event) {
			var self = this;
			var values = {};
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
			values.openingDate = self.$('form input[name=openingDate]').val();
			values.closingDate = self.$('form input[name=closingDate]').val();
			values.committeeMeetingDate = self.$('form input[name=committeeMeetingDate]').val();
			values.nominationCommitteeConvergenceDate = self.$('form input[name=nominationCommitteeConvergenceDate]').val();
			values.nominationToETDate = self.$('form input[name=nominationToETDate]').val();
			values.nominationFEK = self.$('form input[name=nominationFEK]').val();

			// Save to model
			self.model.save(values, {
				wait : true,
				success : function(model, resp) {
					App.router.navigate("position/" + self.model.id, {
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
							App.router.navigate("position", {
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
			return false;
		},

		addCommitteeView : function($el) {
			var self = this;
			var committee = new Models.PositionCommittee({}, {
				position : self.model.get("id")
			});
			var committeeView = new Views.PositionCommitteeEditView({
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
				candidacyView = new Views.CandidacyView({
					model : candidacy
				});
				self.$("div[data-candidacy-id=" + candidacy.get("id") + "]").html(candidacyView.el);
				candidacy.fetch({
					success : function() {
						candidacy.trigger("change");
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
	 * PositionCommitteeView ***************************************************
	 **************************************************************************/
	Views.PositionCommitteeView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_committee);
			_.bindAll(self, "render", "viewMember", "close");
			self.collection.bind('reset', this.render, this);
		},

		events : {
			"click a#viewMember" : "viewMember"
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				committee : self.collection.toJSON()
			}));
			return self;
		},

		viewMember : function(event, positionCommitteeMember) {
			var self = this;
			var selectedModel = positionCommitteeMember ? positionCommitteeMember : this.collection.get($(event.currentTarget).data('committeeMemberId'));
			if (selectedModel) {
				var user;
				var userView = undefined;
				var roles;
				var roleView = undefined;

				// Fill Details View:
				user = new Models.User({
					"id" : selectedModel.get("professor").user.id
				});
				user.fetch({
					cache : false,
					success : function(model, resp) {
						userView = new Views.UserView({
							model : user
						});
						self.$("div#commiteeMemberDetails div.modal-body").append(userView.render().el);
					}
				});

				roles = new Models.Roles();
				roles.user = selectedModel.get("professor").user.id;
				roles.fetch({
					cache : false,
					success : function(collection, resp) {
						roleView = new Views.RoleView({
							model : collection.at(0)
						});
						self.$("div#commiteeMemberDetails div.modal-body").append(roleView.render().el);
					}
				});

				self.$("div#commiteeMemberDetails").on("hidden", function() {
					if (userView) {
						userView.close();
					}
					if (roleView) {
						roleView.close();
					}
					self.$("div#commiteeMemberDetails div.modal-body").empty();
				});

				self.$("div#commiteeMemberDetails").modal('show');
			}
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
			_.bindAll(self, "render", "allowedToEdit", "viewMember", "addMember", "updateMember", "removeMember", "toggleAddMember", "close");
			self.collection.bind('reset', this.render, this);
			self.collection.bind('remove', this.render, this);
			self.collection.bind('add', this.render, this);

			// Initialize Professor, no request is performed until
			// render
			self.professors = new Models.Professors();
			self.professors.url = self.options.position.url() + "/professor";
			self.professors.on("member:add", function(role, type) {
				self.addMember(role, type);
			});
		},

		events : {
			"click a#removeMember" : "removeMember",
			"click a#toggleAddMember" : "toggleAddMember",
			"click a#viewMember" : "viewMember",
			"change select[name=type]" : "updateMember"
		},

		allowedToEdit : function() {
			var self = this;
			return self.options.position.get("status") === "KLEISTI";
		},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				committee : self.collection.toJSON()
			}));

			if (self.allowedToEdit()) {
				// Inner View
				if (self.professorListView) {
					self.professorListView.close();
				}
				self.professorListView = new Views.PositionCommitteeEditProfessorListView({
					collection : self.professors
				});
				self.$("div#committee-professor-list").hide();
				self.$("div#committee-professor-list").html(self.professorListView.el);
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

		viewMember : function(event, positionCommitteeMember) {
			var self = this;
			var selectedModel = positionCommitteeMember ? positionCommitteeMember : this.collection.get($(event.currentTarget).data('committeeMemberId'));
			if (selectedModel) {
				var user;
				var userView = undefined;
				var roles;
				var roleView = undefined;

				// Fill Details View:
				user = new Models.User({
					"id" : selectedModel.get("professor").user.id
				});
				user.fetch({
					cache : false,
					success : function(model, resp) {
						userView = new Views.UserView({
							model : user
						});
						self.$("div#commiteeMemberDetails div.modal-body").append(userView.render().el);
					}
				});

				roles = new Models.Roles();
				roles.user = selectedModel.get("professor").user.id;
				roles.fetch({
					cache : false,
					success : function(collection, resp) {
						roleView = new Views.RoleView({
							model : collection.at(0)
						});
						self.$("div#commiteeMemberDetails div.modal-body").append(roleView.render().el);
					}
				});

				self.$("div#commiteeMemberDetails").on("hidden", function() {
					if (userView) {
						userView.close();
					}
					if (roleView) {
						roleView.close();
					}
					self.$("div#commiteeMemberDetails div.modal-body").empty();
				});

				self.$("div#commiteeMemberDetails").modal('show');
			}
		},

		toggleAddMember : function(event) {
			var self = this;
			self.$("div#committee-professor-list").toggle();
			self.$("a#toggleAddMember").toggleClass('active');
		},

		addMember : function(professor, type) {
			var self = this;
			var positionCommitteeMember = new Models.PositionCommitteeMember();
			positionCommitteeMember.save({
				"position" : {
					id : self.options.position.get("id")
				},
				"professor" : professor.toJSON(),
				"type" : type
			}, {
				wait : true,
				success : function(model, resp) {
					self.collection.add(model);
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
		},

		updateMember : function(event, member) {
			var self = this;
			var selectedModel = member ? member : self.collection.get($(event.currentTarget).data('modelId'));
			if (selectedModel) {
				var confirm = new Views.ConfirmView({
					title : $.i18n.prop('Confirm'),
					message : $.i18n.prop('AreYouSure'),
					yes : function() {
						selectedModel.save({
							"type" : $(event.currentTarget).val()
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
									message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
								});
								popup.show();
							}
						});
					}
				});
			}
		},

		removeMember : function(event) {
			var self = this;
			var selectedModel = self.collection.get($(event.currentTarget).data('committeeMemberId'));
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
	 * PositionCommitteeEditProfessorListView **********************************
	 **************************************************************************/

	Views.PositionCommitteeEditProfessorListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			_.bindAll(this, "render", "showDetails", "addMember", "close");
			this.template = _.template(tpl_position_committee_edit_professor_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
		},

		events : {
			"click a#view" : "showDetails",
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

		showDetails : function(event, professor) {
			var self = this;
			var selectedModel = professor ? professor : this.collection.getByCid($(event.currentTarget).data('modelCid'));
			if (selectedModel) {
				var user;
				var userView = undefined;
				var roles;
				var roleView = undefined;

				// Fill Details View:
				user = new Models.User({
					"id" : selectedModel.get("user").id
				});
				user.fetch({
					cache : false,
					success : function(model, resp) {
						userView = new Views.UserView({
							model : user
						});
						self.$("div#professorDetails div.modal-body").prepend(userView.render().el);
					}
				});

				roles = new Models.Roles();
				roles.user = selectedModel.get("user").id;

				roles.fetch({
					cache : false,
					success : function(collection, resp) {
						roleView = new Views.RoleView({
							model : collection.at(0)
						});
						self.$("div#professorDetails div.modal-body").append(roleView.render().el);
					}
				});

				self.$("div#professorDetails").on("hidden", function() {
					if (userView) {
						userView.close();
					}
					if (roleView) {
						roleView.close();
					}
					self.$("div#professorDetails div.modal-body").empty();
				});

				self.$("div#professorDetails").modal('show');
			}
		},

		addMember : function(event) {
			var self = this;
			var selectedModel = self.collection.getByCid($(event.currentTarget).data('modelCid'));
			var type = self.$("select[name=type]").val();
			self.collection.trigger("member:add", selectedModel, type);
		},

		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});

	/***************************************************************************
	 * PositionCandidacyListView ***********************************************
	 **************************************************************************/
	Views.PositionCandidacyListView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			self.template = _.template(tpl_position_candidacy_list);
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
			if (!App.loggedOnUser.hasRole("INSTITUTION_MANAGER") && !App.loggedOnUser.hasRole("INSTITUTION_ASSISTANT")) {
				return;
			}
			self.$("#actions").append("<div class=\"btn-group\"><select class=\"input-xlarge\" name=\"institution\"></select><a id=\"createRegister\" class=\"btn\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_register') + " </a></div>");
			// Add institutions in selector:
			App.institutions = App.institutions ? App.institutions : new Models.Institutions();
			App.institutions.fetch({
				cache : true,
				success : function(collection, resp) {
					_.each(collection.filter(function(institution) {
						return App.loggedOnUser.isAssociatedWithInstitution(institution);
					}), function(institution) {
						self.$("select[name='institution']").append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
					});
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
					id : self.$("select[name='institution']").val()
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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
			_.bindAll(this, "render", "close");
			this.template = _.template(tpl_register);
			this.model.bind('change', this.render, this);
			this.model.bind("destroy", this.close, this);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFileList(collection, "MITROO", self.$("#mitrooFileList"), {
						withMetadata : true,
						editable : false
					});
				}
			});
			return self;
		},

		close : function() {
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
		tagName : "div",

		id : "registerview",

		validator : undefined,

		initialize : function() {
			_.bindAll(this, "render", "submit", "cancel", "addFile", "close");
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

			// Add institutions in selector:
			self.$("select[name='institution']").change(function(event) {
				self.$("select[name='institution']").next(".help-block").html(self.$("select[name='institution'] option:selected").text());
			});
			App.institutions = App.institutions ? App.institutions : new Models.Institutions();
			App.institutions.fetch({
				cache : true,
				success : function(collection, resp) {
					_.each(collection.filter(function(institution) {
						return App.loggedOnUser.isAssociatedWithInstitution(institution);
					}), function(institution) {
						if (_.isObject(self.model.get("institution")) && _.isEqual(institution.id, self.model.get("institution").id)) {
							self.$("select[name='institution']").append("<option value='" + institution.get("id") + "' selected>" + institution.get("name") + "</option>");
						} else {
							self.$("select[name='institution']").append("<option value='" + institution.get("id") + "'>" + institution.get("name") + "</option>");
						}
					});
					self.$("select[name='institution']").change();
				},
				error : function(model, resp, options) {
					var popup = new Views.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
						self.addFileList(collection, "MITROO", self.$("#mitrooFileList"), {
							withMetadata : true,
							editable : true
						});
					}
				});
			} else {
				self.$("#mitrooFileList").html($.i18n.prop("PressSave"));
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

		submit : function(event) {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					var values = {};
					// Read Input
					values.title = self.$('form input[name=title]').val();
					values.institution = {
						"id" : self.$('form select[name=institution]').val()
					};
					// Save to model
					self.model.save(values, {
						wait : true,
						success : function(model, resp) {
							App.router.navigate("register/" + self.model.id, {
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
							});
							popup.show();
						}
					});
				}
			});
			confirm.show();
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
							App.router.navigate("register", {
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
			"click a#view" : "showDetails",
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
			var selectedModel = professor ? professor : this.collection.getByCid($(event.currentTarget).data('modelCid'));
			if (selectedModel) {
				var user;
				var userView = undefined;
				var roles;
				var roleView = undefined;

				// Fill Details View:
				user = new Models.User({
					"id" : selectedModel.get("user").id
				});
				user.fetch({
					cache : false,
					success : function(model, resp) {
						userView = new Views.UserView({
							model : user
						});
						self.$("div#professorDetails div.modal-body").prepend(userView.render().el);
					}
				});

				roles = new Models.Roles();
				roles.user = selectedModel.get("user").id;

				roles.fetch({
					cache : false,
					success : function(collection, resp) {
						roleView = new Views.RoleView({
							model : collection.at(0)
						});
						self.$("div#professorDetails div.modal-body").append(roleView.render().el);
					}
				});

				self.$("div#professorDetails").on("hidden", function() {
					if (userView) {
						userView.close();
					}
					if (roleView) {
						roleView.close();
					}
					self.$("div#professorDetails div.modal-body").empty();
				});

				self.$("div#professorDetails").modal('show');
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
			// TODO: Send to position/id
		},

		close : function(eventName) {
			this.collection.unbind('reset', this.render, this);
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
			_.bindAll(self, "render", "addFile", "close");
			self.template = _.template(tpl_institution_regulatory_framework_edit);
			self.model.bind('change', self.render, self);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Files:
			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "ORGANISMOS", self.$("#organismosFile"), {
						withMetadata : false,
						editable : true
					});
					self.addFile(collection, "ESWTERIKOS_KANONISMOS", self.$("#eswterikosKanonismosFile"), {
						withMetadata : false,
						editable : true
					});
				}
			});
			return self;
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
			_.bindAll(self, "render", "addFile", "close");
			self.template = _.template(tpl_institution_regulatory_framework);
			self.model.bind('change', self.render, self);
		},

		events : {},

		render : function(eventName) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));

			// Add Files:
			var files = new Models.Files();
			files.url = self.model.url() + "/file";
			files.fetch({
				cache : false,
				success : function(collection, response) {
					self.addFile(collection, "ORGANISMOS", self.$("#organismosFile"), {
						withMetadata : false,
						editable : false
					});
					self.addFile(collection, "ESWTERIKOS_KANONISMOS", self.$("#eswterikosKanonismosFile"), {
						withMetadata : false,
						editable : false
					});
				}
			});
			return self;
		},

		close : function(eventName) {
			this.model.unbind("change");
			this.$el.unbind();
			this.$el.remove();
		}
	});

	/***************************************************************************
	 * PositionSearchView ******************************************************
	 **************************************************************************/
	Views.PositionSearchView = Views.BaseView.extend({
		tagName : "div",

		initialize : function() {
			var self = this;
			_.bindAll(self, "render", "select", "close");
			self.template = _.template(tpl_position_search);
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

		validator : undefined,

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
				return _.isEqual(self.model.get("position").status, "ANOIXTI");
			case "evaluator_fullname_1":
				return _.isEqual(self.model.get("position").status, "ANOIXTI");
			case "evaluator_email_0":
				return _.isEqual(self.model.get("position").status, "ANOIXTI");
			case "evaluator_email_1":
				return _.isEqual(self.model.get("position").status, "ANOIXTI");
			case "ekthesiAutoaksiologisisFile":
				return _.isEqual(self.model.get("position").status, "ANOIXTI");
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
					}
				});
			} else {
				self.$("#mitrooFileList").html($.i18n.prop("PressSave"));
			}
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
					"evaluator_fullname_0" : {
						required : function(element) {
							return self.$("input[name=evaluator_email_0]").val() !== "";
						}
					},
					"evaluator_email_0" : {
						required : function(element) {
							return self.$("input[name=evaluator_fullname_0]").val() !== "";
						},
						email : true
					},
					"evaluator_fullname_1" : {
						required : function(element) {
							return self.$("input[name=evaluator_email_1]").val() !== "";
						}
					},
					"evaluator_email_1" : {
						required : function(element) {
							return self.$("input[name=evaluator_fullname_1]").val() !== "";
						},
						email : true
					}
				},
				messages : {
					"evaluator_fullname_0" : $.i18n.prop("validation_evaluator_fullname"),
					"evaluator_email_0" : $.i18n.prop("validation_evaluator_email"),
					"evaluator_fullname_1" : $.i18n.prop("validation_evaluator_fullname"),
					"evaluator_email_1" : $.i18n.prop("validation_evaluator_email")
				}
			});

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
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
								message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
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
