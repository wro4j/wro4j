package ro.isdc.wro.model.group.processor;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.util.AbstractDecorator;

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
    injector.inject(decorated);
    this.injector = injector;
  }

  protected final Injector getInjector() {
    return injector;
  }
}
