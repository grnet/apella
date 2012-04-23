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

UserRegistration.prototype.verify = function(options) {
	options = options ? _.clone(options) : {};
	var model = this;
	var success = options.success;
	options.success = function(resp, status, xhr) {
		if (!model.set(model.parse(resp, xhr), options)) {
			return false;
		}
		if (success) {
			success(model, resp);
		}
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
			type : 'PUT',
			dataType : 'json',
			data : model.toJSON()
		};

		// Ensure that we have a URL.
		if (!options.url) {
			if (model.url) {
				params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/verify";
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
		id : "",
		email : "",
		firstname : "",
		lastname : "",
		roles : []
	}
});

User.prototype.login = function(key, value, options) {
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

	var attrs, current;
	// Handle both `("key", value)` and `({key: value})` -style calls.
	if (_.isObject(key) || key == null) {
		attrs = key;
		options = value;
	} else {
		attrs = {};
		attrs[key] = value;
	}
	options = options ? _.clone(options) : {};

	// If we're "wait"-ing to set changed attributes, validate early.
	if (options.wait) {
		if (!this._validate(attrs, options))
			return false;
		current = _.clone(this.attributes);
	}

	// Regular saves `set` attributes before persisting to the server.
	var silentOptions = _.extend({}, options, {
		silent : true
	});
	if (attrs && !this.set(attrs, options.wait ? silentOptions : options)) {
		return false;
	}
	// After a successful server-side save, the client is (optionally)
	// updated with the server-side state.
	var model = this;
	var success = options.success;
	options.success = function(resp, status, xhr) {
		var serverAttrs = model.parse(resp, xhr);
		if (options.wait) {
			delete options.wait;
			serverAttrs = _.extend(attrs || {}, serverAttrs);
		}
		if (!model.set(serverAttrs, options))
			return false;
		if (success) {
			success(model, resp);
		} else {
			model.trigger('sync', model, resp, options);
		}
	};
	// Finish configuring and sending the Ajax request.
	options.error = Backbone.wrapError(options.error, model, options);

	var xhr = this.sync.call(this, 'login', this, options);

	if (options.wait)
		this.set(current, silentOptions);

	return xhr;
};

User.prototype.sync = function(method, model, options) {
	console.log('method = ' + method);
	console.log(model.toJSON());
	switch (method) {
	case "login":
		// Default options, unless specified.
		options || (options = {});
		// Default JSON-request options.
		var params = {
			type : 'PUT',
			dataType : 'json',
			data : model.toJSON()
		};

		// Ensure that we have a URL.
		if (!options.url) {
			if (model.url) {
				params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/login";
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