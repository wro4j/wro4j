$(document).ready(function() {
    var streamBar = $("#stream-bar");
    if (streamBar.length == 0) return;

    var streams = $("#streams");

    $(".progress-container li.unit-block-completed, .progress-container li.unit-block-current").popover({
        placement: 'top',
        trigger: 'manual',
        title: function() {
            return $(this).attr("data-unit-code");
        },
        content: function() {
            return $(this).attr("data-unit-name");
        }
    });

    function hoverInHandler() {
        $(this).popover('show');
    }

    function hoverOutHandler() {
        $this = $(this);
        $this.popover('hide');
    }

    $(".progress-container li.unit-block-completed, .progress-container li.unit-block-current").hover(hoverInHandler, hoverOutHandler);
});