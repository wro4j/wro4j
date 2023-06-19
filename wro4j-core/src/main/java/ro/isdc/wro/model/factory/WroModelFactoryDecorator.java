/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Decorates a {@link WroModelFactory}.
 *
 * @author Alex Objelean
 */
public class WroModelFactoryDecorator extends AbstractDecorator<WroModelFactory>
    implements WroModelFactory {

  public WroModelFactoryDecorator(final WroModelFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    return getDecoratedObject().create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().destroy();
  }
}
