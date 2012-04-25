package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Simple decorator of {@link UriLocatorFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public class UriLocatorFactoryDecorator extends AbstractDecorator<UriLocatorFactory>
    implements UriLocatorFactory {
  
  /**
   * Decorates an {@link UriLocatorFactory}.
   */
  public UriLocatorFactoryDecorator(final UriLocatorFactory decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return getDecoratedObject().locate(uri);
  }
  
  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    return getDecoratedObject().getInstance(uri);
  }
}
