package ro.isdc.wro.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Ensure that the {@link SynchronizedCacheStrategyDecorator#loadValue(Object)} will be called only once for the same
 * key. This behavior is important for avoiding redundant execution of expensive computation in concurrent environment
 * which cause high memory and CPU consumption.
 * 
 * @author Alex Objelean
 * @created 30 Apr 2012
 * @since 1.4.6
 */
public abstract class SynchronizedCacheStrategyDecorator<K, V>
    extends CacheStrategyDecorator<K, V> {
  private static final Logger LOG = LoggerFactory.getLogger(SynchronizedCacheStrategyDecorator.class);
  private ConcurrentMap<K, ReadWriteLock> locks = new ConcurrentHashMap<K, ReadWriteLock>();
  
  public SynchronizedCacheStrategyDecorator(final CacheStrategy<K, V> decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public V get(final K key) {
    Validate.notNull(key);
    LOG.debug("Searching cache key: {}", key);
    V value = null;
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
          LOG.debug("Cache is empty. Perform processing...");
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
   * {@inheritDoc}
   */
  public void put(K key, V value) {
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
