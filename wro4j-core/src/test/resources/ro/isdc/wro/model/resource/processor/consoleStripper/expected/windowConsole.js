var msg = '[jquery.form] ' + Array.prototype.join.call(arguments,'');
if (window.console && window.console.log) {
  
}
else if (window.opera && window.opera.postError) {
  window.opera.postError(msg);
}