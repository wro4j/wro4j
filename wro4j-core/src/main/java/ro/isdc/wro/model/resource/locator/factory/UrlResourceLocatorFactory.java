package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;


/**
 * Creates the {@link UrlResourceLocatorFactory} if the uri can be located, otherwise returns null.
 * 
 * @author Alex Objelean
 * @created 9 Jul 2012
 * @since 1.5.0
 */
public class UrlResourceLocatorFactory
    implements ResourceLocatorFactory {
  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    return UrlResourceLocator.isValid(uri) ? new UrlResourceLocator(uri) : null;
  }
}
