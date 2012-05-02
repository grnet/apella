// User
window.User = Backbone.Model.extend({
	url : function() {
		return "/dep/rest/user" + (this.id ? "/" + this.id : "");
	},
	defaults : {
		"id" : undefined,
		"username" : undefined,
		"basicInfo" : {
			"firstname" : undefined,
			"lastname" : undefined,
		},
		"contactInfo" : {
			"address" : {
				"street" : "",
				"number" : "",
				"zip" : "",
				"city" : "",
				"country" : ""
			},
			"email" : "",
			"phoneNumber" : ""
		},
		"roles" : []
	},
	parse : function(resp, xhr) {
		// This is the only place we have access to xhr - response object
		var authToken = xhr.getResponseHeader("X-Auth-Token");
		if (authToken) {
			$.ajaxSetup({
				headers : {
					"X-Auth-Token" : authToken
				}
			});
		}
		return resp;
	}
});

User.prototype.verify = function(options) {
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

User.prototype.login = function(key, value, options) {
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
		if (!this._validate(attrs, options)) {
			return false;
		}
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
		if (!model.set(serverAttrs, options)) {
			return false;
		}
		if (success) {
			success(model, resp);
		} else {
			model.trigger('sync', model, resp, options);
		}
	};
	// Finish configuring and sending the Ajax request.
	options.error = Backbone.wrapError(options.error, model, options);

	var xhr = this.sync.call(this, 'login', this, options);

	if (options.wait) {
		this.set(current, silentOptions);
	}

	return xhr;
};

User.prototype.sync = function(method, model, options) {
	console.log('method = ' + method);
	console.log(model.toJSON());
	switch (method) {

	case "verify":
		// Default options, unless specified.
		options || (options = {});
		// Default JSON-request options.
		var params = {
			type : 'PUT',
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify(model.toJSON())
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

// Role
window.Role = Backbone.Model.extend({
	url : function() {
		return "/dep/rest/role" + (this.id ? "/" + this.id : "");
	},
	defaults : {
		"id" : undefined,
		"discriminator" : undefined,
		"user" : undefined
	}
});

window.Roles = Backbone.Collection.extend({
	model : Role,
	references : {
		// A custom attribute to hold references
		user : undefined
	},
	url : function() {
		return "/dep/rest/role" + (this.references.user ? "?user=" + this.references.user : "");
	}
});