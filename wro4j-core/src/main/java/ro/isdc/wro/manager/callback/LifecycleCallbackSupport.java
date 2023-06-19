/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import ro.isdc.wro.model.resource.Resource;

/**
 * Default implementation of {@link LifecycleCallback} interface with empty implementations.
 *
 * @author Alex Objelean
 * @since 1.4.3
 */
public class LifecycleCallbackSupport
    implements LifecycleCallback {
  /**
   * {@inheritDoc}
   */
  public void onBeforeModelCreated() {
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcess() {
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcess() {
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforeMerge() {
  }

  /**
   * {@inheritDoc}
   */
  public void onProcessingComplete() {
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterMerge() {
  }

  /**
   * {@inheritDoc}
   */
  public void onDestroy() {
  }

  /**
   * {@inheritDoc}
   */
  public void onResourceChanged(final Resource resource) {
  }
}
