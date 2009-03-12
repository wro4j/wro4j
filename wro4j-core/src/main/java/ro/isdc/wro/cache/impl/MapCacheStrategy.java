/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.cache.impl;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.cache.CacheStrategy;

/**
 * Default CacheStrategy implementation using a hashMap to store values.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 18, 2008
 */
public final class MapCacheStrategy<K, V> implements CacheStrategy<K, V> {
  /**
   * Map containing cached items.
   */
  private final Map<K, V> map;

  /**
   * Default constructor. Initialize the map.
   */
  public MapCacheStrategy() {
    map = new HashMap<K, V>();
  }

  /**
   * {@inheritDoc}
   */
  public V get(final K key) {
    return map.get(key);
  }

  /**
   * This method is synchronized in order to avoid possible concurrency issues
   * in multi-threaded environment.
   * <p>
   * {@inheritDoc}
   */
  public synchronized void put(final K key, final V value) {
    map.put(key, value);
  }
}
