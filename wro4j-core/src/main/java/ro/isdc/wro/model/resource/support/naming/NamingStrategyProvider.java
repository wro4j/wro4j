package ro.isdc.wro.model.resource.support.naming;

import java.util.Map;

/**
 * A service provider responsible for providing new implementations of {@link NamingStrategy}.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public interface NamingStrategyProvider {
  /**
   * @return the {@link NamingStrategy} implementations to contribute. The key represents the namingStrategy alias.
   */
  Map<String, NamingStrategy> provideNamingStrategies();
}
