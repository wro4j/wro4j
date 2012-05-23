var nonMobilePages = [
    '/home/search/advanced-search',
    '/my-oua/manage-enrolment/shopping-cart',
    '/my-oua/manage-enrolment/account-summary',
    '/my-oua/manage-enrolment/fee-help-ecan',
    '/my-oua/manage-your-study/book-exam',
    '/my-oua/manage-your-study/book-exam/.*',
    '/my-oua/manage-your-study/student-community',
    '/my-oua/manage-your-study/contact-list',
    '/my-oua/manage-your-study/withdrawal-page',
    '/my-oua/manage-your-study/confirm-withdrawal',
    '/my-oua/manage-your-study/active-units',
    '/my-oua/manage-personal-details/update-profile',
    '/special-circumstances/apply',
    '/login/register',
    '/login/new-register',
    '/login/do-create',
    '/student-admin-and-support/key-dates',
    '/student-admin-and-support/key-dates/key-dates',
    '/student-admin-and-support/exams-and-results/exam-key-dates',
    '/shopping/select-study-period',
    '/course/.*/courseApplication'
];
var nonMobilePagesRegexp = '^/career-advice/.*|^/(public|secure)' + nonMobilePages.join('$|^/(public|secure)') + '$';

var country_init_1 = '';
var state_init_1 = '';

// Support popups from PS pages. The uses of wcm_link_popup() are embedded in the PS pages.
var _WCM_POPUP_FEATURES = 'location=no, statusbar=no, status=no, toolbar=no, menubar=no, scrollbars=yes, resizable=yes, width=600, height=400';
function wcm_link_popup(src) {
    var theWindow = window.open(src.getAttribute('href'), src.getAttribute('target'), _WCM_POPUP_FEATURES);
    theWindow.focus();
    return theWindow;
}

// checks for mobile devices
function isPhone() {
    return navigator.userAgent.match(/iPhone/i) ||
        navigator.userAgent.match(/Android/i) ||
        document.cookie.match(/iphone=true/);
}
// site should look mobile if you're on a phone, there is a mobile version of the page
// and you haven't said that you specifically want to see the full site
if (isPhone() && !document.location.pathname.match(nonMobilePagesRegexp) &&
    !document.cookie.match(/full-site-on-mobile=true/)) {
    $('head').append('<meta name="viewport" content="width=320, user-scalable=no" />');
    $('html').addClass('mobile');
    $('span.dynamic-phone').each(function () {
        $(this).replaceWith("<span class='"+ $(this).attr('class') + "'><a href='tel:" + $(this).text().replace(/[^\d]+/g, '') + "'>" + $(this).text() + "</a></span>");
    });
}

// Trim a string at each end
function trim_string(sInString) {
    if (sInString) {
        sInString = sInString.replace(/^\s+/g, "");// strip leading
        return sInString.replace(/\s+$/g, "");// strip trailing
    }
}

// Populates the country selected with the counties from the country list
function populate_country(country_input, state_input, country_init, state_init, defaultCountry) {
    if (country_init != '') {
        defaultCountry = country_init;
    }

    var countryLineArray = country_data.replace(/\|$/, '').split('|');  // Split into lines
    var selObj = document.getElementById(country_input);
    selObj.options[0] = new Option('Select ...', '');
    selObj.selectedIndex = 0;
    for (var loop = 0; loop < countryLineArray.length; loop++) {
        var lineArray = countryLineArray[loop].split(':');
        var countryCode = trim_string(lineArray[0]);
        var countryName = trim_string(lineArray[1]);
        if (countryCode != '') {
            selObj.options[loop + 1] = new Option(countryName, countryCode);
        }
        if (defaultCountry === countryCode) {
            selObj.selectedIndex = loop + 1;
        }
    }
}

// Populates the state based on the country
function populate_state(country_input, state_input, country_init, state_init) {
    var $country = $(document.getElementById(country_input));
    $country.closest('.location').find('.no-states').replaceWith('<select class="state-selection js-section-required" name="' + state_input + '" id="' + state_input + '"/>');

    var selObj = document.getElementById(state_input);
    var originalVal = $(selObj).val();
    var foundState = false;

    if (selObj.type === 'select-one') {
        for (var i = 0; i < selObj.options.length; i++) {
            selObj.options[i] = null;
        }
        selObj.options.length = null;
        selObj.options[0] = new Option('Select ...', '');
        selObj.selectedIndex = 0;
    }

    var stateLineArray = state_data.replace(/\|$/, '').split("|");  // Split into lines
    var optionCntr = 1;
    for (var loop = 0; loop < stateLineArray.length; loop++) {
        var lineArray = stateLineArray[loop].split(":");
        var countryCode = trim_string(lineArray[0]);
        var stateCode = trim_string(lineArray[1]);
        var stateName = trim_string(lineArray[2]);
        if (countryCode !== '' && $country.val() == countryCode) {
            if (stateCode !== '') {
                selObj.options[optionCntr] = new Option(stateName, stateCode);
            }

            $(selObj).val(originalVal == null ? '' : originalVal);
            foundState = true;
            optionCntr++;
        }
    }
    if (!foundState) {
        selObj.disabled = "disabled";
        selObj.selectedIndex = 0;
    } else {
        selObj.disabled = null;
    }
}

//Initialise value
function init_country(country_input, state_input, country_init, state_init, country) {
    populate_country(country_input, state_input, country_init, state_init, country);
    populate_state(country_input, state_input, country_init, state_init);
}

(function($) {
    $('.visibility-toggler input[type=checkbox]').live('change', function () {
        var targetContainer = $(this).closest('.visibility-toggler').siblings('.toggle-content');
        if ($(this).attr('checked')) {
            targetContainer.show().find(':input').first().focus();
            targetContainer.siblings('.submit-options').removeClass('no-fields-present-submit-options');
        } else {
            targetContainer.hide();
            targetContainer.siblings('.submit-options').addClass('no-fields-present-submit-options');
        }
    });
})(jQuery);
    
if(typeof(Array.indexOf) === 'undefined') {
    Array.prototype.indexOf = function(obj) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] === obj) {
                return i;
            }
        }
        return -1;
    };
}

if(typeof(String.prototype.trim) === 'undefined') {
	String.prototype.trim = function() {
	  return this.replace(/^\s+|\s+$/g,"");
	};
}

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};


/**
 * Cookie plugin
 *
 * Copyright (c) 2006 Klaus Hartl (stilbuero.de)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */
jQuery.cookie = function(name, value, options) {
    if (typeof value != 'undefined') { // name and value given, set cookie
        options = options || {};
        if (value === null) {
            value = '';
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
        }
        // CAUTION: Needed to parenthesize options.path and options.domain
        // in the following expressions, otherwise they evaluate to undefined
        // in the packed version for some reason...
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else { // only name given, get cookie
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};

/*
 * jQuery UI Autocomplete HTML Extension
 *
 * Copyright 2010, Scott Gonz√°lez (http://scottgonzalez.com)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * http://github.com/scottgonzalez/jquery-ui-extensions
 */
(function($) {

    var proto = $.ui.autocomplete.prototype,
        initSource = proto._initSource;

    function filter(array, term) {
        var matcher = new RegExp($.ui.autocomplete.escapeRegex(term), "i");
        return $.grep(array, function(value) {
            return matcher.test($("<div>").html(value.label || value.value || value).text());
        });
    }

    $.extend(proto, {
        _initSource: function() {
            if (this.options.html && $.isArray(this.options.source)) {
                this.source = function(request, response) {
                    response(filter(this.options.source, request.term));
                };
            } else {
                initSource.call(this);
            }
        },

        _renderItem: function(ul, item) {
            return $("<li></li>")
                .data("item.autocomplete", item)
                .append($("<a></a>")[ this.options.html ? "html" : "text" ](item.label))
                .appendTo(ul);
        }
    });

})(jQuery);

jQuery.fn.outerHTML = function() {
    return $('<div>').append(this.eq(0).clone()).html();
};

/**
* hoverIntent r6 // 2011.02.26 // jQuery 1.5.1+
* <http://cherne.net/brian/resources/jquery.hoverIntent.html>
*
* @param  f  onMouseOver function || An object with configuration options
* @param  g  onMouseOut function  || Nothing (use configuration options object)
* @author    Brian Cherne brian(at)cherne(dot)net
*/
(function($){$.fn.hoverIntent=function(f,g){var cfg={sensitivity:7,interval:100,timeout:0};cfg=$.extend(cfg,g?{over:f,out:g}:f);var cX,cY,pX,pY;var track=function(ev){cX=ev.pageX;cY=ev.pageY};var compare=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);if((Math.abs(pX-cX)+Math.abs(pY-cY))<cfg.sensitivity){$(ob).unbind("mousemove",track);ob.hoverIntent_s=1;return cfg.over.apply(ob,[ev])}else{pX=cX;pY=cY;ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}};var delay=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);ob.hoverIntent_s=0;return cfg.out.apply(ob,[ev])};var handleHover=function(e){var ev=jQuery.extend({},e);var ob=this;if(ob.hoverIntent_t){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t)}if(e.type=="mouseenter"){pX=ev.pageX;pY=ev.pageY;$(ob).bind("mousemove",track);if(ob.hoverIntent_s!=1){ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}}else{$(ob).unbind("mousemove",track);if(ob.hoverIntent_s==1){ob.hoverIntent_t=setTimeout(function(){delay(ev,ob)},cfg.timeout)}}};return this.bind('mouseenter',handleHover).bind('mouseleave',handleHover)}})(jQuery);

/*! Copyright (c) 2010 Brandon Aaron (http://brandonaaron.net)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Version 2.1.3-pre
 */
(function($){

jQuery.fn.bgiframe = ($.browser.msie && /msie 6\.0/i.test(navigator.userAgent) ? function(s) {
    s = $.extend({
        top     : 'auto', // auto == .currentStyle.borderTopWidth
        left    : 'auto', // auto == .currentStyle.borderLeftWidth
        width   : 'auto', // auto == offsetWidth
        height  : 'auto', // auto == offsetHeight
        opacity : true,
        src     : 'javascript:false;'
    }, s);
    var html = '<iframe class="bgiframe"frameborder="0"tabindex="-1"src="'+s.src+'"'+
                   'style="display:block;position:absolute;z-index:-1;'+
                       (s.opacity !== false?'filter:Alpha(Opacity=\'0\');':'')+
                       'top:'+(s.top=='auto'?'expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+\'px\')':prop(s.top))+';'+
                       'left:'+(s.left=='auto'?'expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+\'px\')':prop(s.left))+';'+
                       'width:'+(s.width=='auto'?'expression(this.parentNode.offsetWidth+\'px\')':prop(s.width))+';'+
                       'height:'+(s.height=='auto'?'expression(this.parentNode.offsetHeight+\'px\')':prop(s.height))+';'+
                '"/>';
    return this.each(function() {
        if ( $(this).children('iframe.bgiframe').length === 0 )
            this.insertBefore( document.createElement(html), this.firstChild );
    });
} : function() { return this; });

// old alias
jQuery.fn.bgIframe = jQuery.fn.bgiframe;

function prop(n) {
    return n && n.constructor === Number ? n + 'px' : n;
}

})(jQuery);

OUAWebsite = function ($) {
    var timeouts = {};
    var enableFaqUpdate = false;

    var swapMatcher = /Unit to withdraw from:|Unit to add:|Study Period\/Session:/g;
    var enrolmentLetterMatcher = /Please specify years \(e.g. 2006 - 2007\):/g;

    function publicPrefix() {
    	return '/public';
    }
    
    function securePrefix() {
    	return '/secure';
    }
    
    function publicOrSecure() {
        return (document.location.pathname.match(/^\/public\//) ? publicPrefix() : (document.location.pathname.match(/^\/secure\//) ? securePrefix() : publicPrefix()));
    }

    function enrolmentResignEnabled() {
        return $('#study-cart-summary').size() > 0;
    }

    function round(num, places) {
        return Math.round(num * Math.pow(10, places)) / Math.pow(10, places);
    }

    function toPublicOrSecure(link) {
        return link.replace(/\/(public|secure)\//g, publicOrSecure() + "/");
    }

    function liveAndOnPageLoad(selector, event, func) {
        $(selector).live(event, func);
        $(function () {
            $(selector).each(function() {
                func.call(this);
            });
        });
    }

    function pluralise(count, type) {
        return count + ' ' + type + (count === 1 ? '' : 's')
    }

    function indexOf(arr, obj) { // TODO HTML unit would not understad array.prototype.indexOf
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] === obj) {
                return i;
            }
        }
        return -1;
    }

    function trackEvent(category, action, label, valueNumber) {
        setTimeout(function () {
            if (typeof(valueNumber) === 'undefined') {
                if (typeof(label) === 'undefined') {
                    _gaq.push(['_trackEvent', category, action]);
                } else {
                    _gaq.push(['_trackEvent', category, action, label]);
                }
            } else {
                _gaq.push(['_trackEvent', category, action, label, parseInt(valueNumber)]);
            }
        }, 100);
    }

    function trackPage(url) {
        _gaq.push(['_trackPageview', url]);
    }

    function ie6() {
        return $("link[href$='oua-basic-ie6.css']").size() > 0;
    }

    $(function () {
    	$('body').append('<div id="loading-bar" style="position: absolute; left: -2000px;"></div>');
    });

    function loadingBar(message) {
        return $('#loading-bar').clone().css('position', '').css('left', '').append(message ? message : '').addClass(message ? 'with-text' : '');
    }

    function blockPage(e){
    	var message = ($(e.target).attr('data-block-message'));
        $.fancybox({
        	content: loadingBar(message),
            modal: true,
            centerOnScroll: true,
            transitionIn: 'none',
            transitionOut: 'none'
        });
    }

    function unBlockPage(){$.fancybox.close();}

    function lightboxContent(content) {
        $.fancybox(content, {
           modal: true,
           transitionIn: 'none',
           transitionOut: 'none'
        });
      }

      function ajaxSubmitter(formId, success) {
          $('#' + formId).live('submit', function (e) {
            e.preventDefault();
            var $form = $(this);
            var $contentBox = $('#fancybox-content');

            $contentBox.height($contentBox.height());

            $.ajax({
              type: "POST",
              url: $form.attr('action'),
              data: $form.serialize(),
              success: function (data) {
                var $forms = $('#' + formId, data);
                if($forms.size() === 1) {
                  lightboxContent($forms.outerHTML());
                } else {
                  success.call($form[0], data, formId);
                }
              },
              error: function () {
                unBlockPage();
                alert('Sorry, an error occured.');
              }
            });
            
            $('#fancybox-content').html(loadingBar());
          });

          // TODO - workaround for jquery 1.4.2 in IE7 http://bugs.jquery.com/ticket/6359
          $('#' + formId + ' input[type=submit]').live('click', function (e) {
              e.preventDefault();
              $(this).closest('form').submit();
          });
      }    
    
    function scrollToCourseType(link) {
        if (link.length === 0) {
            return;
        }

        $('#browse-courses .links a').removeClass('current');
        link.addClass('current');
        var requestedElement = $(link.attr('href'));
        $('.courses-and-units-area-of-study:visible').not(requestedElement).fadeOut();
        requestedElement.fadeIn();
    }

    function setFlashMessage($parent, message, type) {
        $parent.html('').append($('<p class="message-box ' + type + '-box">' + message + '<a class="close">&times;</a></p>'));
        $parent.find('.message-box').hide().fadeIn("fast");
    }

    function toPermalink(str) {
        return str.toLowerCase().replace(/ /g, '-');
    }

    function toPrice(float) {
        return ("$" + parseFloat(float).toFixed(2)).replace(/(\d+)(\d\d\d)\./, "$1,$2.");
    }

    function chooseTab(number) {
        $('.tabbed-info-container .tab-clickers .current').removeClass('current');
        $('.tab-clickers li:nth-child(' + number + ')').addClass('current');
        $('.tab-content').hide();
        $('.tab-content:nth-child(' + number + ')').show();
    }

    function updateTextareaMaxlength() {
        var jThis = $(this);
        var returns = jThis.val().match(/\n/g);
        if (jThis.val().length + (returns === null ? 0 : returns.length) > jThis.attr('maxlength')) {
            jThis.val(jThis.val().substring(0, jThis.attr('maxlength') - (returns === null ? 0 : returns.length)));
        }
        jThis.siblings('.maxlength').find('span').text('(currently ' + (jThis.val().length + (returns === null ? 0 : returns.length)) + '/' + jThis.attr('maxlength') + ')');
    }

    function updatePhoneNumberMandatoryDisplay() {
        var category = $('input[name=category]:checked, input[type=hidden][name=category]').val();
        var existingStudent = $('input[name=existingStudent]:checked, input[name=existingStudent][type=hidden]').val() === 'true';
        var signedIn = $('input[name=signedIn]').val() === 'true';
        var enrolled = $('input[name=enrolled]').val() === 'true';

        $('.require-phone-number').toggle(category != null && !!(category.match(/EXM1|EXM2/) ||
                ((!existingStudent || (signedIn && !enrolled)) && category.match(/ENQR|ENQ2|PAY1|PAY2|FEE1|FEE2|CRD1|CRD2|CSENQ|CSEN2/))));
    }

    function parseTabs(selector, association) {
        if (ie6() || isPreviewPage() || isPhone()) {
            return;
        }

        var container = $(selector);
        if (container.length !== 1) {
            return;
        }
        var tabHtml = ['<div class="tabbed-info-container">' +
                '<div class="navigation-siblings tab-clickers">' +
                '<ul>'];

        for (var tabName in association) {
            tabHtml.push('<li class="' + toPermalink(tabName) + '-clicker">' +
                    '<span class="sliding-top"><span class="sliding-top-inner"></span></span>' +
                    '<a href="#' + toPermalink(tabName) + '-tab">' + tabName + '</a>' +
                    '</li>');
        }

        tabHtml.push('</ul>' +
                '</div>' +
                '<div class="sliding-top-bar"><div class="sliding-top-inner-bar"></div></div>' +
                '<div class="tabbed-column-container">');

        for (var tabName in association) {
            tabHtml.push('<div id="' + toPermalink(tabName) + '-tab-content" class="tab-content" style="display: none"></div>');
        }

        tabHtml.push('</div>' +
                '<div class="sliding-bottom-bar"><div class="sliding-bottom-bar-inner"></div></div>' +
                '</div>');

        container.html(tabHtml.join(""));

        for (var tabName in association) {
            var count = 0;

            for (var i = 0; i < association[tabName].length; i++) {
                $("#tab-content-code-" + association[tabName][i]).appendTo('#' + toPermalink(tabName) + '-tab-content');
                count++;
            }
        }

        $('.tab-content').each(function (i) {
            var jThis = $(this);
            if (jThis.children().size() === 0) {
                $('.tab-clickers li:nth-child(' + (jThis.index() + 1) + ')').remove();
                jThis.remove();
            }
        });

        var tabContents = $('.tab-content');
        var tabClickers = $('.tab-clickers li a');

        tabContents.each(function (i) {
            var jThis = $(this);
            var clickersHtml = '<div class="next-and-previous-tab-clickers">';
            if (i > 0) {
                var text = $(tabClickers.get(i - 1)).text();
                clickersHtml += '<a class="previous" href="#' + toPermalink(text) + '">' + text + '</a>';
            }

            if (i < tabClickers.length - 1) {
                var text = $(tabClickers.get(i + 1)).text();
                clickersHtml += '<a class="next" href="#' + toPermalink(text) + '">' + text + '</a>';
            }

            $(clickersHtml + "</div>").appendTo(jThis);
        });

        if (window.location.hash.length > 0) {
           // First hunt for an anchor named with the hash
           var index = $("#main-content a[name=" + window.location.hash.replace(/^#/, '') + "]").closest('li, .tab-content').index();
           
           // If not found, then look for an anchor linking to it.
           if (index < 0) {
                   chooseTab(Math.max(0, $("#main-content " + window.location.hash + ", #main-content a[name=" + window.location.hash.replace(/^#/, '') + "], #main-content  a[href$=" + window.location.hash + "]").closest('li, .tab-content').index()) + 1);
           } else {
             chooseTab(index + 1);
           }
        } else {
            chooseTab(1);
        }

        container.show();
    }

    function rawNumber(elem) {
        var value;
        if(typeof(elem) === 'string') {
            value = parseFloat(elem.replace(/[^0-9.]/g, ''));
        } else {
            value = parseFloat($(elem).text().replace(/[^0-9.]/g, ''));
        }

        if (isNaN(value)) {
            return 0;
        } else {
            return value;
        }
    }

    function initialPageFocus(selector) {
        setTimeout(function () {
            $(selector).first().focus();
        }, $.browser.msie ? 250 : 0);  // Give IE a bit of time to zoom down.
    }

    function isPreviewPage() {
        return document.location.pathname.match(/(\/preview\/|\/extract\/)/);
    }

    function checkValidSession() {
        $.get(securePrefix()+'/user/fragment/user-user-id', function (data) {
            if (data.match(/^$/) || data.match(/<!DOCTYPE html PUBLIC /)) {
                if (confirm('Your session has expired. You will need to log in again. By clicking OK you will be redirected to the login page.')) {
                    document.location.href = document.location.href;
                }
            }
        });
    }

    function selectiveJoin(varargs) {
        var blerg = [];
        for (var i = 0; i < arguments.length; i++) {
            if (arguments[i]) {
                blerg.push(arguments[i]);
            }
        }
        return blerg.join(', ');
    }

    return {
        usernameSelection: function () {
            var timeout = timeouts['username-selection'];
            var jThis = $(this);

            clearTimeout(timeout);

            if (jThis.val().length == 0) {
                $('#username-selection').remove();
                return;
            }

            function setText(available, length) {
                var cssClass, text;
                if (length < 6) {
                    text = "Too short";
                    cssClass = "short-username";
                } else {
                    text = available ? "Available" : "Unavailable";
                    cssClass = available ? "good-username" : "bad-username";
                }
                $('#username-selection').text(text).removeClass("loading short-username good-username bad-username").addClass(cssClass);
            }

            if ($('#username-selection').size() === 0) {
                jThis.after("<span id='username-selection' class='inline-status'></span>");
            }

            $('#username-selection').addClass('loading').text('');

            if (!jThis.val().match(/^[0-9a-zA-Z]{6,12}$/)) {
                setText(false, jThis.val().length);
            } else {
                timeouts['username-selection'] = setTimeout(function () {
                    jQuery.getJSON(publicPrefix() + "/login/available-username/" + jThis.val(), function (data) {
                        setText(data, jThis.val().length);
                    });
                }, 200);
            }
        },
        clearSearchDefaults: function () {
            var jThis = $(this);
            var text = jThis.val();
            if (text === 'Enter a keyword' || text === 'Start typing the unit title here') {
                jThis.attr('default', jThis.val());
                jThis.val('');
            }
        },
        returnSearchDefaults: function () {
            var jThis = $(this);
            var text = jThis.val();
            if (text === '') {
                jThis.val(jThis.attr('default'));
            }
        },
        enquiryExistingStudentSelectionListener: function () {
            var selector = '.enquiry-details-form input[name=existingStudent]:checked, .enquiry-details-form input[name=existingStudent][type=hidden], .enquiry-details-form input[name=category]';

            function doUpdate() {

                var existingStudent = $('input[name=existingStudent]:checked, input[name=existingStudent][type=hidden]').val() === 'true';

                if (existingStudent) {
                    $('.oua-id-container').show();
                } else {
                    $('.oua-id-container').hide();
                }

                updatePhoneNumberMandatoryDisplay();
            }

            $(selector).live('change', doUpdate);

            $(document).ready(function() {
                doUpdate.call($(selector));
                updatePhoneNumberMandatoryDisplay();
            });
        },
        enquiryCategorySelectionListener: function () {
            var swapText = 'Unit to withdraw from: \nUnit to add: \nStudy Period/Session: \n\n';
            var enrolmentLetterText = 'Please specify years (e.g. 2006 - 2007): \n\n';

            function addSwapText(jEnquiryComment) {
                if (!jEnquiryComment.val().match(swapMatcher)) {
                    jEnquiryComment.val(swapText + jEnquiryComment.val());
                }
            }

            function removeUneditedTextWeAdded(jEnquiryComment) {
                var value = jEnquiryComment.val();

                if (value.replace(/^\s+|\s+$/, '') === $("#initialComment").val().replace(/^\s+|\s+$/, '')) {
                    jEnquiryComment.val('');
                }

                if (value.match(enrolmentLetterMatcher) || value.match(swapMatcher)) {
                    jEnquiryComment.val(value.replace(swapText, '').replace(enrolmentLetterText, ''));
                }
            }

            function addEnrolmentLetterText(jEnquiryComment) {
                if (!jEnquiryComment.val().match(enrolmentLetterMatcher)) {
                    jEnquiryComment.val(enrolmentLetterText + jEnquiryComment.val());
                }
            }

            $('.enquiry-details-form input[name=category], .enquiry-details-form select#category, .enquiry-details-form input[name=statementDeliveryMethod]').live('change', function (e) {
                var jEnquiryComment = $("#enquiryComment");
                var value = $('.enquiry-details-form input[name=category]:checked, .enquiry-details-form select#category').val();

                removeUneditedTextWeAdded(jEnquiryComment);
                updatePhoneNumberMandatoryDisplay();
                $('#unit-swap-message').hide();
                $('#download-statement-message').hide();
                $('#mailing-address-container').hide();

                if (value.match(/SWAP|SWP2/)) {
                    addSwapText(jEnquiryComment);
                    $('#unit-swap-message').show();
                } else if (value.match(/INFO|INF2/)) {
                    addEnrolmentLetterText(jEnquiryComment);
                    $('#download-statement-message').show();
                    if ($("input[name=statementDeliveryMethod]:checked").val() === 'Email') {
                        $('#mailing-address-container').hide();
                    } else {
                        $('#mailing-address-container').show();
                    }
                } else {
                    if (jEnquiryComment.val().match(/^\s*$/)) {
                        jEnquiryComment.val($("#initialComment").val());
                    }
                }
            });

            $(function () {
                $('.enquiry-details-form input[name=category]:checked').change();
                setTimeout(function () {
                    enableFaqUpdate = true;
                }, 1);
            });
        },
        textareaMaxlengthListener: function () {
            $('textarea[maxlength]').live('change keyup paste', updateTextareaMaxlength);
            $(function () {
                $('textarea[maxlength]').change();
            });
        },
        courseBrowseListener: function () {
            $('#browse-courses .links a').live('click', function (e) {
                scrollToCourseType($(this));
                e.preventDefault();
            });
        },
        courseBrowseInit: function () {
            setTimeout(function () {
                if ($('#browse-courses .links a.current').length == 0) {
                    var matchingLink = $('#browse-courses .links a[href=' + window.location.hash + ']');
                    if (window.location.hash.length > 0 && matchingLink.length > 0) {
                        scrollToCourseType(matchingLink);
                    } else {
                        scrollToCourseType($('#browse-courses .links a').first());
                    }
                }
            }, 100);
        },
        expandingCourseStreamListener: function () {
            if (isPreviewPage() || isPhone()) {
                return;
            }

            $('.stream-details-container.expanded caption').live('click', function () {
                var jThis = $(this);
                var jContainer = jThis.parents('.stream-details-container');
                jContainer.removeClass('expanded');
                jContainer.animate({
                    height: jThis.outerHeight()
                }, function () {
                    jContainer.addClass('closed');

                    $('.collapse-all').hide();
                    $('.expand-all').show();
                });
            });
            $('.stream-details-container.closed caption').live('click', function () {
                var jThis = $(this);
                var jContainer = jThis.parents('.stream-details-container');
                jContainer.removeClass('closed');
                jContainer.css('height', jThis.outerHeight());
                jContainer.animate({
                    height: jContainer.find('table').outerHeight() + ($.browser.mozilla ? jThis.innerHeight() : 0)
                }, function () {
                    jContainer.addClass('expanded');

                    if ($('.stream-details-container.closed').length === 0) {
                        $('.collapse-all').show();
                        $('.expand-all').hide();
                    }
                }
                        );
            });

            $(document).ready(function () {
                var addUnitsContainer = $('.unit-selector').not('.search-results-container .unit-selector');
                var expandCollapseMarkup = '<a style="display: none" class="expand-all" href="#STRUCTURE">Expand all</a>' +
                        '<a class="collapse-all" href="#STRUCTURE">Collapse all</a>';
                if (addUnitsContainer.size() > 0) {
                    addUnitsContainer.first().append(expandCollapseMarkup);
                } else {
                    $('.stream-details-container').first().before(expandCollapseMarkup);
                }

            });
            $('.expand-all').live('click', function () {
                $('.closed caption').click();
            });
            $('.collapse-all').live('click', function () {
                $('.expanded caption').click();
            });
        },
        jsParameterLinkListener: function () {
            $('a[href^='+publicPrefix()+'/enquiry/query], a[href^='+publicPrefix()+'/home/enquiry/query], a[href^='+securePrefix()+'/enquiry/query], a[href^='+securePrefix()+'/home/enquiry/query]').live('click mouseenter', function () {
                var jThis = $(this);
                jThis.attr('href', jThis.attr('href').replace(/\?jsEnabled=true&/, '?').replace(/(\?|#|$)/, '?jsEnabled=true&'));
            });
        },
        courseTabsInit: function () {
            // Because we didn't want to stop the caching of the course fragments.
            if (window.location.hash === "#unit-choice-error") {
                $("#js-shortlist-error").html(
                        '<p class="binding-errors" id="unit-choice-error">' +
                                '  <span class="form-error">Please select at least one unit to add to your shortlist.</span>' +
                                '</p>');
            }

            parseTabs("#tabbed-course-content-container", ((typeof(courseTabsOverride) !== 'undefined' && Object.size(courseTabsOverride) > 0) ? courseTabsOverride : {
                'Overview': ["OVERVIEW", "DESCRIPTION", "OBJECT", "OUTCOMES", "PROFREC", "RELATED"],
                'Preview': ["VIDEO"],
                'Course requirements': ["ELIGIBLE", "APPLY", "OPEN", "SPECIAL", "CAMPUS", "PLACE", "STUDY", "ASSESS"],
                'Completing the course': ["AWARD", "TRANS", "EXIT", "RPL", "RPC", "REGISTER", "STATE"],
                'Course units': ["STUDYPAT", "STRUCTURE"],
                'Cwlth supported place':["CSP-ABT","CSP-BEFR","CSP-ELIG","CSP-ADMT","CSP-PRCE","CSP-STLV"]
            }));

            if (window.location.hash === "#unit-choice-error") {
                $(window).scrollTop($(window.location.hash).offset().top - 10);
            }
        },
        unitTabsInit: function () {
            var unitContentContainerTabs = {
                'Overview': ["OVERVIEW", "DESCRIPTION", "OUTCOMES", "ASSESSMENT", "TEXTBOOKS"],
                'Preview': ["VIDEO"],
                'Before you start': ["RESTRICTION", "PREREQUISITES", "SPECIAL"],
                'In this unit': ["TOPICS", "STUDY"],
                'Relevant courses': ["RELEVANT"]
            };


            // TODO:behrangs: Remove When Feature Goes Live
            //noinspection JSUnresolvedVariable
            if (typeof textBooksTabFeatureEnabled != "undefined" && textBooksTabFeatureEnabled) {
                // convert the textbooks section into its own tab
                var textBooksTab = unitContentContainerTabs['Overview'].pop();
                unitContentContainerTabs['Textbooks'] = [textBooksTab];
            }

            parseTabs("#tabbed-unit-content-container", unitContentContainerTabs);
        },
        megaDropdownInit: function () {

            if ($('#navigation .primary').size() === 0 || isPhone()) {
                return;
            }

            menuOptions = [
                [
                    [
                        [null, "Site support",
                            [
                                ["/home/site-tools/contact-us", "Contact us"],
                                ["/student-admin-and-support/key-dates/key-dates", "Key dates"],
                                ["/future-students/financial-assistance/fee-help", "FEE-HELP"],
                                ["/home/faqs/faqs", "FAQs"],
                                [publicPrefix()+"/home/site-tools/glossary", "Glossary"],
                                ["/student-admin-and-support/student-support-services/getting-advice", "Getting advice"],
                                ["/student-admin-and-support/student-support-services/student-support-services", "Student support services"]
                            ]
                        ],
                        ["/about-us/our-providers", "Shareholder providers",
                            [
                                ["/courses-and-units/curtn", "Curtin University"],
                                ["/courses-and-units/grf", "Griffith University"],
                                ["/courses-and-units/maq", "Macquarie University"],
                                ["/courses-and-units/mon", "Monash University"],
                                ["/courses-and-units/rmit", "RMIT University"],
                                ["/courses-and-units/swin", "Swinburne University of Technology"],
                                ["/courses-and-units/unisa", "University of South Australia"]
                            ]
                        ],
                        [null, "Legal",
                            [
                                ["/home/legal/copyright", "Copyright"],
                                ["/home/legal/disclaimer", "Disclaimer"],
                                ["/home/legal/privacy-policy", "Privacy policy"],
                                ["/home/legal/student-declaration", "Student declaration"],
                                ["/home/legal/terms-and-conditions", "Terms & conditions"]
                            ]
                        ]
                    ],
                    ['/about-us/student-news/news-page', 'Click here to view the latest OUA news']
                ],
                [
                    [
                        ["/future-students/getting-started", "Getting started",
                            [
                                ["/future-students/getting-started/why-oua", "Why OUA?"],
                                ["/future-students/getting-started/preparing-for-study", "Preparing for study"],
                                ["/future-students/getting-started/career-advice-online-readiness-tool", "Career advice & online readiness tool"],
                                ["/future-students/getting-started/levels-and-prerequisites", "Levels & prerequisites"],
                                ["/future-students/getting-started/how-to-enrol-online", "How to enrol online"],
                                ["/future-students/getting-started/student-support-services", "Student support services"],
                                ["/future-students/getting-started/study-resources", "Study resources"],
                                ["/future-students/getting-started/study-materials", "Study materials"],
                                ["/future-students/getting-started/study-load", "Study load"],
                                ["/future-students/getting-started/computer-requirements", "Computer requirements"],
                                ["/future-students/getting-started/credit-for-previous-study", "Credit for previous study"]
                            ]
                        ],
                        ["/future-students/financial-assistance", "Financial assistance",
                            [
                                ["/future-students/financial-assistance/fee-help", "FEE-HELP"],
                                ["/future-students/financial-assistance/hecs-help", "HECS-HELP"],
                                ["/future-students/financial-assistance/vet-fee-help", "VET FEE-HELP"],
                                ["/future-students/financial-assistance/scholarships", "Scholarships"],
                                ["/future-students/financial-assistance/travel-subsidies", "Travel subsidies"],
                                ["/future-students/financial-assistance/government-allowances", "Government allowances"]
                            ]
                        ],
                        ["/future-students/Commonwealth-supported-place-students", "Commonwealth supported students",
                            [
                                ["/future-students/Commonwealth-supported-place-students/about-commonwealth-supported-places", "About Commonwealth supported places"],
                                ["/future-students/Commonwealth-supported-place-students/pricing-and-payment-options", "Pricing and payment options"],
                                ["/future-students/Commonwealth-supported-place-students/how-to-apply", "How to apply"],
                                ["/future-students/Commonwealth-supported-place-students/withdrawals-and-special-circumstances", "Withdrawal and special circumstances"]
                            ]
                        ],
                        [],
                        ["/future-students/international-students", "International students",
                            [
                                ["/future-students/international-students/benefits-and-challenges", "Benefits & challenges"],
                                ["/future-students/international-students/australians-overseas", "Australians overseas"],
                                ["/future-students/international-students/english-proficiency", "English proficiency"],
                                ["/future-students/international-students/international-student-fees", "International student fees"],
                                ["/future-students/international-students/international-exams", "International exams"],
                                ["/future-students/international-students/international-textbooks", "International textbooks"]
                            ]
                        ],
                        ["/future-students/fees-and-charges", "Fees & charges",
                            [
                                ["/future-students/fees-and-charges/unit-fees", "Unit fees"],
                                ["/future-students/fees-and-charges/administration-fees", "Administration fees"],
                                ["/future-students/fees-and-charges/payment-options", "Payment options"]
                            ]
                        ]
                    ],
                    ['/about-us/who-we-are/student-video-testimonials', 'Click here to view student video testimonials']
                ],
                [
                    [
                        ["/courses-and-units/preparatory", "Preparatory",
                            [
                                ["/courses-and-units/preparatory/unilearn-bridging-units", "UNILEARN foundation units"],
                                ["/courses-and-units/preparatory/enabling-units", "Enabling units"],
                                ["/courses-and-units/preparatory/english-language-test-preparation", "English language test preparation"],
                                ["/courses-and-units/preparatory/learning-skills-units", "Learning skills units"],
                                ["/courses-and-units/preparatory/suggested-first-units", "Suggested first units"]
                            ]
                        ],
                        ["/courses-and-units/bus", "Business",
                            [
                                ["/courses-and-units/bus/ugrd", "Undergraduate"],
                                ["/courses-and-units/bus/pgrd", "Postgraduate"],
                                ["/courses-and-units/bus/tafe", "TAFE"]
                            ]
                        ],
                        ["/courses-and-units/it", "IT",
                            [
                                ["/courses-and-units/it/ugrd", "Undergraduate"],
                                ["/courses-and-units/it/pgrd", "Postgraduate"],
                                ["/courses-and-units/it/cpe", "Professional"]
                            ]
                        ],
                        ["/courses-and-units/arts", "Arts & humanities",
                            [
                                ["/courses-and-units/arts/ugrd", "Undergraduate"],
                                ["/courses-and-units/arts/pgrd", "Postgraduate"],
                                ["/courses-and-units/arts/nona", "Non-award"],
                                ["/courses-and-units/arts/tafe", "TAFE"]
                            ]
                        ],
                        ["/courses-and-units/edu", "Education",
                            [
                                ["/courses-and-units/edu/ugrd", "Undergraduate"],
                                ["/courses-and-units/edu/pgrd", "Postgraduate"],
                                ["/courses-and-units/edu/tafe", "TAFE"]
                            ]
                        ],
                        ["/courses-and-units/law", "Law & justice",
                            [
                                ["/courses-and-units/law/ugrd", "Undergraduate"],
                                ["/courses-and-units/law/pgrd", "Postgraduate"]
                            ]
                        ],
                        ["/courses-and-units/tafe", "TAFE", []],
                        ["/courses-and-units/health", "Health",
                            [
                                ["/courses-and-units/health/ugrd", "Undergraduate"],
                                ["/courses-and-units/health/pgrd", "Postgraduate"],
                                ["/courses-and-units/health/tafe", "TAFE"]
                            ]
                        ],
                        ["/courses-and-units/sci", "Science & engineering",
                            [
                                ["/courses-and-units/sci/ugrd", "Undergraduate"],
                                ["/courses-and-units/sci/pgrd", "Postgraduate"],
                                ["/courses-and-units/sci/nona", "Non-award"]
                            ]
                        ]
                    ],
                    ['/home/search/advanced-search', 'Click here to search for units and courses']
                ],
                [
                    [
                        ["/student-admin-and-support/student-support-services", "Student support services",
                            [
                                ["/student-admin-and-support/student-support-services/getting-advice", "Getting advice"],
                                ["/student-admin-and-support/student-support-services/career-resources", "Career resources"],
                                ["/student-admin-and-support/student-support-services/disability-support", "Disability support"],
                                ["/student-admin-and-support/student-support-services/special-circumstances", "Special circumstances"],
                                ["/student-admin-and-support/student-support-services/student-counselling", "Student counselling"],
                                ["/student-admin-and-support/student-support-services/student-card", "Student card"],
                                ["/student-admin-and-support/student-support-services/complaints-management", "Complaints management"]
                            ]
                        ],
                        ["/student-admin-and-support/changing-your-study", "Changing your study",
                            [
                                ["/student-admin-and-support/changing-your-study/swapping-units", "Swapping units"],
                                ["/student-admin-and-support/changing-your-study/withdrawing", "Withdrawing"]
                            ]
                        ],
                        ["/student-admin-and-support/exams-and-results", "Exams & results",
                            [
                                ["/student-admin-and-support/exams-and-results/exam-overview", "Exams overview"],
                                ["/student-admin-and-support/exams-and-results/exam-policies-and-procedures", "Exams policies & procedures"],
                                ["/student-admin-and-support/exams-and-results/exam-key-dates", "Exams key dates"],
                                ["/student-admin-and-support/exams-and-results/getting-results", "Getting results"]
                            ]
                        ],
                        [],
                        ["/student-admin-and-support/study-resources", "Study resources",
                            [
                                ["/student-admin-and-support/study-resources/textbooks", "Textbooks"],
                                ["/student-admin-and-support/study-resources/help-with-assignments", "Help with assignments"],
                                ["/student-admin-and-support/study-resources/academic-writing-guides", "Academic writing guides"]
                            ]
                        ],
                        ["/student-admin-and-support/key-dates", "Key dates", [] ]
                    ],
                    ['/student-admin-and-support/key-dates', 'Click here to view key study dates']
                ],
                [
                    [
                        ["/about-us/who-we-are", "Who we are",
                            [
                                ["/about-us/who-we-are/open-learning-australia", "Open Learning Australia"],
                                ["/about-us/who-we-are/our-commitment", "Our commitment"],
                                ["/about-us/who-we-are/elite-athlete-friendly", "Elite athlete friendly"],
                                ["/about-us/who-we-are/student-video-testimonials", "Student video testimonials"],
                                ["/about-us/who-we-are/corporate-partnerships", "Corporate partnerships"]
                            ]
                        ],
                        ["/about-us/our-providers", "Our providers",
                            [
                                ["/about-us/our-providers/shareholder-providers", "Shareholder providers"],
                                ["/about-us/our-providers/other-providers", "Other providers"]
                            ]
                        ],
                        ["/about-us/student-news", "Student news",
                            [
                                ["/about-us/student-news/news-page", "News"]
                            ]
                        ],
                        [],
                        [],
                        ["/about-us/media-centre", "Media centre",
                            [
                                ["/about-us/media-centre/media-releases", "Media releases"],
                                ["/about-us/media-centre/media-kit", "Media kit"],
                                ["/about-us/media-centre/annual-reports", "Annual reports"],
                                ["/about-us/media-centre/images-graphics", "Images & graphics"]
                            ]
                        ],
                        [ "/about-us/jobs", "Jobs", [] ],
                        ["/about-us/governance", "Governance",
                            [
                                ["/about-us/governance/senior-staff", "Senior staff"],
                                ["/about-us/governance/board-of-directors", "Board of directors"]
                            ]
                        ]
                    ],
                    ['/about-us/media-centre/media-releases', 'Click here to view latest media releases']
                ],
                [
                    [
                        [securePrefix()+"/my-oua/manage-enrolment", "Manage enrolment",
                            [].concat((enrolmentResignEnabled() ?
                                    [
                                        ["/study-cart-and-wishlist", "Study Cart and wishlist"]
                                    ] :
                                    [
                                        ["/my-oua/manage-enrolment/shortlist", "Shortlist"],
                                        [securePrefix()+"/my-oua/manage-enrolment/shopping-cart", "Enrolment shopping cart"]
                                    ]))
                                    .concat([
                                [securePrefix()+(enrolmentResignEnabled() ? "/my-oua/manage-enrolment/make-a-payment" : "/my-oua/manage-enrolment/account-summary"), "Make a payment"],
                                [securePrefix()+"/my-oua/manage-enrolment/view-hecs-status", "View HECS Status"],
                                [securePrefix()+"/my-oua/manage-enrolment/enrolment-statement-list", "View or request enrolment statement"],
                                [securePrefix()+"/my-oua/manage-enrolment/payment-statement-list", "View or request payment statement"],
                                [securePrefix()+"/my-oua/manage-enrolment/payment-receipts", "View payment receipts"],
                                [securePrefix()+"/my-oua/manage-enrolment/credit-adjustments", "View credit adjustments"],
                                [securePrefix()+"/my-oua/manage-enrolment/fee-help-ecan", "FEE-HELP eCAN"]
                            ])
                        ],
                        [securePrefix()+"/my-oua/manage-your-study", "Manage your study",
                            [
                                [securePrefix()+"/my-oua/manage-your-study/active-units", "Active units"],
                                [securePrefix()+"/my-oua/manage-your-study/book-exam", "Book exam"],
                                [securePrefix()+"/my-oua/manage-your-study/student-community", "Student community (Discussion forums)"],
                                [securePrefix()+"/my-oua/manage-your-study/contact-list", "Study contacts"],
                                [securePrefix()+"/my-oua/manage-your-study/academic-results", "View or request record of results"],
                                [securePrefix()+"/my-oua/manage-your-study/withdrawal-page", "Withdraw"]
                            ]
                        ],
                        [securePrefix()+"/my-oua/orientation", "Orientation",
                            [
                                [securePrefix()+"/my-oua/orientation/orientation-welcome", "Welcome"],
                                [securePrefix()+"/my-oua/orientation/orientation-plan-ahead", "Plan ahead"],
                                [securePrefix()+"/my-oua/orientation/orientation-getting-ready", "Getting ready"],
                                [securePrefix()+"/my-oua/orientation/orientation-supporting-your-learning", "Supporting your learning"],
                                [securePrefix()+"/my-oua/orientation/orientation-assessment-tips", "Assessment tips"],
                                [securePrefix()+"/my-oua/orientation/orientation-what-next", "What next?"]
                            ]
                        ]
                    ],
                    ['/login/new-login', "Please sign in to view My OUA features"]
                ]
            ];

            var loginForm = '<form method="post" action="https://' + document.location.host + ''+publicPrefix()+'/login/do-login" name="login-form" id="loginForm">' +
                    '<div class="form-field ">' +
                    '<label class="area-label" for="username">Username</label>' +
                    '<input type="text" maxlength="100" size="26" value="" class="field-text focus-on-load " name="username" id="username">' +
                    '</div>' +
                    '<div class="form-field">' +
                    '<label class="area-label" for="password">Password</label>' +
                    '<input type="password" maxlength="100" size="26" value="" class="field-text " name="password" id="password">' +
                    '</div>' +
                    '<div class="submit-options">' +
                    '<input type="submit" class="" name="" value="Sign in">' +
                    '</div>' +
                    '<input type="hidden" value="'+securePrefix()+'/my-oua" name="success" id="success">' +
                    '<input type="hidden" value="'+publicPrefix()+'/login/form" name="failure" id="failure">' +
                    '</form>';

            var socialMedia = '<ul><li><a href="http://www.facebook.com/OpenUnisAu" ><span class="mm_facebook_icon">&nbsp;</span><span>Facebook</span> </a></li>' +
					            '<li><a href="http://twitter.com/OpenUnisAU"><span class="mm_twitter_icon">&nbsp;</span><span>Twitter</span></a></li>' +
					            '<li><a href="http://www.youtube.com/openuniversitiesau"><span class="mm_youtube_icon">&nbsp;</span><span>YouTube</span></a></li>' +
					            '<li><a href="'+publicPrefix()+'/home/rss-feeds/"><span class="mm_rss_icon">&nbsp;</span><span>RSS</span></a></li>' +
					            '</ul>';
            
            var overDropdown = false;
            var overNav = false;

            $('.mm-dropdown').live('mouseenter',
                    function () {
                        overDropdown = true;
                    }).live('mouseleave', function () {
                overDropdown = false;
                if (!overNav) {
                    $('.mm-dropdown').hide();
                    $('.dropdown').removeClass('dropdown');
                }
            });

            function createNavLink(link, text) {
                return "<a href='" + (link.match(/^\/(secure|public|career-advice)/) ? '' : publicOrSecure()) + link + "'>" + text + "</a>";
            }

            function subnavToHtml(subnav) {
                var html = "";

                if (subnav.length > 0) {
                    html += "<ul>";
                    for (var j = 0; j < subnav.length; j++) {
                        html += "<li>";
                        html += createNavLink(subnav[j][0], subnav[j][1]);
                        html += "</li>";
                    }

                    html += "</ul>";
                }

                return html;
            }

            function navToHtml(data) {
                if (data.length === 0) {
                    return "";
                }
                var html = "<h3>" + (data[0] === null ? data[1] : createNavLink(data[0], data[1])) + "</h3>";
                html += subnavToHtml(data[2]);
                return html;
            }

            function hoverIntentOn() {
                overNav = true;
                overDropdown = false;
                var $this = $(this);
                var index = $this.closest('li').index();
                var data = menuOptions[index];

                $('.mm-dropdown').hide();

                if (data.length > 0) {
                    var nav = data[0];
                    var helpText = "<a href=''>" + data[1] + "</a>";

                    $mm = $('#mm-dropdown-' + index);

                    if ($mm.size() === 0) {
                        $('body').prepend("<div id='mm-dropdown-" + index + "' class='mm-dropdown'><div class='mm-content'><div class='clearfix mm-content-inner'></div></div><div class='mm-footer'>" + createNavLink(data[1][0], data[1][1]) + "</div></div>");

                        $mm = $('#mm-dropdown-' + index);
                        var content = "";
                        var columnCount = 3;
                        for (var column = 0; column < columnCount; column++) {
                            content += "<div class='column'><div class='column-inner'>";

                            for (var i = 0; i < nav.length; i++) {

                                if (i % columnCount === column) {
                                    content += navToHtml(nav[i]);
                                }
                            }

                            content += "</div></div>";
                        }

                        if (typeof socialMediaMMFeatureEnabled != "undefined" && socialMediaMMFeatureEnabled) {
                            if (index === 0) {
                                content += '<div class="column"><div class="column-inner"><h3>Follow us</h3>';
                                content += socialMedia;
                                content += "</div></div>";
                            }
                        }

                        if (index === 5) {
                            if (!document.location.pathname.match(/^\/secure\//)) {
                                content += '<div class="column"><div class="column-inner"><h3><a href="'+securePrefix()+'/my-oua">Sign in</a></h3>';
                                content += loginForm;
                                content += subnavToHtml([
                                    ["/login/forgotten-username-or-password#forgottenUsernameForm", "Forgotten username?"],
                                    ["/login/forgotten-username-or-password#forgottenPasswordForm", "Forgotten password?"],
                                    ["/login/new-register", "Register"],
                                    [securePrefix()+"/my-oua/manage-personal-details/update-profile", "Update profile" ],
                                    [securePrefix()+"/my-oua/manage-personal-details/change-password", "Change password", [] ],
                                    [securePrefix()+"/my-oua/student-updates", "Student updates", [] ]
                                ]);
                                content += "</div></div>";
                            } else {
                                content += '<div class="column"><div class="column-inner"><h3><a href="'+securePrefix()+'/my-oua">My OUA</a></h3>';
                                content += subnavToHtml([
                                    [securePrefix()+"/my-oua/manage-personal-details/update-profile", "Update profile" ],
                                    [securePrefix()+"/my-oua/manage-personal-details/change-password", "Change password", [] ],
                                    [securePrefix()+"/my-oua/manage-applications/applications", "Applications"],
                                    [securePrefix()+"/my-oua/student-updates", "Student updates", [] ],
                                    [publicPrefix()+"/login/do-logout", "Logout" ]
                                ]);


                                $mm.find('.mm-footer').html("<a href='"+securePrefix()+"/my-oua/manage-personal-details/update-profile'>Click here to manage personal details</a>");
                            }
                        }
                    }

                    $mm.find('.mm-content, .mm-footer').bgiframe();

                    $mm.find('.mm-content-inner').html(content);

                    var offset = $this.offset();

                    $this.parent().addClass('dropdown');

                    var endOfNav = $('#navigation .primary ul').offset().left + $('#navigation .primary ul').width();

                    var columnCount = $mm.find('.column').size();

                    $mm.css({top: offset.top + $this.parent().height() + 'px', left: Math.min(endOfNav - columnCount * 200, offset.left - 1) + 'px'}).width((columnCount * 200) + 'px');

                    $mm.show();

                }
            }

            $('#navigation .primary a').hoverIntent(hoverIntentOn,
                    function () {
                        overNav = false;
                        if (!overDropdown) {
                            $('.mm-dropdown').hide();
                            $('.dropdown').removeClass('dropdown');
                        }
                    }).each(function () {
                this.hoverIntentOn = hoverIntentOn;
            }); // To trigger in testing.
        },
        tabListeners: function () {
            if (ie6() || isPhone()) {
                return;
            }

            $('.detail a[href^=#]').live('click', function (e) {
                if ($('.tab-content').size() > 0) {
                    var reference = $(this).attr('href').replace(/^#/, '');
                    var contentAnchor = $('#tab-content-code-' + reference + ', .tab-content a[name=' + reference + '], #' + reference).first();

                    if (contentAnchor.length > 0 && contentAnchor.closest('.tab-content').length > 0) {
                        chooseTab(contentAnchor.closest('.tab-content').index() + 1);
                        $(window).scrollTop(contentAnchor.offset().top - 50);
                        e.preventDefault();
                    }
                }
            });

            $('.tab-clickers a').live('click', function () {
                chooseTab($(this).closest('li').index() + 1);
            });
            $('.next-and-previous-tab-clickers a.next').live('click', function() {
                chooseTab($(this).closest('.tab-content').index() + 2);
                $(window).scrollTop($('.tabbed-info-container').offset().top - 10);
            });
            $('.next-and-previous-tab-clickers a.previous').live('click', function() {
                chooseTab($(this).closest('.tab-content').index());
                $(window).scrollTop($('.tabbed-info-container').offset().top - 10);
            });
        },
        toggleListeners: function () {
            $('.toggle-area .toggle-on a').live('click', function (e) {
                var $this = $(this);
                var $toggleArea = $this.closest('.toggle-area');
                if(!$toggleArea.hasClass('animating')) {
                    $this.parent().hide();
                    $toggleArea.addClass('animating');
                    $toggleArea.find('.on-area').fadeIn(300, function () {
                        $toggleArea.removeClass('animating');
                    }).find('input:visible:first').focus();
                    
                    $toggleArea.find('input:hidden').val('true');

                    $toggleArea.find('input:visible').removeAttr("disabled");
                }

                e.preventDefault();
            });

            $('.toggle-area .toggle-off a').live('click', function (e) {
                var $this = $(this);
                var $toggleArea = $this.closest('.toggle-area');
                if(!$toggleArea.hasClass('animating')) {
                    $toggleArea.addClass('animating');

                    $toggleArea.find('.on-area').fadeOut(300, function () {
                        $toggleArea.removeClass('animating').find('.toggle-on').fadeIn(300);
                    });

                    $toggleArea.find('input:hidden').val('false');
                    $toggleArea.find('input:visible').attr("disabled", true);
                }

                e.preventDefault();
            });

            var toggleSpeed = 0;

            liveAndOnPageLoad('.js-toggle-when .js-toggle-when-value :input', 'change keyup', function() {
                var $this = $(this);
                var $topContainer = $this.closest(".js-toggle-when");
                var $toggleContainer = $this.closest('.js-toggle-when').find("> .js-toggle-when-container");

                var showValue = $topContainer.attr('data-show-value');
                var hideValue = $topContainer.attr('data-hide-value');

                var $orWhen = $($topContainer.attr('data-or-when'));
                var orWhen = false;

                if($orWhen.size() > 0) {
                    var goodOrValues = $topContainer.attr('data-or-when-value').split(',');
                    orWhen = $(goodOrValues).index($orWhen.val()) !== -1;
                }

                var showValueSplit = showValue ? showValue.split(',') : [];
                var hideValueSplit = hideValue === "" ? [''] : (hideValue ? hideValue.split(',') : []);
                var actualValue;

                if($this.attr('type') === 'checkbox' || $this.attr('type') === 'radio') {
                    actualValue = $this.closest('.js-toggle-when-value').find(':checked').val();
                } else {
                    actualValue = $this.val();
                }

                if(($orWhen.size() > 0 && orWhen) || (!!showValue && indexOf(showValueSplit, actualValue) !== -1) || (hideValue !== undefined && indexOf(hideValueSplit, actualValue) === -1)) {
                    $toggleContainer.not(':visible').slideDown(toggleSpeed);
                } else {
                    $toggleContainer.filter(':visible').slideUp(toggleSpeed);
                }
            });

            $(function () {
                $('.toggle-area input[type!=hidden]:hidden').attr("disabled", true);

                setTimeout(function () {
                    toggleSpeed = 250;
                },10);
            });
        },
        parsePasswordFields: function () {
            $('.strength-evaluated-password').each(function () {
                var $this = $(this);

                var $username = $this.closest('form, fieldset').find('input[name$=username]');
                var usernameSelector = "[id='" + $username.attr('id') + "']";

                $this.passStrength({
                    userid: usernameSelector,
                    baseStyle: "inline-status",
                    shortPass: "short-password",
                    badPass: "bad-password",
                    goodPass: "good-password",
                    strongPass: "strong-password"
                });
            });
        },
        focusOnLoad: function () {
            setTimeout(function() {
                if (window.location.hash) {
                    initialPageFocus('form' + window.location.hash + ' input:visible');
                    setTimeout(function () {
                        $(".accordion > h2" + window.location.hash.replace(/\?.*/, "")).trigger('click');
                    }, 1);
                } else if ($('.focus-on-load').size() > 0) {
                    initialPageFocus('.focus-on-load');
                }
            });
        },
        instantSearchListeners: function () {

            var slideSpeed = 250;
            var displayed = [];
            var forceHide = [];

            function addHelperElements() {
                var advanced = $('#advanced-search').size();
                $('.facet-form fieldset, #advanced-search .always-present').each(function () {
                    if (!advanced) {
                        $(this).find('.facet-list-container').before('<a href="javascript:void(0)" class="facetSelectionDescription"><span class="white-bar-top"><span class="white-bar-top-inner"></span></span><span class="text"></span><span class="white-bar-bottom"><span class="white-bar-bottom-inner"></span></span></a>');
                    }

                    $(this).find('ol li:first').before('<li class="all-selection-item clearfix"><label><input type="checkbox" class="all-selection" /> <span>All</span></label></li>');
                });

                updateFacetDescriptions();
            }

            function setFacetHash() {
                var names = [];

                $('#search-faceting').find('input[name]:checked').each(function () {
                    names.push($(this).attr('name') + '=' + $(this).val());
                });

                window.location.hash = names.join('&');
            }

            function applyFacetHash() {
                if (window.location.hash && window.location.href.indexOf('unit-choice-error') == -1 && $('#search-faceting').size() > 0) {
                    var values = window.location.hash.slice(1).split('&');
                    $('#search-faceting input[type=checkbox]').removeAttr('checked');

                    for (var i = 0; i < values.length; i++) {
                        var split = values[i].split('=');
                        var checkbox = $('input[name=' + split[0] + '][value=' + split[1] + ']');

                        checkbox.attr('checked', true);

                        var id = checkbox.parents('fieldset').attr('id');

                        if (id && displayed.indexOf(id) === -1) {
                            displayed.push(id);
                        }
                    }

                    $('#search-faceting input[type=checkbox]').first().change();
                }
            }

            function selectionsIn(elem) {
                var all = elem.find('.all-selection:checked').size() === 1;
                return elem.find('input[type="checkbox"]:checked').size() - (all ? 1 : 0);
            }

            function updateFacetDescriptions() {
                $('.facet-form fieldset, #advanced-search .always-present').each(function () {
                    var jThis = $(this);
                    var all = jThis.find('.all-selection:checked').size() == 1;
                    var checkedElements = jThis.find('input[type="checkbox"]:checked');
                    var checked = checkedElements.size() - (all ? 1 : 0);
                    var total = jThis.find('input[type="checkbox"]').size() - 1;
                    var descriptionLabel = jThis.find('.facetSelectionDescription .text');

                    if (total === checked || checked === 0) {
                        descriptionLabel.html('').text('All');
                        jThis.find('input[type=checkbox]').removeAttr('checked');
                        jThis.find('.all-selection').attr('checked', true);
                    } else {
                        if (checked === 1) {

                            var text = jThis.find('input[type="checkbox"]:checked').closest('li').find('label').text().replace(/ \(\d+\).*/, '').replace(jThis.find('input[type="checkbox"]:checked').closest('li').find('.helper-text').text(), '');
                            if (checkedElements.parents('.sub-faceting ol').size() > 0) {
                                text = checkedElements.parents('.sub-faceting').find('label').first().text().replace(/ \(\d+\).*/, '').replace(" any", "") + " " + text;
                            }

                            descriptionLabel.html('').text(text);
                        } else {
                            descriptionLabel.html('').text('Multiple');
                        }
                        jThis.find('.all-selection').removeAttr('checked');
                    }
                });
            }

            function ajaxSubmitSearch(currentId) {
                $('.search-results-container').fadeOut(250);

                $.get($('.facet-form').attr('action').replace(/\/home\/search-results/, '\/search/fragment/results').replace(/($|\?).*/, '/plain?') + $('.facet-form').serialize(), function (data) {
                    var providerScrollTop = $('#provider-facets ol').scrollTop();
                    var windowScroll = $(window).scrollTop();

                    $('#results-area').html(data);

                    for (var i = 0; i < displayed.length; i++) {
                        if ((forceHide.indexOf(displayed[i]) == -1 && selectionsIn($('#' + displayed[i])) >= 2) || displayed[i] === currentId) {
                            $('#' + displayed[i] + " .facet-list-container").show().closest('fieldset').addClass('shown');
                        }
                    }

                    setFacetHash();

                    addHelperElements();
                    $('.results-description, .multiple-search-result-container, .single-type-search-results ').hide().fadeIn(250);

                    $('#provider-facets ol').scrollTop(providerScrollTop);
                    $(window).scrollTop(windowScroll);
                });
            }

            function hideZeroOrSingleSelectionFacets(e) {
                $('.facet-form fieldset').each(function () {
                    if ((selectionsIn($(this)) <= 1 && displayed.indexOf($(this).attr('id')) >= 0)) {
                        $(this).find('.facet-list-container').stop().slideUp(slideSpeed).closest('fieldset').removeClass('shown');
                        displayed.splice(displayed.indexOf($(this).attr('id')), 1);
                    }
                });
            }

            $('body').live('click', function(e) {
                if ($(e.target).parents('.facet-form fieldset').size() == 0) {
                    hideZeroOrSingleSelectionFacets(e);
                }
            });

            $('.facet-form input[type=checkbox]').live('change', function () {

                var jThis = $(this);

                if (jThis.hasClass('all-selection')) {
                    jThis.closest('fieldset, .always-present').find('input[type=checkbox][name]').removeAttr('checked');
                }

                if ($("input[type='checkbox'][name='resultType']:checked").size() === 0) {
                    if (jThis.parents('#availability-facets, #assessment-mode-facets, #unit-level-facets, #level-of-study-facets, #provider-facets, #assessment-mode-facets, #delivery-methods-facet, #payment-methods-facet').size() > 0) {
                        $('input[value=UNIT]').attr('checked', true);
                    }

                    if (jThis.parents('#level-of-study-facets, #provider-facets, #payment-methods-facet').size() > 0 && jThis.parents('ol .sub-faceting ol').size() == 0) {
                        $('input[value=COURSE]').attr('checked', true);
                    }
                }

                if (jThis.parents(".sub-faceting").size() > jThis.parents("ol .sub-faceting ol").size()) {
                    jThis.parents(".sub-faceting").find("ol input[type='checkbox']").removeAttr('checked');
                }

                if (jThis.parents("ol .sub-faceting ol").size() > 0 && jThis.parents("ol .sub-faceting ol").find('input:checked').size() === jThis.parents('ol .sub-faceting ol').find('input[type=checkbox]').size()) {
                    jThis.parents("ol .sub-faceting ol").find('input[type=checkbox]').removeAttr('checked');
                    jThis.parents('.sub-faceting').find('input[type=checkbox]').first().attr('checked', true);
                }

                if ($('#results-area').size() > 0) {
                    var currentId = jThis.closest('fieldset').attr('id');

                    $('.facet-form input[type=checkbox]').attr('readOnly', 'disabled');

                    ajaxSubmitSearch(currentId);

                } else {
                    updateFacetDescriptions();
                }
            });

            $('#results-area .facetSelectionDescription, #results-area .facet-form h2').live('click', function (e) {

                var jThis = $(this);
                var facetId = jThis.closest('fieldset').attr('id');

                if (selectionsIn(jThis.closest('fieldset')) >= 2 && displayed.indexOf(facetId) >= 0) {
                    forceHide.push(facetId);
                } else if (forceHide.indexOf(facetId) >= 0) {
                    forceHide.splice(forceHide.indexOf(facetId), 1);
                }

                if (displayed.indexOf(facetId) >= 0) {
                    displayed.splice(displayed.indexOf(facetId), 1);
                    jThis.closest('fieldset').find('.facet-list-container').slideUp(slideSpeed).closest('fieldset').removeClass('shown');
                } else {
                    displayed.push(facetId);
                    jThis.closest('fieldset').find('.facet-list-container').slideDown(slideSpeed,
                            function () {
                                $(this).css({height: ''});
                            }).closest('fieldset').addClass('shown');
                }

                $('.facet-form fieldset').each(function () {
                    if ($(this).attr('id') !== facetId && selectionsIn($(this)) <= 1 && displayed.indexOf($(this).attr('id')) >= 0) {
                        $(this).find('.facet-list-container').stop().slideUp(slideSpeed).closest('fieldset').removeClass('shown');
                        displayed.splice(displayed.indexOf($(this).attr('id')), 1);
                    }
                });

                e.preventDefault();
            });

            $(addHelperElements);

            $(function () {
                $('.facet-form fieldset').each(function () {
                    if (selectionsIn($(this)) >= 2) {
                        $(this).find('.facet-list-container').show().closest('fieldset').addClass('shown');
                        displayed.push($(this).attr('id'));
                    }
                });
            });

            $(applyFacetHash);

            $('#clear-facets').live('click', function (e) {
                $('#search-faceting input[type=checkbox]').removeAttr('checked');
                $('.all-selection').attr('checked', true);
                $('#advanced-search .heading-label input[name=q]').val('');
                $('#search-faceting input[type=checkbox]').first().change();
            });
        },
        termTipListeners: function () {
            var toDisplay = false;

            $('.shortlist-units acronym.available, .cart-expander acronym.available, .availabilityCell label, .spAvailabilityCell label').live('mouseover', function (e) {
                var jThis = $(this);
                if (typeof(OUATermMap) != 'undefined') {

                    jThis.removeAttr('title').find('acronym').removeAttr('title');
                    if ($('#tip').size() === 0) {
                        $('body').append('<div id="tip"><p></p><div id="tip-corner"></div></div><div id="tip-arrow"></div>');
                    }

                    var key = trim_string(jThis.add(jThis.find('acronym')).filter('*[type]').attr('type'));

                    $('#tip p').text(OUATermMap[key]);
                    var hoverElemOffset = jThis.first().offset();
                    var tipLeftPosition;

                    if ((hoverElemOffset.left + ((jThis.width() / 2) - 30) + $('#tip').width()) > $(window).width()) {
                        tipLeftPosition = $(window).width() - $('#tip').width() - 5;
                    } else {
                        tipLeftPosition = hoverElemOffset.left + ((jThis.width() / 2) - 30);
                    }

                    $('#tip').show().css({top: hoverElemOffset.top + jThis.height() + 4, left: tipLeftPosition}).show();
                    $('#tip-arrow').show().css({top: hoverElemOffset.top + jThis.height() + 2, left: hoverElemOffset.left + ((jThis.width() / 2) - 8)});
                }
            }).live('mouseout', function (e) {
                $('#tip, #tip-arrow').hide();
            });
        },
        studyPlanner : function () {
            init();

            $('#studyplanner .spAvailabilityCell input').live('change click', function() {
                var studyPeriodSelected = $(this).val() !== 'DEFER' && $(this).val() !== 'RPL';
                $(this).closest('tr').find('input.unitSelectedCheckbox').attr('checked', studyPeriodSelected);
            });

            $("#studyplanner .remove-elective-button").live("click", removeElectiveUnit);

            $('#studyplanner .details-header').live('click', function() {
                var cell = $(this).closest('td');
                var expandedHiddenInput = cell.find('.prereq-expanded-store');

                if (expandedHiddenInput.val() === 'details') {
                    expandedHiddenInput.val('');
                } else {
                    expandedHiddenInput.val('details');
                }

                detailsUpdate(cell, 100);
            });

            $('#studyplanner .prereq-header').live('click', function () {
                var cell = $(this).closest('tr');
                var expandedHiddenInput = cell.find('.prereq-expanded-store');

                if (expandedHiddenInput.val() === 'prereqs') {
                    expandedHiddenInput.val('');
                } else {
                    expandedHiddenInput.val('prereqs');
                }

                detailsUpdate(cell, 100);
            });

            $('#studyplanner #manualUnitCodeToAdd').live('keypress', function (e) {
                if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
                    addElectiveUnit($(this), 'MANUAL');
                    return false;
                } else {
                    return true;
                }
            }
                    );

            function refreshAddUnitByFilterButton() {
                if ($("#studyplanner #unitCode").val() != "") {
                    $("#studyplanner #addUnitButton").css("visibility", "visible");
                } else {
                    $("#studyplanner #addUnitButton").css("visibility", "hidden");
                }
            }

            function init() {
                $("#studyplanner .auto-refresh-add-unit").change(autoRefreshAddUnit);
                $(".hidden-addunit-field").attr('disabled', true);
                $("#areaOfStudy").attr('disabled', false);

                $('#studyplanner .prereq-expanded-store').each(function () {
                    detailsUpdate($(this).closest('td'), 0);
                });

                if ($("#areaOfStudy").val() != "") {
                    $("#discipline").attr('disabled', false);
                }

                $("#studyplanner .add-unit-button-prereq").attr('disabled', true).click(function () {
                    addElectiveUnit($(this), 'PREREQ');
                });

                $("#studyplanner .add-unit-by-filter-button").click(function () {
                    addElectiveUnit($(this), 'FILTER');
                });

                $("#studyplanner #manuallyAddUnitButton").click(function () {
                    addElectiveUnit($(this), 'MANUAL');
                });

                $("#studyplanner .add-unit-checkbox").change(function () {
                    var preReqDiv = $(this).closest("div.prereq-details");
                    var noCheckedItems = preReqDiv.find(".add-unit-checkbox[checked]").length == 0;
                    preReqDiv.find(".add-unit-button-prereq").attr('disabled', noCheckedItems);
                });

                $("#studyplanner #resetAddUnit").click(function () {
                    refreshAddUnit(new Object(), $(this));
                });

                refreshAddUnitByFilterButton();
                $("#studyplanner #unitCode").change(refreshAddUnitByFilterButton);

                $("#studyplanner #manualUnitCodeToAdd").autocomplete({
                    html: true,
                    select: function(event, ui) {
                        var unitCodeTextBox = $("#manualUnitCodeToAdd");
                        unitCodeTextBox.val(ui.item.value);
                        addElectiveUnit(unitCodeTextBox, 'MANUAL');
                        event.preventDefault();
                    },
                    source:
                            function (request, response) {
                                $.ajax({
                                    type: "POST",
                                    url: publicPrefix()+"/search/fragment/autocomplete?q=" + encodeURIComponent(request.term),
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function (data) {
                                        var unitSuggestions = jQuery.grep(data.suggestions, function (item) {
                                            return item.type == "UNIT";
                                        });
                                        response($.map(unitSuggestions, function (item) {
                                            return {
                                                label : "<span class='suggestion-title'>" + item.unitcode.toUpperCase() + " - " + item.title + "</span>",
                                                value: item.unitcode.toUpperCase()
                                            };
                                        }));
                                    }
                                });
                            }
                });
            }

            function detailsUpdate(cell, transitionTime) {
                var expandedHiddenInput = cell.find('.prereq-expanded-store');
                var prereqHeader = cell.find('.prereq-header');
                var detailsHeader = cell.find('.details-header');
                var details = cell.find('.unit-details');
                var prereqs = cell.find('.prereq-details');

                if (expandedHiddenInput.val() === 'details') {
                    detailsHeader.removeClass('expandable-heading');
                    detailsHeader.addClass('expanded-heading');
                    details.slideDown(transitionTime);
                } else {
                    detailsHeader.removeClass('expanded-heading');
                    detailsHeader.addClass('expandable-heading');
                    details.slideUp(transitionTime);
                }
                if (expandedHiddenInput.val() === 'prereqs') {
                    prereqHeader.removeClass('expandable-heading');
                    prereqHeader.addClass('expanded-heading');
                    prereqs.slideDown(transitionTime);
                } else {
                    prereqHeader.removeClass('expanded-heading');
                    prereqHeader.addClass('expandable-heading');
                    prereqs.slideUp(transitionTime);
                }
            }

            function autoRefreshAddUnit() {

                var formArray = $("#form").serializeArray()
                var areaOfStudy = "";
                var discipline = "";
                var unitCode = "";
                $.each(formArray, function (index, element) {
                    if (element.name == "areaOfStudy") {
                        areaOfStudy = element.value;
                    } else if (element.name == "discipline") {
                        discipline = element.value;
                    } else if (element.name == "unitCode") {
                        unitCode = element.value;
                    }
                });

                var data = new Object();
                data.areaOfStudy = areaOfStudy;
                data.discipline = discipline;
                data.unitCode = unitCode;
                refreshAddUnit(data, $(this));
            }

            function getLoadingWidget(jThis) {
                var loadingImage = jThis.siblings("img.loading-icon");
                if (loadingImage.length == 0) {
                    loadingImage = $("#loading");
                }
                return loadingImage;
            }

            function showLoading(jThis) {
                getLoadingWidget(jThis).css("visibility", "visible");
            }

            function hideLoading(jThis) {
                getLoadingWidget(jThis).css("visibility", "hidden");
            }

            function onAddUnitAjaxError(jThis) {
                checkValidSession();
                $("#addError").show();
                $(".add-unit-success").hide();
                hideLoading(jThis);
            }

            /**
             * Adds an elective unit
             * @param jThis - source of the add request
             * @param addMode - either 'MANUAL', 'FILTER', or 'PREREQ'
             */
            function addElectiveUnit(jThis, addMode) {

                clearAddUnitFormEntries(addMode != 'MANUAL', addMode != 'FILTER', addMode != 'PREREQ');

                showLoading(jThis);
                $('<input type="hidden" id="ajaxAddElective" name="AJAX_ADD_ELECTIVE" value="' + addMode + '"/>').appendTo('#electiveStream');
                var formData = $("#form").serialize();
                postElectivesAjax(formData, jThis, addMode == 'MANUAL');
            }

            function clearAddUnitFormEntries(clearManual, clearFilter, clearPrereq) {
                if (clearManual) {
                    $("#manualUnitCodeToAdd").val("").change();
                }
                if (clearFilter) {
                    $("#unitCode").val("").change();
                }
                if (clearPrereq) {
                    $(".add-unit-checkbox").attr('checked', false).change()
                }
            }

            function removeElectiveUnit() {
                var jThis = $(this);
                var unitCode = jThis.attr('value');

                showLoading(jThis);
                $('<input type="hidden" id="ajaxAddElective" name="AJAX_REMOVE_ELECTIVE" value="' + unitCode + '"/>').appendTo('#electiveStream');
                var formData = $("#form").serialize();
                postElectivesAjax(formData, jThis, false);
            }

            function refreshAddUnit(paramData, jThis) {
                showLoading(jThis);
                paramData.AJAX_REFRESH = "true";
                $.ajax({
                    type: "GET",
                    url: window.location.pathname,
                    data: paramData,
                    success: function (data) {
                        if (data.match(/<div id="addUnitDiv"/)) {
                            $("#studyplanner #addUnitDiv").replaceWith(data);
                            init();
                            hideLoading(jThis);
                        } else {
                            onAddUnitAjaxError(jThis);
                        }
                    },
                    error : function () {
                        onAddUnitAjaxError(jThis)
                    }
                });
            }

            function postElectivesAjax(formData, jThis, refocus) {
                $.ajax({
                    type: "POST",
                    url: window.location.pathname,
                    data: formData,
                    success: function (data) {
                        if (data.match(/<div id="addUnitDiv"/)) {
                            $("#studyPlannerTables").replaceWith(data);
                            init();
                            hideLoading(jThis);
                            if (refocus) {
                                $("#manualUnitCodeToAdd").focus();
                            }
                        } else {
                            onAddUnitAjaxError(jThis);
                        }
                    },
                    error: function () {
                        onAddUnitAjaxError(jThis)
                    }
                });
            }
        },
        studyCart: function () {
        	if ($('#study-cart-and-wishlist-form').size() > 0 ) {
                function updateCart(e) {
                    e.preventDefault();
                    blockPage(e);

                    var buttonParam = "validate=true";
                    if($(e.target).is('button, input[type=submit]')) {
                        buttonParam = encodeURIComponent($(e.target).attr('name')) + "=" + encodeURIComponent($(e.target).val());
                    }

                    var expanded = $('.cart-notification.warning .body, .cart-notification.error .body').not(function () { return $(this).height() === 0 }).map(function () { return '#' + $(this).attr('id') }).toArray();

                    var $form = $('#study-cart-and-wishlist-form');
                    $.ajax({
                        type: "POST",
                        cache: false,
                        url: $form.attr('action'),
                        data: $form.serialize() + "&" + buttonParam,
                        success: function (data) {
                            var $newForm = $('#study-cart-and-wishlist-form', data);
                            $('.message-box').remove();
                            var $newMsgBox = $('.message-box', data);

                            $(expanded.join(', '), $newForm).addClass('in');
                            $form.replaceWith($newForm);
                            $('#study-cart-and-wishlist-form').before($newMsgBox);

                            $('#study-cart-summary-count').text(pluralise($('[data-row-unit-code]').size(), 'unit'));
                            $('#wishlist-summary-count').text(pluralise($('#wishlist tbody tr').size(), 'unit'));

//                            $('#studyLoadType').change();
                            setEnrolButtonState();
                            OUAWebsite.collapseToggleHook();
                            OUAWebsite.initTooltips($newForm);
                            unBlockPage();
                        },
                        error: function (data) {
                            unBlockPage();
                        }
                    });
                }
                
                function cartCanProceed() {
                    return $('.cart-notification.error:visible, .form-error', '#study-cart-and-wishlist-form').size() === 0 &&
                        $('.cart-notification warning:visible, .ack-button, .ack-button[name="[ALL.study-load-warning]"]').not('[disabled]').size() === 0 &&
                        $('input[name=enrolmentBlocked]').val() !== 'true';
                }

                function setEnrolButtonState() {
                    if(cartCanProceed()) {
                        $('.enrol-btn').removeAttr('disabled');
                        $('.enrol-btn').removeAttr('title');
                    } else {
                        $('.enrol-btn').attr('disabled', 'disabled');
                        $('.enrol-btn').attr('title', 'You must resolve all errors & recommendations');
                    }
                }

                $(function () {
                    setEnrolButtonState();
                });

                liveAndOnPageLoad('#study-cart-and-wishlist-form #citizenshipStatus', 'change', function () {
                    var $this = $(this);
                    if($(this).val() !== '') {
                        $(this).closest('.form-footer').removeClass('invalid').find('.form-error').remove();
                    }
                    setEnrolButtonState();
                });

                $('#study-cart-and-wishlist-form .current-cart-row .study-cart-term').live('change', updateCart);
                $('.move-down-button, .move-up-button, .remove-button, .add-button, .button.add', '#study-cart-and-wishlist-form').live('click', updateCart);

                $('.ack-button[disabled!=true]').live('click', function (e) {
                	
                    e.preventDefault();
                     
                    var ackUnitCode = $(this).attr('data-acknowledge-for-unit');	
                    var reference = $(this).attr('name').replace(/^[^[]+\[/, '').replace(/\]$/, '');
                    var existingCookie = $.cookie('cartAcknowledgement');
                    if(existingCookie === null || existingCookie === '') {
                        $.cookie('cartAcknowledgement', reference, { path: '/' });
                    } else {
                        $.cookie('cartAcknowledgement', existingCookie + '|' + reference, { path: '/' });
                    }

                    $(this).val('Acknowledged').attr('disabled', 'disabled');

                    var $warningRowCell = $(this).parents('.cart-expander')
                    var $warningRowTitle = $warningRowCell.find('div.accordion-toggle');
                    	$warningRowTitle.css({'background':'#fff', 'font-weight':'normal'}).click().children('.message.in').before('<span> (acknowledged) </span>');
                    var $warningRow = $warningRowCell.parents('tr[data-row-warning-unit^="'+ ackUnitCode +'-"]');
                    	$warningRow.removeClass('warning');
                    $warningRowCell.parents('#study-load-container').removeClass('warning-study-load-container');
                    
                    var $currentRow = $('tr[data-row-unit-code="'+ ackUnitCode +'"]');
                    
                    if ( $('input[data-acknowledge-for-unit="'+ ackUnitCode +'"]:not(:disabled)').size() == 0 ) { //if all warnings for current row are acknowledged then make the current row white
                    	$currentRow.removeClass('warning').removeClass('cart-notification');
                    }
                    
                    if ($('input.ack-button:not(:disabled)').size() == 0) { //hide the main acknowledgement message at the top if all of the ack-button's are disabled
                    	$('.cart-warning-box').slideUp();	
                    }	
                    
                    setEnrolButtonState();

                });
        		
        		$('#study-cart input[name="enrol"]').click(function(e){blockPage(e);});
        		
        		$('#citizenshipStatus, .pre2008check, #study-cart input.csp-checkbox').live('change keyup', function () {
        			updatePrices();
                });
        		
        		function updatePrices(){
        			var totalPrice = 0.0;

                    var form = $('#study-cart-and-wishlist-form');
                    var pre2008 = $('.pre2008check[value=true]').attr('checked') || $('#pre2008[type=hidden]').val() === 'true';
                    var citizenshipStatusFlag = ($('#citizenshipStatus').val() == "") ? 0 : $('#citizenshipStatus').val(); 
                    var domestic = $('#citizenshipStatus option:selected').attr('data-domestic') != 'N';

                    form.find('#study-cart-table tbody tr').each(function () {
                        var $this = $(this);
                        var hecsCheckboxYes = $this.find('td input.csp-checkbox, td input.csp-checkbox-value');
                        var useHecs = hecsCheckboxYes.size() === 1 && (hecsCheckboxYes.is(':checked') || hecsCheckboxYes.is('.csp-checkbox-value[value=true]')) && domestic;
                        var $price = $this.find('.price .current-price');
                        var newPrice;

                        if (useHecs) {
                            newPrice = rawNumber($price.attr('data-hecs'));
                        } else if (domestic) {
                            newPrice = rawNumber(pre2008 ? $price.attr('data-pre-2008-domestic') : $price.attr('data-domestic'));
                        } else {
                            newPrice = rawNumber(pre2008 ? $price.attr('data-pre-2008-international') : $price.attr('data-international'));
                        }

                        if (domestic) {
                            $('.citizenship-note').hide();
                        } else {
                            $('.citizenship-note').show();
                        }

                        $this.find('.price .current-price').text(toPrice(newPrice));
                        
                        if($this.hasClass('current-cart-row')) {
                        	totalPrice += newPrice;
                        }
                    });

                    $('#total-price').text(("$" + totalPrice.toFixed(2)).replace(/(\d+)(\d\d\d)\./, "$1,$2."));        			
        		}
        		
        		if( $('#total-price').length > 0 ){ //initial update of prices if cart not empty 
        			updatePrices();
        		}

        		
        	}
        },
        enrolmentListeners: function () {
            $('.availabilityCell input').live('change click', function() {
                var studyPeriodSelected = $(this).val() !== 'DEFER' && $(this).val() !== 'RPL';
                $(this).closest('tr').find('input.unitSelectedCheckbox').attr('checked', studyPeriodSelected);
            });

            $('#shortlistForm .hecs-option').live('change', function () {
                var $this = $(this);
                var data = $($this.closest('tr').find('script[type=text/html]').html());
                var newPrice = toPrice($this.closest('td').find('.hecs-option:checked').val() === 'true' ? data.find('.hecs').text() : data.find('.non-hecs').text());

                $this.closest('tr').find('.current-price').text(newPrice);
            });

            $('#pricing-options-selection, #unit-selection-form input[type=checkbox], #unit-selection-form .hecs-option').live('change keyup', function () {
                var totalPrice = 0.0;

                var form = $('#unit-selection-form');
                var pre2008 = $('#pricingOptions.pre20081').attr('checked');
                var domestic = !!$('#pricingOptions.citizenshipKey').val().match($('#domestic-flags').html().trim());

                form.find('#selection-table tbody tr').each(function () {
                    var $this = $(this);
                    var intialCheckbox = $this.find('.first input[type=checkbox]');

                    var otherPrices = $($this.find('.price script').html());
                    var hecsCheckboxYes = $this.find('td input.hecs-option[value=true]');

                    var useHecs = hecsCheckboxYes.size() === 1 && hecsCheckboxYes.attr('checked');

                    var newPrice;

                    if (useHecs) {
                        newPrice = rawNumber(otherPrices.find('.hecs'));
                    } else if (domestic) {
                        newPrice = rawNumber(otherPrices.find(pre2008 ? '.pre-2008-domestic' : '.domestic'));
                    } else {
                        newPrice = rawNumber(otherPrices.find(pre2008 ? '.pre-2008-international' : '.international'));
                    }

                    if (domestic) {
                        $('.hecs-option').removeAttr('disabled').closest('.hecs-selection').removeClass('disabled');
                        $('.citizenship-note').hide();
                    } else {
                        $('.hecs-option').attr('disabled', 'true').filter('*[value=false]').attr('checked', 'checked').closest('.hecs-selection').addClass('disabled');
                        $('.citizenship-note').show();
                    }

                    $this.find('.price .current-price').text(toPrice(newPrice));

                    if (intialCheckbox.size() === 1 && intialCheckbox.attr('checked')) {
                        totalPrice += newPrice;
                    }
                });

                $('.total-value').text(("$" + totalPrice.toFixed(2)).replace(/(\d+)(\d\d\d)\./, "$1,$2."));
            });
        },
        chatListeners: function () {
            $('.chat-button-container a').live('click', function () {
                trackPage("/chat/button-click");
            });
            $('a#needRef, .lpInviteChatHrefAccept').live('click', function () {
                trackPage("/chat/pro-active-click");
            });
            $('a[onclick*=CloseInvite], .lpInviteChatHrefClose').live('click', function () {
                trackPage("/chat/pro-active-close");
            });
        },
        collapsibleContainerInit: function () {
            $('.collapsible-box-header a').live('click', function () {
                var $this = $(this);
                var container = $(this).parent().next('.collapsible-box-content');
                if (!container.hasClass('.animating')) {
                    container.addClass('animating');

                    if (container.filter(':visible').size() === 0) {
                        var arrow = $this.find('.show-collapsible-arrow');
                        $(arrow).removeClass('show-collapsible-arrow').addClass('hide-collapsible-arrow');
                        $this.find('.toggle-collapsible-hint').contents().filter(
                                function() {
                                    return this.nodeType == 3;
                                }).replaceWith('click to collapse');
                        container.slideDown(function () {
                            container.removeClass('animating');
                        });
                    } else {
                        container.slideUp(function () {
                            container.removeClass('animating');
                            var arrow = $this.find('.hide-collapsible-arrow')
                            $(arrow).removeClass('hide-collapsible-arrow').addClass('show-collapsible-arrow');
                            $this.find('.toggle-collapsible-hint').contents().filter(
                                    function() {
                                        return this.nodeType == 3;
                                    }).replaceWith('click to expand');
                        });
                    }
                }
            });
        },
        showMoreLinksInit: function () {
            $('a.show-more').live('click', function () {
                var $this = $(this);
                var container = $(this).parent().next('.more-info');
                if (!container.hasClass('.animating')) {
                    container.addClass('animating');

                    if (container.filter(':visible').size() === 0) {
                        $this.find('.expandable-hint').removeClass('expandable-hint').addClass('expanded-hint');
                        container.slideDown(function () {
                            container.removeClass('animating');
                        });
                    } else {
                        container.slideUp(function () {
                            container.removeClass('animating');
                            $this.find('.expanded-hint').removeClass('expanded-hint').addClass('expandable-hint');
                        });
                    }
                }
            });

            $('span.show-more').each(function () {
                $(this).replaceWith("<a class='show-more' href='javascript:void(0)'>" + $(this).html() + "</a>");
            });
        },
        salutationToGenderListener: function () {
            $('select.salutation').live('change', function () {
                if ($(this).val().match(/^mr$/i)) {
                    $('.male-gender').attr('checked', 'true');
                } else if ($(this).val().match(/miss|ms|mrs/i)) {
                    $('.female-gender').attr('checked', 'true');
                }
            });
        },
        countryCodeChangeListener: function () {

            function postCountryUpdate() {
                var $this = $(this);
                if ($this.size() > 0 && $this.attr('id').match(/countryCode$/)) {
                    var aussie = $this.val() === 'AUS';
                    $this.parents('form').toggleClass('australian', aussie);
                    $this.closest('.location').find('.australian-required').toggleClass('js-section-required', aussie)
                            .closest('.form-field').find('.required').toggle(aussie);

                    if ($this.closest('.accordion').size() > 0) {
                        $this.closest('.form-field').find('.form-error').remove();
                    }

                    var stateSelectElement = $('#' + $(this).attr('id').replace('countryCode', 'state').replace(/\./g, '\\.'));

                    if ($this.is(':disabled')) {
                        stateSelectElement.attr('disabled', 'disabled');
                    } else {
                        stateSelectElement.removeAttr('disabled');
                    }

                    setTimeout(function () { // Use timeout so it's after the update from old code
                        if (stateSelectElement.children().text().match(/^\s*(Select \.\.\.)?\s*$/)) {
                            stateSelectElement.closest('.form-field').slideUp(300).hide(
                                    function () {
                                        if (typeof(OUAFormValidation) !== 'undefined') {
                                            OUAFormValidation.applyQuestionCounts($(stateSelectElement).closest('.head'));
                                        }
                                    }).css('display', 'none');
                        } else {
                            stateSelectElement.closest('.form-field').slideDown(300);

                            if (stateSelectElement.closest('.form-field').find('.required').size() === 0) {
                                stateSelectElement.closest('.form-field').find('label').after('<span title="Required" class="required"><abbr class="required" title="Required">*</abbr></span>');
                            }
                        }
                    }, 1);
                }
            }

            $(function () {
                $('.location .country-selection').change();
            });

            $('.country-selection').live('change keyup', function () {
                var stateInputId = $(this).attr('id').replace('countryCode', 'state');
                var idSelector = stateInputId.replace(/\./g, "\\.");

                if (stateInputId.match(/state/) && ($('#' + idSelector).size() > 0 || $('label[for=' + idSelector + ']'))) {
                    populate_state($(this).attr('id'), stateInputId, country_init_1, state_init_1);
                }

                postCountryUpdate.call(this);
            });
        },

        mailingAddressSameAsHomeAddressListener: function () {

            $("input[name='personalDetails.homeAddress.mailingAddressSameAsHomeAddress']").live('change', function () {
                var homeAddressContainer = $("fieldset#home-address");
                if ($(this).val() === 'Y') {
                    homeAddressContainer.slideUp(300).hide(function () {
                        OUAFormValidation.applyQuestionCounts($(this).closest('.head'));
                    });
                } else {
                    homeAddressContainer.slideDown(300).show();
                }
            });

            $(function() {
                if ($("input[name='personalDetails.homeAddress.mailingAddressSameAsHomeAddress'][value=Y]").is(':checked')) {
                    $("fieldset#home-address").hide();
                }
            });
        },

        birthCountryCodeChangeListener: function () {

            function bornInAustralia() {
                return $('#deewrCountryDetails\\.countryOfBirth').val() === 'AUS';
            }

            $('#deewrCountryDetails\\.countryOfBirth').live('change keyup', function () {
                if (!bornInAustralia()) {
                    $('#deewrCountryDetails\\.yearAustArrival').closest('.form-field').slideDown(300).find('.form-error').remove();
                } else {
                    $('#deewrCountryDetails\\.yearAustArrival').closest('.form-field').slideUp(300).find('.form-error').remove();
                }
            });

            $(function () {
                if (bornInAustralia()) {
                    $('#deewrCountryDetails\\.yearAustArrival').closest('.form-field').hide();
                }
            })
        },

        deewrStudyCountryCodeChangeListener: function () {

            function deewrStudyPostCountryUpdate() {
                var aussie = $('#deewrCountryDetails\\.studyResidence').val() === 'AUS';
                $('.studyResPostcode').toggle(aussie);

                if (!aussie) {
                    $('#deewrCountryDetails\\.studyResidence ~ .error-container').html('');
                }

                $('#deewrCountryDetails\\.studyResidence').closest('.postcode-location').find('.postcode-rx-validation[value!=""]').change();
            }

            $('#deewrCountryDetails\\.studyResidence').live('change keyup', function () {
                deewrStudyPostCountryUpdate.call(this);
            });

            $(function() {
                $('#deewrCountryDetails\\.studyResidence').each(deewrStudyPostCountryUpdate);
            });
        },

        deewrPriorStudyLiveListeners: function () {

            function deewrPriorStudyLiveUpdate(selectElement) {
                var correspondingYearContainer = $('label[for=' + selectElement.id + ']').parent().find('.partial-input');
                if (selectElement.value == "2" || selectElement.value == "3") {
                    correspondingYearContainer.show();
                }
                else {
                    correspondingYearContainer.hide();
                }
            }

            $('#deewrEducationDetails\\.postgradStatus, #deewrEducationDetails\\.bachelorStatus, #deewrEducationDetails\\.subDegreeStatus, #deewrEducationDetails\\.vetSubDegreeStatus, #deewrEducationDetails\\.vetAwardStatus, #deewrEducationDetails\\.otherCourseStatus').live('change keyup', function (event) {
                deewrPriorStudyLiveUpdate(event.currentTarget);
            });

            $(function() {
                $('#deewrEducationDetails\\.postgradStatus, #deewrEducationDetails\\.bachelorStatus, #deewrEducationDetails\\.subDegreeStatus, #deewrEducationDetails\\.vetSubDegreeStatus, #deewrEducationDetails\\.vetAwardStatus, #deewrEducationDetails\\.otherCourseStatus').each(function (i, element) {
                    deewrPriorStudyLiveUpdate(element);
                });
                $('.hidden-without-js').show();
            });
        },

        deewrSecSchoolCodeChangeListener: function () {

            function deewrSecSchoolUpdate() {
                var completedSchool = $('#deewrEducationDetails\\.secSchoolStatus').val() in {'S':'', 'T':''};
                $('.secSchoolDetails').each(function() {
                    $(this).closest('div.secSchoolDetailsDiv').find('abbr.required').toggle(completedSchool);
                });
                if (!completedSchool) {
                    $('#deewrEducationDetails\\.secSchoolStatus ~ .error-container').html('');
                    $('#deewrEducationDetails\\.secSchoolStatusOverseas ~ .error-container').html('');
                    $('div.secSchoolDetailsDiv, div.secSchoolAustDetailsDiv, div.secSchoolTERDetailsDiv').slideUp().hide();
                } else {
                    $('div.secSchoolDetailsDiv').slideDown().show();
                    if ($("input[name='deewrEducationDetails\\.secSchoolStatusOverseas']:checked").val() == 'N') {
                        $('div.secSchoolAustDetailsDiv').slideDown();
                        $('#deewrEducationDetails\\.secSchoolStatusYear').trigger('change');
                    }
                }
            }

            $('#deewrEducationDetails\\.secSchoolStatus').live('change keyup', function () {
                deewrSecSchoolUpdate.call(this);
            });

            $(function() {
                deewrSecSchoolUpdate()
            });
        },

        deewrSecSchoolAustCodeChangeListener: function () {

            function deewrSecSchoolAustUpdate() {
                var completedSchool = $('#deewrEducationDetails\\.secSchoolStatus').val() in {'S':'', 'T':''};
                if (completedSchool) {
                    var completedSchoolinAust = $("input[name='deewrEducationDetails\\.secSchoolStatusOverseas']:checked").val() == 'N';
                    $('.secSchoolAustDetails').each(function() {
                        $(this).closest('div.secSchoolAustDetailsDiv').find('abbr.required').toggle(completedSchoolinAust);
                    });
                    if (!completedSchoolinAust) {
                        $("input[name='deewrEducationDetails\\.secSchoolAustState'] ~ .error-container").html('');
                        $('div.secSchoolAustDetailsDiv').slideUp().hide();
                    } else {
                        $('div.secSchoolAustDetailsDiv').slideDown().show();
                    }
                    $('#deewrEducationDetails\\.secSchoolStatusYear').trigger('change');
                }
            }

            $('#deewrEducationDetails\\.secSchoolStatusOverseas1').live('change', deewrSecSchoolAustUpdate);
            $('#deewrEducationDetails\\.secSchoolStatusOverseas2').live('change', deewrSecSchoolAustUpdate);

            $(function() {
                deewrSecSchoolAustUpdate();
            });
        },

        deewrSecSchoolTERChangeListener: function () {
            function deewrTERUpdate() {
                var now = new Date();
                var currentYear = now.getFullYear();
                var secSchoolYear = $("#deewrEducationDetails\\.secSchoolStatusYear").val();
                var finishedSecSchLastYear = null;
                var completedSchool = $('#deewrEducationDetails\\.secSchoolStatus').val() in {'S':'', 'T':''};
                var completedSchoolinAust = $("input[name='deewrEducationDetails\\.secSchoolStatusOverseas']:checked").val() === 'N';
                if (secSchoolYear != null) {
                    finishedSecSchLastYear = (secSchoolYear >= (currentYear - 1));
                    $('div.secSchoolTERDetailsDiv').find('abbr.required').toggle(finishedSecSchLastYear);
                }
                if (finishedSecSchLastYear != null && finishedSecSchLastYear && completedSchoolinAust && completedSchool) {
                    $('div.secSchoolTERDetailsDiv').slideDown().show();
                } else {
                    $('div.secSchoolTERDetailsDiv ~ .error-container').html('');
                    $('div.secSchoolTERDetailsDiv').slideUp().hide();
                }
            }

            $('#deewrEducationDetails\\.secSchoolStatusYear').live('change keyup', deewrTERUpdate);
            $('#deewrEducationDetails\\.secSchoolTER').live('change', function (e) {
                if ($(this).val() != null) {
                    var enteredNumber = $(this).val().trim();
                    if (!isNaN(parseFloat(enteredNumber)) && isFinite(enteredNumber)) {
                        $(this).val(parseFloat(enteredNumber).toFixed(2));
                    }
                }
            });

            $(function() {
                deewrTERUpdate();
            });
        },

        deewrParentEdListener: function () {

            function deewrParentEdUpdate() {
                var parentGender1Required = $('#deewrEducationDetails\\.highestEdLevelPG1').val() != 'X';
                var parentGender2Required = $('#deewrEducationDetails\\.highestEdLevelPG2').val() != 'X';
                $('div#genderPG1').find('abbr.required').toggle(parentGender1Required);
                $('input#deewrEducationDetails\\.genderPG11').toggleClass("js-section-required", parentGender1Required);
                $('input#deewrEducationDetails\\.genderPG12').toggleClass("js-section-required", parentGender1Required);
                $('div#genderPG2').find('abbr.required').toggle(parentGender2Required);
                $('input#deewrEducationDetails\\.genderPG21').toggleClass("js-section-required", parentGender2Required);
                $('input#deewrEducationDetails\\.genderPG22').toggleClass("js-section-required", parentGender2Required);
            }

            $('#deewrEducationDetails\\.highestEdLevelPG1').live('change', deewrParentEdUpdate);
            $('#deewrEducationDetails\\.highestEdLevelPG2').live('change', deewrParentEdUpdate);

            $(function() {
                deewrParentEdUpdate();
            });
        },

        deewrImpairmentListener: function () {

            function deewrImpairmentUpdate() {
                var hasImpairment = $("input[name='deewrImpairmentsDetails\\.hasImpairment']:checked").val() == 'Y';
                $('div#impairments-list').toggleClass('one-of-validation', hasImpairment);
                $('.impairmentDetails').each(function() {
                    $(this).closest('div.impairmentDetailDiv').find('abbr.required').toggle(hasImpairment);
                });
                if (!hasImpairment) {
                    $("div#impairments-list ~ .error-container").html('');
                    $('div.impairmentDetailDiv').slideUp().hide();
                } else {
                    $('div.impairmentDetailDiv').slideDown().show();
                }
            }

            $('#deewrImpairmentsDetails\\.hasImpairment1').live('change', deewrImpairmentUpdate);
            $('#deewrImpairmentsDetails\\.hasImpairment2').live('change', deewrImpairmentUpdate);

            $(function() {
                $("input[name='deewrImpairmentsDetails\\.hasImpairment']").each(deewrImpairmentUpdate);
            });
        },

        deewrPre2010Listener: function () {

            var COMPLETE_DIV = 'div.pre-2010-complete-div';
            var PRE_REQ_DIV = 'div.pre-2010-pre-req-div';
            var START_YEAR_DIV = 'div.pre-2010-start-year-div';

            function hideDiv(divName) {
                $(divName).slideUp().hide();
                $(divName + ' ~ .error-container').html('');
            }

            function showDiv(divName) {
                $(divName).slideDown().show();
            }


            function deewrPre2010Update() {
                if ($("input[name='deewrEducationDetails\\.pre2010CSP']:checked").val() != 'Y') {
                    hideDiv(COMPLETE_DIV);
                    hideDiv(PRE_REQ_DIV);
                    hideDiv(START_YEAR_DIV);
                } else {
                    showDiv(COMPLETE_DIV);
                }
                deewrPre2010CompleteUpdate();
            }

            function deewrPre2010CompleteUpdate() {
                if ($('div.pre-2010-complete-div').is(":visible") && $("input[name='deewrEducationDetails\\.pre2010CSPCourseCompleted']:checked").val()) {
                    if ($("input[name='deewrEducationDetails\\.pre2010CSPCourseCompleted']:checked").val() == 'Y') {
                        hideDiv(START_YEAR_DIV);
                        showDiv(PRE_REQ_DIV);
                    } else {
                        hideDiv(PRE_REQ_DIV);
                        showDiv(START_YEAR_DIV);
                    }
                }
                deewrPre2010PreReqUpdate();
            }

            function deewrPre2010PreReqUpdate() {
                if ($('div.pre-2010-pre-req-div').is(":visible")) {
                    if ($("input[name='deewrEducationDetails\\.pre2010CSPCoursePreReq']:checked").val() == 'Y') {
                        showDiv(START_YEAR_DIV);
                    } else {
                        hideDiv(START_YEAR_DIV);
                    }
                }
            }

            $('#deewrEducationDetails\\.pre2010CSP1').live('change', deewrPre2010Update);
            $('#deewrEducationDetails\\.pre2010CSP2').live('change', deewrPre2010Update);
            $('#deewrEducationDetails\\.pre2010CSPCourseCompleted1').live('change', deewrPre2010CompleteUpdate);
            $('#deewrEducationDetails\\.pre2010CSPCourseCompleted2').live('change', deewrPre2010CompleteUpdate);
            $('#deewrEducationDetails\\.pre2010CSPCoursePreReq1').live('change', deewrPre2010PreReqUpdate);
            $('#deewrEducationDetails\\.pre2010CSPCoursePreReq2').live('change', deewrPre2010PreReqUpdate);

            $(function() {
                hideDiv(COMPLETE_DIV);
                hideDiv(PRE_REQ_DIV);
                hideDiv(START_YEAR_DIV);

                var didPre2010CSPVal = $("input[name='deewrEducationDetails\\.pre2010CSP']:checked").val();
                if (didPre2010CSPVal && (didPre2010CSPVal == 'Y')) {
                    showDiv(COMPLETE_DIV);
                }
                else return;

                var completedPre2010CSP = $("input[name='deewrEducationDetails\\.pre2010CSPCourseCompleted']:checked").val();
                if (completedPre2010CSP) {
                    if (completedPre2010CSP == 'Y') {
                        hideDiv(START_YEAR_DIV);
                        showDiv(PRE_REQ_DIV);
                    }
                    else if (completedPre2010CSP == 'N') {
                        hideDiv(PRE_REQ_DIV);
                        showDiv(START_YEAR_DIV);
                        return;
                    }
                }
                else {
                    hideDiv(PRE_REQ_DIV);
                    hideDiv(START_YEAR_DIV);
                    return;
                }

                var preReqPre2010CSPVal = $("input[name='deewrEducationDetails\\.pre2010CSPCoursePreReq']:checked").val();
                if (preReqPre2010CSPVal) {
                    if (preReqPre2010CSPVal == 'Y') {
                        showDiv(START_YEAR_DIV);
                    }
                    else if (preReqPre2010CSPVal == 'N') {
                        hideDiv(START_YEAR_DIV);
                    }
                }
            });
        },

        faqListeners: function () {
            var enquiryRequestTimeout = null;
            var enquiryRequestDelay = 500;
            var lastSelected = null;
            var lastRequest = "";

            function applyNewWindowTargetToFaqLinks() {
                $('.faq-content-inner a').attr('target', '_blank').addClass('external');
            }

            $(applyNewWindowTargetToFaqLinks);

            $('#enquiryComment, #enquiry-select-category-form input[name=category], #enquiry-select-category-form input[name=existingStudent]').live('change keyup', function (e) {
                if (enableFaqUpdate) {
                    var userComment = $('#enquiryComment').val().replace(enrolmentLetterMatcher, '').replace(swapMatcher, '');
                    var requestUrl = publicPrefix()+'/faq/search?enquiryComment=' + encodeURIComponent(userComment) + '&category=' + encodeURIComponent($('input[name=category]:checked').val()) + '&existingStudent=' + ($('input[name=existingStudent]:checked, input[name=existingStudent][type=hidden]').val() === "true");

                    if (lastRequest !== requestUrl) {
                        lastRequest = requestUrl;
                        clearTimeout(enquiryRequestTimeout);
                        $('#faq-data').html('').addClass('faq-loading');
                        enquiryRequestTimeout = setTimeout(function () {

                            $.get(requestUrl, function (data) {
                                $('#faq-data').removeClass('faq-loading').html(data);
                                applyNewWindowTargetToFaqLinks();
                            });
                        }, enquiryRequestDelay);
                    }
                }
            });

            $('input[name=statementDeliveryMethod]').live('change', function () {
                if ($("input[name=statementDeliveryMethod]").val() === 'Email') {
                    $(this).parents('form').addClass('deliver-email').removeClass('deliver-post');
                } else {
                    $(this).parents('form').addClass('deliver-post').removeClass('deliver-email');
                }
            });

            $('#faq-container .faq-title').live('click', function (e) {
                if (!$(this).closest('#faq-container').hasClass('transitioning')) {

                    $(this).closest('#faq-container').addClass('transitioning');

                    var innerContent = $(this).closest('.faq-item').find('.faq-content-inner');

                    var shrunk = false;
                    while (innerContent.find('.read-more-link').size() === 0 && innerContent.text().length > 700) {
                        innerContent.children().last().remove();
                        shrunk = true;
                    }

                    $("a[href*='/home/enquiry']").each(function () {
                        $(this).replaceWith($(this).text());
                    });

                    if (shrunk === true) {
                        var readMoreLink = $('<a target="_blank" class="read-more-link external">Read more</a>');
                        readMoreLink.attr('href', $(this).attr('href'));
                        innerContent.append(readMoreLink);
                    }

                    $('.faq-item').find('.faq-content').slideUp(function () {
                        $(this).closest('.faq-item').removeClass('expanded').closest('#faq-container').removeClass('transitioning');
                    });

                    if ($(this).closest('.faq-item').find('.vote-up').size() === 0) {
                        innerContent.append('<div class="faq-feedback clearfix">' +
                                '<span class="faq-prompt-message">Was this information helpful?</span>' +
                                '<span class="vote-container">' +
                                '<a href="javascript:void(0)" class="vote-up" title="Yes, this answer was helpful">Yes</a>' +
                                '<a href="javascript:void(0)" class="vote-down" title="No, this answer wasn\'t helpful">No</a>' +
                                '</span>' +
                                '<p class="faq-feedback-message"></p>' +
                                '</div>');
                    }

                    if (!$(this).closest('.faq-item').hasClass('expanded')) {
                        trackEvent('FAQ', 'openfaq', $(this).attr('href').replace(/^.*#/, ''));

                        $(this).closest('.faq-item').addClass('expanded').find('.faq-content').stop().slideDown(function () {
                            $(this).closest('#faq-container').removeClass('transitioning');
                        });
                    }
                }
                e.preventDefault();
            });

            function handleVotes(me, other, message, value) {
                $('.faq-feedback .vote-' + me).live('mouseover',
                        function () {
                            $(this).closest('.faq-feedback').removeClass('faq-feedback-' + other).addClass('faq-feedback-' + me);
                        }).live('mousedown',
                        function () {
                            $(this).closest('.faq-feedback').addClass('faq-active-' + me);
                        }).live('mouseup',
                        function () {
                            $(this).closest('.faq-feedback').removeClass('faq-active-' + me);
                        }).live('mouseout',
                        function () {
                            $(this).closest('.faq-feedback').removeClass('faq-feedback-' + other).removeClass('faq-feedback-' + me).removeClass('faq-active-' + me).removeClass('faq-active-' + other);
                        }).live('click', function () {
                    $(this).closest('.faq-feedback').removeClass('faq-feedback-voted-' + other).addClass('faq-feedback-voted-' + me);
                    trackEvent('FAQ', 'vote', $(this).closest('.faq-item').find('.faq-title').attr('href').replace(/^.*#/, ''), value);

                    $(this).closest('.faq-item').find('.faq-feedback-message').text(message).slideDown();
                });
            }

            handleVotes("up", "down", "Great!  Thanks for your feedback.", 1);
            handleVotes("down", "up", "Thanks for your feedback.", -1);
        },
        searchAutocomplete: function () {
            $(function() {
                $("#search-box, #q, .course-and-unit-search input, #slim-search-box, .search-field").autocomplete({
                    html: true,
                    open: function(event, ui) {
                        $('.ui-autocomplete').css({'marginTop': "-1px", "marginLeft" : "-4px"});
                    },
                    source: function (request, response) {
                        $.ajax({
                            type: "POST",
                            url: publicPrefix()+"/search/fragment/autocomplete?q=" + encodeURIComponent(request.term),
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {

                                $('.ui-autocomplete').css({'marginTop': "0px", "marginLeft" : "0px"});
                                $('.prediction-box').val(data.prediction);
                                response($.map(data.suggestions, function (item) {
                                    var securityPrefix = window.location.pathname.match(/\/public\//) ? publicPrefix() : securePrefix();

                                    return {
                                        label: (item.type.match(/content/i) ? "" : "<span class='type'>" + item.type.toLowerCase() + "</span>") +
                                                "<span class='suggestion-title'>" + item.title + "</span>" +
                                                (item.unitcode === null ? "" : " <span class='unitcode'>(" + item.unitcode + ")</span>") +
                                                "<span class='url'>" + item.url.replace(/^\/(public|secure)/, securityPrefix) + "</span>",
                                        value: item.title
                                    };
                                }));
                            }
                        });
                    },
                    focus: function(event, ui) {
                        event.preventDefault();
                    },
                    select: function(event, ui) {
                        document.location.href = ui.item.label.match(/<span class='url'>(.*)<\/span>/)[1];
                        event.preventDefault();
                    }
                });
            });
        },
        addressAutocomplete: function () {
            $(function() {
            	
            	// This is the 'Change' button
            	$(".ac-address-search input.address-change").live("click", function(event){
            		$(this).closest('.location-search').removeClass('location-available');
            		$('.ac-address-search input.address-search').val('');

                    $('.ac-address-search input.line1').val('');
                    $('.ac-address-search input.line2').val('');
                    $('.ac-address-search input.line3').val('');
                    $('.ac-address-search input.city').val('');
                    $('.ac-address-search select.countryCode').val('');
                    $('.ac-address-search select.state').val('');
                    $('.ac-address-search input.postCode').val('');
            	});
            	
                $(".ac-address-search input.address-entry").autocomplete({
                    html: true,
                    delay: 200,
                    open: function(event, ui) {
                        $('.ui-autocomplete').css({'marginTop': "-1px", "marginLeft" : "-4px"});
                    },
                    source: function (request, response) {
                        $.ajax({
                            type: "GET",
                            url: publicPrefix()+"/enquiry/location/ajax-suggest-address?addressline=" + encodeURIComponent(request.term),
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {

                                $('.ui-autocomplete').css({'marginTop': "0px", "marginLeft" : "0px"});
                                $('.prediction-box').val(data.prediction);
                                
                                data.suggestions.push(jQuery.parseJSON('{"usedefault":true}'))
                                
                                response($.map(data.suggestions, function (item) {
                                	
                                	if (item.usedefault) {
                                		return {
                                			label: '<b>Enter custom address</b>',
                                            address: item,
                                            value: selectiveJoin(item.line1)
                                		}
                                	}
                                	
                                	if (item.unverified) {
                                		return {
                                			label: '<b>'+item.line1+'</b>',
                                            address: item,
                                            value: selectiveJoin(item.line1)
                                		}
                                	}

                                    return {
                                        label: selectiveJoin(item.line1,
                                                item.line2,
                                                item.line3,
                                                item.city,
                                                item.countryCode,
                                                item.state,
                                                item.postCode),
                                        address: item,
                                        value: selectiveJoin(item.line1)
                                    };
                                }));
                            }
                        });
                    },
                    focus: function(event, ui) {
                        event.preventDefault();
                    },
                    select: function(event, ui) {
                    	
                    	$('.location-oneline').html(ui.item.label);
                    	$('.location-search').addClass('location-available');
                    	
                    	if (ui.item.address.usedefault) {
                    		$('.ac-address-search').addClass('address-multiline');
                    		$('.ac-address-search input.line1').val($(this).val());
                    	}
                    	else {
	                    	if (ui.item.address.unverified) {
	                    		$('.ac-address-search').addClass('address-multiline');
	                    	}

	                        $('.ac-address-search input.line1').val(ui.item.address.line1);
	                        $('.ac-address-search input.line2').val(ui.item.address.line2);
	                        $('.ac-address-search input.line3').val(ui.item.address.line3);
	                        $('.ac-address-search input.city').val(ui.item.address.city);
	                        $('.ac-address-search select.countryCode').val(ui.item.address.countryCode);
	                        $('.ac-address-search select.state').val(ui.item.address.state);
	                        $('.ac-address-search input.postCode').val(ui.item.address.postCode);
                        }
                    }
                });
            });
        },
        trackingListeners: function () {
            $('a.open-ebook').live('click', function () {
                trackEvent('eBook click', $(this).text());
            });
            $('a.filetype-PDF').live('click', function () {
                trackEvent('PDF link', $(this).text());
            });
            $('a.youtube-channel-img').live('click', function () {
                trackEvent('Youtube channel link', $(this).find('img').attr('alt'));
            });
            $('a.youtube-channel-img-myoua').live('click', function () {
                trackEvent('Youtube channel link MyOUA', $(this).find('img').attr('alt'));
            });
            $('#enquiry-select-category-form .submit-options input').live('click', function() {
                OUAWebsite.trackOnlineEnquirySubmit();
            });
        },
        trackOnlineEnquirySubmit: function() {
            var firstName = $('input#firstName').val();
            var lastName = $('input#lastName').val();
            var ouaId = $('input#ouaId').val();
            var emailAddress = $('input#email').val();
            var phoneNumber = $('input#phoneCountryCode').val() + $('.form-field input#phoneNumber').val();
            var category = $('input[name=category]:checked').val();

            OUAWebsite.trackAdditionalOnlineEnquiryDetails(firstName, lastName, emailAddress, phoneNumber, ouaId);
            trackPage("/enquiry/additional-user-data");

        },
        trackAdditionalOnlineEnquiryDetails: function(firstName, lastName, emailAddress, phoneNumber, ouaId) {

            _gaq.push(['_setCustomVar',
                1,
                "FirstName",
                firstName,
                3]
                    );

            _gaq.push(['_setCustomVar',
                2,
                "LastName",
                lastName,
                3]
                    );

            _gaq.push(['_setCustomVar',
                3,
                "EmailAddress",
                emailAddress,
                3]
                    );

            _gaq.push(['_setCustomVar',
                4,
                "PhoneNumber",
                phoneNumber,
                3]
                    );

            if (ouaId != null && ouaId != "") {

                _gaq.push(['_setCustomVar',
                    5,
                    "OUAId",
                    ouaId,
                    3]
                        );

            }
        },
        academicStatementListeners: function () {
            var authInputSelector = '#authorise-email-start #authorise';
            var authChange = function () {
                $('#authorised-email-content').toggle($(this).attr('checked'));
            };

            $(authInputSelector).live('change', authChange);
            $(function () {
                authChange.call($(authInputSelector));
            });
        },
        providerSliderInit: function () {
            $(function () {
                var itemCount = $('.secondary-providers li').size();
                var elems = $('.secondary-providers li');
                var random = Math.round(Math.random() * itemCount);

                for (var i = 0; i < itemCount; i++) {
                    $('.secondary-providers ul').append($(elems.get((i + random) % itemCount)));
                }

                $('.secondary-providers').
                        append("<a class='slider-arrow-right' href='javascript:void(0)'><span>Slide provders right</span></a><a class='slider-arrow-left' href='javascript:void(0)'><span>Slide provders left</span></a>").
                        find('li').
                        each(function (i) {
                    $(this).css('left', i * 135);
                });

                $('#content-footer .secondary-providers').css('visibility', 'visible');
            });

            $('.slider-arrow-right, .slider-arrow-left').live('click', function (e) {
                var left = $(this).blur().hasClass('slider-arrow-left');

                if (!$('#content-footer').hasClass('moving')) {
                    $('#content-footer').addClass('moving');

                    if (left) {
                        var itemCount = $('.secondary-providers li').size();
                        $('.secondary-providers li').filter(
                                function(i) {
                                    return itemCount - i <= 5;
                                }).insertBefore($('.secondary-providers li:first'));
                    }

                    $('.secondary-providers li').each(function (i) {
                        $(this).css('left', (i * 135) + (left ? -675 : 0));
                    });

                    var first = true;
                    $('.secondary-providers li').animate({left: (left ? '+' : '-') + '=675'}, 1000, function () {
                        if (!left && first) {
                            first = false;
                            $('.secondary-providers ul').append($('.secondary-providers li').filter(function(i) {
                                return i < 5;
                            }));
                        }
                        $('#content-footer').removeClass('moving');
                    });
                }

                e.preventDefault();
            });
        },
        matchPairListener: function () {

            $('.match-pair input').live('focus change keyup', function () {
                var container = $(this).parents('.match-pair');
                var firstElement = container.find('input').eq(0);
                var secondElement = container.find('input').eq(1);
                var goodMatch = firstElement.val() === secondElement.val();

                if (container.find('.match-pair-result').size() === 0) {
                    secondElement.after("<span class='match-pair-result inline-status'></span>");
                }

                var resultContainer = container.find('.match-pair-result');

                if (firstElement.val().match(/^\s*$/) || secondElement.val().match(/^\s*$/)) {
                    resultContainer.text("");
                    resultContainer.removeClass('good-match').removeClass('bad-match');
                } else {
                    if (goodMatch) {
                        resultContainer.text("Match");
                        resultContainer.addClass('good-match').removeClass('bad-match');
                    } else {
                        resultContainer.text("No match");
                        resultContainer.removeClass('good-match').addClass('bad-match');
                    }
                }
            });
        },
        phoneCallTrackingListener: function () {
            if (isPhone()) {
                $('.ZCT_Phone1').live('click', function () {
                    trackPage('/phone-number-touch/' + $(this).text().replace(/[^\d]+/g, ''));
                });
            }
        },
        trackPaymentMethodSelections: function () {
            if (OUAWebsite.isEnrolmentPaymentOptionsStep()) {
                var ouaId = $(".PALEVEL0PRIMARY").last().text();
                var paymentMethod = $(".PSDROPDOWNLIST").last().val();

                if (document.location.pathname.match(/account-summary/)) {
                    OUAWebsite.trackPaymentMethodDetails(ouaId, paymentMethod);
                    trackPage("/enrolment/additional-enrolment-data/post-tax-invoice-payment");
                } else {
                    if (paymentMethod != null && paymentMethod != "") {
                        OUAWebsite.trackPaymentMethodDetails(ouaId, paymentMethod);
                        trackPage("/enrolment/additional-enrolment-data/initial-payment");
                    }
                }
            }
        },
        trackPaymentMethodDetails: function(ouaId, paymentMethod) {

            _gaq.push(['_setCustomVar',
                1,                       // slot #1.  required
                "OUAId",                // name for the custom variable - required
                ouaId,                  // value of the custom var - required
                3                        // scope = page-level - optional
            ]);

            _gaq.push(['_setCustomVar',
                2,
                "PaymentMethod",
                paymentMethod,
                3
            ]);

        },
        trackGoogleCommerceOnSale: function () {
            if (OUAWebsite.isEnrolmentPaymentOptionsStep()) {
          
            	var ouaId = $("table#ACE_width.PABACKGROUNDINVISIBLE .PALEVEL0PRIMARY").last().html();
            	var invoiceId = ouaId + new Date().getTime();
                var grandTotal = $('label[for=OUA_SS_PYMT_WRK_SSF_TOTAL_DUE]').closest('tr').next().find('.PSEDITBOX_DISPONLY').text().replace(/[^\d.]+/g, '');
     
                if (OUAWebsite.isAppropriateToTrackCommerceEvent(grandTotal,invoiceId)) {
           	
                    _gaq.push(['_addTrans',
                        invoiceId,            // order ID - required
                        "OUA",                // affiliation or store name
                        grandTotal,           // total - required
                        "0",                  // tax
                        "0",                  // shipping
                        "",                   // city
                        "",                   // state or province
                        ""                    // country
                    ]);

                    $('.PSLEVEL1GRIDWBO tr').each(function () {
                        var $data = $(this).find('.PSEDITBOX_DISPONLY');
                        if ($data.size() > 0) {
                            var unitCode = $data.eq(0).text().trim();
                            var price = $data.eq(2).text().replace(/[^\d.]+/g, '');
                            var unitName = $data.eq(1).text().trim();
                            var period = $(this).find('td').eq(2).text().trim();

                            var metadata = $("table#unit-metadata tr#" + unitCode);
                            var provider = metadata.find("td.unit-metadata-provider").first().text();
                            var level = metadata.find("td.unit-metadata-level").first().text();
                            var area = metadata.find("td.unit-metadata-area").first().text();
                            var ouaId = $(".PALEVEL0PRIMARY").last().text();

                            // Generate additional details using custom vars that can't be associated
                            // with the eCommerce transaction any other way:
                            OUAWebsite.trackAdditionalTransactionDetails(invoiceId, provider, level, area, ouaId);

                            _gaq.push(['_addItem',
                                invoiceId,           // order ID - necessary to associate item with transaction
                                unitCode,            // SKU/code - required
                                unitName,            // product name
                                period,              // category or variation
                                price,               // unit price - required
                                "1"                  // quantity - required
                            ]);
                        }
                    });


                    _gaq.push(['_trackTrans']); //submits transaction to the Analytics servers
                }
            }
        },
        dispatchGoogleCommerceInfo: function (googleCommercePackets) {
        	
            if (googleCommercePackets) {
            	
            	for ( var packetIdx=0; packetIdx<googleCommercePackets.length; packetIdx++)  {
            		var packet = googleCommercePackets[packetIdx];
            		
                    _gaq.push(['_addTrans',
                        packet.invoiceId,            // order ID - required
                        "OUA",                // affiliation or store name
                        packet.total,         // total - required
                        "0",                  // tax
                        "0",                  // shipping
                        "",                   // city
                        "",                   // state or province
                        ""                    // country
                    ]);
            	
                	for ( var itemIdx=0; itemIdx<packet.items.length; itemIdx++)  {
                		var item = packet.items[itemIdx];

                        _gaq.push(['_addItem',
                            packet.invoiceId,           // order ID - necessary to associate item with transaction
                            item.code,                  // SKU/code - required
                            item.name,                  // product name
                            item.category,              // category or variation
                            item.price,                 // unit price - required
                            item.quantity               // quantity - required
                        ]);
            			
                    	for ( var customIdx=0; customIdx<item.customVars.length; customIdx++)  {
                    		var custom = item.customVars[customIdx];

                            _gaq.push(['_setCustomVar',
                                custom.slot,               // slot #1.  required
                                custom.name,               // name for the custom variable - required
                                custom.value,              // value of the custom var - required
                                custom.scope               // scope = page-level - optional
                            ]);
            			}
            		}
            	}

                trackPage("/enrolment/additional-enrolment-data");
                
                // TODO: Don't actually push it yet..
                _gaq.push(['_trackTrans']); //submits transaction to the Analytics servers
            }
        },
        isEnrolmentPaymentOptionsStep: function() {
            return ($('#enrolment-process').size() > 0 && $('#enrolment-process').hasClass('payment-options-step'));
        },
        isAppropriateToTrackCommerceEvent: function(grandTotal, invoiceId) {
            if (grandTotal != null && grandTotal != "") {
                if (!OUAWebsite.cookieIndicatesEventHasBeenTracked(invoiceId)) {
                    OUAWebsite.addToTrackedEventCookie(invoiceId);
                    return true;
                }
            }
            return false;
        },
        cookieIndicatesEventHasBeenTracked: function(invoiceId) {
            var trackedIds = OUAWebsite.getTrackedInvoiceIds();

            if (trackedIds.length > 0) {
                for (var i = 0; i < trackedIds.length; i++) {
                    if (trackedIds[i] == invoiceId) {
                        return true;
                    }
                }
            }
            return false;
        },
        addToTrackedEventCookie: function(invoiceId) {
            var trackedIds = OUAWebsite.getTrackedInvoiceIds();
            trackedIds.push(invoiceId);
            document.cookie = "ecommerce-tracking-invoiceIds=" + trackedIds.toString() + ";";
        },
        getTrackedInvoiceIds: function() {
            if (document.cookie.match(/ecommerce-tracking-invoiceIds/)) {
                var fullString = document.cookie.match(/(?:ecommerce-tracking-invoiceIds)[=][\d,]/).pop();
                if (fullString != null && fullString != "") {
                    return fullString.match(/([\d]+(?=\.)*)+/g);
                }
            }
            return [];
        },
        trackAdditionalTransactionDetails: function(invoiceId, provider, level, area, ouaId) {
            if (invoiceId != null && invoiceId != "") {

                _gaq.push(['_setCustomVar',
                    1,                       // slot #1.  required
                    "InvoiceId",            // name for the custom variable - required
                    invoiceId,              // value of the custom var - required
                    3                        // scope = page-level - optional
                ]);

                _gaq.push(['_setCustomVar',
                    2,
                    "Provider",
                    provider,
                    3
                ]);

                _gaq.push(['_setCustomVar',
                    3,
                    "StudyLevel",
                    level,
                    3
                ]);

                _gaq.push(['_setCustomVar',
                    4,
                    "StudyArea",
                    area,
                    3
                ]);

                _gaq.push(['_setCustomVar',
                    5,
                    "OUAId",
                    ouaId,
                    3
                ]);

                trackPage("/enrolment/additional-enrolment-data");
            }
        },
        phoneEnquiryCategoriesInit: function () {
            if (isPhone()) {
                var $container = $('input[name=category]:first').closest('.labeled-area');
                var $oldInputs = $('input[name=category]');
                $container.append("<select id='category' name='category'><option value=''>Select ...</option></select>");

                $oldInputs.each(function () {
                    $('#category').append("<option value='" + $(this).val() + "' " + ($(this).is(':checked') ? ' selected="selected" ' : '') + ">" + $(this).parent().find('label').text() + "</option>");
                });

                $container.find('.user-choices').remove();
            }
        },
        flashCloseListener: function () {
            $(document).delegate(".success-box .close, .warning-box .close, .error-box .close", "click", function (e) {
                $(this).parent().slideUp('fast');
                e.preventDefault();
            });
        },
        linkJumpClickListener: function () {
            $('.link-jump').live('click', function (e) {
                $('' + $(this).attr('href') + '').click();
            });
        },
        hecsFormListeners: function () {
            $('.hecs-form input[name=permanentHumanitarianVisaAndInAustralia]').live('change', function () {
                if ($(this).val() === 'true' && $(this).last().attr('checked')) {
                    $('#item-12').find('input').removeAttr('disabled');
                } else {
                    $('input[name=fullUpFront][value=true]').attr('checked', true);
                    $('input[name=partialPayment]').attr('checked', false);
                    $('#item-12').find('input').attr('disabled', true);
                }
            });

            liveAndOnPageLoad('.hecs-form input[name=fullUpFront]', 'change', function() {
                if ($(this).is(':checked')) {
                    var $partialPayment = $('input[name=partialPayment]');

                    if ($(this).val() === 'true') {
                        $partialPayment.attr('disabled', 'true').removeAttr('checked');
                    } else {
                        $partialPayment.removeAttr('disabled');
                    }

                }
            });
        },
        applyCSPPhoneNumberToSidebar: function () {
            if ($('.cwlth-supported-place-clicker').size() > 0) {
                var data = '<div class="phone-number-section">Commonwealth<br/> supported places<br><p class="phone-number-container"><span class="phone-number">1300 322 870</span></p></div>';
                $('.phone-number-section-bottom').removeClass('phone-number-section-bottom').addClass('phone-number-section').parent().append(data);
            }
        },
        trackCareerAdviceNavigation: function () {
            var cat = "Career-Advice";
            var adv = "Interest-Advice";
            var red = "Readiness";
            var nxt = "Nav: Next-Page";
            var prv = "Nav: Prev-Page";
            var currentPageSelector = "div.career-advisor .pagination li.current";
            // The "by-interest" nav buttons:
            $('div.career-advisor form[action*="interestAdvice"] .form-buttons input.next').live('click', function () {
                trackEvent(cat, adv, nxt, parseInt($(currentPageSelector).text()));
            });
            $('div.career-advisor form[action*="interestAdvice"] .form-buttons input.previous').live('click', function () {
                trackEvent(cat, adv, prv, parseInt($(currentPageSelector).text()));
            });
            // The "online readiness" nav buttons:
            $('div.career-advisor form[action*="response"] .form-buttons input.next').live('click', function () {
                trackEvent(cat, red, nxt, parseInt($(currentPageSelector).text()));
            });
            $('div.career-advisor form[action*="response"] .form-buttons input.previous').live('click', function () {
                trackEvent(cat, red, prv, parseInt($(currentPageSelector).text()));
            });
        },
        trackSpecialCircumstancesNavigation: function () {
            var cat = "Special-Circumstances";
            var appl = "How-to-apply";
            var units = "Unit details";
            var pers = "Personal details";
            var descr = "Description";
            var decl = "Declaration";
            var nxt = "Nav: Next-Tab";
            var prv = "Nav: Prev-Tab";
            var tab = "Tab Select";
            var subm = "Submit";
            var currentPageSelector = "form[action*='scApply'] h2.open";
            // The nav buttons:
            $("form[action*='scApply'] .navigation a.next").live('click', function () {
                trackEvent(cat, cat, nxt, parseInt($(currentPageSelector).attr('id').substr(8, 1)));
            });
            $("form[action*='scApply'] .navigation a.prev").live('click', function () {
                trackEvent(cat, cat, prv, parseInt($(currentPageSelector).attr('id').substr(8, 1)));
            });
            // The tabs:
            $("form[action*='scApply'] #section-1-title a.form-section-name").live('click', function () {
                trackEvent(cat, appl, tab, 1);
            });
            $("form[action*='scApply'] #section-2-title a.form-section-name").live('click', function () {
                trackEvent(cat, units, tab, 2);
            });
            $("form[action*='scApply'] #section-3-title a.form-section-name").live('click', function () {
                trackEvent(cat, pers, tab, 3);
            });
            $("form[action*='scApply'] #section-4-title a.form-section-name").live('click', function () {
                trackEvent(cat, descr, tab, 4);
            });
            $("form[action*='scApply'] #section-5-title a.form-section-name").live('click', function () {
                trackEvent(cat, decl, tab, 5);
            });
            // The submit button:
            $("form[action*='scApply'] .btn-submit").live('click', function () {
                trackEvent(cat, cat, subm, parseInt($(currentPageSelector).attr('id').substr(8, 1)));
            });
        },
        ie6: ie6,
        chopMobileText: function() {
            if (isPhone() && !document.location.pathname.match(nonMobilePagesRegexp) &&
                    !document.cookie.match(/full-site-on-mobile=true/)) {
                // put the nav at the bottom of the page
                $('body').not('[class="home js-enabled"]').each(function() {
                    var nav = $('#navigation').detach();
                    nav.insertBefore($('#page-footer'));
                    nav.find('#navigation .primary ul li:first').css('display', 'block !important');
                    nav.find('#navigation .primary').css('display', 'block !important');
                });
                // link and prompt for non-mobile pages
                $('a').live('click', function(e) {
                    if (this.pathname.match(nonMobilePagesRegexp) &&
                            !confirm("This will take you to our full website.  Continue?")) {
                        e.preventDefault();
                    }
                });
                // link and prompt special case for "Enrol Now" form
                $('input[value~="Enrol"]').live('click', function(e) {
                    if (!confirm("This will take you to our full website.  Continue?")) {
                        e.preventDefault();
                    }
                });
                // change "Availability" to "Avail." to save space in tables
                $('thead .study-periods-column div, .availablity .data-wrapper').each(function(i, toChop) {
                    toChop.innerHTML = toChop.innerHTML.replace(/Availability/,
                            '<abbr title="Availability">Avail.</abbr>');
                });
                // some course and unit headings have non-breaking spaces
                $('h1').each(function(i, h1) {
                    h1.innerHTML = h1.innerHTML.replace(/&nbsp;/g, " ");
                });
                // take the cents off $ for shortlist (to make more space in table)
                $('form#shortlistForm div.price').each(function(i, priceText) {
                    priceText.innerHTML = priceText.innerHTML.replace(/(\$[0-9,]+)\.00/g, "$1");
                });
                // hide the name incase it's huge
                $('span#signed-in-msg').animate(
                { width: 0, paddingLeft: 0, paddingRight: 1 },
                { complete: function() {
                    this.innerHTML = "";
                } }
                        );
                // append unit code to unit title field for unit listing pages
                $('table tbody tr[class|=provider]').each(function(i, row) {
                    var unitCodeText = $(row).find('.unit-code:first').text().trim().toUpperCase();
                    $(row).find("td > div > a").append('<br/>' + unitCodeText);
                });
                $('div.academic-results div.completed-units table tbody tr').each(function(i, row) {
                    var unitCodeText = $(row).find('th').text().trim().toUpperCase();
                    $(row).find("td:first > div").append('<br/>' + unitCodeText);
                });
                // append unit code to unit title field for shortlist
                $('#shortlistForm tbody tr').each(function(i, row) {
                    var unitCodeText = $(row).find('td:first').text().trim().toUpperCase();
                    $(row).find("th + td + td a").append('<br/>' + unitCodeText);
                });
                // change email and telephone fields so the iPhone displays correct keyboard
                // (for some reason jQuery's ".attr('type', 'email')" doesn't work here)
                $('input[name="email"]').each(function(i, field) {
                    field.setAttribute('type', 'email');
                });
                $('input[name*="phone"]').each(function(i, field) {
                    field.setAttribute('type', 'tel');
                });
                // create a link to the full site
                $('<a id="full-site-link" href="#">View full site</a>')
                        .insertAfter('#page-footer')
                        .click(function() {
                    document.cookie = "full-site-on-mobile=true;path=/";
                    window.location.reload();
                });
                // remove extra phone number sections
                $(".phone-number-section:not(:first)").remove();
                // copy rss content out of sidebar into extra section above footer:
                $('#rss-sidebar').each(function () {
                    $(this).prepend("<h3>" + $(this).find("h1").text() + "</h3>").find("h1").remove(); // Convert title from h1 to h3
                    $('.courses-and-units-main').append('<div class="site-area-summary" id="rss-sidebar">' + $(this).html() + "</div>");
                });


                setTimeout(function () {
                    $('span.ZCT_Phone1').each(function () {
                        $(this).replaceWith("<a class='" + $(this).attr('class') + "'href='tel:" + $(this).text().replace(/[^\d]+/g, '') + "'>" + $(this).text() + "</a>");
                    });
                }, 1);
            }
            // if you're on a mobile but you've asked for the full site then allow
            // them to get back to the mobile site (if it's available for this page)
            if (isPhone() && document.cookie.match(/full-site-on-mobile=true/) &&
                    !document.location.pathname.match(nonMobilePagesRegexp)) {
                $('<a id="mobile-site-link" href="#">View mobile site</a>')
                        .insertAfter('#page-footer')
                        .click(function() {
                    document.cookie = "full-site-on-mobile=false;path=/";
                    window.location.reload();
                });
            }
        },
        handleAddUnitAjaxRequest: function() {

            function disable($elem) {
                $elem.addClass("disabled loading").attr('disabled', 'disabled').removeAttr('href');
            }

            function enable($elem) {
                $elem.removeClass("disabled loading").removeAttr('disabled');
            }

            $(".add-unit-controls").each(function() {
                var buttons = $(".add-unit-buttons", this);
                var messages = $(".add-unit-messages", this);
                $("a.add-button", buttons).click(function() {
                    var link = $(this);
                    var href = link.attr("href");
                    link.removeAttr("href").addClass("disabled loading");
                    $.post(href, function(data) {
                        var message = $(".add-unit-result", messages);
                        if (typeof data == "number") {
                            message.text(link.attr("data-success") || "Unit added");
                            var countTarget = link.attr("data-count-target");
                            if (countTarget) {
                                var countStatus = data + " unit" + (data != 1 ? "s" : "");
                                $(countTarget).text(countStatus);
                            }
                        } else {
                            message.text(link.attr("data-error") || "Error");
                            message.addClass("error");
                        }
                        buttons.fadeOut("fast", function() {
                            messages.fadeIn("fast");
                        });
                    }, "json");
                    return false;
                });
            });

            $('form.ajax-submit input[type=submit]').live('click', function (e) {
                e.preventDefault();
                var $this = $(this);
                var $form = $this.closest('form');
                var btn = $form.find("input.ajax-submit-button-val");
                if (btn.length == 0) {
                    btn = $("<input>").attr("type", "hidden")
                            .addClass("ajax-submit-button-val")
                            .appendTo($form);
                }
                btn.attr("name", $this.attr("name")).val($this.val());

                var checkboxes = $form.find('input[name=unit]:checked');

                if (checkboxes.size() === 0) {
                    setFlashMessage($this.closest('.ajax-feedback-container').find('.message-container'), $this.attr('data-none-selected'), 'warning');
                    return;
                }

                disable($this);

                $.post($form.attr('action'), $form.serialize(), function (data, status, xhr) {
                    checkboxes.replaceWith('<a href="' + publicOrSecure() + '/study-cart-and-wishlist" class="tick unit-added">Unit selected</a>');
                    setFlashMessage($this.closest('.ajax-feedback-container').find('.message-container'), checkboxes.size() + ' ' + $this.attr('data-success').replace('units', 'unit' + (checkboxes.size() == 1 ? '' : 's')), 'success');
                    enable($this);
                    var countTarget = $this.attr("data-count-target");
                    if (countTarget) {
                        var countStatus = data + " unit" + (data != 1 ? "s" : "");
                        $(countTarget).text(countStatus);
                    }
                }, "json");
            });
        },
        initCheckAccount: function() {

        	ajaxSubmitter('loginAccountForm', function (data, formId) {
                window.location.href = '/secure/enrol/validate-to-government-information';
            });

            ajaxSubmitter('claimAccountForm', function (data, formId) {
                $('.user-profile-form').html($(".user-profile-form", data).html());
                init_country("countryCode", "state", "AUS", "", 'AU');
                $('#salutationKey').focus();
                OUAWebsite.initTooltips();
                OUAWebsite.parsePasswordFields();
                unBlockPage();
            });

            function handleCheckAccount($form, e) {
              blockPage(e);
              $.ajax({
                type: "POST",
                url: $form.attr('action'),
                data: $form.serialize(),
                success: function (data) {
                  var $forms = $("form", data);

                  var $moreInfoForms = $forms.filter('#claimAccountForm, #loginAccountForm');
                  var $checkForm = $forms.filter('#checkAccountForm');
                  var $profileForm = $forms.filter('#userProfileForm');


                  if($checkForm.size() !== 0 ) {
                    $form.html($checkForm.html());
                    unBlockPage();
                  } else if ($moreInfoForms.size() !== 0) {
                    lightboxContent($moreInfoForms.outerHTML());
                  } else if ($profileForm) {
                    unBlockPage();
                    $('.user-profile-form').html($(".user-profile-form", data).html());
                    $('#salutationKey').focus();
                    OUAWebsite.initTooltips();
                    OUAWebsite.parsePasswordFields();
                  }
                },
                error: function () {
                  unBlockPage();
                  alert('Sorry, an error occured.');
                }
              });
            }

            $('#checkAccountForm').live('submit', function (e) {
              e.preventDefault();
              handleCheckAccount($(this), e);
            });

            // TODO - workaround for jquery 1.4.2 in IE7 http://bugs.jquery.com/ticket/6359
            $('#checkAccountForm input[type=submit]').live('click', function (e) {
                e.preventDefault();
                $(this).closest('form').submit();
            });
        },
        initTooltips: function($elem) {
            if(!$elem) {
                $elem = $('body')
            }
            $(window).resize(function () {
              $('.popover').remove();
            });

            $("a.ajax-popover", $elem).popover({
                trigger: "manual",
                placement: function(popover, trigger) {
                    return $(trigger).attr("data-placement") || "bottom";
                }
            }).click(function() {
                $("a.ajax-popover, a.inline-popover").not(this).popover("hide");
                var link = $(this);
                var href = toPublicOrSecure(link.attr("href"));
                if (link.attr("data-content")) {
                    link.popover("show");
                } else {
                    // IE does not like the page references
                    $.get(href.replace(/#.*$/, ''), function(data) {
                        var fragment = href.match(/#.+$/);
                        if (fragment != null) {
                            // This is to cater for the format of glossary items
                            var selector = 'dt' + fragment[0] + ' + dd, ' + fragment[0];
                            link.attr("data-content", $(selector, data).last().html());
                            link.popover("show");
                        }
                    }, "html");
                }
                return false;
            });

            $("a.inline-popover").popover({
                trigger: "manual",
                placement: function(popover, trigger) {
                    return $(trigger).attr("data-placement") || "bottom";
                },
                content: function() {
                    var $this = $(this);
                    var target = $this.attr("rel") || $this.attr("href");
                    target = "#" + target.replace(/.*#/, "");
                    return $(target).html();
                }
            }).click(function() {
                $("a.ajax-popover, a.inline-popover").not(this).popover("hide");
                $(this).popover("show");
                return false;
            });

            // prevent clicks within popover from reaching the document
            $("body").delegate(".popover", "click", function(e) {
                e.stopPropagation();
            });

            $(document).delegate("body", "click.popover.hide", function() {
                $("a.ajax-popover, a.inline-popover").popover("hide");
            }).delegate("body", "keyup.popover.hide", function(e) {
                if (e.keyCode == 27) {
                    $("a.ajax-popover, a.inline-popover").popover("hide");
                }
            });
        },
        collapseToggleHook: function() {
            $(".accordion-toggle").each(function() {
                var $this = $(this);
                var collapse = $this.attr("data-collapse") || "collapse";
                var expand = $this.attr("data-expand") || "expand";
                $("<span>").addClass("message in underline").text(collapse).appendTo(this);
                $("<span>").addClass("message out underline").text(expand).appendTo(this);
                var target = $this.attr("data-target");
                $(document)
                        .undelegate(target, 'shown')
                        .undelegate(target, 'hidden')
                        .delegate(target, 'shown', function() {
                            $this.addClass("in");
                            return false;
                        })
                        .delegate(target, 'hidden', function() {
                            $this.removeClass("in");
                            return false;
                        });
            });
        },
        enableOnRadioToggle: function() {
            $(".auto-enable").click(function() {
                var $this = $(this);
                var targetId = $this.attr("data-target-id");
	            if ($this.attr("checked")) {   
	                $("#" + targetId).removeAttr('disabled');
	            } else {
	            	$("#" + targetId).attr('disabled', 'disabled');
	            }
	        });
            // Set the initial state:
            $(".auto-enable").each(function() {
                var $this = $(this);
                var targetId = $this.attr("data-target-id");
	            if ($this.attr("checked")) {   
	                $("#" + targetId).removeAttr('disabled');
	            } else {
	            	$("#" + targetId).attr('disabled', 'disabled');
	            }
	        });
        },
        textInputVisibilityToggleListener: function() {
            $(document).delegate('input[type="text"][data-toggle-on-input], textarea[data-toggle-on-input]', 'input.toggle', function(e) {
                var trigger = $(this)
                  , target = $(trigger.attr('data-toggle-on-input'))
                  , val = trigger.val().trim()
                  , visible = target.is(':visible');

                if (!visible && val.length) {
                    target.slideDown();
                }
                if (visible && !val.length) {
                    target.slideUp().find('input[type="text"]').val('');
                }
            });
            $('input[type="text"][data-toggle-on-input], textarea[data-toggle-on-input]').trigger('input.toggle');
        },
        blockOnClickListeners: function() {
            $('.blockonclick').live('click', function (e) {
            	blockPage(e);
            });
        },
        displayLightBox:function () {
            $("a.lightbox-trigger.default").each(function() {
                var $this = $(this);               
                $this.fancybox({
                    'overlayColor':'#000',
                    'titleShow':false,
                    'transitionIn':'none',
                    'transitionOut':'none'
                });
            });

            $("a.lightbox-trigger.custom-size").each(function () {
                var $this = $(this);
                var $href = $this.attr("href") + "-link";
                var $target = $($href);
                $this.fancybox({
                    'overlayColor':'#000',
                    'autoDimensions':false,
                    'autoScale':false,
                    'titleShow':false,
                    'transitionIn':'none',
                    'transitionOut':'none',
                    'width':($target.find(".width").html() == null ? "10" : $target.find(".width").html().trim()),
                    'height':($target.find(".height").html() == null ? "10" : $target.find(".height").html().trim())
                });
            });
            
            /**
             * Hide the content when JS is enabled.
             */
            $("a.lightbox-trigger").each(function() {
                var $this = $(this);
                $this.show();
            });
            $(".lightbox-container").each(function() {
                var $this = $(this);
                $this.hide();

            });
        },
        genericTextbooksHandler: function() {
            var searchForm = $("#search-textbooks-form");
            var searchField = $("#search-textbooks-form input#query");

            // Return immediately if we are not on the generic textbooks page
            // (the #search-textbooks-form form only exists on the generic textbooks page)
            if (typeof searchField == "undefined" || searchForm == "undefined") {
                return;
            }

            $("#generic-textbooks-container .textbooks-by-unitcode").hide();

            var unitAutosuggestActionPath = searchForm.attr("data-autosuggest-action-path");
            var textBooksByUnitCodeActionPath = searchForm.attr("data-books-by-unit-code-action-path");

            var getTextbooksByUnitCode = function (unitCode) {
                $.ajax({
                    url:textBooksByUnitCodeActionPath,
                    type: 'post',
                    data:{
                        unitCode:unitCode
                    },
                    success:function (response) {
                        $(".textbooks-by-unitcode").fadeOut(function () {
                            $(".textbooks-by-unitcode").html(response);
                            $(".textbooks-by-unitcode").fadeIn();
                        });
                    }
                });
            };

            searchForm.submit(function(event) {
                event.preventDefault();

                var searchQuery = searchField.val();
                if (searchQuery != null && searchQuery.length > 0) {
                    $("#generic-textbooks-container .textbooks-by-unitcode").fadeOut(200);
                    searchField.autocomplete('search', searchQuery);
                }

                return false;
            });

            var buildResultItem = function(title, unitCode) {
                var typeSpan = '<span class="type">unit</span>';
                var titleSpan = '<span class="suggestion-title">' + title + '</span>';
                var unitCodeSpan = '<span class="unitcode">&nbsp;(' + unitCode + ')</span>';
                return typeSpan + titleSpan + unitCodeSpan;
            };

            var sourceHandler = function (request, response) {
                $.ajax({
                    url:unitAutosuggestActionPath,
                    dataType:"json",
                    data:{
                        term:request.term
                    },
                    success:function (data) {
                        if (data.length == 0) {
                            response([
                                {
                                    label: "<div class='no-results'>No results match &lsquo;" + request.term + "&rsquo;</div>",
                                    value: ""
                                }
                            ]);
                        } else {
                            response($.map(data, function (item) {
                                return {
                                    label:buildResultItem(item.title, item.unitCode),
                                    value:item.url,
                                    unitCode:item.unitCode,
                                    title:item.title
                                }
                            }));
                        }
                    }
                });
            };

            var focusHandler = function(event, ui) {
                event.preventDefault();
                var inputField = $(event.target);
                inputField.val(ui.item.title + " (" + ui.item.unitCode.toUpperCase() + ")");
            };

            var selectHandler = function(event, ui) {
                event.preventDefault();
                if (typeof ui.item.unitCode !== "undefined") {
                    getTextbooksByUnitCode(ui.item.unitCode);
                }
            };

            searchField.autocomplete({
                html: true,
                source: sourceHandler,
                select: selectHandler,
                focus: focusHandler
            });
        },
        /*studyPeriodLoadListener: function() {

            function isHourLoad() {
                return $("#studyLoadType").val() === 'hours';
            }

            function toLoadReference(str) {
                return "#" + str.toLowerCase().replace(/sess (\d)/, 'session$1') + "Load";
            }

            function saveType() {
                $.cookie("loadType", $("#studyLoadType").val(), { path: '/' });
            }
            
            function studyLoadAcknowledged() {
            	var cookie = $.cookie('cartAcknowledgement');
            	
            	return cookie && cookie.indexOf('ALL.study-load-warning') != -1;
            }
            
            function resetStudyLoadAcknowledgements() {
            	var cookie = $.cookie('cartAcknowledgement');
            	
            	if (cookie) {
	            	var vals = cookie.split("|");
	
	            	cookie = "";  
	            	for (var i=0; i < vals .length; i++) {
	            	    if ( vals[i] == 'ALL.study-load-warning' ) {
	            	        continue;
	            	    }
	            	    
	            	    if ( i > 0 && cookie.length > 0 )
	            	        cookie += '|';
	            	            
	            	    cookie += vals[i];
	            	}
	
	            	$.cookie('cartAcknowledgement', cookie, { path: '/' });
            	}
            }

            liveAndOnPageLoad('#studyLoadType, .study-cart-term', 'change', function() {

                var loads = {
                    'SP1': 0.0,
                    'SP2': 0.0,
                    'SP3': 0.0,
                    'SP4': 0.0,
                    'Sess 1': 0.0,
                    'Sess 2': 0.0,
                    'Sess 3': 0.0
                };

                var selectedUnits = {
                        'SP1': false,
                        'SP2': false,
                        'SP3': false,
                        'SP4': false,
                        'Sess 1': false,
                        'Sess 2': false,
                        'Sess 3': false                		
                };

                saveType();

                $('.study-cart-term:checked').each(function () {
                    var description = $(this).closest('label').attr('data-term-description');
                    if (loads[description] !== undefined) {
                    	var aLoad = parseFloat($(this).attr("data-eftsl"));
                        loads[description] += aLoad;
                    	selectedUnits[description] = (aLoad > 0.000000);
                    }
                });

                var term1FullLoad = 0.0;
                var term2FullLoad = 0.0;
                var term3FullLoad = 0.0;
                var term4FullLoad = 0.0;

                var term1Selected = false;
                var term2Selected = false;
                var term3Selected = false;
                var term4Selected = false;

                for (var key in loads) {
                    if (!loads.hasOwnProperty(key)) continue;
                    var totalLoad = Number(loads[key]) + Number($(toLoadReference(key)).attr('data-active-load'));
                    var hasCurrentUnits = Number($(toLoadReference(key)).attr('data-active-load')) > 0.000000;
                    if (isHourLoad()) {
                        $(toLoadReference(key)).text(round(totalLoad * 80, 1) + ' hrs' + (hasCurrentUnits ? '*' : ''));
                    } else {
                        $(toLoadReference(key)).text(round(totalLoad, 3) + (hasCurrentUnits ? '*' : ''));
                    }

                    if (key == 'SP1' || key == 'Sess 1') {
                        term1FullLoad += Number(totalLoad);
                        term1Selected = term1Selected || selectedUnits[key];
                    }

                    if (key == 'SP2') {
                        term2FullLoad += Number(totalLoad);
                        term2Selected = term2Selected || selectedUnits[key];
                    }

                    if (key == 'SP3' || key == 'Sess 2') {
                        term3FullLoad += Number(totalLoad);
                        term3Selected = term3Selected || selectedUnits[key];
                    }

                    if (key == 'SP4' || key == 'Sess 3') {
                        term4FullLoad += Number(totalLoad);
                        term4Selected = term4Selected || selectedUnits[key];
                    }
                }

				var hasTermLoadWarning = false;
				var hasTermLoadError = false;
				
                for (var i = 1; i <= 4; ++i) {
                    var load = eval('term' + i + 'FullLoad');

					if ( load > 0.25 && load <= 0.5 && eval('term' + i + 'Selected') && !studyLoadAcknowledged() ) {
						$('#sp' + i + 'Row').addClass("load-alert-row");
						$('#sp' + i + 'Row > td:first').addClass("load-alert-icon");
						
						$('#sp' + i + 'Row').removeClass("load-error-row");
						$('#sp' + i + 'Row > td:first').removeClass("load-error-icon");						
						
						if (!hasTermLoadWarning){ 
							hasTermLoadWarning = true;
						}
					}
					else if ( load > 0.5 && eval('term' + i + 'Selected') ) {
						$('#sp' + i + 'Row').addClass("load-error-row");
						$('#sp' + i + 'Row > td:first').addClass("load-error-icon");

						$('#sp' + i + 'Row').removeClass("load-alert-row");
						$('#sp' + i + 'Row > td:first').removeClass("load-alert-icon");
						
						if (!hasTermLoadError) {
							hasTermLoadError = true;
						}
					} 
					else {
						$('#sp' + i + 'Row').removeClass("load-alert-row");
						$('#sp' + i + 'Row > td:first').removeClass("load-alert-icon");
						$('#sp' + i + 'Row').removeClass("load-error-row");
						$('#sp' + i + 'Row > td:first').removeClass("load-error-icon");						
					}
				}
				
				if (hasTermLoadWarning) {
					if ( !studyLoadAcknowledged() ) {
						if ( !$('#study-load-warning').length ) {
							$('#study-load-container').append($('#studyLoadWarnings').html());
						}
						
						$('#study-load-container').addClass('warning-study-load-container');
						$('[data-study-load-msg="study-load-warning"]').removeClass('js-hidden');
					}
				}
				else {
					if ( !$('#study-load-error').length ) {
						$('#study-load-container').append($('#studyLoadErrors').html());
					}
					
					$('#study-load-container').removeClass('warning-study-load-container');
					$('[data-study-load-msg="study-load-warning"]').addClass('js-hidden');
				}
				
				if (hasTermLoadError) {
					$('#study-load-container').addClass('error-study-load-container');
					$('[data-study-load-msg="study-load-error"]').removeClass('js-hidden');
				}
				else {
					$('#study-load-container').removeClass('error-study-load-container');
					$('[data-study-load-msg="study-load-error"]').addClass('js-hidden');
				}

                resetStudyLoadAcknowledgements();
            });
        },*/
        emailToFriend: function() {
             var emailForm = $("#emailForm");
             if (emailForm.length != 0) {
                 $("a#cancel-email-link").click(function() {
                     $.fancybox.close();
                 });

                 $("a#emailFriend, a.emailFriend").fancybox({
                     'overlayColor'  : '#000',
                     'titleShow'     : 'false',
                     'transitionIn'  : 'none',
                     'transitionOut' : 'none',
                     'width'         : 440,
                     'height'        : 424,
                     'href'          : '#emailForm',
                     'onStart'       : function() {
                         $(".info-email-inner-container").show();
                         $("#email-confirmation").hide();

                         $("#friendName").val("");
                         $("#friendEmail").val("");
                         $("#message").val("");
                         if ($("#emailForm").attr("data-logged-in")=="true") {
                             $("#senderName").val($("#emailForm").attr("data-sender-name"));
                             $("#senderEmail").val($("#emailForm").attr("data-sender-email"));
                         }
                         else {
                             $("#senderName").val("");
                             $("#senderEmail").val("");
                         }

                         $("#error-friendName").hide();
                         $("#error-friendEmail").hide();
                         $("#error-senderName").hide();
                         $("#error-senderEmail").hide();
                     }
                 });

                 $("form#emailForm").submit(function(e) {
                     e.preventDefault();

                     var actionUrl = $(this).attr('action');

                     $.ajax({
                         url:       actionUrl,
                         type:     'POST',
                         dataType: 'json',
                         data:     $("form#emailForm").serialize(),
                         success:  function(response) {
                             $(".info-email-inner-container").hide();
                             $("#email-confirmation").show();
                         },
                         error: function(jqXhr, textStatus) {
                             var errors = $.parseJSON(jqXhr.responseText).errors;
                             var i;

                             for (i = 0; i < errors.length; i++) {
                                 var error = errors[i];
                                 var fieldName = error.field;
                                 var msg = error.defaultMessage;
                                 $("#error-" + fieldName).text(msg).show();
                             }
                         }
                     });

                     return false;
                 });
             }
        }
    };
}(jQuery);

(function ($) {

    $('.username-selection').live('focus change keyup', OUAWebsite.usernameSelection);
    
    $('input[name=q]').live('focus', OUAWebsite.clearSearchDefaults);
    $('input[name=q]').live('blur', OUAWebsite.returnSearchDefaults);
    
    OUAWebsite.courseBrowseListener();
    OUAWebsite.expandingCourseStreamListener();
    OUAWebsite.jsParameterLinkListener();
    OUAWebsite.enquiryExistingStudentSelectionListener();
    OUAWebsite.enquiryCategorySelectionListener();
    OUAWebsite.matchPairListener();
    OUAWebsite.textareaMaxlengthListener();
    OUAWebsite.tabListeners();
    OUAWebsite.toggleListeners();
    OUAWebsite.instantSearchListeners();
    OUAWebsite.faqListeners();
    OUAWebsite.academicStatementListeners();
    OUAWebsite.enrolmentListeners();
    OUAWebsite.termTipListeners();
    OUAWebsite.trackingListeners();
    OUAWebsite.searchAutocomplete();
    OUAWebsite.addressAutocomplete();
    OUAWebsite.chatListeners();
    OUAWebsite.salutationToGenderListener();
    OUAWebsite.mailingAddressSameAsHomeAddressListener();
    OUAWebsite.countryCodeChangeListener();
    OUAWebsite.deewrStudyCountryCodeChangeListener();
    OUAWebsite.birthCountryCodeChangeListener();
    OUAWebsite.phoneCallTrackingListener();
    OUAWebsite.deewrPriorStudyLiveListeners();
    OUAWebsite.deewrSecSchoolCodeChangeListener();
    OUAWebsite.deewrSecSchoolAustCodeChangeListener();
    OUAWebsite.deewrParentEdListener();
    OUAWebsite.deewrImpairmentListener();
    OUAWebsite.deewrSecSchoolTERChangeListener();
    OUAWebsite.deewrPre2010Listener();
    OUAWebsite.hecsFormListeners();
    OUAWebsite.flashCloseListener();
    OUAWebsite.linkJumpClickListener();
    OUAWebsite.initCheckAccount();

    
    $(document).ready(function () {
        // Temporarily disable the payment statement
        if (false) {
          $('a[href='+securePrefix()+'/my-oua/manage-enrolment/payment-statement-list"]').removeAttr('href').css('color','#000').attr('title','The web Payment service is currently unavailable due to technical issues. We are currently working to rectify the issue. Thank you for your patience.').text('Payment Statement (Temporarily unavailable)');
        }
    
        $('.toggle-content').each(function () {
            if ($(this).siblings('.visibility-toggler').find('input[type=checkbox]').attr('checked')) {
                $(this).show().siblings('.submit-options').removeClass('no-fields-present-submit-options');
            }
        });
    
        $('.username-selection').change();

        OUAWebsite.parsePasswordFields();
        OUAWebsite.focusOnLoad();
        OUAWebsite.courseBrowseInit();
        OUAWebsite.courseTabsInit();
        OUAWebsite.unitTabsInit();
        OUAWebsite.providerSliderInit();
        OUAWebsite.phoneEnquiryCategoriesInit();
        OUAWebsite.chopMobileText();
        OUAWebsite.showMoreLinksInit();
        OUAWebsite.collapsibleContainerInit();
        OUAWebsite.megaDropdownInit();
        OUAWebsite.trackGoogleCommerceOnSale();
        OUAWebsite.trackPaymentMethodSelections();
        OUAWebsite.applyCSPPhoneNumberToSidebar();
        OUAWebsite.trackCareerAdviceNavigation();
        OUAWebsite.trackSpecialCircumstancesNavigation();
        OUAWebsite.handleAddUnitAjaxRequest();
        OUAWebsite.studyPlanner();
        OUAWebsite.initTooltips();
        OUAWebsite.collapseToggleHook();
        OUAWebsite.enableOnRadioToggle();
        OUAWebsite.displayLightBox();
//        OUAWebsite.studyPeriodLoadListener();
        OUAWebsite.studyCart();
        OUAWebsite.textInputVisibilityToggleListener();
        OUAWebsite.blockOnClickListeners();
    
        // TODO:behrangs: Remove the checks when the feature goes live
        if (typeof genericTextbookSearchFeatureEnabled != "undefined" && genericTextbookSearchFeatureEnabled) {
            OUAWebsite.genericTextbooksHandler();
        }

        if (typeof socialbookmark1 != "undefined" && socialbookmark1) {
            OUAWebsite.emailToFriend();
        }
    
        $('.collapse').collapse({toggle:false});
    
        $('#rss-dropdown-button').hover(function(){$(this).addClass('hover');},function(){$(this).removeClass('hover');});

        if(isPhone()) {
            $('.social-media-links').removeClass('primary');
        }
        
        (function() {
        	var cookieName = "course_unit_history";
        	var cookieOptions = {
        		expires: 90,
        		path: "/"
        	};
        	var yearOfStudy = $("meta[name='yearOfStudy']").attr("content") || new Date().getFullYear();
    
        	var courseCode = $("meta[name='courseCode']").attr("content");
        	if (courseCode) {
        		$.cookie(cookieName, "COURSE-" + courseCode.toUpperCase() + "-" + yearOfStudy, cookieOptions);
        		return;
        	}
    
        	var unitCode = $("meta[name='unitCode']").attr("content");
        	if (unitCode) {
        		$.cookie(cookieName, "UNIT-" + unitCode.toUpperCase() + "-" + yearOfStudy, cookieOptions);
        	}
        })();
    });
})(jQuery);
    
function confirmSubmit(message, action, button, name) {
	var agree=confirm(message);

	if (agree) {
		button.name = name;
		button.form.action = action;
		return true;
	} else {
		return false;
	}
}
