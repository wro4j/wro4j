/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;


/**
 * The simplest implementation of {@link NamingStrategy} which preserve the original name.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class NoOpNamingStrategy
  implements NamingStrategy {
  /**
   * {@inheritDoc}
   */
  public String rename(final String originalName, final InputStream inputStream) {
    try {
      System.out.println(IOUtils.toString(inputStream));
      return originalName;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
