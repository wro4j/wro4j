TA.createErrorPopup = function(e, nrLines, vld, catalog){
    return (function($){
        var t = (e.pageY + 10) + 'px', l = (e.pageX + 10) + 'px';
        var p = $("<ul class='error-popup lines-" + nrLines + "'></ul>").css({
            position: 'absolute',
            'z-index': 10001,
            top: t,
            left: l
        }).bgiframe();
        var v;
        for (var i = 0; i < vld.length; i++) {
            v = vld[i];
            var rowClass = v.isValid == null ? "warn" : v.isValid == true ? "ok" : "error";
            if (v.msg && v.msg.constructor == Array) {
                for (var j = 0; j < v.msg.length; j++) {
                    p.append("<li class=" + rowClass + ">" + (catalog ? catalog[v.msg[j]] : v.msg[j]) + "</li>");
                }
            }
            else {
                p.append("<li class=" + rowClass + ">" + (catalog ? catalog[v.msg] : v.msg) + "</li>");
            }
        }
        return p.appendTo('body');
    })(jQuery);
};

/**
 * Default TA error notifier.
 * @param {Object} ctl
 * @param {Object} params
 */
TA.notifyError = function(ctl, params){
    (function($){
        var inv = ctl.invalidities, el = ctl.feedbackElement || $(ctl.element), catalog = params[0], oldColor;
        
        var popup;
        if (inv > 0) {
            oldColor = el.css("background-color");
            if (el != ctl.element) {
              $(ctl.element).addClass('invalid');
            }
            el.addClass('invalid').mouseover(function(e){
                popup = TA.createErrorPopup(e, ctl.getValidations().length, ctl.getConditionedValidations(), catalog);
            }).mouseout(function(){
                if (popup) {
                    popup.remove();
                }
            });
        }
        else {
            el.removeClass('invalid').unbind('mouseover').unbind('mouseout');
            $('ul.error-popup').remove();
            if (el != ctl.element) {
              $(ctl.element).removeClass('invalid');
            }
        }
    })(jQuery);
};

/**
 * Resets an invalid group of controls to their valid state.
 * @param {Object} ctl: the currently validated control.
 * @param {Object} params: A list of optional parameters.
 */
TA.NotifyGroup = function(ctl, params){
    (function($){
      if (ctl.element.type == 'select-one') {
        TA.notifyError(ctl, [params[1]]);
      }
      else {
        var ctx = params[0];
        var selector = 'input:checked,input.invalid';
        var grp = $(selector, ctx);
        grp.each(function(){
          if (ctl != this.ctl) {
            this.ctl.invalidities = ctl.invalidities;
            var vlds = this.ctl.getValidations();
            var srcValidations = ctl.getValidations();
            var v, srcV;
            for (var j = 0; j < vlds.length; j++) {
              v = vlds[j];
              srcV = srcValidations[j];
              v.isValid = srcV.isValid;
              v.msg = srcV.msg;
              v.executed = srcV.executed;
            }
          }
          TA.notifyError(this.ctl, [params[1]]);
        });
        if (!ctl.element.checked) {
          ctl.invalidities = 0;
        }
        if (ctl.element.type == 'radio') {
          var fls = $(ctl.element).parents('fieldset');
          $('input', fls).not('#' + ctl.element.id.replace(/:/g, '\\:')).each(function(){
            this.ctl.invalidities = 0;
            TA.notifyError(this.ctl, []);
          });
        }
      }
    })(jQuery);
}
/**
 * Disables the button passed as the item 0 of the optional param array.
 * @param {Object} ctl: the currently validated control.
 * @param {Object} params: A list of optional parameters.
 */
TA.disableButton = function(ctl, params){
    (function($){
        if (ctl.invalidities > 0) {
            params[0].attr('disabled', 'disabled');
        }
        else {
            params[0].removeAttr('disabled');
        }
    })(jQuery);
}
