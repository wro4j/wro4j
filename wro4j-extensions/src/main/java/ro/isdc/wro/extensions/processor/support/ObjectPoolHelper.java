/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support;

import static org.apache.commons.lang3.Validate.notNull;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.ObjectFactory;


/**
 * A generic aware object pool wrapper. Probably not the best name, but it can be changed later. It helps you to avoid
 * the cast and hides the exception handling by throwing {@link RuntimeException} when borrowing or returning object to
 * the pool fails.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public class ObjectPoolHelper<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ObjectPoolHelper.class);

  private static final int MAX_IDLE = 5;
  private static final long MAX_WAIT = 10L * DateUtils.MILLIS_PER_SECOND;
  private static final long EVICTABLE_IDLE_TIME = 30L * DateUtils.MILLIS_PER_SECOND;

  // Allows using the objects from the pool in a thread-safe fashion.
  private GenericObjectPool<T> objectPool;

  public ObjectPoolHelper(final ObjectFactory<T> objectFactory) {
    notNull(objectFactory);
    objectPool = createObjectPool(objectFactory);
    notNull(objectPool);
  }

  /**
   * Ensure that a not null pool will be created.
   */
  private GenericObjectPool<T> createObjectPool(final ObjectFactory<T> objectFactory) {
    final GenericObjectPool<T> pool = newObjectPool(objectFactory);
    notNull(pool);
    return pool;
  }

  /**
   * Creates a {@link GenericObjectPool}. Override this method to set custom objectPool configurations.
   */
  protected GenericObjectPool<T> newObjectPool(final ObjectFactory<T> objectFactory) {
    final int maxActive = Math.max(2, Runtime.getRuntime().availableProcessors());
    final GenericObjectPool<T> pool = new GenericObjectPool<T>(new BasePooledObjectFactory<T>() {
        @Override
        public T create() throws Exception {
		    return objectFactory.create();
	    }

        @Override
        public PooledObject<T> wrap(T obj) {
		    return new DefaultPooledObject<T>(obj);
	    }
    });
    pool.setMaxTotal(maxActive);
    pool.setMaxIdle(MAX_IDLE);
    pool.setMaxWaitMillis(MAX_WAIT);
    /**
     * Block when exhausted, otherwise the pool object retrieval can fail. More details here:
     * <a>http://code.google.com/p/wro4j/issues/detail?id=364</a>
     */
    pool.setBlockWhenExhausted(true);
    // make object eligible for eviction after a predefined amount of time.
    pool.setSoftMinEvictableIdleTimeMillis(EVICTABLE_IDLE_TIME);
    pool.setTimeBetweenEvictionRunsMillis(EVICTABLE_IDLE_TIME);
    return pool;
  }

  /**
   * @return object from the pool.
   */
  public T getObject() {
    try {
      return objectPool.borrowObject();
    } catch (final Exception e) {
      // should never happen
      throw new RuntimeException("Cannot get object from the pool", e);
    }
  }


  public void returnObject(final T engine) {
    notNull(engine);
    try {
      objectPool.returnObject(engine);
    } catch (final Exception e) {
      // should never happen
      throw new RuntimeException("Cannot get object from the pool", e);
    }
  }

  /**
   * Use a custom {@link GenericObjectPool}.
   *
   * @param objectPool
   *          to use.
   */
  public final void setObjectPool(final GenericObjectPool<T> objectPool) {
    notNull(objectPool);
    this.objectPool = objectPool;
  }

  /**
   * Close the object pool to avoid the memory leak.
   *
   * @throws Exception
   *           if the close operation failed.
   */
  public void destroy() throws Exception {
    LOG.debug("closing objectPool");
    objectPool.close();
  }
}
