package ro.isdc.wro.cache.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;


/**
 * Default implementation of {@link NamingStrategyProvider} providing all {@link NamingStrategy} implementations from
 * core module.
 * 
 * @author Alex Objelean
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultCacheStrategyProvider
    implements CacheStrategyProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, CacheStrategy> provideCacheStrategies() {
    final Map<String, CacheStrategy> map = new HashMap<String, CacheStrategy>();
    map.put(MemoryCacheStrategy.ALIAS, new MemoryCacheStrategy<CacheEntry, ContentHashEntry>());
    map.put(LruMemoryCacheStrategy.ALIAS, new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>());
    return map;
  }
}
