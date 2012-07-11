var tpl = {
	
	// Hash of preloaded templates for the app
	templates : {},
	
	// Recursively pre-load all the templates for the app.
	// This implementation should be changed in a production environment. All
	// the template files should be
	// concatenated in a single file.
	loadTemplates : function(names, callback) {
		
		var that = this;
		
		var loadTemplate = function(index) {
			var name = names[index];
			$.get('tpl/' + name + '.html', function(data) {
				that.templates[name] = data;
				index++;
				if (index < names.length) {
					loadTemplate(index);
				} else {
					callback();
				}
			});
		};
		
		loadTemplate(0);
	},
	
	// Get template by name from hash of preloaded templates
	get : function(name) {
		return this.templates[name];
	}

};

function formatDate(formatDate, formatString) {
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
};

function formatFileSize(bytes) {
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
};