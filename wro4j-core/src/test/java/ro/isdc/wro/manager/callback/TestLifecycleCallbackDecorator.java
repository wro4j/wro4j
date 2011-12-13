/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Alex Objelean
 */
public class TestLifecycleCallbackDecorator {
  private LifecycleCallbackDecorator decorator;


  @Test(expected=NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    decorator = new LifecycleCallbackDecorator(null);
  }

  @Test
  public void shouldCatchCallbacksExceptionsAndContinueExecution() {
    final LifecycleCallback callback = Mockito.spy(new PerformanceLoggerCallback());
    decorator = new LifecycleCallbackDecorator(callback);
    
    
    LifecycleCallbackRegistry registry = new LifecycleCallbackRegistry();
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
