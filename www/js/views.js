// UserRegistrationView
window.UserRegistrationView = Backbone.View.extend({
	tagName : "div",

	validator : undefined,

	initialize : function() {
		_.bindAll(this, "render", "submit", "resetForm");
		this.template = _.template(tpl.get('user-registration'));
		this.model.bind('change', this.render);
	},

	events : {
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
				var popup = new PopupView({
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
window.LoginView = Backbone.View.extend({
	tagName : "div",

	validator : undefined,

	initialize : function() {
		_.bindAll(this, "render", "login", "resetForm");
		this.template = _.template(tpl.get('login'));
		this.model.bind('change', this.render);
	},

	events : {
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
window.UserVerificationView = Backbone.View.extend({
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

// PopupView
window.PopupView = Backbone.View.extend({
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
window.MenuView = Backbone.View.extend({
	tagName : "ul",

	initialize : function() {
		_.bindAll(this, "render", "logout");
		this.model.bind('change', this.render);
	},

	events : {
		"click a#logout" : "logout"
	},

	render : function(eventName) {
		// Add Logout
		$(this.el).append("<li><a id=\"logout\" href=\"#\">LOGOUT</a>");
		// CREATE MENU BASED ON USER ROLES:
		$(this.el).append("<li><a href=\"\#requests\">REQUESTS</a>");
		$(this.el).append("<li><a href=\"\#profile\">PROFILE</a>");
		$(this.el).append("<li><a href=\"\#\">ΗΟΜΕ</a>");

		return this;
	},

	logout : function(event) {
		// Remove X-Auth-Token
		$.ajaxSetup({
			headers : {}
		});
		console.log("Logging out");
		// Send Redirect
		window.location.href = window.location.pathname;

		event.preventDefault;
		return false;
	}

});

// UserView
window.UserView = Backbone.View.extend({
	tagName : "div",

	validator : undefined,

	initialize : function() {
		_.bindAll(this, "render", "submit", "resetForm");
		this.template = _.template(tpl.get('user'));
		this.model.bind('change', this.render);
	},

	events : {
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
				var popup = new PopupView({
					type : "success",
					message : "The user has been updated"
				});
				popup.show();
			},
			error : function(model, resp, options) {
				console.log("" + resp.status);
				var popup = new PopupView({
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
