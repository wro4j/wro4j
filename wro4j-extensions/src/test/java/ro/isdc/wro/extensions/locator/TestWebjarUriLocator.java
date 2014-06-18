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
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * @author Alex Objelean
 */
public class TestWebjarUriLocator {
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
    victim = new WebjarUriLocator();
  }

  @Test
  public void shouldCreateValidUri() {
    assertEquals("webjar:/path/to/resource.js", WebjarUriLocator.createUri("/path/to/resource.js"));
  }

  @Test(expected = NullPointerException.class)
  public void cannotCreateValidUriFromNullArgument() {
    WebjarUriLocator.createUri(null);
  }

  @Test
  public void shouldAcceptKnownUri() {
    assertTrue(victim.accept(WebjarUriLocator.createUri("/path/to/resource.js")));
  }

  @Test
  public void shouldNotAcceptUnknown() {
    assertFalse(victim.accept("http://www.server.com/path/to/resource.js"));
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
  public void shouldNotFailWhenThereIsAWebjarResourceOutsideOfJar()
      throws IOException {
    assertNotEmpty(victim.locate("webjar:webjarFail.js"));
  }

  private void assertNotEmpty(final InputStream stream)
      throws IOException {
    IOUtils.read(stream, new byte[] {});
    stream.close();
  }

  @Test
  public void shouldLocateWebjarResourceContainingQuestionMarkInUri()
      throws Exception {
    victim.locate("webjar:font-awesome/4.0.3/fonts/fontawesome-webfont.woff?v=4.0.3");
  }
}
