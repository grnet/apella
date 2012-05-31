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
		console.log("Logging out");
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
		var username = $('form input[name=username]', self.el).val();
		var password = $('form input[name=password]', self.el).val();
		
		// Save to model
		self.model.login({
			"username" : username,
			"password" : password
		}, {
			wait : true,
			success : function(model, resp) {
				// Notify AppRouter to start Application (fill Header and handle
				// history token)
				console.log("Succesful Login", model, resp);
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
		var username = $('form input[name=username]', self.el).val();
		var password = $('form input[name=password]', self.el).val();
		
		// Save to model
		self.model.login({
			"username" : username,
			"password" : password
		}, {
			wait : true,
			success : function(model, resp) {
				// Notify AppRouter to start Application (fill Header and handle
				// history token)
				console.log("Succesful Login", model, resp);
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
			console.log("Click a#yes");
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
		_.bindAll(this, "render", "submit");
		this.template = _.template(tpl.get('user-registration'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "submit"
	},
	
	render : function(eventName) {
		console.log("UserRegistrationView: render");
		$(this.el).html(this.template(this.model.get('roles')[0]));
		
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
					email : true,
					minlength : 2
				},
				firstname : "required",
				lastname : "required",
				password : {
					required : true,
					minlength : 5
				},
				confirm_password : {
					required : true,
					minlength : 5,
					equalTo : "form input[name=password]"
				},
				phoneNumber : {
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
				phoneNumber : {
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
		
		return this;
	},
	
	submit : function(event) {
		console.log("UserRegistrationView: submit");
		var self = this;
		
		// Read Input
		var username = $('form input[name=username]', this.el).val();
		var firstname = $('form input[name=firstname]', this.el).val();
		var lastname = $('form input[name=lastname]', this.el).val();
		var password = $('form input[name=password]', this.el).val();
		var phoneNumber = $('form input[name=phoneNumber]', this.el).val();
		var address_street = $('form input[name=address_street]', this.el).val();
		var address_number = $('form input[name=address_number]', this.el).val();
		var address_zip = $('form input[name=address_zip]', this.el).val();
		var address_city = $('form input[name=address_city]', this.el).val();
		var address_country = $('form input[name=address_country]', this.el).val();
		
		// Validate
		
		// Save to model
		self.model.save({
			"username" : username,
			"basicInfo" : {
				"firstname" : firstname,
				"lastname" : lastname
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
				"phoneNumber" : phoneNumber
			},
			"password" : password
		}, {
			wait : true,
			success : function(model, resp) {
				console.log("UserRegistrationView: save(success):", model, resp);
				$("#messages", self.$el).html("Η εγγραφή ολοκληρώθηκε, θα σας αποσταλεί e-mail........");
				var popup = new App.PopupView({
					type : "success",
					message : $.i18n.prop("RegistrationSuccess")
				});
				popup.show();
			},
			error : function(model, resp, options) {
				console.log(model, resp, options);
				var popup = new App.PopupView({
					type : "error",
					message : $.i18n.prop("Error") + " (" + resp.status + ") : " + $.i18n.prop("error." + resp.getResponseHeader("X-Error-Code"))
				});
				popup.show();
			}
		});
		event.preventDefault();
		return false;
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
				username : {
					required : true,
					minlength : 2
				},
				firstname : "required",
				lastname : "required",
				password : {
					minlength : 5
				},
				confirm_password : {
					minlength : 5,
					equalTo : "form input[name=password]"
				},
				phoneNumber : {
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
				username : {
					required : $.i18n.prop('validation_username'),
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
				phoneNumber : {
					required : $.i18n.prop('validation_phone'),
					number : $.i18n.prop('validation_number'),
					minlength : $.i18n.prop('validation_minlength', 10),
					maxlength : $.i18n.prop('validation_maxlength', 12)
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
				var username = $('form input[name=username]', this.el).val();
				var firstname = $('form input[name=firstname]', this.el).val();
				var lastname = $('form input[name=lastname]', this.el).val();
				var password = $('form input[name=password]', this.el).val();
				var phoneNumber = $('form input[name=phoneNumber]', this.el).val();
				var address_street = $('form input[name=address_street]', this.el).val();
				var address_number = $('form input[name=address_number]', this.el).val();
				var address_zip = $('form input[name=address_zip]', this.el).val();
				var address_city = $('form input[name=address_city]', this.el).val();
				var address_country = $('form input[name=address_country]', this.el).val();
				
				// Validate
				
				// Save to model
				self.model.save({
					"username" : username,
					"basicInfo" : {
						"firstname" : firstname,
						"lastname" : lastname
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
						"phoneNumber" : phoneNumber
					},
					"password" : password
				}, {
					wait : true,
					success : function(model, resp) {
						console.log("UserView:save(success):", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("UserView:save(error):", model, resp);
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

App.UserSearchView = Backbone.View.extend({
	tagName : "div",
	
	className : "",
	
	initialize : function() {
		_.bindAll(this, "render", "search");
		this.template = _.template(tpl.get('role-search'));
		this.collection.bind("change", this.render, this);
		this.collection.bind("reset", this.render, this);
	},
	
	events : {
		"click a#search" : "search"
	},
	
	render : function(eventName) {
		var self = this;
		console.log("UserSearchView:render");
		self.$el.html(this.template());
		return self;
	},
	
	search : function(event) {
		var self = this;
		var searchData = {
			email : $('form input[name=email]', this.el).val(),
			firstname : $('form input[name=firstname]', this.el).val(),
			lastname : $('form input[name=lastname]', this.el).val(),
			status : $('form select[name=status]', this.el).val(),
			role : $('form select[name=role]', this.el).val(),
			roleStatus : $('form select[name=roleStatus]', this.el).val()
		};
		
	}
});

// RoleView
App.RoleListView = Backbone.View.extend({
	tagName : "div",
	
	className : "sidebar-nav",
	
	initialize : function() {
		_.bindAll(this, "render", "select", "newRole", "displayRole");
		this.template = _.template(tpl.get('role-list'));
		this.collection.bind("change", this.render, this);
		this.collection.bind("reset", this.render, this);
		this.collection.bind("add", this.render, this);
		this.collection.bind("remove", this.render, this);
	},
	
	events : {
		"click a.createRole" : "newRole",
		"click a.selectRole" : "select"
	},
	
	render : function(eventName) {
		console.log("RoleListView:render");
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
		return this;
	},
	
	select : function(event) {
		var self = this;
		var selectedModel = self.collection.getByCid($(event.target).attr('role'));
		self.displayRole(selectedModel);
	},
	
	newRole : function(event) {
		var self = this;
		var discriminator = $(event.target).attr('discriminator');
		var newRole = new App.Role({
			"discriminator" : discriminator,
			user : self.options.user
		});
		self.collection.add(newRole);
		self.displayRole(newRole);
	},
	
	displayRole : function(role) {
		console.log("RoleListView: displayRole");
		var roleView = new App.RoleView({
			model : role
		});
		// Update Selected:
		$("li.active", this.$el).removeClass("active");
		console.log("li[role=" + role.cid + "]");
		$("a[role=" + role.cid + "]", this.$el).parent("li").addClass("active");
		// Update history
		if (role.id) {
			App.router.navigate("profile/" + role.id, {
				trigger : false
			});
		} else {
			App.router.navigate("profile", {
				trigger : false
			});
		}
		$("#content").unbind();
		$("#content").empty();
		$("#content").html(roleView.render().el);
	}

});

App.RoleView = Backbone.View.extend({
	tagName : "div",
	
	id : "roleview",
	
	className : "box",
	
	validator : undefined,
	
	initialize : function() {
		console.log("RoleView:initialize");
		_.bindAll(this, "render", "submit", "cancel", "addFile");
		this.template = _.template(tpl.get('role'));
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
		console.log("RoleView:render");
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
						"id" : $('form select[name=institution]', this.el).val()
					};
					values.rank = {
						"id" : self.model.has("rank") ? self.model.get("rank").id : undefined,
						"name" : $('form textarea[name=rank]', this.el).val()
					};
					values.profileURL = $('form input[name=profileURL]', this.el).val();
					values.position = $('form input[name=position]', this.el).val();
					values.subject = {
						"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
						"name" : $('form textarea[name=subject]', this.el).val()
					};
					values.fek = $('form input[name=fek]', this.el).val();
					values.fekSubject = {
						"id" : self.model.has("fekSubject") ? self.model.get("fekSubject").id : undefined,
						"name" : $('form textarea[name=fekSubject]', this.el).val()
					};
					break;
				case "PROFESSOR_FOREIGN":
					values.institution = $('form input[name=institution]', this.el).val();
					values.profileURL = $('form input[name=profileURL]', this.el).val();
					values.position = $('form input[name=position]', this.el).val();
					values.rank = {
						"id" : self.model.has("rank") ? self.model.get("rank").id : undefined,
						"name" : $('form textarea[name=rank]', this.el).val()
					};
					values.subject = {
						"id" : self.model.has("subject") ? self.model.get("subject").id : undefined,
						"name" : $('form textarea[name=subject]', this.el).val()
					};
					break;
				case "INSTITUTION_MANAGER":
					values.institution = {
						"id" : $('form select[name=institution]', this.el).val()
					};
					break;
				
				case "INSTITUTION_ASSISTANT":
					values.institution = {
						"id" : $('form select[name=institution]', this.el).val()
					};
					break;
				
				case "DEPARTMENT_MANAGER":
					values.department = {};
					values.department.id = $('form select[name=department]', this.el).val();
					break;
				
				case "MINISTRY_MANAGER":
					values.ministry = $('form input[name=ministry]', this.el).val();
					break;
				}
				// Save to model
				self.model.save(values, {
					wait : true,
					success : function(model, resp) {
						console.log("RoleView: save(success):", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("RoleView: save(error):", model, resp);
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
						console.log("RoleView: remove", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : $.i18n.prop("Success")
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("RoleView: remove(error)", model, resp);
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
		console.log("Roleview:addFile - start");
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
		console.log("Roleview:addFile - end");
	},
	
	addFileList : function(type, $el) {
		console.log("Roleview:addFileCollection - start");
		var self = this;
		var files = new App.Files();
		files.url = self.model.url() + "/" + type;
		var fileListView = new App.FileListView({
			collection : files
		});
		$el.html(fileListView.render().el);
		files.fetch();
		console.log("Roleview:addFileCollection - end");
	}
});

App.FileView = Backbone.View.extend({
	tagName : "table",
	
	className : "table table-striped table-bordered table-condensed",
	
	initialize : function() {
		console.log("FileView:initialize", this.model);
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
		console.log("FileView:render");
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
		$("div.modal", self.$el).on("hidden", function() {
			uploader.pluploadQueue().destroy();
			uploader.empty();
			if (self.model.hasChanged()) {
				self.model.change();
			}
		});
		$("div.modal", self.$el).modal('show');
	},
	
	deleteFile : function(event) {
		var self = this;
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				console.log("File:trying to destroy:", self.model, self.model.url());
				self.model.destroy({
					wait : true,
					success : function(model, resp) {
						console.log("File:destroy-success", model, resp);
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
						console.log("File:destroy-error", model, resp, options);
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
		console.log("FileView:initialize", this.collection);
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
					console.log("FileView:uploadFile->FileUploaded");
					var attributes = JSON.parse(response.response);
					self.collection.add(new App.File(attributes), {
						silent : true
					});
				}
			}
		});
		$("div.modal", self.$el).on("hidden", function() {
			console.log("OnHidden");
			uploader.pluploadQueue().destroy();
			uploader.empty();
			if (length !== self.collection.length) {
				console.log("Triggering Change");
				self.collection.trigger("reset");
			}
		});
		$("div.modal", self.$el).modal('show');
	},
	
	deleteFile : function(event) {
		var self = this;
		var selectedModel = self.collection.get($(event.target).attr('fileId'));
		console.log("FileList: deleteFile", $(event.target).attr('fileId'), selectedModel);
		var confirm = new App.ConfirmView({
			title : $.i18n.prop('Confirm'),
			message : $.i18n.prop('AreYouSure'),
			yes : function() {
				console.log("File:trying to destroy:");
				selectedModel.destroy({
					wait : true,
					success : function(model, resp) {
						console.log("File:destroy-success", model, resp, self.collection);
						var popup;
						if (_.isNull(resp)) {
							popup = new App.PopupView({
								type : "success",
								message : $.i18n.prop("Success")
							});
						} else {
							console.log("Setting ", model, selectedModel);
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
						console.log("File:destroy-error", model, resp, options);
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
			console.log("AnnouncementRender: ", role, role.toJSON());
			if (role.get("status") !== "ACTIVE" ) {
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