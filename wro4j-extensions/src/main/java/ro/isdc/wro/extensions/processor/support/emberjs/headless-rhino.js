exports = {
    precompile: function () {
    } // will be replaced by ember-template-compiler
};

precompile = function (arg) {
    return exports.precompile(arg).toString();
};
