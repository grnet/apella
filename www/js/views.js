// MenuView
App.MenuView = Backbone.View.extend({
	el : "div#menu",
	
	tagName : "ul",
	
	className : "nav",
	
	initialize : function() {
		_.bindAll(this, "render", "logout");
		this.model.bind('change', this.render);
	},
	
	events : {},
	
	render : function(eventName) {
		this.$el.empty();
		this.$el.append("<ul class=\"nav\">");
		this.$el.find("ul").append("<li><a href=\"\#\">" + $.i18n.prop('menu_home') + "</a></li>");
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
		this.$el.find("ul").append("<li><a href=\"\#profile\">" + $.i18n.prop('menu_profile') + "</a>");
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
		_.bindAll(this, "render", "login", "resetForm");
		this.template = _.template(tpl.get('login'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "login",
		"blur form" : "resetForm"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
		this.validator = $("form", this.el).validate({
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
			success : function(model, resp) {
				// Notify AppRouter to start Application (fill Header and handle
				// history token)
				console.log("Succesful Login", model, resp);
				self.model.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				var errorCode = resp.getResponseHeader("X-Error-Code");
				if (errorCode === "wrong.username") {
					$("#messages", self.$el).html("Username does not exist");
				} else if (errorCode === "wrong.password") {
					$("#messages", self.$el).html("Password is not valid");
				}
			}
		});
	},
	
	resetForm : function(event) {
		if (this.validator) {
			this.validator.resetForm();
		}
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
		$('body').prepend(self.el);
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

// UserRegistrationView
App.UserRegistrationView = Backbone.View.extend({
	tagName : "div",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "submit", "resetForm");
		this.template = _.template(tpl.get('user-registration'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "submit",
		"blur form" : "resetForm"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
		this.validator = $("form", this.el).validate({
			rules : {
				username : {
					required : true,
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
	
	submit : function(event) {
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
			success : function(model, resp) {
				console.log("UserRegistrationView: save(success):", model, resp);
				$("#messages", self.$el).html("Η εγγραφή ολοκληρώθηκε, θα σας αποσταλεί e-mail........");
			},
			error : function(model, resp, options) {
				console.log("UserRegistrationView: save(error):", resp);
				var popup = new App.PopupView({
					type : "error",
					message : "Error " + resp.status
				});
				popup.show();
			}
		});
		
		event.preventDefault();
		return false;
	},
	
	resetForm : function(event) {
		if (this.validator) {
			this.validator.resetForm();
		}
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

// UserView
App.UserView = Backbone.View.extend({
	tagName : "div",
	
	className : "box",
	
	validator : undefined,
	
	initialize : function() {
		_.bindAll(this, "render", "submit", "edit", "view");
		this.template = _.template(tpl.get('user'));
		this.model.bind('change', this.render);
	},
	
	events : {
		"dblclick form" : "edit",
		"click a#edit" : "edit",
		"click a#cancel" : "view",
		"click a#save" : function() {
			$("form", this.el).submit();
		},
		"submit form" : "submit"
	},
	
	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		
		this.validator = $("form", this.el).validate({
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
		
		this.view();
		
		return this;
	},
	
	submit : function(event) {
		var self = this;
		var confirm = new App.ConfirmView({
			title : "Title",
			message : "Are you sure?",
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
					success : function(model, resp) {
						console.log("UserView:save(success):", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : "The user has been updated"
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("UserView:save(error):", model, resp);
						var popup = new App.PopupView({
							type : "error",
							message : "Error " + resp.status
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
	
	edit : function(event) {
		$("form span", this.el).hide();
		$("form a#edit", this.el).hide();
		$("form input", this.el).show();
		$("form a#save", this.el).show();
		$("form a#cancel", this.el).show();
	},
	
	view : function(event) {
		if (this.validator) {
			this.validator.resetForm();
		}
		$("form a#save", this.el).hide();
		$("form a#cancel", this.el).hide();
		$("form input", this.el).hide();
		$("form a#edit", this.el).show();
		$("form span", this.el).show();
		
	}
});

// RoleView
App.RoleListView = Backbone.View.extend({
	tagName : "div",
	
	className : "well",
	
	template : _.template("<ul class=\"nav nav-list\"></ul><select name=\"newRole\" id=\"newRole\"></select><a class=\"btn\" id=\"create\" href=\"javascript:void(0)\">(+)</a>"),
	
	initialize : function() {
		_.bindAll(this, "render", "add", "newRole");
		this.collection.bind("reset", this.render, this);
		this.collection.bind("add", this.add, this);
	},
	
	events : {
		"click a#create" : "newRole"
	},
	
	render : function(eventName) {
		console.log("RoleListView:render");
		var self = this;
		self.$el.html(this.template(this.collection.toJSON()));
		// Add options in select for adding roles:
		_.each(_.filter(App.allowedRoles, function(discriminator) {
			return true; // Do filtering here
		}), function(discriminator) {
			$("select[name='newRole']", self.$el).append("<option value='" + discriminator + "'>" + $.i18n.prop(discriminator) + "</option>");
		});
		// Add existing roles
		self.collection.each(function(role) {
			self.add(role);
		});
		return this;
	},
	
	add : function(role) {
		console.log("RoleListView:add", role);
		var roleListItemView = new App.RoleListItemView({
			model : role
		});
		$("ul", this.el).append(roleListItemView.render().el);
	},
	
	newRole : function(event) {
		console.log("RoleListView:newRole");
		var self = this;
		var discriminator = $("select[name='newRole']", self.$el).val();
		var newRole = new App.Role({
			"discriminator" : discriminator,
			user : self.options.user
		});
		
		console.log(newRole);
		self.collection.add(newRole);
		newRole.trigger("select", event);
	}

});

App.RoleListItemView = Backbone.View.extend({
	tagName : "li",
	
	id : "roleitem",
	
	events : {
		"click a" : "select"
	},
	
	initialize : function() {
		_.bindAll(this, "render", "close", "select");
		this.model.bind("change", this.render, this);
		this.model.bind("destroy", this.close, this);
		this.model.bind("select", this.select, this);
	},
	
	render : function(eventName) {
		if (this.model.get("id")) {
			this.$el.html("<a href='javascript:void(0)'>" + $.i18n.prop(this.model.get("discriminator")) + "_" + this.model.get("id") + "</a>");
		} else {
			this.$el.html("<a href='javascript:void(0)'>" + $.i18n.prop(this.model.get("discriminator")) + "*</a>");
		}
		return this;
	},
	
	close : function() {
		$(this.el).unbind();
		$(this.el).remove();
	},
	
	select : function(event) {
		var self = this;
		var roleView = new App.RoleView({
			model : self.model
		});
		$("#content #roleInfo").unbind();
		$("#content #roleInfo").empty();
		$("#content #roleInfo").html(roleView.render().el);
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
				$("#cv", self.$el).html("Press Save to enable uploading");
				$("#identity", self.$el).html("Press Save to enable uploading");
				$("#military1599", self.$el).html("Press Save to enable uploading");
				$("#publications", self.$el).html("Press Save to enable uploading");
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
						message : "Error " + resp.status
					});
					popup.show();
				}
			});
			if (self.model.has("id")) {
				self.addFile("fekFile", $("#fekFile", this.$el));
			} else {
				$("#fekFile", self.$el).html("Press Save to enable uploading");
			}
			
			this.validator = $("form", this.el).validate({
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
						message : "Error " + resp.status
					});
					popup.show();
				}
			});
			this.validator = $("form", this.el).validate({
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
						message : "Error " + resp.status
					});
					popup.show();
				}
			});
			this.validator = $("form", this.el).validate({
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
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "' selected>" + department.get("fullName") + "</option>");
						} else {
							$("select[name='department']", self.$el).append("<option value='" + department.get("id") + "'>" + department.get("fullName") + "</option>");
						}
					});
				},
				error : function(model, resp, options) {
					var popup = new App.PopupView({
						type : "error",
						message : "Error " + resp.status
					});
					popup.show();
				}
			});
			this.validator = $("form", this.el).validate({
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
			title : "Title",
			message : "Are you sure?",
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
					success : function(model, resp) {
						console.log("RoleView: save(success):", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : "The role has been updated"
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("RoleView: save(error):", model, resp);
						var popup = new App.PopupView({
							type : "error",
							message : "Error " + resp.status
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
		console.log("RoleView: Cancel");
		var self = this;
		if (self.model.get("id") === undefined) {
			self.remove();
			return;
		} else {
			if (self.validator) {
				self.validator.resetForm();
			}
			self.render();
		}
	},
	
	remove : function() {
		var self = this;
		var confirm = new App.ConfirmView({
			title : "Title",
			message : "Are you sure?",
			yes : function() {
				self.model.destroy({
					success : function(model, resp) {
						console.log("RoleView: remove", model, resp);
						var popup = new App.PopupView({
							type : "success",
							message : "The role has been removed"
						});
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("RoleView: remove(error)", model, resp);
						var popup = new App.PopupView({
							type : "error",
							message : "Error " + resp.status
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
		this.template = _.template(tpl.get('file'));
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
			title : "Title",
			message : "Are you sure?",
			yes : function() {
				console.log("File:trying to destroy:", self.model, self.model.url());
				self.model.destroy({
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
								message : "The file has been deleted"
							});
						} else {
							self.model.set(resp);
							popup = new App.PopupView({
								type : "warning",
								message : "The file has been reverted to earlier edition"
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("File:destroy-error", model, resp, options);
						var popup = new App.PopupView({
							type : "error",
							message : "Error " + resp.status
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
		this.template = _.template(tpl.get('file'));
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
			title : "Title",
			message : "Are you sure?",
			yes : function() {
				console.log("File:trying to destroy:");
				selectedModel.destroy({
					wait : true,
					silent : true,
					success : function(model, resp) {
						console.log("File:destroy-success", model, resp);
						var popup;
						if (_.isNull(resp)) {
							self.collection.remove(selectedModel);
							popup = new App.PopupView({
								type : "success",
								message : "The file has been deleted"
							});
						} else {
							selectedModel.set(resp);
							popup = new App.PopupView({
								type : "warning",
								message : "The file has been reverted to earlier edition"
							});
						}
						popup.show();
					},
					error : function(model, resp, options) {
						console.log("File:destroy-error", model, resp, options);
						var popup = new App.PopupView({
							type : "error",
							message : "Error " + resp.status
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