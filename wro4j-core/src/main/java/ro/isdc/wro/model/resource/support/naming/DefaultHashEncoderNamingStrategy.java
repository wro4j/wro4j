package ro.isdc.wro.model.resource.support.naming;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;


/**
 * Encodes the hash into the folder where the resources is located.
 * Example:
 *    original name: group.js
 *    renamed:       group-a912810be321.js
 *
 * @author Alex Objelean
 * @since 1.4.9
 */
public class DefaultHashEncoderNamingStrategy
  implements NamingStrategy {
  public static final String ALIAS = "hashEncoder";
  @Inject
  private HashStrategy hashStrategy;


  /**
   * @return the {@link HashStrategy} to use for renaming. By default the used strategy is the same as the one
   *         configured by wro4j. Override this method to provide a custom {@link HashStrategy}.
   */
  protected HashStrategy getHashStrategy() {
    return hashStrategy;
  }

  public String rename(final String originalName, final InputStream inputStream)
    throws IOException {
    notNull(originalName);
    notNull(inputStream);
    try (inputStream) {
      final String baseName = FilenameUtils.getBaseName(originalName);
      final String path = FilenameUtils.getPath(originalName);
      final String extension = FilenameUtils.getExtension(originalName);
      final String hash = getHashStrategy().getHash(inputStream);
      final StringBuilder sb = new StringBuilder(path).append(baseName).append("-").append(hash);
      if (!StringUtils.isEmpty(extension)) {
        sb.append(".").append(extension);
      }
      return sb.toString();
    }
  }
}
