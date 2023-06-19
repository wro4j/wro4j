package ro.isdc.wro.extensions;

/**
 * Records for external JavaScript library dependencies.
 * 
 * @author Paul Podgorsek
 */
public record ExternalLibrary(String name, String version) {

	public static final ExternalLibrary COFFEE_SCRIPT = new ExternalLibrary("coffee-script.min.js", "1.12.7");
	public static final ExternalLibrary FONT_AWESOME = new ExternalLibrary("fontawesome.js", "5.15.1");
	public static final ExternalLibrary JQUERY = new ExternalLibrary("jquery.min.js", "3.5.1");
	public static final ExternalLibrary REQUIRE_JS = new ExternalLibrary("requirejs/2.3.6/bin/requirejs.js", "2.3.6");

}
