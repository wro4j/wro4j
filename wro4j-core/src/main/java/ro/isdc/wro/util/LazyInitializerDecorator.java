package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;


/**
 * A simple decorator for {@link LazyInitializer}.
 *
 * @author Alex Objelean
 * @since 1.7.4
 */
public class LazyInitializerDecorator<T>
    extends LazyInitializer<T> implements ObjectDecorator<LazyInitializer<T>> {
  private final AbstractDecorator<LazyInitializer<T>> decorator;

  public LazyInitializerDecorator(final LazyInitializer<T> decorated) {
    notNull(decorated);
    this.decorator = new AbstractDecorator<LazyInitializer<T>>(decorated) {
    };
  }

  @Override
  protected T initialize() {
    return decorator.getDecoratedObject().initialize();
  }

  @Override
  public LazyInitializer<T> getDecoratedObject() {
    return decorator.getDecoratedObject();
  }

  @Override
  public LazyInitializer<T> getOriginalDecoratedObject() {
    return decorator.getOriginalDecoratedObject();
  }
}
