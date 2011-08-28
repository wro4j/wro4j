/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.model.WroModel;


/**
 * Decorates a {@link WroModelFactory}.
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 */
public class WroModelFactoryDecorator
    implements WroModelFactory, WroConfigurationChangeListener {
  private final WroModelFactory decorated;

  public WroModelFactoryDecorator(final WroModelFactory decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }

  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    return decorated.create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    decorated.destroy();
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged() {
    if (decorated instanceof WroConfigurationChangeListener) {
      ((WroConfigurationChangeListener) decorated).onCachePeriodChanged();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged() {
    if (decorated instanceof WroConfigurationChangeListener) {
      ((WroConfigurationChangeListener) decorated).onModelPeriodChanged();
    }
  }
}
