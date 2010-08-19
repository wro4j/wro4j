/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.util;

/**
 * Classes implementing this interface will have access to {@link NamingStrategy} implementation.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public interface NamingStrategyAware {
  /**
   * @return an implementation of {@link NamingStrategy}.
   */
  NamingStrategy getNamingStrategy();
}
