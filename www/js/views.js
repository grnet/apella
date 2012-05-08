// UserRegistrationView
App.UserRegistrationView = Backbone.View.extend({
	tagName : "div",

	className : "box",

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
		"blur form" : "resetForm",
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
					maxlength : 12,
				},
				address_street : "required",
				address_number : "required",
				address_zip : "required",
				address_city : "required",
				address_country : "required"
			},
			messages : {
				firstname : "Please enter your firstname",
				lastname : "Please enter your lastname",
				username : {
					required : "Please enter a username",
					minlength : "Your username must consist of at least 2 characters"
				},
				password : {
					required : "Please provide a password",
					minlength : "Your password must be at least 5 characters long"
				},
				confirm_password : {
					required : "Please provide a password",
					minlength : "Your password must be at least 5 characters long",
					equalTo : "Please enter the same password as above"
				},
				phoneNumber : {
					required : "Please enter a phone number",
					number : "Please enter only numbers",
					minlength : "Phone number must be between 10 and 12 characters",
					maxlength : "Phone number must be between 10 and 12 characters"
				},
				address_street : "Please enter a street",
				address_number : "Please enter an address",
				address_zip : "Please enter a zip code",
				address_city : "Please enter a city",
				address_country : "Please enter a country"
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
				"lastname" : lastname,
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
				console.log(model);
				console.log(resp);
				$("#messages", self.$el).html("Η εγγραφή ολοκληρώθηκε, θα σας αποσταλεί e-mail........");
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
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

// LoginView
App.LoginView = Backbone.View.extend({
	tagName : "div",

	className : "box",

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
					required : "Please enter a username",
					minlength : "Your username must consist of at least 2 characters"
				},
				password : {
					required : "Please provide a password",
					minlength : "Your password must be at least 5 characters long"
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
				console.log("Succesful Login");
				console.log(model);
				console.log(resp);
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

	className : "box",

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

// PopupView
App.PopupView = Backbone.View.extend({
	tagName : "div",

	className : "popup",

	initialize : function() {
		this.template = _.template(tpl.get('popup'));
		_.bindAll(this, "render", "show", "close");
	},

	events : {},

	render : function(eventName) {
		$(this.el).html(this.template({
			type : this.options.type,
			message : this.options.message
		}));
		return this;
	},

	show : function() {
		var self = this;
		this.render();
		$('body').append(this.el);
		$('body').bind('click.popup', function(event) {
			self.close();
		});
		$('body').bind('keypress.popup', function(event) {
			self.close();
		});
	},

	close : function() {
		$('body').unbind('click.popup');
		$('body').unbind('keypress.popup');
		$(this.el).remove();
	}
});

// MenuView
App.MenuView = Backbone.View.extend({
	tagName : "ul",

	initialize : function() {
		_.bindAll(this, "render", "logout");
		this.model.bind('change', this.render);
	},

	events : {
		"click a#logout" : "logout"
	},

	render : function(eventName) {
		this.$el.empty();
		// CREATE MENU BASED ON USER ROLES:
		this.$el.append("<li><a href=\"\#\">ΗΟΜΕ</a>");
		this.$el.append("<li><a href=\"\#profile\">PROFILE</a>");
		this.$el.append("<li><a href=\"\#requests\">REQUESTS</a>");
		// Add Logout
		this.$el.append("<li><a id=\"logout\" href=\"#\">LOGOUT</a>");
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

		event.preventDefault;
		return false;
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
		"submit form" : "submit",
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
					maxlength : 12,
				},
				address_street : "required",
				address_number : "required",
				address_zip : "required",
				address_city : "required",
				address_country : "required"
			},
			messages : {
				firstname : "Please enter your firstname",
				lastname : "Please enter your lastname",
				username : {
					required : "Please enter a username",
					minlength : "Your username must consist of at least 2 characters"
				},
				password : {
					minlength : "Your password must be at least 5 characters long"
				},
				confirm_password : {
					minlength : "Your password must be at least 5 characters long",
					equalTo : "Please enter the same password as above"
				},
				phoneNumber : {
					required : "Please enter a phone number",
					number : "Please enter only numbers",
					minlength : "Phone number must be between 10 and 12 characters",
					maxlength : "Phone number must be between 10 and 12 characters"
				},
				address_street : "Please enter a street",
				address_number : "Please enter an address",
				address_zip : "Please enter a zip code",
				address_city : "Please enter a city",
				address_country : "Please enter a country"
			}
		});

		this.view();

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
				"lastname" : lastname,
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
				console.log(model);
				console.log(resp);
				var popup = new App.PopupView({
					type : "success",
					message : "The user has been updated"
				});
				popup.show();
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
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

	id : "rolelist",

	className : "sidebar",

	template : _.template("<ul class=\"list\"></ul><a class=\"button\" id=\"create\" href=\"#\">(+)</a></div>"),

	initialize : function() {
		_.bindAll(this, "render", "add", "newRole");
		this.collection.bind("reset", this.render, this);
		this.collection.bind("add", this.add, this);
	},

	events : {
		"click a#create" : "newRole"
	},

	render : function(eventName) {
		var self = this;
		console.log("RoleListView:render");

		self.$el.html(this.template(this.collection.toJSON()));

		self.collection.each(function(role) {
			self.add(role);
		});
		return this;
	},

	add : function(role) {
		console.log("RoleListView:add");
		console.log(role);
		var roleListItemView = new App.RoleListItemView({
			model : role
		});
		$("ul", this.el).append(roleListItemView.render().el);
	},

	newRole : function(event) {
		var self = this;
		console.log("RoleListView:newRole");
		// Need Discriminator
		var newRole = new App.Role({
			discriminator : "CANDIDATE",
			user : self.options.user
		});
		console.log(newRole);
		this.collection.add(newRole);
		newRole.trigger("select", event);

		event.preventDefault();
		return false;
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
			this.$el.html("<a href='#'>" + this.model.get("discriminator") + "_" + this.model.get("id") + "</a>");
		} else {
			this.$el.html("<a href='#'>" + this.model.get("discriminator") + "*</a>");
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
		$("#roleview", $("#content")).unbind();
		$("#roleview", $("#content")).remove();
		$("#content").append(roleView.render().el);

		event.preventDefault;
		return false;
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
		"submit form" : "submit",
	},

	render : function(eventName) {
		var self = this;
		console.log("RoleView:render");
		// Add file uploaders
		self.$el.html(this.template(this.model.toJSON()));
		if (self.model.get("discriminator") === "CANDIDATE") {
			if (self.model.get("id") !== undefined) {
				self.addFile("cv", $("#cv", this.$el));
				self.addFile("identity", $("#identity", this.$el));
				self.addFile("military1599", $("#military1599", this.$el));
			} else {
				self.$el.prepend("Press Save to activate this profile before uploading");
			}
		}
		return this;
	},

	submit : function(event) {
		var self = this;

		// Read Input
		// var username = $('form input[name=username]', this.el).val();

		// Validate

		// Save to model
		self.model.save({}, {
			success : function(model, resp) {
				console.log(model);
				console.log(resp);
				var popup = new App.PopupView({
					type : "success",
					message : "The role has been updated"
				});
				popup.show();
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
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

	cancel : function(event) {
		if (this.model.get("id") === undefined) {
			this.remove();
			return;
		}
		if (this.validator) {
			this.validator.resetForm();
		}
	},

	remove : function() {
		this.model.destroy({
			success : function() {
				console.log("Role Model destroyed");
			}
		});
		return false;
	},

	close : function() {
		$(this.el).unbind();
		$(this.el).remove();
	},

	addFile : function(type, $el) {
		console.log("Roleview:addFile");
		console.log(type);
		console.log($el);

		var self = this;
		var file;
		var fileAttributes = self.model.get(type) ? self.model.get(type) : {};

		fileAttributes.url = self.model.url() + "/" + type;
		file = new App.File(fileAttributes);

		if (self.model.get(type) !== undefined || self.model.get(type) !== null) {
			file.set(self.model.get(type));
			file.url = self.model.url() + "/" + type;
		}

		var fileView = new App.FileView({
			model : file
		});
		$el.html(fileView.render().el);
	}
});

App.FileView = Backbone.View.extend({
	tagName : "span",

	id : "fileview",

	validator : undefined,

	initialize : function() {
		console.log("FileView:initialize");
		console.log(this.model);

		this.template = _.template(tpl.get('file'));

		_.bindAll(this, "render", "deleteFile", "close");
		this.model.bind('change', this.render, this);
	},

	events : {
		"click a#delete" : "deleteFile"
	},

	render : function(eventName) {
		var self = this;
		console.log(self.$el);
		self.$el.html(self.template(self.model.toJSON()));

		$("input[type=file]", self.$el).fileupload({
			dataType : 'json',
			maxNumberOfFiles: 1,
			url : self.model.url,
			done : function(e, data) {
				$("span#progress", self.$el).empty();
				self.model.set(data.result);
			},
			fail : function(e, data) {
				$("span#progress", self.$el).empty();
			},
			progress : function(e, data) {
				var progress = parseInt(data.loaded / data.total * 100, 10);
				$("span#progress", self.$el).html(progress + "%");
			}
		});
		return this;
	},

	deleteFile : function(event) {
		var self = this;
		self.model.destroy({
			success : function(model, resp) {
				console.log(model);
				console.log(resp);
				var popup = new App.PopupView({
					type : "success",
					message : "The file has been deleted"
				});
				popup.show();
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
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

	close : function(eventName) {
		$("input[type=file]", self.$el).fileupload('destroy');
		$(this.el).unbind();
		$(this.el).remove();
	}
});