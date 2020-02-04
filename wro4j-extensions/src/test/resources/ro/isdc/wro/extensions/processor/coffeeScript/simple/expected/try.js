(function() {
  var error;

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