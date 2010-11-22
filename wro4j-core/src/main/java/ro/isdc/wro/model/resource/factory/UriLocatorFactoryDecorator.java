/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Decorator for {@link UriLocatorFactory}.
 *
 * @author Alex Objelean
 * @created 22 Nov 2010
 */
public class UriLocatorFactoryDecorator
  implements UriLocatorFactory {
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
  public InputStream locate(final String uri)
    throws IOException {
    return uriLocatorFactory.locate(uri);
  }

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    return uriLocatorFactory.getInstance(uri);
  }
}
