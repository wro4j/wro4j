/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;


/**
 * Default implementation of {@link UriLocatorFactory}. Holds most used locators.
 *
 * @author Alex Objelean
 * @created 15 May 2011
 * @since 1.3.7
 */
public final class DefaultUriLocatorFactory extends SimpleUriLocatorFactory {
  public DefaultUriLocatorFactory() {
    addUriLocator(new ServletContextUriLocator()).addUriLocator(new ClasspathUriLocator()).addUriLocator(
      new UrlUriLocator());
  }
}
