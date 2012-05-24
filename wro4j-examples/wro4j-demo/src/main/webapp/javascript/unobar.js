$(document).ready(function() {

    function publicPrefix() {
    	return '/public';
    }
    
    function securePrefix() {
    	return '/secure';
    }
    
    $('#submit-search').click(function(){
    	if ($('#uno-search-box').val() == "") { //don't submit the search form for empty searches
    		return false;
    	} 	
    });	
    
    $('#search-results a').live('click', function () {
    	$('#submit-search').attr('disabled','true').
    		prev('.loading').addClass('show-it');
    });	
    
//    $('#submit-search').prev('.loading').ajaxStart(function(){ $(this).addClass('show-it'); }).ajaxStop(function(){ $(this).removeClass('show-it'); });
    
    function createFilterQuery(target) {
        var excludes = [];

        target = $(target);

        var uncheckedFilters = target.find('li.filter input.filter').not(":checked");

        $.each(uncheckedFilters, function(filterIndex, filter) {
            var filterName = $(filter).attr('name');
            excludes.push("x=" + encodeURIComponent(filterName));
        });

        if (excludes.length > 0) {
            return "&" + excludes.join("&");
        } else {
            return "";
        }

    }

    function createEndpointUrl(term, target) {
        var baseUrl = publicPrefix()+"/search/fragment/autocomplete?q=" + encodeURIComponent(term);
        return baseUrl + createFilterQuery(target);
    }

    function installFilter(unoSearchContainer) {
        var searchFiltersTemplate = unoSearchContainer.find(".search-filters-container .search-filters").eq(0);
        var clonedSearchFilters = searchFiltersTemplate.clone();

        // workaround for IE 8 bug: copying the checked state of checkboxes over to the cloned element
        // (looks like this bug is fixed in newer versions of jQuery http://bugs.jquery.com/ticket/3879)
        searchFiltersTemplate.find("input.filter").each(function (index, elem) {
            var targetElem = clonedSearchFilters.find("input[name='" + $(elem).attr('name') + "']").eq(0);
            var checked = $(elem).is(":checked");
            if (checked) {
                targetElem.attr('checked', true);
            } else {
                targetElem.removeAttr('checked');
            }
        });

        var searchFiltersElem = $("<li class='filter'>").prepend(clonedSearchFilters);
        unoSearchContainer.find(".search-results .ui-autocomplete").prepend(searchFiltersElem);
        installFilterListeners(unoSearchContainer);
    }

    function installFilterListeners(unoSearchContainer) {
        unoSearchContainer.find("li.filter input.filter").unbind();

        var changeHandler = function() {
            var filterName = $(this).attr('name');
            var filterElem = unoSearchContainer.find(".search-filters-container input.filter[name='" + filterName + "']");
            if ($(this).is(":checked")) {
                filterElem.attr('checked', 'checked');
            } else {
                filterElem.removeAttr('checked');
            }
            unoSearchContainer.find("input.search-box").focus();
            unoSearchContainer.find("input.search-box").autocomplete("search");
        };


        unoSearchContainer.find("li.filter input.filter").change(changeHandler);
    }

    function installMoreOptionsListener(unoSearchContainer) {
        var moreOptions = $(unoSearchContainer).find(".search-results .more-options");

        $(moreOptions).click(function(e) {
            e.preventDefault();

            var href = $(this).attr('href');
            href += "?q=" + $(unoSearchContainer).find(".search-box").val();
            window.location = href;

            return false;
        });
    }

    var unoSearch = function(selector) {
        selector = typeof selector == "undefined" ? ".uno-search" : selector;
        var targets = $(selector);

        $.each(targets, function(index, target) {
            target = $(target);
            var searchResultsContainer = target.find(".search-results");

            target.find("label.js-shown").css('display', 'block');

            if (isPhone()) {
                target.find("label.search-box-label").html("Search");
            }

            target.find("label.search-box-label").inFieldLabels();

            var createHandler = function(event, ui) {
                installFilter(target);
            };

            var openHandler = function(event, ui) {
                installFilter(target);
                installMoreOptionsListener(target);

                target.find('.ui-autocomplete li a').removeClass('ui-corner-all');
                target.find('.ui-autocomplete').css('z-index', '2000');

                if (!isPhone()) {
                    target.find('.ui-autocomplete').css ({
                        'marginTop': "-5px",
                        'marginLeft': "0"
                    });
                }

                // Fix for shaky search results in Firefox
                // https://jira.open.edu.au/browse/GHTEAMC-128
                // if ($.browser.mozilla) {
                   // var left = Math.round(target.find('.search-box').offset().left);
                   // target.find('.ui-autocomplete').offset({'left' : left - 1});
                // }
            };

            var focusHandler = function(event, ui) {
                event.preventDefault();
            };

            var selectHandler = function(event, ui) {
                document.location.href = ui.item.label.match(/<span class='url'>(.*)<\/span>/)[1];
                event.preventDefault();
            };

            var sourceHandler = function (request, response) {
                var successHandler = function(data, x, y, z) {
                    $('.ui-autocomplete').css({
                        'marginTop': "0",
                        "marginLeft" : "0"
                    });

                    if (data.suggestions.length == 0) {
                        response([{
                            label: "<em>No results match " + request.term + "</em>",
                            value: ""
                        }]);
                    } else {
                        response($.map(data.suggestions, function (item) {
                            var securityPrefix = window.location.pathname.match(/\/public\//) ? publicPrefix() : securePrefix();
                            var label = (item.type.match(/content/i) ? "<span class='type content'>CONTENT</span>" : "<span class='type " +  item.type.toLowerCase() + "'>" + item.type.toUpperCase() + "</span>") +
                                "<span class='suggestion-title'>" + item.highlightedTitle + "</span>" +
                                (item.unitcode === null ? "" : " <span class='unitcode'>(" + item.unitcode + ")</span>") +
                                "<span class='url'>" + item.url.replace(/^\/(public|secure)/, securityPrefix) + "</span>";
                            return {
                                label: label,
                                value: item.title
                            };
                        }));
                    }
                };

                $.ajax({
                    type: "POST",
                    url: createEndpointUrl(request.term, target),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: successHandler
                });
            };

            target.find(".search-box").autocomplete({
                html: true,
                appendTo: searchResultsContainer,
                create: createHandler,
                open: openHandler,
                focus: focusHandler,
                select: selectHandler,
                source: sourceHandler
            });
        });
    };

    unoSearch()
});