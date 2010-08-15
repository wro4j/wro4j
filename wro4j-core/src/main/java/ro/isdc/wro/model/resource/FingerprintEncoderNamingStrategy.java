/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;


/**
 * The simplest implementation of {@link NamingStrategy} which encodes the fingerprint hash code into the name of the
 * resource. For instance: For <code>group.js</code> -> <code>group-<hashcode>.js</code>. This implementation uses by
 * default {@link MD5FingerprintCreator} implementation.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class FingerprintEncoderNamingStrategy
  implements NamingStrategy {
  private FingerprintCreator fingerprintCreator = newFingerprintCreator();
  /**
   * @return
   */
  protected FingerprintCreator newFingerprintCreator() {
    return new MD5FingerprintCreator();
  }
  /**
   * {@inheritDoc}
   */
  public String rename(final String originalName, final InputStream inputStream)
    throws IOException {
    final String baseName = FilenameUtils.getBaseName(originalName);
    final String extension = FilenameUtils.getExtension(originalName);
    final String hash = fingerprintCreator.create(inputStream);
    final StringBuilder sb = new StringBuilder(baseName).append("-").append(hash);
    if (!StringUtils.isEmpty(extension)) {
      sb.append(".").append(extension);
    }
    return sb.toString();
  }
}
