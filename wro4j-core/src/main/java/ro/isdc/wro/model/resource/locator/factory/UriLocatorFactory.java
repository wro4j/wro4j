/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Centralize information about UriLocators to be used.
 *
 * @author Alex Objelean
 */
public interface UriLocatorFactory {
  /**
   * Locates an InputStream for the given uri.
   *
   * @param uri to locate.
   * @return {@link InputStream} of the resource.
   * @throws IOException if uri is invalid or resource couldn't be located.
   */
  InputStream locate(final String uri)
    throws IOException;

  /**
   * @param uri to handle by the locator.
   * @return an instance of {@link UriLocator} which is capable of handling provided uri. Returns null if no locator
   *         found.
   */
  UriLocator getInstance(final String uri);
}
