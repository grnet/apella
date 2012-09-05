define([ "jquery", "bootstrap", "underscore", "backbone", "plupload", "jquery.ui", "jquery.i18n", "jquery.validate", "jquery.dataTables", "jquery.dataTables.bootstrap", "jquery.blockUI", "jquery.plupload", "backbone.cache" ], function($, _, Backbone) {
	if (!window.App) {
		window.App = {
			allowedRoles : [ "PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "CANDIDATE", "INSTITUTION_MANAGER", "MINISTRY_MANAGER" ],
			
			blockUI : function() {
				$.blockUI({
					message : $("<img src=\"css/images/loader.gif\" />"),
					showOverlay : true,
					centerY : false,
					css : {
						'z-index' : 2000,
						width : '30%',
						top : '1%',
						left : '35%',
						padding : 0,
						margin : 0,
						textAlign : 'center',
						color : '#000',
						border : 'none',
						backgroundColor : 'none',
						cursor : 'wait'
					},
					overlayCSS : {
						'z-index' : 1999,
						backgroundColor : 'none',
						opacity : 1.0
					},
				});
			},
			
			unblockUI : function() {
				$.unblockUI();
			},
			
			utils : {
				formatDate : function(formatDate, formatString) {
					if (formatDate instanceof Date) {
						var months = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
						var yyyy = formatDate.getFullYear();
						var yy = yyyy.toString().substring(2);
						var m = formatDate.getMonth();
						var mm = m < 10 ? "0" + m : m;
						var mmm = months[m];
						var d = formatDate.getDate();
						var dd = d < 10 ? "0" + d : d;
						var h = formatDate.getHours();
						var hh = h < 10 ? "0" + h : h;
						var n = formatDate.getMinutes();
						var nn = n < 10 ? "0" + n : n;
						var s = formatDate.getSeconds();
						var ss = s < 10 ? "0" + s : s;
						
						formatString = formatString.replace(/yyyy/i, yyyy);
						formatString = formatString.replace(/yy/i, yy);
						formatString = formatString.replace(/mmm/i, mmm);
						formatString = formatString.replace(/mm/i, mm);
						formatString = formatString.replace(/m/i, m);
						formatString = formatString.replace(/dd/i, dd);
						formatString = formatString.replace(/d/i, d);
						formatString = formatString.replace(/hh/i, hh);
						formatString = formatString.replace(/h/i, h);
						formatString = formatString.replace(/nn/i, nn);
						formatString = formatString.replace(/n/i, n);
						formatString = formatString.replace(/ss/i, ss);
						formatString = formatString.replace(/s/i, s);
						
						return formatString;
					}
					return "n/a";
				},
				
				formatFileSize : function(bytes) {
					var precision = 2;
					var sizes = [ 'Bytes', 'KB', 'MB', 'GB', 'TB' ];
					var posttxt = 0;
					if (bytes === undefined || bytes === 0) {
						return 'n/a';
					}
					while (bytes >= 1024) {
						posttxt++;
						bytes = bytes / 1024;
					}
					return bytes.toFixed(precision) + " " + sizes[posttxt];
				}
			}
		};
	}
	return window.App;
});