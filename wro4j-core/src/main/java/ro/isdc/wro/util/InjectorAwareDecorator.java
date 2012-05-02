package ro.isdc.wro.util;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;

/**
 * Responsible for injecting decorated object and its decorated "children" of {@link AbstractDecorator} type. 
 *   
 * @author Alex Objelean
 * @created 2 May 2012
 * @since 1.4.6
 */
public class InjectorAwareDecorator<T>
    extends AbstractDecorator<T> {
  private final Injector injector;

  public InjectorAwareDecorator(final T decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
    inject(decorated);
  }

  /**
   * Handles injection for decorators.
   */
  protected void inject(final T object) {
    injector.inject(object);
    if (object instanceof AbstractDecorator) {
      injector.inject(((AbstractDecorator<?>) object).getDecoratedObject());
    }
  }

  protected final Injector getInjector() {
    return injector;
  }
}
