require([ 'main' ], function(common) {
	require([ "jquery", "underscore", "backbone", "js/application", "js/routers", "plupload", "bootstrap", "jquery.ui", "jquery.i18n", "jquery.validate", "jquery.dataTables", "jquery.dataTables.bootstrap", "jquery.blockUI", "jquery.plupload", "backbone.cache" ], function($, _, Backbone, App, Routers) {
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