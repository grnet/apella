/*global define */
define(["jquery",
    "underscore",
    "backbone",
    "bootstrap",
    "jquery.ui",
    "jquery.i18n",
    "jquery.validate",
    "jquery.dataTables",
    "jquery.blockUI",
    "jquery.file.upload",
    "jquery.iframe-transport",
    "jquery.dynatree",
    "jquery.selectize",
    "backbone.cache",
    "chosen"
], function ($, _, Backbone) {

    "use strict";

    if (!window.App) {
        // Validator Plugin Extensions/Configurations
        $.validator.setDefaults({
            ignore: []
        });
        $.validator.addMethod("onlyLatin", function (value, element) {
            return this.optional(element) || /^[a-zA-Z0-9][a-zA-Z0-9_ \-'"]*[a-zA-Z0-9]$/.test(value);
        }, "Please type only latin characters");
        $.validator.addMethod("requiredIfOtherGreek", function (value, element, param) {
            var target = $(param);
            if (this.settings.onfocusout) {
                target.unbind(".validate-requiredIfOtherGreek").bind("blur.validate-requiredIfOtherGreek", function () {
                    $(element).valid();
                });
            }
            return (/^[a-zA-Z0-9][a-zA-Z0-9_ \-'"]*[a-zA-Z0-9]$/.test(target.val())) || value;
        }, "Required if other field is in greek characters");

        $.validator.addMethod("pwd", function (value, element) {
            return this.optional(element) || /^[a-zA-Z0-9!@#$%^&*()]*$/.test(value);
        }, "Please type only latin characters");
        $.validator.addMethod("dateAfter", function (value, element, params) {
            var days = params[1] * 86400000; // millisecond in a day
            var beforeDate = params[0].datepicker("getDate");
            var afterDate = $(element).datepicker("getDate");
            if (_.isNull(beforeDate) || _.isNull(afterDate)) {
                return false;
            }
            return (afterDate.getTime() - beforeDate.getTime() >= days);
        }, "Date must be {1} days later than {0}");

        $.datepicker.setDefaults({
            dateFormat: "dd/mm/yy"
        });

        // DataTables Plugin Extensions/Configurations

        // Custom Date sorting
        $.extend($.fn.dataTableExt.oSort, {
            "date-eu-pre": function (date) {
                date = date.replace(" ", "");
                var eu_date, year;

                if (date == '') {
                    return 0;
                }

                if (date.indexOf('.') > 0) {
                    /*date a, format dd.mn.(yyyy) ; (year is optional)*/
                    eu_date = date.split('.');
                } else {
                    /*date a, format dd/mn/(yyyy) ; (year is optional)*/
                    eu_date = date.split('/');
                }

                /*year (optional)*/
                if (eu_date[2]) {
                    year = eu_date[2];
                } else {
                    year = 0;
                }

                /*month*/
                var month = eu_date[1];
                if (month.length == 1) {
                    month = 0 + month;
                }

                /*day*/
                var day = eu_date[0];
                if (day.length == 1) {
                    day = 0 + day;
                }

                return (year + month + day) * 1;
            },

            "date-eu-asc": function (a, b) {
                console.log('date-eu-asc');
                return ((a < b) ? -1 : ((a > b) ? 1 : 0));
            },

            "date-eu-desc": function (a, b) {
                console.log('date-eu-desc');
                return ((a < b) ? 1 : ((a > b) ? -1 : 0));
            }
        });
        /* Default class modification */
        $.extend($.fn.dataTableExt.oStdClasses, {
            "sWrapper": "dataTables_wrapper form-inline"
        });
        /* API method to get paging information */
        $.fn.dataTableExt.oApi.fnPagingInfo = function (oSettings) {
            return {
                "iStart": oSettings._iDisplayStart,
                "iEnd": oSettings.fnDisplayEnd(),
                "iLength": oSettings._iDisplayLength,
                "iTotal": oSettings.fnRecordsTotal(),
                "iFilteredTotal": oSettings.fnRecordsDisplay(),
                "iPage": oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength),
                "iTotalPages": oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
            };
        };
        /* Bootstrap style pagination control */
        $.extend($.fn.dataTableExt.oPagination, {
            "bootstrap": {
                "fnInit": function (oSettings, nPaging, fnDraw) {
                    var oLang = oSettings.oLanguage.oPaginate;
                    var fnClickHandler = function (e) {
                        e.preventDefault();
                        if (oSettings.oApi._fnPageChange(oSettings, e.data.action)) {
                            fnDraw(oSettings);
                        }
                    };

                    $(nPaging).addClass('pagination').append('<ul>' + '<li class="prev disabled"><a href="#">&larr; ' + oLang.sPrevious + '</a></li>' + '<li class="next disabled"><a href="#">' + oLang.sNext + ' &rarr; </a></li>' + '</ul>');
                    var els = $('a', nPaging);
                    $(els[0]).bind('click.DT', {
                        action: "previous"
                    }, fnClickHandler);
                    $(els[1]).bind('click.DT', {
                        action: "next"
                    }, fnClickHandler);
                },

                "fnUpdate": function (oSettings, fnDraw) {
                    var iListLength = 5;
                    var oPaging = oSettings.oInstance.fnPagingInfo();
                    var an = oSettings.aanFeatures.p;
                    var i, ien, j, sClass, iStart, iEnd, iHalf = Math.floor(iListLength / 2);

                    if (oPaging.iTotalPages < iListLength) {
                        iStart = 1;
                        iEnd = oPaging.iTotalPages;
                    } else if (oPaging.iPage <= iHalf) {
                        iStart = 1;
                        iEnd = iListLength;
                    } else if (oPaging.iPage >= (oPaging.iTotalPages - iHalf)) {
                        iStart = oPaging.iTotalPages - iListLength + 1;
                        iEnd = oPaging.iTotalPages;
                    } else {
                        iStart = oPaging.iPage - iHalf + 1;
                        iEnd = iStart + iListLength - 1;
                    }

                    for (i = 0, ien = an.length; i < ien; i++) {
                        // Remove the middle elements
                        $('li:gt(0)', an[i]).filter(':not(:last)').remove();

                        // Add the new list items and their event handlers
                        for (j = iStart; j <= iEnd; j++) {
                            sClass = (j == oPaging.iPage + 1) ? 'class="active"' : '';
                            $('<li ' + sClass + '><a href="#">' + j + '</a></li>').insertBefore($('li:last', an[i])[0]).bind('click', function (e) {
                                e.preventDefault();
                                oSettings._iDisplayStart = (parseInt($('a', this).text(), 10) - 1) * oPaging.iLength;
                                fnDraw(oSettings);
                            });
                        }

                        // Add / remove disabled classes from the static
                        // elements
                        if (oPaging.iPage === 0) {
                            $('li:first', an[i]).addClass('disabled');
                        } else {
                            $('li:first', an[i]).removeClass('disabled');
                        }

                        if (oPaging.iPage === oPaging.iTotalPages - 1 || oPaging.iTotalPages === 0) {
                            $('li:last', an[i]).addClass('disabled');
                        } else {
                            $('li:last', an[i]).removeClass('disabled');
                        }
                    }
                }
            }
        });
        /* Custom Add <tr> function */
        $.fn.dataTableExt.oApi.fnAddTr = function (oSettings, nTrHTML) {
            if (typeof bRedraw == 'undefined') {
                bRedraw = true;
            }
            var nTr = $.parseHTML(nTrHTML)[0];
            var nTds = nTr.getElementsByTagName('td');
            if (nTds.length != oSettings.aoColumns.length) {
                return;
            }

            var aData = [];
            for (var i = 0; i < nTds.length; i++) {
                aData.push(nTds[i].innerHTML || " ");
            }

            /* Add the data and then replace DataTable's generated TR with ours */
            var iIndex = this.oApi._fnAddData(oSettings, aData);
            oSettings.aoData[iIndex].nTr.id = nTr.id;

            oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

            if (bRedraw) {
                this.oApi._fnReDraw(oSettings);
            }
        };

        // Extra plugin to delay filtering in data tables when user types
        $.fn.dataTableExt.oApi.fnSetFilteringDelay = function (oSettings, iDelay) {
            var _that = this;

            if (iDelay === undefined) {
                iDelay = 250;
            }

            this.each(function (i) {
                $.fn.dataTableExt.iApiIndex = i;
                var
                    $this = this,
                    oTimerId = null,
                    sPreviousSearch = null,
                    anControl = $('input', _that.fnSettings().aanFeatures.f);

                anControl.unbind('keyup').bind('keyup', function () {
                    var $$this = $this;

                    if (sPreviousSearch === null || sPreviousSearch != anControl.val()) {
                        window.clearTimeout(oTimerId);
                        sPreviousSearch = anControl.val();
                        oTimerId = window.setTimeout(function () {
                            $.fn.dataTableExt.iApiIndex = i;
                            _that.fnFilter(anControl.val());
                        }, iDelay);
                    }
                });

                return this;
            });
            return this;
        };
        $.fn.dataTable.defaults.bAutoWidth = false;

        // Add _super function in Model, Views
        (function (Backbone) {
            function _super(methodName, args) {
                this._superCallObjects || (this._superCallObjects = {});
                var currentObject = this._superCallObjects[methodName] || this, parentObject = findSuper(methodName, currentObject);
                this._superCallObjects[methodName] = parentObject;
                var result = parentObject[methodName].apply(this, args || []);
                delete this._superCallObjects[methodName];
                return result;
            }

            function findSuper(methodName, childObject) {
                var object = childObject;
                while (object[methodName] === childObject[methodName]) {
                    object = object.constructor.__super__;
                }
                return object;
            }

            _.each(["Model", "Collection", "View", "Router"], function (klass) {
                Backbone[klass].prototype._super = _super;
            });

        })(Backbone);

        window.App = {
            usernameRegistrationRoles: ["PROFESSOR_DOMESTIC", "PROFESSOR_FOREIGN", "CANDIDATE", "INSTITUTION_MANAGER"],

            blockUI: function () {
                $.blockUI({
                    message: $("<img src=\"css/images/loader.gif\" />"),
                    showOverlay: true,
                    centerY: false,
                    css: {
                        'z-index': 2000,
                        width: '30%',
                        top: '1%',
                        left: '35%',
                        padding: 0,
                        margin: 0,
                        textAlign: 'center',
                        color: '#000',
                        border: 'none',
                        backgroundColor: 'none',
                        cursor: 'wait'
                    },
                    overlayCSS: {
                        'z-index': 1999,
                        backgroundColor: 'none',
                        opacity: 1.0
                    }
                });
            },

            unblockUI: function () {
                $.unblockUI();
            },

            utils: {
                dateFromString: function (str) {
                    // "dd/mm/yy HH:MM:SS"
                    var m = str.match(/(\d+)\/(\d+)\/(\d+)\s+(\d+):(\d+):(\d+)/);
                    return new Date(+m[3], +m[2] - 1, +m[1], +m[4], +m[5], +m[6], 0);
                },

                formatFileSize: function (bytes) {
                    var precision = 2;
                    var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
                    var posttxt = 0;
                    if (bytes === undefined || bytes === 0) {
                        return 'n/a';
                    }
                    while (bytes >= 1024) {
                        posttxt++;
                        bytes /= 1024;
                    }
                    return bytes.toFixed(precision) + "" + sizes[posttxt];
                },

                formatPositionID: function (num) {
                    if (!num) {
                        return undefined;
                    }
                    var str = "" + num;
                    while (str.length <= 10) {
                        str = "0" + str;
                    }
                    return str;
                },

                getLocale: function () {
                    function browserLocale() {
                        var locale = $.i18n.browserLang();
                        if (/^en/.test(locale)) {
                            locale = 'en';
                        } else {
                            locale = 'el';
                        }
                        return locale;
                    }

                    return window.App.utils.getCookie("apella-lang") || browserLocale();
                },

                // Cookies
                addCookie: function (name, value, days) {
                    var date, expires;
                    if (days) {
                        date = new Date();
                        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
                        expires = "; expires=" + date.toGMTString();
                    } else {
                        expires = "";
                    }
                    document.cookie = name + "=" + value + expires + "; path=/";
                },

                getCookie: function (name) {
                    var nameEQ = name + "=";
                    var ca = document.cookie.split(';');
                    for (var i = 0; i < ca.length; i++) {
                        var c = ca[i];
                        while (c.charAt(0) == ' ') {
                            c = c.substring(1, c.length);
                        }
                        if (c.indexOf(nameEQ) == 0) {
                            return c.substring(nameEQ.length, c.length);
                        }
                    }
                    return null;
                },

                scrollTo: function ($el) {
                    $('html, body').animate({
                        scrollTop: ($el.offset().top - 75) // 50 is the header height
                    }, 500);
                },

                removeCookie: function (name) {
                    window.App.utils.addCookie(name, "", -1);
                }
            }
        };
    }
    return window.App;
});