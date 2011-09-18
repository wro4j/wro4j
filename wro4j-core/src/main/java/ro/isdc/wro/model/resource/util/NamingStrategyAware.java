/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.util;

/**
 * Classes implementing this interface will have access to {@link NamingStrategy} implementation.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 * @deprecated remove later, not useful
 */
@Deprecated
public interface NamingStrategyAware {
  /**
   * @return an implementation of {@link NamingStrategy}.
   */
  NamingStrategy getNamingStrategy();
}
