/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.StopWatch;

/**
 * Default implementation of {@link LifecycleCallback} interface with empty implementations.
 * 
 * @author Alex Objelean
 * @created 26 Oct 2011
 * @since 1.4.3
 */
public class PerformanceLoggerCallback
    extends LifecycleCallbackSupport {
  private static final Logger LOG = LoggerFactory.getLogger(PerformanceLoggerCallback.class);
  private StopWatch watch;
  
  @Override
  public void onBeforeModelCreated() {
    watch = new StopWatch() {
      @Override
      public String shortSummary() {
        return "=====Performance Logger Statistics==============";
      }
    };
    watch.start("model creation");
  }
  
  @Override
  public void onAfterModelCreated() {
    watch.stop();
  }
  
  @Override
  public void onBeforeMerge() {
    watch.start("PreProcessing");
  }
  
  @Override
  public void onAfterMerge() {
    watch.stop();
    watch.start("PostProcessing");
  }
  
  @Override
  public void onProcessingComplete() {
    watch.stop();
    LOG.debug(watch.prettyPrint());
  }
}
