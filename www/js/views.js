window.HeaderView = Backbone.View.extend({

	initialize : function() {
		this.template = _.template(tpl.get('header'));
	},

	render : function(eventName) {
		$(this.el).html(this.template());
		return this;
	}

});

window.UserRegistrationView = Backbone.View.extend({
	tagName : "div",

	initialize : function() {
		this.template = _.template(tpl.get('user-registration'));

		this.model.bind('change', this.render);
		_.bindAll(this, "render", "submit");
	},

	events : {
		"submit form" : "submit",
	},

	render : function(eventName) {
		$(this.el).html(this.template);
		return this;
	},

	submit : function(event) {
		console.log("Clicked submit");

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
			"password" : password,
			success : function(model, resp) {
				console.log("Succesful save");
			},
			error : function() {
				console.log("Error saving");
			}
		});
		
		event.preventDefault();
		return false;
	}
});