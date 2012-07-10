package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Simple decorator of {@link ResourceLocatorFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public class ResourceLocatorFactoryDecorator extends AbstractDecorator<ResourceLocatorFactory>
    implements ResourceLocatorFactory {
  
  /**
   * Decorates an {@link UriLocatorFactory}.
   */
  public ResourceLocatorFactoryDecorator(final ResourceLocatorFactory decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public ResourceLocator getLocator(final String uri) {
    return getDecoratedObject().getLocator(uri);
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return getDecoratedObject().locate(uri);
  }
}