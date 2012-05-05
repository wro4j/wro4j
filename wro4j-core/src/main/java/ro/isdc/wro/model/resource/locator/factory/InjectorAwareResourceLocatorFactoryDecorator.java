package ro.isdc.wro.model.resource.locator.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ResourceLocatorDecorator;


/**
 * Responsible for injecting each {@link UriLocator} with required fields before being used.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.5.0
 */
public final class InjectorAwareResourceLocatorFactoryDecorator
    extends ResourceLocatorFactoryDecorator {
  private final Injector injector;
  
  /**
   * Decorates an {@link UriLocatorFactory} and uses provided injector to inject the locator.
   * 
   * @param decorated
   * @param injector
   */
  public InjectorAwareResourceLocatorFactoryDecorator(final ResourceLocatorFactory decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
    inject(decorated);
  }
  
  /**
   * Handles injection by inspecting decorated objects if required. 
   */
  private void inject(final Object object) {
    Validate.notNull(object);
    injector.inject(object);
    if (object instanceof ResourceLocatorFactoryDecorator) {
      injector.inject(((ResourceLocatorFactoryDecorator) object).getDecoratedObject());
    }
    if (object instanceof ResourceLocatorDecorator) {
      injector.inject(((ResourceLocatorDecorator) object).getDecoratedObject());
    }
  }

  
  
  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator locate(final String uri) {
    final ResourceLocator locator = getDecoratedObject().locate(uri);
    if (locator != null) {
      inject(locator);
    }
    return locator;
  }
}
