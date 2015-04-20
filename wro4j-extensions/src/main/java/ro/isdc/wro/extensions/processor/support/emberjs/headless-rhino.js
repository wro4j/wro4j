precompile = function (arg) {
	var result = Ember.Handlebars.precompile(arg, false);
	return result.toString();
};
