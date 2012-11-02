define([ "jquery", "underscore", "backbone", "bootstrap", "jquery.ui", "jquery.i18n", "jquery.validate", "jquery.dataTables", "jquery.dataTables.bootstrap", "jquery.blockUI", "jquery.file.upload", "jquery.iframe-transport", "backbone.cache" ], function($, _, Backbone) {
	if (!window.App) {
		// Configuration
		$.validator.addMethod("onlyLatin", function(value, element) {
			return this.optional(element) || /^[a-zA-Z]*$/.test(value);
		}, "Please type only latin characters");

		$.validator.addMethod("pwd", function(value, element) {
			return this.optional(element) || /^[a-zA-Z0-9!@#$%^&*()]*$/.test(value);
		}, "Please type only latin characters");
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
						while (c.charAt(0) == ' ')
							c = c.substring(1, c.length);
						if (c.indexOf(nameEQ) == 0)
							return c.substring(nameEQ.length, c.length);
					}
					return null;
				},

				removeCookie : function(name) {
					addCookie(name, "", -1);
				}
			}
		};
	}
	return window.App;
});