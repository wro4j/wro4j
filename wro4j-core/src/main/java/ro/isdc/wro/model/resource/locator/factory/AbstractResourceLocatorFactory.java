package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.ResourceLocator;


/**
 * Provides default implementation of {@link ResourceLocatorFactory#locate(String)} method.
 * 
 * @author Alex Objelean
 * @created 10 Jul 2012
 * @since 1.5.0
 */
public abstract class AbstractResourceLocatorFactory
    implements ResourceLocatorFactory {

  /**
   * {@inheritDoc}
   */
  public final InputStream locate(final String uri)
      throws IOException {
    final ResourceLocator locator = getLocator(uri);
    if (locator == null) {
      throw new IOException("No locator is capable for handling uri: " + uri);
    }
    return locator.getInputStream();
  }
}
