/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;

import org.apache.commons.io.FilenameUtils;


/**
 * Contains factory methods for creating {@link Transformer} object.
 *
 * @author Alex Objelean
 */
public class Transformers {
  /**
   * Creates a {@link Transformer} which replace a original filename extension with a new extension.
   * @param newExtension extension to use for the returned value.
   * @return original filename but with the new extension.
   */
  public static Transformer<String> extensionTransformer(final String newExtension) {
    return new Transformer<String>() {
      public String transform(final String input) {
        return FilenameUtils.getBaseName(input) + "." + newExtension;
      }
    };
  }

  /**
   * Appends a suffix to the source baseName.
   * @param suffix to append.
   * @return {@link Transformer} capable to append a suffix to provided baseName.
   */
  public static Transformer<String> baseNameSuffixTransformer(final String suffix) {
    return new Transformer<String>() {
      public String transform(final String input) {
        final String baseName = FilenameUtils.getBaseName(input);
        final String extension = FilenameUtils.getExtension(input);
        return baseName + suffix + "." + extension;
      }
    };
  }

  /**
   * @return a {@link Transformer} which doesn't change the input.
   */
  public static Transformer<String> noOpTransformer() {
    return new Transformer<String>() {
      public String transform(final String input) {
        return input;
      }
    };
  }
}
