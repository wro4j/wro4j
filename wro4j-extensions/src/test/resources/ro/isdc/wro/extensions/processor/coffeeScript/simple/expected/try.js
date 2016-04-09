(function() {
  var error, error1;

  try {
    allHellBreaksLoose();
    catsAndDogsLivingTogether();
  } catch (error1) {
    error = error1;
    print(error);
  } finally {
    cleanUp();
  }

}).call(this);