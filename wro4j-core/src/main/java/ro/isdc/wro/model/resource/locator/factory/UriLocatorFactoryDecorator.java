package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Simple decorator of {@link UriLocatorFactory}.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public class UriLocatorFactoryDecorator extends AbstractDecorator<ResourceLocatorFactory>
    implements ResourceLocatorFactory {
  
  /**
   * Decorates an {@link UriLocatorFactory}.
   */
  public UriLocatorFactoryDecorator(final ResourceLocatorFactory decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    return getDecoratedObject().locate(uri);
  }
}
