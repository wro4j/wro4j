package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;


/**
 * Always returns the same instance of {@link WroManager}.
 *
 * @author Alex Objelean
 * @since 1.6.2
 */
public class SimpleWroManagerFactory
    implements WroManagerFactory {
  private final WroManager manager;

  public SimpleWroManagerFactory(final WroManager manager) {
    this.manager = manager;
  }

  /**
   * {@inheritDoc}
   */
  public WroManager create() {
    return manager;
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged(final long value) {
    manager.onCachePeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged(final long value) {
    manager.onModelPeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    manager.destroy();
  }
}
