package ro.isdc.wro.model.resource.locator.factory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;


/**
 * Creates the {@link ServletContextResourceLocatorFactory} if the uri can be located, otherwise returns null.
 * 
 * @author Alex Objelean
 * @created 9 Jul 2012
 * @since 1.5.0
 */
public class ServletContextResourceLocatorFactory
    implements ResourceLocatorFactory {
  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    return uri.startsWith(ServletContextResourceLocator.PREFIX) ? newLocator(uri) : null;
  }

  /**
   * @return the not null {@link ResourceLocator} which should handle the uri.
   */
  protected ResourceLocator newLocator(final String uri) {
    return new ServletContextResourceLocator(Context.get().getServletContext(), uri);
  }
}
