// Sets the require.js configuration for your application.
require.config({
	waitSeconds : 30,
	urlArgs : "v=20140328",
	// Alias names
	paths : {
		// Core Libraries
		"jquery" : "lib/jquery/jquery-1.9.1",
		"bootstrap" : "lib/bootstrap/bootstrap",
		"underscore" : "lib/underscore/underscore",
		"backbone" : "lib/backbone/backbone",
		"text" : "lib/require/text",
		// Plugins
		"jquery.ui" : "lib/jquery.ui/jquery-ui",
		"jquery.i18n" : "lib/jquery.i18n/jquery.i18n.properties-1.0.9",
		"jquery.validate" : "lib/jquery.validate/jquery.validate",
		"jquery.dataTables" : "lib/jquery.dataTables/jquery.dataTables",
		"jquery.blockUI" : "lib/jquery.blockUI/jquery.blockUI",
		"jquery.iframe-transport" : "lib/jquery.file.upload/jquery.iframe-transport",
		"jquery.file.upload" : "lib/jquery.file.upload/jquery.fileupload",
		"jquery.dynatree" : "lib/jquery.dynatree/jquery.dynatree-1.2.4",
		"jquery.selectize" : "lib/selectize/selectize",
		"microplugin" : "lib/selectize/microplugin",
		"sifter" : "lib/selectize/sifter",

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
		"jquery.blockUI" : [ "jquery" ],
		"jquery.iframe-transport" : [ "jquery" ],
		"jquery.file.upload" : [ "jquery", "jquery.ui", "jquery.iframe-transport" ],
		"jquery.dynatree" : [ "jquery", "jquery.ui" ],
		"jquery.selectize" : [ "jquery", "microplugin", "sifter", "jquery.ui", "bootstrap" ],

		"backbone.cache" : [ "backbone" ]
	}

});