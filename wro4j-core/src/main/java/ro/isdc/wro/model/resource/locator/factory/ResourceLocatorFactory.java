/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.locator.ResourceLocator;

/**
 * Describes a way to locate the stream associated with some uri.<br>
 * Defines a contract for classes which are able to read a uri, by returning the
 * corresponding InputStream.
 *
 * @author Alex Objelean
 * @created Created on Mar 31, 2011
 * @since 1.4.0
 */
public interface ResourceLocatorFactory {
  /**
   * Based on provided uri, returns a best suited {@link ResourceLocator}.
   * 
   * @param uri
   *          of the resource for which {@link ResourceLocator} should be returned..
   * @return a {@link ResourceLocator} implementation for the provided uri or null if there is no locator for handling
   *         provided uri.
   */
  ResourceLocator getLocator(final String uri);
  
  /**
   * @param uri
   *          of the resource to locate.
   * @return the InputStream of the resource
   * @throws IOException
   *           if there is no locator capable reading this uri.
   */
  InputStream locate(final String uri)
      throws IOException;
}
