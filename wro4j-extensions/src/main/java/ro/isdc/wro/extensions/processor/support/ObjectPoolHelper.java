/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support;

import org.apache.commons.lang3.Validate;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import ro.isdc.wro.util.ObjectFactory;


/**
 * A generic aware object pool wrapper. Probably not the best name, but it can be changed later. It helps you to avoid
 * the cast and hides the exception handling by throwing {@link RuntimeException} when borrowing or returning object to
 * the pool fails.
 *
 * @author Alex Objelean
 * @created 10 Nov 2011
 * @since 1.4.2
 */
public class ObjectPoolHelper<T> {
  private static final int MAX_IDLE = 2;
  private static final long MAX_WAIT = 5L * 1000L;
  private static final long EVICTABLE_IDLE_TIME = 30 * 1000L;
  // Allows using the objects from the pool in a thread-safe fashion.
  private GenericObjectPool<T> objectPool;


  public ObjectPoolHelper(final ObjectFactory<T> objectFactory) {
    Validate.notNull(objectFactory);
    objectPool = newObjectPool(objectFactory);
    Validate.notNull(objectPool);
  }

  /**
   * Creates a {@link GenericObjectPool}. Override this method to set custom objectPool configurations.
   */
  protected GenericObjectPool<T> newObjectPool(final ObjectFactory<T> objectFactory) {
    final int maxActive = Math.max(2, Runtime.getRuntime().availableProcessors());
    final GenericObjectPool<T> objectPool = new GenericObjectPool<T>(new BasePoolableObjectFactory<T>() {
      @Override
      public T makeObject()
        throws Exception {
        return objectFactory.create();
      }
    });
    objectPool.setMaxActive(maxActive);
    objectPool.setMaxIdle(MAX_IDLE);
    objectPool.setMaxWait(MAX_WAIT);
    /**
     * Use WHEN_EXHAUSTED_GROW strategy, otherwise the pool object retrieval can fail. More details here:
     * <a>http://code.google.com/p/wro4j/issues/detail?id=364</a>
     */
    objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
    // make object elligible for eviction after a predefined amount of time.
    objectPool.setSoftMinEvictableIdleTimeMillis(EVICTABLE_IDLE_TIME);
    objectPool.setTimeBetweenEvictionRunsMillis(EVICTABLE_IDLE_TIME);
    return objectPool;
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
    Validate.notNull(engine);
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
    Validate.notNull(objectPool);
    this.objectPool = objectPool;
  }
}
