package ro.isdc.wro.cache.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements a LRU (Least Recently Used) cache strategy. This cache
 * must have a fixed-size. When new entries are added and the cache reach its
 * maximum capacity, eldest entries are removed.
 * <p>
 * As all {@link MemoryCacheStrategy}s this cache is thread-safe.
 * </p>
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class LruMemoryCacheStrategy<K, V> extends MemoryCacheStrategy<K, V> {
  /**
   * Aliased used by provider for this implementation
   */
  public static final String ALIAS = "LRU-memory";
  /** Default cache size if no capacity is specified by the constructor. */
  public static final int DEFAULT_SIZE = 128;

  /** Load factor that determines the way as the cache will grow. */
  private static final float hashTableLoadFactor = 0.75f;

  /**
   * Constructs a {@link LruMemoryCacheStrategy} and sets the default size up
   * to {@link #DEFAULT_SIZE}
   */
  public LruMemoryCacheStrategy() {
    this(DEFAULT_SIZE);
  }

  /**
   * Constructs a new {@link LruMemoryCacheStrategy} and sets the cache size.
   *
   * @param cacheSize Cache size. It must be greater than 0.
   */
  public LruMemoryCacheStrategy(final int cacheSize) {
    // Uses a LinkedHashMap to implement the LRU pattern.
    super(new LinkedHashMap<K,V>(cacheSize, hashTableLoadFactor, true) {
      /** Default id for serialization. */
      private static final long serialVersionUID = 1;

      /**
       * {@inheritDoc}
       */
      @Override
      protected boolean removeEldestEntry(final Map.Entry<K,V> eldest) {
       return size() > cacheSize;
      }
    });
  }
}
