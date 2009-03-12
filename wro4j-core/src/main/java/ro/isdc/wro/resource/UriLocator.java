/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Describes a primary access mechanism of a uri.<br>
 * Defines a contract for classes which are able to read a uri, by returning the
 * corresponding InputStream.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
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
