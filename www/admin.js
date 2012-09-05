// Sets the require.js configuration for your application.
require.config({
	// Alias names
	paths : {
		// Core Libraries
		"jquery" : "lib/jquery/jquery-1.8.1",
		"bootstrap" : "lib/bootstrap/bootstrap",
		"underscore" : "lib/underscore/underscore",
		"backbone" : "lib/backbone/backbone",
		"plupload" : "lib/plupload/plupload.full",
		
		// Plugins
		"jquery.ui" : "lib/jquery.ui/jquery-ui",
		"jquery.i18n" : "lib/jquery.i18n/jquery.i18n.properties-1.0.9",
		"jquery.validate" : "lib/jquery.validate/jquery.validate",
		"jquery.dataTables" : "lib/jquery.dataTables/jquery.dataTables",
		"jquery.dataTables.bootstrap" : "lib/jquery.dataTables/DT_bootstrap",
		"jquery.blockUI" : "lib/jquery.blockUI/jquery.blockUI",
		"jquery.plupload" : "lib/plupload/jquery.plupload.queue/jquery.plupload.queue",
		"backbone.cache" : "lib/backbone/backbone.cache"
	
	},
	shim : {
		"underscore" : {
			"exports" : "_"
		},
		"backbone" : {
			"deps" : [ "underscore", "jquery" ],
			"exports" : "Backbone" // attaches "Backbone" to the window object
		},
		"bootstrap" : [ "jquery" ],
		"plupload" : {
			"exports" : "plupload"
		},
		"jquery.ui" : [ "jquery" ],
		"jquery.i18n" : [ "jquery" ],
		"jquery.validate" : [ "jquery" ],
		"jquery.dataTables" : [ "jquery" ],
		"jquery.dataTables.bootstrap" : [ "jquery", "jquery.dataTables", "bootstrap" ],
		"jquery.blockUI" : [ "jquery" ],
		"jquery.plupload" : {
			"deps" : [ "jquery", "plupload" ],
		},
		"backbone.cache" : [ "backbone" ],
	
	}
// end Shim Configuration

});

require([ "jquery", "underscore", "backbone", "js/application", "js/routers", "plupload", "bootstrap", "jquery.ui", "jquery.i18n", "jquery.validate", "jquery.dataTables", "jquery.dataTables.bootstrap", "jquery.blockUI", "jquery.plupload", "backbone.cache"], function($, _, Backbone, App, Routers) {
	// Loaded templates
	jQuery.i18n.properties({
		name : 'messages',
		path : 'locale/',
		mode : 'map',
		callback : function() {
			// Loaded locales
			App.router = new Routers.AdminRouter();
		}
	});
});