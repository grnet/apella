window.HeaderView = Backbone.View.extend({

	initialize : function() {
		this.template = _.template(tpl.get('header'));
	},

	render : function(eventName) {
		$(this.el).html(this.template());
		return this;
	}

});

window.LoginView = Backbone.View.extend({
	tagName : "div",

	className : "login",

	initialize : function() {
		this.template = _.template(tpl.get('login'));
		_.bindAll(this, "render", "submit");
	},

	events : {
		"submit" : "submit",
		"click a#goToRegisterLink" : "goToRegister"
	},

	render : function(eventName) {
		$(this.el).html(this.template());
		return this;
	},

	submit : function(event) {
		var username = $('form input[name=username]', this.el).val();
		var password = $('form input[name=password]', this.el).val();
		// Call function from Controller
		console.log("Submit");
		return false;
	},

	goToRegister : function(event) {
		// Call function from Controller
		console.log("Register");
		return false;
	}
});