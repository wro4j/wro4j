package ro.isdc.wro.config.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.Callable;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestContextPropagatingCallable {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;
  private Context context;
  private static final Callable<Void> NO_OP_CALLABLE = new Callable<Void>() {
    public Void call()
        throws Exception {
      return null;
    }
  };

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
    initMocks(this);
    context = Context.webContext(mockRequest, mockResponse, mockFilterConfig);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullCallable() {
    new ContextPropagatingCallable<Void>(null);
  }

  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenNoContextIsAvailable() {
    Context.unset();
    new ContextPropagatingCallable<Void>(NO_OP_CALLABLE);
  }

  @Test
  public void shouldInheritContextInCreatedThread()
      throws Exception {
    Context.set(context);
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        assertTrue(Context.isContextSet());
        assertSame(context, Context.get());
        return null;
      }
    }), 1);
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullRunnable() {
    final Runnable runnable = null;
    ContextPropagatingCallable.decorate(runnable);
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullCallable() {
    final Callable<?> callable = null;
    ContextPropagatingCallable.decorate(callable);
  }

  @Test
  public void shouldDecorateRunnable()
      throws Exception {
    Context.set(context);
    WroTestUtils.runConcurrently(ContextPropagatingCallable.decorate(new Callable<Void>() {
      public Void call()
          throws Exception {
        assertTrue(Context.isContextSet());
        assertSame(context, Context.get());
        return null;
      }
    }), 1);
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
