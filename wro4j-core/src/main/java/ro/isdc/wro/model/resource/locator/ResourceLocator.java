/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.io.InputStream;

import ro.isdc.wro.model.resource.Resource;


/**
 * Interface for a resource locator that abstracts from the actual type of underlying resource, such as a file or class
 * path resource.
 * <p/>
 * An implementation of {@link ResourceLocator} will be used to locate wro4j {@link Resource}'s (js & css) and any other
 * types of resources.
 *
 * @author Alex Objelean
 * @created 28 Mar 2011
 * @since 1.5.0
 */
public interface ResourceLocator {
  /**
   * Return an {@link InputStream}.
   * <p>
   * It is expected each call to create a <i>fresh</i> stream.
   * <p>
   * This requirement is particularly important when you consider an API such as JavaMail, which needs to be able to
   * read the stream multiple times when creating mail attachments. For such a use case, it is <i>required</i> that each
   * <code>getInputStream()</code> call returns a fresh stream.
   *
   * @throws IOException if the stream could not be opened
   * @return
   */
  InputStream getInputStream() throws IOException;

  /**
   * @return Determine the last-modified timestamp for this resource. If the last modified time is not known, this method will return 0.
   *
   */
  long lastModified();

  /**
   * Create a resource relative to this resource.
   * @param relativePath the relative path (relative to this resource)
   * @return the resource handle for the relative resource
   * @throws IOException if the relative resource cannot be determined
   */
  ResourceLocator createRelative(String relativePath) throws IOException;

}
