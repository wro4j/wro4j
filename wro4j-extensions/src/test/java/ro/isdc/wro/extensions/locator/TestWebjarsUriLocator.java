package ro.isdc.wro.extensions.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.ExternalLibrary;
import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * @author Alex Objelean
 */
public class TestWebjarsUriLocator {

	private UriLocator victim;

	@BeforeClass
	public static void onBeforeClass() {
		assertEquals(0, Context.countActive());
	}

	@AfterClass
	public static void onAfterClass() {
		assertEquals(0, Context.countActive());
	}

	@Before
	public void setUp() {
		victim = new WebjarsUriLocator();
	}

	@Test
	public void shouldCreateValidUri() {
		assertEquals("webjars:/path/to/resource.js", WebjarsUriLocator.createUri("/path/to/resource.js"));
	}

	@Test(expected = NullPointerException.class)
	public void cannotCreateValidUriFromNullArgument() {
		WebjarUriLocator.createUri(null);
	}

	@Test
	public void shouldAcceptKnownUri() {
		assertTrue(victim.accept(WebjarsUriLocator.createUri("/path/to/resource.js")));
	}

	@Test
	public void shouldNotAcceptUnknown() {
		assertFalse(victim.accept("http://www.server.com/path/to/resource.js"));
	}

	@Test
	public void shouldFindValidWebjar() throws Exception {
		assertNotEmpty(victim.locate("webjars:jquery.js"));
		assertNotEmpty(victim.locate("webjars:jquery/" + ExternalLibrary.JQUERY.version() + "/jquery.js"));
		assertNotEmpty(victim.locate("webjars:/jquery/" + ExternalLibrary.JQUERY.version() + "/jquery.js"));
	}

	@Test(expected = IOException.class)
	public void cannotFindInvalidWebjar() throws Exception {
		victim.locate("webjars:invalid.js");
	}

	@Test
	public void shouldNotFailWhenThereIsAWebjarResourceOutsideOfJar() throws IOException {
		assertNotEmpty(victim.locate("webjars:webjarFail.js"));
	}

	private void assertNotEmpty(final InputStream stream) throws IOException {
		IOUtils.read(stream, new byte[] {});
		stream.close();
	}

	@Test
	public void shouldLocateWebjarResourceContainingQuestionMarkInUri() throws Exception {
		victim.locate("webjars:font-awesome/" + ExternalLibrary.FONT_AWESOME.version() + "/webfonts/fa-regular-400.woff?v="
				+ ExternalLibrary.FONT_AWESOME.version());
	}

}
