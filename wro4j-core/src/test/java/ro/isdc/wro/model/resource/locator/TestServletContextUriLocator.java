/**
 *
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;


/**
 * Test for {@link ServletContextUriLocator} class.
 *
 * @author Alex Objelean
 */
public class TestServletContextUriLocator {
  private final ServletContextUriLocator locator = new ServletContextUriLocator();


  @Before
  public void initContext() {
    final Context context = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
    Context.set(context);
  }


  @Test(expected = IllegalArgumentException.class)
  public void cannotAcceptNullUri()
    throws Exception {
    locator.locate(null);
  }


  @Test
  public void testWildcard1Resources() throws IOException {
    locator.locate(createUri("/css/*.css"));
  }

  @Test
  public void testWildcard2Resources() throws IOException {
    locator.locate(createUri("/css/*.cs?"));
  }

  @Test
  public void testWildcard3Resources() throws IOException {
    locator.locate(createUri("/css/*.???"));
  }

  @Test
  public void testRecursiveWildcardResources() throws IOException {
    locator.locate(createUri("/css/**.css"));
  }

  @Test
  public void testWildcardInexistentResources() throws IOException {
    locator.locate(createUri("/css/**.NOTEXIST"));
  }

  private String createUri(final String uri) throws IOException {
    final URL url = Thread.currentThread().getContextClassLoader().getResource("ro/isdc/wro/model/resource/locator/");
    Mockito.when(Context.get().getServletContext().getRealPath(Mockito.anyString())).thenReturn(url.getPath());
    //Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    return uri;
  }

  @Test
  public void testSomeUri()
    throws Exception {
    final InputStream is = locator.locate("resourcePath");
    Assert.assertNotNull(is);
  }

  /**
   * Make this test method to follow a flow which will throw IOException
   * @throws Exception
   */
  @Test/*(expected=IOException.class)*/
  public void testInvalidUrl()
    throws Exception {
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    locator.locate("/css/resourcePath.css");
  }

  @After
  public void resetContext() {
    Context.unset();
  }
}
