package ro.isdc.wro.cache.spi;

import java.util.Map;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;

/**
 * A service provider responsible for providing new implementations of {@link HashStrategy}.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public interface CacheStrategyProvider {
  /**
   * @return the {@link CacheStrategy} implementations to contribute. The key represents the alias.
   */
  Map<String, CacheStrategy<CacheKey, CacheValue>> provideCacheStrategies();
}
