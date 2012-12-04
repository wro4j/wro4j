package ro.isdc.wro.extras.hazelcast;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Map;

import ro.isdc.wro.cache.CacheStrategy;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;


/**
 * An implementation of {@link CacheStrategy} which uses Hazelcast distributed cache.
 *
 * @author Alex Objelean
 * @created 4 Dec 2012
 * @since 1.6.2
 */
public class HazelcastCacheStrategy<K, V>
    implements CacheStrategy<K, V> {
  /**
   * Aliased used by provider for this implementation
   */
  public static final String ALIAS = "hazelcast";
  private static final String DEFAULT_MAP_NAME = "default";
  private Map<K, V> map;

  /**
   * Creates a strategy with default map name.
   */
  public HazelcastCacheStrategy() {
    this(DEFAULT_MAP_NAME);
  }

  /**
   * Creates a strategy using a predefined mapName.
   *
   * @param name
   *          a notNull map name.
   */
  public HazelcastCacheStrategy(final String name) {
    notNull(name);
    this.map = Hazelcast.newHazelcastInstance(new Config()).getMap(name);
  }

  /**
   * {@inheritDoc}
   */
  public void put(final K key, final V value) {
    if (key != null) {
      if (value != null) {
        map.put(key, value);
      } else {
        map.remove(key);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public V get(final K key) {
    return key != null ? map.get(key) : null;
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
    map.clear();
  }
}
