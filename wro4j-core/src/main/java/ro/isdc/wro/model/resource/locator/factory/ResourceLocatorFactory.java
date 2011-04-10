/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

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
   *          uri to read.
   * @return a not null {@link ResourceLocator} implementation for the provided uri.
   */
  ResourceLocator locate(final String uri);
}
