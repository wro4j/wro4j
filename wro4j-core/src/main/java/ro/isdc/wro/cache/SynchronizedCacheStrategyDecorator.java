package ro.isdc.wro.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles multiple writers synchronization, required for performance improvements.  
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
  public V get(K key) {
    V value = null;
    
    LOG.debug("getKey: {}", key);
    final ReadWriteLock lock = getLockForKey(key);
    LOG.debug("lock: {}", lock);
    lock.readLock().lock();
    LOG.debug("before READ: {}", lock);
    value = getDecoratedObject().get(key);
    LOG.debug("after READ value {} for lock: {}", value, lock);
    try {
      if (value == null) {
        //lock.readLock().unlock();
        put(key, loadValue(key));
        //lock.readLock().lock();
        value = getDecoratedObject().get(key);
      }
      LOG.debug("return value {} for lock: {}", value, lock);
      return value;
    } finally {
      lock.readLock().unlock();
      LOG.debug("after READ released: {}", lock);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void put(K key, V value) {
    final ReadWriteLock lock = getLockForKey(key);
    LOG.debug("before WRITE: {}", lock);
    lock.writeLock().lock();
    LOG.debug("after WRITE: {}", lock);
    try {
      getDecoratedObject().put(key, value);
    } finally {
      lock.writeLock().unlock();
      LOG.debug("after WRITE released: {}", lock);
    }
  };

  /**
   * Ensure that the returned lock will never be null.
   */
  private ReadWriteLock getLockForKey(final K key) {
    final ReadWriteLock lock = locks.putIfAbsent(key, new ReentrantReadWriteLock(false));
    try {
      return lock == null ? locks.get(key) : lock;
    } finally {
      LOG.debug("getLockForKey {} -> {}", key, lock);
    }
  }
  
  /**
   * Loads the value associated with the key. 
   */
  protected abstract V loadValue(final K key);
}
