(function() {
  var error;

  alert((function() {
    try {
      return nonexistent / void 0;
    } catch (error1) {
      error = error1;
      return "And the error is ... " + error;
    }
  })());

}).call(this);