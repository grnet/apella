define([ "jquery", "underscore", "backbone", "application" ], function($, _, Backbone, App) {

	var Models = {};

	// User
	Models.User = Backbone.Model.extend({
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
		getRole : function(role) {
			var self = this;
			if (self.has("roles")) {
				return _.find(self.get("roles"), function(r) {
					return r.discriminator === role;
				});
			}
			return undefined;
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
			var institutionId;
			if (department instanceof Backbone.Model) {
				institutionId = department.get("institution").id;
			} else {
				institutionId = department.institution.id;
			}
			return _.any(self.get("roles"), function(r) {
				if (r.discriminator === "INSTITUTION_MANAGER" && r.institution.id === institutionId) {
					return true;
				} else if (r.discriminator === "INSTITUTION_ASSISTANT" && r.institution.id === institutionId) {
					return true;
				} else {
					return false;
				}
			});
		},

		isAssociatedWithInstitution : function(institution) {
			var self = this;
			var institutionId;
			if (institution instanceof Backbone.Model) {
				institutionId = institution.get("id");
			} else {
				institutionId = institution.id;
			}
			return _.any(self.get("roles"), function(r) {
				if (r.discriminator === "INSTITUTION_MANAGER" && r.institution.id === institutionId) {
					return true;
				} else if (r.discriminator === "INSTITUTION_ASSISTANT" && r.institution.id === institutionId) {
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
				}
				return memo;
			}, []);
		}
	});

	Models.User.prototype.verify = function(options) {
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

	Models.User.prototype.login = function(key, value, options) {
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

	Models.User.prototype.status = function(key, value, options) {
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

	Models.User.prototype.resetPassword = function(key, value, options) {
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

		var xhr = this.sync.call(this, 'resetPassword', this, options);

		if (options.wait) {
			this.set(current, silentOptions);
		}

		return xhr;
	};

	Models.User.prototype.resendVerificationEmail = function(key, value, options) {
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

		var xhr = this.sync.call(this, 'resendVerificationEmail', this, options);

		if (options.wait) {
			this.set(current, silentOptions);
		}

		return xhr;
	};

	Models.User.prototype.sync = function(method, model, options) {
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

		case "resetPassword":
			// Default options, unless specified.
			options || (options = {});
			// Default JSON-request options.
			var params = {
				type : 'PUT',
				dataType : 'json',
				data : {
					"username" : model.get("username")
				}
			};
			// Ensure that we have a URL.
			if (!options.url) {
				if (model.url) {
					params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/resetPassword";
				} else {
					urlError();
				}
			}
			// Make the request, allowing the user to override any Ajax options.
			return $.ajax(_.extend(params, options));

		case "resendVerificationEmail":
			// Default options, unless specified.
			options || (options = {});
			// Default JSON-request options.
			var params = {
				type : 'PUT',
				dataType : 'json',
				data : {
					"username" : model.get("username")
				}
			};
			// Ensure that we have a URL.
			if (!options.url) {
				if (model.url) {
					params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/sendVerificationEmail";
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

	Models.Users = Backbone.Collection.extend({
		model : Models.User,
		url : "/dep/rest/user"
	});

	// Role
	Models.Role = Backbone.Model.extend({
		urlRoot : "/dep/rest/role",
		defaults : {
			// Common Fields
			"id" : undefined,
			"discriminator" : undefined,
			"status" : undefined,
			"statusDate" : undefined,
			"user" : undefined,
			// Specific Fields
			"identification" : undefined,
			"verificationAuthority" : undefined,
			"verificationAuthorityName" : undefined,
			"phone" : undefined,
			"institution" : undefined,
			"department" : undefined,
			"profileURL" : undefined,
			"rank" : undefined,
			"subject" : undefined,
			"fek" : undefined,
			"fekSubject" : undefined,
			"manager" : undefined,
			"ministry" : undefined
		},
		isPrimary : function() {
			var self = this;
			if (self.has("user")) {
				return _.isEqual(self.get("discriminator"), self.get("user").primaryRole);
			} else {
				return false;
			}
		}
	});

	Models.Role.prototype.status = function(key, value, options) {
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

	Models.Role.prototype.sync = function(method, model, options) {
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

	Models.Roles = Backbone.Collection.extend({
		model : Models.Role,
		user : undefined,
		url : function() {
			return "/dep/rest/role" + (this.user ? "?user=" + this.user : "");
		},
		comparator : function(role) {
			return _.indexOf(App.allowedRoles, role.get('discriminator'), false);
		}
	});

	Models.Professors = Models.Roles.extend({
		url : "/dep/rest/professor"
	});

	// File
	Models.File = Backbone.Model.extend({
		defaults : {
			"id" : undefined,
			"type" : undefined,
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

	Models.Files = Backbone.Collection.extend({
		model : Models.File,
		type : undefined
	});

	Models.Institution = Backbone.Model.extend({
		urlRoot : "/dep/rest/institution",
		defaults : {
			"id" : undefined,
			"name" : undefined
		}
	});

	Models.Institutions = Backbone.Collection.extend({
		url : "/dep/rest/institution",
		model : Models.Institution,
		comparator : function(institution) {
			return institution.get('name');
		}
	});

	Models.Department = Backbone.Model.extend({
		urlRoot : "/dep/rest/department",
		defaults : {
			"id" : undefined,
			"department" : undefined,
			"school" : undefined,
			"fullName" : undefined,
			"institution" : undefined
		}
	});

	Models.Departments = Backbone.Collection.extend({
		url : "/dep/rest/department",
		model : Models.Department,
		comparator : function(department) {
			if (_.isObject(department.get("institution"))) {
				return department.get('institution').name + department.get('department');
			} else {
				return "_" + department.get('department');
			}
		}
	});

	Models.Rank = Backbone.Model.extend({
		urlRoot : "/dep/rest/rank",
		defaults : {
			"id" : undefined,
			"name" : undefined
		}
	});

	Models.Ranks = Backbone.Collection.extend({
		url : "/dep/rest/rank",
		model : Models.Rank
	});

	Models.Position = Backbone.Model.extend({
		urlRoot : "/dep/rest/position",
		defaults : {
			id : undefined,
			permanent : undefined,
			name : undefined,
			description : undefined,
			department : undefined,
			subject : undefined,
			status : undefined,
			fek : undefined,
			fekSentDate : undefined,
			openingDate : undefined,
			closingDate : undefined,
			committeeMeetingDate : undefined,
			nominationCommitteeConvergenceDate : undefined,
			nominationToETDate : undefined,
			nominationFEK : undefined
		}
	});

	Models.Positions = Backbone.Collection.extend({
		url : "/dep/rest/position",
		model : Models.Position
	});

	Models.Register = Backbone.Model.extend({
		urlRoot : "/dep/rest/register",
		defaults : {
			id : undefined,
			permanent : undefined,
			title : undefined,
			institution : undefined
		}
	});

	Models.Registries = Backbone.Collection.extend({
		url : "/dep/rest/register",
		model : Models.Register
	});

	Models.PositionCommitteeMember = Backbone.Model.extend({
		position : undefined,
		urlRoot : function() {
			return "/dep/rest/position/" + this.attributes.position.id + "/committee";
		},
		defaults : {
			id : undefined,
			position : undefined,
			professor : undefined,
			confirmedMembership : undefined,
		}
	});

	Models.PositionCommittee = Backbone.Collection.extend({
		position : undefined,
		model : Models.PositionCommitteeMember,
		initialize : function(models, options) {
			this.position = options.position;
		},
		url : function() {
			return "/dep/rest/position/" + this.position + "/committee";
		}
	});

	Models.ProfessorCommittees = Backbone.Collection.extend({
		professor : undefined,
		model : Models.PositionCommitteeMember,
		initialize : function(models, options) {
			this.professor = options.professor;
		},
		url : function() {
			return "/dep/rest/professor/" + this.professor + "/committees";
		}
	});

	Models.Candidacy = Backbone.Model.extend({
		urlRoot : "/dep/rest/candidacy",
		defaults : {
			id : undefined,
			permanent : undefined,
			date : undefined,
			position : {
				id : undefined,
				permanent : undefined,
				name : undefined,
				description : undefined,
				department : undefined,
				subject : undefined,
				status : undefined,
				fek : undefined,
				fekSentDate : undefined,
				openingDate : undefined,
				closingDate : undefined
			},
			candidate : {
				"id" : undefined
			},
			snapshot : {}
		}
	});

	Models.CandidateCandidacies = Backbone.Collection.extend({
		candidate : undefined,
		model : Models.Candidacy,
		initialize : function(models, options) {
			this.candidate = options.candidate;
		},
		url : function() {
			return "/dep/rest/candidate/" + this.candidate + "/candidacies";
		}
	});

	Models.PositionCandidacies = Backbone.Collection.extend({
		position : undefined,
		model : Models.Candidacy,
		initialize : function(models, options) {
			this.position = options.position;
		},
		url : function() {
			return "/dep/rest/position/" + this.position + "/candidacies";
		}
	});

	return Models;

});