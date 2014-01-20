package ro.isdc.wro.cache.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * Default implementation of {@link StaleCacheKeyAware} which implements als {@link CacheStrategy} interface.
 * 
 * @author Alex Objelean
 * @created 20 Jan 2014
 * @since 1.7.3
 */
public class StaleCacheKeyAwareCacheStrategyDecorator<K, V>
    extends CacheStrategyDecorator<K, V>
    implements CacheStrategy<K, V> {
  private Map<K,V> staleKeys = Collections.synchronizedMap(new HashMap<K,V>());

  public StaleCacheKeyAwareCacheStrategyDecorator(final CacheStrategy<K, V> cacheStrategy) {
    super(cacheStrategy);
  }
  
  public void markAsStale(final K key) {
    notNull(key);
    final V value = super.get(key);
    staleKeys.put(key, value);
    super.markAsStale(key);
    super.put(key, null);
  }
  
  public boolean isStale(final K key) {
    notNull(key);
    return staleKeys.containsKey(key);
  }
  
  public V get(K key) {
    //As long as a key is stale, return it instead of original key.
    return isStale(key) ? staleKeys.get(key) : super.get(key);
  }
  
  public void put(K key, V value) {
    if (isStale(key)) {
      staleKeys.remove(key);
    }
    super.put(key, value);
  }
  
  @Override
  public void destroy() {
    super.destroy();
    staleKeys.clear();
  }
}
