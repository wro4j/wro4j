package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Simple decorator of {@link UriLocatorFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public class UriLocatorFactoryDecorator
    implements UriLocatorFactory {
  private final UriLocatorFactory decorated;
  
  /**
   * Decorates an {@link UriLocatorFactory}.
   */
  public UriLocatorFactoryDecorator(final UriLocatorFactory decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return decorated.locate(uri);
  }
  
  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    return decorated.getInstance(uri);
  }
  
  /**
   * @return the decorated locator factory.
   */
  public final UriLocatorFactory getDecoratedFactory() {
    return decorated;
  }
}
