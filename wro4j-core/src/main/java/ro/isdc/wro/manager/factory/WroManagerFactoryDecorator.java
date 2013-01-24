package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManager.Builder;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Simple decorator for {@link WroManagerFactory}.
 *
 * @author Alex Objelean
 * @created 23 Jun 2012
 * @since 1.4.7
 */
public class WroManagerFactoryDecorator
    extends AbstractDecorator<WroManagerFactory>
    implements WroManagerFactory {

  public WroManagerFactoryDecorator(final WroManagerFactory managerFactory) {
    super(managerFactory);
  }

  /**
   * {@inheritDoc}
   */
  public WroManager create() {
    final WroManager.Builder builder = new WroManager.Builder(getDecoratedObject().create());
    onBeforeBuild(builder);
    return builder.build();
  }

  /**
   * Allows client code to change the builder before the {@link WroManager} is created.
   * @param builder
   */
  protected void onBeforeBuild(final Builder builder) {
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged(final long value) {
    getDecoratedObject().onCachePeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged(final long value) {
    getDecoratedObject().onModelPeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().destroy();
  }
}
