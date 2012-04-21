
define('testrequire2',[],function () {
	return {foo: "2"};
});

define('testrequire3',[],function () {
	return {foo: "2"};
});

define('testrequire1',["testrequire2", "testrequire3"],function (testrequire2, testrequire3) {
	return {modules: [testrequire2, testrequire3]};
});

/* RequireJSTransformer=on */
require(["testrequire1"], function (testrequire1) {
	console.log(testrequire1);
});

define("testrequire", function(){});
