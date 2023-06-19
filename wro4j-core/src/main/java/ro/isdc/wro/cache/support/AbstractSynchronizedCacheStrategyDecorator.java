package ro.isdc.wro.cache.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;


/**
 * Ensure that the {@link AbstractSynchronizedCacheStrategyDecorator#loadValue(Object)} will be called only once for the same
 * key. This behavior is important for avoiding redundant execution of expensive computation in concurrent environment
 * which cause high memory and CPU consumption.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public abstract class AbstractSynchronizedCacheStrategyDecorator<K, V>
    extends CacheStrategyDecorator<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSynchronizedCacheStrategyDecorator.class);
  private final ConcurrentMap<K, ReadWriteLock> locks = new ConcurrentHashMap<K, ReadWriteLock>();

  public AbstractSynchronizedCacheStrategyDecorator(final CacheStrategy<K, V> decorated) {
    super(decorated);
  }

  @Override
  public final V get(final K key) {
    notNull(key);
    LOG.debug("Searching cache key: {}", key);
    V value = null;
    //invoke this callback method before the lock is acquired to avoid dead-lock
    onBeforeGet(key);
    final ReadWriteLock lock = getLockForKey(key);
    lock.readLock().lock();
    try {
      value = getDecoratedObject().get(key);
    } finally {
      lock.readLock().unlock();
    }
    if (value == null) {
      lock.writeLock().lock();
      try {
        // this is necessary to ensure that the load wasn't invoked
        value = getDecoratedObject().get(key);
        if (value == null) {
          LOG.debug("Cache is empty. Loading new value...");
          value = loadValue(key);
          put(key, value);
        }
      } finally {
        lock.writeLock().unlock();
      }
    }
    return value;
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
    final ReadWriteLock lock = locks.putIfAbsent(key, new ReentrantReadWriteLock());
    return lock == null ? locks.get(key) : lock;
  }

  /**
   * Loads the value associated with the key. This is a potential slow operation which will be prevented to be called
   * multiple times for the same key.
   */
  protected abstract V loadValue(final K key);
}
