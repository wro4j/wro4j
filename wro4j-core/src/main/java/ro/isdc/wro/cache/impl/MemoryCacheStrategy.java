/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.cache.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.cache.CacheStrategy;

/**
 * <p>Default CacheStrategy implementation using a {@link Map} to store values
 * in memory.</p>
 *
 * <p>Memory caches are thread-safe in all operations.</p>
 *
 * @author Alex Objelean
 */
public class MemoryCacheStrategy<K, V> implements CacheStrategy<K, V> {
  /**
   * Aliased used by provider for this implementation
   */
  public static final String ALIAS = "memory";
  /**
   * Map containing cached items.
   */
  private final Map<K, V> map;

  /**
   * Default constructor. Uses a {@link HashMap} as memory cache.
   */
  public MemoryCacheStrategy() {
    this(new HashMap<K, V>());
  }

  /**
   * Constructs a new {@link MemoryCacheStrategy} and sets the Map that will be
   * used as memory cache.
   *
   * @param cacheHolder Map to use as memory cache. It cannot be null.
   */
  public MemoryCacheStrategy(final Map<K, V> cacheHolder) {
    Validate.notNull(cacheHolder, "The cache map cannot be null.");
    map = Collections.synchronizedMap(cacheHolder);
  }

  /**
   * {@inheritDoc}
   */
  public V get(final K key) {
    return map.get(key);
  }

  /**
   * {@inheritDoc}
   */
  public void put(final K key, final V value) {
    map.put(key, value);
  }

  /**
   * {@inheritDoc}
   */
  public void clear() {
    map.clear();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    clear();
  }
}
