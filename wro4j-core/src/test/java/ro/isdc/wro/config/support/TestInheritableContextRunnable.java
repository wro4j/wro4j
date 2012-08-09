package ro.isdc.wro.config.support;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestInheritableContextRunnable {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;
  private Context context;
  private static final Runnable NO_OP_RUNNABLE = new Runnable() {
    public void run() {
    }
  };
  
  @Before
  public void setUp() {
    initMocks(this);
    context = Context.webContext(mockRequest, mockResponse, mockFilterConfig);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullRunnable() {
    new InheritableContextRunnable(null);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenNoContextIsAvailable() {
    new InheritableContextRunnable(NO_OP_RUNNABLE);
  }
  
  @Test
  public void shouldInheritContextInCreatedThread()
      throws Exception {
    Context.set(context);
    WroTestUtils.runConcurrently(new InheritableContextRunnable(new Runnable() {
      public void run() {
        assertTrue(Context.isContextSet());
        assertNotSame(context, Context.get());
      }
    }), 1);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
}
