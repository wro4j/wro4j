package ro.isdc.wro.extensions.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;


/**
 * @author Alex Objelean
 */
public class TestWebjarResourceLocator {
  private ResourceLocatorFactory victim;

  @Before
  public void setUp() {
    victim = new WebjarResourceLocatorFactory();
  }

  @Test
  public void shouldCreateValidUri() {
    assertEquals("webjar:/path/to/resource.js", WebjarResourceLocator.createUri("/path/to/resource.js"));
  }

  @Test(expected = NullPointerException.class)
  public void cannotCreateValidUriFromNullArgument() {
    WebjarResourceLocator.createUri(null);
  }

  @Test
  public void shouldAcceptKnownUri() {
    final ResourceLocator locator = victim.getLocator(WebjarResourceLocator.createUri("/path/to/resource.js"));
    assertNotNull(locator);
  }

  @Test
  public void shouldNotAcceptUnknown() {
    final ResourceLocator locator = victim.getLocator(WebjarResourceLocator.createUri("http://www.server.com/path/to/resource.js"));
    assertNull(locator);
  }

  @Test
  public void shouldFindValidWebjar()
      throws Exception {
    assertNotEmpty(victim.locate("webjar:jquery.js"));
    assertNotEmpty(victim.locate("webjar:jquery/2.0.0/jquery.js"));
    assertNotEmpty(victim.locate("webjar:/jquery/2.0.0/jquery.js"));
  }

  @Test(expected = IOException.class)
  public void cannotFindInvalidWebjar()
      throws Exception {
    victim.locate("webjar:invalid.js");
  }
  
  @Test
  public void shouldNotFailWhenThereIsAWebjarResourceOutsideOfJar() throws IOException {
	  assertNotEmpty(victim.locate("webjar:webjarFail.js"));
  }

  private void assertNotEmpty(final InputStream stream)
      throws IOException {
    IOUtils.read(stream, new byte[] {});
    stream.close();
  }
}
