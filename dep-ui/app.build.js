({
	appDir: "./www",
	baseUrl: "./",
	dir: "./bin/www",
	optimize: "closure",
	closure: {
		CompilerOptions: {},
		CompilationLevel: 'SIMPLE_OPTIMIZATIONS',
		loggingLevel: 'WARNING'
	},
	inlineText: true,
	mainConfigFile: './www/main.js',
	keepBuildDir: true,
	removeCombined: true,
	skipModuleInsertion: false,
	optimizeAllPluginResources: true,
	findNestedDependencies: true,
	modules: [
		{
			name: "index",
			include: [],
			exclude: []
		},
		{
			name: "registration",
			include: [],
			exclude: []
		} ,
		{
			name: "apella",
			include: [],
			exclude: []
		},
		{
			name: "helpdesk",
			include: [],
			exclude: []
		}
	]
})