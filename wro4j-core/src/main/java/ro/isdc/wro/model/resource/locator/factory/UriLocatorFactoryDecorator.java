/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Decorator for {@link UriLocatorFactory}.
 *
 * @author Alex Objelean
 * @created 22 Nov 2010
 */
public class UriLocatorFactoryDecorator
  extends AbstractUriLocatorFactory {
  private final UriLocatorFactory uriLocatorFactory;

  public UriLocatorFactoryDecorator(final UriLocatorFactory uriLocatorFactory) {
    if (uriLocatorFactory == null) {
      throw new IllegalArgumentException("uriLocatorFactory cannot be null!");
    }
    this.uriLocatorFactory = uriLocatorFactory;
  }

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    return uriLocatorFactory.getInstance(uri);
  }
}
