/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.Resource;


/**
 * Defines the way resources containing wildcard characters are located.
 *
 * @author Alex Objelean
 */
public interface WildcardStreamLocator {
  /**
   * @param uri to check
   * @return true if the uri contains a wildcard.
   */
  boolean hasWildcard(final String uri);

  /**
   * Locates the stream based on the fileName containing the wildcard and the folder where to search.
   *
   * @param uri
   *          the resource of the uri to locate. This uri should be exactly the same as defined in {@link Resource}.
   * @param folder
   *          parent from where the search of fileNameWithWildcard should start.
   * @return {@link InputStream} to the resources collection matching the wildcard.
   * @throws IOException
   *           if folder is invalid or when I/O error occurs while locating the stream.
   */
  InputStream locateStream(final String uri, final File folder)
    throws IOException;
}
