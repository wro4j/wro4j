/**
 *
 */
package ro.isdc.wro.resource;

import java.io.InputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;

/**
 * Test for {@link ServletContextUriLocator} class.
 *
 * @author Alex Objelean
 */
public class TestServletContextUriLocator {
  private final ServletContextUriLocator locator = new ServletContextUriLocator();
  @Before
  public void initContext() {
    final Context context = Mockito.mock(Context.class);
    Context.set(context);
    final ServletContext sc = Mockito.mock(ServletContext.class);
    Mockito.when(sc.getResourceAsStream(Mockito.anyString())).thenReturn(null);
    Mockito.when(context.getServletContext()).thenReturn(sc);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final RequestDispatcher dispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(dispatcher);
    Mockito.when(context.getRequest()).thenReturn(request);
    Mockito.when(context.getResponse()).thenReturn(response);
  }
  @After
  public void resetContext() {
    Context.unset();
  }
  @Test(expected=IllegalArgumentException.class)
  public void cannotAcceptNullUri() throws Exception {
    locator.locate(null);
  }
  @Test
  public void testSomeUri() throws Exception {
    final InputStream is = locator.locate("resourcePah");
    Assert.assertNotNull(is);
  }
}
