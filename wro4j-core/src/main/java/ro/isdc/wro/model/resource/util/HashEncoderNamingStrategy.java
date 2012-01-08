/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * The simplest implementation of {@link NamingStrategy} which encodes the hash code into the name of the
 * resource. For instance: For <code>group.js</code> -> <code>group-<hashcode>.js</code>. This implementation uses by
 * default {@link CRC32HashBuilder} implementation.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class HashEncoderNamingStrategy
  implements NamingStrategy {
  private HashBuilder hashBuilder = newHashBuilder();


  /**
   * @return an implementation of {@link HashBuilder}.
   */
  protected HashBuilder newHashBuilder() {
    return new CRC32HashBuilder();
  }

  /**
   * {@inheritDoc}
   */
  public String rename(final String originalName, final InputStream inputStream)
    throws IOException {
    final String baseName = FilenameUtils.getBaseName(originalName);
    final String extension = FilenameUtils.getExtension(originalName);
    final String hash = hashBuilder.getHash(inputStream);
    final StringBuilder sb = new StringBuilder(baseName).append("-").append(hash);
    if (!StringUtils.isEmpty(extension)) {
      sb.append(".").append(extension);
    }
    return sb.toString();
  }
}
