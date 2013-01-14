/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestLifecycleCallbackDecorator {
  private LifecycleCallbackDecorator decorator;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    decorator = new LifecycleCallbackDecorator(null);
  }

  @Test
  public void shouldCatchCallbacksExceptionsAndContinueExecution() {
    final LifecycleCallback callback = Mockito.spy(new PerformanceLoggerCallback());
    decorator = new LifecycleCallbackDecorator(callback);

    final LifecycleCallbackRegistry registry = new LifecycleCallbackRegistry();
    registry.registerCallback(decorator);

    registry.onBeforeModelCreated();
    registry.onAfterModelCreated();
    registry.onBeforePreProcess();
    registry.onAfterPreProcess();
    registry.onBeforePostProcess();
    registry.onAfterPostProcess();
    registry.onBeforeMerge();
    registry.onAfterMerge();
    registry.onProcessingComplete();

    Mockito.verify(callback).onBeforeModelCreated();
    Mockito.verify(callback).onAfterModelCreated();
    Mockito.verify(callback).onBeforePreProcess();
    Mockito.verify(callback).onAfterPreProcess();
    Mockito.verify(callback).onBeforePostProcess();
    Mockito.verify(callback).onAfterPostProcess();
    Mockito.verify(callback).onBeforeMerge();
    Mockito.verify(callback).onAfterMerge();
    Mockito.verify(callback).onProcessingComplete();
  }
}
