/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.callback;

import org.apache.commons.lang3.Validate;



/**
 * Default implementation of {@link LifecycleCallback} interface with empty implementations.
 *
 * @author Alex Objelean
 * @created 26 Oct 2011
 * @since 1.4.3
 */
public class LifecycleCallbackDecorator
    implements LifecycleCallback {
  private final LifecycleCallback decorated;

  public LifecycleCallbackDecorator(final LifecycleCallback decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforeModelCreated() {
    decorated.onBeforeModelCreated();
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterModelCreated() {
    decorated.onAfterModelCreated();
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePreProcess() {
    decorated.onBeforePreProcess();
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPreProcess() {
    decorated.onAfterPreProcess();
  }

  /**
   * {@inheritDoc}
   */
  public void onBeforePostProcess() {
    decorated.onBeforePostProcess();
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterPostProcess() {
    decorated.onAfterPostProcess();
  }

  /**
   * {@inheritDoc}
   */  
  public void onBeforeProcess() {
    decorated.onBeforeProcess();
  }

  /**
   * {@inheritDoc}
   */
  public void onAfterProcess() {
    decorated.onAfterProcess();
  }
}
