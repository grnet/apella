window.UserRegistration = Backbone.Model.extend({
	url: "/dep/rest/user",
	defaults : {
		email : "",
		password : "",
		firstname : "",
		lastname : "",
		roles : []
	}
});
/*Example of how to override sync method
UserRegistration.prototype.sync = function(method, model, options) {
	console.log("Sync called " + method);
	switch (method) {
	case "read":
		break;
	case "create":
		options.success();
		break;
	case "update":
		break;
	case "delete":
		break;
	}
};
*/