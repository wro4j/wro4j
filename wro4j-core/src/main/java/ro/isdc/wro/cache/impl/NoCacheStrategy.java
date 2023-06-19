package ro.isdc.wro.cache.impl;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * Dummy cache strategy implementation to totaly disable cache
 *
 * @author Philippe Da Costa &lt;pdacosta@gmail.com&gt;
 * @since 1.7.9
 */
public class NoCacheStrategy<CacheKey, CacheValue>
    implements CacheStrategy<CacheKey, CacheValue> {
  public static final String ALIAS = "nocache";
  
  public void put(final CacheKey key, final CacheValue value) {
    // Nothing to do because we are a no cache implementation
  }
  
  public CacheValue get(final CacheKey key) {
    return null;
  }
  
  public void clear() {
    // Nothing to do
  }
  
  public void destroy() {
    // Nothing to do
  }
}
