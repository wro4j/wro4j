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
  private static final int MAX_ACTIVE = 2;
  private static final int MAX_IDLE = 1;
  private static final byte EXHAUSTED_ACTION = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
  private static final long MAX_WAIT = 1000L * 5L;
  // Allows using the objects from the pool in a thread-safe fashion.
  private GenericObjectPool objectPool;


  public ObjectPoolHelper(final ObjectFactory<T> objectFactory) {
    Validate.notNull(objectFactory);
    objectPool = new GenericObjectPool(new BasePoolableObjectFactory() {
      @Override
      public Object makeObject()
        throws Exception {
        return objectFactory.create();
      }
    }, MAX_ACTIVE, EXHAUSTED_ACTION, MAX_WAIT, MAX_IDLE);
  }


  @SuppressWarnings("unchecked")
  public T getObject() {
    try {
      return (T) objectPool.borrowObject();
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
}
