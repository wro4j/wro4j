package ro.isdc.wro.extras.hazelcast.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.extras.hazelcast.HazelcastCacheStrategy;


/**
 * Default implementation of {@link CacheStrategyProvider} providing all {@link CacheStrategy} implementations from
 * core module.
 *
 * @author Alex Objelean
 * @created 4 Dec 2012
 * @since 1.6.2
 */
public class DefaultCacheStrategyProvider
    implements CacheStrategyProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, CacheStrategy> provideCacheStrategies() {
    final Map<String, CacheStrategy> map = new HashMap<String, CacheStrategy>();
    map.put(HazelcastCacheStrategy.ALIAS, new HazelcastCacheStrategy<CacheKey, CacheValue>());
    return map;
  }
}
