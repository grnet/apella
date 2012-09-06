require([ 'main' ], function(common) {
	require([ "jquery", "underscore", "backbone", "application", "routers"], function($, _, Backbone, App, Routers) {
		// Loaded templates
		jQuery.i18n.properties({
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