exports = {
    precompile: function () {
    } // will be replaced by ember-template-compiler
};
precompile = function (arg) {
	var result = Ember.Handlebars.precompile(arg);
	print(result)
	print(typeof result)
    return exports.precompile(arg).toString();
};
