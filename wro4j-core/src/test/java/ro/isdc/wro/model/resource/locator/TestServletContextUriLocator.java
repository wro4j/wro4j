/**
 *
 */
package ro.isdc.wro.model.resource.locator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;


/**
 * Test for {@link ServletContextUriLocator} class.
 *
 * @author Alex Objelean
 */
public class TestServletContextUriLocator {
  private ServletContextUriLocator locator;


  @Before
  public void initContext() {
    final Context mockContext = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
    locator = new ServletContextUriLocator();
    final WroConfiguration config = new WroConfiguration();
    config.setConnectionTimeout(100);
    Mockito.when(mockContext.getConfig()).thenReturn(config);
    Context.set(mockContext);
  }


  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullUri()
    throws Exception {
    locator.locate(null);
  }


  @Test
  public void testWildcard1Resources()
    throws IOException {
    locator.locate(createUri("/css/*.css"));
  }


  @Test
  public void testWildcard2Resources()
    throws IOException {
    locator.locate(createUri("/css/*.cs?"));
  }


  @Test
  public void testWildcard3Resources()
    throws IOException {
    locator.locate(createUri("/css/*.???"));
  }


  @Test
  public void testRecursiveWildcardResources()
    throws IOException {
    locator.locate(createUri("/css/**.css"));
  }


  @Test
  public void testWildcardInexistentResources()
    throws IOException {
    locator.locate(createUri("/css/**.NOTEXIST"));
  }


  private String createUri(final String uri)
    throws IOException {
    final URL url = Thread.currentThread().getContextClassLoader().getResource("ro/isdc/wro/model/resource/locator/");
    Mockito.when(Context.get().getServletContext().getRealPath(Mockito.anyString())).thenReturn(url.getPath());
    // Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    return uri;
  }


  @Test
  public void testSomeUri()
    throws Exception {
    final InputStream is = locator.locate("resourcePath");
    Assert.assertNotNull(is);
  }


  /**
   * Make this test method to follow a flow which throws IOException
   */
  @Test
  public void testInvalidUrl()
    throws Exception {
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);

    final InputStream is = locator.locate("/css/resourcePath.css");
    // the response should be empty
    Assert.assertEquals(-1, is.read());
  }


  /**
   * Simulates a resource which redirects to some valid location.
   */
  @Test
  public void testRedirectingResource()
    throws Exception {
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    final InputStream is = simulateRedirectWithLocation("http://code.jquery.com/jquery-1.4.2.js");
    Assert.assertNotSame(-1, is.read());
  }


  /**
   * Simulates a resource which redirects to some valid location.
   */
  @Test(expected = IOException.class)
  public void testRedirectingResourceToInvalidLocation()
    throws Exception {
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    simulateRedirectWithLocation("http://INVALID/");
  }


  @Test
  public void shouldPreferServletContextBasedResolving()
    throws IOException {
    final InputStream is = new ByteArrayInputStream("a {}".getBytes());
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(is);

    final ServletContextUriLocator locator = new ServletContextUriLocator();
    locator.setLocatorStrategy(ServletContextUriLocator.LocatorStrategy.SERVLET_CONTEXT_FIRST);

    final InputStream actualIs = locator.locate("test.css");

    final BufferedReader br = new BufferedReader(new InputStreamReader(actualIs));
    Assert.assertEquals("a {}", br.readLine());
  }


  private InputStream simulateRedirectWithLocation(final String location)
    throws IOException {
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(Context.get().getResponse()).thenReturn(response);

    final RequestDispatcher requestDispatcher = new RequestDispatcher() {
      public void include(final ServletRequest request, final ServletResponse response)
        throws ServletException, IOException {
        final HttpServletResponse res = (HttpServletResponse)response;
        // valid resource
        res.sendRedirect(location);
      }


      public void forward(final ServletRequest request, final ServletResponse response)
        throws ServletException, IOException {
        throw new UnsupportedOperationException();
      }
    };

    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(Context.get().getRequest()).thenReturn(request);
    Mockito.when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(requestDispatcher);
    final InputStream is = locator.locate(location);
    return is;
  }


  @Test(expected = NullPointerException.class)
  public void cannotSetNullLocatorStrategy() {
    locator.setLocatorStrategy(null);
  }


  @After
  public void resetContext() {
    Context.unset();
  }
}
