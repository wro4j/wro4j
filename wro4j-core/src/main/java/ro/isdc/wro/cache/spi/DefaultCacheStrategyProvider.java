package ro.isdc.wro.cache.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.cache.impl.NoCacheStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;


/**
 * Default implementation of {@link NamingStrategyProvider} providing all {@link NamingStrategy} implementations from
 * core module.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class DefaultCacheStrategyProvider
    implements CacheStrategyProvider {
 
  public Map<String, CacheStrategy<CacheKey, CacheValue>> provideCacheStrategies() {
    final Map<String, CacheStrategy<CacheKey, CacheValue>> map = new HashMap<String, CacheStrategy<CacheKey, CacheValue>>();
    map.put(MemoryCacheStrategy.ALIAS, new MemoryCacheStrategy<CacheKey, CacheValue>());
    map.put(LruMemoryCacheStrategy.ALIAS, new LruMemoryCacheStrategy<CacheKey, CacheValue>());
    map.put(NoCacheStrategy.ALIAS, new NoCacheStrategy<CacheKey, CacheValue>());
    return map;
  }
}
