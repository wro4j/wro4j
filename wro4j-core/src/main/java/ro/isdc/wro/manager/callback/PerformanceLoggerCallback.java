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
 * @since 1.4.3
 */
public class PerformanceLoggerCallback
    extends LifecycleCallbackSupport {
  private static final Logger LOG = LoggerFactory.getLogger(PerformanceLoggerCallback.class);
  private static final String SHORT_SUMMARY = "=====Performance Logger Statistics==============";
  private StopWatch watch;

  /**
   * @return instance of watch to use.
   */
  private StopWatch getWatch() {
    if (watch == null) {
      watch = new StopWatch() {
        @Override
        public String shortSummary() {
          return SHORT_SUMMARY;
        }
      };
    }
    return watch;
  }

  @Override
  public void onBeforeModelCreated() {
    resetWatch();
    getWatch().start("model creation");
  }

  /**
   * Make sure that the next call to {@link PerformanceLoggerCallback#getWatch()} returns a fresh instance.
   */
  private void resetWatch() {
    watch = null;
  }

  @Override
  public void onAfterModelCreated() {
    stopWatchIfRunning();
  }

  @Override
  public void onBeforeMerge() {
    getWatch().start("PreProcessing");
  }

  @Override
  public void onAfterMerge() {
    stopWatchIfRunning();
    getWatch().start("PostProcessing");
  }

  @Override
  public void onProcessingComplete() {
    stopWatchIfRunning();
    if (getWatch().getTaskCount() > 0) {
      LOG.debug(getWatch().prettyPrint());
    }
  }

  /**
   * Safe way to stop the watch. This method will check if it is running - in order to avoid {@link IllegalStateException}.
   */
  private void stopWatchIfRunning() {
    if (getWatch().isRunning()) {
      getWatch().stop();
    }
  }
}
