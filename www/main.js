// Sets the require.js configuration for your application.
require.config({
	waitSeconds : 30,
	// Alias names
	paths : {
		// Core Libraries
		"jquery" : "lib/jquery/jquery-1.8.1",
		"bootstrap" : "lib/bootstrap/bootstrap",
		"underscore" : "lib/underscore/underscore",
		"backbone" : "lib/backbone/backbone",
		"plupload" : "lib/plupload/plupload.full",
		
		"text" : "lib/require/text",
		
		// Plugins
		"jquery.ui" : "lib/jquery.ui/jquery-ui",
		"jquery.i18n" : "lib/jquery.i18n/jquery.i18n.properties-1.0.9",
		"jquery.validate" : "lib/jquery.validate/jquery.validate",
		"jquery.dataTables" : "lib/jquery.dataTables/jquery.dataTables",
		"jquery.dataTables.bootstrap" : "lib/jquery.dataTables/DT_bootstrap",
		"jquery.blockUI" : "lib/jquery.blockUI/jquery.blockUI",
		"jquery.plupload" : "lib/plupload/jquery.plupload.queue/jquery.plupload.queue",
		"backbone.cache" : "lib/backbone/backbone.cache",
		
		// Apella Application
		"application" : "js/application",
		"models" : "js/models",
		"routers" : "js/routers",
		"views" : "js/views"
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
		"jquery.plupload" : [ "jquery", "plupload" ],
		"backbone.cache" : [ "backbone" ]
	}

});