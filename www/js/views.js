// MenuView
App.MenuView = Backbone.View.extend({
	el : "div#menu",
	
	tagName : "ul",
	
	className : "nav",
	
	initialize : function() {
		_.bindAll(this, "render");
		this.model.bind('change', this.render);
	},
	
	events : {},
	
	render : function(eventName) {
		this.$el.empty();
		this.$el.append("<ul class=\"nav\">");
		this.$el.find("ul").append("<li><a href=\"\#profile\">" + $.i18n.prop('menu_profile') + "</a></li>");
		return this;
	}

});

// MenuView
App.AdminMenuView = Backbone.View.extend({
	el : "div#menu",
	
	tagName : "ul",
	
	className : "nav",
	
	initialize : function() {
		_.bindAll(this, "render");
		this.model.bind('change', this.render);
	},
	
	events : {},
	
	render : function(eventName) {
		this.$el.empty();
		this.$el.append("<ul class=\"nav\">");
		this.$el.find("ul").append("<li><a href=\"\#users\">" + $.i18n.prop('adminmenu_users') + "</a></li>");
		return this;
	}

});

// UserMenuView
App.UserMenuView = Backbone.View.extend({
	el : "div#user-menu",
	
	className : "nav",
	
	initialize : function() {
		_.bindAll(this, "render", "logout");
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
		this.$el.find("ul").append("<li><a id=\"logout\" href=\"javascript:void(0)\">" + $.i18n.prop('menu_logout') + "</a>");
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
	}
});

// LoginView
App.LoginView = Backbone.View.extend({
	tagName : "div",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "login");
		this.template = _.template(tpl.get('login-main'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#save" : function() {
			this.$("form").submit();
		},
		"submit form" : "login"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
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
				// Notify AppRouter to start Application (fill Header and handle
				// history token)
				self.model.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				var popup = new App.PopupView({
					type : "error",
					message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
				});
				popup.show();
			}
		});
	}
});

// LoginView
App.AdminLoginView = Backbone.View.extend({
	tagName : "div",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "login");
		this.template = _.template(tpl.get('login-admin'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "login"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
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
				// Notify AppRouter to start Application (fill Header and handle
				// history token)
				self.model.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				var popup = new App.PopupView({
					type : "error",
					message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
				});
				popup.show();
			}
		});
	}
});

// PopupView
App.PopupView = Backbone.View.extend({
	tagName : "div",
	
	className : "alert fade in",
	
	initialize : function() {
		this.template = _.template(tpl.get('popup'));
		_.bindAll(this, "render", "show");
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
	}
});

// ConfirmView
App.ConfirmView = Backbone.View.extend({
	tagName : "div",
	
	className : "modal",
	
	initialize : function() {
		this.template = _.template(tpl.get('confirm'));
		_.bindAll(this, "render", "show");
	},
	
	events : {
		"click a#yes" : function(event) {
			this.$el.modal('hide');
			if (_.isFunction(this.options.yes)) {
				this.options.yes();
			}
		},
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
	}
});

App.UserRegistrationSelectView = Backbone.View.extend({
	tagName : "div",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render");
		this.template = _.template(tpl.get('user-registration-select'));
	},
	
	events : {},
	
	render : function(eventName) {
		$(this.el).html(this.template({
			roles : App.allowedRoles
		}));
		return this;
	}
});

// UserRegistrationView
App.UserRegistrationView = Backbone.View.extend({
	tagName : "div",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "submit", "selectInstitution");
		this.template = _.template(tpl.get('user-registration'));
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
		// institution first in case their institution supports Shibboleth Login
		if (role.discriminator === "PROFESSOR_DOMESTIC") {
			// Add institutions in selector:
			App.institutions = App.institutions ? App.institutions : new App.Institutions();
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
					var popup = new App.PopupView({
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
				firstnamelatin : "required",
				lastnamelatin : "required",
				password : {
					required : true,
					minlength : 5
				},
				confirm_password : {
					required : true,
					minlength : 5,
					equalTo : "form input[name=password]"
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
				firstnamelatin : $.i18n.prop('validation_firstnamelatin'),
				lastnamelatin : $.i18n.prop('validation_lastnamelatin'),
				username : {
					required : $.i18n.prop('validation_username'),
					email : $.i18n.prop('validation_username'),
					minlength : $.i18n.prop('validation_minlength', 2)
				},
				password : {
					required : $.i18n.prop('validation_password'),
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
		var firstnamelatin = self.$('form input[name=firstnamelatin]').val();
		var lastnamelatin = self.$('form input[name=lastnamelatin]').val();
		var password = self.$('form input[name=password]').val();
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
				"lastname" : lastname
			},
			"basicInfoLatin" : {
				"firstname" : firstnamelatin,
				"lastname" : lastnamelatin
			},
			"contactInfo" : {
				"address" : {
					"street" : address_street,
					"number" : address_number,
					"zip" : address_zip,
					"city" : address_city,
					"country" : address_country
				},
				"email" : username,
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
				var popup = new App.PopupView({
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
	}
});

// UserVerificationView
App.UserVerificationView = Backbone.View.extend({
	tagName : "div",
	
	initialize : function() {
		this.template = _.template(tpl.get('user-verification'));
		this.model.bind('change', this.render);
		_.bindAll(this, "render");
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}

});

// LoginView
App.HomeView = Backbone.View.extend({
	tagName : "div",
	
	initialize : function() {
		_.bindAll(this, "render");
		this.template = _.template(tpl.get('home'));
		this.model.bind('change', this.render);
	},
	
	events : {},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});

// AccountView
App.AccountView = Backbone.View.extend({
	tagName : "div",
	
	className : "box",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "submit", "cancel");
		this.template = _.template(tpl.get('user-edit'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#cancel" : "cancel",
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "submit"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
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
				firstname : "required",
				lastname : "required",
				firstnamelatin : "required",
				lastnamelatin : "required",
				password : {
					minlength : 5
				},
				confirm_password : {
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
				firstname : $.i18n.prop('validation_firstname'),
				lastname : $.i18n.prop('validation_lastname'),
				firstnamelatin : $.i18n.prop('validation_firstnamelatin'),
				lastnamelatin : $.i18n.prop('validation_lastnamelatin'),
				password : {
					required : $.i18n.prop('validation_password'),
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
		return this;
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
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				
				// Read Input
				var firstname = self.$('form input[name=firstname]').val();
				var lastname = self.$('form input[name=lastname]').val();
				var firstnamelatin = self.$('form input[name=firstnamelatin]').val();
				var lastnamelatin = self.$('form input[name=lastnamelatin]').val();
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
					"basicInfo" : {
						"firstname" : firstname,
						"lastname" : lastname
					},
					"basicInfoLatin" : {
						"firstname" : firstnamelatin,
						"lastname" : lastnamelatin
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
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new App.PopupView({
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
	}
});

App.UserView = Backbone.View.extend({
	tagName : "div",
	
	className : "",
	
	initialize : function() {
		this.template = _.template(tpl.get('user'));
		_.bindAll(this, "render", "status");
		this.model.bind("change", this.render, this);
	},
	
	events : {
		"click a.status" : "status"
	},
	
	render : function(event) {
		var self = this;
		self.$el.html(self.template(self.model.toJSON()));
		return self;
	},
	
	status : function(event) {
		var status = $(event.target).attr('status');
		this.model.status({
			"status" : status
		}, {
			wait : true,
			success : function(model, resp) {
				var popup = new App.PopupView({
					type : "success",
					message : $.i18n.prop("Success")
				});
				popup.show();
			},
			error : function(model, resp, options) {
				var popup = new App.PopupView({
					type : "error",
					message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
				});
				popup.show();
			}
		});
	}
});

App.UserSearchView = Backbone.View.extend({
	tagName : "div",
	
	className : "",
	
	initialize : function() {
		_.bindAll(this, "render", "search");
		this.template = _.template(tpl.get('user-search'));
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
	}
});

App.UserListView = Backbone.View.extend({
	tagName : "table",
	
	className : "table table-striped table-bordered table-condensed",
	
	initialize : function() {
		_.bindAll(this, "render", "select");
		this.template = _.template(tpl.get('user-list'));
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
					var item = model.toJSON();
					item.cid = model.cid;
					result.push(item);
				});
				return result;
			})()
		};
		self.$el.html(self.template(tpl_data));
		return self;
	},
	
	select : function(event) {
		var selectedModel = this.collection.getByCid($(event.target).attr('user'));
		this.collection.trigger("user:selected", selectedModel);
	}
});

// RoleListView
App.RoleListView = Backbone.View.extend({
	tagName : "div",
	
	className : "sidebar-nav",
	
	initialize : function() {
		_.bindAll(this, "render", "select", "newRole", "highlightSelected");
		this.template = _.template(tpl.get('role-list'));
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
		var selectedModel = role ? role : this.collection.getByCid($(event.target).attr('role'));
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
		var discriminator = $(event.target).attr('discriminator');
		var newRole = new App.Role({
			"discriminator" : discriminator,
			user : self.options.user
		});
		self.collection.add(newRole);
		self.select(undefined, newRole);
	}
});

App.RoleView = Backbone.View.extend({
	tagName : "div",
	
	className : "",
	
	initialize : function() {
		this.template = _.template(tpl.get('role'));
		_.bindAll(this, "render", "status");
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
					self.$el.append($(self.template(role.toJSON())).addClass("well"));
				}
			});
		} else if (self.model) {
			if (role.get("discriminator") !== "ADMINISTRATOR") {
				self.$el.append($(self.template(self.model.toJSON())).addClass("well"));
			}
		}
		return self;
	},
	
	status : function(event) {
		var roleId = $(event.target).attr('role');
		var status = $(event.target).attr('status');
		if (this.model) {
			this.model.status({
				"status" : status
			}, {
				wait : true,
				success : function(model, resp) {
					var popup = new App.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new App.PopupView({
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
					var popup = new App.PopupView({
						type : "success",
						message : $.i18n.prop("Success")
					});
					popup.show();
				},
				error : function(model, resp, options) {
					var popup = new App.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
		}
	},
	
	close : function() {
		this.$el.unbind();
		this.$el.remove();
	}
});

App.RoleEditView = Backbone.View.extend({
	tagName : "div",
	
	id : "roleview",
	
	className : "box",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "submit", "cancel", "addFile");
		this.template = _.template(tpl.get('role-edit'));
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
		self.$el.html(this.template(this.model.toJSON()));
		
		switch (self.model.get("discriminator")) {
		case "CANDIDATE":
			if (self.model.get("id") !== undefined) {
				self.addFile("cv", $("#cv", this.$el));
				self.addFile("identity", $("#identity", this.$el));
				self.addFile("military1599", $("#military1599", this.$el));
				self.addFileList("publications", $("#publications", this.$el));
			} else {
				$("#cv", self.$el).html($.i18n.prop("PressSave"));
				$("#identity", self.$el).html($.i18n.prop("PressSave"));
				$("#military1599", self.$el).html($.i18n.prop("PressSave"));
				$("#publications", self.$el).html($.i18n.prop("PressSave"));
			}
			break;
		case "PROFESSOR_DOMESTIC":
			App.institutions = App.institutions ? App.institutions : new App.Institutions();
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
					var popup = new App.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			App.ranks = App.ranks ? App.ranks : new App.Ranks();
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
					var popup = new App.PopupView({
						type : "error",
						message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
					});
					popup.show();
				}
			});
			
			if (self.model.has("id")) {
				self.addFile("fekFile", $("#fekFile", this.$el));
			} else {
				$("#fekFile", self.$el).html($.i18n.prop("PressSave"));
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
					position : "required",
					subject : "required",
					fek : "required",
					fekSubject : "required"
				},
				messages : {
					institution : $.i18n.prop('validation_institution'),
					profileURL : $.i18n.prop('validation_profileURL'),
					rank : $.i18n.prop('validation_rank'),
					position : $.i18n.prop('validation_position'),
					subject : $.i18n.prop('validation_subject'),
					fek : $.i18n.prop('validation_fek'),
					fekSubject : $.i18n.prop('validation_fekSubject')
				}
			});
			
			break;
		case "PROFESSOR_FOREIGN":
			App.ranks = App.ranks ? App.ranks : new App.Ranks();
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
					var popup = new App.PopupView({
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
					position : "required",
					subject : "required"
				},
				messages : {
					institution : $.i18n.prop('validation_institution'),
					profileURL : $.i18n.prop('validation_profileURL'),
					rank : $.i18n.prop('validation_rank'),
					position : $.i18n.prop('validation_position'),
					subject : $.i18n.prop('validation_subject')
				}
			});
			break;
		case "INSTITUTION_MANAGER":
			App.institutions = App.institutions ? App.institutions : new App.Institutions();
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
					var popup = new App.PopupView({
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
			App.institutions = App.institutions ? App.institutions : new App.Institutions();
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
					var popup = new App.PopupView({
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
		
		case "DEPARTMENT_MANAGER":
			App.departments = App.departments ? App.departments : new App.Departments();
			App.departments.fetch({
				cache : true,
				success : function(collection, resp) {
					collection.each(function(department) {
						if (_.isObject(self.model.get("department")) && _.isEqual(department.id, self.model.get("department").id)) {
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "' selected>" + department.get("institution").name + ":" + department.get("department") + "</option>");
						} else {
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "'>" + department.get("institution").name + ":" + department.get("department") + "</option>");
						}
					});
				},
				error : function(model, resp, options) {
					var popup = new App.PopupView({
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
		
		return this;
	},
	
	submit : function(event) {
		var self = this;
		var confirm = new App.ConfirmView({
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
					values.rank = {
						"id" : self.$('form select[name=rank]').val()
					};
					values.profileURL = self.$('form input[name=profileURL]').val();
					values.position = self.$('form input[name=position]').val();
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
					values.position = self.$('form input[name=position]').val();
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
				
				case "DEPARTMENT_MANAGER":
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
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new App.PopupView({
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
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				self.model.destroy({
					wait : true,
					success : function(model, resp) {
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new App.PopupView({
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
	},
	
	addFile : function(type, $el) {
		var self = this;
		var fileView;
		var file;
		var fileAttributes = self.model.has(type) ? self.model.get(type) : {};
		fileAttributes.name = type;
		file = new App.File(fileAttributes);
		file.urlRoot = self.model.url() + "/" + type;
		fileView = new App.FileView({
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
		var self = this;
		var files = new App.Files();
		files.url = self.model.url() + "/" + type;
		var fileListView = new App.FileListView({
			collection : files
		});
		$el.html(fileListView.el);
		files.fetch({
			cache : false
		});
	}
});

App.FileView = Backbone.View.extend({
	tagName : "table",
	
	className : "table table-striped table-bordered table-condensed",
	
	initialize : function() {
		this.template = _.template(tpl.get('file-edit'));
		_.bindAll(this, "render", "deleteFile", "uploadFile", "close");
		this.model.bind('change', this.render, this);
	},
	
	events : {
		"click a#upload" : "uploadFile",
		"click a#delete" : "deleteFile",
	},
	
	render : function(eventName) {
		var self = this;
		self.$el.html(self.template({
			files : [ self.model.toJSON() ]
		}));
		return self;
	},
	
	uploadFile : function(event) {
		var self = this;
		var uploader = $("div.uploader", self.$el).pluploadQueue({
			runtimes : 'html5,flash,browserplus',
			url : self.model.url(),
			max_file_size : '30mb',
			unique_names : true,
			// Flash settings
			flash_swf_url : 'lib/plupload/plupload.flash.swf',
			// Silverlight settings
			silverlight_xap_url : 'lib/plupload/plupload.silverlight.xap',
			headers : {
				"X-Auth-Token" : App.authToken
			},
			init : {
				FilesAdded : function(uploader, files) {
					var i, len = uploader.files.length;
					for (i = 0; i < len - 1; i++) {
						uploader.removeFile(uploader.files[i]);
					}
				},
				FileUploaded : function(uploader, file, response) {
					var attributes = JSON.parse(response.response);
					self.model.set(attributes, {
						silent : true
					});
				}
			}
		});
		self.$("div.modal").on("hidden", function() {
			uploader.pluploadQueue().destroy();
			uploader.empty();
			if (self.model.hasChanged()) {
				self.model.change();
			}
		});
		self.$("div.modal").modal('show');
	},
	
	deleteFile : function(event) {
		var self = this;
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				self.model.destroy({
					wait : true,
					success : function(model, resp) {
						var name = model.get("name");
						var popup;
						if (_.isNull(resp)) {
							self.model.set(self.model.defaults, {
								silent : true
							});
							self.model.set("name", name);
							popup = new App.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
						} else {
							self.model.set(resp);
							popup = new App.PopupView({
								type : "warning",
								message : $.i18n.prop("FileReverted")
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new App.PopupView({
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
		if (!_.isUndefined(this.uploader)) {
			this.uploader.destroy();
		}
		$(this.el).unbind();
		$(this.el).remove();
	}
});

App.FileListView = Backbone.View.extend({
	tagName : "table",
	
	className : "table table-striped table-bordered table-condensed",
	
	uploader : undefined,
	
	initialize : function() {
		this.template = _.template(tpl.get('file-edit'));
		_.bindAll(this, "render", "deleteFile", "uploadFile", "close");
		this.collection.bind('reset', this.render, this);
		this.collection.bind('remove', this.render, this);
		this.collection.bind('add', this.render, this);
	},
	
	events : {
		"click a#upload" : "uploadFile",
		"click a#delete" : "deleteFile",
	},
	
	render : function(eventName) {
		var self = this;
		self.$el.html(self.template({
			files : self.collection.toJSON()
		}));
		return self;
	},
	
	uploadFile : function(event) {
		var self = this;
		var length = self.collection.length;
		var uploader = $("div.uploader", self.$el).pluploadQueue({
			runtimes : 'html5,flash,browserplus',
			url : self.collection.url,
			max_file_size : '30mb',
			unique_names : true,
			// Flash settings
			flash_swf_url : 'lib/plupload/plupload.flash.swf',
			// Silverlight settings
			silverlight_xap_url : 'lib/plupload/plupload.silverlight.xap',
			headers : {
				"X-Auth-Token" : App.authToken
			},
			init : {
				FileUploaded : function(uploader, file, response) {
					var attributes = JSON.parse(response.response);
					self.collection.add(new App.File(attributes), {
						silent : true
					});
				}
			}
		});
		self.$("div.modal").on("hidden", function() {
			uploader.pluploadQueue().destroy();
			uploader.empty();
			if (length !== self.collection.length) {
				self.collection.trigger("reset");
			}
		});
		self.$("div.modal").modal('show');
	},
	
	deleteFile : function(event) {
		var self = this;
		var selectedModel = self.collection.get($(event.target).attr('fileId'));
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				selectedModel.destroy({
					wait : true,
					success : function(model, resp) {
						var popup;
						if (_.isNull(resp)) {
							popup = new App.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
						} else {
							selectedModel.set(resp);
							self.collection.add(selectedModel);
							popup = new App.PopupView({
								type : "warning",
								message : $.i18n.prop("FileReverted")
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						var popup = new App.PopupView({
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
		$(this.el).unbind();
		$(this.el).remove();
	}
});

// AnnouncementsView
App.AnnouncementListView = Backbone.View.extend({
	tagName : "div",
	
	className : "well",
	
	initialize : function() {
		this.template = _.template(tpl.get('announcement-list'));
		_.bindAll(this, "render");
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
					text : (function() {
						return $.i18n.prop('RoleIsNotActive', $.i18n.prop(role.get('discriminator')));
					})(),
					url : "#profile/" + role.id
				});
			}
			
		});
		// 2. Show
		self.$el.html(self.template(data));
		
		return self;
	}

});