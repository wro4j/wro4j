package ro.isdc.wro.cache.support;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * This interface is meant to be implemented by {@link CacheStrategy} (actually its implementations). Designed to mark
 * the implementation about the capability of marking a cache key as stale without actual removal from cache. This is
 * useful to keep serving the old cache value until the new value is computed and added to the cache. Typical use-case:
 * resource watch of resources which can potentially be changed. The purpose is to improve the perceived performance
 * when changes occur.
 * 
 * @author Alex Objelean
 * @created 20 Jan 2014
 * @since 1.7.3
 */
public interface StaleCacheKeyAware<K> {
  /**
   * @param key
   *          marks the provided key as stale. This is equivalent to invalidating this key. The only difference is that
   *          it will still live in cache until a new value will override the old one.
   */
  void markAsStale(K key);
  
  /**
   * @param key
   *          The cache key to check for stale state.
   * @return true if the provided key is stale (was invalidated).
   */
  boolean isStale(K key);
}
