/*global define */
define([ "jquery", "underscore", "backbone", "application" ], function ($, _, Backbone, App) {
	"use strict";

	var Models = {};

	// User
	Models.User = Backbone.Model.extend({
		urlRoot: "/dep/rest/user",
		defaults: {
			"id": undefined,
			"username": undefined,
			"identification": undefined,
			"registrationType" : undefined,
			"missingRequiredFields": undefined,
			"basicInfo": {
				"firstname": undefined,
				"lastname": undefined,
				"fathername": undefined
			},
			"basicInfoLatin": {
				"firstname": undefined,
				"lastname": undefined,
				"fathername": undefined
			},
			"contactInfo": {
				"email": "",
				"mobile": "",
				"phone": ""
			},
			"shibbolethInfo" : {},
			"roles": []
		},

		parse: function (resp, options) {
			// This is the only place we have access to xhr - response object
			if (options && options.xhr) {
				var authToken = options.xhr.getResponseHeader("X-Auth-Token");
				if (authToken) {
					$.ajaxSetup({
						headers: {
							"X-Auth-Token": authToken
						}
					});
					App.authToken = authToken;
				}
			}
			return resp;
		},

		isShibbolethRegistrationIncomplete: function() {
			var self = this;
			return _.isEqual(self.get("registrationType"), "SHIBBOLETH") && self.get("missingRequiredFields");
		},

		getDisplayName: function() {
			var self = this;
			var displayName = ""
				.concat(self.get("basicInfo").firstname || "-")
				.concat(" ")
				.concat(self.get("basicInfo").lastname || "-")
				.concat(" [" + self.get("id") + "]");

			return displayName;
		},

		getRole: function (role) {
			var self = this;
			if (self.has("roles")) {
				return _.find(self.get("roles"), function (r) {
					return r.discriminator === role;
				});
			}
			return undefined;
		},

		hasRole: function (role) {
			var self = this;
			if (self.has("roles")) {
				return _.any(self.get("roles"), function (r) {
					return r.discriminator === role;
				});
			}
			return false;
		},

		hasRoleWithStatus: function (role, status) {
			var self = this;
			if (self.has("roles")) {
				return _.any(self.get("roles"), function (r) {
					return (r.discriminator === role && r.status === status);
				});
			}
			return false;
		},

		isAssociatedWithDepartment: function (department) {
			var self = this;
			var institutionId;
			if (department instanceof Backbone.Model) {
				institutionId = department.get("institution").id;
			} else {
				institutionId = department.institution.id;
			}
			return _.any(self.get("roles"), function (r) {
				if (r.discriminator === "INSTITUTION_MANAGER" && r.institution.id === institutionId) {
					return true;
				}
				if (r.discriminator === "INSTITUTION_ASSISTANT" && r.institution.id === institutionId) {
					return true;
				}
				return false;
			});
		},

		isAssociatedWithInstitution: function (institution) {
			var self = this;
			var institutionId;
			if (institution instanceof Backbone.Model) {
				institutionId = institution.get("id");
			} else {
				institutionId = institution.id;
			}
			return _.any(self.get("roles"), function (r) {
				if (r.discriminator === "INSTITUTION_MANAGER" && r.institution.id === institutionId) {
					return true;
				}
				if (r.discriminator === "INSTITUTION_ASSISTANT" && r.institution.id === institutionId) {
					return true;
				}
				return false;
			});
		},

		getAssociatedInstitutions: function () {
			var self = this;
			return _.reduce(self.get("roles"), function (memo, r) {
				if (r.discriminator === "INSTITUTION_MANAGER") {
					memo.push(r.institution);
				} else if (r.discriminator === "INSTITUTION_ASSISTANT") {
					memo.push(r.institution);
				}
				return memo;
			}, []);
		},

		// Sync Methods
		verify: function (options) {
			options = options ? _.clone(options) : {};
			if (options.parse === void 0) {
				options.parse = true;
			}
			var success = options.success;
			options.success = function (model, resp, options) {
				if (!model.set(model.parse(resp, options), options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};
			return this.sync('verify', this, options);
		},

		login: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("login", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		status: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("status", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		resetPassword: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("resetPassword", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		resendVerificationEmail: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("resendVerificationEmail", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		sync: function (method, model, options) {
			var params, success, error, xhr;

			switch (method) {

				case "verify":
					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					// Default JSON-request options.
					params = {
						type: 'PUT',
						dataType: 'json',
						contentType: 'application/json',
						data: JSON.stringify(model.toJSON())
					};
					// Ensure that we have a URL.
					if (!options.url) {
						if (model.url) {
							params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/verify";
						} else {
							urlError();
						}
					}
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				case "login":
					// Default options, unless specified.
					options || (options = {});
					options = options || {};
					params = {
						type: 'PUT',
						dataType: 'json',
						data: {
							"username": model.get("username"),
							"password": model.get("password")
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
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				case "status":
					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					params = {
						type: 'PUT',
						dataType: 'json',
						contentType: 'application/json',
						data: JSON.stringify({
							"id": model.get("id"),
							"status": model.get("status")
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
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				case "resetPassword":
					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					params = {
						type: 'PUT',
						contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
						dataType: 'json',
						data: {
							"email": model.get("email")
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
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				case "resendVerificationEmail":
					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					params = {
						type: 'PUT',
						contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
						dataType: 'json',
						data: {
							"email": model.get("email")
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
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				default:
					return (Backbone.sync).call(this, method, this, options);
			}
		}
	});

	Models.Users = Backbone.Collection.extend({
		model: Models.User,
		url: "/dep/rest/user"
	});

	// Role
	Models.Role = Backbone.Model.extend({
		urlRoot: "/dep/rest/role",
		defaults: {
			// Common Fields
			"id": undefined,
			"discriminator": undefined,
			"status": undefined,
			"statusDate": undefined,
			"user": undefined,
			// Specific Fields
			"verificationAuthority": undefined,
			"verificationAuthorityName": undefined,
			"institution": undefined,
			"department": undefined,
			"hasOnlineProfile": undefined,
			"profileURL": undefined,
			"rank": undefined,
			"subject": undefined,
			"fek": undefined,
			"fekSubject": undefined,
			"manager": undefined,
			"ministry": undefined,
			"alternateBasicInfo": {},
			"alternateBasicInfoLatin": {},
			"alternateContactInfo": {}
		},
		isPrimary: function () {
			var self = this;
			if (self.has("user")) {
				return _.isEqual(self.get("discriminator"), self.get("user").primaryRole);
			}
			return false;
		},

		// Sync
		status: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("status", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		sync: function (method, model, options) {
			var params, success, error, xhr;
			switch (method) {
				case "status":
					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					// Default JSON-request options.
					params = {
						type: 'PUT',
						dataType: 'json',
						contentType: 'application/json',
						data: JSON.stringify({
							"id": model.get("id"),
							"discriminator": model.get("discriminator"),
							"status": model.get("status")
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
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				default:
					return (Backbone.sync).call(this, method, this, options);
			}
		}
	});

	Models.Roles = Backbone.Collection.extend({
		model: Models.Role,
		user: undefined,
		url: function () {
			return "/dep/rest/role" + (this.user ? "?user=" + this.user : "");
		},
		comparator: function (role) {
			return _.indexOf(App.allowedRoles, role.get('discriminator'), false);
		}
	});

	Models.Professors = Models.Roles.extend({
		url: "/dep/rest/professor"
	});

	// File
	Models.File = Backbone.Model.extend({
		defaults: {
			"id": undefined,
			"type": undefined,
			"name": undefined,
			"description": undefined,
			"currentBody": {
				"id": undefined,
				"mimeType": undefined,
				"originalFilename": undefined,
				"storedFilePath": undefined,
				"fileSize": undefined,
				"date": undefined
			}
		}
	});

	Models.Files = Backbone.Collection.extend({
		model: Models.File,
		type: undefined,
		comparator: function (file) {
			if (!file.has("currentBody") || !file.get("currentBody").date) {
				// Should not happen, but better to be safe
				return file.get("id");
			}
			return App.utils.dateFromString(file.get("currentBody").date).getTime();
		}
	});

	Models.Institution = Backbone.Model.extend({
		urlRoot: "/dep/rest/institution",
		defaults: {
			"id": undefined,
			"name": undefined
		}
	});

	Models.Institutions = Backbone.Collection.extend({
		url: "/dep/rest/institution",
		model: Models.Institution,
		comparator: function (institution) {
			return institution.get('name');
		}
	});

	Models.InstitutionRegulatoryFramework = Backbone.Model.extend({
		urlRoot: "/dep/rest/institutionrf",
		defaults: {
			"id": undefined,
			"institution": undefined,
			"organismosURL": undefined,
			"eswterikosKanonismosURL": undefined
		}
	});

	Models.InstitutionRegulatoryFrameworks = Backbone.Collection.extend({
		url: "/dep/rest/institutionrf",
		model: Models.InstitutionRegulatoryFramework
	});

	Models.Department = Backbone.Model.extend({
		urlRoot: "/dep/rest/department",
		defaults: {
			"id": undefined,
			"department": undefined,
			"school": undefined,
			"fullName": undefined,
			"institution": undefined
		}
	});

	Models.Departments = Backbone.Collection.extend({
		url: "/dep/rest/department",
		model: Models.Department,
		comparator: function (department) {
			if (_.isObject(department.get("institution"))) {
				return department.get('institution').name + department.get('department');
			}
			return "_" + department.get('department');
		}
	});

	Models.Rank = Backbone.Model.extend({
		urlRoot: "/dep/rest/rank",
		defaults: {
			"id": undefined,
			"name": undefined
		}
	});

	Models.Ranks = Backbone.Collection.extend({
		url: "/dep/rest/rank",
		model: Models.Rank
	});

	Models.Country = Backbone.Model.extend({
		urlRoot: "/dep/rest/country",
		defaults: {
			"code": undefined,
			"name": undefined,
			"alpha2": undefined,
			"alpha3": undefined,
			"region": undefined,
			"subregion": undefined
		}
	});

	Models.Countries = Backbone.Collection.extend({
		url: "/dep/rest/country",
		model: Models.Country,
		comparator: function (model) {
			return model.get("name") + model.get("code");
		}
	});

	Models.Position = Backbone.Model.extend({
		urlRoot: "/dep/rest/position",
		defaults: {
			id: undefined,
			permanent: undefined,
			name: undefined,
			description: undefined,
			department: undefined,
			subject: undefined,
			status: undefined,
			fek: undefined,
			fekSentDate: undefined,
			canSubmitCandidacy: undefined,
			phase: {
				id: undefined,
				status: undefined,
				clientStatus: undefined,
				order: undefined,
				candidacies: {
					id: undefined,
					openingDate: undefined,
					closingDate: undefined
				},
				committee: {
					id: undefined,
					committeeMeetingDate: undefined
				},
				evaluation: {
					id: undefined
				},
				nomination: {
					id: undefined,
					nominationCommitteeConvergenceDate: undefined,
					nominationFEK: undefined,
					nominatedCandidacy: undefined,
					secondNominatedCandidacy: undefined
				},
				complementaryDocuments: {
					id: undefined
				}
			},
			createdBy: {
				"id": undefined,
				"username": undefined,
				"basicInfo": {},
				"basicInfoLatin": {},
				"contactInfo": {}
			}
		},

		phase: function (key, val, options) {
			var attrs, success, xhr, attributes = this.attributes;

			// Handle both `"key", value` and `{key: value}` -style
			// arguments.
			if (key == null || typeof key === 'object') {
				attrs = key;
				options = val;
			} else {
				(attrs = {})[key] = val;
			}

			// If we're not waiting and attributes exist, save acts as
			// `set(attr).save(null, opts)`.
			if (attrs && (!options || !options.wait) && !this.set(attrs, options)) {
				return false;
			}

			options = _.extend({
				validate: true
			}, options);

			// Do not persist invalid models.
			if (!this._validate(attrs, options)) {
				return false;
			}

			// Set temporary attributes if `{wait: true}`.
			if (attrs && options.wait) {
				this.attributes = _.extend({}, attributes, attrs);
			}

			// After a successful server-side save, the client is
			// (optionally)
			// updated with the server-side state.
			if (options.parse === void 0) {
				options.parse = true;
			}
			success = options.success;
			options.success = function (model, resp, options) {
				// Ensure attributes are restored during synchronous saves.
				model.attributes = attributes;
				var serverAttrs = model.parse(resp, options);
				if (options.wait) {
					serverAttrs = _.extend(attrs || {}, serverAttrs);
				}
				if (_.isObject(serverAttrs) && !model.set(serverAttrs, options)) {
					return false;
				}
				if (success) {
					success(model, resp, options);
				}
			};

			// Finish configuring and sending the Ajax request.
			xhr = this.sync("phase", this, options);

			// Restore attributes.
			if (attrs && options.wait) {
				this.attributes = attributes;
			}

			return xhr;
		},

		sync: function (method, model, options) {
			var params, success, error, xhr;
			switch (method) {
				case "phase":

					// Default options, unless specified.
					_.defaults(options || (options = {}), {
						emulateHTTP: Backbone.emulateHTTP,
						emulateJSON: Backbone.emulateJSON
					});
					// Default JSON-request options.
					params = {
						type: 'PUT',
						dataType: 'json',
						contentType: 'application/json',
						data: JSON.stringify({
							phase: {
								"status": model.get("phase").status
							}
						})
					};
					// Ensure that we have a URL.
					if (!options.url) {
						if (model.url) {
							params.url = (_.isFunction(model.url) ? model.url() : model.url) + "/phase";
						} else {
							urlError();
						}
					}
					success = options.success;
					options.success = function (resp) {
						if (success) {
							success(model, resp, options);
						}
						model.trigger('sync', model, resp, options);
					};
					error = options.error;
					options.error = function (xhr) {
						if (error) {
							error(model, xhr, options);
						}
						model.trigger('error', model, xhr, options);
					};
					// Make the request, allowing the user to override any Ajax
					// options.
					xhr = options.xhr = Backbone.ajax(_.extend(params, options));
					model.trigger('request', model, xhr, options);
					return xhr;

				default:
					return (Backbone.sync).call(this, method, this, options);
			}
		}
	});

	Models.Positions = Backbone.Collection.extend({
		url: "/dep/rest/position",
		model: Models.Position
	});

	Models.PositionCandidacies = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.position.id + "/candidacies";
		},
		defaults: {
			id: undefined,
			position: {
				id: undefined
			},
			candidacies: []
		}
	});

	Models.PositionCommittee = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.position.id + "/committee";
		},
		defaults: {
			id: undefined,
			position: {
				id: undefined
			},
			committeeMeetingDate: undefined,
			members: [],
			canUpdateMembers: undefined
		}
	});

	Models.PositionCommitteeMember = Backbone.Model.extend({
		defaults: {
			id: undefined,
			committee: undefined,
			professor: undefined,
			confirmedMembership: undefined
		}
	});

	Models.PositionComplementaryDocuments = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.position.id + "/complementaryDocuments";
		},
		defaults: {
			id: undefined
		}
	});

	Models.PositionEvaluation = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.position.id + "/evaluation";
		},
		defaults: {
			id: undefined,
			position: {
				id: undefined
			},
			evaluators: [],
			canUpdateEvaluators: undefined,
			canUploadEvaluations: undefined
		}
	});

	Models.PositionEvaluator = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.evaluation.position.id + "/evaluation/" + this.attributes.evaluation.id + "/evaluator";
		},
		defaults: {
			id: undefined,
			position: undefined,
			evaluation: {
				id: undefined,
				position: {
					id: undefined
				}
			},
			registerMember: {
				id: undefined
			}
		}
	});

	Models.PositionNomination = Backbone.Model.extend({
		urlRoot: function () {
			return "/dep/rest/position/" + this.attributes.position.id + "/nomination";
		},
		defaults: {
			id: undefined,
			nominationCommitteeConvergenceDate: undefined,
			nominationFEK: undefined,
			nominatedCandidacy: undefined,
			secondNominatedCandidacy: undefined
		}
	});

	Models.PositionSearchCriteria = Backbone.Model.extend({
		urlRoot: "/dep/rest/position/criteria",
		defaults: {
			id: undefined
		}
	});

	Models.Register = Backbone.Model.extend({
		urlRoot: "/dep/rest/register",
		defaults: {
			id: undefined,
			permanent: undefined,
			title: undefined,
			institution: undefined,
			members : []
		}
	});

	Models.Registries = Backbone.Collection.extend({
		url: "/dep/rest/register",
		model: Models.Register
	});

	Models.RegisterMember = Backbone.Model.extend({
		register: undefined,
		urlRoot: function () {
			return "/dep/rest/register/" + this.attributes.register.id + "/members";
		},
		defaults: {
			id: undefined,
			professor: undefined,
			internal: undefined,
			canBeDeleted: undefined
		}
	});

	Models.RegisterMembers = Backbone.Collection.extend({
		register: undefined,
		model: Models.RegisterMember,
		initialize: function (models, options) {
			this.register = options.register;
		},
		url: function () {
			return "/dep/rest/register/" + this.register + "/members";
		}
	});

	Models.PositionCommitteeRegisterMembers = Backbone.Collection.extend({
		model: Models.RegisterMember,
		url: undefined
	});

	Models.PositionEvaluationRegisterMembers = Backbone.Collection.extend({
		model: Models.RegisterMember,
		url: undefined
	});

	Models.ProfessorCommittees = Backbone.Collection.extend({
		professor: undefined,
		model: Models.PositionCommitteeMember,
		initialize: function (models, options) {
			this.professor = options.professor;
		},
		url: function () {
			return "/dep/rest/professor/" + this.professor + "/committees";
		}
	});

	Models.ProfessorEvaluations = Backbone.Collection.extend({
		professor: undefined,
		model: Models.PositionEvaluator,
		initialize: function (models, options) {
			this.professor = options.professor;
		},
		url: function () {
			return "/dep/rest/professor/" + this.professor + "/evaluations";
		}
	});

	Models.Candidacy = Backbone.Model.extend({
		urlRoot: "/dep/rest/candidacy",
		defaults: {
			id: undefined,
			permanent: undefined,
			date: undefined,
			openToOtherCandidates: undefined,
			candidacies: {
				id: undefined,
				openingDate: undefined,
				closingDate: undefined,
				position: {
					id: undefined,
					permanent: undefined,
					name: undefined,
					description: undefined,
					department: undefined,
					subject: undefined,
					status: undefined,
					fek: undefined,
					fekSentDate: undefined
				}
			},
			candidate: {
				"id": undefined
			},
			proposedEvaluators: [], // {registerMember : {id : x}}
			snapshot: {
				basicInfo: {},
				basicInfoLatin: {},
				contactInfo: {}
			}
		}
	});

	Models.CandidacyRegisterMembers = Backbone.Collection.extend({
		model: Models.RegisterMember,
		url: undefined
	});

	Models.Candidacies = Backbone.Collection.extend({
		position: undefined,
		model: Models.Candidacy,
		initialize: function (models, options) {
			this.position = options.position;
		},
		url: function () {
			return "/dep/rest/position/" + this.position + "/candidacies";
		}
	});

	Models.CandidateCandidacies = Backbone.Collection.extend({
		candidate: undefined,
		model: Models.Candidacy,
		initialize: function (models, options) {
			this.candidate = options.candidate;
		},
		url: function () {
			return "/dep/rest/candidate/" + this.candidate + "/candidacies";
		}
	});

	Models.Subject = Backbone.Model.extend({
		urlRoot: "/dep/rest/subject/",
		defaults: {
			id: undefined,
			name: undefined
		}
	});

	Models.Subjects = Backbone.Collection.extend({
		model: Models.Subject,
		url: "/dep/rest/subject/"
	});

	Models.Sector = Backbone.Model.extend({
		urlRoot: "/dep/rest/sector/",
		defaults: {
			id: undefined,
			area: undefined,
			category: undefined
		}
	});

	Models.Sectors = Backbone.Collection.extend({
		model: Models.Sector,
		url: "/dep/rest/sector/",
		comparator: function (model) {
			return model.get("area") + model.get("category");
		}

	});

	return Models;

});