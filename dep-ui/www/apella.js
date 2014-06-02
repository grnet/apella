require([
	'main'
], function (common) {
	require([
		"jquery",
		"underscore",
		"backbone",
		"application",
		"routers"
	], function ($, _, Backbone, App, Routers) {
		// Loaded templates
		$.i18n.properties({
			language: App.utils.getLocale(),
			name: 'messages',
			path: 'locale/',
			mode: 'map',
			callback: function () {
				// Loaded locales
				App.router = new Routers.ApellaRouter();
			}
		});
	});
});