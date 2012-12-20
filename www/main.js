// Sets the require.js configuration for your application.
require.config({
	waitSeconds : 30,
	urlArgs : "v=20120908",
	// Alias names
	paths : {
		// Core Libraries
		"jquery" : "lib/jquery/jquery-1.8.3",
		"bootstrap" : "lib/bootstrap/bootstrap",
		"underscore" : "lib/underscore/underscore",
		"backbone" : "lib/backbone/backbone",
		"text" : "lib/require/text",
		// Plugins
		"jquery.ui" : "lib/jquery.ui/jquery-ui",
		"jquery.i18n" : "lib/jquery.i18n/jquery.i18n.properties-1.0.9",
		"jquery.validate" : "lib/jquery.validate/jquery.validate",
		"jquery.dataTables" : "lib/jquery.dataTables/jquery.dataTables",
		"jquery.dataTables.bootstrap" : "lib/jquery.dataTables/DT_bootstrap",
		"jquery.blockUI" : "lib/jquery.blockUI/jquery.blockUI",
		"jquery.iframe-transport" : "lib/jquery.file.upload/jquery.iframe-transport",
		"jquery.file.upload" : "lib/jquery.file.upload/jquery.fileupload",

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
		"jquery.ui" : [ "jquery" ],
		"jquery.i18n" : [ "jquery" ],
		"jquery.validate" : [ "jquery" ],
		"jquery.dataTables" : [ "jquery" ],
		"jquery.dataTables.bootstrap" : [ "jquery", "jquery.dataTables", "bootstrap" ],
		"jquery.blockUI" : [ "jquery" ],
		"jquery.iframe-transport" : [ "jquery" ],
		"jquery.file.upload" : [ "jquery", "jquery.ui", "jquery.iframe-transport" ],

		"backbone.cache" : [ "backbone" ]
	}

});