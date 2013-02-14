define([ "jquery", "underscore", "backbone", "bootstrap", "jquery.ui", "jquery.i18n", "jquery.validate", "jquery.dataTables", "jquery.dataTables.bootstrap", "jquery.blockUI", "jquery.file.upload", "jquery.iframe-transport", "backbone.cache" ], function($, _, Backbone) {
	if (!window.App) {
		// Configuration
		$.validator.setDefaults({
			ignore : []
		});
		$.validator.addMethod("onlyLatin", function(value, element) {
			return this.optional(element) || /^[a-zA-Z0-9]*$/.test(value);
		}, "Please type only latin characters");
		$.validator.addMethod("requiredIfOtherGreek", function(value, element, param) {
			var target = $(param);
			if (this.settings.onfocusout) {
				target.unbind(".validate-requiredIfOtherGreek").bind("blur.validate-requiredIfOtherGreek", function() {
					$(element).valid();
				});
			}
			return (/^[a-zA-Z0-9]*$/.test(target.val()));
		}, "Required if other field is in greek characters");

		$.validator.addMethod("pwd", function(value, element) {
			return this.optional(element) || /^[a-zA-Z0-9!@#$%^&*()]*$/.test(value);
		}, "Please type only latin characters");
		$.validator.addMethod("dateAfter", function(value, element, params) {
			var days = params[1] * 86400000; // millisecond in a day
			var beforeDate = params[0].datepicker("getDate");
			var afterDate = $(element).datepicker("getDate");
			if (_.isNull(beforeDate) || _.isNull(afterDate)) {
				return false;
			}
			return (afterDate.getTime() - beforeDate.getTime() >= days);
		}, "Date must be {1} days later than {0}");

		$.datepicker.setDefaults({
			dateFormat : "dd/mm/yy"
		});
		// Add _super function in Model, Views
		(function(Backbone) {
			function _super(methodName, args) {
				this._superCallObjects || (this._superCallObjects = {});
				var currentObject = this._superCallObjects[methodName] || this, parentObject = findSuper(methodName, currentObject);
				this._superCallObjects[methodName] = parentObject;
				var result = parentObject[methodName].apply(this, args || []);
				delete this._superCallObjects[methodName];
				return result;
			}

			function findSuper(methodName, childObject) {
				var object = childObject;
				while (object[methodName] === childObject[methodName]) {
					object = object.constructor.__super__;
				}
				return object;
			}

			_.each([ "Model", "Collection", "View", "Router" ], function(klass) {
				Backbone[klass].prototype._super = _super;
			});

		})(Backbone);

		window.App = {
			allowedRoles : [ "PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "CANDIDATE", "INSTITUTION_MANAGER" ],

			blockUI : function() {
				$.blockUI({
					message : $("<img src=\"css/images/loader.gif\" />"),
					showOverlay : true,
					centerY : false,
					css : {
						'z-index' : 2000,
						width : '30%',
						top : '1%',
						left : '35%',
						padding : 0,
						margin : 0,
						textAlign : 'center',
						color : '#000',
						border : 'none',
						backgroundColor : 'none',
						cursor : 'wait'
					},
					overlayCSS : {
						'z-index' : 1999,
						backgroundColor : 'none',
						opacity : 1.0
					}
				});
			},

			unblockUI : function() {
				$.unblockUI();
			},

			utils : {
				dateFromString : function(str) {
					// "dd/mm/yy HH:MM:SS"
					var m = str.match(/(\d+)\/(\d+)\/(\d+)\s+(\d+):(\d+):(\d+)/);
					return new Date(+m[3], +m[2] - 1, +m[1], +m[4], +m[5], +m[6], 0);
				},

				formatFileSize : function(bytes) {
					var precision = 2;
					var sizes = [ 'Bytes', 'KB', 'MB', 'GB', 'TB' ];
					var posttxt = 0;
					if (bytes === undefined || bytes === 0) {
						return 'n/a';
					}
					while (bytes >= 1024) {
						posttxt++;
						bytes = bytes / 1024;
					}
					return bytes.toFixed(precision) + "" + sizes[posttxt];
				},

				// Cookies
				addCookie : function(name, value, days) {
					var date, expires;
					if (days) {
						date = new Date();
						date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
						expires = "; expires=" + date.toGMTString();
					} else {
						expires = "";
					}
					document.cookie = name + "=" + value + expires + "; path=/";
				},

				getCookie : function(name) {
					var nameEQ = name + "=";
					var ca = document.cookie.split(';');
					for ( var i = 0; i < ca.length; i++) {
						var c = ca[i];
						while (c.charAt(0) == ' ') {
							c = c.substring(1, c.length);
						}
						if (c.indexOf(nameEQ) == 0) {
							return c.substring(nameEQ.length, c.length);
						}
					}
					return null;
				},

				removeCookie : function(name) {
					window.App.util.addCookie(name, "", -1);
				}
			}
		};
	}
	return window.App;
});