/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.model.WroModel;


/**
 * Decorates a {@link WroModelFactory}.
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 */
public class WroModelFactoryDecorator
  implements WroModelFactory {
  private WroModelFactory decorated;

  public WroModelFactoryDecorator(final WroModelFactory decorated) {
    if (decorated == null) {
      throw new IllegalArgumentException("Decorated WroModelFactory cannot be null!");
    }
    this.decorated = decorated;
  }
  /**
   * {@inheritDoc}
   */
  public WroModel getInstance() {
    return decorated.getInstance();
  }


  /**
   * {@inheritDoc}
   */
  public void destroy() {
    decorated.destroy();
  }
}
