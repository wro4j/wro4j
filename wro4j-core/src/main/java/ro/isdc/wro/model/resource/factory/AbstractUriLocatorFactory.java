/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Holds a list of uri locators. The uriLocator will be created based on the first
 * uriLocator from the supplied list which will accept the url.
 *
 * @author Alex Objelean
 * @created Created on Nov 4, 2008
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
    LOG.debug("locate: " + uri);
    final UriLocator uriLocator = getInstance(uri);
    LOG.debug("using locator: " + uriLocator);
    if (uriLocator == null) {
      throw new IOException("No locator is capable of handling uri: " + uri);
    }
    return uriLocator.locate(uri);
  }
}
