/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.ResourceLocator;

/**
 * Decorator for {@link UriLocatorFactory} responsible for processing @Inject annotations of locators provided by
 * decorated factory.
 *
 * @author Alex Objelean
 * @created 31 Mar 2011
 * @since 1.4.0
 */
public class InjectorResourceLocatorFactoryDecorator
  extends ResourceLocatorFactoryDecorator {
  private final Injector injector;

  public InjectorResourceLocatorFactoryDecorator(final ResourceLocatorFactory locatorFactory, final Injector injector) {
    super(locatorFactory);
    if (injector == null) {
      throw new IllegalArgumentException("injector cannot be null!");
    }
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator locate(final String uri) {
    final ResourceLocator locator = super.locate(uri);
    if (locator == null) {
      throw new WroRuntimeException("No ResourceLocator can handle the following uri: " + uri);
    }
    injector.inject(locator);
    return locator;
  }
}
