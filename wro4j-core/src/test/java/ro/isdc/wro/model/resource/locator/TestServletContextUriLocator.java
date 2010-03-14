/**
 *
 */
package ro.isdc.wro.model.resource.locator;

import java.io.InputStream;

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
    final InputStream is = locator.locate("/css/resourcePath.css");
  }

  @After
  public void resetContext() {
    Context.unset();
  }
}
