function f1() {
  var a = 1, b = 2, c;
  myconsole.debug("hello world");
  c = a + b;
  myconsole.log(c);c = c + 2;
}
myconsole.info("completed");

function myconsole() {
	return {
		info: function() {},
		log: function() {},
		debug: function() {}
	};
}