precompile = function (arg) {
	var result = Ember.Handlebars.precompile(arg);
	return result.toString();
};
