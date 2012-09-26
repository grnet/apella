define([ "jquery", "underscore", "backbone", "application", "models", "text!tpl/announcement-list.html", "text!tpl/confirm.html", "text!tpl/file-edit.html", "text!tpl/home.html", "text!tpl/login-admin.html", "text!tpl/login-main.html", "text!tpl/popup.html", "text!tpl/position-committee-edit.html", "text!tpl/position-edit.html", "text!tpl/position-list.html", "text!tpl/professor-list.html", "text!tpl/register-edit.html", "text!tpl/register-list.html", "text!tpl/role-edit.html", "text!tpl/role-tabs.html", "text!tpl/role.html", "text!tpl/user-edit.html", "text!tpl/user-list.html", "text!tpl/user-registration-select.html", "text!tpl/user-registration-success.html", "text!tpl/user-registration.html", "text!tpl/user-role-info.html", "text!tpl/user-search.html", "text!tpl/user-verification.html", "text!tpl/user.html", "text!tpl/language.html", "text!tpl/file-multiple-edit.html" ], function($, _, Backbone, App, Models, tpl_announcement_list, tpl_confirm, tpl_file_edit, tpl_home,
		tpl_login_admin, tpl_login_main, tpl_popup, tpl_position_committee_edit, tpl_position_edit, tpl_position_list, tpl_professor_list, tpl_register_edit, tpl_register_list, tpl_role_edit, tpl_role_tabs, tpl_role, tpl_user_edit, tpl_user_list, tpl_user_registration_select, tpl_user_registration_success, tpl_user_registration, tpl_user_role_info, tpl_user_search, tpl_user_verification, tpl_user, tpl_language, tpl_file_multiple_edit) {
	
	var Views = {};
	// MenuView
	Views.MenuView = Backbone.View.extend({
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
			if (self.model.hasRoleWithStatus("DEPARTMENT_ASSISTANT", "ACTIVE")) {
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
	
	// LanguageView
	Views.LanguageView = Backbone.View.extend({
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
	
	// MenuView
	Views.AdminMenuView = Backbone.View.extend({
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
	
	// UserMenuView
	Views.UserMenuView = Backbone.View.extend({
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
	
	// LoginView
	Views.LoginView = Backbone.View.extend({
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
				"username" : username,
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
				"username" : username,
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
	
	// AdminLoginView
	Views.AdminLoginView = Backbone.View.extend({
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
	
	// PopupView
	Views.PopupView = Backbone.View.extend({
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
	
	// ConfirmView
	Views.ConfirmView = Backbone.View.extend({
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
	
	Views.UserRegistrationSelectView = Backbone.View.extend({
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
	
	// UserRegistrationView
	Views.UserRegistrationView = Backbone.View.extend({
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
						onlyLatin : $.i18n.prop('validation_latin'),
					},
					lastnamelatin : {
						required : $.i18n.prop('validation_lastnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin'),
					},
					fathernamelatin : {
						required : $.i18n.prop('validation_fathernamelatin'),
						onlyLatin : $.i18n.prop('validation_latin'),
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
						minlength : $.i18n.prop('validation_minlength', 2),
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
	
	// UserVerificationView
	Views.UserVerificationView = Backbone.View.extend({
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
	
	// LoginView
	Views.HomeView = Backbone.View.extend({
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
	Views.AccountView = Backbone.View.extend({
		tagName : "div",
		
		className : "box",
		
		validator : undefined,
		
		initialize : function() {
			_.bindAll(this, "render", "submit", "remove", "cancel", "close");
			this.template = _.template(tpl_user_edit);
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
			self.$el.html(this.template(self.model.toJSON()));
			
			if (!self.model.isNew()) {
				self.$("input[name=username]").attr("disabled", "disabled");
			}
			if (self.options.removable) {
				self.$("a#remove").show();
			} else {
				self.$("a#remove").hide();
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
						onlyLatin : $.i18n.prop('validation_latin'),
					},
					lastnamelatin : {
						required : $.i18n.prop('validation_lastnamelatin'),
						onlyLatin : $.i18n.prop('validation_latin'),
					},
					fathernamelatin : {
						required : $.i18n.prop('validation_fathernamelatin'),
						onlyLatin : $.i18n.prop('validation_latin'),
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
			event.preventDefault();
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
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	Views.UserView = Backbone.View.extend({
		tagName : "div",
		
		className : "",
		
		options : {
			editable : true
		},
		
		initialize : function() {
			this.template = _.template(tpl_user);
			_.bindAll(this, "render", "status", "close");
			this.model.bind("change", this.render, this);
		},
		
		events : {
			"click a.status" : "status"
		},
		
		render : function(event) {
			var self = this;
			self.$el.html(self.template(self.model.toJSON()));
			if (self.options.editable) {
				self.$("a.btn.btn-small.dropdown-toggle").removeClass("disabled");
			} else {
				self.$("a.btn.btn-small.dropdown-toggle").addClass("disabled");
			}
			return self;
		},
		
		status : function(event) {
			var status = $(event.currentTarget).attr('status');
			this.model.status({
				"status" : status
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
	
	Views.UserSearchView = Backbone.View.extend({
		tagName : "div",
		
		className : "",
		
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
	
	Views.UserListView = Backbone.View.extend({
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
	
	Views.UserRoleInfoView = Backbone.View.extend({
		tagName : "p",
		
		initialize : function() {
			this.template = _.template(tpl_user_role_info);
			this.model.bind('change', this.render);
			_.bindAll(this, "render", "close");
		},
		
		render : function(eventName) {
			var tpl_data = {
				roles : this.model.get("roles")
			};
			$(this.el).html(this.template(tpl_data));
			return this;
		},
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	// RoleTabsView
	Views.RoleTabsView = Backbone.View.extend({
		tagName : "div",
		
		className : "sidebar-nav",
		
		initialize : function() {
			_.bindAll(this, "render", "select", "newRole", "highlightSelected", "close");
			this.template = _.template(tpl_role_tabs);
			this.collection.bind("change", this.render, this);
			this.collection.bind("reset", this.render, this);
			this.collection.bind("add", this.render, this);
			this.collection.bind("remove", this.render, this);
			this.collection.bind("role:selected", this.highlightSelected, this);
		},
		
		events : {
			"click a.createRole" : "newRole",
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
				})(),
				discriminators : _.filter(App.allowedRoles, function(discriminator) {
					return true;
				})
			};
			self.$el.html(this.template(tpl_data));
			$("a[rel=\"tooltip\"]", self.$el).tooltip();
			return self;
		},
		
		select : function(event, role) {
			var selectedModel = role ? role : this.collection.getByCid($(event.currentTarget).attr('role'));
			if (selectedModel) {
				this.collection.trigger("role:selected", selectedModel);
			}
		},
		
		highlightSelected : function(role) {
			$("li.active", this.$el).removeClass("active");
			$("a[role=" + role.cid + "]", this.$el).parent("li").addClass("active");
		},
		
		newRole : function(event) {
			var self = this;
			var discriminator = $(event.currentTarget).attr('discriminator');
			var newRole = new Models.Role({
				"discriminator" : discriminator,
				user : {
					id : self.options.user
				}
			});
			self.collection.add(newRole);
			self.select(undefined, newRole);
		},
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	Views.RoleView = Backbone.View.extend({
		tagName : "div",
		
		className : "",
		
		options : {
			editable : true
		},
		
		initialize : function() {
			this.template = _.template(tpl_role);
			_.bindAll(this, "render", "status", "close");
			if (this.collection) {
				this.collection.bind("change", this.render, this);
				this.collection.bind("reset", this.render, this);
				this.collection.bind("add", this.render, this);
				this.collection.bind("remove", this.render, this);
			} else if (this.model) {
				this.model.bind("change", this.render, this);
			}
		},
		
		events : {
			"click a.status" : "status"
		},
		
		render : function(eventName) {
			var self = this;
			self.$el.empty();
			if (self.collection) {
				self.collection.each(function(role) {
					if (role.get("discriminator") !== "ADMINISTRATOR") {
						self.$el.append($(self.template(role.toJSON())));
					}
				});
			} else if (self.model) {
				if (role.get("discriminator") !== "ADMINISTRATOR") {
					self.$el.append($(self.template(self.model.toJSON())));
				}
			}
			if (self.options.editable) {
				self.$("a.btn.btn-small.dropdown-toggle").removeClass("disabled");
			} else {
				self.$("a.btn.btn-small.dropdown-toggle").addClass("disabled");
			}
			return self;
		},
		
		status : function(event) {
			var roleId = $(event.currentTarget).attr('role');
			var status = $(event.currentTarget).attr('status');
			if (this.model) {
				this.model.status({
					"status" : status
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
			} else if (this.collection) {
				this.collection.get(roleId).status({
					"status" : status
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
		},
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	Views.RoleEditView = Backbone.View.extend({
		tagName : "div",
		
		id : "roleview",
		
		className : "box",
		
		validator : undefined,
		
		innerViews : [],
		
		initialize : function() {
			_.bindAll(this, "render", "submit", "cancel", "addFile", "close");
			this.template = _.template(tpl_role_edit);
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
			
			self.$el.html(this.template(this.model.toJSON()));
			
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
				self.$("select[name='institution']").change(function() {
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
						// Trigger change to update department selector
						$("select[name='institution']", self.$el).change();
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
					var files = new Models.Files({
						urlRoot : self.model.url() + "/file"
					});
					files.fetch({
						cache : false,
						success : function(collection, response) {
							self.addFile(collection, "PROFILE", self.$("#profileFile"));
							self.addFile(collection, "FEK", self.$("#fekFile"));
						}
					});
				} else {
					$("#fekFile", self.$el).html($.i18n.prop("PressSave"));
					$("#profileFile", self.$el).html($.i18n.prop("PressSave"));
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
					self.addFile("profileFile", $("#profileFile", this.$el));
				} else {
					$("#profileFile", self.$el).html($.i18n.prop("PressSave"));
				}
				break;
			case "INSTITUTION_MANAGER":
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
						institution : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution')
					}
				});
				break;
			
			case "INSTITUTION_ASSISTANT":
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
						institution : "required"
					},
					messages : {
						institution : $.i18n.prop('validation_institution')
					}
				});
				break;
			
			case "DEPARTMENT_ASSISTANT":
				App.departments = App.departments ? App.departments : new Models.Departments();
				App.departments.fetch({
					cache : true,
					success : function(collection, resp) {
						collection.each(function(department) {
							if (_.isObject(self.model.get("department")) && _.isEqual(department.id, self.model.get("department").id)) {
								$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "' selected>" + department.get("institution").name + ": " + department.get("department") + "</option>");
							} else {
								$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "'>" + department.get("institution").name + ": " + department.get("department") + "</option>");
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
						department : "required"
					},
					messages : {
						department : $.i18n.prop('validation_department')
					}
				});
				break;
			
			case "MINISTRY_MANAGER":
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
					
					case "DEPARTMENT_ASSISTANT":
						values.department = {};
						values.department.id = self.$('form select[name=department]').val();
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
		},
		
		close : function() {
			_.each(this.innerViews, function(innerView) {
				innerView.close();
			});
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	Views.FileView = Backbone.View.extend({
		tagName : "div",
		
		initialize : function() {
			this.template = _.template(tpl_file_edit);
			_.bindAll(this, "render", "deleteFile", "close");
			this.model.bind('change', this.render, this);
		},
		
		events : {
			"click a#delete" : "deleteFile",
		},
		
		render : function(eventName) {
			var self = this;
			var tpl_data = {
				withMetadata : self.options.withMetadata,
				file : self.model.toJSON()
			};
			if (_.isObject(tpl_data.file.currentBody)) {
				tpl_data.file.currentBody.url = self.model.url() + "/body";
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
				url : self.model.url(),
				replaceFileInput : false,
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
						self.model.set(JSON.parse(data.jqXHR.responseText));
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
					
				},
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
	
	Views.FileListView = Backbone.View.extend({
		tagName : "div",
		
		uploader : undefined,
		
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
					file.currentBody.url = model.url() + "/body";
				}
				tpl_data.files.push(file);
			});
			self.$el.html(self.template(tpl_data));
			
			// if (!$.fn.DataTable.fnIsDataTable(self.$("table"))) {
			// self.$("table").dataTable({
			// "sDom" :
			// "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
			// "sPaginationType" : "bootstrap",
			// "oLanguage" : {
			// "sLengthMenu" : "_MENU_ records per page"
			// }
			// });
			// }
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
				url : self.collection.url,
				replaceFileInput : false,
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
						var newFile = new Models.File(JSON.parse(data.jqXHR.responseText));
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
					
				},
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
	
	// AnnouncementsView
	Views.AnnouncementListView = Backbone.View.extend({
		tagName : "div",
		
		className : "well",
		
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
						url : "#profile/" + role.id
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
	 * ***** AssistantsView
	 * *******************************************************
	 **************************************************************************/
	Views.AssistantsView = Backbone.View.extend({
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
			"click a#createInstitutionAssistant" : "createInstitutionAssistant",
			"click a#createDepartmentAssistant" : "createDepartmentAssistant"
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
			self.$("#actions").html("<div class=\"btn-group input-append\"><a id=\"createInstitutionAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_ia') + " </a></div><div class=\"btn-group input-append\"><select name=\"department\"></select><a id=\"createDepartmentAssistant\" class=\"btn btn-small add-on\"><i class=\"icon-plus\"></i> " + $.i18n.prop('btn_create_da') + "</a></div>");
			App.departments = App.departments ? App.departments : new Models.Departments();
			App.departments.fetch({
				cache : true,
				success : function(collection, resp) {
					_.each(collection.filter(function(department) {
						return App.loggedOnUser.isAssociatedWithDepartment(department);
					}), function(department) {
						$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "'>" + department.get("institution").name + ": " + department.get("department") + "</option>");
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
		
		createDepartmentAssistant : function(event) {
			var self = this;
			var user = new Models.User({
				"roles" : [ {
					"discriminator" : "DEPARTMENT_ASSISTANT",
					"department" : App.departments.get(self.$("select[name=department]").val()).toJSON()
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
	 * ***** PositionView *********
	 **************************************************************************/
	Views.PositionListView = Backbone.View.extend({
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
	
	Views.PositionEditView = Backbone.View.extend({
		tagName : "div",
		
		id : "positionview",
		
		className : "box",
		
		validator : undefined,
		
		initialize : function() {
			_.bindAll(this, "render", "submit", "cancel", "addFile", "close");
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
			self.$el.html(self.template(self.model.toJSON()));
			
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
				self.addFile("fekFile", this.$("#fekFile"));
				self.addFile("prosklisiKosmitora", this.$("#prosklisiKosmitora"));
				self.addFile("recommendatoryReport", this.$("#recommendatoryReport"));
				self.addFile("recommendatoryReportSecond", this.$("#recommendatoryReportSecond"));
				self.addCommitteeView(this.$("#committee"));
			} else {
				self.$("#fekFile").html($.i18n.prop("PressSave"));
				self.$("#prosklisiKosmitora").html($.i18n.prop("PressSave"));
				self.$("#recommendatoryReport").html($.i18n.prop("PressSave"));
				self.$("#recommendatoryReportSecond").html($.i18n.prop("PressSave"));
				self.$("#committee").html($.i18n.prop("PressSave"));
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
					name : "required",
					description : "required",
					department : "required",
					subject : "required",
					status : "required",
					deanStatus : "required",
					fek : "required",
					fekSentDate : "required"
				},
				messages : {
					name : $.i18n.prop('validation_positionName'),
					description : $.i18n.prop('validation_description'),
					department : $.i18n.prop('validation_department'),
					subject : $.i18n.prop('validation_subject'),
					status : $.i18n.prop('validation_positionStatus'),
					deanStatus : $.i18n.prop('validation_positionDeanStatus'),
					fek : $.i18n.prop('validation_fek'),
					fekSentDate : $.i18n.prop('validation_fekSentDate')
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
					values.description = self.$('form input[name=description]').val();
					values.department = {
						"id" : self.$('form select[name=department]').val()
					};
					values.subject = {
						"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
						"name" : self.$('form textarea[name=subject]').val()
					};
					values.status = self.$('form input[name=status]').val();
					values.deanStatus = self.$('form input[name=deanStatus]').val();
					values.fek = self.$('form input[name=fek]').val();
					values.fekSentDate = self.$('form input[name=fekSentDate]').val();
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
		
		addFile : function(type, $el) {
			var self = this;
			var fileView;
			var file;
			var fileAttributes = self.model.has(type) ? self.model.get(type) : {};
			fileAttributes.name = type;
			file = new Models.File(fileAttributes);
			file.urlRoot = self.model.url() + "/" + type;
			fileView = new Views.FileView({
				model : file
			});
			file.bind("change", function() {
				self.model.set(type, file.toJSON(), {
					silent : true
				});
			});
			$el.html(fileView.render().el);
		},
		
		addFileList : function(type, $el) {
			var files = new Models.Files();
			var fileListView = new Views.FileListView({
				collection : files
			});
			files.url = self.model.url() + "/" + type;
			$el.html(fileListView.el);
			files.fetch({
				cache : false
			});
		},
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	Views.PositionCommitteeView = Backbone.View.extend({
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
				// Fill Details View:
				var user = new Models.User({
					"id" : selectedModel.get("professor").user.id
				});
				var roles = new Models.Roles();
				roles.user = selectedModel.get("professor").user.id;
				var userView = new Views.UserView({
					editable : false,
					model : user
				});
				var roleView = new Views.RoleView({
					editable : false,
					collection : roles
				});
				user.fetch({
					cache : false
				});
				roles.fetch({
					cache : false
				});
				
				self.$("div#commiteeMemberDetails div.modal-body").append(userView.render().el);
				self.$("div#commiteeMemberDetails div.modal-body").append(roleView.render().el);
				self.$("div#commiteeMemberDetails").on("hidden", function() {
					userView.close();
					roleView.close();
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
	 * ****** RegisterEditView
	 * *****************************************************
	 **************************************************************************/
	Views.RegisterListView = Backbone.View.extend({
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
	
	Views.RegisterEditView = Backbone.View.extend({
		tagName : "div",
		
		id : "registerview",
		
		className : "box",
		
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
				self.addFile("registerFile", this.$("#registerFile"));
			} else {
				self.$("#registerFile").html($.i18n.prop("PressSave"));
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
					department : "required"
				},
				messages : {
					department : $.i18n.prop('validation_department')
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
					values.department = {
						"id" : self.$('form select[name=department]').val(),
						"institution" : {}
					};
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
		
		addFile : function(type, $el) {
			var self = this;
			var fileView;
			var file;
			var fileAttributes = self.model.has(type) ? self.model.get(type) : {};
			fileAttributes.name = type;
			file = new Models.File(fileAttributes);
			file.urlRoot = self.model.url() + "/" + type;
			fileView = new Views.FileView({
				model : file
			});
			file.bind("change", function() {
				self.model.set(type, file.toJSON(), {
					silent : true
				});
			});
			$el.html(fileView.render().el);
		},
		
		close : function() {
			$(this.el).unbind();
			$(this.el).remove();
		}
	});
	
	// RoleTabsView
	Views.ProfessorListView = Backbone.View.extend({
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
				// Fill Details View:
				var user = new Models.User({
					"id" : selectedModel.get("user").id
				});
				var roles = new Models.Roles();
				roles.user = selectedModel.get("user").id;
				var userView = new Views.UserView({
					editable : false,
					model : user
				});
				var roleView = new Views.RoleView({
					editable : false,
					collection : roles
				});
				user.fetch({
					cache : false
				});
				roles.fetch({
					cache : false
				});
				
				self.$("div#professorDetails div.modal-body").append(userView.render().el);
				self.$("div#professorDetails div.modal-body").append(roleView.render().el);
				self.$("div#professorDetails").on("hidden", function() {
					userView.close();
					roleView.close();
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
