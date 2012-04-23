window.UserRegistrationView = Backbone.View.extend({
	tagName : "div",

	initialize : function() {
		_.bindAll(this, "render", "submit");
		this.template = _.template(tpl.get('user-registration'));
		this.model.bind('change', this.render);
	},

	events : {
		"submit form" : "submit",
	},

	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	},

	submit : function(event) {
		var email = $('form input[name=email]', this.el).val();
		var firstname = $('form input[name=firstname]', this.el).val();
		var lastname = $('form input[name=lastname]', this.el).val();
		var password = $('form input[name=password]', this.el).val();

		// Validate
	
		// Save to model
		this.model.save({
			"email" : email,
			"firstname" : firstname,
			"lastname" : lastname,
			"password" : password
		}, {
			success : function(model, resp) {
				console.log(model);
				console.log(resp);
				$(this.el, "#messages").html("Η εγγραφή ολοκληρώθηκε, θα σας αποσταλεί e-mail........");
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
	}
});

window.LoginView = Backbone.View.extend({
	tagName : "div",

	initialize : function() {
		_.bindAll(this, "render", "login");
		this.template = _.template(tpl.get('login'));
		this.model.bind('change', this.render);
	},

	events : {
		"submit form" : "login",
	},

	render : function(eventName) {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	},

	login : function(event) {
		var self = this;
		var email = $('form input[name=email]', self.el).val();
		var password = $('form input[name=password]', self.el).val();

		// Save to model
		self.model.login({
			"email" : email,
			"password" : password
		}, {
			success : function(model, resp) {
				// Notify AppRouter to start Application (fill Header and handle history token)
				self.model.trigger("user:loggedon");
			},
			error : function(model, resp, options) {
				$(self.el, "#messages").html("Σφάλμα εισόδου");
			}
		});

		event.preventDefault();
		return false;
	}
});

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