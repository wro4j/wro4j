/**
 * Creates location object used by less.js 
*/
var location = location || {
    protocol: "",
    hostname: "",
    port: ""
};
/**
 * The problem is that less.js runs in develoment mode and sets a timer (via
 * window.setInterval()). The env.rhino.js implementation of window.setInterval
 * spawns a backround thread (java.lang.Thread(Runnable)) to fire the timer
 * events, and the "window" object is never closed.
 */
window.less = {}; 
window.less.env = 'production';
var exports = {};

var lessIt = function(css) {
    var result;
    var parser = new less.Parser({ optimization: 2 });

    parser.parse(css, function (e, root) {
      if (e) {
          throw e;        
      }
      result = css;
      result = root.toCSS();
    });
    return result;
};
