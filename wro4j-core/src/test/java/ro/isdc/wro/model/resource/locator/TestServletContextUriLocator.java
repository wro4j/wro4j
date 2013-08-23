/**
 *
 */
package ro.isdc.wro.model.resource.locator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator.LocatorStrategy;


/**
 * Test for {@link ServletContextUriLocator} class.
 *
 * @author Alex Objelean
 */
public class TestServletContextUriLocator {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  private ServletContextUriLocator victim;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    when(mockRequest.getServletPath()).thenReturn("");
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);

    final Context context = Context.webContext(mockRequest, mockResponse, mockFilterConfig);
    final WroConfiguration config = new WroConfiguration();
    config.setConnectionTimeout(100);
    Context.set(context, config);

    victim = new ServletContextUriLocator();

    initLocator(victim);
  }

  /**
   * Initialize the locator by injecting all required fields.
   */
  private void initLocator(final ServletContextUriLocator locator) {
    final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
    injector.inject(locator);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullUri()
      throws Exception {
    victim.locate(null);
  }

  @Test
  public void testWildcard1Resources()
      throws IOException {
    victim.locate(createUri("/css/*.css"));
  }

  @Test
  public void testWildcard2Resources()
      throws IOException {
    victim.locate(createUri("/css/*.cs?"));
  }

  @Test
  public void testWildcard3Resources()
      throws IOException {
    victim.locate(createUri("/css/*.???"));
  }

  @Test
  public void testRecursiveWildcardResources()
      throws IOException {
    victim.locate(createUri("/css/**.css"));
  }

  @Test
  public void shouldFindWildcardResourcesForFolderContainingSpaces()
      throws IOException {
    victim.locate(createUri("/folder with spaces/**.css", "test"));
  }

  @Test(expected = IOException.class)
  public void testWildcardInexistentResources()
      throws IOException {
    victim.locate(createUri("/css/**.NOTEXIST"));
  }

  private String createUri(final String uri)
      throws IOException {
    return createUri(uri, "ro/isdc/wro/model/resource/locator/");
  }

  private String createUri(final String uri, final String path)
      throws IOException {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    when(mockServletContext.getRealPath(Mockito.anyString())).thenReturn(url.getPath());
    return uri;
  }

  /**
   * Make this test method to follow a flow which throws IOException
   */
  @Test(expected = IOException.class)
  public void testInvalidUrl()
      throws Exception {
    victim.locate("/invalid/resource.css");
  }

  @Test
  public void shouldPreferServletContextBasedResolving()
      throws IOException {
    final InputStream is = new ByteArrayInputStream("a {}".getBytes());
    Mockito.when(Context.get().getServletContext().getResourceAsStream(Mockito.anyString())).thenReturn(is);

    final ServletContextUriLocator locator = new ServletContextUriLocator();
    initLocator(locator);
    locator.setLocatorStrategy(ServletContextUriLocator.LocatorStrategy.SERVLET_CONTEXT_FIRST);

    final InputStream actualIs = locator.locate("test.css");

    final BufferedReader br = new BufferedReader(new InputStreamReader(actualIs));
    assertEquals("a {}", br.readLine());
  }

  @Test(expected = IOException.class)
  public void shouldNotInvokeDispatcherWhenServletContextOnlyStrategyIsUsed() throws Exception {
    final AtomicBoolean dispatcherInvokedFlag = new AtomicBoolean();
    victim = new ServletContextUriLocator() {
      @Override
      InputStream locateWithDispatcher(final String uri)
          throws IOException {
        dispatcherInvokedFlag.set(true);
        throw new IOException("No resource exist");
      }
    };
    initLocator(victim);
    victim.setLocatorStrategy(LocatorStrategy.SERVLET_CONTEXT_ONLY);
    try {
      victim.locate("/test.css");
    } finally {
      assertFalse(dispatcherInvokedFlag.get());
    }
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullLocatorStrategy() {
    victim.setLocatorStrategy(null);
  }

  @After
  public void resetContext() {
    Context.unset();
  }
}
