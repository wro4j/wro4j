/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Decorator for {@link UriLocatorFactory} responsible for processing @Inject annotations of locators provided by
 * decorated factory.
 *
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class InjectorUriLocatorFactoryDecorator
  extends UriLocatorFactoryDecorator {
  private final Injector injector;

  public InjectorUriLocatorFactoryDecorator(final UriLocatorFactory uriLocatorFactory, final Injector injector) {
    super(uriLocatorFactory);
    Validate.notNull(injector);
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UriLocator getInstance(final String uri) {
    final UriLocator uriLocator = super.getInstance(uri);
    //TODO shouldn't we throw exception here?
    if (uriLocator != null) {
      injector.inject(uriLocator);
    }
    return uriLocator;
  }
}
