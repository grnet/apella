'use strict';
module.exports = function (grunt) {

    // Load grunt tasks automatically, when needed
    require('jit-grunt')(grunt, {
        connect: 'grunt-contrib-connect',
        useminPrepare: 'grunt-usemin',
        ngtemplates: 'grunt-angular-templates',
        protractor: 'grunt-protractor-runner',
        injector: 'grunt-injector',
        configureProxies: 'grunt-connect-proxy',
        cachebreaker: 'grunt-cache-breaker'
    });

    // Time how long tasks take. Can help when optimizing build times
    require('time-grunt')(grunt);

    // Define the configuration for all the tasks
    grunt.initConfig({

        // Project settings
        pkg: grunt.file.readJSON('package.json'),

        /**********************
         * Build Targets ******
         *********************/

        // Empties folders to start fresh
        clean: {
            build: './target/build',
            dist: './target/dist'
        },
        // Copies remaining files to places other tasks can use
        copy: {
            build: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: './src/main/javascript',
                    dest: './target/build',
                    src: [
                        '**/*'
                    ]
                }]
            },
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: './target/build',
                    dest: './target/dist',
                    src: [
                        '*.{ico,png,txt}',
                        '.htaccess',
                        'vendor/**/*',
                        'assets/**/*',
                        'index.html'
                    ]
                }]
            }
        },

        /*********************
         ** Dist targets *****
         *********************/
        requirejs: {
            compile: {
                options: {
                    appDir: "./target/build",
                    dir: "./target/dist",
                    baseUrl: "./",
                    mainConfigFile: './target/build/main.js',
                    optimize: "uglify2",
                    inlineText: true,
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
                        },
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
                }
            }
        },

        /********************************
         ** Development server targets **
         ********************************/

        connect: {
            options: {
                port: 9000,
                hostname: 'localhost',
                base: './target/build',
                middleware: function (connect, options) {
                    var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
                    var serveStatic = require('serve-static');
                    return [
                        // Include the proxy first
                        proxy,
                        // Serve static files.
                        serveStatic(options.base[0])
                    ];
                }
            },
            proxies: [
                {
                    context: '/dep',
                    host: 'localhost',
                    port: '8080',
                    https: false,
                    changeOrigin: false
                }
            ]
        },

        watch: {
            rebuild: {
                files: [
                    './src/main/javascript/*',
                    './src/main/javascript/**/*',
                    '!{app,origination}/**/*.spec.js',
                    '!{app,origination}/**/*.mock.js',
                ],
                options: {
                    livereload: false
                },
                tasks: ['build']
            }
        }

    });

    /*****************
     ** Run Targets **
     *****************/

    grunt.registerTask('build', [
        'copy:build'
    ]);

    grunt.registerTask('build-clean', [
        'clean',
        'copy:build'
    ]);

    grunt.registerTask('dist', [
        'clean',
        'build',
        'requirejs'
    ]);

    grunt.registerTask('serve', function (target) {
        grunt.task.run([
            'build-clean',
            'configureProxies:connect',
            'connect',
            'watch'
        ]);
    });

    // Used for delaying livereload until after server has restarted
    grunt.registerTask('wait', function () {
        grunt.log.ok('Waiting for server reload...');

        var done = this.async();

        setTimeout(function () {
            grunt.log.writeln('Done waiting!');
            done();
        }, 1500);
    });
};
