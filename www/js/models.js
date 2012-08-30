// User
App.User = Backbone.Model.extend({
	urlRoot : "/dep/rest/user",
	defaults : {
		"id" : undefined,
		"username" : undefined,
		"basicInfo" : {
			"firstname" : undefined,
			"lastname" : undefined,
			"fathername" : undefined
		},
		"basicInfoLatin" : {
			"firstname" : undefined,
			"lastname" : undefined,
			"fathername" : undefined
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
			"mobile" : ""
		},
		"roles" : []
	},
	parse : function(resp, xhr) {
		// This is the only place we have access to xhr - response object
		if (xhr) {
			var authToken = xhr.getResponseHeader("X-Auth-Token");
			if (authToken) {
				$.ajaxSetup({
					headers : {
						"X-Auth-Token" : authToken
					}
				});
				App.authToken = authToken;
			}
		}
		return resp;
	},
	hasRole : function(role) {
		var self = this;
		if (self.has("roles")) {
			return _.any(self.get("roles"), function(r) {
				return r.discriminator === role;
			});
		}
		return false;
	},
	
	hasRoleWithStatus : function(role, status) {
		var self = this;
		if (self.has("roles")) {
			return _.any(self.get("roles"), function(r) {
				return (r.discriminator === role && r.status === status);
			});
		}
		return false;
	},
	
	isAssociatedWithDepartment : function(department) {
		var self = this;
		return _.any(self.get("roles"), function(r) {
			if (r.discriminator === "INSTITUTION_MANAGER" && r.institution.id === department.get("institution").id) {
				return true;
			} else if (r.discriminator === "INSTITUTION_ASSISTANT" && r.institution.id === department.get("institution").id) {
				return true;
			} else if (r.discriminator === "DEPARTMENT_MANAGER" && r.department.id === department.get("id")) {
				return true;
			} else {
				return false;
			}
		});
	},
	
	getAssociatedInstitutions : function() {
		var self = this;
		return _.reduce(self.get("roles"), function(memo, r) {
			if (r.discriminator === "INSTITUTION_MANAGER") {
				memo.push(r.institution);
			} else if (r.discriminator === "INSTITUTION_ASSISTANT") {
				memo.push(r.institution);
			} else if (r.discriminator === "DEPARTMENT_MANAGER") {
				memo.push(r.department.institution);
			}
			return memo;
		}, []);
	}
});

App.User.prototype.verify = function(options) {
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

App.User.prototype.login = function(key, value, options) {
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

App.User.prototype.status = function(key, value, options) {
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
	
	var xhr = this.sync.call(this, 'status', this, options);
	
	if (options.wait) {
		this.set(current, silentOptions);
	}
	
	return xhr;
};

App.User.prototype.sync = function(method, model, options) {
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
			data : {
				"username" : model.get("username"),
				"password" : model.get("password")
			}
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
		
	case "status":
		// Default options, unless specified.
		options || (options = {});
		// Default JSON-request options.
		var params = {
			type : 'PUT',
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify({
				"id" : model.get("id"),
				"status" : model.get("status")
			})
		};
		// Ensure that we have a URL.
		if (!options.url) {
			if (model.url) {
				params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/status";
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

App.Users = Backbone.Collection.extend({
	model : App.User,
	url : "/dep/rest/user"
});

// Role
App.Role = Backbone.Model.extend({
	urlRoot : "/dep/rest/role",
	defaults : {
		// Common Fields
		"id" : undefined,
		"discriminator" : undefined,
		"status" : undefined,
		"statusDate" : undefined,
		"user" : undefined,
		// Specific Fields
		"institution" : undefined,
		"department" : undefined,
		"profileURL" : undefined,
		"rank" : undefined,
		"subject" : undefined,
		"fek" : undefined,
		"fekSubject" : undefined,
		"manager" : undefined,
		"ministry" : undefined,
		// Files:
		"fekFile" : undefined,
		"cv" : undefined,
		"identity" : undefined,
		"military1599" : undefined,
		"publications" : undefined
	}
});

App.Role.prototype.status = function(key, value, options) {
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
	
	var xhr = this.sync.call(this, 'status', this, options);
	
	if (options.wait) {
		this.set(current, silentOptions);
	}
	
	return xhr;
};

App.Role.prototype.sync = function(method, model, options) {
	switch (method) {
	case "status":
		// Default options, unless specified.
		options || (options = {});
		// Default JSON-request options.
		var params = {
			type : 'PUT',
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify({
				"id" : model.get("id"),
				"discriminator" : model.get("discriminator"),
				"status" : model.get("status")
			})
		};
		// Ensure that we have a URL.
		if (!options.url) {
			if (model.url) {
				params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/status";
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

App.Roles = Backbone.Collection.extend({
	model : App.Role,
	user : undefined,
	url : function() {
		return "/dep/rest/role" + (this.user ? "?user=" + this.user : "");
	},
	comparator : function(role) {
		return _.indexOf(App.allowedRoles, role.get('discriminator'), false);
	}
});

// File
App.File = Backbone.Model.extend({
	defaults : {
		"id" : undefined,
		"name" : undefined,
		"description" : undefined,
		"currentBody" : {
			"id" : undefined,
			"mimeType" : undefined,
			"originalFilename" : undefined,
			"storedFilePath" : undefined,
			"fileSize" : undefined,
			"date" : undefined
		}
	}
});

App.Files = Backbone.Collection.extend({
	name : undefined,
	model : App.File
});

App.Institution = Backbone.Model.extend({
	urlRoot : "/dep/rest/institution",
	defaults : {
		"id" : undefined,
		"name" : undefined
	}
});

App.Institutions = Backbone.Collection.extend({
	url : "/dep/rest/institution",
	model : App.Institution,
	comparator : function(institution) {
		return institution.get('name');
	}
});

App.Department = Backbone.Model.extend({
	urlRoot : "/dep/rest/department",
	defaults : {
		"id" : undefined,
		"department" : undefined,
		"school" : undefined,
		"fullName" : undefined,
		"institution" : undefined
	}
});

App.Departments = Backbone.Collection.extend({
	url : "/dep/rest/department",
	model : App.Department,
	comparator : function(department) {
		if (_.isObject(department.get("institution"))) {
			return department.get('institution').name + department.get('department');
		} else {
			return "_" + department.get('department');
		}
	}
});

App.Rank = Backbone.Model.extend({
	urlRoot : "/dep/rest/rank",
	defaults : {
		"id" : undefined,
		"name" : undefined
	}
});

App.Ranks = Backbone.Collection.extend({
	url : "/dep/rest/rank",
	model : App.Rank
});

App.Position = Backbone.Model.extend({
	urlRoot : "/dep/rest/position",
	defaults : {
		id : undefined,
		name : undefined,
		description : undefined,
		department : undefined,
		subject : undefined,
		status : undefined,
		deanStatus : undefined,
		fek : undefined,
		fekSentDate : undefined,
		// files
		fekFile : undefined,
		prosklisiKosmitora : undefined,
		recommendatoryReport : undefined,
		recommendatoryReportSecond : undefined
	}
});

App.Positions = Backbone.Collection.extend({
	url : "/dep/rest/position",
	model : App.Position
});

App.Register = Backbone.Model.extend({
	urlRoot : "/dep/rest/register",
	defaults : {
		id : undefined,
		department : undefined,
		registerFile : undefined
	}
});

App.Registries = Backbone.Collection.extend({
	url : "/dep/rest/register",
	model : App.Register
});

App.PositionCommitteeMember = Backbone.Model.extend({
	urlRoot : "/dep/rest/position/committee",
	defaults : {
		id : undefined,
		position : undefined,
		professor : undefined,
		confirmedMembership : undefined,
		// Files
		recommendatoryReport : undefined
	}
});

App.PositionCommittee = Backbone.Collection.extend({
	model : App.PositionCommitteeMember,
	initialize : function(models, options) {
		this.position = options.position;
	},
	url : function() {
		return "/dep/rest/position/committee" + (this.position ? "?position=" + this.position : "");
	}
});