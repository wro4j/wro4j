/**
 *
 */
package ro.isdc.wro.model.resource.locator.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator.LocatorStrategy;


/**
 * Test for {@link ServletContextUriLocator} class.
 *
 * @author Alex Objelean
 */
public class TestServletContextResourceLocator {
  private ServletContextResourceLocator victim;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    Mockito.when(mockRequest.getServletPath()).thenReturn("");
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);

    final Context context = Context.webContext(mockRequest, mockResponse, mockFilterConfig);

    final WroConfiguration config = new WroConfiguration();
    config.setConnectionTimeout(100);
    Context.set(context, config);
  }

  private void useLocator(final ServletContextResourceLocator locator) {
    Validate.notNull(locator);
    this.victim = locator;
    new InjectorBuilder().build().inject(locator);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullUri()
      throws Exception {
    victim = new ServletContextResourceLocator(mockServletContext, null);
  }

  @Test
  public void shouldAcceptNullServletContext()
      throws Exception {
    victim = new ServletContextResourceLocator(mockServletContext, "");
    Assert.assertNotNull(victim);
  }

  @Test
  public void testWildcard1Resources()
      throws IOException {
    victim = new ServletContextResourceLocator(mockServletContext, createUri("/css/*.css"));
    Assert.assertNotNull(victim.getInputStream());
  }

  @Test
  public void testWildcard2Resources()
      throws IOException {
    victim = new ServletContextResourceLocator(mockServletContext, createUri("/css/*.cs?"));
    Assert.assertNotNull(victim.getInputStream());
  }

  @Test
  public void testWildcard3Resources()
      throws IOException {
    victim = new ServletContextResourceLocator(mockServletContext, createUri("/css/*.???"));
    Assert.assertNotNull(victim.getInputStream());
  }

  @Test
  public void testRecursiveWildcardResources()
      throws IOException {
    victim = new ServletContextResourceLocator(mockServletContext, createUri("/css/**.css"));
    Assert.assertNotNull(victim.getInputStream());
  }

  @Test(expected = IOException.class)
  public void testWildcardInexistentResources()
      throws IOException {
    useLocator(new ServletContextResourceLocator(mockServletContext, createUri("/css/**.NOTEXIST")));
    victim.getInputStream();
  }

  private String createUri(final String uri)
      throws IOException {
    return createUri(uri, "ro/isdc/wro/model/resource/locator/");
  }

  private String createUri(final String uri, final String path)
      throws IOException {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Mockito.when(mockServletContext.getRealPath(Mockito.anyString())).thenReturn(url.getPath());
    // Mockito.when(Context.get().getServletContext().getRequestDispatcher(Mockito.anyString())).thenReturn(null);
    return uri;
  }

  @Test(expected = IOException.class)
  public void testSomeUri()
      throws Exception {
    useLocator(new ServletContextResourceLocator(mockServletContext, createUri("resourcePath")));
    victim.getInputStream();
  }

  /**
   * Make this test method to follow a flow which throws IOException
   */
  @Test(expected = IOException.class)
  public void testInvalidUrl()
      throws Exception {
    Mockito.when(mockServletContext.getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(mockServletContext.getRequestDispatcher(Mockito.anyString())).thenReturn(null);

    useLocator(new ServletContextResourceLocator(mockServletContext, "/css/resourcePath.css"));
    final InputStream is = victim.getInputStream();
    // the response should be empty
    assertEquals(-1, is.read());
  }

  @Test
  public void shouldPreferServletContextBasedResolving()
      throws IOException {
    final InputStream is = new ByteArrayInputStream("a {}".getBytes());
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(is);

    useLocator(new ServletContextResourceLocator(mockServletContext, "test.css"));
    victim.setLocatorStrategy(ServletContextResourceLocator.LocatorStrategy.SERVLET_CONTEXT_FIRST);

    final InputStream actualIs = victim.getInputStream();

    final BufferedReader br = new BufferedReader(new InputStreamReader(actualIs));
    assertEquals("a {}", br.readLine());
  }

  @Test(expected = IOException.class)
  public void shouldNotInvokeDispatcherWhenServletContextOnlyStrategyIsUsed() throws Exception {
    final AtomicBoolean dispatcherInvokedFlag = new AtomicBoolean();
    useLocator(new ServletContextResourceLocator(mockServletContext, "/test.css") {
      @Override
      InputStream locateWithDispatcher(final String uri)
          throws IOException {
        dispatcherInvokedFlag.set(true);
        throw new IOException("No resource exist");
      }
    });
    victim.setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_ONLY);
    try {
      victim.getInputStream();
    } finally {
      assertFalse(dispatcherInvokedFlag.get());
    }
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullLocatorStrategy() {
    victim = new ServletContextResourceLocator(mockServletContext, "/doesntMatter");
    victim.setLocatorStrategy(null);
  }


  @Test
  public void shouldFindWildcardResourcesForFolderContainingSpaces()
      throws IOException {
    useLocator(new ServletContextResourceLocator(mockServletContext, createUri("/folder with spaces/**.css", "test")));
    victim.getInputStream();
  }

  @After
  public void resetContext() {
    Context.unset();
  }
}