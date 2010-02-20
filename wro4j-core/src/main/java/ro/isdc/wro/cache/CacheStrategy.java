/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.cache;

/**
 * CacheStrategy.java.
 * <p>
 * This interface will be implemented by all classes which will support a
 * caching strategy.
 *
 * @author Alex Objelean
 * @created Created on Nov 18, 2008
 */
public interface CacheStrategy<K, V> {
  /**
   * Put a value in the cache using a key.
   *
   * @param key
   *          Object.
   * @param value
   *          Object.
   */
  void put(final K key, final V value);

  /**
   * Restore a value from the cache.
   *
   * @param key
   *          Object
   * @return value Object.
   */
  V get(final K key);

  /**
   * Clear all cache contents.
   */
  void clear();

  /**
   * Perform the clean up.
   */
  void destroy();

}
