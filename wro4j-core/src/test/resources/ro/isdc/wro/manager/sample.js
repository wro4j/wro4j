/**
 * This is a test function.
 */
function bar(a) {
	try {
		// this is a single line comment
		foo();
	} catch (e) {
		alert("Exception caught (foo not defined)");
	}
	alert(a);
	// end
}
bar(10);