(function() {

  try {
    allHellBreaksLoose();
    catsAndDogsLivingTogether();
  } catch (error) {
    print(error);
  } finally {
    cleanUp();
  }

}).call(this);