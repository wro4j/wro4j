/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestPerformanceLoggerCallback {
  private PerformanceLoggerCallback callback;

  @Before
  public void setUp() {
    callback = new PerformanceLoggerCallback();
  }

  @Test
  public void testOnProcessingCompleteOnly() {
    callback.onBeforeModelCreated();
    callback.onProcessingComplete();
  }

  @Test
  public void testDefaultFlow() throws Exception {
    callback.onBeforeModelCreated();
    Thread.sleep(10);
    callback.onAfterModelCreated();

    callback.onBeforeMerge();
    Thread.sleep(10);

    callback.onBeforePreProcess();
    callback.onAfterPreProcess();
    Thread.sleep(10);

    callback.onAfterMerge();
    Thread.sleep(10);

    callback.onBeforePostProcess();
    callback.onAfterPostProcess();
    Thread.sleep(10);

    callback.onProcessingComplete();
  }
}
