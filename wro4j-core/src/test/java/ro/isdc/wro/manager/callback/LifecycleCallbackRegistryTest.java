/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Alex Objelean
 */
public class LifecycleCallbackRegistryTest {
  private LifecycleCallbackRegistry registry;

  @Before
  public void setUp() {
    registry = new LifecycleCallbackRegistry();
  }

  @Test(expected=NullPointerException.class)
  public void shouldNotAcceptNullCallback() {
    registry.registerCallback(null);
  }

  @Test
  public void shouldInvokeRegisteredCallbacks() {
    final LifecycleCallback callback = Mockito.mock(LifecycleCallback.class);
    registry.registerCallback(callback);

    registry.onBeforeModelCreated();
    Mockito.verify(callback).onBeforeModelCreated();

    registry.onAfterModelCreated();
    Mockito.verify(callback).onAfterModelCreated();

    registry.onBeforePreProcess();
    Mockito.verify(callback).onBeforePreProcess();

    registry.onAfterPreProcess();
    Mockito.verify(callback).onAfterPreProcess();

    registry.onBeforePostProcess();
    Mockito.verify(callback).onBeforePostProcess();

    registry.onAfterPostProcess();
    Mockito.verify(callback).onAfterPostProcess();
  }
}
