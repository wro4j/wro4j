package ro.isdc.wro.cache.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    implements CacheStrategy<K, V>, StaleCacheKeyAware<K> {
  private Set<K> staleKeys = Collections.synchronizedSet(new HashSet<K>());

  public StaleCacheKeyAwareCacheStrategyDecorator(final CacheStrategy<K, V> cacheStrategy) {
    super(cacheStrategy);
  }
  
  public void markAsStale(final K key) {
    notNull(key);
    staleKeys.add(key);
  }
  
  public boolean isStale(final K key) {
    notNull(key);
    return staleKeys.contains(key);
  }
  
  public void put(K key, V value) {
    if (isNotStaleAnymore(key, value)) {
      staleKeys.remove(key);
    }
    super.put(key, value);
  }

  /**
   * @return true if the provided key should be removed from the {@link #staleKeys} set.
   */
  private boolean isNotStaleAnymore(K key, V value) {
    return value != null && isStale(key);
  };
}
