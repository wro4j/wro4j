/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Holds a list of uri locators. The uriLocator will be created based on the first
 * uriLocator from the supplied list which will accept the url.
 *
 * @author Alex Objelean
 */
public abstract class AbstractUriLocatorFactory implements UriLocatorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractUriLocatorFactory.class);
  /**
   * Locates an InputStream for the given uri.
   *
   * @param uri to locate.
   * @return {@link InputStream} of the resource.
   * @throws IOException if uri is invalid or resource couldn't be located.
   */
  public final InputStream locate(final String uri)
    throws IOException {
    final UriLocator uriLocator = getInstance(uri);
    if (uriLocator == null) {
      throw new WroRuntimeException("No locator is capable of handling uri: " + uri);
    }
    LOG.debug("[OK] locating {} using locator: {}", uri, uriLocator.getClass().getSimpleName());
    return new AutoCloseInputStream(uriLocator.locate(uri));
  }
}
