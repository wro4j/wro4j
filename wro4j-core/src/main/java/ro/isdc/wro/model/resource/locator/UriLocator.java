/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;

/**
 * Describes a way to locate the stream associated with some uri.<br>
 * Defines a contract for classes which are able to read a uri, by returning the
 * corresponding InputStream.
 *
 * @author Alex Objelean
 */
public interface UriLocator {
  /**
   * Locates the uri by retrieving the InputStream. The client is responsible
   * for closing the InputStream.
   *
   * @param uri
   *          uri to read.
   * @return InputStream for the provided uri.
   * @throws IOException
   *           if the resource cannot be read for some reason.
   */
  InputStream locate(final String uri) throws IOException;

  /**
   * Check if this uri can be located by concrete implementation.
   *
   * @param uri
   *          to read.
   * @return true if UriLocator is able to return an InputStream of this uri.
   */
  boolean accept(final String uri);
}
