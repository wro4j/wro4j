package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;


/**
 * Creates the {@link ClasspathResourceLocator} if the uri can be located, otherwise returns null.
 * 
 * @author Alex Objelean
 * @created 9 Jul 2012
 * @since 1.5.0
 */
public class ClasspathResourceLocatorFactory
    extends AbstractResourceLocatorFactory {
  /**
   * {@inheritDoc}
   */
  public ResourceLocator getLocator(final String uri) {
    return uri.startsWith(ClasspathResourceLocator.PREFIX) ? new ClasspathResourceLocator(uri) : null;
  }
}
