(function() {
  var food, i, len, ref;

  ref = ['toast', 'cheese', 'wine'];
  for (i = 0, len = ref.length; i < len; i++) {
    food = ref[i];
    eat(food);
  }

}).call(this);