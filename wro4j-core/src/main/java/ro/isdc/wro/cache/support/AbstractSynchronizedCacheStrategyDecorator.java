package ro.isdc.wro.cache.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * Ensure that the {@link AbstractSynchronizedCacheStrategyDecorator#loadValue(Object)} will be called only once for the
 * same key. This behavior is important for avoiding redundant execution of expensive computation in concurrent
 * environment which cause high memory and CPU consumption.
 * 
 * @author Alex Objelean
 * @created 30 Apr 2012
 * @since 1.4.6
 */
public abstract class AbstractSynchronizedCacheStrategyDecorator<K, V>
    extends CacheStrategyDecorator<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSynchronizedCacheStrategyDecorator.class);
  private final ConcurrentMap<K, ReadWriteLock> locks = new ConcurrentHashMap<K, ReadWriteLock>();
  private Set<K> loadInProgressKeys = Collections.synchronizedSet(new HashSet<K>());
  
  public AbstractSynchronizedCacheStrategyDecorator(final CacheStrategy<K, V> decorated) {
    super(decorated);
  }
  
  @Override
  public final V get(final K key) {
    notNull(key);
    LOG.debug("Searching cache key: {}", key);
    V value = null;
    // invoke this callback method before the lock is acquired to avoid dead-lock
    onBeforeGet(key);
    final ReadWriteLock lock = getLockForKey(key);
    lock.readLock().lock();
    try {
      value = getDecoratedObject().get(key);
    } finally {
      lock.readLock().unlock();
    }
    if (isUpdateRequired(key, value)) {
      value = updateValueForKey(key, value, lock);
    }
    return value;
  }
  
  private V updateValueForKey(final K key, final V oldValue, final ReadWriteLock lock) {
    V newValue = oldValue;
    if (!loadInProgressKeys.contains(key)) {
      loadInProgressKeys.add(key);
      try {
        if (isUpdateRequired(key, oldValue)) {
          // put old value to clear stale flag
          put(key, oldValue);
          // prevent subsequent loads
          LOG.debug("Loading new value...");
          newValue = loadValue(key);
          // perform sync load only if the old value is not available
          put(key, newValue);
        }
      } finally {
        loadInProgressKeys.remove(key);
      }
    }
    return newValue;
  }
  
  private boolean isUpdateRequired(K key, V value) {
    return value == null || isStale(key);
  }
  
  /**
   * Invoked just before the get method is invoked. Can be useful for checking if resources are stale and invalidating
   * the cache.
   */
  protected void onBeforeGet(final K key) {
  }
  
  @Override
  public final void put(final K key, final V value) {
    final ReadWriteLock lock = getLockForKey(key);
    lock.writeLock().lock();
    try {
      getDecoratedObject().put(key, value);
    } finally {
      lock.writeLock().unlock();
    }
  }
  
  /**
   * Ensure that the returned lock will never be null.
   */
  private ReadWriteLock getLockForKey(final K key) {
    final ReadWriteLock lock = locks.putIfAbsent(key, new ReentrantReadWriteLock(true));
    return lock == null ? locks.get(key) : lock;
  }
  
  /**
   * Loads the value associated with the key. This is a potential slow operation which will be prevented to be called
   * multiple times for the same key.
   */
  protected abstract V loadValue(final K key);
}
