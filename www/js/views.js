define([ "jquery", "underscore", "backbone", "application", "models", "text!tpl/announcement-list.html", "text!tpl/confirm.html", "text!tpl/file-edit.html", "text!tpl/home.html", "text!tpl/login-admin.html", "text!tpl/login-main.html", "text!tpl/popup.html", "text!tpl/position-committee-edit.html", "text!tpl/position-edit.html", "text!tpl/position-list.html", "text!tpl/professor-list.html", "text!tpl/register-edit.html", "text!tpl/register-list.html", "text!tpl/role-edit.html", "text!tpl/role-tabs.html", "text!tpl/role.html", "text!tpl/user-edit.html", "text!tpl/user-list.html", "text!tpl/user-registration-select.html", "text!tpl/user-registration-success.html", "text!tpl/user-registration.html", "text!tpl/user-role-info.html", "text!tpl/user-search.html", "text!tpl/user-verification.html", "text!tpl/user.html", "text!tpl/language.html", "text!tpl/file-multiple-edit.html" ], function($, _, Backbone, App, Models, tpl_announcement_list, tpl_confirm, tpl_file_edit, tpl_home,
		tpl_login_admin, tpl_login_main, tpl_popup, tpl_position_committee_edit, tpl_position_edit, tpl_position_list, tpl_professor_list, tpl_register_edit, tpl_register_list, tpl_role_edit, tpl_role_tabs, tpl_role, tpl_user_edit, tpl_user_list, tpl_user_registration_select, tpl_user_registration_success, tpl_user_registration, tpl_user_role_info, tpl_user_search, tpl_user_verification, tpl_user, tpl_language, tpl_file_multiple_edit) {
	
	var Views = {};
	
	/***************************************************************************
	 * BaseView ****************************************************************
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
	 * MenuView ****************************************************************
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
				menuItems.push("commities");
			}
			if (self.model.hasRoleWithStatus("PROFESSOR_FOREIGN", "ACTIVE")) {
				menuItems.push("commities");
			}
			if (self.model.hasRoleWithStatus("CANDIDATE", "ACTIVE")) {
				menuItems.push("candidacies");
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_MANAGER", "ACTIVE")) {
				menuItems.push("assistants");
				menuItems.push("register");
				menuItems.push("position");
			}
			if (self.model.hasRoleWithStatus("INSTITUTION_ASSISTANT", "ACTIVE")) {
				menuItems.push("register");
				menuItems.push("position");
			}
			if (self.model.hasRoleWithStatus("MINISTRY_MANAGER", "ACTIVE")) {
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
						required : $.i18n.prop('validation_password'),
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
					// Notify AppRouter to start Application (fill Header and
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
						required : $.i18n.prop('validation_password'),
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
					// Notify AppRouter to start Application (fill Header and
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
		
		className : "alert fade in",
		
		initialize : function() {
			this.template = _.template(tpl_popup);
			_.bindAll(this, "render", "show", "close");
		},
		
		events : {},
		
		render : function(eventName) {
			$(this.el).html(this.template({
				message : this.options.message
			}));
			switch (this.options.type) {
			case 'info':
				this.$el.addClass("alert-info");
				break;
			case 'success':
				this.$el.addClass("alert-success");
				break;
			case 'warning':
				this.$el.addClass("alert-danger");
				break;
			case 'error':
				this.$el.addClass("alert-error");
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
			
			// Especially for PROFESSOR_DOMESTIC there is a demand to select
			// institution first in case their institution supports Shibboleth
			// Login
			if (role.discriminator === "PROFESSOR_DOMESTIC") {
				// Add institutions in selector:
				self.$("select[name='institution']").change(function(event) {
					self.$("select[name='institution']").next(".help-block").html(jQuery("select[name='institution'] option:selected").text());
				});
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
						minlength : 10,
						maxlength : 12
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
						required : $.i18n.prop('validation_password'),
						pwd : $.i18n.prop('validation_password'),
						minlength : $.i18n.prop('validation_minlength', 5)
					},
					confirm_password : {
						required : $.i18n.prop('validation_password'),
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
						minlength : $.i18n.prop('validation_minlength', 10),
						maxlength : $.i18n.prop('validation_maxlength', 12)
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
						minlength : 10,
						maxlength : 12
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
						required : $.i18n.prop('validation_password'),
						pwd : $.i18n.prop('validation_password'),
						minlength : $.i18n.prop('validation_minlength', 5)
					},
					confirm_password : {
						required : $.i18n.prop('validation_password'),
						minlength : $.i18n.prop('validation_minlength', 5),
						equalTo : $.i18n.prop('validation_confirmpassword')
					},
					mobile : {
						required : $.i18n.prop('validation_phone'),
						number : $.i18n.prop('validation_number'),
						minlength : $.i18n.prop('validation_minlength', 10),
						maxlength : $.i18n.prop('validation_maxlength', 12)
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
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					
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
				}
			});
			confirm.show();
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
			self.$("input").removeAttr("disable");
			self.$("a#status").removeClass("disabled");
			self.$("a#save").show();
			self.$("a#remove").show();
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
						"sLengthMenu" : "_MENU_ records per page"
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
			_.bindAll(this, "render", "submit", "cancel", "addFile", "addFileList", "close");
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
		
		render : function(eventName) {
			var self = this;
			// Close inner views (fileviews)
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			self.innerViews = [];
			
			// Re-render
			self.$el.html(this.template(this.model.toJSON()));
			
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
								editable : _.isEqual(self.model.get("status"), "UNAPPROVED")
							});
							self.addFile(collection, "BEBAIWSH_STRATIOTIKIS_THITIAS", self.$("#bebaiwsiStratiwtikisThitiasFile"), {
								withMetadata : false,
								editable : _.isEqual(self.model.get("status"), "UNAPPROVED")
							});
							self.addFile(collection, "FORMA_SYMMETOXIS", self.$("#formaSymmetoxisFile"), {
								withMetadata : false,
								editable : _.isEqual(self.model.get("status"), "UNAPPROVED")
							});
							self.addFile(collection, "BIOGRAFIKO", self.$("#biografikoFile"), {
								withMetadata : false,
								editable : true
							});
							self.addFileList(collection, "PTYXIO", self.$("#ptyxioFileList"), {
								withMetadata : true,
								editable : true
							});
							self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
								withMetadata : true,
								editable : true
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
				// Bind change on institution selector to update department
				// selector
				self.$("select[name='department']").change(function(event) {
					self.$("select[name='department']").next(".help-block").html(jQuery("select[name='department'] option:selected").text());
				});
				self.$("select[name='institution']").change(function() {
					self.$("select[name='institution']").next(".help-block").html(jQuery("select[name='institution'] option:selected").text());
					
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
								editable : true
							});
							self.addFile(collection, "FEK", self.$("#fekFile"), {
								withMetadata : false,
								editable : true
							});
							self.addFileList(collection, "DIMOSIEYSI", self.$("#dimosieusiFileList"), {
								withMetadata : true,
								editable : true
							});
						}
					});
					// Set Read-Only fields if is ACTIVE
					if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
						self.$("select[name=institution]").removeAttr("disabled");
						self.$("select[name=department]").removeAttr("disabled");
					} else {
						self.$("select[name=institution]").attr("disabled", true);
						self.$("select[name=department]").attr("disabled", true);
					}
				} else {
					$("#fekFile", self.$el).html($.i18n.prop("PressSave"));
					$("#profileFile", self.$el).html($.i18n.prop("PressSave"));
					self.$("select[name=institution]").removeAttr("disabled");
					self.$("select[name=department]").removeAttr("disabled");
				}
				
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
							message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
						});
						popup.show();
					}
				});
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
				if (self.model.has("id")) {
					var files = new Models.Files();
					files.url = self.model.url() + "/file";
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"), {
								withMetadata : false,
								editable : true
							});
						}
					});
					// Set Read-Only fields if is ACTIVE
					if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
						self.$("input[name=institution]").removeAttr("disabled");
					} else {
						self.$("input[name=institution]").attr("disabled", true);
					}
				} else {
					self.$("#profileFile").html($.i18n.prop("PressSave"));
					self.$("input[name=institution]").removeAttr("disabled");
				}
				break;
			case "INSTITUTION_MANAGER":
				self.$("select[name='institution']").change(function(event) {
					self.$("select[name='institution']").next(".help-block").html(jQuery("select[name='institution'] option:selected").text());
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
				// Set Read-Only fields if is ACTIVE
				if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
					self.$("select[name=institution]").removeAttr("disabled");
				} else {
					self.$("select[name=institution]").attr("disabled", true);
				}
				
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
						institution : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution')
					}
				});
				break;
			
			case "INSTITUTION_ASSISTANT":
				self.$("select[name='institution']").change(function(event) {
					self.$("select[name='institution']").next(".help-block").html(jQuery("select[name='institution'] option:selected").text());
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
				// Set Read-Only fields if is ACTIVE
				if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
					self.$("select[name=institution]").removeAttr("disabled");
				} else {
					self.$("select[name=institution]").attr("disabled", true);
				}
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
						institution : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution')
					}
				});
				break;
			
			case "MINISTRY_MANAGER":
				// Set Read-Only fields if is ACTIVE
				if (_.isEqual(self.model.get("status"), "UNAPPROVED")) {
					self.$("input[name=ministry]").removeAttr("disabled");
				} else {
					self.$("input[name=ministry]").attr("disabled", true);
				}
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
						ministry : "required"
					},
					messages : {
						ministry : $.i18n.prop('validation_ministry')
					}
				});
				break;
			}
			
			this.$('a[rel=popover]').popover();
			return this;
		},
		
		submit : function(event) {
			var self = this;
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					
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
						break;
					
					case "INSTITUTION_ASSISTANT":
						values.institution = {
							"id" : self.$('form select[name=institution]').val()
						};
						break;
					
					case "MINISTRY_MANAGER":
						values.ministry = self.$('form input[name=ministry]').val();
						break;
					}
					// Save to model
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
		
		render : function(eventName) {
			var self = this;
			self._super('render', [ eventName ]);
			// Enable All
			self.$("input").removeAttr("disabled");
			self.$("select").removeAttr("disabled");
			self.$("a#status").removeClass("disabled");
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
				self.$("input[name=name]").show();
				self.$("textarea[name=description]").show();
			} else {
				self.$("input[name=name]").hide();
				self.$("textarea[name=description]").hide();
			}
			self.$('div.progress').hide();
			
			// Initialize FileUpload widget
			self.$('input[name=file]').fileupload({
				dataType : 'json',
				url : self.model.url() + "?X-Auth-Token=" + App.authToken,
				replaceFileInput : false,
				forceIframeTransport : true,
				multipart : true,
				add : function(e, data) {
					self.$("a#upload").bind("click", function(e) {
						self.$('div.progress').show();
						self.$("a#upload").unbind("click");
						data.formData = {
							"type" : self.$("input[name=type]").val(),
							"name" : self.$("input[name=name]").val(),
							"description" : self.$("textarea[name=description]").val()
						};
						data.submit();
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
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
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
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					var tmp = {
						type : self.model.get("type"),
						url : self.model.url,
						urlRoot : self.model.urlRoot
					};
					self.model.destroy({
						wait : true,
						success : function(model, resp) {
							var popup;
							if (_.isNull(resp)) {
								// Reset Object to empty (without id) status
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
				self.$("input[name=name]").show();
				self.$("textarea[name=description]").show();
			} else {
				self.$("input[name=name]").hide();
				self.$("textarea[name=description]").hide();
			}
			self.$('div.progress').hide();
			// Initialize FileUpload widget
			self.$('input[name=file]').fileupload({
				dataType : 'json',
				url : self.collection.url + "?X-Auth-Token=" + App.authToken,
				replaceFileInput : false,
				forceIframeTransport : true,
				add : function(e, data) {
					self.$("a#upload").bind("click", function(e) {
						self.$('div.progress').show();
						self.$("a#upload").unbind("click");
						data.formData = {
							"type" : self.$("input[name=type]").val(),
							"name" : self.$("input[name=name]").val(),
							"description" : self.$("textarea[name=description]").val()
						};
						data.submit();
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
					var popup = new Views.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
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
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
					selectedModel.destroy({
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
				if (role.get("status") !== "ACTIVE") {
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
	 * AssistantsView **********************************************************
	 **************************************************************************/
	Views.AssistantsView = Views.BaseView.extend({
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
						"sLengthMenu" : "_MENU_ records per page"
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
				self.$("a#save").show();
				self.$("input").removeAttr("disabled");
			} else {
				self.$("a#save").hide();
				self.$("input").attr("disabled", true);
			}
			self.$("a#status").addClass("disabled");
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
			_.bindAll(this, "render", "select", "newPosition", "close");
			this.template = _.template(tpl_position_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
		},
		
		events : {
			"click a#createPosition" : "newPosition",
			"click a#selectPosition" : "select"
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
						"sLengthMenu" : "_MENU_ records per page"
					}
				});
			}
			return self;
		},
		
		select : function(event, position) {
			var selectedModel = position ? position : this.collection.getByCid($(event.currentTarget).attr('data-position-cid'));
			if (selectedModel) {
				this.collection.trigger("position:selected", selectedModel);
			}
		},
		
		newPosition : function(event) {
			var self = this;
			var newPosition = new Models.Position();
			self.collection.add(newPosition);
			self.select(undefined, newPosition);
		},
		
		close : function() {
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
			_.bindAll(this, "render", "submit", "cancel", "addFile", "addFileList", "close");
			this.template = _.template(tpl_position_edit);
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
			_.each(self.innerViews, function(innerView) {
				innerView.close();
			});
			self.innerViews = [];
			self.$el.html(self.template(self.model.toJSON()));
			
			// Departments
			self.$("select[name='department']").change(function(event) {
				self.$("select[name='department']").next(".help-block").html(jQuery("select[name='department'] option:selected").text());
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
			
			// Dependencies (Files, Committee):
			if (self.model.has("id")) {
				var files = new Models.Files();
				files.url = self.model.url() + "/file";
				files.fetch({
					cache : false,
					success : function(collection, response) {
						self.addFile(collection, "PRAKTIKO_SYNEDRIASIS_EPITROPIS_GIA_AKSIOLOGITES", self.$("#praktikoSynedriasisEpitropisGiaAksiologitesFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "TEKMIRIOSI_EPITROPIS_GIA_AKSIOLOGITES", self.$("#tekmiriosiEpitropisGiaAksiologitesFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "AITIMA_EPITROPIS_PROS_AKSIOLOGITES", self.$("#aitimaEpitropisProsAksiologitesFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFileList(collection, "AKSIOLOGISI_PROTOU_AKSIOLOGITI", self.$("#aksiologisiProtouAksiologitiFileList"), {
							withMetadata : true,
							editable : true
						});
						self.addFileList(collection, "AKSIOLOGISI_DEUTEROU_AKSIOLOGITI", self.$("#aksiologisiDeuterouAksiologitiFileList"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "PROSKLISI_KOSMITORA", self.$("#prosklisiKosmitoraFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFileList(collection, "EISIGISI_DEP_YPOPSIFIOU", self.$("#eisigisiDEPYpopsifiouFileList"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "PRAKTIKO_EPILOGIS", self.$("#praktikoEpilogisFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "DIAVIVASTIKO_PRAKTIKOU", self.$("#diavivastikoPraktikouFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "PRAKSI_DIORISMOU", self.$("#praksiDiorismouFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFileList(collection, "DIOIKITIKO_EGGRAFO", self.$("#dioikitikoEggrafoFileList"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "APOFASI_ANAPOMPIS", self.$("#apofasiAnapompisFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "ORGANISMOS", self.$("#organismosFile"), {
							withMetadata : true,
							editable : true
						});
						self.addFile(collection, "ESWTERIKOS_KANONISMOS", self.$("#eswterikosKanonismosFile"), {
							withMetadata : true,
							editable : true
						});
					}
				});
				
				self.addCommitteeView(self.$("#positionCommittee"));
			} else {
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
			}
			// End of files
			
			// Widgets
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
			var confirm = new Views.ConfirmView({
				title : $.i18n.prop('Confirm'),
				message : $.i18n.prop('AreYouSure'),
				yes : function() {
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
			var committeeView = new Views.PositionCommitteeView({
				maxmembers : 7,
				position : {
					id : self.model.get("id")
				},
				collection : committee
			});
			
			$el.html(committeeView.el);
			committee.fetch({
				cache : false
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
		
		uploader : undefined,
		
		initialize : function() {
			var self = this;
			
			self.template = _.template(tpl_position_committee_edit);
			_.bindAll(self, "render", "viewMember", "removeMember", "toggleAddMember", "close");
			self.collection.bind('reset', this.render, this);
			self.collection.bind('remove', this.render, this);
			self.collection.bind('add', this.render, this);
			
			self.professors = new Models.Roles();
			self.professors.on("role:selected", function(role) {
				var confirm = new Views.ConfirmView({
					title : $.i18n.prop('Confirm'),
					message : $.i18n.prop('AreYouSure'),
					yes : function() {
						var positionCommitteeMember = new Models.PositionCommitteeMember();
						positionCommitteeMember.save({
							position : {
								id : self.options.position.id
							},
							professor : role.toJSON()
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
					}
				});
				confirm.show();
			});
		},
		
		events : {
			"click a#removeMember" : "removeMember",
			"click a#addMember" : "toggleAddMember",
			"click a#viewMember" : "viewMember"
		},
		
		render : function(eventName) {
			var self = this;
			self.$el.html(self.template({
				committee : self.collection.toJSON()
			}));
			
			// Inner View
			if (self.professorListView) {
				self.professorListView.close();
			}
			self.professorListView = new Views.ProfessorListView({
				collection : self.professors
			});
			self.$("div#committee-members").hide();
			self.$("div#committee-members").html(self.professorListView.el);
			self.professors.fetch({
				data : {
					"discriminator" : "PROFESSOR_DOMESTIC,PROFESSOR_FOREIGN"
				}
			});
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
					},
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
					},
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
			self.$("div#committee-members").toggle();
			self.$("a#addMember").toggleClass('active');
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
	 * RegisterListView ********************************************************
	 **************************************************************************/
	
	Views.RegisterListView = Views.BaseView.extend({
		tagName : "div",
		
		initialize : function() {
			_.bindAll(this, "render", "select", "newRegister", "close");
			this.template = _.template(tpl_register_list);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
		},
		
		events : {
			"click a#createRegister" : "newRegister",
			"click a#select" : "select"
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
			self.$("#actions").html("<div class=\"btn-group\"><a id=\"createRegister\" class=\"btn btn-small\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_add') + " </a></div>");
			if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
				self.$("table").dataTable({
					"sDom" : "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
					"sPaginationType" : "bootstrap",
					"oLanguage" : {
						"sLengthMenu" : "_MENU_ records per page"
					}
				});
			}
			return self;
		},
		
		select : function(event, register) {
			var selectedModel = register ? register : this.collection.getByCid($(event.currentTarget).attr('data-register-cid'));
			if (selectedModel) {
				this.collection.trigger("register:selected", selectedModel);
			}
		},
		
		newRegister : function(event) {
			var self = this;
			var newRegister = new Models.Register();
			self.collection.add(newRegister);
			self.select(undefined, newRegister);
		},
		
		close : function() {
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
				self.$("select[name='institution']").next(".help-block").html(jQuery("select[name='institution'] option:selected").text());
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
						"sLengthMenu" : "_MENU_ records per page"
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
	
	return Views;
});
