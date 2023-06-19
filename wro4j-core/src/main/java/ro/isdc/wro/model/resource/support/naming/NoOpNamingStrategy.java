/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.support.naming;

import java.io.InputStream;


/**
 * The simplest implementation of {@link NamingStrategy} which preserve the original name.
 *
 * @author Alex Objelean
 */
public class NoOpNamingStrategy
  implements NamingStrategy {
  /**
   * A short name of this naming strategy.
   */
  public static final String ALIAS = "noOp";
  /**
   * {@inheritDoc}
   */
  public String rename(final String originalName, final InputStream inputStream) {
    return originalName;
  }
}
