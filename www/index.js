require([ 'main' ], function(common) {
	require([ "jquery", "underscore", "backbone", "application", "routers" ], function($, _, Backbone, App, Routers) {
		// Loaded templates
		jQuery.i18n.properties({
			language : (function() {
				return App.utils.getCookie("apella-lang") ? App.utils.getCookie("apella-lang") : "el";
			})(),
			name : 'messages',
			path : 'locale/',
			mode : 'map',
			callback : function() {
				// Loaded locales
				App.router = new Routers.Router();
			}
		});
	});
});