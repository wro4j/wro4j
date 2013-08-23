package ro.isdc.wro.model.factory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Responsible for lazy initialization of {@link WroModelFactory}. Useful when a factory initialization has a lot of
 * dependencies, which shouldn't be required as long as factory is not used.
 *
 * @author Alex Objelean
 * @created 6 Feb 2013
 * @since 1.6.3
 */
public class LazyWroModelFactoryDecorator
    extends AbstractDecorator<LazyInitializer<WroModelFactory>>
    implements WroModelFactory {

  public LazyWroModelFactoryDecorator(final LazyInitializer<WroModelFactory> initializer) {
    super(initializer);
  }

  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    return getDecoratedObject().get().create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().get().destroy();
  }
}
