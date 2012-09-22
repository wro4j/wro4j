package ro.isdc.wro.cache;

import java.util.Map;

import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * A service provider responsible for providing new implementations of {@link HashStrategy}.
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 22 Sep 2012
 */
public interface CacheStrategyProvider {
  /**
   * @return the {@link CacheStrategy} implementations to contribute. The key represents the alias.
   */
  Map<String, CacheStrategy> provideCacheStrategies();  
}
