var AppRouter = Backbone.Router.extend({

    data : {
        loggedOnUser : new User()
    },

	initialize : function() {
		var self = this;
        
		_.extend(self, Backbone.Events);
		_.bindAll(self, "showLoginView", "showHomeView", "start");

		//Get LoggedOnUser
		
		self.data.loggedOnUser.fetch({
			success : function(model, resp) {
                console.log("Fetch User success");
				self.start();
			},
			error : function(model, resp, options) {
                console.log("Fetch User Error");
				self.showLoginView();
			}
		});
	},

	routes : {
		"" : "showHomeView",
	},

	start : function() {
		console.log("Start called");
        self.data.loggedOnUser.off("user:loggedon", self.start);
		// Create Header, Menu, and other side content and bind them to the same model

		// Start Routing
		Backbone.history.start();
	},

	showLoginView : function() {
		var self = this;
        
        self.data.loggedOnUser.on("user:loggedon", self.start);
        
		var loginView = new LoginView({
			model : self.data.loggedOnUser
		});

		$("#content").html(loginView.render().el);

		this.currentView = loginView;
		return loginView;
	},

	showHomeView : function() {
        console.log("showHomeView");
		$("#content").html("<h1>Home</h1>");
	}
});

$(document).ready(function() {
	tpl.loadTemplates([ "login", "popup" ], function() {
		app = new AppRouter();
	});
});
