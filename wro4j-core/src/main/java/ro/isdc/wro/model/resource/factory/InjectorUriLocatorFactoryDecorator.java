/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;
import java.io.InputStream;

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
    if (injector == null) {
      throw new IllegalArgumentException("injector cannot be null!");
    }
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
    throws IOException {
    return super.locate(uri);
  }

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    final UriLocator uriLocator = super.getInstance(uri);
    injector.inject(uriLocator);
    return uriLocator;
  }
}
