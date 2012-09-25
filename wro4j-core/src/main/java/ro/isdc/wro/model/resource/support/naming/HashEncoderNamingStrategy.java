/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.support.naming;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.support.hash.CRC32HashStrategy;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;


/**
 * The simplest implementation of {@link NamingStrategy} which encodes the hash code into the name of the
 * resource. For instance: For <code>group.js</code> -> <code>group-<hashcode>.js</code>. This implementation uses by
 * default {@link CRC32HashBuilder} implementation.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 * @deprecated prefer Using {@link DefaultHashEncoderNamingStrategy}.
 */
public class HashEncoderNamingStrategy
  implements NamingStrategy {
  public static final String ALIAS = "hashEncoder-CRC32";
  private HashStrategy hashStrategy = newHashStrategy();


  /**
   * @return an implementation of {@link HashStrategy}.
   */
  protected HashStrategy newHashStrategy() {
    return new CRC32HashStrategy();
  }
  
  /**
   * @return the {@link HashStrategy} to use for renaming. By default the used strategy is the same as the one
   *         configured by wro4j. Override this method to provide a custom {@link HashStrategy}.
   */
  protected HashStrategy getHashStrategy() {
    return hashStrategy;
  }


  /**
   * {@inheritDoc}
   */
  public String rename(final String originalName, final InputStream inputStream)
    throws IOException {
    Validate.notNull(originalName);
    Validate.notNull(inputStream);
    final String baseName = FilenameUtils.getBaseName(originalName);
    final String extension = FilenameUtils.getExtension(originalName);
    final String hash = hashStrategy.getHash(inputStream);
    final StringBuilder sb = new StringBuilder(baseName).append("-").append(hash);
    if (!StringUtils.isEmpty(extension)) {
      sb.append(".").append(extension);
    }
    return sb.toString();
  }
}
