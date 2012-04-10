window.UserRegistration = Backbone.Model.extend({
	url : "/dep/user",
	defaults : {
		email : "",
		firstname : "",
		lastname : "",
		password : "",
		roles : [],
		verificationNumber : "",
		verified : false
	}
});
/* Example of how to override sync method */
UserRegistration.prototype.verify = function(options) {
	options = options ? _.clone(options) : {};
	var model = this;
	var success = options.success;
	options.success = function(resp, status, xhr) {
		if (!model.set(model.parse(resp, xhr), options))
			return false;
		if (success)
			success(model, resp);
	};
	options.error = Backbone.wrapError(options.error, model, options);
	return (this.sync || Backbone.sync).call(this, 'verify', this, options);
};

UserRegistration.prototype.sync = function(method, model, options) {
	console.log('method = ' + method);
	console.log('model = ' + model.toJSON());
	switch (method) {
	case "verify":
		// Default options, unless specified.
		options || (options = {});
		// Default JSON-request options.
		var params = {
			type : 'GET',
			dataType : 'json'
		};

		// Ensure that we have a URL.
		if (!options.url) {
			if (model.url) {
				params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/verify/" + model.get("verificationNumber");
			} else {
				urlError();
			}
		}
		// Make the request, allowing the user to override any Ajax options.
		return $.ajax(_.extend(params, options));
	default:
		return (Backbone.sync).call(this, method, this, options);
	}
};

window.User = Backbone.Model.extend({
	url : "/dep/user",
	defaults : {
		email : "",
		firstname : "",
		lastname : "",
		roles : []
	}
});