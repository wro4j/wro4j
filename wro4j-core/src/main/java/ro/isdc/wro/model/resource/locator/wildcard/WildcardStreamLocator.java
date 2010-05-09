/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Defines the way resources containing wildcard characters are located.
 *
 * @author Alex Objelean
 * @created 8 May, 2010
 */
public interface WildcardStreamLocator {
  /**
   * @param uri to check
   * @return true if the uri contains a wildcard.
   */
  boolean hasWildcard(final String uri);


  /**
   * Locates the stream based on passed uri containing wildcard.
   *
   * @param uri to locate.
   * @param folder the folder where the search for files should start.
   * @return {@link InputStream} to the resources collection matching the wildcard.
   * @throws IOException if folder is invalid or when I/O error occurs while locating the stream.
   */
  InputStream locateStream(final String uri, final File folder)
    throws IOException;
}
